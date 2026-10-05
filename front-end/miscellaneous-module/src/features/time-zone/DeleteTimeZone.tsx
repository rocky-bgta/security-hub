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
import { ITimeZone } from 'models/TimeZone';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  timeZone: ITimeZone;
  onDeleteTimeZone: () => void;
}

const DeleteTimeZone = ({
  isOpen,
  setIsOpen,
  timeZone,
  onDeleteTimeZone,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteTimeZone = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_TIME_ZONE.replace(':id', timeZone.id),
      );
      onDeleteTimeZone();
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
          <DialogTitle>Delete Time Zone</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the organization size "
            {timeZone.displayName}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteTimeZone}
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

export default DeleteTimeZone;
