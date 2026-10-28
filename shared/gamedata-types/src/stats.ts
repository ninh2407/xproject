export interface PoiseBreakStunParams {
  PLAYER: number;
  NORMAL: number;
  ELITE: number;
  BOSS: number;
}

export interface ReactionParams {
  STAGGER: number;
  KNOCKBACK: number;
  KNOCKDOWN: number;
}

export interface DodgeParams {
  staminaCost: number;
  distance: number;
  durationSec: number;
  iframeSec: number;
  cooldownSec: number;
  doubleWindowSec: number;
}

export interface BlockParams {
  staminaCostPerHit: number;
  damageMult: number;
}

export interface EnergyParams {
  max: number;
  onBigHitTaken: number;
  bigHitPctMaxHp: number;
  decayPerSec: number;
  decayDelaySec: number;
}

export interface CharmParams {
  affectsRanks: string[];
  affectsPlayers: boolean;
  reapplyImmunitySec: number;
}

export interface StatsParams {
  combatTimeoutSec: number;
  baseGrowthPerStage: number;
  baseGrowthPerRealm: number;
  phamNhanFactor: number;
  mitigationK: number;
  mitigationCap: number;
  elementStrongMult: number;
  elementWeakMult: number;
  varianceMin: number;
  varianceMax: number;
  poiseBreakStunSec: PoiseBreakStunParams;
  poiseBreakDamageTakenBonus: number;
  poiseRegenPctPerSec: number;
  poiseRegenDelaySec: number;
  knockdownImmunitySec: number;
  reactionSec: ReactionParams;
  staminaRegenDelaySec: number;
  dodge: DodgeParams;
  block: BlockParams;
  perfectParryWindowSec: number;
  perfectParryPoisePct: number;
  perfectParryEnergy: number;
  defenseLagCompMs: number;
  energy: EnergyParams;
  eliteCcMult: number;
  charm: CharmParams;
}
