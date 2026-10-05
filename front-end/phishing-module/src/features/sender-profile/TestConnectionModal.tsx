import { Button } from 'common/Button';
import { Dialog, DialogContent } from 'common/Dialog';
import { ITestResult } from 'models/SenderProfile';
import { useEffect, useState } from 'react';

interface TestConnectionModalProps {
  isOpen: boolean;
  onClose: () => void;
  profileName: string;
  onTest: () => Promise<ITestResult | null>;
}

/**
 * Modal for testing SMTP connection
 * Based on Task-06 Sender Profile Management (AC-07, AC-08)
 */
export const TestConnectionModal = ({
  isOpen,
  onClose,
  profileName,
  onTest,
}: TestConnectionModalProps) => {
  const [testing, setTesting] = useState(false);
  const [result, setResult] = useState<ITestResult | null>(null);

  const runTest = async () => {
    setTesting(true);
    setResult(null);
    const testResult = await onTest();
    setResult(testResult);
    setTesting(false);
  };

  useEffect(() => {
    if (isOpen) {
      setTimeout(() => {
        setResult(null);
        runTest();
      }, 0);
    }
  }, [isOpen]);

  const handleRetry = () => {
    runTest();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <div className="space-y-6">
          {/* Profile info */}
          <div className="rounded-lg p-4">
            <p className="text-sm text-muted-foreground">Testing profile</p>
            <p className="font-medium text-foreground">{profileName}</p>
          </div>

          {/* Testing state */}
          {testing && (
            <div className="py-8 text-center">
              <div className="mx-auto mb-4 size-12 animate-spin rounded-full border-4 border-primary border-t-transparent" />
              <p className="text-muted-foreground">
                Testing SMTP connection...
              </p>
              <p className="mt-2 text-sm text-muted-foreground">
                This may take a few seconds
              </p>
            </div>
          )}

          {/* Result */}
          {!testing && result && (
            <div className="space-y-4">
              {/* Success/Failure Banner */}
              <div
                className={`rounded-lg p-4 ${
                  result.success
                    ? 'border border-green-200 bg-green-500/10'
                    : 'border border-red-200 bg-red-500/10'
                }`}
              >
                <div className="flex items-center gap-3">
                  {result.success ? (
                    <svg
                      className="size-8 text-primary"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                    >
                      <path
                        fillRule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                        clipRule="evenodd"
                      />
                    </svg>
                  ) : (
                    <svg
                      className="size-8 text-destructive"
                      fill="currentColor"
                      viewBox="0 0 20 20"
                    >
                      <path
                        fillRule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                        clipRule="evenodd"
                      />
                    </svg>
                  )}
                  <div>
                    <h3
                      className={`font-medium ${result.success ? 'text-primary' : 'text-destructive'}`}
                    >
                      {result.success
                        ? 'Connection Successful'
                        : 'Connection Failed'}
                    </h3>
                    <p
                      className={`text-sm ${result.success ? 'text-primary' : 'text-destructive'}`}
                    >
                      {result.message}
                    </p>
                  </div>
                </div>
              </div>

              {/* Details */}
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground">Response Time</p>
                  <p className="font-medium text-foreground">
                    {result.responseTimeMs}ms
                  </p>
                </div>
                <div>
                  <p className="text-muted-foreground">Status</p>
                  <p
                    className={`font-medium ${result.success ? 'text-primary' : 'text-destructive'}`}
                  >
                    {result.success ? 'Verified' : 'Failed'}
                  </p>
                </div>
              </div>

              {/* Server Response (on failure) */}
              {!result.success && result.serverResponse && (
                <div className="rounded border border-card-border bg-card-background p-4">
                  <p className="mb-2 text-sm text-muted-foreground">
                    Server Response
                  </p>
                  <pre className="whitespace-pre-wrap rounded border bg-card-background p-3 font-mono text-xs text-foreground">
                    {result.serverResponse}
                  </pre>
                </div>
              )}
            </div>
          )}

          {/* Actions */}
          <div className="flex justify-end gap-3 border-t border-card-border bg-card-background p-4">
            {!testing && !result?.success && (
              <Button variant="outline" onClick={handleRetry}>
                <svg
                  className="mr-2 size-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                  />
                </svg>
                Retry
              </Button>
            )}
            <Button variant="default" onClick={onClose}>
              Close
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default TestConnectionModal;
