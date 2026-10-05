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
import { INetTerm } from 'models/NetTerm';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  netTerm: INetTerm;
  onDeleteNetTerm: () => void;
}

const DeleteNetTerm = ({
  isOpen,
  setIsOpen,
  netTerm,
  onDeleteNetTerm,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const handleDeleteNetTerm = async () => {
    setLoading(true);
    try {
      await apiClient.del(
        API_END_POINTS.DELETE_NET_TERM.replace(':id', netTerm.id),
      );
      onDeleteNetTerm();
    } catch (error) {
      console.error('Error deleting net term:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Net Term</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the net term &quot;
            {netTerm.netTermName}&quot;? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteNetTerm}
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

export default DeleteNetTerm;
