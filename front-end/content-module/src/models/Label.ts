import { LabelHTMLAttributes } from 'react';

export interface ILabelProps extends LabelHTMLAttributes<HTMLLabelElement> {
  required?: boolean;
}
