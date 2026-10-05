import { cva, type VariantProps } from 'class-variance-authority';
import { forwardRef, HTMLAttributes } from 'react';

import { cn } from 'utils/Helper';

const alertVariants = cva(
  'home-relative home-w-full home-rounded home-border home-border-secondary home-p-4 [&>svg+div]:home-translate-y-[-3px] [&>svg]:home-absolute [&>svg]:home-left-4 [&>svg]:home-top-4 [&>svg]:home-text-foreground [&>svg~*]:home-pl-7',
  {
    variants: {
      variant: {
        default: 'home-text-foreground',
        destructive:
          'home-border-destructive/50 home-text-destructive dark:home-border-destructive [&>svg]:home-text-destructive',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
);

const Alert = forwardRef<
  HTMLDivElement,
  HTMLAttributes<HTMLDivElement> & VariantProps<typeof alertVariants>
>(({ className, variant, ...props }, ref) => (
  <div
    ref={ref}
    role="alert"
    className={cn(alertVariants({ variant }), className)}
    {...props}
  />
));

Alert.displayName = 'Alert';

const AlertDescription = forwardRef<
  HTMLParagraphElement,
  HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn('home-text-sm [&_p]:home-leading-relaxed', className)}
    {...props}
  />
));

AlertDescription.displayName = 'AlertDescription';

export { Alert, AlertDescription };
