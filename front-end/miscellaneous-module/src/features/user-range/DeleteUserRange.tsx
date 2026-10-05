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
import { IUserRange } from 'models/UserRange';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  userRange: IUserRange;
  onDeleteUserRange: () => void;
}

const DeleteUserRange = ({
  isOpen,
  setIsOpen,
  userRange,
  onDeleteUserRange,
}: IProps) => {
  const handleDeleteUserRange = async () => {
    onDeleteUserRange();
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Delete User Range</DialogTitle>
          <DialogDescription>
            Are you sure you want to delete the user range "
            {userRange.rangeName}"? This action cannot be undone.
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button variant="secondary" onClick={() => setIsOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleDeleteUserRange}
            className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
          >
            Delete
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default DeleteUserRange;
