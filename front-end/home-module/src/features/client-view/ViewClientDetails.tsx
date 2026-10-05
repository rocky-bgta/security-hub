import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import useAPI from 'hooks/UseAPI';
import { IClientAdminDetails } from 'models/Client';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  clientId: string;
  open: boolean;
  onClose: () => void;
}

const ViewClientDetails = ({ clientId, open, onClose }: IProps) => {
  const apiClient = useAPI();
  const [clientDetails, setClientDetails] = useState<IClientAdminDetails>();
  const [loading, setLoading] = useState(false);

  const fetchClientDetails = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_DETAILS.replace(':id', clientId),
      );
      setClientDetails(response.data);
    } catch (error) {
      console.error('Error fetching client details:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open && clientId) {
      fetchClientDetails();
    }
  }, [clientId, open]);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const getStatusBadgeColor = (status: string) => {
    switch (status?.toUpperCase()) {
      case 'PENDING':
        return 'home-bg-yellow-100 home-text-yellow-800';
      case 'ENABLED':
        return 'home-bg-green-100 home-text-green-800';
      case 'DISABLED':
        return 'home-bg-red-100 home-text-red-800';
      default:
        return 'home-bg-gray-100 home-text-gray-800';
    }
  };

  if (loading) {
    return (
      <Dialog open={open} onOpenChange={onClose}>
        <DialogContent className="home-max-h-[90vh] home-max-w-4xl home-overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Client Details</DialogTitle>
          </DialogHeader>
          <div className="home-flex home-items-center home-justify-center home-py-12">
            <div className="home-text-white">Loading...</div>
          </div>
        </DialogContent>
      </Dialog>
    );
  }

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="home-max-h-[90vh] home-overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Client Details</DialogTitle>
          <DialogDescription></DialogDescription>
        </DialogHeader>

        <div className="home-space-y-6 home-py-4">
          {/* Organization Info */}
          <div className="home-border-b home-border-card-border home-pb-4">
            <div className="home-mb-4 home-flex home-items-start home-justify-between">
              <div className="home-flex home-items-center home-gap-4">
                {clientDetails?.logoUrl && (
                  <img
                    src={FILE_PATH_PREFIX + clientDetails?.logoUrl}
                    alt={clientDetails?.organizationName}
                    className="home-size-16"
                  />
                )}
                <div>
                  <h3 className="home-text-xl home-font-semibold home-text-foreground">
                    {clientDetails?.organizationName}
                  </h3>
                  <span
                    className={`home-mt-1 home-inline-block home-rounded-full home-px-3 home-py-1 home-text-sm home-font-medium ${getStatusBadgeColor(
                      clientDetails?.status || '',
                    )}`}
                  >
                    {clientDetails?.status}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Contact Information */}
          <div>
            <h4 className="home-mb-3 home-text-sm home-font-semibold home-uppercase home-tracking-wide home-text-foreground">
              Contact Information
            </h4>
            <div className="home-grid home-grid-cols-2 home-gap-4">
              <div>
                <p className="home-text-sm home-text-white">Email</p>
                <p className="home-text-sm home-font-medium home-text-white">
                  {clientDetails?.email}
                </p>
              </div>
              <div>
                <p className="home-text-sm home-text-white">Contact Email</p>
                <p className="home-text-sm home-font-medium home-text-white">
                  {clientDetails?.contactEmail}
                </p>
              </div>
              <div>
                <p className="home-text-sm home-text-white">Phone Number</p>
                <p className="home-text-sm home-font-medium home-text-white">
                  {clientDetails?.phoneNumber}
                </p>
              </div>
              <div>
                <p className="home-text-sm home-text-white">Domain</p>
                <p className="home-text-sm home-font-medium home-text-white">
                  {clientDetails?.domain}
                </p>
              </div>
            </div>
          </div>

          {/* Billing Information */}
          <div>
            <h4 className="home-mb-3 home-text-sm home-font-semibold home-uppercase home-tracking-wide home-text-foreground">
              Billing Information
            </h4>
            <div className="home-grid home-grid-cols-2 home-gap-4">
              <div>
                <p className="home-text-sm home-text-white">Billing Name</p>
                <p className="home-text-sm home-font-medium home-text-white">
                  {clientDetails?.billingName}
                </p>
              </div>
              <div>
                <p className="home-text-sm home-text-white">Billing Email</p>
                <p className="home-text-sm home-font-medium home-text-white">
                  {clientDetails?.billingEmail}
                </p>
              </div>
              <div className="home-col-span-2">
                <p className="home-text-sm home-text-white">Billing Address</p>
                <p className="home-text-sm home-font-medium home-text-white">
                  {clientDetails?.billingAddress}
                </p>
              </div>
            </div>
          </div>

          {/* Organization Details */}
          <div>
            <h4 className="home-mb-3 home-text-sm home-font-semibold home-uppercase home-tracking-wide home-text-foreground">
              Organization Details
            </h4>
            <div className="home-grid home-grid-cols-2 home-gap-4">
              <div>
                <p className="home-text-sm home-text-white">Address</p>
                <p className="home-text-sm home-font-medium home-text-white">
                  {clientDetails?.address}
                </p>
              </div>
              <div>
                <p className="home-text-sm home-text-white">Created At</p>
                <p className="home-text-sm home-font-medium home-text-white">
                  {formatDate(clientDetails?.createdAt || '')}
                </p>
              </div>
            </div>
          </div>

          {/* Products & Licenses */}
          {clientDetails?.clientProducts &&
            clientDetails?.clientProducts?.length > 0 && (
              <div>
                <h4 className="home-mb-3 home-text-sm home-font-semibold home-uppercase home-tracking-wide home-text-foreground">
                  Products & Licenses
                </h4>
                <div className="home-space-y-4">
                  {clientDetails?.clientProducts?.map((product, index) => (
                    <div
                      key={product?.id}
                      className="home-space-y-4 home-rounded home-border home-border-card-border home-p-4"
                    >
                      <div className="home-mb-3 home-flex home-items-start home-justify-between">
                        <div>
                          <h5 className="home-mb-1 home-font-semibold home-text-white">
                            {product?.product?.productName}
                          </h5>
                          <p className="home-text-sm home-text-muted-foreground">
                            {product?.product?.productDescription}
                          </p>
                        </div>
                        <span
                          className={`home-inline-block home-rounded-full home-px-2 home-py-1 home-text-xs home-font-medium ${getStatusBadgeColor(
                            product?.licenseStatus,
                          )}`}
                        >
                          {product?.licenseStatus}
                        </span>
                      </div>

                      <div className="home-mb-3 home-grid home-grid-cols-3 home-gap-4">
                        <div>
                          <p className="home-text-xs home-text-white">
                            Package
                          </p>
                          <p className="home-text-sm home-font-medium home-text-white">
                            {product?.packageDetails?.packageName}
                          </p>
                        </div>
                        <div>
                          <p className="home-text-xs home-text-white">
                            Price per License
                          </p>
                          <p className="home-text-sm home-font-medium home-text-white">
                            ${product?.pricePerLicense}
                          </p>
                        </div>
                        <div>
                          <p className="home-text-xs home-text-white">
                            Total Price
                          </p>
                          <p className="home-text-sm home-font-medium home-text-white">
                            ${product?.totalPrice}
                          </p>
                        </div>
                      </div>

                      <div className="home-grid home-grid-cols-3 home-gap-4">
                        <div>
                          <p className="home-text-xs home-text-white">
                            License Count
                          </p>
                          <p className="home-text-sm home-font-medium home-text-white">
                            {product?.licenseCount}
                          </p>
                        </div>
                        <div>
                          <p className="home-text-xs home-text-white">
                            Used Licenses
                          </p>
                          <p className="home-text-sm home-font-medium home-text-white">
                            {product?.usedLicenseCount}
                          </p>
                        </div>
                        <div>
                          <p className="home-text-xs home-text-white">
                            Validity Period
                          </p>
                          <p className="home-text-sm home-font-medium home-text-white">
                            {product?.validityPeriod} {product?.validityUnit}
                          </p>
                        </div>
                      </div>

                      <div className="home-mt-3 home-border-t home-border-card-border home-pt-3">
                        <p className="home-text-xs home-text-white">
                          Assigned At
                        </p>
                        <p className="home-text-sm home-font-medium home-text-white">
                          {formatDate(product?.assignedAt)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Total Summary */}
                <div className="home-mt-4 home-rounded-lg home-border home-border-card-border home-p-4">
                  <div className="home-flex home-items-center home-justify-between">
                    <span className="home-text-sm home-font-semibold home-text-foreground">
                      Total Products
                    </span>
                    <span className="home-text-lg home-font-bold home-text-white">
                      {clientDetails?.clientProducts?.length}
                    </span>
                  </div>
                  <div className="home-mt-2 home-flex home-items-center home-justify-between">
                    <span className="home-text-sm home-font-semibold home-text-foreground">
                      Grand Total
                    </span>
                    <span className="home-text-lg home-font-bold home-text-primary">
                      $
                      {clientDetails?.clientProducts?.reduce(
                        (sum, p) => sum + p?.totalPrice,
                        0,
                      )}
                    </span>
                  </div>
                </div>
              </div>
            )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ViewClientDetails;
