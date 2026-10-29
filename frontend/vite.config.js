import { defineConfig } from "vite";
import react, { reactCompilerPreset } from "@vitejs/plugin-react";
import babel from "@rolldown/plugin-babel";
import https from "https";

export default defineConfig({
  plugins: [react(), babel({ presets: [reactCompilerPreset()] })],
  server: {
    proxy: {
      "/api": {
        target: "https://localhost:9192",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ""),
        agent: new https.Agent({ rejectUnauthorized: false }),
      },
    },
  },
});