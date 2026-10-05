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
      'content-peer content-h-4 content-w-4 content-shrink-0 content-rounded-sm content-border content-border-primary content-ring-offset-background focus-visible:content-outline-none focus-visible:content-ring-2 focus-visible:content-ring-primary focus-visible:content-ring-offset-2 disabled:content-cursor-not-allowed disabled:content-opacity-50 data-[state=checked]:content-bg-primary data-[state=checked]:content-text-white',
      className,
    )}
    {...props}
  >
    <Indicator
      className={cn(
        'content-flex content-items-center content-justify-center content-text-current',
      )}
    >
      <Check className="content-size-4" />
    </Indicator>
  </Root>
));

Checkbox.displayName = 'Checkbox';

export { Checkbox };
