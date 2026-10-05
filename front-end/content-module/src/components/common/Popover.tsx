import * as React from 'react';
import * as PopoverPrimitive from '@radix-ui/react-popover';

import { cn } from 'utils/Helper';

const Popover = PopoverPrimitive.Root;

const PopoverTrigger = PopoverPrimitive.Trigger;

const PopoverContent = React.forwardRef<
  React.ElementRef<typeof PopoverPrimitive.Content>,
  React.ComponentPropsWithoutRef<typeof PopoverPrimitive.Content>
>(({ className, align = 'center', sideOffset = 4, ...props }, ref) => (
  <PopoverPrimitive.Portal>
    <PopoverPrimitive.Content
      ref={ref}
      align={align}
      sideOffset={sideOffset}
      className={cn(
        'p-4 data-[state=open]:content-animate-in data-[state=closed]:content-animate-out data-[state=closed]:content-fade-out-0 data-[state=open]:content-fade-in-0 data-[state=closed]:content-zoom-out-95 data-[state=open]:content-zoom-in-95 data-[side=bottom]:content-slide-in-from-top-2 data-[side=left]:content-slide-in-from-right-2 data-[side=right]:content-slide-in-from-left-2 data-[side=top]:content-slide-in-from-bottom-2 content-z-50 content-w-72 content-rounded-md content-border content-bg-secondary content-text-secondary-foreground content-shadow-md content-outline-none',
        className,
      )}
      {...props}
    />
  </PopoverPrimitive.Portal>
));
PopoverContent.displayName = PopoverPrimitive.Content.displayName;

export { Popover, PopoverTrigger, PopoverContent };
