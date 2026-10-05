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
import { ISuspendReason } from 'models/SuspendReason';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  suspendReason: ISuspendReason;
  onDeleteSuspendReason: () => void;
}

const DeleteSuspendReason = ({
  isOpen,
  setIsOpen,
  suspendReason,
  onDeleteSuspendReason,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const handleDeleteSuspendReason = async () => {
    setLoading(true);
    try {
      await apiClient.del(
        API_END_POINTS.DELETE_SUSPEND_REASON.replace(':id', suspendReason.id),
      );
      onDeleteSuspendReason();
    } catch (error) {
      console.error('Error deleting suspend reason:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Suspend Reason</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the suspend reason &quot;
            {suspendReason.name}&quot;? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteSuspendReason}
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

export default DeleteSuspendReason;
