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
      'content-select-default',
      'content-group content-items-center content-justify-between [&>span]:content-line-clamp-1',
      className,
    )}
    {...props}
  >
    {children}
    <Icon asChild>
      <ChevronDown className="content-size-4 content-opacity-50 content-transition-transform content-duration-200 group-data-[state=open]:content-rotate-180" />
    </Icon>
  </Trigger>
));

SelectTrigger.displayName = Trigger.displayName;

const SelectScrollUpButton = forwardRef<
  ComponentRef<typeof ScrollUpButton>,
  ComponentPropsWithoutRef<typeof ScrollUpButton>
>(({ className, ...props }, ref) => (
  <ScrollUpButton
    ref={ref}
    className={cn(
      'content-flex content-cursor-default content-items-center content-justify-center content-py-1',
      className,
    )}
    {...props}
  >
    <ChevronUp className="content-size-4" />
  </ScrollUpButton>
));

SelectScrollUpButton.displayName = ScrollUpButton.displayName;

const SelectScrollDownButton = forwardRef<
  ComponentRef<typeof ScrollDownButton>,
  ComponentPropsWithoutRef<typeof ScrollDownButton>
>(({ className, ...props }, ref) => (
  <ScrollDownButton
    ref={ref}
    className={cn(
      'content-flex content-cursor-default content-items-center content-justify-center content-py-1',
      className,
    )}
    {...props}
  >
    <ChevronDown className="content-size-4" />
  </ScrollDownButton>
));

SelectScrollDownButton.displayName = ScrollDownButton.displayName;

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
          'content-relative content-z-50 content-max-h-96 content-min-w-[8rem] content-overflow-hidden content-rounded-md content-border content-bg-secondary content-text-secondary-foreground content-shadow-md data-[state=open]:content-animate-in data-[state=closed]:content-animate-out data-[state=closed]:content-fade-out-0 data-[state=open]:content-fade-in-0 data-[state=closed]:content-zoom-out-95 data-[state=open]:content-zoom-in-95 data-[side=bottom]:content-slide-in-from-top-2 data-[side=left]:content-slide-in-from-right-2 data-[side=right]:content-slide-in-from-left-2 data-[side=top]:content-slide-in-from-bottom-2',
          position === 'popper' &&
            'data-[side=bottom]:content-translate-y-1 data-[side=left]:-content-translate-x-1 data-[side=right]:content-translate-x-1 data-[side=top]:-content-translate-y-1',
          className,
        )}
        position={position}
        {...props}
      >
        {searchHeader}
        <SelectScrollUpButton />
        <Viewport
          className={cn(
            'content-p-1',
            position === 'popper' &&
              'content-h-[var(--radix-select-trigger-height)] content-w-full content-min-w-[var(--radix-select-trigger-width)]',
          )}
        >
          {children}
        </Viewport>
        <SelectScrollDownButton />
      </Content>
    </Portal>
  ),
);

SelectContent.displayName = Content.displayName;

const SelectItem = forwardRef<
  ComponentRef<typeof Item>,
  ComponentPropsWithoutRef<typeof Item>
>(({ className, children, ...props }, ref) => (
  <Item
    ref={ref}
    className={cn(
      'content-relative content-flex content-w-full content-cursor-pointer content-select-none content-items-center content-rounded-sm content-py-1.5 content-pl-8 content-pr-2 content-text-sm content-outline-none focus:content-bg-secondary focus:content-text-secondary-foreground data-[disabled]:content-opacity-50 data-[disabled]:content-pointer-events-none',
      className,
    )}
    {...props}
  >
    <span className="content-absolute content-left-2 content-flex content-size-3.5 content-items-center content-justify-center">
      <ItemIndicator>
        <Check className="content-size-4" />
      </ItemIndicator>
    </span>

    <ItemText>{children}</ItemText>
  </Item>
));

SelectItem.displayName = Item.displayName;

export {
  Select,
  SelectContent,
  SelectItem,
  SelectScrollDownButton,
  SelectScrollUpButton,
  SelectTrigger,
  SelectValue,
};
