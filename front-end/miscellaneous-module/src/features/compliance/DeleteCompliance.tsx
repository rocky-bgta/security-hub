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
import { ICompliance } from 'models/Compliance';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  compliance: ICompliance;
  onDeleteCompliance: () => void;
}

const DeleteCompliance = ({
  isOpen,
  setIsOpen,
  compliance,
  onDeleteCompliance,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const handleDeleteCompliance = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_COMPLIANCE.replace(':id', compliance.id),
      );
      onDeleteCompliance();
    } catch (error) {
      console.error('Error deleting compliance:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Compliance</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the compliance "
            {compliance.complianceName}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteCompliance}
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

export default DeleteCompliance;
