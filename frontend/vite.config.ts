/// <reference types="vitest/config" />
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

const backend = process.env.BACKEND_URL ?? "http://localhost:4704";
// Spring rechaza el handshake WS si Origin ≠ host (WebSocketConfig sin allowedOrigins)
const rewriteOrigin = (p: any) => {
  p.on("proxyReq", (r: any) => r.setHeader("origin", backend));
  p.on("proxyReqWs", (r: any) => r.setHeader("origin", backend));
};

export default defineConfig({
  plugins: [react()],
  server: {
    port: 4705,
    strictPort: true,
    proxy: {
      "/api": { target: backend, changeOrigin: true, configure: rewriteOrigin },
      "/the-last-shipbender": { target: backend, ws: true, changeOrigin: true, configure: rewriteOrigin },
    },
  },
  test: { environment: "jsdom", exclude: ["e2e/**", "node_modules/**"] },
});
