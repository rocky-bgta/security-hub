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
  hideCancel?: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

const ConfirmDialog = ({
  isOpen,
  message,
  loading = false,
  loadingText = 'Updating...',
  buttonText = 'Confirm',
  hideCancel = false,
  onClose,
  onConfirm,
}: IProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogTitle>
        <DialogDescription></DialogDescription>
      </DialogTitle>
      <DialogContent className="!content-w-1/3 !content-px-6 content-py-6">
        <h2 className="content-mb-4 content-text-lg content-font-medium content-text-white">
          {message}
        </h2>
        <div className="content-flex content-justify-end content-gap-3">
          {!hideCancel && (
            <Button variant="destructive" onClick={onClose} disabled={loading}>
              Cancel
            </Button>
          )}
          <Button onClick={onConfirm} disabled={loading}>
            {loading ? loadingText : buttonText}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ConfirmDialog;
