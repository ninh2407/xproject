# API Service

Spring Boot 3 modular monolith, Java 21. PostgreSQL access is configured with Spring Data JPA for simple CRUD and jOOQ for hot queries (TD-10 CC-R-065).

Package root: `vn.tienlo.api`. Each business module owns `api`, `web`, `app`, `domain`, `persistence`, and `events` subpackages.

## Local authentication keys

The service requires an Ed25519 JWT key pair and a 256-bit HMAC key. Do not commit these values or reuse local keys in deployed environments. From PowerShell, create local-only keys and export their DER encodings as Base64:

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

Run `.\gradlew.bat bootRun` from this directory. `POST /v1/auth/login` authenticates an existing `game.account`, creates an `account_session`, and returns an Ed25519 access token plus an opaque refresh token. Account registration, refresh rotation, Valkey-backed login throttling, and CAPTCHA are separate follow-up flows.

## Build and test

Run `.\gradlew.bat compileJava` to verify the API service without running unit tests.
