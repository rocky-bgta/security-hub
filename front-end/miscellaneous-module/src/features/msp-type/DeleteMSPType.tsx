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
import { IMSPType } from 'models/MSPType';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  mspType: IMSPType;
  onDeleteMSPType: () => void;
}

const DeleteMSPType = ({
  isOpen,
  setIsOpen,
  mspType,
  onDeleteMSPType,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const handleDeleteMSPType = async () => {
    setLoading(true);
    try {
      await apiClient.del(
        API_END_POINTS.DELETE_MSP_TYPE.replace(':id', mspType.id),
      );
      onDeleteMSPType();
    } catch (error) {
      console.error('Error deleting MSP Type:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete MSP Type</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the MSP Type &quot;{mspType.name}
            &quot;? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteMSPType}
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

export default DeleteMSPType;
