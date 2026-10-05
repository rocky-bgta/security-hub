import { Indicator, Root } from '@radix-ui/react-checkbox';
import { Check } from 'lucide-react';
import { ComponentPropsWithoutRef, ComponentRef, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const Checkbox = forwardRef<
  ComponentRef<typeof Root>,
  ComponentPropsWithoutRef<typeof Root>
>(({ className, ...props }, ref) => (
  <Root
    ref={ref}
    className={cn(
      'home-peer home-h-4 home-w-4 home-shrink-0 home-rounded-sm home-border home-border-primary home-ring-offset-background focus-visible:home-outline-none focus-visible:home-ring-2 focus-visible:home-ring-primary focus-visible:home-ring-offset-2 disabled:home-cursor-not-allowed disabled:home-opacity-50 data-[state=checked]:home-bg-primary data-[state=checked]:home-text-primary-foreground',
      className,
    )}
    {...props}
  >
    <Indicator
      className={cn(
        'home-flex home-items-center home-justify-center home-text-current',
      )}
    >
      <Check className="home-size-3 home-shrink-0" />
    </Indicator>
  </Root>
));

Checkbox.displayName = 'Checkbox';

export { Checkbox };
