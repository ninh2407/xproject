# API Service

Spring Boot 3 modular monolith, Java 21. PostgreSQL access is configured with Spring Data JPA for simple CRUD and jOOQ for hot queries (TD-10 CC-R-065).

Package root: `vn.tienlo.api`. Each business module owns `api`, `web`, `app`, `domain`, `persistence`, and `events` subpackages.

## Local authentication keys

The service requires an Ed25519 JWT key pair and a 256-bit HMAC key. The HMAC key also derives a separate AES-GCM key for encrypted auth idempotency responses; keep it stable while those records are retained. Auth refresh tokens are sent only as the `__Secure-xproject_refresh` HttpOnly, Secure, SameSite=Strict cookie. Browser clients must send credentialed requests; configure `TL_CORS_ALLOWED_ORIGINS` as a comma-separated allowlist of exact game origins (never `*`). CAPTCHA is provider-neutral: configure `TL_CAPTCHA_VERIFICATION_URI` and `TL_CAPTCHA_SECRET` for a verifier accepting `{ secret, response, remoteIp }` and returning `{ success }`. Registration always verifies CAPTCHA; login requires it after five failed attempts. Missing verifier configuration fails closed with `503 UNAVAILABLE`. Do not commit secrets or reuse local keys in deployed environments. From PowerShell, create local-only keys and export their DER encodings as Base64:

```powershell
Set-Location api-service
openssl genpkey -algorithm ED25519 -out jwt-private.pem
openssl pkey -in jwt-private.pem -pubout -out jwt-public.pem
openssl pkcs8 -topk8 -nocrypt -in jwt-private.pem -outform DER -out jwt-private.der
openssl pkey -pubin -in jwt-public.pem -outform DER -out jwt-public.der
$env:TL_JWT_KEY_ID = "local-dev-1"
$env:TL_JWT_PRIVATE_KEY = [Convert]::ToBase64String([IO.File]::ReadAllBytes(".\jwt-private.der"))
$env:TL_JWT_PUBLIC_KEY = [Convert]::ToBase64String([IO.File]::ReadAllBytes(".\jwt-public.der"))
$env:TL_AUTH_IDEMPOTENCY_HMAC_KEY = node -p "require('node:crypto').randomBytes(32).toString('base64')"
```

Set the same values in the ignored root `.env` file when using Docker Compose. Store deployed private/HMAC keys in the secret manager. Startup fails when required keys are absent or malformed.

## Database connection

The service uses Spring Boot's PostgreSQL `DataSource` auto-configuration. Set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` for the target database; Docker Compose supplies these values for local development. Flyway keeps its history in `api`, while all game tables are explicitly created in `game`; JPA is restricted to `game` with schema generation disabled, and jOOQ is configured for PostgreSQL.

Run `node tools/db/generate-jpa.mjs` from the repository root to regenerate entity mappings and repositories from the registered tables' documented DDL. It generates mappings for all 118 registered tables, including the security and purchase-receipt DDL documented in TD-08. Append-only ledger repositories intentionally expose read methods only; ledger inserts belong in transactional application use cases.

Flyway applies `V1__baseline.sql`, the existing authentication migration, and timestamped module migrations for the complete 118-table PostgreSQL registry. The DDL is sourced from TD-03 and its owning TD/GD storage sections; `telemetry_event` is intentionally excluded because GD-20 assigns it to a separate ClickHouse/analytics store. Existing versioned migrations are preserved.

The `rng_audit` and `pvp_kill_log` partition parents start with default partitions so writes remain valid before DB-R-040 partition maintenance provisions time ranges.

The jOOQ repositories cover the hot queries currently specified in TD-03: conditional character snapshot updates, auction listing search, and item history lookup. Other straightforward persistence operations use Spring Data JPA.

`compileJava` runs `openApiGenerate` first and generates Java interfaces for public, internal, and admin contracts. The auth controller implements the generated public `AuthApi` interface for register, login, refresh, logout, and logout-all. Valkey enforces the auth IP token bucket, login CAPTCHA threshold, account lockout, and IP failure block. Refresh-token reuse revokes all account sessions; revoked access-token session IDs are rejected, and `account.sessions_revoked` is stored in the transactional outbox and relayed to `evt:account`.

## Build and test

Run `.\gradlew.bat compileJava` to compile the API service. Focused auth acceptance checks: `.\gradlew.bat test --tests "vn.tienlo.api.security.app.AuthServiceTest" --tests "vn.tienlo.api.security.web.AuthLoginControllerTest" --tests "vn.tienlo.api.security.ApiSecurityConfigurationTest"`.
