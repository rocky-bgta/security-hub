import { Root, Thumb } from '@radix-ui/react-switch';
import { ComponentPropsWithoutRef, ComponentRef, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const Switch = forwardRef<
  ComponentRef<typeof Root>,
  ComponentPropsWithoutRef<typeof Root>
>(({ className, ...props }, ref) => (
  <Root
    className={cn(
      'content-peer content-inline-flex content-h-6 content-w-11 content-shrink-0 content-cursor-pointer content-items-center content-rounded-full content-border-2 content-border-transparent content-transition-colors focus-visible:content-outline-none focus-visible:content-ring-2 focus-visible:content-ring-primary focus-visible:content-ring-offset-2 focus-visible:content-ring-offset-background disabled:content-cursor-not-allowed disabled:content-opacity-50 data-[state=checked]:content-bg-primary data-[state=unchecked]:content-bg-steel-gray',
      className,
    )}
    {...props}
    ref={ref}
  >
    <Thumb
      className={cn(
        'content-pointer-events-none content-block content-h-5 content-w-5 content-rounded-full content-bg-background content-shadow-lg content-ring-0 content-transition-transform data-[state=checked]:content-translate-x-5 data-[state=unchecked]:content-translate-x-0',
      )}
    />
  </Root>
));

export { Switch };
