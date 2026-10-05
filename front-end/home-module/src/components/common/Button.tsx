import { Slot } from '@radix-ui/react-slot';
import { cva, VariantProps } from 'class-variance-authority';
import { ButtonHTMLAttributes, forwardRef } from 'react';

import { cn } from 'utils/Helper';

export interface ButtonProps
  extends
    ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

const buttonVariants = cva(
  'home-inline-flex home-items-center home-justify-center home-gap-2 home-whitespace-nowrap home-rounded-md home-text-sm home-font-medium home-ring-offset-background home-transition-colors home-duration-200 home-ease-in-out focus-visible:home-outline-none focus-visible:home-ring-2 focus-visible:home-ring-primary focus-visible:home-ring-offset-2 disabled:home-pointer-events-none disabled:home-opacity-50 [&_svg]:home-pointer-events-none [&_svg]:home-size-4 [&_svg]:home-shrink-0',
  {
    variants: {
      variant: {
        default:
          'home-bg-primary home-text-primary-foreground hover:home-bg-primary/90',
        destructive:
          'home-bg-destructive home-text-destructive-foreground hover:home-bg-destructive/90',
        outline:
          'home-border home-border-card-border home-bg-transparent home-text-cloudy-white hover:home-bg-secondary hover:home-text-secondary-foreground',
        secondary:
          'home-bg-secondary home-text-secondary-foreground hover:home-bg-secondary/80',
        ghost: 'hover:home-bg-secondary hover:home-text-secondary-foreground',
        link: 'home-text-primary home-underline-offset-4 hover:home-underline',
      },
      size: {
        default: 'home-h-10 home-px-4 home-py-2',
        sm: 'home-h-9 home-rounded-md home-px-3',
        lg: 'home-h-11 home-rounded-md home-px-8',
        icon: 'home-size-10',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
);

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : 'button';
    return (
      <Comp
        className={cn(buttonVariants({ variant, size, className }))}
        ref={ref}
        {...props}
      />
    );
  },
);

Button.displayName = 'Button';

export { Button, buttonVariants };
