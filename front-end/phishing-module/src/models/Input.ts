import { MultiValue, SingleValue } from 'react-select';

export interface ISelectOption {
  id: number | string;
  label: string;
  value: string;
}

export interface ICustomSelectOption extends ISelectOption {
  isFixed?: boolean;
  isDisabled?: boolean;
}

export enum MENU_PLACEMENT {
  AUTO = 'auto',
  TOP = 'top',
  BOTTOM = 'bottom',
}

export interface ICustomSelectProps {
  data: Array<ICustomSelectOption>;
  name: string;
  value: TSingleValue<ICustomSelectOption> | TMultiValue<ICustomSelectOption>;
  handleChange: (
    newValue:
      | TMultiValue<ICustomSelectOption>
      | TSingleValue<ICustomSelectOption>,
  ) => void;
  customClassName?: string;
  menuPlacement?: MENU_PLACEMENT;
  isMulti?: boolean;
  isSearchable?: boolean;
  isDisabled?: boolean;
  placeholder?: string;
  isLoading?: boolean;
  isClearable?: boolean;
  isRtl?: boolean;
}

export type TSingleValue<T> = SingleValue<T>;
export type TMultiValue<T> = MultiValue<T>;
