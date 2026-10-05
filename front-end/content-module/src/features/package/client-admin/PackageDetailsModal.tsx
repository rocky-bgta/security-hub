import { Badge } from 'common/Badge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { useAPI } from 'hooks/UseAPI';
import {
  BookOpen,
  Calendar,
  Check,
  DollarSign,
  Loader2,
  Package,
  Shield,
  Users,
} from 'lucide-react';
import { IResponse } from 'models/Global';
import {
  IAssignedLicense,
  IClientAssignedPackageDetail,
} from 'models/License';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { HumanizeDate, humanizeText } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  packageData: IAssignedLicense;
}

const formatAmount = (amount: number, currency = 'USD') =>
  new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
  }).format(amount);

const getLicenseStatusBadge = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return (
        <Badge variant="default" className="content-bg-primary">
          Active
        </Badge>
      );
    case 'EXPIRING_SOON':
      return <Badge variant="destructive">Expiring Soon</Badge>;
    case 'EXPIRED':
      return <Badge variant="secondary">Expired</Badge>;
    default:
      return <Badge variant="outline">{humanizeText(status)}</Badge>;
  }
};

const PackageDetailsModal = ({ isOpen, onClose, packageData }: IProps) => {
  const apiClient = useAPI();

  const [packageDetails, setPackageDetails] =
    useState<IClientAssignedPackageDetail>();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (isOpen) {
      fetchPackageDetails();
    }
  }, [isOpen]);

  const fetchPackageDetails = async () => {
    setLoading(true);
    try {
      const response: IResponse<IClientAssignedPackageDetail> =
        await apiClient.get(
          API_END_POINTS.CLIENT_ASSIGNED_PACKAGE_DETAILS.replace(
            ':id',
            packageData.id,
          ),
        );
      setPackageDetails(response.data);
    } catch (error) {
      console.error('Error fetching package details:', error);
    } finally {
      setLoading(false);
    }
  };

  const currency = packageDetails?.paymentPayload?.currency ?? 'USD';
  const availableLicenses =
    (packageDetails?.licenseCount ?? 0) -
    (packageDetails?.usedLicenseCount ?? 0);
  const productName =
    packageDetails?.product?.productName ??
    packageData.product?.productName;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="content-max-h-[90vh] content-w-1/2 content-overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="content-flex content-items-center content-gap-2 content-text-2xl content-font-bold">
            <Package className="content-size-6 content-text-primary" />
            Package Details
          </DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="content-flex content-h-full content-items-center content-justify-center content-py-12">
            <Loader2 className="content-size-6 content-animate-spin content-text-primary" />
          </div>
        ) : (
          <div className="content-mt-4 content-space-y-6">
            {/* Header */}
            <div className="content-flex content-items-start content-justify-between content-gap-4">
              <div>
                <h3 className="content-text-2xl content-font-bold content-text-white">
                  {packageDetails?.packageDetails?.packageName}
                </h3>
                {productName && (
                  <p className="content-mt-1 content-text-sm content-text-cloudy-white">
                    {productName}
                  </p>
                )}
              </div>
              {packageDetails?.licenseStatus &&
                getLicenseStatusBadge(packageDetails.licenseStatus)}
            </div>

            {/* License overview */}
            <div className="content-grid content-grid-cols-2 content-gap-4 md:content-grid-cols-4">
              <div className="content-rounded content-border content-border-card-border content-p-4">
                <div className="content-mb-2 content-flex content-items-center content-gap-2 content-text-sm content-text-cloudy-white">
                  <Users className="content-size-4" />
                  Total Licenses
                </div>
                <p className="content-text-2xl content-font-bold content-text-white">
                  {packageDetails?.licenseCount ?? 0}
                </p>
              </div>
              <div className="content-rounded content-border content-border-card-border content-p-4">
                <div className="content-mb-2 content-flex content-items-center content-gap-2 content-text-sm content-text-cloudy-white">
                  <Users className="content-size-4" />
                  Assigned
                </div>
                <p className="content-text-2xl content-font-bold content-text-white">
                  {packageDetails?.usedLicenseCount ?? 0}
                </p>
              </div>
              <div className="content-rounded content-border content-border-card-border content-p-4">
                <div className="content-mb-2 content-flex content-items-center content-gap-2 content-text-sm content-text-cloudy-white">
                  <Users className="content-size-4" />
                  Available
                </div>
                <p className="content-text-2xl content-font-bold content-text-white">
                  {availableLicenses}
                </p>
              </div>
              <div className="content-rounded content-border content-border-card-border content-p-4">
                <div className="content-mb-2 content-flex content-items-center content-gap-2 content-text-sm content-text-cloudy-white">
                  <BookOpen className="content-size-4" />
                  Topics
                </div>
                <p className="content-text-2xl content-font-bold content-text-white">
                  {packageDetails?.topicCount ?? 0}
                </p>
              </div>
            </div>

            {/* Subscription period */}
            <div className="content-rounded content-border content-border-card-border content-p-6">
              <div className="content-mb-4 content-flex content-items-center content-gap-2">
                <Calendar className="content-size-5 content-text-primary" />
                <h4 className="content-text-lg content-font-bold content-text-white">
                  Subscription Period
                </h4>
              </div>
              <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-3">
                <div>
                  <p className="content-text-sm content-text-cloudy-white">
                    Assigned
                  </p>
                  <p className="content-font-medium content-text-white">
                    {packageDetails?.assignedAt
                      ? HumanizeDate(packageDetails.assignedAt)
                      : '-'}
                  </p>
                </div>
                <div>
                  <p className="content-text-sm content-text-cloudy-white">
                    Expires
                  </p>
                  <p className="content-font-medium content-text-white">
                    {packageDetails?.expiryDate
                      ? HumanizeDate(packageDetails.expiryDate)
                      : '-'}
                  </p>
                </div>
                <div>
                  <p className="content-text-sm content-text-cloudy-white">
                    Validity
                  </p>
                  <p className="content-font-medium content-text-white">
                    {packageDetails?.validityPeriod}{' '}
                    {humanizeText(packageDetails?.validityUnit)}
                  </p>
                </div>
              </div>
            </div>

            {/* Pricing */}
            <div className="content-rounded content-border content-border-card-border content-p-6">
              <div className="content-mb-4 content-flex content-items-center content-gap-2">
                <DollarSign className="content-size-5 content-text-primary" />
                <h4 className="content-text-lg content-font-bold content-text-white">
                  Pricing
                </h4>
              </div>
              <div className="content-space-y-3">
                <div className="content-flex content-items-center content-justify-between">
                  <span className="content-text-sm content-text-cloudy-white">
                    Price per license
                  </span>
                  <span className="content-font-semibold content-text-white">
                    {formatAmount(
                      packageDetails?.pricePerLicense ?? 0,
                      currency,
                    )}
                    <span className="content-ml-1 content-text-sm content-font-normal content-text-cloudy-white">
                      / {humanizeText(packageDetails?.validityUnit)}
                    </span>
                  </span>
                </div>
                <div className="content-flex content-items-center content-justify-between">
                  <span className="content-text-sm content-text-cloudy-white">
                    License subtotal
                  </span>
                  <div className="content-text-right">
                    <span className="content-font-semibold content-text-white">
                      {formatAmount(packageDetails?.totalPrice ?? 0, currency)}
                    </span>
                    <p className="content-text-xs content-text-cloudy-white">
                      {packageDetails?.licenseCount} ×{' '}
                      {formatAmount(
                        packageDetails?.pricePerLicense ?? 0,
                        currency,
                      )}
                    </p>
                  </div>
                </div>
              </div>
            </div>



            {/* Features */}
            {(packageDetails?.packageDetails?.features?.length ?? 0) > 0 && (
              <div>
                <div className="content-mb-4 content-flex content-items-center content-gap-2">
                  <Shield className="content-size-5 content-text-primary" />
                  <h4 className="content-text-lg content-font-bold content-text-white">
                    Included Features
                  </h4>
                  <span className="content-rounded-full content-bg-primary content-px-2.5 content-py-0.5 content-text-xs content-font-semibold content-text-white">
                    {packageDetails?.packageDetails?.features?.length}
                  </span>
                </div>

                <div className="content-md:content-grid-cols-2 content-grid content-grid-cols-1 content-gap-3">
                  {packageDetails?.packageDetails?.features?.map(feature => (
                    <div
                      key={feature.id}
                      className="content-flex content-items-start content-gap-3 content-rounded-lg content-border content-border-card-border content-p-3 content-transition-all hover:content-border-primary hover:content-shadow-sm"
                    >
                      <div className="content-mt-0.5 content-shrink-0">
                        <div className="content-rounded-full content-bg-primary content-p-1">
                          <Check className="content-size-4 content-text-white" />
                        </div>
                      </div>
                      <span className="content-text-sm content-font-medium content-leading-relaxed content-text-cloudy-white">
                        {feature.name}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default PackageDetailsModal;
