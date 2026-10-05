import { cva, type VariantProps } from 'class-variance-authority';
import { HTMLAttributes } from 'react';

import { cn } from 'utils/Helper';

const badgeVariants = cva(
  'content-focus:outline-none content-focus:ring-2 content-focus:ring-primary content-focus:ring-offset-2 content-inline-flex content-items-center content-rounded-full content-border content-px-2.5 content-py-0.5 content-text-xs content-font-semibold content-transition-colors',
  {
    variants: {
      variant: {
        default:
          'content-border-primary content-bg-primary content-text-white hover:content-bg-primary/80',
        secondary:
          'content-border-secondary content-bg-secondary content-text-white hover:content-bg-secondary/80',
        destructive:
          'content-border-destructive content-bg-destructive content-text-white hover:content-bg-destructive/80',
        outline: 'content-text-cloudy-white',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
);

export interface BadgeProps
  extends HTMLAttributes<HTMLDivElement>, VariantProps<typeof badgeVariants> {}

const Badge = ({ className, variant, ...props }: BadgeProps) => {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  );
};

export { Badge, badgeVariants };
