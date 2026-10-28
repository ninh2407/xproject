import { createServer } from "node:http";

const port = Number(process.env.PORT ?? 2567);
if (!Number.isInteger(port) || port < 1 || port > 65535) {
  throw new RangeError("PORT must be an integer between 1 and 65535.");
}

const server = createServer((request, response) => {
  if (request.method === "GET" && request.url === "/health") {
    response.writeHead(200, { "content-type": "application/json" });
    response.end(JSON.stringify({ status: "ok", service: "game-server" }));
    return;
  }
  response.writeHead(404);
  response.end();
});

server.listen(port, "0.0.0.0", () => {
  console.log(`Game Server M0 health endpoint listening on ${port}.`);
});

for (const signal of ["SIGINT", "SIGTERM"] as const) {
  process.on(signal, () => {
    server.close((error) => {
      if (error) {
        console.error("Game Server shutdown failed.", error);
        process.exitCode = 1;
      }
    });
  });
}
