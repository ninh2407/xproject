# API contracts

OpenAPI is the source of truth for public, internal, and admin HTTP APIs (TD-04 API-R-001/010/011). Validate with `pnpm contracts:check`; generate TypeScript Fetch clients and Java Spring interfaces with `pnpm contracts:gen`.

Do not implement an endpoint before adding it here. Generated output is placed in ignored build directories.

Run the local Prism mock servers with `pnpm mock:api` (public, port 4010) or `pnpm mock:api:internal` (internal, port 4011).
