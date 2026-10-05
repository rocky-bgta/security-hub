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
import { ISubIndustries } from 'models/SubIndustries';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  subIndustries: ISubIndustries;
  onDeleteSubIndustries: () => void;
}

const DeleteSubIndustries = ({
  isOpen,
  setIsOpen,
  subIndustries,
  onDeleteSubIndustries,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteSubIndustries = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_SUB_INDUSTRIES.replace(':id', subIndustries.id),
      );
      onDeleteSubIndustries();
    } catch (error) {
      console.error('Error deleting sub-industry:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Sub-Industry</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the sub-industry &quot;
            {subIndustries.name}&quot;? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteSubIndustries}
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

export default DeleteSubIndustries;
