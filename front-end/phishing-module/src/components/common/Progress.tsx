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
      'relative h-4 w-full overflow-hidden rounded-full bg-white/10',
      className,
    )}
    {...props}
  >
    <Indicator
      className="h-full bg-primary transition-transform duration-300"
      style={{ transform: `translateX(-${100 - (value as number)}%)` }}
    />
  </Root>
));

Progress.displayName = 'Progress';

export { Progress };
