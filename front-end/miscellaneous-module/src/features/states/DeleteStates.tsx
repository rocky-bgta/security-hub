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
import { IState } from 'models/States';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  state: IState;
  onDeleteState: () => void;
}

const DeleteStates = ({ isOpen, setIsOpen, state, onDeleteState }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteState = async () => {
    setLoading(true);

    try {
      await apiClient.del(API_END_POINTS.DELETE_STATE.replace(':id', state.id));
      onDeleteState();
    } catch (error) {
      console.error('Error deleting state:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete State</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the organization size "{state.name}
            "? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteState}
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

export default DeleteStates;
