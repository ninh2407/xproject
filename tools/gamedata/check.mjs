import Ajv from "ajv";
import { createHash } from "node:crypto";
import { readFileSync, readdirSync, mkdirSync, writeFileSync, copyFileSync } from "node:fs";
import { resolve, relative, sep } from "node:path";
import { fileURLToPath } from "node:url";
import { spawnSync } from "node:child_process";
import { generateSchemas } from "./generate.mjs";

const root = resolve(fileURLToPath(new URL("../..", import.meta.url)));
const dataRoot = resolve(root, "shared/gamedata");
const buildRoot = resolve(root, "shared/gamedata-build");
const registry = JSON.parse(readFileSync(resolve(dataRoot, "registry.json"), "utf8"));
const ajv = new Ajv({ allErrors: true, strict: true });
const files = new Set(registry.files.map((entry) => entry.path));
const errors = [];

function canonicalize(value) {
  if (Array.isArray(value)) return value.map(canonicalize);
  if (value !== null && typeof value === "object") {
    return Object.fromEntries(
      Object.keys(value).sort().map((key) => [key, canonicalize(value[key])])
    );
  }
  return value;
}

function findJsonFiles(directory) {
  return readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const path = resolve(directory, entry.name);
    if (entry.isDirectory()) return findJsonFiles(path);
    if (directory === dataRoot) return [];
    return entry.isFile() && entry.name.endsWith(".json") && entry.name !== "registry.json" ? [path] : [];
  });
}

generateSchemas();

for (const path of findJsonFiles(dataRoot)) {
  const projectPath = relative(dataRoot, path).split(sep).join("/");
  if (!files.has(projectPath)) errors.push(`[REGISTRY] ${projectPath} is not registered (DT-R-100).`);
}

const loaded = new Map();
for (const entry of registry.files) {
  const file = resolve(dataRoot, entry.path);
  const schemaFile = resolve(buildRoot, "schema", `${entry.type}.schema.json`);
  if (!file.startsWith(`${dataRoot}${sep}`) || !schemaFile.startsWith(`${buildRoot}${sep}`)) {
    errors.push(`[REGISTRY] ${entry.path} resolves outside the registered data/schema directories.`);
    continue;
  }
  let value;
  let schema;
  try {
    value = JSON.parse(readFileSync(file, "utf8"));
    schema = JSON.parse(readFileSync(schemaFile, "utf8"));
  } catch (error) {
    errors.push(`[JSON] ${entry.path}: ${error instanceof Error ? error.message : String(error)}`);
    continue;
  }
  const validate = ajv.compile(schema);
  if (!validate(value)) {
    for (const issue of validate.errors ?? []) {
      errors.push(`[SCHEMA] ${entry.path}${issue.instancePath} ${issue.message ?? "invalid value"} (${issue.schemaPath})`);
    }
  } else {
    loaded.set(entry.path, value);
  }
}

if (errors.length > 0) {
  console.error(errors.join("\n"));
  process.exitCode = 1;
} else {
  const serverData = {};
  const clientData = {};
  for (const entry of registry.files) {
    const value = loaded.get(entry.path);
    if (value === undefined) continue;
    const target = entry.visibility === "client" ? clientData : serverData;
    target[entry.path] = value;
    serverData[entry.path] = value;
  }
  const profileIndex = process.argv.indexOf("--profile");
  const profile = process.argv.find((argument) => argument.startsWith("--profile="))?.slice("--profile=".length) ??
    (profileIndex >= 0 ? process.argv[profileIndex + 1] : undefined) ??
    "development";
  const normalizedServer = JSON.stringify(canonicalize(serverData));
  const version = createHash("sha256").update(`${normalizedServer}\n${profile}`).digest("hex").slice(0, 12);
  const normalizedClient = JSON.stringify(canonicalize(clientData));
  const output = resolve(buildRoot, "dist");
  mkdirSync(output, { recursive: true });
  const serverBundle = resolve(output, "bundle.server.json");
  const clientBundle = resolve(output, "bundle.client.json");
  writeFileSync(serverBundle, `${JSON.stringify({ version, profile, data: canonicalize(serverData) }, null, 2)}\n`);
  writeFileSync(clientBundle, `${JSON.stringify({ version, profile, data: canonicalize(clientData) }, null, 2)}\n`);
  const clientPublic = resolve(root, "client/public/gamedata");
  mkdirSync(clientPublic, { recursive: true });
  copyFileSync(clientBundle, resolve(clientPublic, "bundle.client.json"));
  const git = spawnSync("git", ["rev-parse", "HEAD"], { cwd: root, encoding: "utf8" });
  if (git.error) throw git.error;
  if (git.status !== 0) throw new Error("Unable to resolve the current Git commit for the gamedata manifest.");
  writeFileSync(resolve(output, "manifest.json"), `${JSON.stringify({
    version,
    profile,
    gitCommit: git.stdout.trim(),
    builtAt: new Date().toISOString(),
    files: Object.fromEntries([...loaded].map(([path, value]) => [
      path,
      createHash("sha256").update(JSON.stringify(canonicalize(value))).digest("hex")
    ])),
    clientBundleSha256: createHash("sha256").update(normalizedClient).digest("hex")
  }, null, 2)}\n`);
  console.log(`Gamedata ${version}: validated ${loaded.size} registered files.`);
}
