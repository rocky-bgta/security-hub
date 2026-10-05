const SUBJECT_ALLOWED_REGEX = /^[A-Za-z0-9 \-'\.,()]+$/;

const DESCRIPTION_ALLOWED_REGEX =
  /^[A-Za-z0-9\s.,;:!?'"\-()[\]/\@#%&*_+=\r\n]*$/;

const HTML_SCRIPT_PATTERNS = [
  /<script\b/i,
  /<\/script>/i,
  /<iframe\b/i,
  /javascript:/i,
  /\bon\w+\s*=/i,
];

const HTML_TAG_PATTERN = /<[^>]+>/;

export const stripHtmlToText = (value: string): string => {
  if (!value) return '';

  if (typeof DOMParser !== 'undefined') {
    const doc = new DOMParser().parseFromString(value, 'text/html');
    return doc.body.textContent?.replace(/\u00a0/g, ' ') ?? '';
  }

  return value
    .replace(/<[^>]+>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/&[a-z]+;/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim();
};

export const containsHtmlOrScript = (value: string): boolean => {
  if (!value) return false;

  return (
    HTML_SCRIPT_PATTERNS.some(pattern => pattern.test(value)) ||
    HTML_TAG_PATTERN.test(value)
  );
};

export const isWhitespaceOnly = (value: string): boolean => {
  return stripHtmlToText(value).trim().length === 0;
};

export const validateSupportTicketSubject = (value: string): string | null => {
  const trimmed = value?.trim() ?? '';

  if (!trimmed) {
    return 'Subject is required.';
  }

  if (trimmed.length < 5) {
    return 'Subject must be at least 5 characters long.';
  }

  if (trimmed.length > 150) {
    return 'Subject cannot exceed 150 characters.';
  }

  if (!SUBJECT_ALLOWED_REGEX.test(trimmed) || containsHtmlOrScript(trimmed)) {
    return 'Subject contains unsupported characters.';
  }

  return null;
};

export const validateSupportTicketDescription = (
  value: string,
): string | null => {
  const raw = value ?? '';
  const text = stripHtmlToText(raw);

  if (!text) {
    return 'Description is required.';
  }

  if (/^\s+$/.test(text)) {
    return 'Please provide a meaningful description.';
  }

  if (text.length < 20) {
    return 'Description must be at least 20 characters long.';
  }

  if (text.length > 5000) {
    return 'Description cannot exceed 5,000 characters.';
  }

  if (
    HTML_SCRIPT_PATTERNS.some(pattern => pattern.test(raw)) ||
    !DESCRIPTION_ALLOWED_REGEX.test(text)
  ) {
    return 'Description contains unsupported content or characters.';
  }

  return null;
};

export const validateSupportTicketComment = (value: string): string | null => {
  const trimmed = value?.trim() ?? '';

  if (!trimmed) {
    return 'Please provide a meaningful comment.';
  }

  if (trimmed.length < 5) {
    return 'Comment must be at least 5 characters long.';
  }

  if (trimmed.length > 5000) {
    return 'Comment cannot exceed 5,000 characters.';
  }

  if (containsHtmlOrScript(trimmed)) {
    return 'Comment contains unsupported content or characters.';
  }

  return null;
};
