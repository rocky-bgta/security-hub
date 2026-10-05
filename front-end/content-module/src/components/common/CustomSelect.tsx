import { useState } from 'react';
import Select from 'react-select';

import { ICustomSelectProps, MENU_PLACEMENT } from 'models/Input';
import { cn } from 'utils/Helper';

const customStyles = {
  control: (provided: any, state: any) => ({
    ...provided,
    display: 'flex',
    width: '100%',
    borderRadius: '0.375rem',
    border: state.isFocused ? '1px solid transparent' : '1px solid #7c7c7c',
    backgroundColor: 'transparent',
    height: 'auto',
    fontSize: '1rem',
    boxShadow: state.isFocused
      ? '0 0 0 1px #00d4aa , 0 0 0 2px #00d4aa '
      : 'none',
    borderColor: state.isFocused ? 'transparent' : '#6b7280',
    outline: 'none',
    cursor: state.isDisabled ? 'not-allowed' : 'default',
    opacity: state.isDisabled ? 0.5 : 1,

    '&:hover': {
      borderColor: state.isFocused ? 'transparent' : '#6b7280',
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
    color: 'white',
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
      className={cn(
        isMenuOpen
          ? 'content-rotate-up-indicator'
          : 'content-rotate-down-indicator',
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
