import { ArrowLeft, Calendar, MailOpen, Shield, TriangleAlert, User } from 'lucide-react';

import { Button } from 'common/Button';
import { cn } from 'utils/Helper';

import { IPhishingEmailLink, IPhishingGameEmail } from '../types';

interface IProps {
  email: IPhishingGameEmail | null;
  hasProcessedAll: boolean;
  onBack?: () => void;
  onClassify: (isPhishing: boolean) => void;
  onHoverUrl: (url: string | null) => void;
  className?: string;
}

const EmailLink = ({
  link,
  onHoverUrl,
}: {
  link: IPhishingEmailLink;
  onHoverUrl: (url: string | null) => void;
}) => {
  return (
    <span className="content-inline">
      {link.label?.trim() ? (
        <span className="content-mr-1">{link.label.trim()}</span>
      ) : null}
      <button
        type="button"
        className="content-font-medium content-text-primary content-underline content-underline-offset-2 hover:content-text-primary/80"
        onMouseEnter={() => onHoverUrl(link.actualUrl)}
        onMouseLeave={() => onHoverUrl(null)}
        onFocus={() => onHoverUrl(link.actualUrl)}
        onBlur={() => onHoverUrl(null)}
        onClick={event => {
          event.preventDefault();
        }}
      >
        {link.displayText}
      </button>
    </span>
  );
};

const EmailViewer = ({
  email,
  hasProcessedAll,
  onBack,
  onClassify,
  onHoverUrl,
  className,
}: IProps) => {
  return (
    <section
      className={cn(
        'content-flex content-min-h-0 content-flex-col content-rounded-lg content-border content-border-white/10 content-bg-card-background/80 content-p-3 content-shadow-sm content-backdrop-blur-sm',
        className,
      )}
    >
      {!email ? (
        <div className="content-flex content-flex-1 content-flex-col content-items-center content-justify-center content-gap-2 content-py-8 content-text-center content-text-muted-foreground">
          <MailOpen className="content-size-8 content-opacity-40" />
          <h3 className="content-text-sm content-text-white">
            {hasProcessedAll ? 'All emails processed!' : 'Select an email to view'}
          </h3>
          <p className="content-max-w-sm content-text-xs md:content-text-sm">
            {hasProcessedAll
              ? "You've reviewed all emails in your inbox."
              : "Choose an email from your inbox to determine if it's a phishing attempt or legitimate."}
          </p>
        </div>
      ) : (
        <>
          <Button
            type="button"
            variant="ghost"
            size="sm"
            className="content-mb-2 content-w-fit lg:content-hidden"
            onClick={onBack}
          >
            <ArrowLeft className="content-size-4" />
            Back to inbox
          </Button>

          <div className="content-mb-2 content-border-b content-border-white/10 content-pb-2">
            <h2 className="content-text-sm content-font-semibold content-text-white md:content-text-base">
              {email.subject}
            </h2>
          </div>

          <div className="content-mb-2 content-space-y-1 content-text-xs md:content-text-sm">
            <div className="content-flex content-items-start content-gap-2">
              <User className="content-mt-0.5 content-size-3.5 content-shrink-0 content-text-primary" />
              <p>
                <span className="content-mr-2 content-font-semibold content-text-muted-foreground">
                  From:
                </span>
                <span className="content-text-white">
                  {email.senderName} &lt;{email.senderEmail}&gt;
                </span>
              </p>
            </div>
            <div className="content-flex content-items-start content-gap-2">
              <Calendar className="content-mt-0.5 content-size-3.5 content-shrink-0 content-text-primary" />
              <p>
                <span className="content-mr-2 content-font-semibold content-text-muted-foreground">
                  Date:
                </span>
                <span className="content-text-white">
                  {email.date} at {email.time}
                </span>
              </p>
            </div>
          </div>

          <p className="content-mb-2 content-text-xs content-text-muted-foreground">
            Hover or focus links to inspect the real destination. Links never
            open.
          </p>

          <div className="content-min-h-0 content-flex-1 content-overflow-y-auto content-rounded-md content-border content-border-white/10 content-bg-white/5 content-p-3">
            <div
              className="content-max-w-none content-text-sm content-leading-relaxed content-text-white [&_a]:content-pointer-events-none [&_p]:content-mb-3"
              dangerouslySetInnerHTML={{ __html: email.body }}
            />
            {(email.links ?? []).length > 0 ? (
              <div className="content-mt-3 content-space-y-1.5 content-border-t content-border-white/10 content-pt-3">
                {email.links.map(link => (
                  <div key={link.id}>
                    <EmailLink link={link} onHoverUrl={onHoverUrl} />
                  </div>
                ))}
              </div>
            ) : null}
          </div>

          <div className="content-mt-2 content-grid content-grid-cols-1 content-gap-2 sm:content-grid-cols-2">
            <Button
              type="button"
              size="sm"
              variant="destructive"
              onClick={() => onClassify(true)}
            >
              <TriangleAlert className="content-size-4" />
              Phishing
            </Button>
            <Button type="button" size="sm" onClick={() => onClassify(false)}>
              <Shield className="content-size-4" />
              Safe
            </Button>
          </div>
        </>
      )}
    </section>
  );
};

export default EmailViewer;
