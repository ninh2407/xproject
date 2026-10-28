import { describe, expect, it } from "vitest";
import { transitionScreen } from "./screenMachine.js";

describe("CL-01 screen machine", () => {
  it("starts at Login after bundle boot", () => {
    expect(transitionScreen("Boot", "Login")).toBe("Login");
  });

  it("rejects transitions not in the TD-05 screen flow", () => {
    expect(() => transitionScreen("Boot", "InGame")).toThrow(
      "Invalid screen transition",
    );
  });
});
