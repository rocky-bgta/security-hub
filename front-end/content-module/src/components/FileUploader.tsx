import { ChangeEvent, forwardRef, useImperativeHandle, useRef } from 'react';
import { toast } from 'react-toastify';

import { IFileUploaderProps } from 'models/Input';
import { cn } from 'utils/Helper';
import { cva } from 'class-variance-authority';

export interface FileUploaderHandle {
  clearFiles: () => void;
}

const buttonVariants = cva('', {
  variants: {
    variant: {
      default:
        'content-rounded content-border content-border-card-border content-px-4 content-py-2 content-text-base content-font-normal content-text-cloudy-white',
      outline: 'content-border-transparent',
      secondary:
        'content-w-full content-rounded-lg content-border-2 content-border-dashed content-border-card-border content-py-10 content-text-cloudy-white',
    },
  },
});

const FileUploader = forwardRef<FileUploaderHandle, IFileUploaderProps>(
  (
    {
      id = 'file-upload',
      containerClassName,
      onUpload,
      onError,
      accept = '*',
      multiple = false,
      maxSize = 5,
      placeholder = 'Upload File',
      disabled = false,
      variant = 'default',
    },
    ref,
  ) => {
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
      const files = event.target.files;
      if (!files) return;

      const validSizeFiles = Array.from(files).filter(
        file => file.size / 1024 / 1024 <= maxSize,
      );

      const validFormatFiles = Array.from(files).filter(file => {
        if (accept.split(',').length === 0) return true;
        return accept.split(',').includes(file.type);
      });

      if (validFormatFiles.length !== files.length) {
        toast.warning(`Only ${accept} files are allowed.`);
        return;
      }

      if (validSizeFiles.length !== files.length) {
        toast.warning(`Some files exceed the ${maxSize}MB limit.`);
        return;
      }

      if (validSizeFiles.length > 0 && validFormatFiles.length > 0) {
        onUpload(files, id);
      }
    };

    const clearFiles = () => {
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    };

    useImperativeHandle(ref, () => ({
      clearFiles,
    }));

    return (
      <div className={containerClassName}>
        <input
          type="file"
          ref={fileInputRef}
          accept={accept}
          multiple={multiple}
          onChange={handleFileChange}
          className="content-hidden"
        />
        <button
          onClick={() => fileInputRef.current?.click()}
          type="button"
          className={cn(buttonVariants({ variant }))}
          disabled={disabled}
        >
          {placeholder}
        </button>
      </div>
    );
  },
);

export default FileUploader;
