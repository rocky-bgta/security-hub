import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Download, Loader2, Upload } from 'lucide-react';
import { DragEvent, useEffect, useRef, useState } from 'react';
import { cn } from 'utils/Helper';

import {
  BULK_IMPORT_ACCEPT,
  getBulkImportFileError,
} from 'features/bulk-import/utils';

interface IProps {
  uploadedFile: File | null;
  error?: string;
  submitting: boolean;
  templateLoading: boolean;
  onFileChange: (file: File | null) => void;
  onFileError: (message: string) => void;
  onClear: () => void;
  onImport: () => void;
  onDownloadTemplate: () => void;
}

const UploadStep = ({
  uploadedFile,
  error,
  submitting,
  templateLoading,
  onFileChange,
  onFileError,
  onClear,
  onImport,
  onDownloadTemplate,
}: IProps) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isDragging, setIsDragging] = useState(false);

  useEffect(() => {
    if (!uploadedFile && fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  }, [uploadedFile]);

  const applyFile = (file: File | undefined) => {
    const validationError = getBulkImportFileError(file);
    if (validationError) {
      onFileError(validationError);
      onFileChange(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      return;
    }
    onFileError('');
    onFileChange(file!);
  };

  const openPicker = () => fileInputRef.current?.click();

  const handleDrop = (event: DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setIsDragging(false);
    applyFile(event.dataTransfer.files?.[0]);
  };

  const handleDragLeave = (event: DragEvent<HTMLDivElement>) => {
    if (!event.currentTarget.contains(event.relatedTarget as Node)) {
      setIsDragging(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Upload className="size-5" />
          Upload CSV/Excel File *
        </CardTitle>
        <CardDescription>
          Accepted formats: CSV, XLS, XLSX (max 15 MB)
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-4">
          <Label htmlFor="file-upload" className="sr-only">
            Upload CSV/Excel File
          </Label>
          <div
            className={cn(
              'cursor-pointer rounded-lg border-2 border-dashed p-6 text-center',
              error
                ? 'border-red-500'
                : isDragging
                  ? 'border-primary bg-primary/5'
                  : 'border-card-border',
            )}
            onClick={openPicker}
            onDragOver={event => {
              event.preventDefault();
              setIsDragging(true);
            }}
            onDragEnter={event => {
              event.preventDefault();
              setIsDragging(true);
            }}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
          >
            <div className="space-y-4">
              <Upload className="mx-auto size-12 text-muted-foreground" />
              <div>
                <p className="mb-2 text-sm text-muted-foreground">
                  Drag and drop your file here, or click to browse
                </p>
                <Input
                  ref={fileInputRef}
                  id="file-upload"
                  type="file"
                  accept={BULK_IMPORT_ACCEPT}
                  onChange={event => applyFile(event.target.files?.[0])}
                  className="hidden"
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={event => {
                    event.stopPropagation();
                    openPicker();
                  }}
                >
                  Choose File
                </Button>
              </div>
              {uploadedFile && (
                <div className="text-sm text-foreground">
                  Selected: {uploadedFile.name}
                </div>
              )}
            </div>
          </div>
          {error && <p className="text-sm text-red-500">{error}</p>}
          <Button
            type="button"
            variant="link"
            className="h-auto px-0"
            onClick={onDownloadTemplate}
            disabled={templateLoading}
          >
            {templateLoading ? (
              <Loader2 className="size-4 animate-spin" />
            ) : (
              <Download className="size-4" />
            )}
            Download Template
          </Button>
        </div>

        <div className="flex justify-end space-x-4 pt-4">
          <Button variant="outline" onClick={onClear} disabled={submitting}>
            Clear
          </Button>
          <Button onClick={onImport} disabled={submitting || !uploadedFile}>
            {submitting ? 'Processing...' : 'Import Users'}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default UploadStep;
