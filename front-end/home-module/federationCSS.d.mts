import type { Plugin } from 'vite';

export function patchFederationCssBase(source: string): string;
export function federationCSS(): Plugin;
