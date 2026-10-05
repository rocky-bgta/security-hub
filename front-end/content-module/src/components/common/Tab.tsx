import { Content, List, Root, Trigger } from '@radix-ui/react-tabs';
import { ComponentPropsWithoutRef, ComponentRef, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const Tabs = Root;

const TabsList = forwardRef<
  ComponentRef<typeof List>,
  ComponentPropsWithoutRef<typeof List>
>(({ className, ...props }, ref) => (
  <List
    ref={ref}
    className={cn(
      'content-inline-flex content-items-center content-justify-center content-rounded-md content-border content-border-steel-gray content-bg-transparent content-p-1 content-text-muted-foreground',
      className,
    )}
    {...props}
  />
));

TabsList.displayName = 'TabsList';

const TabsTrigger = forwardRef<
  ComponentRef<typeof Trigger>,
  ComponentPropsWithoutRef<typeof Trigger>
>(({ className, ...props }, ref) => (
  <Trigger
    ref={ref}
    className={cn(
      'content-inline-flex content-items-center content-justify-center content-whitespace-nowrap content-rounded-sm content-px-3 content-py-1.5 content-text-sm content-font-medium content-ring-offset-background content-transition-all focus-visible:content-outline-none focus-visible:content-ring-2 focus-visible:content-ring-primary focus-visible:content-ring-offset-2 disabled:content-pointer-events-none disabled:content-opacity-50 data-[state=active]:content-bg-primary data-[state=active]:content-text-background data-[state=active]:content-shadow-sm',
      className,
    )}
    {...props}
  />
));

TabsTrigger.displayName = 'TabsTrigger';

const TabsContent = forwardRef<
  ComponentRef<typeof Content>,
  ComponentPropsWithoutRef<typeof Content>
>(({ className, ...props }, ref) => (
  <Content
    ref={ref}
    className={cn(
      'content-mt-2 content-ring-offset-background focus-visible:content-outline-none focus-visible:content-ring-2 focus-visible:content-ring-primary focus-visible:content-ring-offset-2',
      className,
    )}
    {...props}
  />
));

TabsContent.displayName = 'TabsContent';

export { Tabs, TabsContent, TabsList, TabsTrigger };
