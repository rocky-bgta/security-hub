import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import { Dialog, DialogContent, DialogTitle } from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { IImportedSite } from 'models/LandingPage';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { ImportSiteSchema, TImportSiteForm } from 'schemas/LandingPageSchema';

interface ImportSiteModalProps {
  isOpen: boolean;
  onClose: () => void;
  onImport: (
    url: string,
    includeAssets: boolean,
  ) => Promise<IImportedSite | null>;
  onUseContent: (importedSite: IImportedSite) => void;
  importing?: boolean;
}

/**
 * Modal for importing website content from URL
 * Based on Task-05 Landing Page Creation (AC-03, AC-04, AC-05)
 */
export const ImportSiteModal = ({
  isOpen,
  onClose,
  onImport,
  onUseContent,
  importing = false,
}: ImportSiteModalProps) => {
  const [importedSite, setImportedSite] = useState<IImportedSite | null>(null);
  const [showPreview, setShowPreview] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors: _errors },
    reset,
  } = useForm<TImportSiteForm>({
    resolver: zodResolver(ImportSiteSchema),
    defaultValues: {
      websiteUrl: '',
      includeAssets: true,
    },
  });

  const handleImport = async (data: TImportSiteForm) => {
    const result = await onImport(data.websiteUrl, data.includeAssets ?? true);
    if (result) {
      setImportedSite(result);
      setShowPreview(true);
    }
  };

  const handleUse = () => {
    if (importedSite) {
      onUseContent(importedSite);
      handleClose();
    }
  };

  const handleClose = () => {
    setImportedSite(null);
    setShowPreview(false);
    reset();
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] w-2/3 overflow-y-auto">
        <DialogTitle>Import Website</DialogTitle>
        <div className="space-y-6">
          {!showPreview ? (
            // Import Form
            <form onSubmit={handleSubmit(handleImport)} className="space-y-4">
              <div>
                <Label htmlFor="websiteUrl">Website URL</Label>
                <Input
                  id="websiteUrl"
                  type="url"
                  {...register('websiteUrl')}
                  placeholder="https://example.com"
                />
                <p className="mt-1 text-xs text-gray-500">
                  Enter the URL of the website you want to import
                </p>
              </div>

              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="includeAssets"
                  {...register('includeAssets')}
                  className="size-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <Label htmlFor="includeAssets" className="mb-0 cursor-pointer">
                  Include images and assets
                </Label>
              </div>

              <div className="rounded-lg border border-yellow-200 bg-yellow-50 p-4">
                <div className="flex items-start gap-2">
                  <svg
                    className="mt-0.5 size-5 text-yellow-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                    />
                  </svg>
                  <div className="text-sm text-yellow-800">
                    <p className="font-medium">Important:</p>
                    <ul className="mt-1 list-inside list-disc space-y-1">
                      <li>Imported content will be sanitized for security</li>
                      <li>JavaScript will be removed from imported pages</li>
                      <li>Some dynamic content may not work correctly</li>
                    </ul>
                  </div>
                </div>
              </div>

              <div className="flex justify-end gap-3 border-t border-gray-200 pt-4">
                <Button type="button" variant="secondary" onClick={handleClose}>
                  Cancel
                </Button>
                <Button type="submit" variant="default" disabled={importing}>
                  {importing ? (
                    <>
                      <svg
                        className="-ml-1 mr-2 size-4 animate-spin"
                        fill="none"
                        viewBox="0 0 24 24"
                      >
                        <circle
                          className="opacity-25"
                          cx="12"
                          cy="12"
                          r="10"
                          stroke="currentColor"
                          strokeWidth="4"
                        />
                        <path
                          className="opacity-75"
                          fill="currentColor"
                          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                        />
                      </svg>
                      Importing...
                    </>
                  ) : (
                    'Import Website'
                  )}
                </Button>
              </div>
            </form>
          ) : (
            // Preview imported content
            <div className="space-y-4">
              {/* Info cards */}
              <div className="grid grid-cols-2 gap-4">
                <div className="rounded-lg p-4">
                  <p className="text-sm text-gray-500">Page Title</p>
                  <p className="font-medium text-foreground">
                    {importedSite?.pageTitle || 'Untitled'}
                  </p>
                </div>
                <div className="rounded-lg p-4">
                  <p className="text-sm text-gray-500">Login Form Detected</p>
                  <p className="font-medium text-foreground">
                    {importedSite?.hasLoginForm ? (
                      <span className="flex items-center gap-1 text-green-600">
                        <svg
                          className="size-4"
                          fill="currentColor"
                          viewBox="0 0 20 20"
                        >
                          <path
                            fillRule="evenodd"
                            d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                            clipRule="evenodd"
                          />
                        </svg>
                        Yes
                      </span>
                    ) : (
                      'No'
                    )}
                  </p>
                </div>
              </div>

              {/* Form fields detected */}
              {importedSite?.detectedFormFields &&
                importedSite.detectedFormFields.length > 0 && (
                  <div className="rounded-lg border border-blue-200 bg-blue-50 p-4">
                    <p className="mb-2 text-sm font-medium text-blue-800">
                      Detected Form Fields:
                    </p>
                    <div className="flex flex-wrap gap-2">
                      {importedSite.detectedFormFields.map((field, index) => (
                        <span
                          key={index}
                          className="rounded bg-blue-100 px-2 py-1 text-xs text-blue-800"
                        >
                          {field}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

              {/* Preview iframe */}
              <div className="overflow-hidden rounded-lg border border-gray-200">
                <div className="border-b border-gray-200 p-2">
                  <span className="text-sm text-gray-500">Preview</span>
                </div>
                <div className="h-64 bg-white">
                  {importedSite?.htmlContent ? (
                    <iframe
                      srcDoc={importedSite.htmlContent}
                      title="Imported Content Preview"
                      className="size-full border-0"
                      sandbox="allow-same-origin"
                    />
                  ) : (
                    <div className="flex h-full items-center justify-center text-gray-400">
                      No content to preview
                    </div>
                  )}
                </div>
              </div>

              <div className="flex justify-between gap-3 border-t border-gray-200 pt-4">
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => {
                    setShowPreview(false);
                    setImportedSite(null);
                  }}
                >
                  Import Different URL
                </Button>
                <div className="flex gap-3">
                  <Button
                    type="button"
                    variant="secondary"
                    onClick={handleClose}
                  >
                    Cancel
                  </Button>
                  <Button type="button" variant="default" onClick={handleUse}>
                    Use This Content
                  </Button>
                </div>
              </div>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ImportSiteModal;
