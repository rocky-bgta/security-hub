import { Eye, EyeOff } from 'lucide-react';
import { ComponentProps, forwardRef, useState } from 'react';

import { cn } from 'utils/Helper';

import 'styles/input.css';

const selectOnZero = (e: React.FocusEvent<HTMLInputElement>) => {
  if (Number(e.target.value) === 0) {
    e.target.select();
  }
};

const Input = forwardRef<HTMLInputElement, ComponentProps<'input'>>(
  ({ className, type, onFocus, ...props }, ref) => {
    const [isPasswordVisible, setIsPasswordVisible] = useState<boolean>(false);

    const togglePasswordVisibility = () =>
      setIsPasswordVisible(prevState => !prevState);

    const handleFocus = (e: React.FocusEvent<HTMLInputElement>) => {
      if (type === 'number') selectOnZero(e);
      onFocus?.(e);
    };

    if (type === 'password') {
      return (
        <div className="relative">
          <input
            className={cn('pr-12', 'input-default', className)}
            type={isPasswordVisible ? 'text' : 'password'}
            ref={ref}
            {...props}
          />

          {isPasswordVisible ? (
            <Eye
              className="absolute right-4 top-1/2 size-5 -translate-y-1/2"
              onClick={togglePasswordVisibility}
            />
          ) : (
            <EyeOff
              className="absolute right-4 top-1/2 size-5 -translate-y-1/2"
              onClick={togglePasswordVisibility}
            />
          )}
        </div>
      );
    }

    return (
      <input
        className={cn('input-default', className)}
        type={type}
        onFocus={handleFocus}
        ref={ref}
        {...props}
      />
    );
  },
);

Input.displayName = 'Input';

export { Input };
