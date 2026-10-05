import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  editingMSP: {
    name: string;
    tier: string;
    status: string;
    joinDate: string;
    email: string;
    phone: string;
    totalLicenses: number;
    usedLicenses: number;
    availableCredit: number;
    balance: number;
  } | null;
  setEditingMSP: (msp: any) => void;
  onClose: () => void;
  onSave: (msp: any) => void;
}

const EditDialog = ({
  isOpen,
  setIsOpen,
  editingMSP,
  setEditingMSP,
  onClose,
  onSave,
}: IProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Edit MSP - {editingMSP?.name}</DialogTitle>
        </DialogHeader>
        {editingMSP && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="name">MSP Name</Label>
                <Input
                  id="name"
                  value={editingMSP.name}
                  onChange={e =>
                    setEditingMSP({ ...editingMSP, name: e.target.value })
                  }
                />
              </div>
              <div>
                <Label htmlFor="tier">Tier</Label>
                <Select
                  value={editingMSP.tier}
                  onValueChange={value =>
                    setEditingMSP({ ...editingMSP, tier: value as any })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="Authorized Partner">
                      Authorized Partner
                    </SelectItem>
                    <SelectItem value="Silver Partner">
                      Silver Partner
                    </SelectItem>
                    <SelectItem value="Gold Partner">Gold Partner</SelectItem>
                    <SelectItem value="Platinum Partner">
                      Platinum Partner
                    </SelectItem>
                    <SelectItem value="Elite Partner">Elite Partner</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  value={editingMSP.email}
                  onChange={e =>
                    setEditingMSP({ ...editingMSP, email: e.target.value })
                  }
                />
              </div>
              <div>
                <Label htmlFor="phone">Phone</Label>
                <Input
                  id="phone"
                  value={editingMSP.phone}
                  onChange={e =>
                    setEditingMSP({ ...editingMSP, phone: e.target.value })
                  }
                />
              </div>
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={onClose}>
                Cancel
              </Button>
              <Button onClick={onSave}>Save Changes</Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default EditDialog;
