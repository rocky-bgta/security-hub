import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogHeader,
} from 'common/Dialog';
import { useAPI } from 'hooks/UseAPI';
import { IClientAdmin } from 'pages/client-admin/List';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  clientId: string;
  open: boolean;
  onClose: () => void;
}

const ViewClientDetails = ({ clientId, open, onClose }: IProps) => {
  const apiClient = useAPI();
  const [clientDetails, setClientDetails] = useState<IClientAdmin>();
  const [loading, setLoading] = useState(false);

  const fetchClientDetails = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_DETAILS.replace(':id', clientId),
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
        return 'bg-yellow-100 text-yellow-800';
      case 'ENABLED':
        return 'bg-green-100 text-green-800';
      case 'DISABLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading) {
    return (
      <Dialog open={open} onOpenChange={onClose}>
        <DialogContent className="max-h-[90vh] max-w-4xl overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Client Details</DialogTitle>
          </DialogHeader>
          <div className="flex items-center justify-center py-12">
            <div className="text-white">Loading...</div>
          </div>
        </DialogContent>
      </Dialog>
    );
  }

  return (
    <Fragment>
      <Dialog open={open} onOpenChange={onClose}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Client Details</DialogTitle>
          </DialogHeader>

          {clientDetails && (
            <div className="space-y-6 py-4">
              {/* Organization Info */}
              <div className="border-b border-card-border pb-4">
                <div className="mb-4 flex items-start justify-between">
                  <div className="flex items-center gap-4">
                    {clientDetails.logoUrl && (
                      <img
                        src={FILE_PATH_PREFIX + clientDetails.logoUrl}
                        alt={clientDetails.organizationName}
                        className="size-16"
                      />
                    )}
                    <div>
                      <h3 className="text-xl font-semibold text-foreground">
                        {clientDetails.organizationName}
                      </h3>
                      <span
                        className={`mt-1 inline-block rounded-full px-3 py-1 text-sm font-medium ${getStatusBadgeColor(
                          clientDetails.status,
                        )}`}
                      >
                        {clientDetails.status}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Contact Information */}
              <div>
                <h4 className="mb-3 text-sm font-semibold uppercase tracking-wide text-foreground">
                  Contact Information
                </h4>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-white">Email</p>
                    <p className="text-sm font-medium text-white">
                      {clientDetails.email}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-white">Contact Email</p>
                    <p className="text-sm font-medium text-white">
                      {clientDetails.contactEmail}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-white">Phone Number</p>
                    <p className="text-sm font-medium text-white">
                      {clientDetails.phoneNumber}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-white">Domain</p>
                    <p className="text-sm font-medium text-white">
                      {clientDetails.domain}
                    </p>
                  </div>
                </div>
              </div>

              {/* Billing Information */}
              <div>
                <h4 className="mb-3 text-sm font-semibold uppercase tracking-wide text-foreground">
                  Billing Information
                </h4>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-white">Billing Name</p>
                    <p className="text-sm font-medium text-white">
                      {clientDetails.billingName}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-white">Billing Email</p>
                    <p className="text-sm font-medium text-white">
                      {clientDetails.billingEmail}
                    </p>
                  </div>
                  <div className="col-span-2">
                    <p className="text-sm text-white">Billing Address</p>
                    <p className="text-sm font-medium text-white">
                      {clientDetails.billingAddress}
                    </p>
                  </div>
                </div>
              </div>

              {/* Organization Details */}
              <div>
                <h4 className="mb-3 text-sm font-semibold uppercase tracking-wide text-foreground">
                  Organization Details
                </h4>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-white">Address</p>
                    <p className="text-sm font-medium text-white">
                      {clientDetails.address}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-white">Created At</p>
                    <p className="text-sm font-medium text-white">
                      {formatDate(clientDetails.createdAt)}
                    </p>
                  </div>
                </div>
              </div>

              {/* Products & Licenses */}
              {clientDetails?.clientProducts &&
                clientDetails?.clientProducts?.length > 0 && (
                  <div>
                    <h4 className="mb-3 text-sm font-semibold uppercase tracking-wide text-foreground">
                      Products & Licenses
                    </h4>
                    <div className="space-y-4">
                      {clientDetails?.clientProducts?.map((product, index) => (
                        <div
                          key={product?.id}
                          className="space-y-4 rounded border border-card-border p-4"
                        >
                          <div className="mb-3 flex items-start justify-between">
                            <div>
                              <h5 className="mb-1 font-semibold text-white">
                                {product?.product?.productName}
                              </h5>
                              <p className="text-sm text-muted-foreground">
                                {product?.product?.productDescription}
                              </p>
                            </div>
                            <span
                              className={`inline-block rounded-full px-2 py-1 text-xs font-medium ${getStatusBadgeColor(
                                product?.licenseStatus,
                              )}`}
                            >
                              {product?.licenseStatus}
                            </span>
                          </div>

                          <div className="mb-3 grid grid-cols-3 gap-4">
                            <div>
                              <p className="text-xs text-white">Package</p>
                              <p className="text-sm font-medium text-white">
                                {product?.packageDetails?.packageName}
                              </p>
                            </div>
                            <div>
                              <p className="text-xs text-white">
                                Price per License
                              </p>
                              <p className="text-sm font-medium text-white">
                                ${product?.pricePerLicense}
                              </p>
                            </div>
                            <div>
                              <p className="text-xs text-white">Total Price</p>
                              <p className="text-sm font-medium text-white">
                                ${product?.totalPrice}
                              </p>
                            </div>
                          </div>

                          <div className="grid grid-cols-3 gap-4">
                            <div>
                              <p className="text-xs text-white">
                                License Count
                              </p>
                              <p className="text-sm font-medium text-white">
                                {product?.licenseCount}
                              </p>
                            </div>
                            <div>
                              <p className="text-xs text-white">
                                Used Licenses
                              </p>
                              <p className="text-sm font-medium text-white">
                                {product?.usedLicenseCount}
                              </p>
                            </div>
                            <div>
                              <p className="text-xs text-white">
                                Validity Period
                              </p>
                              <p className="text-sm font-medium text-white">
                                {product?.validityPeriod}{' '}
                                {product?.validityUnit}
                              </p>
                            </div>
                          </div>

                          <div className="mt-3 border-t border-card-border pt-3">
                            <p className="text-xs text-white">Assigned At</p>
                            <p className="text-sm font-medium text-white">
                              {formatDate(product?.assignedAt)}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* Total Summary */}
                    <div className="mt-4 rounded-lg border border-card-border p-4">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-semibold text-foreground">
                          Total Products
                        </span>
                        <span className="text-lg font-bold text-white">
                          {clientDetails?.clientProducts?.length}
                        </span>
                      </div>
                      <div className="mt-2 flex items-center justify-between">
                        <span className="text-sm font-semibold text-foreground">
                          Grand Total
                        </span>
                        <span className="text-lg font-bold text-primary">
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
          )}
        </DialogContent>
      </Dialog>
    </Fragment>
  );
};

export default ViewClientDetails;
