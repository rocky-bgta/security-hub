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
import { IContentType } from 'models/ContentType';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  contentType: IContentType;
  onDeleteContentType: () => void;
}

const DeleteContentType = ({
  isOpen,
  setIsOpen,
  contentType,
  onDeleteContentType,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const handleDeleteContentType = () => async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_CONTENT_TYPE.replace(':id', contentType.id),
      );
      onDeleteContentType();
    } catch (error) {
      console.error('Error deleting content type:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Content Type</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the content type "
            {contentType.typeName}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteContentType}
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

export default DeleteContentType;
