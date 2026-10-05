import clsx from 'clsx';
import { Eye, EyeOff } from 'lucide-react';
import { ComponentProps, forwardRef, useState } from 'react';

import 'styles/input.css';

const Input = forwardRef<HTMLInputElement, ComponentProps<'input'>>(
  ({ className, type, ...props }, ref) => {
    const [isPasswordVisible, setIsPasswordVisible] = useState<boolean>(false);

    const togglePasswordVisibility = () =>
      setIsPasswordVisible(prevState => !prevState);

    if (type === 'password') {
      return (
        <div className="content-relative">
          <input
            className={clsx(
              'content-pr-12',
              'content-input-default',
              className,
            )}
            type={isPasswordVisible ? 'text' : 'password'}
            ref={ref}
            {...props}
          />

          {isPasswordVisible ? (
            <Eye
              className="content-absolute content-right-4 content-top-1/2 content-size-4 -content-translate-y-1/2 content-transform"
              onClick={togglePasswordVisibility}
            />
          ) : (
            <EyeOff
              className="content-absolute content-right-4 content-top-1/2 content-size-4 -content-translate-y-1/2 content-transform"
              onClick={togglePasswordVisibility}
            />
          )}
        </div>
      );
    }

    return (
      <input
        className={clsx('content-input-default', className)}
        type={type}
        ref={ref}
        {...props}
      />
    );
  },
);

Input.displayName = 'Input';

export { Input };
