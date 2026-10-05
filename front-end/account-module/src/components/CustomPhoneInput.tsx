import PhoneInput, { PhoneInputProps } from 'react-phone-input-2';
import { cn } from 'utils/Helper';

interface IProps extends PhoneInputProps {
  customInputClass?: string;
  customButtonClass?: string;
  customDropdownClass?: string;
  handleChange: (value: string) => void;
}

const CustomPhoneInput = ({
  customInputClass = '',
  customButtonClass = '',
  customDropdownClass = '',
  value,
  country = 'us',
  countryCodeEditable = false,
  handleChange,
  ...rest
}: IProps) => {
  return (
    <PhoneInput
      containerClass="custom-phone-input-container"
      inputClass={cn('custom-phone-input', customInputClass)}
      buttonClass={cn('custom-flag-button', customButtonClass)}
      dropdownClass={cn('custom-dropdown', customDropdownClass)}
      country={country}
      value={value}
      onChange={value => handleChange(value)}
      countryCodeEditable={countryCodeEditable}
      {...rest}
    />
  );
};

export default CustomPhoneInput;
