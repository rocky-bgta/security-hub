import { Badge } from 'common/Badge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { ICoupon } from 'models/Coupon';
import { formatDateTime, getUsagePercentage, isExpired } from 'utils/Helper';

interface ViewCouponModalProps {
  isOpen: boolean;
  onClose: () => void;
  coupon: ICoupon | null;
}

const ViewCouponModal = ({ isOpen, onClose, coupon }: ViewCouponModalProps) => {
  if (!coupon) return null;

  const getStatusBadge = () => {
    if (!coupon.active) {
      return <Badge variant="secondary">Inactive</Badge>;
    }
    if (isExpired(coupon.validUntil)) {
      return <Badge variant="destructive">Expired</Badge>;
    }
    return <Badge className="bg-green-500 hover:bg-green-600">Active</Badge>;
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[80vh] w-2/3 overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Coupon Details</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Name
              </h4>
              <p className="text-sm">{coupon.name}</p>
            </div>
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Code
              </h4>
              <code className="rounded bg-muted px-2 py-1 text-sm">
                {coupon.code}
              </code>
            </div>
          </div>

          <div>
            <h4 className="text-sm font-semibold text-muted-foreground">
              Description
            </h4>
            <p className="text-sm">{coupon.description}</p>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Type
              </h4>
              <Badge variant="outline">{coupon.type}</Badge>
            </div>
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Value
              </h4>
              <p className="text-sm">{coupon.value}</p>
            </div>
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Status
              </h4>
              {getStatusBadge()}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Valid From
              </h4>
              <p className="text-sm">{formatDateTime(coupon.validFrom)}</p>
            </div>
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Valid Until
              </h4>
              <p className="text-sm">{formatDateTime(coupon.validUntil)}</p>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Usage
              </h4>
              <div className="flex items-center gap-2">
                <span className="text-sm">
                  {coupon.totalUsed}/{coupon.usageLimit}
                </span>
                {coupon.usageLimit > 0 && (
                  <div className="h-2 w-full rounded-full bg-muted">
                    <div
                      className="h-2 rounded-full bg-primary"
                      style={{
                        width: `${getUsagePercentage(coupon.totalUsed, coupon.usageLimit)}%`,
                      }}
                    />
                  </div>
                )}
              </div>
            </div>
            <div>
              <h4 className="text-sm font-semibold text-muted-foreground">
                Min Purchase
              </h4>
              <p className="text-sm">
                {coupon.currency} {coupon.minPurchaseAmount}
              </p>
            </div>
          </div>

          {coupon.productRestrictions &&
            coupon.productRestrictions.length > 0 && (
              <div>
                <h4 className="mb-2 text-sm font-semibold text-muted-foreground">
                  Product Restrictions
                </h4>
                <div className="space-y-2">
                  {coupon.productRestrictions.map((restriction, index) => (
                    <div
                      key={index}
                      className="rounded border border-card-border bg-muted/30 p-3"
                    >
                      <div className="grid grid-cols-2 gap-2">
                        <div>
                          <p className="text-xs font-medium text-muted-foreground">
                            Product
                          </p>
                          <p className="text-sm font-medium">
                            {restriction.productName}
                          </p>
                        </div>
                        <div>
                          <p className="text-xs font-medium text-muted-foreground">
                            Package
                          </p>
                          <p className="text-sm font-medium">
                            {restriction.packageName}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ViewCouponModal;
