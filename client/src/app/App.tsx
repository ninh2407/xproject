import { FreeCamera } from "@babylonjs/core/Cameras/freeCamera.js";
import { Engine } from "@babylonjs/core/Engines/engine.js";
import { Vector3 } from "@babylonjs/core/Maths/math.vector.js";
import { Scene } from "@babylonjs/core/scene.js";
import { useEffect, useRef, useState } from "react";
import { loadClientBundle, type ClientBundle } from "@tienlo/gamedata";
import { transitionScreen, type Screen } from "./screenMachine.js";

export function App() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [screen, setScreen] = useState<Screen>("Boot");
  const [bundle, setBundle] = useState<ClientBundle | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    loadClientBundle("/gamedata/bundle.client.json")
      .then((loaded) => {
        if (!active) return;
        setBundle(loaded);
        setScreen((current) => transitionScreen(current, "Login"));
      })
      .catch((error: unknown) => {
        if (!active) return;
        setLoadError(error instanceof Error ? error.message : String(error));
      });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (canvas === null) return;

    const engine = new Engine(canvas, true);
    const scene = new Scene(engine);
    const camera = new FreeCamera("m0-camera", new Vector3(0, 0, -10), scene);
    camera.setTarget(Vector3.Zero());
    camera.attachControl(canvas, true);
    scene.activeCamera = camera;
    engine.runRenderLoop(() => scene.render());
    const resize = () => engine.resize();
    window.addEventListener("resize", resize);

    return () => {
      window.removeEventListener("resize", resize);
      engine.stopRenderLoop();
      scene.dispose();
      engine.dispose();
    };
  }, []);

  return (
    <main className="app-shell">
      <canvas
        ref={canvasRef}
        className="render-surface"
        aria-label="Game scene"
      />
      <section className="boot-panel" aria-live="polite">
        <h1>XProject</h1>
        {loadError !== null ? (
          <p role="alert">Không thể nạp gamedata: {loadError}</p>
        ) : bundle === null ? (
          <p>Đang nạp dữ liệu…</p>
        ) : (
          <p>
            Màn hình: {screen} · gamedata {bundle.version}
          </p>
        )}
      </section>
    </main>
  );
}
