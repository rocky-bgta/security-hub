import { ReactNode } from 'react';

export interface IFileUploaderProps {
  id?: string;
  onUpload: (files: FileList, id: string) => void;
  onError?: (error: string) => void;
  accept?: string;
  multiple?: boolean;
  maxSize?: number;
  placeholder?: string | ReactNode;
  containerClassName?: string;
  disabled?: boolean;
}
