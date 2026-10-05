import { Badge } from 'common/Badge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { ITier } from 'models/Tier';
import { formatDate } from 'utils/Helper';

interface ViewTierModalProps {
  isOpen: boolean;
  onClose: () => void;
  tier: ITier | null;
}

const ViewTierModal = ({ isOpen, onClose, tier }: ViewTierModalProps) => {
  if (!tier) return null;

  const getStatusBadge = () => {
    if (tier.active) {
      return <Badge className="bg-green-500 hover:bg-green-600">Active</Badge>;
    }
    return <Badge variant="secondary">Inactive</Badge>;
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="w-2/3">
        <DialogHeader>
          <DialogTitle>Tier Details</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Tier Name
              </h4>
              <p className="text-sm">{tier.tierName}</p>
            </div>
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Status
              </h4>
              {getStatusBadge()}
            </div>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-muted-foreground">
              Description
            </h4>
            <p className="text-sm">{tier.tierDescription || 'N/A'}</p>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Commission Percentage
              </h4>
              <Badge variant="outline" className="mt-1">
                {tier.commissionPercentage}%
              </Badge>
            </div>
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Sales Threshold
              </h4>
              <p className="text-sm">${tier.salesThreshold.toLocaleString()}</p>
            </div>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-muted-foreground">
              Eligibility Criteria
            </h4>
            <p className="text-sm">{tier.eligibilityCriteria || 'N/A'}</p>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-muted-foreground">
              Tier Benefits
            </h4>
            <p className="text-sm">{tier.tierBenefits || 'N/A'}</p>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-muted-foreground">
              Created At
            </h4>
            <p className="text-sm">{formatDate(tier.createdAt)}</p>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ViewTierModal;
