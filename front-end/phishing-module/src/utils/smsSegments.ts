export const SMS_MAX_CHARACTERS = 160;

/** Warning threshold at ~90% of single-SMS limit */
export const SMS_WARNING_CHARACTERS = 144;

export const PHISHING_LINK_TOKEN = '{{PHISHING_LINK}}';

export const SMS_PHISHING_PATH_SEGMENT_LENGTH = 10;

const PATH_SEGMENT_CHARS = 'xxxxxxxxxx';

export interface SmsCharInfo {
  chars: number;
  maxChars: number;
  remaining: number;
  isOverLimit: boolean;
  isNearLimit: boolean;
  progressPercent: number;
}

export interface SmsSegmentInfo {
  chars: number;
  segments: number;
  perSegment: number;
}

/** Estimate SMS segment count for multi-part messages */
export function calcSmsSegments(text: string): SmsSegmentInfo {
  const len = text.length;
  if (len === 0)
    return { chars: 0, segments: 0, perSegment: SMS_MAX_CHARACTERS };
  if (len <= SMS_MAX_CHARACTERS) {
    return { chars: len, segments: 1, perSegment: SMS_MAX_CHARACTERS };
  }
  const perSegment = 153;
  return {
    chars: len,
    segments: Math.ceil(len / perSegment),
    perSegment,
  };
}

/** Build a preview phishing URL using the selected tracking domain */
export function buildSmsPhishingPreviewUrl(
  domain: string,
  pathSegment: string,
): string {
  const host = domain.replace(/^https?:\/\//i, '').replace(/\/+$/, '');
  return `https://${host}/${pathSegment}`;
}

/** Generate a 10-character placeholder path segment */
export function createSmsPhishingPathSegment(
  length = SMS_PHISHING_PATH_SEGMENT_LENGTH,
): string {
  let segment = '';
  for (let i = 0; i < length; i += 1) {
    segment += PATH_SEGMENT_CHARS.charAt(
      Math.floor(Math.random() * PATH_SEGMENT_CHARS.length),
    );
  }
  return segment;
}

/** Replace {{PHISHING_LINK}} with the preview URL (all occurrences) */
export function expandSmsPhishingLink(
  text: string,
  previewUrl?: string,
): string {
  if (!previewUrl || !text.includes(PHISHING_LINK_TOKEN)) return text;
  return text.split(PHISHING_LINK_TOKEN).join(previewUrl);
}

/** Effective SMS length counting the phishing URL instead of the placeholder */
export function getSmsEffectiveLength(
  text: string,
  phishingPreviewUrl?: string,
): number {
  return expandSmsPhishingLink(text, phishingPreviewUrl).length;
}

export function getSmsCharInfo(
  text: string,
  phishingPreviewUrl?: string,
): SmsCharInfo {
  const chars = getSmsEffectiveLength(text, phishingPreviewUrl);
  const maxChars = SMS_MAX_CHARACTERS;
  const remaining = Math.max(0, maxChars - chars);
  const isOverLimit = chars > maxChars;
  const isNearLimit = chars >= SMS_WARNING_CHARACTERS && !isOverLimit;
  const progressPercent = Math.min(100, (chars / maxChars) * 100);

  return {
    chars,
    maxChars,
    remaining,
    isOverLimit,
    isNearLimit,
    progressPercent,
  };
}

/**
 * Truncate raw SMS body so that after phishing-link expansion it fits
 * within the single-SMS character limit.
 */
export function truncateSmsBodyToEffectiveLimit(
  text: string,
  phishingPreviewUrl?: string,
  maxChars: number = SMS_MAX_CHARACTERS,
): string {
  if (getSmsEffectiveLength(text, phishingPreviewUrl) <= maxChars) {
    return text;
  }

  if (!phishingPreviewUrl || !text.includes(PHISHING_LINK_TOKEN)) {
    return text.slice(0, maxChars);
  }

  const parts = text.split(PHISHING_LINK_TOKEN);
  const urlLen = phishingPreviewUrl.length;
  const tokenCount = parts.length - 1;
  const budgetForOther = maxChars - tokenCount * urlLen;

  if (budgetForOther < 0) {
    // URL alone exceeds limit; keep tokens and empty surrounding text
    return Array(tokenCount).fill(PHISHING_LINK_TOKEN).join('');
  }

  let remaining = budgetForOther;
  const kept: string[] = [];

  for (let i = 0; i < parts.length; i += 1) {
    const part = parts[i];
    if (part.length <= remaining) {
      kept.push(part);
      remaining -= part.length;
    } else {
      kept.push(part.slice(0, remaining));
      remaining = 0;
      for (let j = i + 1; j < parts.length; j += 1) {
        kept.push('');
      }
      break;
    }
  }

  return kept.join(PHISHING_LINK_TOKEN);
}

/** Strip basic HTML tags for legacy SMS templates stored as HTML */
export function stripHtmlToPlainText(html: string): string {
  if (!html) return '';
  return html
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/p>/gi, '\n')
    .replace(/<[^>]+>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .trim();
}

export function getSmsBodyFromPreview(
  preview:
    | { smsBody?: string; emailBodyText?: string; emailBody?: string }
    | null
    | undefined,
): string {
  if (!preview) return '';
  if (preview.smsBody?.trim()) return preview.smsBody;
  if (preview.emailBodyText?.trim()) return preview.emailBodyText;
  if (preview.emailBody) return stripHtmlToPlainText(preview.emailBody);
  return '';
}
