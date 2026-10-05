import { Upload, X } from 'lucide-react';
import { useRef, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'components/common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import FileUploader, {
  type FileUploaderHandle,
} from 'components/common/FileUploader';
import type { BackgroundOption } from 'models/Deepfake';

interface IBackgroundUploadDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onUploaded: (background: BackgroundOption, file: File) => void;
}

const BackgroundUploadDialog = ({
  open,
  onOpenChange,
  onUploaded,
}: IBackgroundUploadDialogProps) => {
  const uploaderRef = useRef<FileUploaderHandle>(null);
  const [file, setFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [processing, setProcessing] = useState(false);

  const resetState = () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setFile(null);
    setPreviewUrl(null);
    setProcessing(false);
    uploaderRef.current?.clearFiles();
  };

  const handleOpenChange = (nextOpen: boolean) => {
    if (!nextOpen) resetState();
    onOpenChange(nextOpen);
  };

  const handleUpload = (files: FileList) => {
    const selected = files[0];
    if (!selected) return;

    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setFile(selected);
    setPreviewUrl(URL.createObjectURL(selected));
  };

  const handleClear = () => {
    if (previewUrl) URL.revokeObjectURL(previewUrl);
    setFile(null);
    setPreviewUrl(null);
    uploaderRef.current?.clearFiles();
  };

  const handleConfirm = () => {
    if (!file) {
      toast.error('Select an image to upload.');
      return;
    }

    setProcessing(true);
    const reader = new FileReader();
    reader.onload = () => {
      const url = reader.result as string;
      onUploaded(
        {
          name: file.name.replace(/\.[^.]+$/, '').slice(0, 20) || 'Custom',
          url,
          value: `center / cover no-repeat url(${url})`,
        },
        file,
      );
      toast.success('Background uploaded');
      setProcessing(false);
      handleOpenChange(false);
    };
    reader.onerror = () => {
      toast.error('Failed to read the image. Please try again.');
      setProcessing(false);
    };
    reader.readAsDataURL(file);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle>Upload background image</DialogTitle>
          <DialogDescription>
            Choose a custom image for the deepfake video background. PNG or JPG,
            up to 1MB.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {previewUrl ? (
            <div className="relative overflow-hidden rounded-xl border border-card-border">
              <div
                className="aspect-video w-full bg-muted"
                style={{
                  background: `center / cover no-repeat url(${previewUrl})`,
                }}
              />
              <div className="flex items-center justify-between gap-3 border-t border-card-border bg-background/80 px-3 py-2">
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium">{file?.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {file ? `${(file.size / 1024).toFixed(0)} KB` : ''}
                  </p>
                </div>
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={handleClear}
                  disabled={processing}
                >
                  <X className="mr-1 size-3.5" /> Remove
                </Button>
              </div>
            </div>
          ) : (
            <FileUploader
              ref={uploaderRef}
              id="deepfake-background-upload"
              accept="image/png,image/jpeg,image/jpg"
              maxSize={1}
              multiple={false}
              placeholder="Upload image"
              description="PNG or JPG · max 1MB"
              disabled={processing}
              onUpload={handleUpload}
            />
          )}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="secondary"
            onClick={() => handleOpenChange(false)}
            disabled={processing}
          >
            Cancel
          </Button>
          <Button
            type="button"
            onClick={handleConfirm}
            disabled={!file || processing}
          >
            <Upload className="mr-1.5 size-4" />
            {processing ? 'Uploading...' : 'Use image'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default BackgroundUploadDialog;
