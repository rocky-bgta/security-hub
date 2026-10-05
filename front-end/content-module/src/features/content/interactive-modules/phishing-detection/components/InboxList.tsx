import { Inbox } from 'lucide-react';

import { Badge } from 'common/Badge';
import { cn } from 'utils/Helper';

import {
  getAvatarInitials,
  IPhishingGameEmail,
  stripHtml,
} from '../types';

interface IProps {
  emails: Array<IPhishingGameEmail>;
  selectedId: string | null;
  totalCount: number;
  unreadCount: number;
  onSelect: (id: string) => void;
  className?: string;
}

const InboxList = ({
  emails,
  selectedId,
  totalCount,
  unreadCount,
  onSelect,
  className,
}: IProps) => {
  return (
    <section
      className={cn(
        'content-flex content-min-h-0 content-flex-col content-rounded-lg content-border content-border-white/10 content-bg-card-background/80 content-p-3 content-shadow-sm content-backdrop-blur-sm',
        className,
      )}
    >
      <div className="content-mb-2 content-flex content-flex-wrap content-items-center content-justify-between content-gap-2">
        <h2 className="content-text-sm content-font-semibold content-text-white">
          Inbox
        </h2>
        <div className="content-flex content-gap-1.5 content-text-xs content-text-muted-foreground">
          <span className="content-rounded-md content-border content-border-white/10 content-px-2 content-py-0.5">
            Total:{' '}
            <span className="content-font-semibold content-text-white">{totalCount}</span>
          </span>
          <span className="content-rounded-md content-border content-border-white/10 content-px-2 content-py-0.5">
            Unread:{' '}
            <span className="content-font-semibold content-text-white">{unreadCount}</span>
          </span>
        </div>
      </div>

      {emails.length === 0 ? (
        <div className="content-flex content-flex-1 content-flex-col content-items-center content-justify-center content-gap-2 content-py-8 content-text-center content-text-muted-foreground">
          <Inbox className="content-size-8 content-opacity-40" />
          <h3 className="content-text-sm content-text-white">No emails found</h3>
          <p className="content-text-xs">Your inbox is empty.</p>
        </div>
      ) : (
        <ul className="content-min-h-0 content-flex-1 content-space-y-1.5 content-overflow-y-auto">
          {emails.map(email => {
            const preview = stripHtml(email.body);
            const isSelected = selectedId === email.id;

            return (
              <li key={email.id}>
                <button
                  type="button"
                  onClick={() => onSelect(email.id)}
                  className={cn(
                    'content-w-full content-rounded-md content-border content-p-2 content-text-left content-transition-colors',
                    isSelected
                      ? 'content-border-primary content-bg-primary/10'
                      : 'content-border-white/10 content-bg-white/5 hover:content-border-primary/60 hover:content-bg-white/10',
                  )}
                >
                  <div className="content-mb-1 content-flex content-items-start content-justify-between content-gap-2">
                    <div className="content-flex content-min-w-0 content-items-center content-gap-2">
                      <div className="content-flex content-size-8 content-shrink-0 content-items-center content-justify-center content-rounded-full content-bg-primary/15 content-text-[10px] content-font-bold content-text-primary">
                        {getAvatarInitials(email.senderName, email.avatar)}
                      </div>
                      <div className="content-min-w-0">
                        <p className="content-truncate content-text-sm content-font-semibold content-text-white">
                          {email.senderName}
                        </p>
                        <p className="content-truncate content-text-xs content-text-muted-foreground">
                          {email.senderEmail} • {email.time}
                        </p>
                      </div>
                    </div>
                    {!email.read ? (
                      <Badge className="content-shrink-0">Unread</Badge>
                    ) : null}
                  </div>
                  <h3 className="content-truncate content-text-sm content-font-semibold content-text-white">
                    {email.subject}
                  </h3>
                  {preview ? (
                    <p className="content-mt-0.5 content-line-clamp-1 content-text-xs content-text-muted-foreground">
                      {preview}
                    </p>
                  ) : null}
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
};

export default InboxList;
