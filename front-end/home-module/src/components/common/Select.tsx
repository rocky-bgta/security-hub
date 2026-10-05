import {
  Content,
  Icon,
  Item,
  ItemIndicator,
  ItemText,
  Portal,
  Root,
  ScrollDownButton,
  ScrollUpButton,
  Trigger,
  Value,
  Viewport,
} from '@radix-ui/react-select';
import { Check, ChevronDown, ChevronUp } from 'lucide-react';
import {
  ComponentPropsWithoutRef,
  ComponentRef,
  forwardRef,
  ReactNode,
} from 'react';

import { cn } from 'utils/Helper';

import 'styles/input.css';

type SelectContentProps = ComponentPropsWithoutRef<typeof Content> & {
  searchHeader?: ReactNode;
};

const Select = Root;

const SelectValue = Value;

const SelectTrigger = forwardRef<
  ComponentRef<typeof Trigger>,
  ComponentPropsWithoutRef<typeof Trigger>
>(({ className, children, ...props }, ref) => (
  <Trigger
    ref={ref}
    className={cn(
      'home-select-default',
      'home-group home-items-center home-justify-between [&>span]:home-line-clamp-1',
      className,
    )}
    {...props}
  >
    {children}
    <Icon asChild>
      <ChevronDown className="home-size-4 home-opacity-50 home-transition-transform home-duration-200 group-data-[state=open]:home-rotate-180" />
    </Icon>
  </Trigger>
));

SelectTrigger.displayName = 'SelectTrigger';

const SelectScrollUpButton = forwardRef<
  ComponentRef<typeof ScrollUpButton>,
  ComponentPropsWithoutRef<typeof ScrollUpButton>
>(({ className, ...props }, ref) => (
  <ScrollUpButton
    ref={ref}
    className={cn(
      'home-flex home-cursor-default home-items-center home-justify-center home-py-1',
      className,
    )}
    {...props}
  >
    <ChevronUp className="home-size-4" />
  </ScrollUpButton>
));

SelectScrollUpButton.displayName = 'SelectScrollUpButton';

const SelectScrollDownButton = forwardRef<
  ComponentRef<typeof ScrollDownButton>,
  ComponentPropsWithoutRef<typeof ScrollDownButton>
>(({ className, ...props }, ref) => (
  <ScrollDownButton
    ref={ref}
    className={cn(
      'home-flex home-cursor-default home-items-center home-justify-center home-py-1',
      className,
    )}
    {...props}
  >
    <ChevronDown className="home-size-4" />
  </ScrollDownButton>
));

SelectScrollDownButton.displayName = 'SelectScrollDownButton';

const SelectContent = forwardRef<
  ComponentRef<typeof Content>,
  SelectContentProps
>(
  (
    { className, children, searchHeader, position = 'popper', ...props },
    ref,
  ) => (
    <Portal>
      <Content
        ref={ref}
        className={cn(
          'home-relative home-z-50 home-max-h-96 home-min-w-[8rem] home-overflow-hidden home-rounded-md home-border home-bg-secondary home-text-secondary-foreground home-shadow-md data-[state=open]:home-animate-in data-[state=closed]:home-animate-out data-[state=closed]:home-fade-out-0 data-[state=open]:home-fade-in-0 data-[state=closed]:home-zoom-out-95 data-[state=open]:home-zoom-in-95 data-[side=bottom]:home-slide-in-from-top-2 data-[side=left]:home-slide-in-from-right-2 data-[side=right]:home-slide-in-from-left-2 data-[side=top]:home-slide-in-from-bottom-2',
          position === 'popper' &&
            'data-[side=bottom]:home-translate-y-1 data-[side=left]:-home-translate-x-1 data-[side=right]:home-translate-x-1 data-[side=top]:-home-translate-y-1',
          className,
        )}
        position={position}
        {...props}
      >
        {searchHeader}
        <SelectScrollUpButton />
        <Viewport
          className={cn(
            'home-p-1',
            position === 'popper' &&
              'home-h-[var(--radix-select-trigger-height)] home-w-full home-min-w-[var(--radix-select-trigger-width)]',
          )}
        >
          {children}
        </Viewport>
        <SelectScrollDownButton />
      </Content>
    </Portal>
  ),
);

SelectContent.displayName = 'SelectContent';

const SelectItem = forwardRef<
  ComponentRef<typeof Item>,
  ComponentPropsWithoutRef<typeof Item>
>(({ className, children, ...props }, ref) => (
  <Item
    ref={ref}
    className={cn(
      'home-relative home-flex home-w-full home-cursor-pointer home-select-none home-items-center home-rounded-sm home-py-1.5 home-pl-8 home-pr-2 home-text-sm home-outline-none focus:home-bg-secondary focus:home-text-secondary-foreground data-[disabled]:home-opacity-50 data-[disabled]:home-pointer-events-none',
      className,
    )}
    {...props}
  >
    <span className="home-absolute home-left-2 home-flex home-size-3.5 home-items-center home-justify-center">
      <ItemIndicator>
        <Check className="home-size-4" />
      </ItemIndicator>
    </span>

    <ItemText>{children}</ItemText>
  </Item>
));

SelectItem.displayName = 'SelectItem';

export {
  Select,
  SelectContent,
  SelectItem,
  SelectScrollDownButton,
  SelectScrollUpButton,
  SelectTrigger,
  SelectValue,
};
