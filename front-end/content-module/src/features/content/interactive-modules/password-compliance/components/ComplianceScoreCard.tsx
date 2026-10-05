import { getScoreToneClass } from '../utils/compliance';
import { cn } from 'utils/Helper';

interface IProps {
  score: number;
}

const ComplianceScoreCard = ({ score }: IProps) => {
  return (
    <div className="content-flex content-items-center content-justify-between content-gap-3 content-rounded-lg content-border content-border-primary/30 content-bg-primary/10 content-p-3">
      <div>
        <h3 className="content-text-sm content-font-medium content-text-white md:content-text-base">
          Overall Compliance
        </h3>
        <p className="content-text-xs content-text-muted-foreground md:content-text-sm">
          Percentage of frameworks satisfied
        </p>
      </div>
      <div
        className={cn(
          'content-text-2xl content-font-bold content-tabular-nums',
          getScoreToneClass(score),
        )}
        aria-live="polite"
        aria-label={`Overall compliance ${score} percent`}
      >
        {score}%
      </div>
    </div>
  );
};

export default ComplianceScoreCard;
