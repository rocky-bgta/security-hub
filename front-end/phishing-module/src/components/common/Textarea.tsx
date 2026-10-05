import { ComponentProps, forwardRef } from 'react';

import { cn } from 'utils/Helper';

import 'styles/input.css';

const Textarea = forwardRef<HTMLTextAreaElement, ComponentProps<'textarea'>>(
  ({ className, ...props }, ref) => {
    return (
      <textarea
        className={cn('input-default min-h-[80px]', className)}
        ref={ref}
        {...props}
      />
    );
  },
);

Textarea.displayName = 'Textarea';

export { Textarea };
