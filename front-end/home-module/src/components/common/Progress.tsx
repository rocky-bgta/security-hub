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
      'home-relative home-h-2 home-w-full home-overflow-hidden home-rounded-full home-bg-ash-gray',
      className,
    )}
    {...props}
  >
    <Indicator
      className="home-h-full home-bg-primary home-transition-transform home-duration-300"
      style={{ transform: `translateX(-${100 - (value as number)}%)` }}
    />
  </Root>
));

Progress.displayName = 'Progress';

export { Progress };
