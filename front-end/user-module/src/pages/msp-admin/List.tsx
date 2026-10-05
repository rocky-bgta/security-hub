import {
  AlertTriangle,
  Building2,
  Calendar,
  Edit,
  Eye,
  Key,
  MoreHorizontal,
  Package,
  Search,
  Users,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Avatar, AvatarFallback } from 'common/Avatar';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent } from 'common/Card';
import { Dialog, DialogContent, DialogTitle } from 'common/Dialog';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/Dropdown';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import PasswordResetDialog from 'features/common/PasswordResetDialog';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IGetListParams, IList, Status } from 'models/Global';
import { IMSPList } from 'models/MSP';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import {
  formatDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

interface IProps {
  hostPath?: typeof routes;
}

const MSPList = ({ hostPath = routes }: IProps) => {
  const navigate = useNavigate();
  const [mspList, setMSPList] = useState<IList<IMSPList>>({
    offset: 0,
    pageSize: 0,
    total: 0,
    items: [],
  });
  const [showAlert, setShowAlert] = useState<boolean>(false);
  const [selectedMSP, setSelectedMSP] = useState<IMSPList | null>(null);
  const [resetPasswordDialogOpen, setResetPasswordDialogOpen] =
    useState<boolean>(false);
  const [selectedMSPForPasswordReset, setSelectedMSPForPasswordReset] =
    useState<IMSPList | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    status: Status.ALL,
  });
  const [queryString, setQueryString] = useState<string>('');

  const apiClient = useAPI();
  const searchDebounce = useDebounce(queryString, 500);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchMSPList();
    }
  }, [searchDebounce]);

  const fetchMSPList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.MSP_LIST + queryString,
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      setMSPList(response.data);
    } catch (error) {
      console.error('Error fetching MSPList data:', error);
      toast.error(error instanceof Error ? error.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  // const onPageChangeHandler = (page: number) => {
  //   setQueryParams(prevState => ({
  //     ...prevState,
  //     offset: page - 1,
  //   }));
  // };

  const handleSuspendClick = async (msp: IMSPList) => {
    if (msp.status === Status.ACTIVE) {
      navigate(hostPath.mspSuspend.path + '?id=' + msp.id);
    } else if (msp.status === Status.SUSPEND) {
      try {
        const response = await apiClient.put(
          API_END_POINTS.SUSPEND_MSP.replace(':id', msp.id),
          {
            data: {
              status: Status.ACTIVE,
            },
          },
        );
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }
        toast.success('MSP activated successfully');
        fetchMSPList();
      } catch (error) {
        toast.error(error instanceof Error ? error.message : 'An error occurred');
        console.error('Error suspending MSP:', error);
      }
    }
  };

  const handleStatusChange = async () => {
    if (!selectedMSP) return;

    try {
      const response = await apiClient.put(
        API_END_POINTS.CHANGE_STATUS_MSP_ADMIN.replace(':id', selectedMSP.id),
        {
          data: {
            status:
              selectedMSP.status === Status.INACTIVE
                ? Status.ACTIVE
                : Status.INACTIVE,
          },
        },
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      toast.success('MSP status changed successfully');
      fetchMSPList();
      setShowAlert(false);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'An error occurred');
      console.error('Error changing MSP status:', error);
    }
  };

  return (
    <>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2>MSP Management</h2>
            <p className="text-muted-foreground">
              Manage and monitor all MSP partners
            </p>
          </div>
          <Button
            className="bg-primary text-primary-foreground"
            onClick={() => navigate(hostPath.mspOnboarding.path)}
          >
            <Building2 className="mr-2 size-4" />
            Add New MSP
          </Button>
        </div>

        <div className="flex items-center gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search MSPs..."
              className="w-full pl-9 text-foreground"
              value={queryParams.search}
              onChange={e =>
                setQueryParams({ ...queryParams, search: e.target.value })
              }
            />
          </div>
          <Select
            value={queryParams.status}
            onValueChange={value =>
              setQueryParams(prev => ({
                ...prev,
                status: (value === 'ALL' ? '' : value) as Status,
              }))
            }
          >
            <SelectTrigger className="w-48">
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent className="border-card-border bg-secondary">
              <SelectItem value="ALL">All Status</SelectItem>
              <SelectItem value={Status.ACTIVE}>Active</SelectItem>
              <SelectItem value={Status.PENDING}>Pending</SelectItem>
              <SelectItem value={Status.INACTIVE}>Inactive</SelectItem>
            </SelectContent>
          </Select>
          <Button
            variant="outline"
            className="w-32"
            onClick={() =>
              setQueryParams({
                ...InitGetListParams,
                search: '',
                status: Status.ALL,
              })
            }
          >
            Reset
          </Button>
        </div>

        <div className="space-y-4">
          {loading ? (
            <p className="text-center text-muted-foreground">Loading...</p>
          ) : mspList.items.length === 0 ? (
            <p className="text-center text-muted-foreground">No MSPs found.</p>
          ) : (
            mspList.items.map(msp => (
              <Card key={msp.id} className="pt-4">
                <CardContent className="space-y-4">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-4">
                      <Avatar className="size-12">
                        <AvatarFallback className="bg-primary text-primary-foreground">
                          {msp.organizationName
                            .split(' ')
                            .map(n => n[0])
                            .join('')}
                        </AvatarFallback>
                      </Avatar>
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <h3 className="text-lg font-semibold text-foreground">
                            {msp.organizationName}
                          </h3>
                          <Badge
                            variant={
                              msp.status === Status.ACTIVE
                                ? 'default'
                                : msp.status === Status.PENDING
                                  ? 'secondary'
                                  : 'destructive'
                            }
                            className={
                              msp.status === Status.ACTIVE
                                ? 'bg-green-500/20 text-green-400'
                                : msp.status === Status.PENDING
                                  ? 'bg-orange-500/20 text-orange-400'
                                  : 'bg-red-500/20 text-red-400'
                            }
                          >
                            {msp.status}
                          </Badge>
                        </div>
                        <p className="text-sm text-muted-foreground">
                          ID: {msp.mspId}
                        </p>
                        <p className="text-sm text-foreground">
                          {msp.mspAdminEmail}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          {msp.contactEmail} • {msp.phoneNumber}
                        </p>
                      </div>
                    </div>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreHorizontal className="size-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent
                        align="end"
                        className="border-card-border bg-secondary"
                      >
                        <DropdownMenuItem
                          className="cursor-pointer text-secondary-foreground"
                          onClick={() =>
                            navigate(
                              hostPath.mspEdit.path.replace(':id', msp.id),
                            )
                          }
                        >
                          <Edit className="mr-2 size-4" />
                          Edit MSP
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          className="cursor-pointer text-secondary-foreground"
                          onClick={() =>
                            navigate(hostPath.mspProfile.path + `?id=${msp.id}`)
                          }
                        >
                          <Eye className="mr-2 size-4" />
                          View Profile
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          className="cursor-pointer text-secondary-foreground"
                          onClick={() => navigate(hostPath.mspLicense.path)}
                        >
                          <Key className="mr-2 size-4" />
                          Manage Licenses
                        </DropdownMenuItem>

                        <DropdownMenuItem
                          className="cursor-pointer text-red-400"
                          onClick={_ => handleSuspendClick(msp)}
                        >
                          <AlertTriangle className="mr-2 size-4" />
                          {msp.status === Status.SUSPEND
                            ? 'Activate MSP'
                            : 'Suspend MSP'}
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          className="cursor-pointer text-red-400"
                          onClick={_ => {
                            setSelectedMSP(msp);
                            setShowAlert(true);
                          }}
                        >
                          <AlertTriangle className="mr-2 size-4" />
                          {msp.status === Status.INACTIVE
                            ? 'Activate MSP'
                            : 'Deactivate MSP'}
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          className="cursor-pointer text-red-400"
                          onClick={() => {
                            setSelectedMSPForPasswordReset(msp);
                            setResetPasswordDialogOpen(true);
                          }}
                        >
                          <Key className="mr-2 size-4" />
                          Reset Password
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>

                  <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
                    <div className="flex items-center gap-2">
                      <Users className="size-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium text-foreground">
                          {msp.totalClients}
                        </p>
                        <p className="text-xs text-muted-foreground">Clients</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Package className="size-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium text-foreground">
                          {msp.usedLicenseCount}/{msp.licenseCount}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          Licenses Used
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Calendar className="size-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium text-foreground">
                          {formatDateAndTime(msp.joinedDate)}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          Join Date
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Building2 className="size-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium text-foreground">
                          {msp.revenue}
                        </p>
                        <p className="text-xs text-muted-foreground">Revenue</p>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="flex flex-wrap gap-1">
                      {msp.productLists.map(pkg => (
                        <Badge key={pkg} variant="outline" className="text-xs">
                          {pkg}
                        </Badge>
                      ))}
                    </div>
                    <p className="text-xs text-muted-foreground">
                      Last login: {msp.lastLoginAt ? formatDateAndTime(msp.lastLoginAt) : 'Never'}
                    </p>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>
      </div>

      {showAlert && (
        <Dialog open={showAlert} onOpenChange={setShowAlert}>
          <DialogContent className="!w-1/3 !px-6 py-6">
            <DialogTitle>Confirm Deactivate</DialogTitle>

            <h4 className="mb-4 text-base text-white">
              Are you sure you want to deactivate this MSP?
            </h4>
            <div className="flex justify-end gap-3">
              <Button
                variant="destructive"
                onClick={() => {
                  setShowAlert(false);
                  setSelectedMSP(null);
                }}
              >
                Cancel
              </Button>
              <Button onClick={handleStatusChange}>Confirm</Button>
            </div>
          </DialogContent>
        </Dialog>
      )}

      {resetPasswordDialogOpen && selectedMSPForPasswordReset && (
        <PasswordResetDialog
          isOpen={resetPasswordDialogOpen}
          onClose={() => {
            setResetPasswordDialogOpen(false);
            setSelectedMSPForPasswordReset(null);
          }}
          userId={selectedMSPForPasswordReset.id}
          userInfo={{
            name: selectedMSPForPasswordReset.organizationName,
            email: selectedMSPForPasswordReset.mspAdminEmail,
            organizationName: selectedMSPForPasswordReset.organizationName,
          }}
          onSuccess={() => {
            // Optionally refresh data or show additional success message
          }}
        />
      )}
    </>
  );
};

export default MSPList;
