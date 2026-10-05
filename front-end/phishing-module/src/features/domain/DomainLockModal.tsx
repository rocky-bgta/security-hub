import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { AlertTriangle, Lock, LockOpen, RefreshCw } from 'lucide-react';
import { DomainStatus, IDomain } from 'models/Domain';
import { useState } from 'react';

interface DomainLockModalProps {
  isOpen: boolean;
  onClose: () => void;
  domain: IDomain | null;
  onLock: (domainId: string) => Promise<boolean>;
  onUnlock: (domainId: string) => Promise<boolean>;
}

/**
 * Confirmation modal for locking/unlocking a domain
 */
const DomainLockModal = ({
  isOpen,
  onClose,
  domain,
  onLock,
  onUnlock,
}: DomainLockModalProps) => {
  const [submitting, setSubmitting] = useState(false);

  if (!domain) return null;

  const isLocking = domain.status === DomainStatus.VERIFIED;
  const action = isLocking ? 'Lock' : 'Unlock';

  const handleConfirm = async () => {
    setSubmitting(true);
    let success: boolean;

    if (isLocking) {
      success = await onLock(domain.domainId);
    } else {
      success = await onUnlock(domain.domainId);
    }

    setSubmitting(false);
    if (success) {
      onClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            {isLocking ? (
              <Lock className="size-5 text-primary" />
            ) : (
              <LockOpen className="size-5 text-yellow-600" />
            )}
            {action} Domain
          </DialogTitle>
          <DialogDescription>
            {isLocking
              ? 'Are you sure you want to lock this domain?'
              : 'Are you sure you want to unlock this domain?'}
          </DialogDescription>
        </DialogHeader>

        <div className="py-4">
          {/* Domain info */}
          <div className="rounded-md p-4">
            <p className="text-sm text-foreground">Domain:</p>
            <p className="text-lg font-medium text-primary">{domain.domain}</p>
          </div>

          {/* Warning message */}
          {isLocking && (
            <div className="mt-4 flex items-start gap-3 rounded-md border border-yellow-600/30 bg-yellow-600/10 p-3">
              <AlertTriangle className="mt-0.5 size-5 shrink-0 text-yellow-600" />
              <div className="text-sm">
                <p className="font-medium text-yellow-600">Important</p>
                <p className="mt-1 text-foreground">
                  Locking this domain will prevent other organizations from
                  verifying and using it for phishing campaigns. This action is
                  available only for Professional and Enterprise plans.
                </p>
              </div>
            </div>
          )}

          {!isLocking && (
            <div className="mt-4 flex items-start gap-3 rounded-md border border-yellow-600/30 bg-yellow-600/10 p-3">
              <AlertTriangle className="mt-0.5 size-5 shrink-0 text-yellow-600" />
              <div className="text-sm">
                <p className="font-medium text-yellow-600">Warning</p>
                <p className="mt-1 text-foreground">
                  Unlocking this domain will allow other organizations to verify
                  and potentially use it. Make sure this is intentional.
                </p>
              </div>
            </div>
          )}

          {/* Upgrade message for non-eligible users */}
          {isLocking && !domain.canLock && (
            <div className="mt-4 rounded-md border border-primary/30 bg-primary/10 p-3">
              <p className="text-sm text-foreground">
                Domain locking is available only for{' '}
                <span className="font-medium text-primary">Professional</span>{' '}
                and <span className="font-medium text-primary">Enterprise</span>{' '}
                plans. Please upgrade your subscription to access this feature.
              </p>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button
            onClick={handleConfirm}
            disabled={submitting || (isLocking && !domain.canLock)}
            variant={isLocking ? 'default' : 'destructive'}
          >
            {submitting ? (
              <>
                <RefreshCw className="mr-2 size-4 animate-spin" />
                Processing...
              </>
            ) : (
              <>
                {isLocking ? (
                  <Lock className="mr-2 size-4" />
                ) : (
                  <LockOpen className="mr-2 size-4" />
                )}
                {action} Domain
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default DomainLockModal;
