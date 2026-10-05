import { Root, Thumb } from '@radix-ui/react-switch';
import { ComponentPropsWithoutRef, ComponentRef, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const Switch = forwardRef<
  ComponentRef<typeof Root>,
  ComponentPropsWithoutRef<typeof Root>
>(({ className, ...props }, ref) => (
  <Root
    className={cn(
      'home-peer home-inline-flex home-h-6 home-w-11 home-shrink-0 home-cursor-pointer home-items-center home-rounded-full home-border-2 home-border-transparent home-transition-colors focus-visible:home-outline-none focus-visible:home-ring-2 focus-visible:home-ring-primary focus-visible:home-ring-offset-2 focus-visible:home-ring-offset-background disabled:home-cursor-not-allowed disabled:home-opacity-50 data-[state=checked]:home-bg-primary data-[state=unchecked]:home-bg-steel-gray',
      className,
    )}
    {...props}
    ref={ref}
  >
    <Thumb
      className={cn(
        'home-pointer-events-none home-block home-h-5 home-w-5 home-rounded-full home-bg-background home-shadow-lg home-ring-0 home-transition-transform data-[state=checked]:home-translate-x-5 data-[state=unchecked]:home-translate-x-0',
      )}
    />
  </Root>
));

Switch.displayName = 'Switch';

export { Switch };
