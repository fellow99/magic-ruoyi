import type { Plugin } from 'vite';
import path from 'path';
import fs from 'fs';
import { execSync } from 'child_process';

export default function monacoEditorWorker(): Plugin {
  const monacoEsm = path.resolve(
    __dirname,
    '../../node_modules/.pnpm/monaco-editor@0.29.1/node_modules/monaco-editor/esm'
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
        if (url.startsWith('/assets/') && url.includes('.worker')) {
          const fileName = url.split('/').pop() || '';
          let entryPath = '';
          if (fileName.startsWith('editor.worker')) {
            entryPath = path.join(monacoEsm, 'vs/editor/editor.worker.js');
          } else if (fileName.startsWith('json.worker')) {
            entryPath = path.join(monacoEsm, 'vs/language/json/json.worker.js');
          }
          if (entryPath && fs.existsSync(entryPath)) {
            try {
              const result = execSync(
                `${esbuildPath} "${entryPath}" --bundle --format=iife --target=es2020 --external:crypto`,
                { encoding: 'utf-8', maxBuffer: 50 * 1024 * 1024 }
              );
              res.setHeader('Content-Type', 'application/javascript');
              res.end(result);
            } catch (e) {
              res.writeHead(500);
              res.end(String(e));
            }
          } else {
            next();
          }
        } else {
          next();
        }
      });
    }
  };
}
