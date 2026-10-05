import clsx from 'clsx';
import { forwardRef, HTMLAttributes, ReactNode } from 'react';

import { cn } from 'utils/Helper';
import Border from '../UserBorder';

interface ICardProps {
  id?: string;
  className?: string;
  title?: string;
  children?: ReactNode;
  onClick?: () => void;
}

const Card = ({
  className = '',
  title = '',
  children,
  onClick,
}: ICardProps) => {
  return (
    <Border>
      <div className={clsx(className)} onClick={onClick}>
        {title && (
          <h4 className="content-p-2.5 content-text-base content-font-semibold content-text-white">
            {title}
          </h4>
        )}
        {children}
      </div>
    </Border>
  );
};

const CardHeader = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        'content-flex content-flex-col content-space-y-1.5 content-p-6',
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
        'content-text-2xl content-font-semibold content-leading-none content-tracking-tight content-text-white',
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
    className={cn('content-text-sm content-text-muted-foreground', className)}
    {...props}
  />
));

CardDescription.displayName = 'CardDescription';

const CardContent = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div
      ref={ref}
      className={cn('content-p-6 content-pt-0', className)}
      {...props}
    />
  ),
);

CardContent.displayName = 'CardContent';

const CardFooter = forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        'content-flex content-items-center content-p-6 content-pt-0',
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
