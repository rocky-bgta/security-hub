import { Indicator, Item, Root } from '@radix-ui/react-radio-group';
import { Circle } from 'lucide-react';
import { ComponentPropsWithoutRef, ComponentRef, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const RadioGroup = forwardRef<
  ComponentRef<typeof Root>,
  ComponentPropsWithoutRef<typeof Root>
>(({ className, ...props }, ref) => {
  return (
    <Root
      className={cn('content-grid content-gap-2', className)}
      {...props}
      ref={ref}
    />
  );
});

const RadioGroupItem = forwardRef<
  ComponentRef<typeof Item>,
  ComponentPropsWithoutRef<typeof Item>
>(({ className, ...props }, ref) => {
  return (
    <Item
      ref={ref}
      className={cn(
        'content-aspect-square content-h-4 content-w-4 content-rounded-full content-border content-border-primary content-text-primary content-ring-offset-background focus:content-outline-none focus-visible:content-ring-2 focus-visible:content-ring-primary focus-visible:content-ring-offset-2 disabled:content-cursor-not-allowed disabled:content-opacity-50',
        className,
      )}
      {...props}
    >
      <Indicator className="content-flex content-items-center content-justify-center">
        <Circle className="content-size-2.5 content-fill-current content-text-current" />
      </Indicator>
    </Item>
  );
});

export { RadioGroup, RadioGroupItem };
