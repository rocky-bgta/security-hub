import { cva, type VariantProps } from 'class-variance-authority';
import {
  Severity,
  severityClasses,
  statusColors,
  ThreatStatus,
} from 'models/BreachMonitor';
import { HTMLAttributes } from 'react';

import { cn } from 'utils/Helper';

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2',
  {
    variants: {
      variant: {
        default:
          'border-transparent bg-primary text-primary-foreground hover:bg-primary/80',
        secondary:
          'border-transparent bg-secondary text-secondary-foreground hover:bg-secondary/80',
        destructive:
          'border-transparent bg-destructive text-destructive-foreground hover:bg-destructive/80',
        warning:
          'border-transparent bg-amber-500 text-white hover:bg-amber-600',
        outline: 'text-foreground',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
);

export interface BadgeProps
  extends HTMLAttributes<HTMLDivElement>, VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  );
}

function SeverityBadge({ severity }: { severity: Severity }) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded px-2 py-0.5 text-xs font-semibold capitalize',
        severityClasses[severity],
      )}
    >
      {severity}
    </span>
  );
}

function StatusBadge({ status }: { status: ThreatStatus }) {
  const label =
    status === 'in_mitigation'
      ? 'In Mitigation'
      : status === 'completed'
        ? 'Completed'
        : 'Open';
  return (
    <span
      className={cn(
        'inline-flex items-center rounded px-2 py-0.5 text-xs font-semibold',
        statusColors[status],
      )}
    >
      {label}
    </span>
  );
}

export { Badge, badgeVariants, SeverityBadge, StatusBadge };
