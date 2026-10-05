import { Indicator, Root } from '@radix-ui/react-progress';
import { ComponentPropsWithoutRef, ComponentRef, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const Progress = forwardRef<
  ComponentRef<typeof Root>,
  ComponentPropsWithoutRef<typeof Root>
>(({ className, value = 0, ...props }, ref) => (
  <Root
    ref={ref}
    className={cn(
      'content-relative content-h-2 content-w-full content-overflow-hidden content-rounded-full content-bg-ash-gray',
      className,
    )}
    {...props}
  >
    <Indicator
      className="content-h-full content-bg-primary content-transition-transform content-duration-300"
      style={{ transform: `translateX(-${100 - (value as number)}%)` }}
    />
  </Root>
));

export { Progress };
