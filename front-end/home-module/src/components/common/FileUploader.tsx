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

interface FileUploaderHandle {
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
    };

    useImperativeHandle(ref, () => ({
      clearFiles,
    }));

    return (
      <div
        className={cn(
          'home-relative home-rounded-lg home-border-2 home-border-dashed home-border-card-border home-p-6 home-text-center',
          containerClassName,
        )}
      >
        <div className="home-space-y-4">
          <Upload className="home-mx-auto home-size-12 home-text-muted-foreground" />
          <div>
            <p className="home-mb-2 home-text-sm home-text-muted-foreground">
              Drag and drop your file here, or click to browse
            </p>
            <Input
              type="file"
              ref={fileInputRef}
              accept={accept}
              multiple={multiple}
              onChange={handleFileChange}
              className="home-absolute home-inset-0 home-cursor-pointer home-opacity-0"
            />
            <Button
              variant="outline"
              onClick={() => fileInputRef.current?.click()}
              disabled={disabled}
            >
              {placeholder}
            </Button>
            {description && (
              <p className="home-mt-2 home-mx-auto home-w-4/5 home-text-[10px] home-text-muted-foreground">
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
