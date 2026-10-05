import * as DropdownMenuPrimitive from '@radix-ui/react-dropdown-menu';
import { Check, ChevronRight, Circle } from 'lucide-react';
import * as React from 'react';

import { cn } from 'utils/Helper';

const DropdownMenu = DropdownMenuPrimitive.Root;

const DropdownMenuTrigger = DropdownMenuPrimitive.Trigger;

const DropdownMenuGroup = DropdownMenuPrimitive.Group;

const DropdownMenuPortal = DropdownMenuPrimitive.Portal;

const DropdownMenuSub = DropdownMenuPrimitive.Sub;

const DropdownMenuRadioGroup = DropdownMenuPrimitive.RadioGroup;

const DropdownMenuSubTrigger = React.forwardRef<
  React.ElementRef<typeof DropdownMenuPrimitive.SubTrigger>,
  React.ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.SubTrigger> & {
    inset?: boolean;
  }
>(({ className, inset, children, ...props }, ref) => (
  <DropdownMenuPrimitive.SubTrigger
    ref={ref}
    className={cn(
      'content-focus:bg-secondary content-data-[state=open]:bg-secondary content-flex content-cursor-default content-select-none content-items-center content-rounded-sm content-px-2 content-py-1.5 content-text-sm content-outline-none',
      inset && 'content-pl-8',
      className,
    )}
    {...props}
  >
    {children}
    <ChevronRight className="content-ml-auto content-size-4" />
  </DropdownMenuPrimitive.SubTrigger>
));
DropdownMenuSubTrigger.displayName =
  DropdownMenuPrimitive.SubTrigger.displayName;

const DropdownMenuSubContent = React.forwardRef<
  React.ElementRef<typeof DropdownMenuPrimitive.SubContent>,
  React.ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.SubContent>
>(({ className, ...props }, ref) => (
  <DropdownMenuPrimitive.SubContent
    ref={ref}
    className={cn(
      'content-data-[state=open]:animate-in content-data-[state=closed]:animate-out content-data-[state=closed]:fade-out-0 content-data-[state=open]:fade-in-0 content-data-[state=closed]:zoom-out-95 content-data-[state=open]:zoom-in-95 content-data-[side=bottom]:slide-in-from-top-2 content-data-[side=left]:slide-in-from-right-2 content-data-[side=right]:slide-in-from-left-2 content-data-[side=top]:slide-in-from-bottom-2 content-z-50 content-min-w-[8rem] content-overflow-hidden content-rounded-md content-border content-bg-secondary content-p-1 content-text-secondary-foreground content-shadow-lg',
      className,
    )}
    {...props}
  />
));
DropdownMenuSubContent.displayName =
  DropdownMenuPrimitive.SubContent.displayName;

const DropdownMenuContent = React.forwardRef<
  React.ElementRef<typeof DropdownMenuPrimitive.Content>,
  React.ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.Content>
>(({ className, sideOffset = 4, ...props }, ref) => (
  <DropdownMenuPrimitive.Portal>
    <DropdownMenuPrimitive.Content
      ref={ref}
      sideOffset={sideOffset}
      className={cn(
        'content-data-[state=open]:animate-in content-data-[state=closed]:animate-out content-data-[state=closed]:fade-out-0 content-data-[state=open]:fade-in-0 content-data-[state=closed]:zoom-out-95 content-data-[state=open]:zoom-in-95 content-data-[side=bottom]:slide-in-from-top-2 content-data-[side=left]:slide-in-from-right-2 content-data-[side=right]:slide-in-from-left-2 content-data-[side=top]:slide-in-from-bottom-2 content-z-50 content-min-w-[8rem] content-overflow-hidden content-rounded-md content-border content-bg-secondary content-p-1 content-text-secondary-foreground content-shadow-md',
        className,
      )}
      {...props}
    />
  </DropdownMenuPrimitive.Portal>
));
DropdownMenuContent.displayName = DropdownMenuPrimitive.Content.displayName;

const DropdownMenuItem = React.forwardRef<
  React.ElementRef<typeof DropdownMenuPrimitive.Item>,
  React.ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.Item> & {
    inset?: boolean;
  }
