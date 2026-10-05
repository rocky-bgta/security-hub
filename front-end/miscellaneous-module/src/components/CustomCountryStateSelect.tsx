import { getCountries, getStates } from 'country-state-picker';

import SearchSelect from 'components/SearchSelect';

interface ICountry {
  name: string;
  code: string;
  dial_code: string;
}

interface ISelectCountry {
  value: string;
  placeholder?: string;
  showAllCountryOption?: boolean;
  hasError?: boolean;
  disabled?: boolean;
  handleChange: (country: any) => void;
}

const CountryList: Array<ICountry> = getCountries();

const CustomCountrySelect = ({
  value,
  placeholder = 'Select country',
  showAllCountryOption = false,
  hasError = false,
  disabled = false,
  handleChange,
}: ISelectCountry) => {
  return (
    <SearchSelect
      value={value}
      onValueChange={value => handleChange(value)}
      placeholder={placeholder}
      items={[
        ...(showAllCountryOption
          ? [{ value: 'all', label: 'All Countries' }]
          : []),
        ...CountryList.map(country => ({
          value: country.code,
          label: country.name,
        })),
      ]}
      hasError={hasError}
      disabled={disabled}
    />
  );
};

interface ISelectState extends ISelectCountry {
  countryCode: string;
}

const CustomStateSelect = ({
  value,
  countryCode,
  placeholder = 'Select state',
  hasError = false,
  disabled = false,
  handleChange,
}: ISelectState) => {
  const states: Array<string> = getStates(countryCode) ?? [];

  return (
    <SearchSelect
      value={value}
      onValueChange={value => handleChange(value)}
      placeholder={placeholder}
      items={states.map(state => ({
        value: state,
        label: state,
      }))}
      hasError={hasError}
      disabled={disabled || countryCode === ''}
    />
  );
};

export { CountryList, CustomCountrySelect, CustomStateSelect };
