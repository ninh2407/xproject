import tseslint from "typescript-eslint";

export default tseslint.config(
  ...tseslint.configs.recommended,
  {
    files: ["shared/sim/src/**/*.ts"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["@tienlo/client", "@tienlo/game-server", "node:*"],
              message: "shared/sim must stay runtime-agnostic (TA-R-021, CC-R-030)."
            }
          ]
        }
      ]
    }
  },
  {
    files: ["shared/sim/**/*.ts"],
    rules: {
      "no-restricted-properties": [
        "error",
        {
          object: "Math",
          property: "sin",
          message: "GEO-R-003 forbids Math.sin in simulation code."
        },
        {
          object: "Math",
          property: "cos",
          message: "GEO-R-003 forbids Math.cos in simulation code."
        },
        {
          object: "Math",
          property: "tan",
          message: "GEO-R-003 forbids Math.tan in simulation code."
        },
        {
          object: "Math",
          property: "atan2",
          message: "GEO-R-003 forbids Math.atan2 in simulation code."
        },
        {
          object: "Math",
          property: "exp",
          message: "GEO-R-003 forbids Math.exp in simulation code."
        },
        {
          object: "Math",
          property: "random",
          message: "GEO-R-003 forbids Math.random in simulation code."
        },
        {
          object: "Math",
          property: "pow",
          message: "Use multiplication instead of Math.pow in deterministic simulation code (GEO-R-003)."
        }
      ]
    }
  },
  {
    ignores: [
      "**/dist/**",
      "**/build/**",
      "**/node_modules/**",
      "**/gamedata-build/**",
      "shared/contracts/generated/**"
    ]
  }
);
