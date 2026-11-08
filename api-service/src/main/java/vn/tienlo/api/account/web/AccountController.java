package vn.tienlo.api.account.web;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import vn.tienlo.api.account.app.AccountService;
import vn.tienlo.api.account.app.AccountService.AccountOperationResult;
import vn.tienlo.api.character.api.CharacterAccountQuery.CharacterSummaryView;
import vn.tienlo.api.generated.publicapi.AccountApi;
import vn.tienlo.api.generated.publicapi.model.AccountCharacters;
import vn.tienlo.api.generated.publicapi.model.AccountProfile;
import vn.tienlo.api.generated.publicapi.model.AccountSettingsPatch;
import vn.tienlo.api.generated.publicapi.model.CharacterSummary;
import vn.tienlo.api.generated.publicapi.model.DeletionRequestAccepted;
import vn.tienlo.api.platform.api.ApiFailureException;

@RestController
@Validated
// API-R-011: bind all account operations through the generated contract interface.
public class AccountController implements AccountApi {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public ResponseEntity<AccountProfile> accountGetCurrent(UUID xRequestId, String traceparent) {
        UUID accountId = authenticatedAccountId();
        return ResponseEntity.ok(toProfile(accountService.getProfile(accountId)));
    }

    @Override
    public ResponseEntity<AccountCharacters> accountListCharacters(UUID xRequestId, String traceparent) {
        var characters = accountService
                .listCharacters(authenticatedAccountId())
                .stream()
                .map(AccountController::toSummary)
                .toList();
        return ResponseEntity.ok(new AccountCharacters().characters(characters));
    }

    @Override
    public ResponseEntity<DeletionRequestAccepted> accountRequestDeletion(
            UUID xRequestId, String traceparent, String idempotencyKey) {
        var result = accountService.requestDeletion(authenticatedAccountId(), idempotencyKey, xRequestId);
        throwIfFailure(result);
        return ResponseEntity.status(result.httpStatus())
                .body(new DeletionRequestAccepted(
                        DeletionRequestAccepted.StatusEnum.fromValue(result.value().status())));
    }

    @Override
    public ResponseEntity<AccountProfile> accountUpdateSettings(
            UUID xRequestId,
            String traceparent,
            String idempotencyKey,
            AccountSettingsPatch accountSettingsPatch) {
        var command = new AccountService.AccountSettingsPatchCommand(
                accountSettingsPatch.getHideNameInBroadcast(), accountSettingsPatch.getLocale());
        var result = accountService.updateSettings(
                authenticatedAccountId(), idempotencyKey, command, xRequestId);
        throwIfFailure(result);
        return ResponseEntity.status(result.httpStatus()).body(toProfile(result.value()));
    }

    private static void throwIfFailure(AccountOperationResult<?> result) {
        if (result.error() != null) {
            throw new ApiFailureException(
                    result.httpStatus(),
                    result.error().code(),
                    result.error().messageKey(),
                    result.error().requestId());
        }
    }

    private static AccountProfile toProfile(AccountService.AccountProfileView account) {
        return new AccountProfile(
                        account.accountId(),
                        AccountProfile.StatusEnum.fromValue(account.status()),
                        account.locale(),
                        account.settings(),
                        OffsetDateTime.ofInstant(account.createdAt(), ZoneOffset.UTC))
                .displayHandle(account.displayHandle());
    }

    private static CharacterSummary toSummary(CharacterSummaryView character) {
        return new CharacterSummary(
                        character.id(),
                        character.name(),
                        character.serverId().intValue(),
                        character.generation(),
                        character.backgroundId(),
                        CharacterSummary.LifeStateEnum.fromValue(character.lifeState()))
                .realm(character.realm())
                .realmStage(character.realmStage())
                .endCause(character.endCause());
    }

    private static UUID authenticatedAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication)) {
            throw new ApiFailureException(401, "UNAUTHENTICATED", "err.unauthenticated", null);
        }
        try {
            return UUID.fromString(jwtAuthentication.getToken().getSubject());
        } catch (IllegalArgumentException exception) {
            throw new ApiFailureException(401, "UNAUTHENTICATED", "err.unauthenticated", null);
        }
    }
}
