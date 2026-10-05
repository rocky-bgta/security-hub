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
    <Dialog
      open={isOpen}
      onOpenChange={open => {
        if (!open) onClose();
      }}
    >
      <DialogTitle></DialogTitle>
      <DialogDescription></DialogDescription>
      <DialogContent className="!home-w-1/3 !home-px-6 home-py-6">
        <h2 className="home-mb-4 home-text-lg home-font-medium home-text-white">
          {message}
        </h2>
        <div className="home-flex home-justify-end home-gap-3">
          <Button variant="outline" onClick={onClose} disabled={loading}>
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
