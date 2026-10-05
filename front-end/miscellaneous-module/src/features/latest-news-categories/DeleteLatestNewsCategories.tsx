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
import { ILatestNewsCategories } from 'models/LatestNewsCategories';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  latestNewsCategories: ILatestNewsCategories;
  onDeleteLatestNewsCategories: () => void;
}

const DeleteLatestNewsCategories = ({
  isOpen,
  setIsOpen,
  latestNewsCategories,
  onDeleteLatestNewsCategories,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteLatestNewsCategories = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_LATEST_NEWS_CATEGORY.replace(
          ':id',
          latestNewsCategories.id,
        ),
      );
      onDeleteLatestNewsCategories();
    } catch (error) {
      console.error('Error deleting latest news categories:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Latest News Categories</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the latest news categories"
            {latestNewsCategories.name}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteLatestNewsCategories}
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

export default DeleteLatestNewsCategories;
