import { ChangeEvent, HTMLInputTypeAttribute } from 'react';
import { ActionMeta, MultiValue, SingleValue } from 'react-select';

export interface IInputProps {
  type?: HTMLInputTypeAttribute;
  placeholder?: string;
  className?: string;
  variant?: string;
  id: string;
  name?: string;
  value?: string | number;
  onChangeInput?: (e: ChangeEvent<HTMLInputElement>) => void;
  accept?: string;
}

export interface IFileUploaderProps {
  id?: string;
  onUpload: (files: FileList, id: string) => void;
  onError?: (error: string) => void;
  accept?: string;
  multiple?: boolean;
  maxSize?: number;
  placeholder?: string;
  containerClassName?: string;
  disabled?: boolean;
}

export interface ISelectOption {
  id: number | string;
  label: string;
  value: string;
}

export interface ICheckboxProps {
  className?: string;
  id?: string;
  checked?: boolean;
  onChange?: (e: ChangeEvent<HTMLInputElement>) => void;
  variant?: 'default' | 'exam';
}

export interface ISelectProps {
  id: string;
  name: string;
  value: string | number;
  disabled?: boolean;
  placeholder?: string;
  variant?: string;
  className?: string;
  containerClassName?: string;
  options: Array<ISelectOption>;
  onChangeOption: (e: ChangeEvent<HTMLSelectElement>) => void;
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
    actionMeta: TActionMeta<ICustomSelectOption>,
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
export type TActionMeta<T> = ActionMeta<T>;
