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
      'home-fixed home-inset-0 home-z-50 home-bg-black/80 data-[state=open]:home-animate-in data-[state=closed]:home-animate-out data-[state=closed]:home-fade-out-0 data-[state=open]:home-fade-in-0',
      className,
    )}
    {...props}
  />
));

const DialogContent = forwardRef<
  ComponentRef<typeof Content>,
  ComponentPropsWithoutRef<typeof Content> & {
    shouldPreventClose?: boolean;
  }
>(({ className, shouldPreventClose = false, children, ...props }, ref) => (
  <DialogPortal>
    <DialogOverlay />
    <Content
      ref={ref}
      className={cn(
        'home-fixed home-left-[50%] home-top-[50%] home-z-50 home-grid home-w-3/5 home-translate-x-[-50%] home-translate-y-[-50%] home-gap-4 home-border home-border-card-border home-bg-background home-p-6 home-shadow-lg home-duration-200 data-[state=open]:home-animate-in data-[state=closed]:home-animate-out data-[state=closed]:home-fade-out-0 data-[state=open]:home-fade-in-0 data-[state=closed]:home-zoom-out-95 data-[state=open]:home-zoom-in-95 data-[state=closed]:home-slide-out-to-left-1/2 data-[state=closed]:home-slide-out-to-top-[48%] data-[state=open]:home-slide-in-from-left-1/2 data-[state=open]:home-slide-in-from-top-[48%] sm:home-rounded-lg',
        className,
      )}
      onInteractOutside={e => {
        if (shouldPreventClose) {
          e.preventDefault();
        }
      }}
      {...props}
    >
      {children}
      <Close className="home-absolute home-right-4 home-top-4 home-rounded-sm home-opacity-70 home-ring-offset-background home-transition-opacity hover:home-opacity-100 focus:home-outline-none focus:home-ring-2 focus:home-ring-primary focus:home-ring-offset-2 disabled:home-pointer-events-none data-[state=open]:home-bg-secondary data-[state=open]:home-text-muted-foreground">
        <X className="home-size-4" />
        <span className="home-sr-only">Close</span>
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
      'home-flex home-flex-col home-space-y-1.5 home-text-center sm:home-text-left',
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
      'home-flex home-flex-col-reverse sm:home-flex-row sm:home-justify-end sm:home-space-x-2',
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
      'home-text-lg home-font-semibold home-leading-none home-tracking-tight',
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
    className={cn('home-text-sm home-text-muted-foreground', className)}
    {...props}
  />
));

DialogOverlay.displayName = 'DialogOverlay';
DialogContent.displayName = 'DialogContent';
DialogTitle.displayName = 'DialogTitle';
DialogDescription.displayName = 'DialogDescription';
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
