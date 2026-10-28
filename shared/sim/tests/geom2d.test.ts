import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";
import {
  intersectsCirclePolygon,
  move,
  type ConvexPolygon,
  type Vec2,
} from "../src/index.js";
import {
  runMoveVectors,
  type MoveVector,
} from "../../testvectors/src/index.js";

const vectors = JSON.parse(
  readFileSync(
    new URL("../../testvectors/geom2d/move.json", import.meta.url),
    "utf8",
  ),
) as MoveVector[];

const square: ConvexPolygon = {
  verts: [
    { x: 2, z: 2 },
    { x: 4, z: 2 },
    { x: 4, z: 4 },
    { x: 2, z: 4 },
  ],
};

describe("GEO-AC-001 circle/polygon collision", () => {
  it("intersects when the circle touches an edge", () => {
    expect(
      intersectsCirclePolygon({ c: { x: 1.5, z: 3 }, r: 0.5 }, square),
    ).toBe(true);
  });

  it("intersects when the circle covers a vertex", () => {
    expect(
      intersectsCirclePolygon({ c: { x: 1.7, z: 1.7 }, r: 0.5 }, square),
    ).toBe(true);
  });

  it("does not intersect a separated polygon", () => {
    expect(intersectsCirclePolygon({ c: { x: 0, z: 0 }, r: 0.5 }, square)).toBe(
      false,
    );
  });

  it("intersects when its center is inside the polygon", () => {
    expect(intersectsCirclePolygon({ c: { x: 3, z: 3 }, r: 0.1 }, square)).toBe(
      true,
    );
  });
});

describe("GEO-AC-005 collide-and-slide movement", () => {
  it("stops at a wall and keeps the tangential component", () => {
    const world = {
      bounds: { width: 20, depth: 20 },
      shapes: [
        {
          id: "wall",
          kind: "POLYGON" as const,
          verts: [
            { x: 10, z: 0 },
            { x: 11, z: 0 },
            { x: 11, z: 20 },
            { x: 10, z: 20 },
          ],
          flags: { move: true },
        },
      ],
    };
    const result = move(world, { x: 8, z: 4 }, 0.4, { x: 2, z: 2 });
    expect(result.x).toBeCloseTo(9.59, 2);
    expect(result.z).toBeCloseTo(6, 2);
  });

  it("keeps the player inside map bounds", () => {
    const world = { bounds: { width: 10, depth: 10 }, shapes: [] };
    const result = move(world, { x: 1, z: 1 }, 0.4, { x: -3, z: 0 });
    expect(result.x).toBeCloseTo(0.4, 8);
  });

  it("runs shared movement testvectors identically", () => {
    const results = runMoveVectors(vectors);
    expect(
      results.every((result) => result.passed),
      JSON.stringify(results),
    ).toBe(true);
  });
});

describe("GEO-AC-006 input validation", () => {
  it("rejects a radius that cannot fit in map bounds", () => {
    const world = { bounds: { width: 1, depth: 10 }, shapes: [] };
    const pos: Vec2 = { x: 0.5, z: 5 };
    expect(() => move(world, pos, 0.6, { x: 0, z: 0 })).toThrow(RangeError);
  });
});