>(({ className, inset, ...props }, ref) => (
  <DropdownMenuPrimitive.Item
    ref={ref}
    className={cn(
      'content-focus:bg-secondary content-focus:text-secondary-foreground content-data-[disabled]:opacity-50 content-relative content-flex content-cursor-pointer content-select-none content-items-center content-rounded-sm content-px-2 content-py-1.5 content-text-sm content-outline-none content-transition-colors data-[disabled]:content-pointer-events-none',
      inset && 'content-pl-8',
      className,
    )}
    {...props}
  />
));
DropdownMenuItem.displayName = DropdownMenuPrimitive.Item.displayName;

const DropdownMenuCheckboxItem = React.forwardRef<
  React.ElementRef<typeof DropdownMenuPrimitive.CheckboxItem>,
  React.ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.CheckboxItem>
>(({ className, children, checked, ...props }, ref) => (
  <DropdownMenuPrimitive.CheckboxItem
    ref={ref}
    className={cn(
      'content-focus:bg-secondary content-focus:text-secondary-foreground content-data-[disabled]:opacity-50 content-relative content-flex content-cursor-default content-select-none content-items-center content-rounded-sm content-py-1.5 content-pl-8 content-pr-2 content-text-sm content-outline-none content-transition-colors data-[disabled]:content-pointer-events-none',
      className,
    )}
    checked={checked}
    {...props}
  >
    <span className="content-absolute content-left-2 content-flex content-size-3.5 content-items-center content-justify-center">
      <DropdownMenuPrimitive.ItemIndicator>
        <Check className="content-size-4" />
      </DropdownMenuPrimitive.ItemIndicator>
    </span>
    {children}
  </DropdownMenuPrimitive.CheckboxItem>
));
DropdownMenuCheckboxItem.displayName =
  DropdownMenuPrimitive.CheckboxItem.displayName;

const DropdownMenuRadioItem = React.forwardRef<
  React.ElementRef<typeof DropdownMenuPrimitive.RadioItem>,
  React.ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.RadioItem>
>(({ className, children, ...props }, ref) => (
  <DropdownMenuPrimitive.RadioItem
    ref={ref}
    className={cn(
      'content-focus:bg-secondary content-focus:text-secondary-foreground content-data-[disabled]:opacity-50 content-relative content-flex content-cursor-default content-select-none content-items-center content-rounded-sm content-py-1.5 content-pl-8 content-pr-2 content-text-sm content-outline-none content-transition-colors data-[disabled]:content-pointer-events-none',
      className,
    )}
    {...props}
  >
    <span className="content-absolute content-left-2 content-flex content-size-3.5 content-items-center content-justify-center">
      <DropdownMenuPrimitive.ItemIndicator>
        <Circle className="content-size-2 content-fill-current" />
      </DropdownMenuPrimitive.ItemIndicator>
    </span>
    {children}
  </DropdownMenuPrimitive.RadioItem>
));
DropdownMenuRadioItem.displayName = DropdownMenuPrimitive.RadioItem.displayName;

const DropdownMenuLabel = React.forwardRef<
  React.ElementRef<typeof DropdownMenuPrimitive.Label>,
  React.ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.Label> & {
    inset?: boolean;
  }
>(({ className, inset, ...props }, ref) => (
  <DropdownMenuPrimitive.Label
    ref={ref}
    className={cn(
      'content-px-2 content-py-1.5 content-text-sm content-font-semibold',
      inset && 'content-pl-8',
      className,
    )}
    {...props}
  />
));
DropdownMenuLabel.displayName = DropdownMenuPrimitive.Label.displayName;

const DropdownMenuSeparator = React.forwardRef<
  React.ElementRef<typeof DropdownMenuPrimitive.Separator>,
  React.ComponentPropsWithoutRef<typeof DropdownMenuPrimitive.Separator>
>(({ className, ...props }, ref) => (
  <DropdownMenuPrimitive.Separator
    ref={ref}
    className={cn(
      'content--mx-1 content-my-1 content-h-px content-bg-muted',
      className,
    )}
    {...props}
  />
));
DropdownMenuSeparator.displayName = DropdownMenuPrimitive.Separator.displayName;

const DropdownMenuShortcut = ({
  className,
  ...props
}: React.HTMLAttributes<HTMLSpanElement>) => {
  return (
    <span
      className={cn(
        'content-ml-auto content-text-xs content-tracking-widest content-opacity-60',
        className,
      )}
      {...props}
    />
  );
};
DropdownMenuShortcut.displayName = 'DropdownMenuShortcut';

export {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuPortal,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuShortcut,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
};
