import { cn } from 'utils/Helper';

interface Props {
  value?: TimeRange;
  onChange: (range: TimeRange) => void;
}

const options: { label: string; value: TimeRange }[] = [
  { label: 'Last 30 Days', value: '30' },
  { label: 'Last 60 Days', value: '60' },
  { label: 'Last 90 Days', value: '90' },
];

const TimeRangeSelector = ({ value = '30', onChange }: Props) => {
  return (
    <div className="flex items-center gap-1">
      {options.map(opt => (
        <button
          key={opt.value}
          onClick={() => onChange(opt.value)}
          className={cn(
            'rounded-md px-3 py-1 text-xs font-medium transition-colors',
            value === opt.value
              ? 'bg-primary text-primary-foreground'
              : 'bg-secondary text-muted-foreground hover:text-foreground',
          )}
        >
          {opt.label}
        </button>
      ))}
    </div>
  );
};

export default TimeRangeSelector;

export type TimeRange = '30' | '60' | '90';
