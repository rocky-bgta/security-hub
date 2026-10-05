import { RadioGroup, RadioGroupItem } from 'common/Radio';
import { Check, Info } from 'lucide-react';
import { cn } from 'utils/Helper';

export type SmsDeliveryMode = 'single' | 'multi';

interface SmsDeliveryOption {
  id: SmsDeliveryMode;
  title: string;
  rules: string[];
}

const DELIVERY_OPTIONS: SmsDeliveryOption[] = [
  {
    id: 'single',
    title: 'Single SMS (160 chars max)',
    rules: [
      'Message must be 160 characters or less.',
      "If message exceeds 160 characters, you will see an error and won't be able to save.",
      'Best for high delivery rate and low cost.',
    ],
  },
  {
    id: 'multi',
    title: 'Allow Multi-part SMS',
    rules: [
      'Messages longer than 160 characters are split into multiple SMS segments.',
      'Each segment is up to 160 characters.',
      'Additional segments may incur extra cost.',
    ],
  },
];

interface SmsDeliveryRulesCardProps {
  value: SmsDeliveryMode;
  onChange: (mode: SmsDeliveryMode) => void;
}

const SmsDeliveryRulesCard = ({
  value,
  onChange,
}: SmsDeliveryRulesCardProps) => {
  return (
    <div className="rounded-lg border border-card-border bg-card-background p-4">
      <div className="mb-4 flex items-center gap-2">
        <Info className="size-4 text-primary" />
        <h3 className="text-sm font-semibold text-primary">
          SMS Delivery & Validation Rules
        </h3>
      </div>

      <RadioGroup
        value={value}
        onValueChange={v => onChange(v as SmsDeliveryMode)}
        className="grid grid-cols-2 gap-3"
      >
        {DELIVERY_OPTIONS.map(option => (
          <label
            key={option.id}
            className={cn(
              'flex cursor-pointer flex-col gap-3 rounded-lg border border-card-border p-4 transition-colors',
              value === option.id && 'border-primary bg-primary/5',
            )}
          >
            <div className="flex items-center  gap-2">
              <RadioGroupItem value={option.id} className="mt-1 shrink-0" />
              <p className="text-sm font-semibold text-primary">
                {option.title}
              </p>
            </div>
            <div className="min-w-0 space-y-2">
              <ul className="space-y-1.5">
                {option.rules.map(rule => (
                  <li
                    key={rule}
                    className="flex items-start gap-2 text-xs text-muted-foreground"
                  >
                    <Check className="mt-0.5 size-3.5 shrink-0 text-green-500" />
                    <span>{rule}</span>
                  </li>
                ))}
              </ul>
            </div>
          </label>
        ))}
      </RadioGroup>
    </div>
  );
};

export default SmsDeliveryRulesCard;
