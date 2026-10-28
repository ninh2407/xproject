export type JsonValue =
  null | boolean | number | string | JsonValue[] | { [key: string]: JsonValue };

export interface ClientBundle {
  version: string;
  profile: string;
  data: Readonly<Record<string, JsonValue>>;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === "object" && !Array.isArray(value);
}

function isJsonValue(value: unknown): value is JsonValue {
  if (value === null || typeof value === "string" || typeof value === "boolean")
    return true;
  if (typeof value === "number") return Number.isFinite(value);
  if (Array.isArray(value)) return value.every(isJsonValue);
  if (isRecord(value)) return Object.values(value).every(isJsonValue);
  return false;
}

function deepFreeze<T extends object>(value: T): Readonly<T> {
  for (const child of Object.values(value)) {
    if (child !== null && typeof child === "object" && !Object.isFrozen(child))
      deepFreeze(child);
  }
  return Object.freeze(value);
}

function parseClientBundle(value: unknown): ClientBundle {
  if (!isRecord(value))
    throw new TypeError("Gamedata bundle must be a JSON object.");
  if (
    typeof value.version !== "string" ||
    !/^[0-9a-f]{12}$/.test(value.version)
  ) {
    throw new TypeError("Gamedata bundle has an invalid version.");
  }
  if (typeof value.profile !== "string" || value.profile.length === 0) {
    throw new TypeError("Gamedata bundle has no profile.");
  }
  if (!isRecord(value.data))
    throw new TypeError("Gamedata bundle data must be an object.");
  const data: Record<string, JsonValue> = {};
  for (const [path, file] of Object.entries(value.data)) {
    if (!isJsonValue(file))
      throw new TypeError(`Gamedata bundle entry ${path} is not valid JSON.`);
    data[path] = file;
  }
  for (const path of [
    "world/client.json",
    "world/camera.json",
    "world/controls.json",
  ]) {
    if (!isRecord(data[path]))
      throw new TypeError(`Gamedata bundle is missing ${path}.`);
  }
  return deepFreeze({
    version: value.version,
    profile: value.profile,
    data,
  });
}

export async function loadClientBundle(
  url: string,
  fetcher: typeof fetch = fetch,
): Promise<ClientBundle> {
  const response = await fetcher(url);
  if (!response.ok) {
    throw new Error(`Gamedata request failed with HTTP ${response.status}.`);
  }
  const payload: unknown = JSON.parse(await response.text());
  return parseClientBundle(payload);
}
