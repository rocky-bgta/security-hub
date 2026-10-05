import { useState } from 'react';
import { AlertTriangle, Loader2, Trash2 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Button } from 'common/Button';
import { ISmsServerConfiguration } from 'models/SmsServerConfiguration';

interface SmsServerConfigurationDeleteConfirmProps {
  isOpen: boolean;
  onClose: () => void;
  configuration: ISmsServerConfiguration | null;
  onDelete: (id: string) => Promise<boolean>;
}

export const SmsServerConfigurationDeleteConfirm = ({
  isOpen,
  onClose,
  configuration,
  onDelete,
}: SmsServerConfigurationDeleteConfirmProps) => {
  const [deleting, setDeleting] = useState(false);

  if (!configuration) return null;

  const handleDelete = async () => {
    setDeleting(true);
    const success = await onDelete(configuration.id);
    setDeleting(false);
    if (success) {
      onClose();
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={open => !open && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-vibrant-red">
            <Trash2 className="size-5" />
            Delete SMS Configuration
          </DialogTitle>
          <DialogDescription>
            Are you sure you want to delete this SMS server configuration?
          </DialogDescription>
        </DialogHeader>

        <div className="py-4">
          <div className="rounded-md p-4">
            <p className="text-sm text-foreground">Configuration:</p>
            <p className="font-medium text-primary">{configuration.name}</p>
          </div>

          <div className="mt-4 flex items-start gap-3 rounded-md border border-vibrant-red/30 bg-vibrant-red/10 p-3">
            <AlertTriangle className="mt-0.5 size-5 shrink-0 text-vibrant-red" />
            <div className="text-sm">
              <p className="font-medium text-vibrant-red">Warning</p>
              <p className="mt-1 text-muted-foreground">
                This action cannot be undone. The configuration will be
                permanently deleted and cannot be recovered.
              </p>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button
            variant="destructive"
            onClick={handleDelete}
            disabled={deleting}
          >
            {deleting ? (
              <>
                <Loader2 className="mr-2 size-4 animate-spin" />
                Deleting...
              </>
            ) : (
              <>
                <Trash2 className="mr-2 size-4" />
                Delete Configuration
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default SmsServerConfigurationDeleteConfirm;
