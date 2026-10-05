import { CSSProperties, useState } from 'react';
import Select, {
  ControlProps,
  CSSObjectWithLabel,
  OptionProps,
  StylesConfig,
} from 'react-select';

import {
  ICustomSelectOption,
  ICustomSelectProps,
  MENU_PLACEMENT,
} from 'models/Input';
import { cn } from 'utils/Helper';

const customStyles = {
  control: (
    provided: CSSObjectWithLabel,
    state: ControlProps<ICustomSelectOption, boolean>,
  ) => ({
    ...provided,
    backgroundColor: 'transparent',
    borderColor: state.isFocused ? '#6f7781' : '#6f7781',
    boxShadow: 'none',
    '&:hover': {
      borderColor: '#6f7781',
    },
  }),
  menu: (provided: CSSProperties) => ({
    ...provided,
    backgroundColor: '#253340',
  }),
  option: (
    provided: CSSProperties,
    state: OptionProps<ICustomSelectOption, boolean>,
  ) => ({
    ...provided,
    backgroundColor: state.isSelected
      ? '#2AA684'
      : state.isFocused
        ? '#ffffff50'
        : 'transparent',
    color: 'white',
  }),
  singleValue: (provided: CSSProperties) => ({
    ...provided,
    color: 'white',
  }),
  input: (provided: CSSProperties) => ({
    ...provided,
    color: 'white',
  }),
  placeholder: (provided: CSSProperties) => ({
    ...provided,
    color: 'gray',
  }),
  multiValue: (provided: CSSProperties) => ({
    ...provided,
    backgroundColor: '#EFF4FB40',
    color: '#ED4337',
  }),
  multiValueLabel: (provided: CSSProperties) => ({
    ...provided,
    color: 'white',
  }),
};

const CustomSelect = ({
  data,
  name,
  value,
  handleChange,
  customClassName,
  menuPlacement = MENU_PLACEMENT.AUTO,
  isMulti = false,
  isSearchable = false,
  isDisabled = false,
  isLoading = false,
  isClearable = false,
  isRtl = false,
  placeholder = 'select',
}: ICustomSelectProps) => {
  const [isMenuOpen, setIsMenuOpen] = useState<boolean>(false);

  return (
    <Select
      styles={customStyles as StylesConfig<ICustomSelectOption, boolean>}
      className={cn(
        isMenuOpen ? 'rotate-up-indicator' : 'rotate-down-indicator',
        customClassName,
      )}
      placeholder={placeholder}
      onMenuOpen={() => setIsMenuOpen(true)}
      onMenuClose={() => setIsMenuOpen(false)}
      name={name}
      options={data}
      onChange={handleChange}
      classNamePrefix="select"
      value={value}
      menuPlacement={menuPlacement}
      isMulti={isMulti}
      isSearchable={isSearchable}
      isDisabled={isDisabled}
      isLoading={isLoading}
      isClearable={isClearable}
      isRtl={isRtl}
    />
  );
};

export default CustomSelect;
