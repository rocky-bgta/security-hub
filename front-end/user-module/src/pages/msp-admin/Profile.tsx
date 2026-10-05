import {
  Building2,
  Globe,
  Mail,
  MapPin,
  Package,
  Phone,
  TrendingUp,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Avatar, AvatarFallback, AvatarImage } from 'common/Avatar';
import { Badge } from 'common/Badge';
import IconBackButton from 'components/IconBackButton';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Progress } from 'common/Progress';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IMSPProfile } from 'models/MSP';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { isSuccessResponse } from 'utils/Helper';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  hostPath: typeof routes;
}

const MSPProfile = ({ hostPath }: IProps) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const mspId = searchParams.get('id');
  const [loading, setLoading] = useState<boolean>(true);
  const [mspProfile, setMspProfile] = useState<IMSPProfile | null>(null);
  const apiClient = useAPI();

  useEffect(() => {
    if (mspId) {
      fetchMSPProfile();
    } else {
      toast.error('MSP ID is required');
      navigate(hostPath.mspList.path);
    }
  }, [mspId]);

  const fetchMSPProfile = async () => {
    if (!mspId) return;

    try {
      setLoading(true);
      const response: IResponse<IMSPProfile> = await apiClient.get(
        API_END_POINTS.MSP_VIEW.replace(':id', mspId),
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to fetch MSP profile');
      }

      setMspProfile(response.data);
    } catch (error: any) {
      console.error('Error fetching MSP profile:', error);
      toast.error(error.message || 'Failed to load MSP profile');
      navigate(hostPath.mspList.path);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <p className="text-muted-foreground">Loading MSP profile...</p>
      </div>
    );
  }

  if (!mspProfile) {
    return (
      <div className="flex h-screen items-center justify-center">
        <p className="text-muted-foreground">MSP profile not found</p>
      </div>
    );
  }

  const formatDate = (dateString: string) => {
    if (!dateString) return 'N/A';
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      });
    } catch {
      return dateString;
    }
  };

  const formatAddress = () => {
    const org = mspProfile.organization;
    const parts = [
      org.streetAddress,
      org.streetAddressLine2,
      org.city,
      org.stateProvince,
      org.zipPostalCode,
      org.country,
    ].filter(Boolean);
    return parts.join(', ') || 'N/A';
  };

  return (
    <div className="space-y-6">
      <div className="space-y-3">
        <IconBackButton
          onClick={() => navigate(hostPath.mspList.path)}
          label="Back to MSP List"
        />
        <div>
          <h1 className="text-3xl font-bold text-foreground">MSP Profile</h1>
          <p className="text-muted-foreground">
            Detailed view of MSP partner information and performance
          </p>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <div className="space-y-6 lg:col-span-2">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-start gap-6">
                <Avatar className="size-20">
                  <AvatarImage
                    src={FILE_PATH_PREFIX + mspProfile.organization.logoUrl}
                  />
                  <AvatarFallback className="bg-primary text-2xl text-primary-foreground">
                    {mspProfile.organization.organizationName
                      .split(' ')
                      .map(n => n[0])
                      .join('')
                      .toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <div className="flex-1">
                  <div className="mb-2 flex items-center gap-3">
                    <h2 className="text-2xl font-bold text-foreground">
                      {mspProfile.organization.organizationName}
                    </h2>
                    <Badge
                      className={
                        mspProfile.status === 'ACTIVE'
                          ? 'bg-green-500/20 text-green-400'
                          : mspProfile.status === 'SUSPENDED'
                            ? 'bg-orange-500/20 text-orange-400'
                            : 'bg-red-500/20 text-red-400'
                      }
                    >
                      {mspProfile.status}
                    </Badge>
                  </div>
                  <p className="mb-2 text-lg text-muted-foreground">
                    {mspProfile.organization.mspAdminEmail}
                  </p>
                  <p className="mb-4 text-sm text-muted-foreground">
                    {mspProfile.notes || 'No description available'}
                  </p>
                  <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
                    <div className="text-center">
                      <p className="text-2xl font-bold text-primary">
                        {mspProfile.assignedClients?.length || 0}
                      </p>
                      <p className="text-xs text-muted-foreground">Clients</p>
                    </div>
                    <div className="text-center">
                      <p className="text-2xl font-bold text-primary">
                        {mspProfile.licenseAllocation?.usagePercentage || 0}%
                      </p>
                      <p className="text-xs text-muted-foreground">
                        License Usage
                      </p>
                    </div>
                    <div className="text-center">
                      <p className="text-2xl font-bold text-primary">
                        {mspProfile.licenseAllocation?.usedLicenses || 0}/
                        {mspProfile.licenseAllocation?.totalLicenses || 0}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Licenses Used
                      </p>
                    </div>
                    <div className="text-center">
                      <p className="text-2xl font-bold text-primary">
                        {mspProfile.organization.tier?.name || 'N/A'}
                      </p>
                      <p className="text-xs text-muted-foreground">Tier</p>
                    </div>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-foreground">
                <Package className="size-5" />
                License Allocation
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div>
                  <div className="mb-2 flex justify-between">
                    <span className="text-sm font-medium text-foreground">
                      License Usage
                    </span>
                    <span className="text-sm text-foreground">
                      {mspProfile.licenseAllocation?.usagePercentage || 0}%
                    </span>
                  </div>
                  <Progress
                    value={mspProfile.licenseAllocation?.usagePercentage || 0}
                    className="h-2"
                  />
                </div>
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <p className="text-xs text-muted-foreground">Total</p>
                    <p className="text-lg font-semibold text-foreground">
                      {mspProfile.licenseAllocation?.totalLicenses || 0}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">Used</p>
                    <p className="text-lg font-semibold text-foreground">
                      {mspProfile.licenseAllocation?.usedLicenses || 0}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-muted-foreground">Available</p>
                    <p className="text-lg font-semibold text-foreground">
                      {mspProfile.licenseAllocation?.availableLicenses || 0}
                    </p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-foreground">
                <Building2 className="size-5" />
                Assigned Clients
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="max-h-[60vh] space-y-4 overflow-y-auto">
                {mspProfile.assignedClients &&
                mspProfile.assignedClients.length > 0 ? (
                  mspProfile.assignedClients.map((client, index) => (
                    <div
                      key={client.clientId}
                      className="flex items-center justify-between rounded-lg border border-card-border p-3"
                    >
                      <div className="flex items-center gap-3">
                        <div className="flex size-8 items-center justify-center rounded-full bg-primary/20 text-sm font-bold text-primary">
                          {index + 1}
                        </div>
                        <div>
                          <p className="font-medium text-foreground">
                            {client.clientName}
                          </p>
                          <p className="text-sm text-muted-foreground">
                            {client.contactEmail}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <Badge
                          className={
                            client.status === 'ACTIVE'
                              ? 'bg-green-500/20 text-green-400'
                              : 'bg-orange-500/20 text-orange-400'
                          }
                        >
                          {client.status}
                        </Badge>
                        <p className="mt-1 text-xs text-muted-foreground">
                          {client.licenseCount} licenses
                        </p>
                      </div>
                    </div>
                  ))
                ) : (
                  <p className="text-center text-muted-foreground">
                    No clients assigned
                  </p>
                )}
              </div>
            </CardContent>
          </Card>

          {mspProfile.creditInfo && mspProfile.creditInfo.enableCredit && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-foreground">
                  <TrendingUp className="size-5" />
                  Credit Information
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <p className="text-xs text-muted-foreground">
                        Credit Amount
                      </p>
                      <p className="text-lg font-semibold text-foreground">
                        ${mspProfile.creditInfo.creditAmount.toLocaleString()}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Reason</p>
                      <p className="text-sm font-medium text-foreground">
                        {mspProfile.creditInfo.reason}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">Net Days</p>
                      <p className="text-sm font-medium text-foreground">
                        {mspProfile.creditInfo.netDays?.name || 'N/A'}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">
                        Auto Suspend
                      </p>
                      <p className="text-sm font-medium text-foreground">
                        {mspProfile.creditInfo.autoSuspendOnOverdue
                          ? 'Yes'
                          : 'No'}
                      </p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-foreground">
                Contact Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center gap-3">
                <Mail className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.organization.contactEmail}
                  </p>
                  <p className="text-xs text-muted-foreground">Contact Email</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Mail className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.organization.mspAdminEmail}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    MSP Admin Email
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Phone className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-foreground">
                    + {mspProfile.organization.phoneNumber || 'N/A'}
                  </p>
                  <p className="text-xs text-muted-foreground">Phone Number</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <MapPin className="mt-1 size-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {formatAddress()}
                  </p>
                  <p className="text-xs text-muted-foreground">
                    Business Address
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <Globe className="size-4 text-muted-foreground" />
                <div>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.organization.domain || 'N/A'}
                  </p>
                  <p className="text-xs text-muted-foreground">Domain</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-foreground">
                Business Details
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-xs text-muted-foreground">Industry</p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.organization.industry?.toString() || 'N/A'}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">
                  Organization Type
                </p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.organization.organizationType?.toString() || 'N/A'}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">
                  Organization Size
                </p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.organization.organizationSize?.toString() || 'N/A'}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Country</p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.organization.country?.toString() || 'N/A'}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Time Zone</p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.organization.timeZone?.toString() || 'N/A'}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Language</p>
                <p className="text-sm font-medium text-foreground">
                  {mspProfile.organization.language?.toString() || 'N/A'}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Created At</p>
                <p className="text-sm font-medium text-foreground">
                  {formatDate(mspProfile.createdAt)}
                </p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Updated At</p>
                <p className="text-sm font-medium text-foreground">
                  {formatDate(mspProfile.updatedAt)}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-foreground">
                Tier Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div>
                  <p className="text-xs text-muted-foreground">Tier</p>
                  <Badge className="bg-primary/20 text-primary">
                    {mspProfile.organization.tier?.name || 'N/A'}
                  </Badge>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">MSP Type</p>
                  <Badge variant="outline">
                    {mspProfile.organization.mspType?.name || 'N/A'}
                  </Badge>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Net Days</p>
                  <Badge variant="outline">
                    {mspProfile.organization.netDays?.name || 'N/A'}
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>

          {mspProfile.billingEmail && (
            <Card>
              <CardHeader>
                <CardTitle className="text-foreground">
                  Billing Information
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <p className="text-xs text-muted-foreground">Billing Name</p>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.billingName || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">Billing Email</p>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.billingEmail || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground">
                    Billing Address
                  </p>
                  <p className="text-sm font-medium text-foreground">
                    {mspProfile.billingAddress ||
                      mspProfile.billingStreetAddress ||
                      'N/A'}
                  </p>
                </div>
              </CardContent>
            </Card>
          )}

          {mspProfile.notes && (
            <Card>
              <CardHeader>
                <CardTitle className="text-foreground">Notes</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-foreground">{mspProfile.notes}</p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
};

export default MSPProfile;
