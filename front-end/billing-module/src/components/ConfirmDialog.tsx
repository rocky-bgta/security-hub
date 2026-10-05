import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from 'common/Dialog';

interface IProps {
  isOpen: boolean;
  message: string;
  loading?: boolean;
  loadingText?: string;
  buttonText?: string;
  onClose: () => void;
  onConfirm: () => void;
}

const ConfirmDialog = ({
  isOpen,
  message,
  loading = false,
  loadingText = 'Updating...',
  buttonText = 'Confirm',
  onClose,
  onConfirm,
}: IProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogTitle></DialogTitle>
      <DialogDescription></DialogDescription>
      <DialogContent className="!w-1/3 !px-6 py-6">
        <h2 className="mb-4 text-lg font-medium text-white">{message}</h2>
        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button onClick={onConfirm} disabled={loading}>
            {loading ? loadingText : buttonText}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ConfirmDialog;
