import { Fallback, Image, Root } from '@radix-ui/react-avatar';
import { ComponentPropsWithoutRef, ComponentRef, forwardRef } from 'react';

import { cn } from 'utils/Helper';

const Avatar = forwardRef<
  ComponentRef<typeof Root>,
  ComponentPropsWithoutRef<typeof Root>
>(({ className, ...props }, ref) => (
  <Root
    ref={ref}
    className={cn(
      'content-relative content-flex content-h-10 content-w-10 content-shrink-0 content-overflow-hidden content-rounded-full',
      className,
    )}
    {...props}
  />
));

const AvatarImage = forwardRef<
  ComponentRef<typeof Image>,
  ComponentPropsWithoutRef<typeof Image>
>(({ className, ...props }, ref) => (
  <Image
    ref={ref}
    className={cn(
      'content-aspect-square content-h-full content-w-full',
      className,
    )}
    {...props}
  />
));

const AvatarFallback = forwardRef<
  ComponentRef<typeof Fallback>,
  ComponentPropsWithoutRef<typeof Fallback>
>(({ className, ...props }, ref) => (
  <Fallback
    ref={ref}
    className={cn(
      'content-flex content-h-full content-w-full content-items-center content-justify-center content-rounded-full content-bg-muted',
      className,
    )}
    {...props}
  />
));

export { Avatar, AvatarFallback, AvatarImage };
