import {
  Close,
  Content,
  Description,
  Overlay,
  Portal,
  Root,
  Title,
  Trigger,
} from '@radix-ui/react-dialog';
import { X } from 'lucide-react';
import {
  ComponentPropsWithoutRef,
  ComponentRef,
  forwardRef,
  HTMLAttributes,
} from 'react';

import { cn } from 'utils/Helper';

const Dialog = Root;

const DialogTrigger = Trigger;

const DialogPortal = Portal;

const DialogOverlay = forwardRef<
  ComponentRef<typeof Overlay>,
  ComponentPropsWithoutRef<typeof Overlay>
>(({ className, ...props }, ref) => (
  <Overlay
    ref={ref}
    className={cn(
      'data-[state=open]:content-animate-in data-[state=closed]:content-animate-out data-[state=closed]:content-fade-out-0 data-[state=open]:content-fade-in-0 content-fixed content-inset-0 content-z-50 content-bg-black/50',
      className,
    )}
    {...props}
  />
));

const DialogContent = forwardRef<
  ComponentRef<typeof Content>,
  ComponentPropsWithoutRef<typeof Content>
>(({ className, children, ...props }, ref) => (
  <DialogPortal>
    <DialogOverlay />
    <Content
      ref={ref}
      className={cn(
        'data-[state=open]:content-animate-in data-[state=closed]:content-animate-out data-[state=closed]:content-fade-out-0 data-[state=open]:content-fade-in-0 data-[state=closed]:content-zoom-out-95 data-[state=open]:content-zoom-in-95 data-[state=closed]:content-slide-out-to-left-1/2 data-[state=closed]:content-slide-out-to-top-[48%] data-[state=open]:content-slide-in-from-left-1/2 data-[state=open]:content-slide-in-from-top-[48%] content-fixed content-left-[50%] content-top-[50%] content-z-50 content-grid content-translate-x-[-50%] content-translate-y-[-50%] content-gap-4 content-border content-border-card-border content-bg-dark content-p-6 content-shadow-lg content-duration-200 sm:content-rounded-lg',
        className,
      )}
      {...props}
    >
      {children}
      <Close className="content-absolute content-right-4 content-top-4 content-rounded-sm content-text-white content-opacity-70 content-ring-offset-background content-transition-opacity hover:content-opacity-100 focus:content-outline-none focus:content-ring-2 focus:content-ring-primary focus:content-ring-offset-2 disabled:content-pointer-events-none data-[state=open]:content-bg-secondary data-[state=open]:content-text-muted-foreground">
        <X className="content-size-4" />
        <span className="content-sr-only">Close</span>
      </Close>
    </Content>
  </DialogPortal>
));

const DialogHeader = ({
  className,
  ...props
}: HTMLAttributes<HTMLDivElement>) => (
  <div
    className={cn(
      'content-flex content-flex-col content-space-y-1.5 content-text-center sm:content-text-left',
      className,
    )}
    {...props}
  />
);

const DialogFooter = ({
  className,
  ...props
}: HTMLAttributes<HTMLDivElement>) => (
  <div
    className={cn(
      'content-flex content-flex-col-reverse sm:content-flex-row sm:content-justify-end sm:content-space-x-2',
      className,
    )}
    {...props}
  />
);

const DialogTitle = forwardRef<
  ComponentRef<typeof Title>,
  ComponentPropsWithoutRef<typeof Title>
>(({ className, ...props }, ref) => (
  <Title
    ref={ref}
    className={cn(
      'content-text-lg content-font-semibold content-leading-none content-tracking-tight content-text-white',
      className,
    )}
    {...props}
  />
));

const DialogDescription = forwardRef<
  ComponentRef<typeof Description>,
  ComponentPropsWithoutRef<typeof Description>
>(({ className, ...props }, ref) => (
  <Description
    ref={ref}
    className={cn('content-text-sm content-text-muted-foreground', className)}
    {...props}
  />
));

export {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogOverlay,
  DialogPortal,
  DialogTitle,
  DialogTrigger,
};
