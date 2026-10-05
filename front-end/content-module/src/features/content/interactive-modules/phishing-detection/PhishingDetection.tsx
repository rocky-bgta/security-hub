import { Clock, Trophy } from 'lucide-react';

import { DEFAULT_PHISHING_TIME_LIMIT, IPhishingEmail } from 'models/Content';
import { cn } from 'utils/Helper';

import ClassifyFeedback from './components/ClassifyFeedback';
import EmailViewer from './components/EmailViewer';
import GameOverDialog from './components/GameOverDialog';
import InboxList from './components/InboxList';
import MailboxSidebar from './components/MailboxSidebar';
import UrlPreview from './components/UrlPreview';
import { usePhishingDetection } from './hooks/usePhishingDetection';
import { formatCountdown } from './types';

const EMPTY_EMAILS: Array<IPhishingEmail> = [];

export interface PhishingDetectionProps {
  title?: string;
  description?: string;
  className?: string;
  emails?: Array<IPhishingEmail>;
  timeLimitSeconds?: number;
  onGameOver?: () => void;
}

const PhishingDetectionGame = ({
  title = 'Phishing Detection Game',
  description = 'Inspect each email, hover suspicious links, and decide whether it is phishing or safe.',
  className,
  emails = EMPTY_EMAILS,
  timeLimitSeconds = DEFAULT_PHISHING_TIME_LIMIT,
  onGameOver,
}: PhishingDetectionProps) => {
  const game = usePhishingDetection({
    emails,
    timeLimitSeconds,
    onGameOver,
  });

  return (
    <div
      className={cn(
        'content-mx-auto content-relative content-flex content-min-h-0 content-w-full content-max-w-7xl content-flex-col content-overflow-hidden content-p-3 md:content-p-4',
        className,
      )}
    >
      <header className="content-mb-3 content-flex content-shrink-0 content-flex-col content-gap-2 md:content-flex-row md:content-items-center md:content-justify-between">
        <div>
          <h1 className="content-mb-1 content-text-xl content-font-bold content-text-white md:content-text-2xl">
            {title}
          </h1>
          {description ? (
            <p className="content-text-sm content-leading-snug content-text-muted-foreground">
              {description}
            </p>
          ) : null}
        </div>
        <div className="content-flex content-gap-2">
          <div className="content-flex content-items-center content-gap-2 content-rounded-full content-border content-border-white/10 content-bg-card-background/80 content-px-2.5 content-py-1 content-text-xs content-text-white md:content-text-sm">
            <Trophy className="content-size-3.5 content-text-primary" />
            Score: {game.score}
          </div>
          <div className="content-flex content-items-center content-gap-2 content-rounded-full content-border content-border-white/10 content-bg-card-background/80 content-px-2.5 content-py-1 content-text-xs content-text-white md:content-text-sm">
            <Clock className="content-size-3.5 content-text-primary" />
            Time: {formatCountdown(game.timeLeft)}
          </div>
        </div>
      </header>

      <div className="content-grid content-min-h-[280px] content-flex-1 content-grid-cols-1 content-gap-3 lg:content-grid-cols-[200px_minmax(0,1fr)_minmax(0,1.3fr)]">
        <MailboxSidebar unreadCount={game.unreadCount} />

        <InboxList
          emails={game.inbox}
          selectedId={game.selectedEmail?.id ?? null}
          totalCount={game.inbox.length}
          unreadCount={game.unreadCount}
          onSelect={game.openEmail}
          className={cn(
            game.showMobileViewer && game.selectedEmail
              ? 'content-hidden lg:content-flex'
              : 'content-flex',
          )}
        />

        <EmailViewer
          email={game.selectedEmail}
          hasProcessedAll={game.inbox.length === 0}
          onBack={game.closeMobileViewer}
          onClassify={game.classifyEmail}
          onHoverUrl={game.setHoveredUrl}
          className={cn(
            'content-hidden lg:content-flex',
            game.showMobileViewer && game.selectedEmail && 'content-flex',
          )}
        />
      </div>

      <ClassifyFeedback feedback={game.feedback} />
      <UrlPreview url={game.hoveredUrl} />
      <GameOverDialog
        open={game.isGameOver}
        score={game.score}
        correctCount={game.correctCount}
        totalEmails={game.totalEmails}
        onPlayAgain={game.resetGame}
      />
    </div>
  );
};

const PhishingDetection = (props: PhishingDetectionProps) => {
  const payloadKey = JSON.stringify({
    emails: props.emails ?? EMPTY_EMAILS,
    timeLimitSeconds: props.timeLimitSeconds ?? DEFAULT_PHISHING_TIME_LIMIT,
  });

  return <PhishingDetectionGame key={payloadKey} {...props} />;
};

export default PhishingDetection;
