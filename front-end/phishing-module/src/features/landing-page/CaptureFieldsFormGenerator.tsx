import { sanitizeHtml } from 'home-module/security';
import { Button } from 'common/Button';
import { Check, FormInput, X } from 'lucide-react';
import {
  CAPTURE_FIELDS,
  generateCaptureFormHtml,
  getInputTypeForField,
} from 'models/LandingPage';
import { useState } from 'react';

interface CaptureFieldsFormGeneratorProps {
  isOpen: boolean;
  onInsert: (html: string) => void;
  onClose: () => void;
}

const CaptureFieldsFormGenerator = ({
  isOpen,
  onInsert,
  onClose,
}: CaptureFieldsFormGeneratorProps) => {
  const [selected, setSelected] = useState<string[]>([]);

  const toggle = (fieldId: string) => {
    setSelected(prev =>
      prev.includes(fieldId)
        ? prev.filter(id => id !== fieldId)
        : [...prev, fieldId],
    );
  };

  const toggleAll = () => {
    setSelected(prev =>
      prev.length === CAPTURE_FIELDS.length
        ? []
        : CAPTURE_FIELDS.map(f => f.id),
    );
  };

  const handleDone = () => {
    const html = generateCaptureFormHtml(selected);
    if (html) {
      onInsert(html);
    }
    setSelected([]);
    onClose();
  };

  const handleClose = () => {
    setSelected([]);
    onClose();
  };

  if (!isOpen) return null;

  const allSelected = selected.length === CAPTURE_FIELDS.length;

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-black/60"
      onClick={handleClose}
    >
      <div
        className="mx-4 flex w-full max-w-xl flex-col rounded-xl border border-card-border bg-card-background shadow-2xl"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between border-b border-card-border px-5 py-4">
          <div className="flex items-center gap-2">
            <div className="flex size-8 items-center justify-center rounded-lg bg-gradient-to-br from-emerald-500 to-teal-500">
              <FormInput className="size-4 text-white" />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-primary">
                Generate Capture Form
              </h3>
              <p className="text-xs text-muted-foreground">
                Select fields to include in the form
              </p>
            </div>
          </div>
          <button
            onClick={handleClose}
            className="text-muted-foreground transition-colors hover:text-primary"
          >
            <X className="size-4" />
          </button>
        </div>

        <div className="space-y-4 p-5">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium text-muted-foreground">
              {selected.length} of {CAPTURE_FIELDS.length} fields selected
            </span>
            <button
              type="button"
              onClick={toggleAll}
              className="text-sm font-medium text-primary hover:underline"
            >
              {allSelected ? 'Deselect All' : 'Select All'}
            </button>
          </div>

          <div className="grid grid-cols-2 gap-2">
            {CAPTURE_FIELDS.map(field => {
              const isSelected = selected.includes(field.id);
              const inputType = getInputTypeForField(field.id);
              return (
                <button
                  key={field.id}
                  type="button"
                  onClick={() => toggle(field.id)}
                  className={`flex items-center gap-3 rounded-lg border p-3 text-left transition-all ${
                    isSelected
                      ? 'border-primary bg-primary/10'
                      : 'border-card-border hover:border-primary/40 hover:bg-primary/5'
                  }`}
                >
                  <div
                    className={`flex size-5 shrink-0 items-center justify-center rounded border transition-colors ${
                      isSelected
                        ? 'border-primary bg-primary text-white'
                        : 'border-gray-400'
                    }`}
                  >
                    {isSelected && <Check className="size-3" />}
                  </div>
                  <div className="min-w-0">
                    <span className="block text-sm font-medium text-primary">
                      {field.label}
                    </span>
                    <span className="block text-[10px] text-muted-foreground">
                      type: {inputType}
                    </span>
                  </div>
                </button>
              );
            })}
          </div>

          {selected.length > 0 && (
            <div>
              <label className="mb-1.5 block text-xs font-medium text-muted-foreground">
                Preview
              </label>
              <div className="max-h-40 overflow-auto rounded-md border border-card-border bg-white p-3 dark:bg-gray-900">
                <div
                  dangerouslySetInnerHTML={{
                    __html: sanitizeHtml(generateCaptureFormHtml(selected)),
                  }}
                />
              </div>
            </div>
          )}
        </div>

        <div className="flex items-center justify-end gap-2 border-t border-card-border px-5 py-3">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={handleClose}
          >
            Cancel
          </Button>
          <Button
            type="button"
            size="sm"
            onClick={handleDone}
            disabled={selected.length === 0}
            className="gap-1.5"
          >
            <FormInput className="size-3.5" />
            Done &mdash; Insert Form
          </Button>
        </div>
      </div>
    </div>
  );
};

export default CaptureFieldsFormGenerator;
