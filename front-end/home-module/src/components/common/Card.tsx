import { forwardRef, HTMLAttributes } from 'react';
import { cn } from 'utils/Helper';

const Card = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, children, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        "before:[''] after:[''] home-relative home-bg-card home-text-card-foreground before:home-pointer-events-none before:home-absolute before:home-bottom-0 before:home-left-6 before:home-right-6 before:home-top-0 before:home-z-0 before:home-border-b before:home-border-t before:home-border-card-border after:home-pointer-events-none after:home-absolute after:home-bottom-6 after:home-left-0 after:home-right-0 after:home-top-6 after:home-z-0 after:home-border-l after:home-border-r after:home-border-card-border",
        className,
      )}
      {...props}
    >
      <div className="home-pointer-events-none home-absolute home-left-0 home-top-0 home-size-full">
        <div className="home-absolute home-left-0 home-top-0 home-size-3 home-border-l-2 home-border-t-2 home-border-cloudy-white" />
        <div className="home-absolute home-right-0 home-top-0 home-size-3 home-border-r-2 home-border-t-2 home-border-cloudy-white" />
        <div className="home-absolute home-bottom-0 home-left-0 home-size-3 home-border-b-2 home-border-l-2 home-border-cloudy-white" />
        <div className="home-absolute home-bottom-0 home-right-0 home-size-3 home-border-b-2 home-border-r-2 home-border-cloudy-white" />
      </div>
      {children}
    </div>
  ),
);

Card.displayName = 'Card';

const CardHeader = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        'home-flex home-flex-col home-space-y-1.5 home-p-6',
        className,
      )}
      {...props}
    />
  ),
);

CardHeader.displayName = 'CardHeader';

const CardTitle = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        'home-text-2xl home-font-semibold home-leading-none home-tracking-tight',
        className,
      )}
      {...props}
    />
  ),
);

CardTitle.displayName = 'CardTitle';

const CardDescription = forwardRef<
  HTMLDivElement,
  HTMLAttributes<HTMLDivElement>
>(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn('home-text-sm home-text-muted-foreground', className)}
    {...props}
  />
));

CardDescription.displayName = 'CardDescription';

const CardContent = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div ref={ref} className={cn('home-p-6 home-pt-0', className)} {...props} />
  ),
);

CardContent.displayName = 'CardContent';

const CardFooter = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        'home-flex home-items-center home-p-6 home-pt-0',
        className,
      )}
      {...props}
    />
  ),
);

CardFooter.displayName = 'CardFooter';

export {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
};
