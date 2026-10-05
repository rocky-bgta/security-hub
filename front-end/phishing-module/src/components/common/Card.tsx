import { forwardRef, HTMLAttributes } from 'react';

import { cn } from 'utils/Helper';

const Card = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, children, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        "before:[''] after:[''] relative bg-transparent before:pointer-events-none before:absolute before:bottom-0 before:left-6 before:right-6 before:top-0 before:z-0 before:border-b before:border-t before:border-card-border after:pointer-events-none after:absolute after:bottom-6 after:left-0 after:right-0 after:top-6 after:z-0 after:border-l after:border-r after:border-card-border",
        className,
      )}
      {...props}
    >
      <div className="pointer-events-none absolute left-0 top-0 size-full">
        <div className="absolute left-0 top-0 size-3 border-l-2 border-t-2 border-white/75" />
        <div className="absolute right-0 top-0 size-3 border-r-2 border-t-2 border-white/75" />
        <div className="absolute bottom-0 left-0 size-3 border-b-2 border-l-2 border-white/75" />
        <div className="absolute bottom-0 right-0 size-3 border-b-2 border-r-2 border-white/75" />
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
      className={cn('flex flex-col space-y-1.5 p-6', className)}
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
        'text-2xl font-semibold leading-none tracking-tight text-white',
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
    className={cn('text-sm text-muted-foreground', className)}
    {...props}
  />
));

CardDescription.displayName = 'CardDescription';

const CardContent = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div ref={ref} className={cn('p-6 pt-0', className)} {...props} />
  ),
);

CardContent.displayName = 'CardContent';

const CardFooter = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div
      ref={ref}
      className={cn('flex items-center p-6 pt-0', className)}
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
