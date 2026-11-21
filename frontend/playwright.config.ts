import { defineConfig } from "@playwright/test";

// El backend lo levanta quien corre el test: PORT=4704 ./mvnw spring-boot:test-run (requiere Docker)
export default defineConfig({
  testDir: "e2e",
  use: { baseURL: "http://localhost:4705", browserName: "chromium" },
  webServer: { command: "npm run dev", port: 4705, reuseExistingServer: true },
});
