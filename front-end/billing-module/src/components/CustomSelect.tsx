import clsx from 'clsx';
import { useState } from 'react';
import Select from 'react-select';

import { ICustomSelectProps, MENU_PLACEMENT } from 'models/Input';

const customStyles = {
  control: (provided: any, state: any) => ({
    ...provided,
    backgroundColor: 'transparent',
    borderColor: state.isFocused ? 'white' : '#6f7781',
    boxShadow: 'none',
    '&:hover': {
      borderColor: 'white',
    },
  }),
  menu: (provided: any) => ({
    ...provided,
    backgroundColor: '#253340',
  }),
  option: (provided: any, state: any) => ({
    ...provided,
    backgroundColor: state.isSelected
      ? '#2AA684'
      : state.isFocused
        ? '#ffffff50'
        : 'transparent',
    color: 'white',
  }),
  singleValue: (provided: any) => ({
    ...provided,
    color: 'white',
  }),
  input: (provided: any) => ({
    ...provided,
    color: 'black',
  }),
  placeholder: (provided: any) => ({
    ...provided,
    color: 'gray',
  }),
  multiValue: (provided: any) => ({
    ...provided,
    backgroundColor: '#EFF4FB40',
    color: '#ED4337',
  }),
  multiValueLabel: (provided: any) => ({
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
      styles={customStyles}
      className={clsx(
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
