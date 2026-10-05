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
import { IMenu } from 'models/Menu';
import { API_END_POINTS } from 'routes/APIEndpoints';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  menu: IMenu;
  onDeleteMenu: () => void;
}

const DeleteMenu = ({ isOpen, onClose, menu, onDeleteMenu }: IProps) => {
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const handleDeleteMenu = async () => {
    setLoading(true);

    try {
      await apiClient.del(API_END_POINTS.DELETE_MENU.replace(':id', menu.id));
      onDeleteMenu();
    } catch (error) {
      console.error('Error deleting menu:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="text-foreground">Delete Menu</DialogTitle>
          <DialogDescription className="text-muted-foreground">
            Are you sure you want to delete the menu "{menu.name}"? This action
            cannot be undone.
            {menu.children?.length && (
              <span className="mt-2 block text-destructive">
                Warning: This menu has {menu.children.length} submenus.
              </span>
            )}
          </DialogDescription>
        </DialogHeader>
        <DialogFooter>
          <Button onClick={onClose}>Cancel</Button>
          <Button
            onClick={handleDeleteMenu}
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

export default DeleteMenu;
