import { Slot } from '@radix-ui/react-slot';
import { cva, type VariantProps } from 'class-variance-authority';
import { ButtonHTMLAttributes, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const buttonVariants = cva(
  'content-inline-flex content-items-center content-justify-center content-gap-2 content-whitespace-nowrap content-rounded-md content-text-sm content-font-medium content-ring-offset-background content-transition-colors focus-visible:content-outline-none focus-visible:content-ring-2 focus-visible:content-ring-primary focus-visible:content-ring-offset-2 disabled:content-pointer-events-none disabled:content-opacity-50 [&_svg]:content-pointer-events-none [&_svg]:content-size-4 [&_svg]:content-shrink-0',
  {
    variants: {
      variant: {
        default:
          'content-bg-primary content-text-secondary hover:content-bg-primary/90',
        destructive:
          'content-bg-vibrant-red content-text-white hover:content-bg-vibrant-red/90',
        outline:
          'content-border content-border-card-border content-bg-transparent content-text-white hover:content-bg-secondary hover:content-text-secondary-foreground',
        secondary:
          'content-bg-secondary content-text-white hover:content-bg-secondary/80',
        ghost:
          'hover:content-bg-secondary hover:content-text-secondary-foreground',
        link: 'content-text-primary content-underline-offset-4 hover:content-underline',
      },
      size: {
        default: 'content-h-10 content-px-4 content-py-2',
        sm: 'content-h-9 content-rounded-md content-px-3',
        lg: 'content-h-11 content-rounded-md content-px-8',
        icon: 'content-size-10',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
);

export interface ButtonProps
  extends
    ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

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
