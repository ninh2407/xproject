package vn.tienlo.api.account.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tienlo.api.account.persistence.AccountIdempotencyRepository;
import vn.tienlo.api.account.persistence.AccountRepository;
import vn.tienlo.api.character.api.CharacterAccountQuery;
import vn.tienlo.api.character.api.CharacterAccountQuery.CharacterSummaryView;
import vn.tienlo.api.security.api.RequestFingerprint;
import vn.tienlo.api.security.api.SessionRevocationGate;
import vn.tienlo.api.platform.api.ApiFailureException;

@Service
@Transactional(readOnly = true)
public class AccountService {
    private static final String SETTINGS_ENDPOINT = "/v1/accounts/me/settings";
    private static final String DELETION_ENDPOINT = "/v1/accounts/me/deletion-requests";
    private static final TypeReference<AccountOperationResult<AccountProfileView>> PROFILE_RESULT_TYPE =
            new TypeReference<>() {};
    private static final TypeReference<AccountOperationResult<DeletionAcceptedView>> DELETION_RESULT_TYPE =
            new TypeReference<>() {};

    private final AccountRepository accountRepository;
    private final CharacterAccountQuery characterAccountQuery;
    private final AccountIdempotencyRepository idempotencyRepository;
    private final RequestFingerprint requestFingerprint;
    private final SessionRevocationGate sessionRevocationGate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AccountService(
            AccountRepository accountRepository,
            CharacterAccountQuery characterAccountQuery,
            AccountIdempotencyRepository idempotencyRepository,
            RequestFingerprint requestFingerprint,
            SessionRevocationGate sessionRevocationGate,
            ObjectMapper objectMapper,
            Clock clock) {
        this.accountRepository = accountRepository;
        this.characterAccountQuery = characterAccountQuery;
        this.idempotencyRepository = idempotencyRepository;
        this.requestFingerprint = requestFingerprint;
        this.sessionRevocationGate = sessionRevocationGate;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public AccountProfileView getProfile(UUID accountId) {
        var account = accountRepository
                .findById(accountId)
                .orElseThrow(() -> new ApiFailureException(404, "NOT_FOUND", "err.not_found", null));
        return profile(account.getId(), account.getStatus(), account.getLocale(), account.getSettings(),
                account.getCreatedAt(), account.getDisplayHandle());
    }

    public List<CharacterSummaryView> listCharacters(UUID accountId) {
        return characterAccountQuery.listByAccountId(accountId);
    }

    @Transactional
    public AccountOperationResult<AccountProfileView> updateSettings(
            UUID accountId,
            String idempotencyKey,
            AccountSettingsPatchCommand patch,
            UUID requestId) {
        byte[] requestHash = requestFingerprint.hash(SETTINGS_ENDPOINT, accountId.toString(), fingerprintBody(patch));
        var attempt = idempotencyRepository.begin(SETTINGS_ENDPOINT, idempotencyKey, requestHash);
        if (attempt.state() == AccountIdempotencyRepository.AttemptState.MISMATCH) {
            return AccountOperationResult.failure(
                    422, new AccountError("IDEMPOTENCY_MISMATCH", "err.idempotency_mismatch", requestId));
        }
        if (attempt.state() == AccountIdempotencyRepository.AttemptState.REPLAY) {
            return replay(attempt.response(), PROFILE_RESULT_TYPE);
        }

        AccountOperationResult<AccountProfileView> result;
        if (patch.hideNameInBroadcast() == null && patch.locale() == null) {
            result = AccountOperationResult.failure(
                    400, new AccountError("VALIDATION", "err.validation", requestId));
        } else {
            var account = accountRepository.findByIdForUpdate(accountId).orElse(null);
            if (account == null) {
                result = AccountOperationResult.failure(
                        404, new AccountError("NOT_FOUND", "err.not_found", requestId));
            } else {
                JsonNode currentSettings = account.getSettings();
                if (!currentSettings.isObject()) {
                    throw new IllegalStateException("Account settings must be a JSON object");
                }
                ObjectNode settings = currentSettings.deepCopy();
                if (patch.hideNameInBroadcast() != null) {
                    settings.put("hideNameInBroadcast", patch.hideNameInBroadcast());
                }
                if (patch.locale() != null) {
                    account.setLocale(patch.locale());
                }
                account.setSettings(settings);
                accountRepository.save(account);
                result = AccountOperationResult.success(
                        200,
                        profile(
                                account.getId(),
                                account.getStatus(),
                                account.getLocale(),
                                settings,
                                account.getCreatedAt(),
                                account.getDisplayHandle()));
            }
        }
        idempotencyRepository.complete(SETTINGS_ENDPOINT, idempotencyKey, result.httpStatus(), result);
        return result;
    }

    @Transactional
    public AccountOperationResult<DeletionAcceptedView> requestDeletion(
            UUID accountId, String idempotencyKey, UUID requestId) {
        byte[] requestHash = requestFingerprint.hash(DELETION_ENDPOINT, accountId.toString());
        var attempt = idempotencyRepository.begin(DELETION_ENDPOINT, idempotencyKey, requestHash);
        if (attempt.state() == AccountIdempotencyRepository.AttemptState.MISMATCH) {
            return AccountOperationResult.failure(
                    422, new AccountError("IDEMPOTENCY_MISMATCH", "err.idempotency_mismatch", requestId));
        }
        if (attempt.state() == AccountIdempotencyRepository.AttemptState.REPLAY) {
            return replay(attempt.response(), DELETION_RESULT_TYPE);
        }

        var account = accountRepository.findByIdForUpdate(accountId).orElse(null);
        AccountOperationResult<DeletionAcceptedView> result;
        if (account == null) {
            result = AccountOperationResult.failure(
                    404, new AccountError("NOT_FOUND", "err.not_found", requestId));
        } else {
            if (!"DELETED".equals(account.getStatus())) {
                account.setLoginEmail("deleted+" + accountId + "@example.invalid");
                account.setDisplayHandle(null);
                account.setStatus("DELETED");
                account.setDeletedAt(clock.instant());
                accountRepository.save(account);
            }
            sessionRevocationGate.revokeAndDeleteAll(accountId);
            result = AccountOperationResult.success(202, new DeletionAcceptedView("REQUESTED"));
        }
        idempotencyRepository.complete(DELETION_ENDPOINT, idempotencyKey, result.httpStatus(), result);
        return result;
    }

    private <T> AccountOperationResult<T> replay(JsonNode response, TypeReference<AccountOperationResult<T>> type) {
        try {
            return objectMapper.readValue(objectMapper.writeValueAsBytes(response), type);
        } catch (IOException exception) {
            throw new IllegalStateException("Stored account idempotency response has an invalid shape", exception);
        }
    }

    private String fingerprintBody(AccountSettingsPatchCommand patch) {
        try {
            return objectMapper.writeValueAsString(patch);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not fingerprint account settings request", exception);
        }
    }

    private static AccountProfileView profile(
            UUID accountId, String status, String locale, JsonNode settings, Instant createdAt, String displayHandle) {
        return new AccountProfileView(accountId, status, locale, settings, createdAt, displayHandle);
    }

    public record AccountProfileView(
            UUID accountId, String status, String locale, JsonNode settings, Instant createdAt, String displayHandle) {}

    public record DeletionAcceptedView(String status) {}

    public record AccountSettingsPatchCommand(Boolean hideNameInBroadcast, String locale) {}

    public record AccountError(String code, String messageKey, UUID requestId) {}

    public record AccountOperationResult<T>(int httpStatus, T value, AccountError error) {
        public static <T> AccountOperationResult<T> success(int httpStatus, T value) {
            return new AccountOperationResult<>(httpStatus, value, null);
        }

        public static <T> AccountOperationResult<T> failure(int httpStatus, AccountError error) {
            return new AccountOperationResult<>(httpStatus, null, error);
        }
    }
}
