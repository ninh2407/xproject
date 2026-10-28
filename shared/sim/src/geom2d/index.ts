export interface Vec2 {
  x: number;
  z: number;
}

export interface Circle {
  c: Vec2;
  r: number;
}

export interface ConvexPolygon {
  verts: readonly Vec2[];
}

export interface CollisionFlags {
  move: boolean;
  projectile?: boolean;
  sight?: boolean;
  fly?: boolean;
}

export interface PolygonObstacle {
  id: string;
  kind: "POLYGON";
  verts: readonly Vec2[];
  flags: CollisionFlags;
}

export interface CircleObstacle {
  id: string;
  kind: "CIRCLE";
  c: Vec2;
  r: number;
  flags: CollisionFlags;
}

export type CollisionObstacle = PolygonObstacle | CircleObstacle;

export interface CollisionWorld {
  bounds: {
    width: number;
    depth: number;
  };
  shapes: readonly CollisionObstacle[];
}

export interface Hit {
  t: number;
  normal: Vec2;
  obstacleId: string;
}

const EPSILON = 1e-9;
const SKIN = 0.01;
const SLIDE_ITERATIONS = 3;

function dot(a: Vec2, b: Vec2): number {
  return a.x * b.x + a.z * b.z;
}

function subtract(a: Vec2, b: Vec2): Vec2 {
  return { x: a.x - b.x, z: a.z - b.z };
}

function addScaled(a: Vec2, b: Vec2, scale: number): Vec2 {
  return { x: a.x + b.x * scale, z: a.z + b.z * scale };
}

function length(value: Vec2): number {
  return Math.sqrt(dot(value, value));
}

function normalized(value: Vec2): Vec2 {
  const magnitude = length(value);
  return magnitude <= EPSILON
    ? { x: 1, z: 0 }
    : { x: value.x / magnitude, z: value.z / magnitude };
}

function closestPointOnSegment(point: Vec2, a: Vec2, b: Vec2): Vec2 {
  const edge = subtract(b, a);
  const edgeLengthSquared = dot(edge, edge);
  if (edgeLengthSquared <= EPSILON) return a;
  const t = Math.max(
    0,
    Math.min(1, dot(subtract(point, a), edge) / edgeLengthSquared),
  );
  return addScaled(a, edge, t);
}

function polygonContainsPoint(point: Vec2, polygon: ConvexPolygon): boolean {
  for (let i = 0; i < polygon.verts.length; i += 1) {
    const a = polygon.verts[i];
    const b = polygon.verts[(i + 1) % polygon.verts.length];
    if (a === undefined || b === undefined) return false;
    const edge = subtract(b, a);
    const relative = subtract(point, a);
    if (edge.x * relative.z - edge.z * relative.x < -EPSILON) return false;
  }
  return polygon.verts.length >= 3;
}

export function intersectsCirclePolygon(
  circle: Circle,
  polygon: ConvexPolygon,
): boolean {
  if (circle.r < 0 || polygon.verts.length < 3) {
    throw new RangeError(
      "A circle radius must be non-negative and a polygon must have at least three vertices.",
    );
  }
  if (polygonContainsPoint(circle.c, polygon)) return true;
  const radiusSquared = circle.r * circle.r;
  for (let i = 0; i < polygon.verts.length; i += 1) {
    const a = polygon.verts[i];
    const b = polygon.verts[(i + 1) % polygon.verts.length];
    if (a === undefined || b === undefined) continue;
    const closest = closestPointOnSegment(circle.c, a, b);
    const separation = subtract(circle.c, closest);
    if (dot(separation, separation) <= radiusSquared + EPSILON) return true;
  }
  return false;
}

function polygonOutwardNormal(a: Vec2, b: Vec2): Vec2 {
  const edge = subtract(b, a);
  return normalized({ x: edge.z, z: -edge.x });
}

