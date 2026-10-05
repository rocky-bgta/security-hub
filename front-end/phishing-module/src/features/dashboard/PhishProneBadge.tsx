import { cn } from 'utils/Helper';
import {
  getSimulationCopy,
  type SimulationChannel,
} from 'utils/SimulationChannel';

interface PhishProneBadgeProps {
  percentage: number;
  size?: 'sm' | 'md' | 'lg';
  channel?: SimulationChannel;
}

/**
 * Phish-prone percentage — same color system as KPI cards (tailored bands).
 */
export const PhishProneBadge = ({
  percentage,
  size = 'md',
  channel = 'phishing',
}: PhishProneBadgeProps) => {
  const copy = getSimulationCopy(channel);
  const clampedPercentage = Math.max(0, Math.min(100, percentage));
  const markerPosition = `${clampedPercentage}%`;

  const getRiskColor = () => {
    if (percentage >= 30)
      return { bg: 'bg-red-100', text: 'text-red-700', ring: 'ring-red-400' };
    if (percentage >= 20)
      return {
        bg: 'bg-orange-100',
        text: 'text-orange-700',
        ring: 'ring-orange-400',
      };
    if (percentage >= 10)
      return {
        bg: 'bg-yellow-100',
        text: 'text-yellow-700',
        ring: 'ring-yellow-400',
      };
    return {
      bg: 'bg-green-100',
      text: 'text-green-700',
      ring: 'ring-green-400',
    };
  };

  const colors = getRiskColor();

  const sizeClasses = {
    sm: 'size-20 text-xl',
    md: 'size-28 text-3xl',
    lg: 'size-36 text-4xl',
  };

  const labelSize = {
    sm: 'text-xs',
    md: 'text-sm',
    lg: 'text-base',
  };

  return (
    <div className="flex shrink-0 flex-col items-center text-center">
      <div
        className={cn(
          sizeClasses[size],
          colors.bg,
          'flex flex-col items-center justify-center rounded-full ring-4',
          colors.ring,
        )}
      >
        <span className={cn('font-bold tabular-nums', colors.text)}>
          {percentage.toFixed(1)}%
        </span>
      </div>
      <div className="mt-7 w-full px-4">
        <div className="relative h-2 overflow-hidden rounded-full bg-muted/40">
          <div className="absolute inset-y-0 left-0 w-[10%] bg-green-500/70" />
          <div className="absolute inset-y-0 left-[10%] w-[10%] bg-yellow-500/70" />
          <div className="absolute inset-y-0 left-[20%] w-[10%] bg-orange-500/70" />
          <div className="absolute inset-y-0 left-[30%] right-0 bg-red-500/70" />

          <div
            className="absolute top-1/2 z-10 size-3 -translate-x-1/2 -translate-y-1/2 rounded-full border-2 border-background bg-foreground shadow-sm"
            style={{ left: markerPosition }}
          />
        </div>
        <div className="mt-1 flex items-center justify-between text-[10px] text-muted-foreground">
          <span>0%</span>
          <span>10%</span>
          <span>20%</span>
          <span>30%</span>
          <span>40%</span>
          <span>50%</span>
          <span>60%</span>
          <span>70%</span>
          <span>80%</span>
          <span>90%</span>
          <span>100%</span>
        </div>
      </div>
      <span className={cn('mt-2 font-medium text-foreground', labelSize[size])}>
        {copy.proneBadgeLabel}
      </span>
      <span className="mt-1 text-xs text-muted-foreground">
        {percentage >= 30
          ? 'High Risk'
          : percentage >= 20
            ? 'Elevated'
            : percentage >= 10
              ? 'Moderate'
              : 'Low Risk'}
      </span>
    </div>
  );
};

export default PhishProneBadge;
