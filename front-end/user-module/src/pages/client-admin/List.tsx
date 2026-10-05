import {
  AlertTriangle,
  Building2,
  Calendar,
  Edit,
  Eye,
  KeyRound,
  MoreHorizontal,
  Search,
  Shield,
  Users,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
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
import Pagination from 'common/Pagination';
import { Progress } from 'common/Progress';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import ViewClientDetails from 'features/client-admin/ViewClientDetails';
import PasswordResetDialog from 'features/common/PasswordResetDialog';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IGetListParams, IResponse, Status } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { FILE_PATH_PREFIX, InitGetListParams } from 'utils/Constants';
import {
  formatDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

interface IProps {
  hostPath: typeof routes;
}

// Root interface
export interface IClientAdminsResponse {
  clientAdmins: IClientAdmin[];
  offset: number;
  pageSize: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

// Client admin info
export interface IClientAdmin {
  id: string;
  email: string;
  organizationName: string;
  contactEmail: string;
  phoneNumber: string;
  billingName: string;
  billingEmail: string;
  billingAddress: string;
  mspId: string;
  countryCode: string | null;
  stateCode: string | null;
  domain: string;
  address: string;
  logoUrl: string;
  status: string;
  createdAt: string;
  clientAdminId: string | null;
  creditId: string | null;
  tierId: string | null;
  mspType: string | null;
  mspAdminEmail: string | null;
  netDays: number | null;
  department: string | null;
  roleIds: string[] | null;
  clientProducts: IClientProduct[];
  country: string;
  state: string;
  timeZone: string;
  language: string;
  industry: string;
  organizationSize: string;
  organizationType: string;
}

// Client product info
export interface IClientProduct {
  id: string;
  clientAdminId: string;
  productId: string;
  packageId: string;
  product: IProduct;
  packageDetails: IPackageDetails;
  licenseCount: number;
  usedLicenseCount: number;
  pricePerLicense: number;
  totalPrice: number;
  validityPeriod: number;
  validityUnit: string;
  assignedAt: string;
  expiryDate: string | null;
  licenseStatus: string;
}

// Product details
interface IProduct {
  productId: string;
  productName: string;
  productDescription: string;
  productStatus: string;
  thumbnailUrl: string;
}

// Package details
interface IPackageDetails {
  id: string;
  packageName: string;
  price: number;
  packageStatus: string;
  basePackageId: string | null;
  userRangeId: string;
}

const ClientList = ({ hostPath }: IProps) => {
  const navigate = useNavigate();
  const [viewClientDialogOpen, setViewClientDialogOpen] =
    useState<boolean>(false);
  const [clientId, setClientId] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    status: Status.ALL,
  });
  const [queryString, setQueryString] = useState<string>('');
  const [data, setData] = useState<IClientAdminsResponse>({
    clientAdmins: [],
    offset: 0,
    pageSize: 0,
    total: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });
  const [showAlert, setShowAlert] = useState<boolean>(false);
  const [selectedClient, setSelectedClient] = useState<IClientAdmin | null>(
    null,
  );
  const [resetPasswordDialogOpen, setResetPasswordDialogOpen] =
    useState<boolean>(false);
  const [selectedClientForPasswordReset, setSelectedClientForPasswordReset] =
    useState<IClientAdmin | null>(null);
  const apiClient = useAPI();
  const searchDebounce = useDebounce(queryString, 1000);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchData();
    }
  }, [searchDebounce]);

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const fetchData = async () => {
    try {
      const response: IResponse<IClientAdminsResponse> = await apiClient.get(
        API_END_POINTS.GET_CLIENT_ADMIN_LIST + queryString,
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      setData(response.data);
    } catch (error) {
      console.error('Error fetching ClientList data:', error);
      toast.error(error instanceof Error ? error.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  const handleSuspendClick = async (client: IClientAdmin) => {
    if (client.status === Status.SUSPEND) {
      try {
        const response = await apiClient.put(
          API_END_POINTS.SUSPEND_CLIENT_ADMIN.replace(':id', client.id),
          {
            data: {
              status: Status.ACTIVE,
            },
          },
        );
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }
        toast.success('Client activated successfully');
        fetchData();
      } catch (error) {
        toast.error(error instanceof Error ? error.message : 'An error occurred');
        console.error('Error activating client:', error);
      }
    } else {
      navigate(hostPath.clientSuspend.path + '?id=' + client.id);
    }
  };

  const handleStatusChange = async () => {
    if (!selectedClient) return;

    try {
      const response = await apiClient.put(
        API_END_POINTS.CHANGE_STATUS_CLIENT_ADMIN.replace(
          ':id',
          selectedClient.id,
        ),
        {
          data: {
            status:
              selectedClient.status === Status.INACTIVE
                ? Status.ACTIVE
                : Status.INACTIVE,
          },
        },
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      toast.success(`Client status changed successfully`);
      fetchData();
      setShowAlert(false);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'An error occurred');
      console.error('Error changing client status:', error);
    }
  };

  return (
    <>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2>Client Management</h2>
            <p className="text-muted-foreground">
              Manage and monitor all client organizations
            </p>
          </div>
          <Button
            className="bg-primary text-primary-foreground"
            onClick={() => navigate(hostPath.clientOnboarding.path)}
          >
            <Building2 className="mr-2 size-4" />
            Add New Client
          </Button>
        </div>

        <div className="flex items-center gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search Clients..."
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

        <div className="space-y-6">
          {loading ? (
            <p className="text-center text-muted-foreground">Loading...</p>
          ) : data?.clientAdmins?.length === 0 ? (
            <p className="text-center text-muted-foreground">No data found</p>
          ) : (
            data?.clientAdmins?.map(client => (
              <Card key={client.id} className="pt-4">
                <CardContent className="space-y-4">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-4">
                      {client?.logoUrl ? (
                        <img
                          src={FILE_PATH_PREFIX + client?.logoUrl}
                          alt={client?.organizationName}
                          className="size-12 object-cover"
                        />
                      ) : (
                        <Avatar className="size-12">
                          <AvatarFallback className="bg-primary text-primary-foreground">
                            {client?.organizationName
                              .split(' ')
                              .map((n: string) => n[0])
                              .join('')
                              .slice(0, 2)}
                          </AvatarFallback>
                        </Avatar>
                      )}
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <h3 className="text-lg font-semibold text-foreground">
                            {client?.organizationName}
                          </h3>
                          <Badge
                            variant={
                              client?.status === Status.ACTIVE
                                ? 'default'
                                : client.status === Status.PENDING
                                  ? 'secondary'
                                  : 'destructive'
                            }
                            className={
                              client?.status === Status.ACTIVE
                                ? 'bg-green-500/20 text-green-400'
                                : client.status === Status.PENDING
                                  ? 'bg-orange-500/20 text-orange-400'
                                  : 'bg-red-500/20 text-red-400'
                            }
                          >
                            {client?.status}
                          </Badge>
                          {/* <Badge variant="outline" className="text-xs">
                            {client?.industry.name}
                          </Badge> */}
                        </div>
                        <p className="text-sm text-muted-foreground">
                          MSP: {client?.mspAdminEmail} Aspire Tech
                        </p>
                        <p className="text-sm text-foreground">
                          email: {client?.email}
                        </p>
                        <p className="text-sm text-foreground">
                          domain: {client?.domain}
                        </p>
                        <p className="text-sm text-muted-foreground">
                          contact email: {client?.contactEmail} | contact phone:{' '}
                          {`${client?.phoneNumber}`}
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
                          onClick={() => {
                            setViewClientDialogOpen(true);
                            setClientId(client.id);
                          }}
                          className="cursor-pointer text-secondary-foreground"
                        >
                          <Eye className="mr-2 size-4" />
                          View Details
                        </DropdownMenuItem>
                        <DropdownMenuItem className="cursor-pointer text-secondary-foreground">
                          <Edit className="mr-2 size-4" />
                          <Link
                            to={hostPath.clientEdit.path.replace(
                              ':id',
                              client.id,
                            )}
                          >
                            Edit Client
                          </Link>
                        </DropdownMenuItem>
                        <DropdownMenuItem className="cursor-pointer text-secondary-foreground">
                          <Users className="mr-2 size-4" />
                          <Link
                            to={hostPath.clientUserList.path.replace(
                              ':id',
                              client.id,
                            )}
                          >
                            Manage Users
                          </Link>
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          className="cursor-pointer text-red-400"
                          onClick={_ => handleSuspendClick(client)}
                        >
                          <AlertTriangle className="mr-2 size-4" />
                          {client.status === Status.SUSPEND
                            ? 'Activate Client'
                            : 'Suspend Client'}
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          className="cursor-pointer text-red-400"
                          onClick={_ => {
                            setSelectedClient(client);
                            setShowAlert(true);
                          }}
                        >
                          <AlertTriangle className="mr-2 size-4" />
                          {client.status === Status.INACTIVE
                            ? 'Activate Client'
                            : 'Deactivate Client'}
                        </DropdownMenuItem>
                        {/* <DropdownMenuItem className="text-secondary-foreground">
                        View Reports
                      </DropdownMenuItem> */}
                        <DropdownMenuItem
                          className="cursor-pointer text-red-400"
                          onClick={() => {
                            setSelectedClientForPasswordReset(client);
                            setResetPasswordDialogOpen(true);
                          }}
                        >
                          <KeyRound className="mr-2 size-4" />
                          Reset Password
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>

                  <div className="grid grid-cols-2 gap-4 md:grid-cols-5">
                    <div className="flex items-center gap-2">
                      <Users className="size-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium text-foreground">
                          {client?.clientProducts.map((product: IClientProduct) => product?.usedLicenseCount).reduce((acc: number, curr: number) => acc + curr, 0)}
                        </p>
                        <p className="text-xs text-muted-foreground">Users</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Shield className="size-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium text-foreground">
                          {client?.clientProducts.map((product: IClientProduct) => product?.licenseCount).reduce((acc: number, curr: number) => acc + curr, 0)}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          Licenses
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Calendar className="size-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium text-foreground">
                          {/* {client.createdAt} */} demo data
                        </p>
                        <p className="text-xs text-muted-foreground">
                          Campaigns
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      <Building2 className="size-4 text-primary" />
                      <div>
                        <p className="text-sm font-medium text-foreground">
                          {/* {client.status} */}
                          demo data
                        </p>
                        <p className="text-xs text-muted-foreground">
                          Completion
                        </p>
                      </div>
                    </div>
                    <div>
                      <p className="mb-1 text-xs text-muted-foreground">
                        License Usage demo progress
                      </p>
                      <Progress
                        value={
                          (client?.clientProducts.map((product: IClientProduct) => product?.usedLicenseCount).reduce((acc: number, curr: number) => acc + curr, 0) /
                            client?.clientProducts.map((product: IClientProduct) => product?.licenseCount).reduce((acc: number, curr: number) => acc + curr, 0)) *
                          100
                        }
                        className="h-2"
                      />
                    </div>
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="flex flex-wrap gap-1">
                      {client?.clientProducts?.map((pkg: IClientProduct) => (
                        <Badge
                          key={pkg.id + pkg.productId}
                          variant="outline"
                          className="text-xs"
                        >
                          {pkg?.product?.productName}
                        </Badge>
                      ))}
                    </div>
                    <div className="text-right">
                      <p className="text-xs text-muted-foreground">
                        Joined: {formatDateAndTime(client?.createdAt)}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        Last activity: {formatDateAndTime(client?.createdAt)}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
          <Pagination
            total={data?.total}
            perPage={data?.pageSize}
            onPageChange={onPageChangeHandler}
          />
        </div>

        {viewClientDialogOpen && (
          <ViewClientDetails
            clientId={clientId}
            open={viewClientDialogOpen}
            onClose={() => setViewClientDialogOpen(false)}
          />
        )}
      </div>

      {showAlert && (
        <Dialog open={showAlert} onOpenChange={setShowAlert}>
          <DialogContent className="!w-1/3 !px-6 py-6">
            <DialogTitle>Confirm Deactivate</DialogTitle>

            <h4 className="mb-4 text-base text-white">
              Are you sure you want to deactivate this client?
            </h4>
            <div className="flex justify-end gap-3">
              <Button
                variant="destructive"
                onClick={() => {
                  setShowAlert(false);
                  setSelectedClient(null);
                }}
              >
                Cancel
              </Button>
              <Button onClick={handleStatusChange}>Confirm</Button>
            </div>
          </DialogContent>
        </Dialog>
      )}

      {resetPasswordDialogOpen && selectedClientForPasswordReset && (
        <PasswordResetDialog
          isOpen={resetPasswordDialogOpen}
          onClose={() => {
            setResetPasswordDialogOpen(false);
            setSelectedClientForPasswordReset(null);
          }}
          userId={
            selectedClientForPasswordReset.clientAdminId ||
            selectedClientForPasswordReset.id
          }
          userInfo={{
            name: selectedClientForPasswordReset.organizationName,
            email: selectedClientForPasswordReset.email,
            organizationName: selectedClientForPasswordReset.organizationName,
          }}
          onSuccess={() => {
            // Optionally refresh data or show additional success message
          }}
        />
      )}
    </>
  );
};

export default ClientList;
