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
import { ILanguage } from 'models/Languages';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  language: ILanguage;
  onDeleteLanguage: () => void;
}

const DeleteLanguage = ({
  isOpen,
  setIsOpen,
  language,
  onDeleteLanguage,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);

  const apiClient = useAPI();

  const handleDeleteLanguage = async () => {
    setLoading(true);

    try {
      await apiClient.del(
        API_END_POINTS.DELETE_LANGUAGE.replace(':id', language.id),
      );
      onDeleteLanguage();
    } catch (error) {
      console.error('Error deleting language:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete Language</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the organization size "
            {language.displayName}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteLanguage}
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

export default DeleteLanguage;
