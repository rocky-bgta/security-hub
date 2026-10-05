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
      className={cn('home-grid home-gap-2', className)}
      {...props}
      ref={ref}
    />
  );
});

RadioGroup.displayName = 'RadioGroup';

const RadioGroupItem = forwardRef<
  ComponentRef<typeof Item>,
  ComponentPropsWithoutRef<typeof Item>
>(({ className, ...props }, ref) => {
  return (
    <Item
      ref={ref}
      className={cn(
        'focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed home-aspect-square home-h-4 home-w-4 home-rounded-full home-border home-border-primary home-text-primary home-ring-offset-background focus:home-outline-none focus-visible:home-ring-2 disabled:home-opacity-50',
        className,
      )}
      {...props}
    >
      <Indicator className="home-flex home-items-center home-justify-center">
        <Circle className="home-size-2.5 home-fill-current home-text-current" />
      </Indicator>
    </Item>
  );
});

RadioGroupItem.displayName = 'RadioGroupItem';

export { RadioGroup, RadioGroupItem };
