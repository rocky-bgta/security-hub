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
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IBillingAction } from 'models/Billing';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  billingNextStep: IBillingAction;
  onDeleteBillingNextStep: () => void;
}

const DeleteBillingsNextStep = ({
  isOpen,
  setIsOpen,
  billingNextStep,
  onDeleteBillingNextStep,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteBilling = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_BILLING_NEXT_STEP.replace(
          ':id',
          billingNextStep.id,
        ),
      );
      onDeleteBillingNextStep();
    } catch (error) {
      console.error('Error deleting billing next step:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Billing Next Step</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the billing next step "
            {billingNextStep.name}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteBilling}
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

export default DeleteBillingsNextStep;
