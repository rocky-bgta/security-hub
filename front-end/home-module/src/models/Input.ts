import { ChangeEvent } from 'react';

export interface IInputProps {
  type?: string;
  placeholder?: string;
  className?: string;
  id: string;
  name?: string;
  value?: string;
  onChangeInput?: (e: ChangeEvent<HTMLInputElement>) => void;
}

export interface ICheckboxProps {
  className?: string;
  id?: string;
  checked?: boolean;
  onChange?: (e: ChangeEvent<HTMLInputElement>) => void;
  variant?: 'default' | 'exam';
}
