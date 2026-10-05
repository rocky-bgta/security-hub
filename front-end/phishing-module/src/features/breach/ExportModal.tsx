import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogHeader,
} from 'common/Dialog';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { BreachStatus } from 'models/Breach';
import { useState } from 'react';

interface ExportModalProps {
  isOpen: boolean;
  onClose: () => void;
  onExport: (format: string, domain?: string, status?: BreachStatus) => void;
  exporting?: boolean;
}

/**
 * Modal for exporting breach data
 */
const ExportModal = ({
  isOpen,
  onClose,
  onExport,
  exporting = false,
}: ExportModalProps) => {
  const [format, setFormat] = useState<string>('csv');
  const [filterDomain, setFilterDomain] = useState<string>('');
  const [filterStatus, setFilterStatus] = useState<BreachStatus | ''>('');

  if (!isOpen) return null;

  const handleExport = () => {
    onExport(format, filterDomain || undefined, filterStatus || undefined);
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Export Breaches</DialogTitle>
        </DialogHeader>
        <div className="space-y-4 px-6 py-4">
          {/* Format Selection */}
          <div>
            <label className="mb-2 block text-sm font-medium text-foreground">
              Export Format
            </label>
            <div className="grid grid-cols-3 gap-3">
              {[
                { value: 'csv', label: 'CSV', icon: '📊' },
                { value: 'excel', label: 'Excel', icon: '📗' },
                { value: 'pdf', label: 'PDF', icon: '📄' },
              ].map(option => (
                <button
                  key={option.value}
                  type="button"
                  onClick={() => setFormat(option.value)}
                  className={`flex flex-col items-center justify-center rounded-lg border-2 p-4 transition-colors ${
                    format === option.value
                      ? 'border-primary bg-primary/10'
                      : 'border-card-border hover:border-card-border'
                  }`}
                >
                  <span className="mb-1 text-2xl">{option.icon}</span>
                  <span className="text-sm font-medium">{option.label}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Filter by Domain */}
          <div>
            <label className="mb-1 block text-sm font-medium text-foreground">
              Filter by Domain (optional)
            </label>
            <Input
              type="text"
              value={filterDomain}
              onChange={e => setFilterDomain(e.target.value)}
              placeholder="e.g., example.com"
            />
          </div>

          {/* Filter by Status */}
          <div>
            <label className="mb-1 block text-sm font-medium text-foreground">
              Filter by Status (optional)
            </label>
            <Select
              value={filterStatus}
              onValueChange={value =>
                setFilterStatus(value as BreachStatus | '')
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Select a status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={BreachStatus.ACTION_REQUIRED}>
                  Action Required
                </SelectItem>
                <SelectItem value={BreachStatus.IN_PROGRESS}>
                  In Progress
                </SelectItem>
                <SelectItem value={BreachStatus.RESOLVED}>Resolved</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>

        <div className="flex justify-end gap-3 border-t border-card-border px-6 py-4">
          <button
            onClick={onClose}
            disabled={exporting}
            className="rounded-lg bg-muted-foreground/10 px-4 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-muted-foreground/20 disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            onClick={handleExport}
            disabled={exporting}
            className="flex items-center gap-2 rounded-lg bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/80 disabled:opacity-50"
          >
            {exporting ? (
              <>
                <svg className="size-4 animate-spin" viewBox="0 0 24 24">
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                    fill="none"
                  />
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  />
                </svg>
                Exporting...
              </>
            ) : (
              <>
                <svg
                  className="size-4"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                  />
                </svg>
                Export
              </>
            )}
          </button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ExportModal;