function sweepPolygon(
  pos: Vec2,
  r: number,
  delta: Vec2,
  obstacle: PolygonObstacle,
): Hit | null {
  let first: Hit | null = null;
  const vertices = obstacle.verts;
  for (let i = 0; i < vertices.length; i += 1) {
    const a = vertices[i];
    const b = vertices[(i + 1) % vertices.length];
    if (a === undefined || b === undefined) continue;

    const normal = polygonOutwardNormal(a, b);
    const velocity = dot(delta, normal);
    const distance = dot(subtract(pos, a), normal);
    if (velocity < -EPSILON && distance >= r - EPSILON) {
      const t = (r - distance) / velocity;
      if (t >= -EPSILON && t <= 1 + EPSILON) {
        const centerAtImpact = addScaled(pos, delta, Math.max(0, t));
        const contact = addScaled(centerAtImpact, normal, -r);
        const edge = subtract(b, a);
        const projection = dot(subtract(contact, a), edge);
        const edgeLengthSquared = dot(edge, edge);
        if (
          projection >= -EPSILON &&
          projection <= edgeLengthSquared + EPSILON
        ) {
          first = chooseEarlier(first, {
            t: Math.max(0, t),
            normal,
            obstacleId: obstacle.id,
          });
        }
      }
    }

    const toVertex = subtract(pos, a);
    const quadraticA = dot(delta, delta);
    const quadraticB = 2 * dot(toVertex, delta);
    const quadraticC = dot(toVertex, toVertex) - r * r;
    const discriminant = quadraticB * quadraticB - 4 * quadraticA * quadraticC;
    if (quadraticA > EPSILON && discriminant >= 0) {
      const t = (-quadraticB - Math.sqrt(discriminant)) / (2 * quadraticA);
      if (t >= -EPSILON && t <= 1 + EPSILON) {
        const centerAtImpact = addScaled(pos, delta, Math.max(0, t));
        const vertexNormal = normalized(subtract(centerAtImpact, a));
        if (dot(delta, vertexNormal) < -EPSILON) {
          first = chooseEarlier(first, {
            t: Math.max(0, t),
            normal: vertexNormal,
            obstacleId: obstacle.id,
          });
        }
      }
    }
  }
  return first;
}

function sweepCircle(
  pos: Vec2,
  r: number,
  delta: Vec2,
  obstacle: CircleObstacle,
): Hit | null {
  const combinedRadius = r + obstacle.r;
  const relative = subtract(pos, obstacle.c);
  const quadraticA = dot(delta, delta);
  const quadraticB = 2 * dot(relative, delta);
  const quadraticC = dot(relative, relative) - combinedRadius * combinedRadius;
  const discriminant = quadraticB * quadraticB - 4 * quadraticA * quadraticC;
  if (quadraticA <= EPSILON || discriminant < 0) return null;
  const t = (-quadraticB - Math.sqrt(discriminant)) / (2 * quadraticA);
  if (t < -EPSILON || t > 1 + EPSILON) return null;
  const impact = addScaled(pos, delta, Math.max(0, t));
  const normal = normalized(subtract(impact, obstacle.c));
  if (dot(delta, normal) >= -EPSILON) return null;
  return { t: Math.max(0, t), normal, obstacleId: obstacle.id };
}

function chooseEarlier(current: Hit | null, candidate: Hit): Hit {
  if (current === null) return candidate;
  if (candidate.t < current.t - EPSILON) return candidate;
  if (
    Math.abs(candidate.t - current.t) <= EPSILON &&
    candidate.obstacleId < current.obstacleId
  )
    return candidate;
  return current;
}

function sweepBounds(
  pos: Vec2,
  r: number,
  delta: Vec2,
  bounds: CollisionWorld["bounds"],
): Hit | null {
  let first: Hit | null = null;
  const candidates: Hit[] = [];
  if (delta.x < -EPSILON)
    candidates.push({
      t: (r - pos.x) / delta.x,
      normal: { x: 1, z: 0 },
      obstacleId: "$bounds",
    });
  if (delta.x > EPSILON)
    candidates.push({
      t: (bounds.width - r - pos.x) / delta.x,
      normal: { x: -1, z: 0 },
      obstacleId: "$bounds",
    });
  if (delta.z < -EPSILON)
    candidates.push({
      t: (r - pos.z) / delta.z,
      normal: { x: 0, z: 1 },
      obstacleId: "$bounds",
    });
  if (delta.z > EPSILON)
    candidates.push({
      t: (bounds.depth - r - pos.z) / delta.z,
      normal: { x: 0, z: -1 },
      obstacleId: "$bounds",
    });
  for (const candidate of candidates) {
    if (candidate.t >= -EPSILON && candidate.t <= 1 + EPSILON) {
      first = chooseEarlier(first, {
        ...candidate,
        t: Math.max(0, candidate.t),
      });
    }
  }
  return first;
}

