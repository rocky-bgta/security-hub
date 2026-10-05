import { Eye, EyeOff } from 'lucide-react';
import { ComponentProps, forwardRef, useState } from 'react';
import { cn } from 'utils/Helper';

import 'styles/input.css';

export interface InputProps extends ComponentProps<'input'> {
  /** Validation error message to display below the input */
  error?: string;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ className, type, error, ...props }, ref) => {
    const [isPasswordVisible, setIsPasswordVisible] = useState<boolean>(false);

    const togglePasswordVisibility = () =>
      setIsPasswordVisible(prevState => !prevState);

    if (type === 'password') {
      return (
        <div className="w-full">
          <div className="relative">
            <input
              className={cn(
                'pr-12',
                'input-default',
                error && 'has-error',
                className,
              )}
              type={isPasswordVisible ? 'text' : 'password'}
              ref={ref}
              {...props}
            />

            {isPasswordVisible ? (
              <Eye
                className="absolute right-4 top-1/2 size-5 -translate-y-1/2 cursor-pointer"
                onClick={togglePasswordVisibility}
              />
            ) : (
              <EyeOff
                className="absolute right-4 top-1/2 size-5 -translate-y-1/2 cursor-pointer"
                onClick={togglePasswordVisibility}
              />
            )}
          </div>
          {error && <p className="mt-1 text-sm text-vibrant-red">{error}</p>}
        </div>
      );
    }

    return (
      <div className="w-full">
        <input
          className={cn('input-default', error && 'has-error', className)}
          type={type}
          ref={ref}
          {...props}
        />
        {error && <p className="mt-1 text-sm text-vibrant-red">{error}</p>}
      </div>
    );
  },
);

Input.displayName = 'Input';

export { Input };
