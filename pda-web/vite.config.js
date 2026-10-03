import { defineConfig, loadEnv } from 'vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxy = { '/web/finder': { target: env.BACKEND_URL || 'http://localhost:8080', changeOrigin: true } };
  return { server: { port: 5173, strictPort: true, proxy }, preview: { port: 4173, proxy } };
});
