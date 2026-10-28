export interface DistanceParams {
  default: number;
  min: number;
  max: number;
  step: number;
}

export interface AimLookAheadParams {
  enabled: boolean;
  ratio: number;
  maxM: number;
}

export interface OccluderFadeParams {
  radiusM: number;
  minAlpha: number;
  fadeSec: number;
}

export interface CameraShakeParams {
  maxAmplitudeM: number;
  maxDurationSec: number;
}

export interface CameraParams {
  pitchDeg: number;
  pitchRangeDeg: number[];
  yawDeg: number;
  fovDeg: number;
  distance: DistanceParams;
  followLerp: number;
  aimLookAhead: AimLookAheadParams;
  occluderFade: OccluderFadeParams;
  shake: CameraShakeParams;
  boundsInsetM: number;
}

export type InputDevice = "KBM" | "GAMEPAD" | "TOUCH";

export type ActionId =
  | "MOVE_UP"
  | "MOVE_DOWN"
  | "MOVE_LEFT"
  | "MOVE_RIGHT"
  | "WALK_HOLD"
  | "BASIC_ATTACK"
  | "HEAVY_ATTACK"
  | "DODGE"
  | "BLOCK"
  | "SKILL_Q"
  | "SKILL_E"
  | "SKILL_R"
  | "SKILL_F"
  | "SKILL_V"
  | "ULTIMATE"
  | "ARTIFACT"
  | "QUICK_1"
  | "QUICK_2"
  | "QUICK_3"
  | "QUICK_4"
  | "INTERACT"
  | "SCAN"
  | "MEDITATE"
  | "BEAST_COMMAND"
  | "BEAST_SUMMON"
  | "TARGET_LOCK"
  | "SHOW_NAMES"
  | "UI_BAG"
  | "UI_CHARACTER"
  | "UI_TECHNIQUES"
  | "UI_QUESTS"
  | "UI_MAP"
  | "UI_CODEX"
  | "UI_FRIENDS"
  | "UI_BEASTS"
  | "UI_CAVE"
  | "UI_PK_MODE"
  | "UI_CHAT"
  | "UI_MENU"
  | "CAMERA_ZOOM_IN"
  | "CAMERA_ZOOM_OUT";

export interface KeyBinding {
  action: ActionId;
  device: InputDevice;
  primary: string;
  secondary?: string;
}

export interface ToggleSettings {
  block: boolean;
  walk: boolean;
}

export type AimAssist = "STANDARD" | "LOW" | "OFF";

export interface GamepadSettings {
  deadzone: number;
  aimAssist: AimAssist;
  vibration: boolean;
}

export type JoystickMode = "FLOATING" | "FIXED";
export type TelegraphPalette =
  "DEFAULT" | "DEUTERANOPIA" | "PROTANOPIA" | "TRITANOPIA";

export interface TouchSettings {
  joystick: JoystickMode;
  leftHanded: boolean;
  buttonScale: number;
  smartCast: boolean;
}

export interface AccessibilitySettings {
  screenShake: number;
  flashReduction: boolean;
  telegraphPalette: TelegraphPalette;
  telegraphPattern: boolean;
  telegraphOpacity: number;
  uiScale: number;
  fontScale: number;
  subtitles: boolean;
}

export interface ControlParams {
  version: number;
  bindings: KeyBinding[];
  clickToMove: boolean;
  holdToAim: boolean;
  aimLookAhead: boolean;
  cameraPitchDeg: number;
  toggleInsteadOfHold: ToggleSettings;
  gamepad: GamepadSettings;
  touch: TouchSettings;
  accessibility: AccessibilitySettings;
}

export interface ChunkParams {
  loadRadius: number;
  unloadRadius: number;
}

export interface AnimationBlendParams {
  actionStartSec: number;
  comboNextSec: number;
  recoveryExitSec: number;
  idleCombatSwapSec: number;
  toReactionSec: number;
  toDeathSec: number;
  loopEnterSec: number;
  meditateExitSec: number;
  flightSec: number;
  flightFallSec: number;
}

export interface AnimationParams {
  cancelBlendSec: number;
  despawnFadeSec: number;
  blend: AnimationBlendParams;
  rateMin: number;
  rateMax: number;
}

export interface VfxBudgetParams {
  concurrentParticles: number[];
  skillParticlesHigh: number;
  skillParticlePct: number[];
  otherPlayersParticlePct: number[];
  weatherParticlePct: number[];
  skillMeshTrisMax: number;
  lifetimeSecMax: number;
  atlasMaxPx: number;
}

export interface QualityParams {
  autoDowngradeFpsPc: number;
  autoDowngradeFpsMobile: number;
  autoDowngradeWindowSec: number;
  benchSec: number;
}

export interface CacheParams {
  quotaMbWeb: number;
  quotaMbMobile: number;
}

export interface ReconnectParams {
  backoffMs: number[];
  backgroundKeepAliveSec: number;
}

export interface BreakpointParams {
  tablet: number;
  pc: number;
}

export interface UiParams {
  breakpoints: BreakpointParams;
  minTouchTargetPx: number;
}

export interface BundleBudgetParams {
  firstLoad: number;
  coreJsGzip: number;
  gamedataI18n: number;
}

export interface ClientParams {
  chunks: ChunkParams;
  anim: AnimationParams;
  vfxBudget: VfxBudgetParams;
  quality: QualityParams;
  cache: CacheParams;
  reconnect: ReconnectParams;
  ui: UiParams;
  bundleBudgetMb: BundleBudgetParams;
}
