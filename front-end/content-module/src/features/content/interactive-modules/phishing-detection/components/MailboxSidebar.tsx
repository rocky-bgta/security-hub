import {
  Archive,
  CalendarClock,
  FileText,
  Inbox,
  Send,
  StickyNote,
  Trash2,
  TriangleAlert,
} from 'lucide-react';

import { Badge } from 'common/Badge';
import { cn } from 'utils/Helper';

import { IMailboxFolder, MAILBOX_FOLDERS } from '../types';

interface IProps {
  unreadCount: number;
  className?: string;
}

const FOLDER_ICONS: Record<
  IMailboxFolder['icon'],
  typeof Inbox
> = {
  inbox: Inbox,
  junk: TriangleAlert,
  drafts: FileText,
  sent: Send,
  scheduled: CalendarClock,
  deleted: Trash2,
  archive: Archive,
  notes: StickyNote,
};

const MailboxSidebar = ({ unreadCount, className }: IProps) => {
  return (
    <aside
      className={cn(
        'content-rounded-lg content-border content-border-white/10 content-bg-card-background/80 content-p-3 content-shadow-sm content-backdrop-blur-sm',
        className,
      )}
    >
      <h3 className="content-mb-2 content-px-2 content-text-xs content-font-semibold content-uppercase content-tracking-wide content-text-muted-foreground">
        Mailboxes
      </h3>
      <ul className="content-flex content-gap-1.5 content-overflow-x-auto lg:content-flex-col lg:content-overflow-visible">
        {MAILBOX_FOLDERS.map(folder => {
          const Icon = FOLDER_ICONS[folder.icon];
          const isInbox = folder.id === 'inbox';

          return (
            <li key={folder.id} className="content-shrink-0 lg:content-w-full">
              <div
                className={cn(
                  'content-flex content-items-center content-justify-between content-gap-2 content-rounded-md content-px-2 content-py-1.5 content-text-sm',
                  isInbox
                    ? 'content-bg-primary content-text-secondary'
                    : 'content-text-muted-foreground',
                  folder.disabled && 'content-cursor-not-allowed content-opacity-60',
                )}
                aria-disabled={folder.disabled}
              >
                <span className="content-flex content-items-center content-gap-2">
                  <Icon className="content-size-4" />
                  <span className="content-whitespace-nowrap">{folder.label}</span>
                </span>
                {isInbox && unreadCount > 0 ? (
                  <Badge
                    variant="destructive"
                    className="content-min-w-6 content-justify-center content-border-0"
                  >
                    {unreadCount}
                  </Badge>
                ) : null}
              </div>
            </li>
          );
        })}
      </ul>
    </aside>
  );
};

export default MailboxSidebar;
