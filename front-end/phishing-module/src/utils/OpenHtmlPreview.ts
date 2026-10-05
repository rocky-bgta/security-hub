/**
 * Open app-generated HTML in a new tab via a blob URL (preview use only).
 * Local copy avoids module-federation lag when home-module/security is stale.
 */
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
