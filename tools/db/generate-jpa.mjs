import { mkdirSync, readFileSync, readdirSync, writeFileSync } from "node:fs";
import { dirname, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(fileURLToPath(new URL("../..", import.meta.url)));
const registryPath = join(root, "docs/tech/02-database.md");
const sourceRoot = join(root, "docs");
const javaRoot = join(root, "api-service/src/main/java/vn/tienlo/api");
const registry = readFileSync(registryPath, "utf8").split(/^## 5\./m)[1].split(/^## 6\./m)[0];
const tables = new Map();

for (const row of registry.split(/\r?\n/)) {
  if (!row.startsWith("| `")) continue;
  const cells = row.split("|");
  const owner = cells[2].trim().replaceAll("`", "").split(/\s/)[0];
  for (const match of cells[1].matchAll(/`([^`]+)`/g)) {
    for (const table of match[1].split(",").map((name) => name.trim())) {
      tables.set(table, owner);
    }
  }
}

function stripSqlComments(sql) {
  return sql.replace(/--[^\r\n]*/g, "");
}

function splitSqlList(value) {
  const parts = [];
  let part = "";
  let depth = 0;
  let inString = false;
  for (const character of value) {
    if (character === "'") inString = !inString;
    if (!inString) {
      if (character === "(") depth += 1;
      if (character === ")") depth -= 1;
      if (character === "," && depth === 0) {
        parts.push(part.trim());
        part = "";
        continue;
      }
    }
    part += character;
  }
  if (part.trim()) parts.push(part.trim());
  return parts;
}

function parseTableDefinitions(sql, sourcePath, definitions) {
  const source = stripSqlComments(sql);
  const createTable = /CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?:\w+\.)?(\w+)\s*\(/gi;
  let match;
  while ((match = createTable.exec(source)) !== null) {
    const table = match[1];
    if (!tables.has(table)) continue;

    let depth = 1;
    let inString = false;
    let end = createTable.lastIndex;
    for (; end < source.length && depth > 0; end += 1) {
      const character = source[end];
      if (character === "'") inString = !inString;
      if (!inString) {
        if (character === "(") depth += 1;
        if (character === ")") depth -= 1;
      }
    }
    if (depth !== 0) {
      throw new Error(`Unclosed CREATE TABLE definition for ${table} in ${sourcePath}`);
    }

    if (!definitions.has(table) || sourcePath === registryPath) {
      definitions.set(table, {
        body: source.slice(createTable.lastIndex, end - 1),
        sourcePath
      });
    }
    createTable.lastIndex = end;
  }
}

function walkMarkdown(directory, definitions) {
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    const sourcePath = join(directory, entry.name);
    if (entry.isDirectory()) {
      walkMarkdown(sourcePath, definitions);
    } else if (sourcePath.endsWith(".md")) {
      parseTableDefinitions(readFileSync(sourcePath, "utf8"), sourcePath, definitions);
    }
  }
}

function addAlteredColumns(directory, definitions) {
  for (const entry of readdirSync(directory, { withFileTypes: true })) {
    const sourcePath = join(directory, entry.name);
    if (entry.isDirectory()) {
      addAlteredColumns(sourcePath, definitions);
    } else if (sourcePath.endsWith(".md")) {
      const source = stripSqlComments(readFileSync(sourcePath, "utf8"));
      const alterColumn = /\bALTER\s+TABLE\s+(?:ONLY\s+)?(?:\w+\.)?(\w+)\s+ADD\s+COLUMN\s+(?:IF\s+NOT\s+EXISTS\s+)?([\s\S]*?);/gi;
      let match;
      while ((match = alterColumn.exec(source)) !== null) {
        const definition = definitions.get(match[1]);
        if (!definition) continue;

        const columnDefinition = match[2].trim();
        const columnName = columnDefinition.match(/^"?(\w+)"?\s+/)?.[1];
        if (!columnName) {
          throw new Error(`Could not parse ALTER TABLE column in ${sourcePath}: ${columnDefinition}`);
        }
        const existingColumns = splitSqlList(definition.body)
          .map((part) => part.match(/^"?(\w+)"?\s+/)?.[1])
          .filter(Boolean);
        if (!existingColumns.includes(columnName)) {
          definition.body += `,\n  ${columnDefinition}`;
        }
      }
    }
  }
}

function pascalCase(value) {
  return value
    .split("_")
    .map((part) => part[0].toUpperCase() + part.slice(1))
    .join("");
}

function camelCase(value) {
  return value.replace(/_([a-z0-9])/g, (_, character) => character.toUpperCase());
}

function javaType(sqlType) {
  const normalized = sqlType.toUpperCase().replace(/\s+/g, " ");
  if (normalized === "UUID") return "UUID";
  if (normalized === "BIGINT" || normalized === "BIGSERIAL") return "Long";
  if (normalized === "INT" || normalized === "INTEGER" || normalized === "SERIAL") return "Integer";
  if (normalized === "SMALLINT") return "Short";
  if (normalized === "BOOLEAN" || normalized === "BOOL") return "Boolean";
  if (normalized === "DATE") return "LocalDate";
  if (normalized === "TIMESTAMPTZ" || normalized.startsWith("TIMESTAMP WITH TIME ZONE")) return "Instant";
  if (normalized === "BYTEA") return "byte[]";
  if (normalized === "JSONB") return "JsonNode";
  if (normalized === "INET") return "InetAddress";
  if (normalized === "TEXT" || normalized.startsWith("VARCHAR") || normalized.startsWith("CHAR")) return "String";
  if (normalized.startsWith("NUMERIC") || normalized.startsWith("DECIMAL")) return "BigDecimal";
  throw new Error(`Unsupported SQL type: ${sqlType}`);
}

function parseColumns(table, definition) {
  const columns = [];
  const primaryKey = [];
  for (const part of splitSqlList(definition.body)) {
    const primaryKeyMatch = part.match(/^PRIMARY\s+KEY\s*\(([^)]+)\)/i);
    if (primaryKeyMatch) {
      primaryKey.push(...primaryKeyMatch[1].split(",").map((name) => name.trim().replaceAll('"', "")));
      continue;
    }
    if (/^(CONSTRAINT|UNIQUE|CHECK|FOREIGN|EXCLUDE)\b/i.test(part)) continue;

    const match = part.match(/^"?(\w+)"?\s+([A-Za-z]+(?:\s+(?:WITH(?:OUT)?\s+TIME\s+ZONE|PRECISION|VARYING))?(?:\s*\([^)]*\))?(?:\[\])?)([\s\S]*)$/i);
    if (!match) throw new Error(`Could not parse ${table} DDL column: ${part}`);
    const [, name, sqlType, options] = match;
    const field = camelCase(name);
    const type = sqlType.toUpperCase().replace(/\s+/g, " ").replace(/\s*\(/, "(");
    const column = {
      name,
      field,
      javaType: javaType(type),
      sqlType: type,
      nullable: !/\bNOT\s+NULL\b/i.test(options),
      generated: /\b(?:BIGSERIAL|SERIAL)\b/i.test(type) || /\bGENERATED\s+(?:ALWAYS|BY\s+DEFAULT)\s+AS\s+IDENTITY\b/i.test(options),
      length: type.match(/^(?:VAR)?CHAR\((\d+)\)$/i)?.[1],
      precision: type.match(/^(?:NUMERIC|DECIMAL)\((\d+)(?:,(\d+))?\)$/i)
    };
    columns.push(column);
    if (/\bPRIMARY\s+KEY\b/i.test(options)) primaryKey.push(name);
  }

  if (primaryKey.length === 0) throw new Error(`No primary key found for registered table ${table}`);
  const primaryFields = primaryKey.map((name) => {
    const column = columns.find((candidate) => candidate.name === name);
    if (!column) throw new Error(`Primary-key column ${name} is missing from ${table}`);
    column.nullable = false;
    return column;
  });
  return { columns, primaryFields };
}

function entitySource(table, owner, definition) {
  const { columns, primaryFields } = parseColumns(table, definition);
  const className = `${pascalCase(table)}Entity`;
  const imports = new Set([
    "jakarta.persistence.Column",
    "jakarta.persistence.Entity",
    "jakarta.persistence.Id",
    "jakarta.persistence.Table"
  ]);
  if (columns.some((column) => column.field === "version" && column.sqlType === "INT")) {
    imports.add("jakarta.persistence.Version");
  }
  if (primaryFields.length > 1) imports.add("jakarta.persistence.IdClass");
  if (columns.some((column) => column.javaType === "UUID")) imports.add("java.util.UUID");
  if (columns.some((column) => column.javaType === "BigDecimal")) imports.add("java.math.BigDecimal");
  if (columns.some((column) => column.javaType === "Instant")) imports.add("java.time.Instant");
  if (columns.some((column) => column.javaType === "LocalDate")) imports.add("java.time.LocalDate");
  if (columns.some((column) => column.javaType === "JsonNode")) {
    imports.add("com.fasterxml.jackson.databind.JsonNode");
    imports.add("org.hibernate.annotations.JdbcTypeCode");
    imports.add("org.hibernate.type.SqlTypes");
  }
  if (columns.some((column) => column.javaType === "InetAddress")) imports.add("java.net.InetAddress");
  if (columns.some((column) => column.generated && primaryFields.length === 1)) {
    imports.add("jakarta.persistence.GeneratedValue");
    imports.add("jakarta.persistence.GenerationType");
  }
  if (primaryFields.length > 1) {
    imports.add("java.io.Serializable");
    imports.add("java.util.Objects");
  }

  const lines = [
    `package vn.tienlo.api.${owner}.persistence.entity;`,
    "",
    ...[...imports].sort().map((name) => `import ${name};`),
    "",
    `/** DB-R-011 mapping for {@code game.${table}}; DDL: ${relative(root, definition.sourcePath).replaceAll("\\", "/")}. */`,
    `@Entity`,
    `@Table(name = "${table}", schema = "game")`,
    ...(primaryFields.length > 1 ? [`@IdClass(${className}.Key.class)`] : []),
    `public class ${className} {`
  ];

  for (const column of columns) {
    if (column.javaType === "JsonNode") {
      lines.push("    @JdbcTypeCode(SqlTypes.JSON)");
    }
    const columnOptions = [`name = "${column.name}"`, `nullable = ${column.nullable}`];
    if (column.length) columnOptions.push(`length = ${column.length}`);
    if (column.precision) {
      const [, precision, scale] = column.precision;
      columnOptions.push(`precision = ${precision}`, `scale = ${scale ?? 0}`);
    }
    if (column.javaType === "JsonNode") columnOptions.push('columnDefinition = "jsonb"');
    if (column.javaType === "InetAddress") columnOptions.push('columnDefinition = "inet"');
    lines.push(`    @Column(${columnOptions.join(", ")})`);
    if (primaryFields.includes(column)) lines.push("    @Id");
    if (column.generated && primaryFields.length === 1) {
      lines.push("    @GeneratedValue(strategy = GenerationType.IDENTITY)");
    }
    if (column.field === "version" && column.sqlType === "INT") lines.push("    @Version");
    lines.push(`    private ${column.javaType} ${column.field};`, "");
  }
  lines.push(`    public ${className}() {}`, "");
  for (const column of columns) {
    const accessor = column.field[0].toUpperCase() + column.field.slice(1);
    lines.push(
      `    public ${column.javaType} get${accessor}() {`,
      `        return ${column.field};`,
      "    }",
      "",
      `    public void set${accessor}(${column.javaType} ${column.field}) {`,
      `        this.${column.field} = ${column.field};`,
      "    }",
      ""
    );
  }

  if (primaryFields.length > 1) {
    lines.push("    public static class Key implements Serializable {");
    lines.push("        private static final long serialVersionUID = 1L;", "");
    for (const column of primaryFields) lines.push(`        public ${column.javaType} ${column.field};`);
    lines.push("", "        public Key() {}", "", "        @Override", "        public boolean equals(Object other) {");
    lines.push("            if (this == other) return true;");
    lines.push(`            if (!(other instanceof ${className}.Key that)) return false;`);
    lines.push(`            return ${primaryFields.map((column) => `Objects.equals(${column.field}, that.${column.field})`).join(" && ")};`);
    lines.push("        }", "", "        @Override", "        public int hashCode() {");
    lines.push(`            return Objects.hash(${primaryFields.map((column) => column.field).join(", ")});`);
    lines.push("        }", "    }");
  }
  lines.push("}", "");
  return { className, idType: primaryFields.length > 1 ? `${className}.Key` : primaryFields[0].javaType, source: lines.join("\n") };
}

function repositorySource(owner, className, idType) {
  const repoName = className.replace(/Entity$/, "Repository");
  const idImport = {
    UUID: "java.util.UUID",
    Instant: "java.time.Instant",
    LocalDate: "java.time.LocalDate"
  }[idType];
  if (className.endsWith("LedgerEntity")) {
    return {
      repoName,
      source: [
        `package vn.tienlo.api.${owner}.persistence;`,
        "",
        "import java.util.List;",
        "import java.util.Optional;",
        "import org.springframework.data.repository.Repository;",
        "import org.springframework.data.repository.RepositoryDefinition;",
        `import vn.tienlo.api.${owner}.persistence.entity.${className};`,
        ...(idImport ? [`import ${idImport};`] : []),
        "",
        `@RepositoryDefinition(domainClass = ${className}.class, idClass = ${idType}.class)`,
        `public interface ${repoName} extends Repository<${className}, ${idType}> {`,
        `    Optional<${className}> findById(${idType} id);`,
        `    List<${className}> findAll();`,
        "    long count();",
        "}",
        ""
      ].join("\n")
    };
  }
  return {
    repoName,
    source: [
      `package vn.tienlo.api.${owner}.persistence;`,
      "",
      "import org.springframework.data.jpa.repository.JpaRepository;",
      ...(idImport ? [`import ${idImport};`] : []),
      `import vn.tienlo.api.${owner}.persistence.entity.${className};`,
      "",
      `public interface ${repoName} extends JpaRepository<${className}, ${idType}> {}`,
      ""
    ].join("\n")
  };
}

function writeSource(packageParts, fileName, content) {
  const target = join(javaRoot, ...packageParts, `${fileName}.java`);
  mkdirSync(dirname(target), { recursive: true });
  writeFileSync(target, content, "utf8");
}

const definitions = new Map();
walkMarkdown(sourceRoot, definitions);
addAlteredColumns(sourceRoot, definitions);
const missing = [...tables.keys()].filter((table) => !definitions.has(table));
for (const table of missing) {
  console.warn(`No authoritative DDL found for registered table ${table}; entity/repository not generated.`);
}

for (const [table, owner] of tables) {
  const definition = definitions.get(table);
  if (!definition) continue;
  const entity = entitySource(table, owner, definition);
  const repository = repositorySource(owner, entity.className, entity.idType);
  writeSource([owner, "persistence", "entity"], entity.className, entity.source);
  writeSource([owner, "persistence"], repository.repoName, repository.source);
}

console.log(`Generated ${definitions.size} entity/repository pairs; ${missing.length} registered tables need DDL first.`);
