import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

/**
 * originjs federation emits CSS as basename "style-xxxxx.css" and joins it to
 * Vite `base` (the module root). Hashed CSS lives in dist/assets/, which is the
 * S3 layout: web-static/dev/<module>/assets/style-*.css
 *
 * Prefix those names with assets/ and keep joining against the module-root base.
 */
export function patchFederationCssBase(source) {
  let text = source.replace(/^\uFEFF/, '');
  text = text.replace(
    /(\[["'])(?!assets\/)(style-[A-Za-z0-9_-]+\.css)(["']\])/g,
    '$1assets/$2$3',
  );

  text = text.replace(
    /(\w+)='https?:\/\/[^']+\/';'assets',(\w+)\.forEach\((\w+)=>\{let (\w+)="";const (\w+)=(\w+)\|\|(\w+)/,
    (match, baseVar, _arr, _item, _href, joinVar, left, right) => {
      if (left === baseVar) {
        return match;
      }
      if (right === baseVar) {
        return match.replace(
          `const ${joinVar}=${left}||${right}`,
          `const ${joinVar}=${baseVar}||${left}`,
        );
      }
      return match;
    },
  );

  return text;
}

export function federationCSS() {
  return {
    name: 'federation-css-assets-prefix',
    closeBundle() {
      const moduleRoot = path.dirname(fileURLToPath(import.meta.url));
      const remoteEntry = path.join(
        moduleRoot,
        'dist',
        'assets',
        'remoteEntry.js',
      );
      if (!fs.existsSync(remoteEntry)) {
        return;
      }
      const source = fs.readFileSync(remoteEntry, 'utf8');
      const patched = patchFederationCssBase(source);
      if (patched !== source) {
        fs.writeFileSync(remoteEntry, patched);
      }
    },
  };
}
