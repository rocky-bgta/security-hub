import { StrengthLabel } from '../types';
import { getStrengthBarClass } from '../utils/compliance';
import { cn } from 'utils/Helper';

interface IProps {
  strength: number;
  label: StrengthLabel;
}

const StrengthBar = ({ strength, label }: IProps) => {
  return (
    <div>
      <div className="content-mb-1 content-flex content-items-center content-justify-between">
        <span className="content-text-sm content-font-medium content-text-white">
          Password Strength
        </span>
        <span
          className="content-text-sm content-font-medium content-text-muted-foreground"
          aria-live="polite"
        >
          {label}
        </span>
      </div>
      <div
        className="content-h-2.5 content-w-full content-overflow-hidden content-rounded-full content-bg-steel-gray"
        role="progressbar"
        aria-valuenow={strength}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label={`Password strength: ${label}`}
      >
        <div
          className={cn(
            'content-h-2.5 content-rounded-full content-transition-all content-duration-500',
            getStrengthBarClass(strength),
          )}
          style={{ width: `${strength}%` }}
        />
      </div>
    </div>
  );
};

export default StrengthBar;
