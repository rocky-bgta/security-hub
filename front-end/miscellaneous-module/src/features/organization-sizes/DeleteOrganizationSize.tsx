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
import { IOrganizationSize } from 'models/OrganizationSize';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  organizationSize: IOrganizationSize;
  onDeleteOrganizationSize: () => void;
}

const DeleteOrganizationSize = ({
  isOpen,
  setIsOpen,
  organizationSize,
  onDeleteOrganizationSize,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteOrganizationSize = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_ORGANIZATION_SIZE.replace(
          ':id',
          organizationSize.id,
        ),
      );
      onDeleteOrganizationSize();
    } catch (error) {
      console.error('Error deleting organization size:', error);
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
            {organizationSize.name}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteOrganizationSize}
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

export default DeleteOrganizationSize;
