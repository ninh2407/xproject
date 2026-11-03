import { spawnSync } from "node:child_process";
import { mkdirSync, rmSync } from "node:fs";
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("../..", import.meta.url)));
const contracts = resolve(root, "shared/contracts");

function run(args) {
  const command = resolve(root, "node_modules/@openapitools/openapi-generator-cli/main.js");
  const result = spawnSync(process.execPath, [command, ...args], {
    cwd: root,
    stdio: "inherit"
  });
  if (result.error) throw result.error;
  if (result.status !== 0) throw new Error(`OpenAPI Generator failed (${result.status ?? "signal"}).`);
}

for (const name of ["public", "internal"]) {
  const tsOutput = resolve(contracts, "generated", name, "typescript-fetch");
  const javaOutput = resolve(root, "api-service", "build", "generated", "openapi", name);
  const javaPackage = `vn.tienlo.api.generated.${name === "public" ? "publicapi" : name}`;
  rmSync(tsOutput, { recursive: true, force: true });
  rmSync(javaOutput, { recursive: true, force: true });
  mkdirSync(tsOutput, { recursive: true });
  mkdirSync(javaOutput, { recursive: true });

  run([
    "generate",
    "-i", `shared/contracts/${name}.yaml`,
    "-g", "typescript-fetch",
    "-o", tsOutput,
    "--additional-properties=supportsES6=true,typescriptThreePlus=true,useSingleRequestParameter=true,npmName=@tienlo/contracts-client,npmVersion=0.1.0",
    "--global-property=apiTests=false,modelTests=false,apiDocs=false,modelDocs=false"
  ]);
  run([
    "generate",
    "-i", `shared/contracts/${name}.yaml`,
    "-g", "spring",
    "-o", javaOutput,
    `--additional-properties=interfaceOnly=true,useSpringBoot3=true,useTags=true,skipDefaultInterface=true,documentationProvider=none,annotationLibrary=none,useBeanValidation=true,openApiNullable=false,hideGenerationTimestamp=true,apiPackage=${javaPackage},modelPackage=${javaPackage}.model,invokerPackage=${javaPackage}`,
    "--global-property=apiTests=false,modelTests=false,apiDocs=false,modelDocs=false"
  ]);
}
