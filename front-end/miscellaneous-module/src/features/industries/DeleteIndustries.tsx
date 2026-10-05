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
import { IIndustries } from 'models/Industries';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  industries: IIndustries;
  onDeleteIndustries: () => void;
}

const DeleteIndustries = ({
  isOpen,
  setIsOpen,
  industries,
  onDeleteIndustries,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteIndustries = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_INDUSTRIES.replace(':id', industries.id),
      );
      onDeleteIndustries();
    } catch (error) {
      console.error('Error deleting industries:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Organization Size</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the organization size "
            {industries.name}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteIndustries}
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

export default DeleteIndustries;
