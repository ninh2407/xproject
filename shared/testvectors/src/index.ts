import { move, type CollisionWorld, type Vec2 } from "@tienlo/sim";

export interface MoveVector {
  name: string;
  world: CollisionWorld;
  pos: Vec2;
  radius: number;
  delta: Vec2;
  expected: Vec2;
}

export interface VectorResult {
  name: string;
  actual: Vec2;
  expected: Vec2;
  passed: boolean;
}

export function runMoveVectors(
  vectors: readonly MoveVector[],
  tolerance = 1e-8,
): VectorResult[] {
  return vectors.map((vector) => {
    const actual = move(vector.world, vector.pos, vector.radius, vector.delta);
    return {
      name: vector.name,
      actual,
      expected: vector.expected,
      passed:
        Math.abs(actual.x - vector.expected.x) <= tolerance &&
        Math.abs(actual.z - vector.expected.z) <= tolerance,
    };
  });
}
