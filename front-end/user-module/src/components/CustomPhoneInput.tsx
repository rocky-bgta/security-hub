import PhoneInput, { CountryData, PhoneInputProps } from 'react-phone-input-2';
import { cn } from 'utils/Helper';

interface IProps extends PhoneInputProps {
  customInputClass?: string;
  customButtonClass?: string;
  customDropdownClass?: string;
  handleChange: (value: string, data: CountryData) => void;
}

const CustomPhoneInput = ({
  customInputClass = '',
  customButtonClass = '',
  customDropdownClass = '',
  value,
  country = 'us',
  countryCodeEditable = false,
  enableSearch = true,
  handleChange,
  ...rest
}: IProps) => {
  return (
    <PhoneInput
      containerClass="custom-phone-input-container"
      inputClass={cn('custom-phone-input !text-white', customInputClass)}
      buttonClass={cn('custom-flag-button', customButtonClass)}
      dropdownClass={cn('custom-dropdown', customDropdownClass)}
      country={country}
      value={value}
      onChange={(value, data) => handleChange(value, data as CountryData)}
      countryCodeEditable={countryCodeEditable}
      enableSearch={enableSearch}
      {...rest}
    />
  );
};

export default CustomPhoneInput;