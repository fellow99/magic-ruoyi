import type { Plugin } from 'vite';
import path from 'path';
import fs from 'fs';

/**
 * Vite plugin to serve Monaco Editor web workers.
 *
 * The @fellow99/magic-editor package ships pre-built worker files in
 * dist/assets/ (e.g. editor.worker-*.js, json.worker-*.js).
 * At runtime the magic-editor bundle sets self.MonacoEnvironment.getWorker
 * to create Workers at absolute paths like /assets/editor.worker-*.js.
 *
 * This middleware intercepts those requests and serves the physical files
 * from the magic-editor package's dist/assets directory.
 *
 * Falls back to esbuild-bundling from monaco-editor ESM source for any
 * worker files NOT found in magic-editor's pre-built assets.
 */
export default function monacoEditorWorker(): Plugin {
  const magicEditorAssets = path.resolve(
    __dirname,
    '../../node_modules/@fellow99/magic-editor/dist/assets'
  );
  const monacoEsm = path.resolve(
    __dirname,
    '../../node_modules/.pnpm/monaco-editor@0.55.1/node_modules/monaco-editor/esm'
  );
  const esbuildPath = path.resolve(
    __dirname,
    '../../node_modules/.pnpm/esbuild@0.25.12/node_modules/esbuild/bin/esbuild'
  );

  return {
    name: 'vite-plugin-monaco-editor-worker',
    configureServer(server) {
      server.middlewares.use(async (req, res, next) => {
        const url = req.url || '';
        if (!url.startsWith('/assets/') || !url.includes('.worker')) {
          return next();
        }

        const fileName = url.split('/').pop() || '';

        // 1) Try pre-built worker from magic-editor dist/assets first
        const prebuiltPath = path.join(magicEditorAssets, fileName);
        if (fs.existsSync(prebuiltPath)) {
          res.setHeader('Content-Type', 'application/javascript');
          res.setHeader('Cache-Control', 'public, max-age=31536000');
          fs.createReadStream(prebuiltPath).pipe(res);
          return;
        }

        // 2) Fallback: bundle from monaco-editor ESM source via esbuild
        let entryPath = '';
        if (fileName.startsWith('editor.worker')) {
          entryPath = path.join(monacoEsm, 'vs/editor/editor.worker.js');
        } else if (fileName.startsWith('json.worker')) {
          entryPath = path.join(monacoEsm, 'vs/language/json/json.worker.js');
        } else if (fileName.startsWith('css.worker')) {
          entryPath = path.join(monacoEsm, 'vs/language/css/css.worker.js');
        } else if (fileName.startsWith('html.worker')) {
          entryPath = path.join(monacoEsm, 'vs/language/html/html.worker.js');
        } else if (fileName.startsWith('ts.worker')) {
          entryPath = path.join(monacoEsm, 'vs/language/typescript/ts.worker.js');
        }

        if (entryPath && fs.existsSync(entryPath)) {
          try {
            const { execSync } = await import('child_process');
            const result = execSync(
              `${JSON.stringify(esbuildPath)} ${JSON.stringify(entryPath)} --bundle --format=iife --target=es2020 --external:crypto`,
              { encoding: 'utf-8', maxBuffer: 50 * 1024 * 1024 }
            );
            res.setHeader('Content-Type', 'application/javascript');
            res.end(result);
          } catch {
            res.writeHead(500);
            res.end('Worker build failed');
          }
        } else {
          next();
        }
      });
    }
  };
}
