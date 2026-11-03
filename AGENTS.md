# AI / contributor instructions

## Read first

1. `docs/README.md` and `docs/lore/glossary.md`
2. `docs/tech/00-tdd.md` and `docs/tech/09-conventions.md`
3. The TD/GD referenced by the task, then the code in its package

## Repository

- TypeScript workspace: `client/`, `game-server/`, `shared/`; Java API: `api-service/`.
- Contracts live in `shared/contracts`; gamedata types, source data, and tools live in `shared/gamedata-types`, `shared/gamedata`, and `tools/gamedata`.
- Follow the spec before changing behavior. Do not invent endpoints, enums, error codes, gamedata fields, or gameplay constants.
- `shared/sim` is pure TypeScript and cannot depend on browser, Node.js, Babylon.js, React, or Colyseus APIs.
- Every persistent API write must follow the idempotency, ledger, and transaction rules in TD-01. API contracts are changed before implementation.
- Keep simulation and data code deterministic; see GEO-R-003 and TD-07.
- Add/update a package README when adding a workspace package.

## Commands

```sh
pnpm install
pnpm contracts:check
pnpm contracts:gen
pnpm gamedata:check
pnpm -r build
pnpm -r test
pnpm lint
pnpm license:check
cd api-service && ./gradlew check
docker compose -f infra/docker-compose.yml up --build
```

Generated files under `shared/gamedata-build/` and `api-service/build/` are not committed.

## Change checklist

- Cite the relevant spec rule/acceptance criterion in implementation and test names.
- Run the smallest relevant test, then all workspace checks for cross-package changes.
- Keep dependency versions exact and add direct dependencies to `docs/tech/14-oss-register.md` before use.
- If the user explicitly asks to skip unit tests to save effort, honor that for the requested task; use compile/build or configuration checks instead and report that tests were skipped. Do not remove tests or disable CI checks.