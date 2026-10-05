import { cva, type VariantProps } from 'class-variance-authority';
import { HTMLAttributes } from 'react';

import { cn } from 'utils/Helper';

const badgeVariants = cva(
  'home-inline-flex home-items-center home-rounded-full home-border home-px-2.5 home-py-0.5 home-text-xs home-font-semibold home-transition-colors focus:home-outline-none focus:home-ring-2 focus:home-ring-primary focus:home-ring-offset-2',

  {
    variants: {
      variant: {
        default:
          'home-border-transparent home-bg-primary home-text-primary-foreground hover:home-bg-primary/80',
        secondary:
          'home-border-transparent home-bg-secondary home-text-secondary-foreground hover:home-bg-secondary/80',
        destructive:
          'home-border-transparent home-bg-destructive home-text-destructive-foreground hover:home-bg-destructive/80',
        outline: 'home-text-foreground',
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
