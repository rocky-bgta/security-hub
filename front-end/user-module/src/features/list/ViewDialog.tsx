import { Badge } from 'common/Badge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Label } from 'common/Label';

interface IProps {
  isOpen: boolean;
  setIsOpen: (open: boolean) => void;
  selectedMSP: {
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
}

const ViewDialog = ({ isOpen, setIsOpen, selectedMSP }: IProps) => {
  const getStatusVariant = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return 'bg-[#22c55e] text-[#f8fafc]';
      case 'completed':
        return 'bg-[#22c55e] text-[#f8fafc]';
      case 'pending':
        return 'bg-[#facc15] text-[#0f172a]';
      case 'overdue':
        return 'bg-[#ef4444] text-[#f8fafc]';
      case 'failed':
        return 'bg-[#ef4444] text-[#f8fafc]';
      case 'suspended':
        return 'bg-[#ef4444] text-[#f8fafc]';
      case 'inactive':
        return 'bg-muted text-muted-foreground';
      default:
        return 'bg-[#0ea5e9] text-[#f8fafc]';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>MSP Details - {selectedMSP?.name}</DialogTitle>
        </DialogHeader>
        {selectedMSP && (
          <div className="space-y-6">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-sm font-medium">MSP Name</Label>
                <p className="text-sm text-muted-foreground">
                  {selectedMSP.name}
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">Tier</Label>
                <p className="text-sm text-muted-foreground">
                  {selectedMSP.tier}
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">Status</Label>
                <Badge className={getStatusVariant(selectedMSP.status)}>
                  {selectedMSP.status}
                </Badge>
              </div>
              <div>
                <Label className="text-sm font-medium">Join Date</Label>
                <p className="text-sm text-muted-foreground">
                  {formatDate(selectedMSP.joinDate)}
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">Email</Label>
                <p className="text-sm text-muted-foreground">
                  {selectedMSP.email}
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">Phone</Label>
                <p className="text-sm text-muted-foreground">
                  {selectedMSP.phone}
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">Total Licenses</Label>
                <p className="text-sm text-muted-foreground">
                  {selectedMSP.totalLicenses.toLocaleString()}
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">Used Licenses</Label>
                <p className="text-sm text-muted-foreground">
                  {selectedMSP.usedLicenses.toLocaleString()}
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">Available Credit</Label>
                <p className="text-sm text-muted-foreground">
                  {formatCurrency(selectedMSP.availableCredit)}
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">Balance</Label>
                <p className="text-sm text-muted-foreground">
                  {formatCurrency(selectedMSP.balance)}
                </p>
              </div>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ViewDialog;
