import DOMPurify, { type Config } from 'dompurify';

const DEFAULT_HTML_CONFIG: Config = {
  USE_PROFILES: { html: true },
  ADD_ATTR: ['target', 'rel', 'class', 'style', 'id'],
};

const EMPTY_HTML_DOCUMENT =
  '<!DOCTYPE html><html><head></head><body></body></html>';

const ALLOWED_EXEC_COMMANDS = new Set([
  'copy',
  'cut',
  'paste',
  'bold',
  'createLink',
  'fontName',
  'fontSize',
  'foreColor',
  'formatBlock',
  'hiliteColor',
  'indent',
  'insertHTML',
  'insertImage',
  'insertOrderedList',
  'insertUnorderedList',
  'italic',
  'justifyCenter',
  'justifyLeft',
  'justifyRight',
  'outdent',
  'redo',
  'removeFormat',
  'strikeThrough',
  'subscript',
  'superscript',
  'underline',
  'undo',
]);

/** Sanitize HTML before rendering with dangerouslySetInnerHTML or iframe injection. */
export function sanitizeHtml(html: string, config?: Config): string {
  if (!html) return '';
  return DOMPurify.sanitize(html, { ...DEFAULT_HTML_CONFIG, ...config });
}

/** Validate URLs used with window.open or navigation. */
export function isAllowedExternalUrl(url: string): boolean {
  const trimmed = url?.trim();
  if (!trimmed) return false;

  const lower = trimmed.toLowerCase();
  if (lower.startsWith('javascript:') || lower.startsWith('data:')) {
    return false;
  }

  if (trimmed.startsWith('/') && !trimmed.startsWith('//')) {
    return true;
  }

  try {
    const parsed = new URL(trimmed, window.location.origin);
    if (parsed.protocol === 'http:' || parsed.protocol === 'https:') {
      return true;
    }
    return false;
  } catch {
    return false;
  }
}

/** Open a URL in a new tab only when it passes validation. */
export function safeWindowOpen(
  url: string,
  target = '_blank',
  features?: string,
): Window | null {
  if (!isAllowedExternalUrl(url)) {
    console.warn('[security] Blocked window.open for untrusted URL:', url);
    return null;
  }
  const opened = window.open(url, target, features);
  if (opened) {
    opened.opener = null;
  }
  return opened;
}

/** Open app-generated HTML in a new tab via a blob URL (preview use only). */
export function openHtmlInNewTab(html: string): Window | null {
  if (!html?.trim()) return null;

  const blob = new Blob([html], { type: 'text/html' });
  const url = URL.createObjectURL(blob);
  const opened = window.open(url, '_blank', 'noopener,noreferrer');
  if (opened) {
    opened.opener = null;
  }
  return opened;
}

/** Navigate only to validated checkout/API redirect URLs (https). */
export function safeRedirect(url: string): boolean {
  if (!isAllowedExternalUrl(url)) {
    console.warn('[security] Blocked redirect to untrusted URL:', url);
    return false;
  }
  window.location.href = url;
  return true;
}

/** Run document.execCommand with an allowlist; sanitizes insertHTML payloads. */
export function safeExecCommand(
  doc: Document,
  command: string,
  value?: string,
): boolean {
  if (!ALLOWED_EXEC_COMMANDS.has(command)) {
    console.warn('[security] Blocked execCommand:', command);
    return false;
  }

  if (command === 'insertHTML' && value !== undefined) {
    return doc.execCommand(command, false, sanitizeHtml(value));
  }

  if (command === 'createLink' && value !== undefined) {
    if (!isAllowedExternalUrl(value)) return false;
  }

  return doc.execCommand(command, false, value);
}

/**
 * Load HTML into an iframe document without document.write().
 * Content is sanitized before injection.
 */
export function loadHtmlIntoDocument(doc: Document, html: string): void {
  const safeHtml = sanitizeHtml(html) || EMPTY_HTML_DOCUMENT;
  const parsed = new DOMParser().parseFromString(safeHtml, 'text/html');

  doc.open();
  doc.close();

  if (parsed.head && doc.head) {
    doc.head.innerHTML = parsed.head.innerHTML;
  }
  if (parsed.body && doc.body) {
    doc.body.innerHTML = parsed.body.innerHTML;
  }

  Array.from(parsed.documentElement.attributes).forEach(({ name, value }) => {
    doc.documentElement.setAttribute(name, value);
  });
}

export { EMPTY_HTML_DOCUMENT };
