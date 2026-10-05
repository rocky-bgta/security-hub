import { Upload } from 'lucide-react';
import {
  ChangeEvent,
  forwardRef,
  ReactNode,
  useImperativeHandle,
  useRef,
} from 'react';
import { toast } from 'react-toastify';
import { cn, getAcceptDescription, isFileAccepted } from 'utils/Helper';
import { Button } from './Button';
import { Input } from './Input';

export interface FileUploaderHandle {
  clearFiles: () => void;
}

interface IFileUploaderProps {
  id?: string;
  onUpload: (files: FileList, id: string) => void;
  onError?: (error: string) => void;
  accept?: string;
  multiple?: boolean;
  maxSize?: number;
  placeholder?: string | ReactNode;
  containerClassName?: string;
  disabled?: boolean;
  description?: string;
  /** When provided, replaces the default uploader chrome while keeping validation. */
  children?: ReactNode;
}

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
      description,
      children,
    },
    ref,
  ) => {
    const fileInputRef = useRef<HTMLInputElement>(null);

    const clearFiles = () => {
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    };

    const handleFileChange = (event: ChangeEvent<HTMLInputElement>) => {
      const files = event.target.files;
      if (!files?.length) return;

      const hasInvalidFormat = Array.from(files).some(
        file => !isFileAccepted(file, accept),
      );

      if (hasInvalidFormat) {
        const message = `Only ${getAcceptDescription(accept)} files are allowed.`;
        toast.warning(message);
        onError?.(message);
        clearFiles();
        return;
      }

      const hasInvalidSize = Array.from(files).some(
        file => file.size / 1024 / 1024 > maxSize,
      );

      if (hasInvalidSize) {
        const message = `Some files exceed the ${maxSize}MB limit.`;
        toast.warning(message);
        onError?.(message);
        clearFiles();
        return;
      }

      onUpload(files, id);
      clearFiles();
    };

    useImperativeHandle(ref, () => ({
      clearFiles,
    }));

    if (children) {
      return (
        <div
          className={cn(
            'relative',
            disabled && 'pointer-events-none opacity-50',
            containerClassName,
          )}
        >
          <Input
            type="file"
            ref={fileInputRef}
            accept={accept}
            multiple={multiple}
            onChange={handleFileChange}
            disabled={disabled}
            className="absolute inset-0 z-10 cursor-pointer opacity-0"
          />
          {children}
        </div>
      );
    }

    return (
      <div
        className={cn(
          'relative rounded-lg border-2 border-dashed border-card-border p-6 text-center',
          containerClassName,
        )}
      >
        <div className="space-y-4">
          <Upload className="mx-auto size-12 text-muted-foreground" />
          <div>
            <p className="mb-2 text-sm text-muted-foreground">
              Drag and drop your file here, or click to browse
            </p>
            <Input
              type="file"
              ref={fileInputRef}
              accept={accept}
              multiple={multiple}
              onChange={handleFileChange}
              className="absolute inset-0 cursor-pointer opacity-0"
            />
            <Button
              variant="outline"
              onClick={() => fileInputRef.current?.click()}
              disabled={disabled}
            >
              {placeholder}
            </Button>
            {description && (
              <p className="mx-auto mt-2 w-4/5 text-[10px] text-muted-foreground">
                {description}
              </p>
            )}
          </div>
        </div>
      </div>
    );
  },
);

FileUploader.displayName = 'FileUploader';

export default FileUploader;
