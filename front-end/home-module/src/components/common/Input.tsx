import { Eye, EyeOff } from 'lucide-react';
import { ComponentProps, forwardRef, useState } from 'react';

import { cn } from 'utils/Helper';

import 'styles/input.css';

const Input = forwardRef<HTMLInputElement, ComponentProps<'input'>>(
  ({ className, type, ...props }, ref) => {
    const [isPasswordVisible, setIsPasswordVisible] = useState<boolean>(false);

    const togglePasswordVisibility = () =>
      setIsPasswordVisible(prevState => !prevState);

    if (type === 'password') {
      return (
        <div className="home-relative">
          <input
            className={cn('home-pr-12', 'home-input-default', className)}
            type={isPasswordVisible ? 'text' : 'password'}
            ref={ref}
            {...props}
          />

          {isPasswordVisible ? (
            <Eye
              className="home-absolute home-right-4 home-top-1/2 home-size-5 -home-translate-y-1/2 home-transform home-text-background"
              onClick={togglePasswordVisibility}
            />
          ) : (
            <EyeOff
              className="home-absolute home-right-4 home-top-1/2 home-size-5 -home-translate-y-1/2 home-transform home-text-background"
              onClick={togglePasswordVisibility}
            />
          )}
        </div>
      );
    }

    return (
      <input
        className={cn('home-input-default', className)}
        type={type}
        ref={ref}
        {...props}
      />
    );
  },
);

Input.displayName = 'Input';

export { Input };
