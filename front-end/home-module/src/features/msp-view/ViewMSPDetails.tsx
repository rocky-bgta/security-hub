import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import useAPI from 'hooks/UseAPI';
import { IMSPAdminDetails } from 'models/Client';
import React, { useCallback, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  mspId: string;
  open: boolean;
  onClose: () => void;
}

const ViewMSPDetails = ({ mspId, open, onClose }: IProps) => {
  const apiClient = useAPI();
  const [mspDetails, setMspDetails] = useState<IMSPAdminDetails>();
  const [loading, setLoading] = useState(false);

  const fetchMSPDetails = useCallback(async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.MSP_DETAILS.replace(':id', mspId),
      );
      setMspDetails(response.data);
    } catch (error) {
      console.error('Error fetching MSP details:', error);
    } finally {
      setLoading(false);
    }
  }, [apiClient, mspId]);

  useEffect(() => {
    if (open && mspId) {
      fetchMSPDetails();
    }
  }, [fetchMSPDetails, mspId, open]);

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

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="home-max-h-[90vh] home-w-[95vw] home-max-w-4xl home-overflow-y-auto">
        <DialogHeader>
          <DialogTitle>MSP Details</DialogTitle>
        </DialogHeader>

        {loading ? (
          <div className="home-flex home-items-center home-justify-center home-py-12">
            <div className="home-text-white">Loading...</div>
          </div>
        ) : (
          <div className="home-space-y-6 home-py-4">
            {/* Organization Info */}
            <div className="home-border-b home-border-card-border home-pb-4">
              <div className="home-mb-4 home-flex home-items-start home-justify-between">
                <div className="home-flex home-w-full home-flex-col home-items-start home-gap-3 sm:home-flex-row sm:home-items-center sm:home-gap-4">
                  {mspDetails?.organization?.logoUrl && (
                    <img
                      src={FILE_PATH_PREFIX + mspDetails?.organization?.logoUrl}
                      alt={mspDetails?.organization?.organizationName}
                      className="home-size-14 sm:home-size-16"
                    />
                  )}
                  <div>
                    <h3 className="home-break-all home-text-lg home-font-semibold home-text-foreground sm:home-text-xl">
                      {mspDetails?.organization?.organizationName}
                    </h3>
                    <span
                      className={`home-mt-1 home-inline-block home-rounded-full home-px-3 home-py-1 home-text-sm home-font-medium ${getStatusBadgeColor(
                        mspDetails?.status || '',
                      )}`}
                    >
                      {mspDetails?.status}
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
              <div className="home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-2">
                <div>
                  <p className="home-text-sm home-text-white">Email</p>
                  <p className="home-break-all home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.mspAdminEmail}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Contact Email</p>
                  <p className="home-break-all home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.contactEmail}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Phone Number</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.phoneNumber}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Domain</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.domain}
                  </p>
                </div>
              </div>
            </div>

            {/* Billing Information */}
            <div>
              <h4 className="home-mb-3 home-text-sm home-font-semibold home-uppercase home-tracking-wide home-text-foreground">
                Billing Information
              </h4>
              <div className="home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-2">
                <div>
                  <p className="home-text-sm home-text-white">Billing Name</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.billing?.billingName}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Billing Email</p>
                  <p className="home-break-all home-text-sm home-font-medium home-text-white">
                    {mspDetails?.billing?.billingEmail}
                  </p>
                </div>
                <div className="md:home-col-span-2">
                  <p className="home-text-sm home-text-white">
                    Billing Address
                  </p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {[
                      mspDetails?.billing?.streetAddress,
                      mspDetails?.billing?.streetAddressLine2,
                      mspDetails?.billing?.city,
                      mspDetails?.billing?.stateProvince,
                      mspDetails?.billing?.zipPostalCode,
                      mspDetails?.billing?.country,
                    ]
                      .filter(Boolean)
                      .join(', ')}
                  </p>
                </div>
              </div>
            </div>

            {/* Organization Details */}
            <div>
              <h4 className="home-mb-3 home-text-sm home-font-semibold home-uppercase home-tracking-wide home-text-foreground">
                Organization Details
              </h4>
              <div className="home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-2">
                <div>
                  <p className="home-text-sm home-text-white">
                    Organization Type
                  </p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.organizationType || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">
                    Organization Size
                  </p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.organizationSize}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Industry</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.industry}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Tier</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.tier?.name}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">MSP Type</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.mspType?.name || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Net Days</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.netDays?.name || 'N/A'}
                  </p>
                </div>
                <div className="md:home-col-span-2">
                  <p className="home-text-sm home-text-white">Address</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {[
                      mspDetails?.organization?.streetAddress,
                      mspDetails?.organization?.streetAddressLine2,
                      mspDetails?.organization?.city,
                      mspDetails?.organization?.stateProvince,
                      mspDetails?.organization?.zipPostalCode,
                      mspDetails?.organization?.country,
                    ]
                      .filter(Boolean)
                      .join(', ')}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Country</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.country}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">State/Province</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.stateProvince}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Time Zone</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.timeZone}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Language</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {mspDetails?.organization?.language}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Created At</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {formatDate(mspDetails?.createdAt || '')}
                  </p>
                </div>
                <div>
                  <p className="home-text-sm home-text-white">Updated At</p>
                  <p className="home-text-sm home-font-medium home-text-white">
                    {formatDate(mspDetails?.updatedAt || '')}
                  </p>
                </div>
              </div>
            </div>

            {/* Credit Information */}
            {mspDetails?.creditInfo && (
              <div>
                <h4 className="home-mb-3 home-text-sm home-font-semibold home-uppercase home-tracking-wide home-text-foreground">
                  Credit Information
                </h4>
                <div className="home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-2">
                  <div>
                    <p className="home-text-sm home-text-white">
                      Credit Enabled
                    </p>
                    <p className="home-text-sm home-font-medium home-text-white">
                      {mspDetails?.creditInfo?.enableCredit ? 'Yes' : 'No'}
                    </p>
                  </div>
                  <div>
                    <p className="home-text-sm home-text-white">
                      Credit Amount
                    </p>
                    <p className="home-text-sm home-font-medium home-text-white">
                      ${mspDetails?.creditInfo?.creditAmount || 0}
                    </p>
                  </div>
                  <div>
                    <p className="home-text-sm home-text-white">
                      Credit Reason
                    </p>
                    <p className="home-text-sm home-font-medium home-text-white">
                      {mspDetails?.creditInfo?.reason || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <p className="home-text-sm home-text-white">Net Days</p>
                    <p className="home-text-sm home-font-medium home-text-white">
                      {mspDetails?.creditInfo?.netDays?.name || 'N/A'}
                    </p>
                  </div>
                  <div>
                    <p className="home-text-sm home-text-white">
                      Auto Suspend on Overdue
                    </p>
                    <p className="home-text-sm home-font-medium home-text-white">
                      {mspDetails?.creditInfo?.autoSuspendOnOverdue
                        ? 'Yes'
                        : 'No'}
                    </p>
                  </div>
                  <div>
                    <p className="home-text-sm home-text-white">
                      Credit Start Date
                    </p>
                    <p className="home-text-sm home-font-medium home-text-white">
                      {mspDetails?.creditInfo?.creditStartDate
                        ? formatDate(mspDetails.creditInfo.creditStartDate)
                        : 'N/A'}
                    </p>
                  </div>
                  <div>
                    <p className="home-text-sm home-text-white">
                      Credit End Date
                    </p>
                    <p className="home-text-sm home-font-medium home-text-white">
                      {mspDetails?.creditInfo?.creditEndDate
                        ? formatDate(mspDetails.creditInfo.creditEndDate)
                        : 'N/A'}
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Notes */}
            {mspDetails?.notes && (
              <div>
                <h4 className="home-mb-3 home-text-sm home-font-semibold home-uppercase home-tracking-wide home-text-foreground">
                  Notes
                </h4>
                <p className="home-text-sm home-font-medium home-text-white">
                  {mspDetails.notes}
                </p>
              </div>
            )}

            {/* Products & Licenses */}
            {mspDetails?.products && mspDetails?.products?.length > 0 && (
              <div>
                <h4 className="home-mb-3 home-text-sm home-font-semibold home-uppercase home-tracking-wide home-text-foreground">
                  Products & Licenses
                </h4>
                <div className="home-space-y-4">
                  {mspDetails?.products?.map(product => (
                    <div
                      key={product?.id}
                      className="home-space-y-4 home-rounded home-border home-border-card-border home-p-4"
                    >
                      <div className="home-mb-3 home-flex home-flex-col home-gap-3 sm:home-flex-row sm:home-items-start sm:home-justify-between">
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

                      <div className="home-mb-3 home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-3">
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

                      <div className="home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-3">
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
                      {mspDetails?.products?.length}
                    </span>
                  </div>
                  <div className="home-mt-2 home-flex home-items-center home-justify-between">
                    <span className="home-text-sm home-font-semibold home-text-foreground">
                      Grand Total
                    </span>
                    <span className="home-text-lg home-font-bold home-text-primary">
                      $
                      {mspDetails?.products?.reduce(
                        (sum, product) => sum + (product?.totalPrice || 0),
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
  );
};

export default ViewMSPDetails;
