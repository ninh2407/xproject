import { existsSync, readFileSync, readdirSync } from "node:fs";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("../..", import.meta.url)));
const register = readFileSync(resolve(root, "docs/tech/14-oss-register.md"), "utf8");
const packageFiles = [
  "package.json",
  "client/package.json",
  "game-server/package.json",
  ...readdirSync(resolve(root, "shared"), { withFileTypes: true })
    .filter((entry) => entry.isDirectory())
    .map((entry) => `shared/${entry.name}/package.json`)
    .filter((path) => existsSync(resolve(root, path)))
];

const licenseLabels = {
  "@openapitools/openapi-generator-cli": "@openapitools/openapi-generator-cli",
  "@stoplight/prism-cli": "Prism CLI",
  "@types/node": "@types/node",
  "@types/react": "@types/react",
  "@types/react-dom": "@types/react-dom",
  "@vitejs/plugin-react": "@vitejs/plugin-react",
  "@babylonjs/core": "Babylon.js",
  ajv: "Ajv",
  eslint: "ESLint",
  prettier: "Prettier",
  react: "React",
  "react-dom": "React DOM",
  typescript: "TypeScript",
  "typescript-eslint": "typescript-eslint",
  vite: "Vite",
  vitest: "Vitest"
};
const missing = new Set();
for (const packageFile of packageFiles) {
  const manifest = JSON.parse(readFileSync(resolve(root, packageFile), "utf8"));
  const dependencies = {
    ...manifest.dependencies,
    ...manifest.devDependencies
  };
  for (const dependency of Object.keys(dependencies)) {
    if (dependency.startsWith("@tienlo/")) continue;
    const label = licenseLabels[dependency];
    if (label === undefined) {
      missing.add(`${packageFile}: ${dependency} has no TD-15 mapping`);
      continue;
    }
    const line = register.split(/\r?\n/).find((row) => row.startsWith(`| ${label} |`));
    const columns = line?.split("|").map((column) => column.trim()) ?? [];
    if (columns[6] !== "✅") missing.add(`${packageFile}: ${dependency} is not approved in TD-15`);
  }
}

for (const label of [
  "Spring Boot (+ Spring Framework)",
  "Spring Boot Validation (Hibernate Validator)",
  "Flyway (Community)",
  "PostgreSQL",
  "ArchUnit",
  "JUnit 5",
  "Prism CLI"
]) {
  const line = register.split(/\r?\n/).find((row) => row.startsWith(`| ${label} |`));
  const columns = line?.split("|").map((column) => column.trim()) ?? [];
  if (columns[6] !== "✅") missing.add(`api-service: ${label} is not approved in TD-15`);
}

if (missing.size > 0) {
  console.error([...missing].sort().join("\n"));
  process.exitCode = 1;
} else {
  console.log(`License register covers ${packageFiles.length} workspace package manifests and API dependencies.`);
}
