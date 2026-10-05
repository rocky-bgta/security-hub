import langs from 'langs';

import SearchSelect from 'components/SearchSelect';

interface IProps {
  value: string;
  handleChange: (value: string) => void;
  placeholder?: string;
  hasError?: boolean;
  disabled?: boolean;
}

const CustomLanguageSelect = ({
  value,
  placeholder = 'Select language',
  hasError = false,
  disabled = false,
  handleChange,
}: IProps) => {
  const languages = langs.all();

  return (
    <SearchSelect
      value={value}
      onValueChange={value => handleChange(value)}
      placeholder={placeholder}
      items={languages.map(language => ({
        value: language[1],
        label: language.name,
      }))}
      hasError={hasError}
      disabled={disabled}
    />
  );
};

export default CustomLanguageSelect;
