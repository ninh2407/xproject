import ts from "typescript";
import { mkdirSync, readdirSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("../..", import.meta.url)));
const typesRoot = resolve(root, "shared/gamedata-types/src");
const schemaRoot = resolve(root, "shared/gamedata-build/schema");
const javaRoot = resolve(root, "shared/gamedata-build/java/vn/tienlo/api/gamedata/generated");
const typeFiles = readdirSync(typesRoot)
  .filter((name) => name.endsWith(".ts"))
  .map((name) => resolve(typesRoot, name));
const normalizedTypeFiles = new Set(typeFiles.map((path) => path.replaceAll("\\", "/")));
const program = ts.createProgram(typeFiles, {
  strict: true,
  target: ts.ScriptTarget.ES2022,
  module: ts.ModuleKind.NodeNext,
  moduleResolution: ts.ModuleResolutionKind.NodeNext
});
const checker = program.getTypeChecker();

const declarations = new Map();
for (const sourceFile of program.getSourceFiles()) {
  if (!normalizedTypeFiles.has(sourceFile.fileName.replaceAll("\\", "/"))) continue;
  for (const statement of sourceFile.statements) {
    if (
      (ts.isInterfaceDeclaration(statement) || ts.isTypeAliasDeclaration(statement)) &&
      statement.modifiers?.some((modifier) => modifier.kind === ts.SyntaxKind.ExportKeyword)
    ) {
      declarations.set(statement.name.text, statement);
    }
  }
}

function symbolName(type) {
  return type.aliasSymbol?.getName() ?? type.getSymbol()?.getName();
}

function schemaFor(type, expandNamed = false) {
  if ((type.flags & ts.TypeFlags.StringLike) !== 0 && (type.flags & ts.TypeFlags.StringLiteral) === 0) {
    return { type: "string" };
  }
  if ((type.flags & ts.TypeFlags.NumberLike) !== 0) return { type: "number" };
  if ((type.flags & ts.TypeFlags.BooleanLike) !== 0) return { type: "boolean" };
  if ((type.flags & ts.TypeFlags.StringLiteral) !== 0) return { type: "string", enum: [type.value] };

  const name = symbolName(type);
  if (!expandNamed && name !== undefined && declarations.has(name)) {
    return { $ref: `#/definitions/${name}` };
  }
  if (checker.isArrayType(type)) {
    const element = checker.getElementTypeOfArrayType(type);
    if (element === undefined) throw new Error("Unable to resolve an array element type.");
    return { type: "array", items: schemaFor(element) };
  }
  if (type.isUnion()) {
    if (type.types.every((member) => (member.flags & ts.TypeFlags.StringLiteral) !== 0)) {
      return { type: "string", enum: type.types.map((member) => member.value) };
    }
    return { anyOf: type.types.map((member) => schemaFor(member)) };
  }

  if ((type.flags & ts.TypeFlags.Object) !== 0) {
    const properties = checker.getPropertiesOfType(type);
    const schemaProperties = {};
    const required = [];
    for (const property of properties) {
      const propertyType = checker.getTypeOfSymbolAtLocation(property, property.valueDeclaration ?? property.declarations[0]);
      const schemaType = (property.flags & ts.SymbolFlags.Optional) !== 0
        ? checker.getNonNullableType(propertyType)
        : propertyType;
      schemaProperties[property.name] = schemaFor(schemaType);
      if ((property.flags & ts.SymbolFlags.Optional) === 0) required.push(property.name);
    }
    return {
      type: "object",
      additionalProperties: false,
      properties: schemaProperties,
      required
    };
  }

  throw new Error(`Unsupported gamedata type: ${checker.typeToString(type)}`);
}

function schemaForDeclaration(declaration) {
  const type = checker.getTypeAtLocation(declaration);
  if (ts.isInterfaceDeclaration(declaration)) {
    return schemaFor(type, true);
  }
  return schemaFor(type, true);
}

function javaType(type) {
  if (checker.isArrayType(type)) {
    const element = checker.getElementTypeOfArrayType(type);
    if (element === undefined) throw new Error("Unable to resolve Java array element type.");
    return `List<${javaType(element)}>`;
  }
  if ((type.flags & ts.TypeFlags.StringLike) !== 0 || type.isUnion()) return "String";
  if ((type.flags & ts.TypeFlags.NumberLike) !== 0) return "double";
  if ((type.flags & ts.TypeFlags.BooleanLike) !== 0) return "boolean";
  const name = symbolName(type);
  if (name !== undefined && declarations.has(name) && ts.isInterfaceDeclaration(declarations.get(name))) {
    return name;
  }
  throw new Error(`Unsupported Java gamedata type: ${checker.typeToString(type)}`);
}

function generateJavaRecords() {
  mkdirSync(javaRoot, { recursive: true });
  for (const [name, declaration] of declarations) {
    if (!ts.isInterfaceDeclaration(declaration)) continue;
    const type = checker.getTypeAtLocation(declaration);
    const properties = checker.getPropertiesOfType(type).map((property) => {
      const value = checker.getTypeOfSymbolAtLocation(property, property.valueDeclaration ?? property.declarations[0]);
      return { name: property.name, type: javaType(value) };
    });
    const imports = properties.some((property) => property.type.startsWith("List<"))
      ? "import java.util.List;\n\n"
      : "";
    const components = properties.map((property) => `    ${property.type} ${property.name}`).join(",\n");
    const source = `package vn.tienlo.api.gamedata.generated;\n\n${imports}public record ${name}(\n${components}\n) {}\n`;
    writeFileSync(resolve(javaRoot, `${name}.java`), source);
  }
}

export function generateSchemas() {
  mkdirSync(schemaRoot, { recursive: true });
  for (const name of declarations.keys()) {
    const schema = {
      $schema: "http://json-schema.org/draft-07/schema#",
      title: name,
      definitions: Object.fromEntries([...declarations].map(([definitionName, definition]) => [
        definitionName,
        schemaForDeclaration(definition)
      ])),
      $ref: `#/definitions/${name}`
    };
    const output = resolve(schemaRoot, `${name}.schema.json`);
    mkdirSync(dirname(output), { recursive: true });
    writeFileSync(output, `${JSON.stringify(schema, null, 2)}\n`);
  }
  generateJavaRecords();
}

if (process.argv[1] !== undefined && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  generateSchemas();
}
