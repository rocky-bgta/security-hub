import clsx from 'clsx';
import { ICheckboxProps } from 'models/Input';

import { FaCheck } from 'react-icons/fa6';

const CustomCheckbox = ({
  onChange,
  id,
  className = '',
  checked = false,
  variant = 'default',
}: ICheckboxProps) => {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange?.(e);
  };

  return (
    <label htmlFor={id} className="content-cursor-pointer">
      <input
        id={id}
        type="checkbox"
        checked={checked}
        onChange={handleChange}
        className="content-peer content-absolute content-left-0 content-top-0 content-size-full content-cursor-pointer !content-appearance-none content-opacity-0"
      />

      <div
        className={clsx(
          variant == 'default'
            ? 'content-rounded peer-checked:content-border-transparent peer-checked:content-bg-primary'
            : 'content-rounded-full content-border-2',
          'content-flex content-size-5 content-items-center content-justify-center content-border content-border-cloudy-white',
        )}
      >
        {variant == 'default'
          ? checked && <FaCheck className="content-text-white" />
          : checked && (
              <div className="content-size-3 content-rounded-full content-bg-white"></div>
            )}
      </div>
    </label>
  );
};

export default CustomCheckbox;
