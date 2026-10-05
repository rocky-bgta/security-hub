import { IPhishingEmail, IPhishingEmailLink } from 'models/Content';

export type { IPhishingEmail, IPhishingEmailLink };

export interface IPhishingGameEmail extends IPhishingEmail {
  read: boolean;
}

export interface IClassifyFeedback {
  isCorrect: boolean;
  message: string;
}

export interface IMailboxFolder {
  id: string;
  label: string;
  icon: 'inbox' | 'junk' | 'drafts' | 'sent' | 'scheduled' | 'deleted' | 'archive' | 'notes';
  disabled: boolean;
}

export const MAILBOX_FOLDERS: Array<IMailboxFolder> = [
  { id: 'inbox', label: 'Inbox', icon: 'inbox', disabled: false },
  { id: 'junk', label: 'Junk Email', icon: 'junk', disabled: true },
  { id: 'drafts', label: 'Drafts', icon: 'drafts', disabled: true },
  { id: 'sent', label: 'Sent Items', icon: 'sent', disabled: true },
  { id: 'scheduled', label: 'Scheduled', icon: 'scheduled', disabled: true },
  { id: 'deleted', label: 'Deleted Items', icon: 'deleted', disabled: true },
  { id: 'archive', label: 'Archive', icon: 'archive', disabled: true },
  { id: 'notes', label: 'Notes', icon: 'notes', disabled: true },
];

export const cloneInbox = (
  emails: Array<IPhishingEmail>,
): Array<IPhishingGameEmail> =>
  emails.map(email => ({
    ...email,
    links: (email.links ?? []).map(link => ({ ...link })),
    read: false,
  }));

export const formatCountdown = (seconds: number) => {
  const safe = Math.max(0, seconds);
  const minutes = Math.floor(safe / 60);
  const remaining = safe % 60;
  return `${String(minutes).padStart(2, '0')}:${String(remaining).padStart(2, '0')}`;
};

export const getAvatarInitials = (senderName: string, avatar?: string) => {
  if (avatar?.trim()) {
    return avatar.trim().slice(0, 3).toUpperCase();
  }

  const parts = senderName.trim().split(/\s+/).filter(Boolean);
  if (parts.length === 0) return '?';
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();

  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
};

export const stripHtml = (html: string) =>
  html
    .replace(/<[^>]*>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/\s+/g, ' ')
    .trim();
