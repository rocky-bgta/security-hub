import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { CAPTURE_FIELDS } from 'models/LandingPage';
import React from 'react';

interface DataCaptureConfigProps {
  enabled: boolean;
  onEnabledChange: (enabled: boolean) => void;
  selectedFields: string[];
  onFieldsChange: (fields: string[]) => void;
  redirectUrl: string;
  onRedirectUrlChange: (url: string) => void;
  redirectUrlError?: string;
}

/**
 * Data capture configuration component
 * Based on Task-05 Landing Page Creation (AC-09)
 */
export const DataCaptureConfig: React.FC<DataCaptureConfigProps> = ({
  enabled,
  onEnabledChange,
  selectedFields,
  onFieldsChange,
  redirectUrl,
  onRedirectUrlChange,
  redirectUrlError: _redirectUrlError,
}) => {
  const handleFieldToggle = (fieldId: string) => {
    if (selectedFields.includes(fieldId)) {
      onFieldsChange(selectedFields.filter(f => f !== fieldId));
    } else {
      onFieldsChange([...selectedFields, fieldId]);
    }
  };

  const handleSelectAll = () => {
    if (selectedFields.length === CAPTURE_FIELDS.length) {
      onFieldsChange([]);
    } else {
      onFieldsChange(CAPTURE_FIELDS.map(f => f.id));
    }
  };

  return (
    <div className="overflow-hidden rounded-lg border border-card-border">
      {/* Header with toggle */}
      <div className="flex items-center justify-between border-b border-card-border p-4">
        <div className="flex items-center gap-3">
          <label className="relative inline-flex cursor-pointer items-center">
            <input
              type="checkbox"
              checked={enabled}
              onChange={e => onEnabledChange(e.target.checked)}
              className="peer sr-only"
            />
            <div className="peer h-6 w-11 rounded-full bg-primary/10 after:absolute after:left-[2px] after:top-[2px] after:size-5 after:rounded-full after:border after:border-card-border after:bg-white after:transition-all after:content-[''] peer-checked:bg-primary peer-checked:after:translate-x-full peer-checked:after:border-white peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary" />
          </label>
          <div>
            <h4 className="font-medium text-muted-foreground">
              Capture Submitted Data
            </h4>
            <p className="text-sm text-muted-foreground">
              Capture form data submitted by users
            </p>
          </div>
        </div>

        {enabled && (
          <button
            type="button"
            onClick={handleSelectAll}
            className="text-sm text-primary hover:text-primary"
          >
            {selectedFields.length === CAPTURE_FIELDS.length
              ? 'Deselect All'
              : 'Select All'}
          </button>
        )}
      </div>

      {/* Field selection */}
      {enabled && (
        <div className="p-4">
          <p className="mb-4 text-sm text-muted-foreground">
            Select which form fields to capture:
          </p>

          <div className="mb-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
            {CAPTURE_FIELDS.map(field => {
              const isSelected = selectedFields.includes(field.id);
              return (
                <label
                  key={field.id}
                  className={`flex cursor-pointer items-center gap-2 rounded-lg border p-3 transition-colors ${
                    isSelected
                      ? 'border-primary bg-primary/10 text-primary'
                      : 'border-card-border bg-card-background text-muted-foreground'
                  }`}
                >
                  <input
                    type="checkbox"
                    checked={isSelected}
                    onChange={() => handleFieldToggle(field.id)}
                    className="size-4 rounded border-gray-300 text-primary focus:ring-primary"
                  />
                  <span className="text-sm font-medium text-muted-foreground">
                    {field.label}
                  </span>
                </label>
              );
            })}
          </div>

          {/* Selected count */}
          {selectedFields.length > 0 && (
            <div className="mb-4 rounded-lg border border-primary bg-primary/10 p-3">
              <p className="text-sm text-primary">
                <svg
                  className="mr-1 inline size-4"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
                {selectedFields.length} field
                {selectedFields.length > 1 ? 's' : ''} will be captured
              </p>
            </div>
          )}

          {/* Redirect URL */}
          <div className="border-t border-card-border pt-4">
            <Label htmlFor="redirectUrl">
              Redirect URL (after form submission)
            </Label>
            <Input
              id="redirectUrl"
              type="url"
              value={redirectUrl}
              onChange={e => onRedirectUrlChange(e.target.value)}
              placeholder="https://example.com/thank-you"
            />
            <p className="mt-1 text-xs text-muted-foreground">
              Users will be redirected to this URL after submitting the form
            </p>
          </div>
        </div>
      )}

      {/* Disabled state */}
      {!enabled && (
        <div className="p-4 text-center text-muted-foreground">
          <svg
            className="mx-auto mb-2 size-12 text-muted-foreground"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1}
              d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
            />
          </svg>
          <p className="text-sm">
            Enable data capture to configure form fields
          </p>
        </div>
      )}
    </div>
  );
};

export default DataCaptureConfig;
