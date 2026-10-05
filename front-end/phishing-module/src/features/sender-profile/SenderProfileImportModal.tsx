import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Label } from 'common/Label';
import { FileDown, Upload } from 'lucide-react';
import { type ChangeEvent, useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';
import { SENDER_PROFILE_IMPORT_TEMPLATE_URL } from 'utils/Constants';

const IMPORT_MAX_BYTES = 15 * 1024 * 1024;

const isCsvFile = (file: File) => file.name.toLowerCase().endsWith('.csv');

type SenderProfileImportModalProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  importing: boolean;
  onImportCsv: (file: File) => Promise<void>;
};

const SenderProfileImportModal = ({
  open,
  onOpenChange,
  importing,
  onImportCsv,
}: SenderProfileImportModalProps) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [selectedLabel, setSelectedLabel] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      setTimeout(() => {
        setSelectedLabel(null);
      }, 0);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  }, [open]);

  const handleDownloadSimpleTemplate = () => {
    const link = document.createElement('a');
    link.href = SENDER_PROFILE_IMPORT_TEMPLATE_URL;
    link.download = 'Sender_profile_import_template.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handlePickFile = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;

    if (!isCsvFile(file)) {
      toast.error('Please choose a CSV file (.csv).');
      return;
    }
    if (file.size > IMPORT_MAX_BYTES) {
      toast.error('File is too large. Maximum size is 15 MB.');
      return;
    }

    setSelectedLabel(file.name);
    await onImportCsv(file);
  };

  return (
    <Dialog
      open={open}
      onOpenChange={next => {
        if (!next && importing) return;
        onOpenChange(next);
      }}
    >
      <DialogContent className="max-w-lg sm:rounded-lg">
        <DialogHeader>
          <DialogTitle>Import sender profiles</DialogTitle>
          <DialogDescription>
            Download the CSV template, fill in your profiles, then upload the
            file. Use <span className="font-medium text-foreground">|</span> to
            separate multiple tags or psychological triggers in a single cell.
          </DialogDescription>
        </DialogHeader>

        <input
          ref={fileInputRef}
          type="file"
          className="hidden"
          accept=".csv,text/csv"
          onChange={handleFileChange}
          disabled={importing}
        />

        <div className="space-y-4 py-2">
          <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
            <Button
              type="button"
              variant="outline"
              className="w-full sm:w-auto"
              onClick={handleDownloadSimpleTemplate}
              disabled={importing}
            >
              <FileDown className="mr-2 size-4" />
              Simple Template
            </Button>
            <Button
              type="button"
              variant="default"
              className="w-full sm:w-auto"
              onClick={handlePickFile}
              disabled={importing}
            >
              <Upload className="mr-2 size-4" />
              {importing ? 'Importing…' : 'Upload CSV file'}
            </Button>
          </div>
          <div>
            <Label className="text-muted-foreground">
              CSV only, max 15 MB.
            </Label>
            {selectedLabel && (
              <p className="mt-1 text-sm text-muted-foreground">
                {importing ? 'Importing' : 'Selected'}: {selectedLabel}
              </p>
            )}
          </div>
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="secondary"
            onClick={() => onOpenChange(false)}
            disabled={importing}
          >
            Cancel
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default SenderProfileImportModal;
