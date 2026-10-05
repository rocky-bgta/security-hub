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
import { IOrganizationType } from 'models/OrganizationType';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  organizationType: IOrganizationType;
  onDeleteOrganizationType: () => void;
}

const DeleteOrganizationType = ({
  isOpen,
  setIsOpen,
  organizationType,
  onDeleteOrganizationType,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteOrganizationType = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_ORGANIZATION_TYPE.replace(
          ':id',
          organizationType.id,
        ),
      );
      onDeleteOrganizationType();
    } catch (error) {
      console.error('Error deleting organization type:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Organization Type</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the organization type "
            {organizationType.organizationType}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteOrganizationType}
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

export default DeleteOrganizationType;
