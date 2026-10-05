import {
  Content,
  Header,
  Item,
  Root,
  Trigger,
} from '@radix-ui/react-accordion';
import { ChevronDown } from 'lucide-react';
import { ComponentPropsWithoutRef, ComponentRef, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const Accordion = Root;

const AccordionItem = forwardRef<
  ComponentRef<typeof Item>,
  ComponentPropsWithoutRef<typeof Item>
>(({ className, ...props }, ref) => (
  <Item ref={ref} className={cn('content-border-b', className)} {...props} />
));

const AccordionTrigger = forwardRef<
  ComponentRef<typeof Trigger>,
  ComponentPropsWithoutRef<typeof Trigger>
>(({ className, children, ...props }, ref) => (
  <Header className="content-flex">
    <Trigger
      ref={ref}
      className={cn(
        'content-flex content-flex-1 content-items-center content-justify-between content-py-4 content-font-medium content-transition-all [&[data-state=open]>svg]:content-rotate-180',
        className,
      )}
      {...props}
    >
      {children}
      <ChevronDown className="content-size-4 content-shrink-0 content-transition-transform content-duration-200" />
    </Trigger>
  </Header>
));

const AccordionContent = forwardRef<
  ComponentRef<typeof Content>,
  ComponentPropsWithoutRef<typeof Content>
>(({ className, children, ...props }, ref) => (
  <Content
    ref={ref}
    className="data-[state=closed]:animate-accordion-up data-[state=open]:animate-accordion-down content-overflow-hidden content-text-sm content-transition-all"
    {...props}
  >
    <div className={cn('content-pb-4 content-pt-0', className)}>{children}</div>
  </Content>
));

export { Accordion, AccordionContent, AccordionItem, AccordionTrigger };