export function sweepCircleFirstHit(
  world: CollisionWorld,
  pos: Vec2,
  r: number,
  delta: Vec2,
  mask: keyof CollisionFlags = "move",
): Hit | null {
  if (r <= 0)
    throw new RangeError("The moving circle radius must be greater than zero.");
  let first = sweepBounds(pos, r, delta, world.bounds);
  for (const obstacle of world.shapes) {
    if (!obstacle.flags[mask]) continue;
    const hit =
      obstacle.kind === "POLYGON"
        ? sweepPolygon(pos, r, delta, obstacle)
        : sweepCircle(pos, r, delta, obstacle);
    if (hit !== null) first = chooseEarlier(first, hit);
  }
  return first;
}

function depenetrate(world: CollisionWorld, initial: Vec2, r: number): Vec2 {
  let pos = initial;
  for (let pass = 0; pass < SLIDE_ITERATIONS; pass += 1) {
    const bounded = {
      x: Math.max(r, Math.min(world.bounds.width - r, pos.x)),
      z: Math.max(r, Math.min(world.bounds.depth - r, pos.z)),
    };
    pos = bounded;
    let moved = false;
    for (const obstacle of world.shapes) {
      if (!obstacle.flags.move) continue;
      if (obstacle.kind === "CIRCLE") {
        const separation = subtract(pos, obstacle.c);
        const distance = length(separation);
        const penetration = r + obstacle.r - distance;
        if (penetration > EPSILON) {
          pos = addScaled(pos, normalized(separation), penetration + SKIN);
          moved = true;
        }
        continue;
      }

      const polygon: ConvexPolygon = { verts: obstacle.verts };
      let closest: Vec2 | undefined;
      let closestDistance = Number.POSITIVE_INFINITY;
      let outward = { x: 1, z: 0 };
      for (let i = 0; i < obstacle.verts.length; i += 1) {
        const a = obstacle.verts[i];
        const b = obstacle.verts[(i + 1) % obstacle.verts.length];
        if (a === undefined || b === undefined) continue;
        const point = closestPointOnSegment(pos, a, b);
        const distance = length(subtract(pos, point));
        if (distance < closestDistance) {
          closest = point;
          closestDistance = distance;
          outward = polygonOutwardNormal(a, b);
        }
      }
      if (closest === undefined) continue;
      const inside = polygonContainsPoint(pos, polygon);
      const penetration = inside ? r + closestDistance : r - closestDistance;
      if (penetration > EPSILON) {
        const normal = inside ? outward : normalized(subtract(pos, closest));
        pos = addScaled(pos, normal, penetration + SKIN);
        moved = true;
      }
    }
    if (!moved) break;
  }
  return pos;
}

export function move(
  world: CollisionWorld,
  pos: Vec2,
  r: number,
  delta: Vec2,
): Vec2 {
  if (!Number.isFinite(r) || r <= 0)
    throw new RangeError(
      "The moving circle radius must be a positive finite number.",
    );
  if (world.bounds.width < r * 2 || world.bounds.depth < r * 2) {
    throw new RangeError(
      "Collision bounds must be at least twice the moving circle radius.",
    );
  }

  let result = depenetrate(world, pos, r);
  const distance = length(delta);
  const steps = Math.max(1, Math.ceil(distance / (0.5 * r)));
  const stepDelta = { x: delta.x / steps, z: delta.z / steps };

  for (let step = 0; step < steps; step += 1) {
    let remaining = stepDelta;
    for (let iteration = 0; iteration < SLIDE_ITERATIONS; iteration += 1) {
      if (length(remaining) <= EPSILON) break;
      const hit = sweepCircleFirstHit(world, result, r, remaining);
      if (hit === null) {
        result = addScaled(result, remaining, 1);
        break;
      }

      result = addScaled(result, remaining, hit.t);
      result = addScaled(result, hit.normal, -SKIN);
      const afterImpact = {
        x: remaining.x * (1 - hit.t),
        z: remaining.z * (1 - hit.t),
      };
      const intoWall = dot(afterImpact, hit.normal);
      remaining =
        intoWall < 0
          ? {
              x: afterImpact.x - hit.normal.x * intoWall,
              z: afterImpact.z - hit.normal.z * intoWall,
            }
          : afterImpact;
    }
  }

  return depenetrate(world, result, r);
}
