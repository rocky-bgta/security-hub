import { Button } from 'common/Button';

interface IProps {
  open: boolean;
  score: number;
  correctCount: number;
  totalEmails: number;
  onPlayAgain: () => void;
}

const GameOverDialog = ({
  open,
  score,
  correctCount,
  totalEmails,
  onPlayAgain,
}: IProps) => {
  if (!open) return null;

  return (
    <div
      className="content-absolute content-inset-0 content-z-50 content-flex content-items-center content-justify-center content-bg-black/50 content-p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="phishing-game-over-title"
    >
      <div className="content-w-full content-max-w-md content-rounded-lg content-border content-border-card-border content-bg-dark content-p-6 content-shadow-lg">
        <div className="content-mb-4 content-text-center">
          <h2
            id="phishing-game-over-title"
            className="content-text-lg content-font-semibold content-leading-none content-tracking-tight content-text-white"
          >
            Game Over!
          </h2>
          <p className="content-mt-2 content-text-sm content-text-muted-foreground">
            You&apos;ve successfully identified {correctCount} out of{' '}
            {totalEmails} emails.
          </p>
        </div>
        <p className="content-mb-4 content-text-center content-text-5xl content-font-bold content-text-primary">
          {score}
        </p>
        <div className="content-flex content-justify-center">
          <Button type="button" onClick={onPlayAgain}>
            Play Again
          </Button>
        </div>
      </div>
    </div>
  );
};

export default GameOverDialog;
