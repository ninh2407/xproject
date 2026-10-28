import { readFileSync } from "node:fs";
import { spawnSync } from "node:child_process";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("../..", import.meta.url)));
const contracts = resolve(root, "shared/contracts");
const errorDocument = readFileSync(resolve(contracts, "errors.yaml"), "utf8");
const rngDocument = readFileSync(resolve(contracts, "rng-kinds.yaml"), "utf8");

function entries(document, field) {
  return [...document.matchAll(new RegExp(`^  - ${field}: ([A-Z][A-Z0-9_]*)$`, "gm"))].map((match) => match[1]);
}

function assertUnique(values, label) {
  if (values.length === 0 || new Set(values).size !== values.length) {
    throw new Error(`${label} must contain unique entries.`);
  }
}

const errorCodes = entries(errorDocument, "code");
const errorMessageKeys = [...errorDocument.matchAll(/^    messageKey: (err\.[a-z0-9_]+)$/gm)].map((match) => match[1]);
assertUnique(errorCodes, "errors.yaml codes");
if (errorCodes.length !== errorMessageKeys.length) {
  throw new Error("Every error code must define a messageKey (API-R-041).");
}

const rngKinds = entries(rngDocument, "kind");
assertUnique(rngKinds, "rng-kinds.yaml kinds");
if (/^    class: (?![ABCDE]$).+$/m.test(rngDocument) ||
    /^    source: (?!API_SERVICE$|GAME_SERVER$).+$/m.test(rngDocument)) {
  throw new Error("rng-kinds.yaml contains an invalid class or source (TD-04 §8).");
}

const command = resolve(root, "node_modules/@openapitools/openapi-generator-cli/main.js");
for (const name of ["public", "internal", "admin"]) {
  const result = spawnSync(process.execPath, [
    command,
    "validate",
    "-i",
    `shared/contracts/${name}.yaml`
  ], {
    cwd: root,
    stdio: "inherit"
  });
  if (result.error) throw result.error;
  if (result.status !== 0) throw new Error(`OpenAPI validation failed for ${name}.yaml.`);
}

console.log(`Validated ${errorCodes.length} error codes, ${rngKinds.length} RNG kinds, and 3 OpenAPI contracts.`);
