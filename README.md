# XProject

XProject is a multiplayer game project organized as a pnpm TypeScript workspace and a Java 21 Spring Boot API service.

## Prerequisites

- Node.js 22+
- pnpm 10.13.0 (`corepack enable`)
- Java 21
- Docker Compose (for local PostgreSQL and Valkey)

## Build and validate

```sh
pnpm install
pnpm contracts:check
pnpm contracts:gen
pnpm gamedata:check
pnpm -r build
pnpm -r test
pnpm lint
```

For the API service, run `cd api-service; .\gradlew.bat check` in Windows PowerShell, or `cd api-service && ./gradlew check` on macOS/Linux.

## OpenAPI mocks

Run the public contract mock on port 4010 with `pnpm mock:api`, or the internal mock on port 4011 with `pnpm mock:api:internal`.

See [AGENTS.md](AGENTS.md) for project-specific instructions and package entry points. Specifications live under `docs/`.
