export type Screen =
  | "Boot"
  | "Login"
  | "CharacterSelect"
  | "MapLoading"
  | "InGame"
  | "Transfer"
  | "Ending";

const transitions: Record<Screen, readonly Screen[]> = {
  Boot: ["Login"],
  Login: ["CharacterSelect", "Boot"],
  CharacterSelect: ["MapLoading", "Login"],
  MapLoading: ["InGame", "CharacterSelect", "Transfer"],
  InGame: ["Transfer", "Ending", "Login"],
  Transfer: ["MapLoading", "InGame"],
  Ending: ["Login", "CharacterSelect"],
};

export function transitionScreen(current: Screen, next: Screen): Screen {
  if (!transitions[current].includes(next)) {
    throw new Error(`Invalid screen transition: ${current} -> ${next}.`);
  }
  return next;
}
