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
import { ISupportTicketType } from 'models/Billing';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  supportTicketType: ISupportTicketType;
  onDeleteSupportTicketType: () => void;
}

const DeleteSupportTicketType = ({
  isOpen,
  setIsOpen,
  supportTicketType,
  onDeleteSupportTicketType,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteSupportTicketType = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_SUPPORT_TICKET_TYPE.replace(
          ':id',
          supportTicketType.id,
        ),
      );
      onDeleteSupportTicketType();
    } catch (error) {
      console.error('Error deleting support ticket type:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Support Ticket Type</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the support ticket type "
            {supportTicketType.name}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteSupportTicketType}
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

export default DeleteSupportTicketType;
