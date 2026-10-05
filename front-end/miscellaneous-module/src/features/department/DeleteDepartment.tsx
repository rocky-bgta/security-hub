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
import { IDepartment } from 'models/Department';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  department: IDepartment;
  onDeleteDepartment: () => void;
}

const DeleteDepartment = ({
  isOpen,
  setIsOpen,
  department,
  onDeleteDepartment,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const handleDeleteDepartment = async () => {
    setLoading(true);
    try {
      await apiClient.del(
        API_END_POINTS.DELETE_DEPARTMENT.replace(':id', department.id),
      );
      onDeleteDepartment();
    } catch (error) {
      console.error('Error deleting department:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Department</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the department "{department.name}"?
            This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteDepartment}
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

export default DeleteDepartment;
