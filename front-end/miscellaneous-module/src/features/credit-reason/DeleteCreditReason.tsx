import { useState } from 'react';

import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { useAPI } from 'hooks/UseAPI';
import { ICreditReason } from 'models/CreditReason';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  creditReason: ICreditReason;
  onDeleteCreditReason: () => void;
}

const DeleteCreditReason = ({
  isOpen,
  setIsOpen,
  creditReason,
  onDeleteCreditReason,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const handleDeleteCreditReason = async () => {
    setLoading(true);
    try {
      await apiClient.del(
        API_END_POINTS.DELETE_CREDIT_REASON.replace(':id', creditReason.id),
      );
      onDeleteCreditReason();
    } catch (error) {
      console.error('Error deleting credit reason:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Credit Reason</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the credit reason &quot;
            {creditReason.reasonName}&quot;? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteCreditReason}
            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            disabled={loading}
          >
            {loading ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default DeleteCreditReason;
