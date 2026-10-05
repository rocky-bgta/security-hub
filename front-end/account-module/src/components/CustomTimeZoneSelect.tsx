import { getTimeZones } from '@vvo/tzdb';

import SearchSelect from 'components/SearchSelect';

interface IProps {
  value: string;
  countryCode: string;
  handleChange: (value: string) => void;
  placeholder?: string;
  hasError?: boolean;
  disabled?: boolean;
}

const CustomTimeZoneSelect = ({
  value,
  countryCode,
  placeholder = 'Select time zone',
  hasError = false,
  disabled = false,
  handleChange,
}: IProps) => {
  const timeZones = getTimeZones()
    .filter(tz => tz.countryCode.toLowerCase() === countryCode.toLowerCase())
    .sort((a, b) => a.name.localeCompare(b.name))
    .map(tz => tz.name.replace(/_/g, ' '));

  return (
    <SearchSelect
      value={value}
      onValueChange={value => handleChange(value)}
      placeholder={placeholder}
      items={timeZones.map(timeZone => ({
        value: timeZone,
        label: timeZone,
      }))}
      hasError={hasError}
      disabled={disabled || countryCode === ''}
    />
  );
};

export default CustomTimeZoneSelect;
