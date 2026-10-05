import { Root } from '@radix-ui/react-label';
import { cva, type VariantProps } from 'class-variance-authority';
import { ComponentPropsWithoutRef, ComponentRef, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const labelVariants = cva(
  'home-text-sm home-font-medium home-leading-none home-text-foreground peer-disabled:home-cursor-not-allowed peer-disabled:home-opacity-70',
);

const Label = forwardRef<
  ComponentRef<typeof Root>,
  ComponentPropsWithoutRef<typeof Root> & VariantProps<typeof labelVariants>
>(({ className, ...props }, ref) => (
  <Root ref={ref} className={cn(labelVariants(), className)} {...props} />
));

Label.displayName = 'Label';

export { Label };
