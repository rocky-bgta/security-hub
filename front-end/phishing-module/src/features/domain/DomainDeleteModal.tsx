import { useState } from 'react';
import { Trash2, AlertTriangle, RefreshCw } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Button } from 'common/Button';
import { IDomain } from 'models/Domain';

interface DomainDeleteModalProps {
  isOpen: boolean;
  onClose: () => void;
  domain: IDomain | null;
  onDelete: (domainId: string) => Promise<boolean>;
}

/**
 * Confirmation modal for deleting a domain
 */
const DomainDeleteModal = ({
  isOpen,
  onClose,
  domain,
  onDelete,
}: DomainDeleteModalProps) => {
  const [submitting, setSubmitting] = useState(false);

  if (!domain) return null;

  const handleConfirm = async () => {
    setSubmitting(true);
    const success = await onDelete(domain.domainId);
    setSubmitting(false);

    if (success) {
      onClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-vibrant-red">
            <Trash2 className="size-5" />
            Delete Domain
          </DialogTitle>
          <DialogDescription>
            Are you sure you want to delete this domain?
          </DialogDescription>
        </DialogHeader>

        <div className="py-4">
          {/* Domain info */}
          <div className="rounded-md p-4">
            <p className="text-sm text-foreground">Domain:</p>
            <p className="text-lg font-medium text-primary">{domain.domain}</p>
          </div>

          {/* Warning message */}
          <div className="mt-4 flex items-start gap-3 rounded-md border border-vibrant-red/30 bg-vibrant-red/10 p-3">
            <AlertTriangle className="mt-0.5 size-5 shrink-0 text-vibrant-red" />
            <div className="text-sm">
              <p className="font-medium text-vibrant-red">Warning</p>
              <p className="mt-1 text-foreground">
                This action cannot be undone. Deleting this domain will remove
                all verification and lock status. You will need to verify it
                again if you want to use it in the future.
              </p>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button
            onClick={handleConfirm}
            disabled={submitting}
            variant="destructive"
          >
            {submitting ? (
              <>
                <RefreshCw className="mr-2 size-4 animate-spin" />
                Deleting...
              </>
            ) : (
              <>
                <Trash2 className="mr-2 size-4" />
                Delete Domain
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default DomainDeleteModal;
