import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import CustomSelect from 'common/CustomSelect';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Pagination from 'common/Pagination';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import UserTableLoader from 'components/skeleton/UserTableLoader';
import ConfirmDialog from 'components/ConfirmDialog';
import ActionModal from 'features/user-onboard/from-aspire-admin/ActionModal';
import ViewModal from 'features/user-onboard/from-aspire-admin/ViewModal';
import PasswordResetDialog from 'features/user-onboard/from-client-admin/PasswordResetDialog';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  Ban,
  Edit,
  Filter,
  KeyRound,
  Power,
  PowerOff,
  RefreshCw,
  Search,
  Upload,
  UserPlus,
  Users,
  View,
} from 'lucide-react';
import { IDropdownOption } from 'models/Dropdown';
import {
  IGetListParams,
  IList,
  IResponse,
  RiskGroup,
  Status,
} from 'models/Global';
import { ISelectOption, TMultiValue, TSingleValue } from 'models/Input';
import { ISuspensionReason } from 'pages/client-admin/ClientSuspend';
import { Fragment, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import {
  cn,
  HumanizeDate,
  isSuccessResponse,
  objectToQueryStringWithArray,
} from 'utils/Helper';

interface IClientUser {
  id: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  department: string;
  status: Status;
  clientAdminId: string;
  lastLoginAt: string;
  riskGroup: RiskGroup;
}

enum ModalType {
  ADD_USER = 'ADD_USER',
  EDIT_USER = 'EDIT_USER',
  VIEW_USER = 'VIEW_USER',
  RESET_PASSWORD = 'RESET_PASSWORD',
  NONE = 'NONE',
}

const ClientUsersList = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [departmentFilter, setDepartmentFilter] = useState<
    Array<ISelectOption>
  >([]);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState<ModalType>(ModalType.NONE);
  const [selectedUserId, setSelectedUserId] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [users, setUsers] = useState<IList<IClientUser>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    department: [],
    status: Status.ALL,
    clientAdminId: id || '',
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [departmentList, setDepartmentList] = useState<Array<IDropdownOption>>(
    [],
  );
  const [confirmDialog, setConfirmDialog] = useState({
    isOpen: false,
    message: '',
    loading: false,
  });
  const [selectedUserForStatusChange, setSelectedUserForStatusChange] =
    useState<IClientUser | null>(null);
  const [suspendModalOpen, setSuspendModalOpen] = useState(false);
  const [selectedUserForSuspend, setSelectedUserForSuspend] =
    useState<IClientUser | null>(null);
  const [suspensionReasons, setSuspensionReasons] = useState<
    Array<ISuspensionReason>
  >([]);
  const [suspensionReason, setSuspensionReason] = useState('');
  const [isSuspending, setIsSuspending] = useState(false);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryStringWithArray(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchingData();
    }
  }, [searchDebounce]);

  useEffect(() => {
    fetchDepartmentList();
  }, []);

  useEffect(() => {
    if (suspendModalOpen) fetchSuspensionReasons();
  }, [suspendModalOpen]);

  const fetchSuspensionReasons = async () => {
    try {
      const response: IResponse<Array<ISuspensionReason>> = await apiClient.get(
        API_END_POINTS.GET_SUSPENSION_REASONS,
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      setSuspensionReasons(response.data);
    } catch (error) {
      console.error('Error fetching suspension reasons:', error);
      toast.error(
        error instanceof Error ? error.message : 'Failed to load suspension reasons',
      );
    }
  };

  const fetchDepartmentList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_DEPARTMENT_LIST +
        'active=true' +
        '&isSystemDefined=true&pageSize=1000',
      );
      if (isSuccessResponse(response.statusCode)) {
        setDepartmentList(
          response.data.items.map((department: IDropdownOption) => ({
            id: department.name,
            name: department.name,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching department list:', error);
    }
  };

  const fetchingData = async () => {
    // setLoading(true);

    try {
      const response: IResponse<IList<IClientUser>> = await apiClient.get(
        API_END_POINTS.FETCH_END_USER_LIST + queryString,
      );

      setUsers({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditUser = async (id: string) => {
    setSelectedUserId(id);
    setIsModalOpen(ModalType.EDIT_USER);
  };

  const handleViewUser = async (id: string) => {
    setSelectedUserId(id);
    setIsModalOpen(ModalType.VIEW_USER);
  };

  const handleResetPassword = async (id: string) => {
    setSelectedUserId(id);
    setIsModalOpen(ModalType.RESET_PASSWORD);
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleBulkImport = () => {
    navigate(routes.userBulkImport.path + '?clientAdminId=' + id);
  };

  const handleReset = () => {
    setQueryParams(prevState => ({
      ...prevState,
      search: '',
      department: [],
      status: 'ALL' as Status,
    }));
    setStatusFilter('ALL');
    setDepartmentFilter([]);
  };

  const closeConfirmDialog = () => {
    setConfirmDialog({ isOpen: false, message: '', loading: false });
    setSelectedUserForStatusChange(null);
  };

  const handleStatusToggleClick = (user: IClientUser) => {
    if (user.status === Status.SUSPEND) return;

    setSelectedUserForStatusChange(user);
    setConfirmDialog({
      isOpen: true,
      message: `Are you sure you want to ${
        user.status === Status.ACTIVE ? 'deactivate' : 'activate'
      } ${user.fullName}?`,
      loading: false,
    });
  };

  const handleConfirmSuspendClick = (user: IClientUser) => {
    setSelectedUserForSuspend(user);
    setSuspensionReason('');
    setSuspendModalOpen(true);
  };

  const handleCloseSuspendModal = () => {
    setSuspendModalOpen(false);
    setSelectedUserForSuspend(null);
    setSuspensionReason('');
    setIsSuspending(false);
  };

  const suspendUser = async () => {
    if (!selectedUserForSuspend) return;

    if (
      selectedUserForSuspend.status === Status.ACTIVE &&
      !suspensionReason
    ) {
      toast.error('Please select a suspension reason');
      return;
    }

    setIsSuspending(true);

    try {
      const payload = {
        status: Status.ACTIVE,
        suspendReason: '',
      };

      if (selectedUserForSuspend.status === Status.ACTIVE) {
        payload.status = Status.SUSPEND;
        payload.suspendReason = suspensionReason;
      }

      const response = await apiClient.put(
        API_END_POINTS.SUSPEND_END_USER.replace(
          ':id',
          selectedUserForSuspend.id,
        ),
        { data: payload },
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      toast.success(response.message);
      fetchingData();
      handleCloseSuspendModal();
    } catch (error) {
      console.error('Error updating user suspension:', error);
      toast.error(error instanceof Error ? error.message : 'An error occurred');
      setIsSuspending(false);
    }
  };

  const handleConfirmStatusChange = async () => {
    if (!selectedUserForStatusChange) return;

    const newStatus =
      selectedUserForStatusChange.status === Status.ACTIVE
        ? Status.INACTIVE
        : Status.ACTIVE;

    setConfirmDialog(prev => ({ ...prev, loading: true }));

    try {
      const response = await apiClient.put(API_END_POINTS.ADD_END_USER, {
        data: {
          id: selectedUserForStatusChange.id,
          status: newStatus,
        },
      });

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to update user status');
      }

      toast.success(response.message);
      fetchingData();
      closeConfirmDialog();
    } catch (error) {
      console.error('Error updating user status:', error);
      toast.error(
        error instanceof Error ? error.message : 'Failed to update user status',
      );
      setConfirmDialog(prev => ({ ...prev, loading: false }));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2>User Management</h2>
          <p className="text-muted-foreground">
            Add, import, and sync users across your organization
          </p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            onClick={() => setIsModalOpen(ModalType.ADD_USER)}
          >
            <RefreshCw className="mr-2 size-4" />
            Sync
          </Button>
          <Button variant="outline" onClick={handleBulkImport}>
            <Upload className="mr-2 size-4" />
            Bulk Import
          </Button>
          <Button
            variant="outline"
            onClick={() => setIsModalOpen(ModalType.ADD_USER)}
          >
            <UserPlus className="mr-2 size-4" />
            Add User
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-white">
            <Users className="size-5" />
            Total Users ({users?.total})
          </CardTitle>
          <CardDescription>
            Search and filter users by department or status
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-4">
            <div className="grid grid-cols-1 gap-3 md:grid-cols-7">
              <div className="relative col-span-3">
                <Search className="absolute left-3 top-3 size-4 text-white" />
                <Input
                  placeholder="Search users..."
                  value={String(queryParams.search)}
                  onChange={e => {
                    setQueryParams(prevState => ({
                      ...prevState,
                      search: e.target.value,
                    }));
                  }}
                  className="w-full pl-9"
                />
              </div>
              <div className="col-span-2">
                <CustomSelect
                  data={departmentList.map(item => ({
                    id: item.id,
                    label: item.name,
                    value: item.name,
                  }))}
                  isMulti
                  isSearchable
                  customClassName="w-full"
                  name="department"
                  placeholder="Select departments..."
                  value={departmentFilter}
                  handleChange={(
                    newValue:
                      | TMultiValue<ISelectOption>
                      | TSingleValue<ISelectOption>,
                  ) => {
                    const selectedDepartments =
                      newValue as TMultiValue<ISelectOption>;

                    // Check if "ALL" is selected or if it's the only selection
                    const hasAllSelected = selectedDepartments?.some(
                      item => item.value === 'ALL',
                    );

                    if (hasAllSelected) {
                      // If "ALL" is selected, clear other selections
                      setDepartmentFilter([]);
                      setQueryParams(prevState => ({
                        ...prevState,
                        department: '',
                        offset: 0,
                      }));
                    } else {
                      setDepartmentFilter(Array.from(selectedDepartments));
                      const departmentValues =
                        selectedDepartments?.map(
                          (item: ISelectOption) => item.value,
                        ) || [];

                      setQueryParams(prevState => ({
                        ...prevState,
                        department: departmentValues,
                        offset: 0,
                      }));
                    }
                  }}
                />
              </div>
              <div className="col-span-1">
                <Select
                  value={statusFilter}
                  onValueChange={e => {
                    setStatusFilter(e);
                    setQueryParams(prevState => ({
                      ...prevState,
                      status: e === 'ALL' ? ('' as Status) : (e as Status),
                    }));
                  }}
                >
                  <SelectTrigger className="w-full">
                    <Filter className="mr-1 size-4 text-white" />
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ALL">All</SelectItem>
                    <SelectItem value="ACTIVE">Active</SelectItem>
                    <SelectItem value="INACTIVE">Inactive</SelectItem>
                    <SelectItem value="SUSPEND">Suspended</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="col-span-1">
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={handleReset}
                >
                  Reset
                </Button>
              </div>
            </div>
          </div>
          {loading ? (
            <UserTableLoader />
          ) : (
            <Fragment>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Email</TableHead>
                    {/* <TableHead>Role</TableHead> */}
                    <TableHead>Department</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Last Login</TableHead>
                    <TableHead className="text-center">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {users?.total === 0 && (
                    <TableRow>
                      <TableCell colSpan={6} className="p-4 text-center">
                        No users found.
                      </TableCell>
                    </TableRow>
                  )}
                  {users?.items?.map((user: IClientUser) => (
                    <TableRow key={user.id}>
                      <TableCell className="font-medium">
                        {user.fullName}
                      </TableCell>
                      <TableCell>{user.email}</TableCell>
                      {/* <TableCell>{user.rolesName[0]}</TableCell> */}
                      <TableCell>{user.department}</TableCell>
                      {/* <TableCell>
                        {user.department.charAt(0).toUpperCase() +
                          user.department.slice(1).toLowerCase()}
                      </TableCell> */}
                      <TableCell>
                        <Badge
                          variant={
                            user.status === Status.ACTIVE
                              ? 'default'
                              : 'destructive'
                          }
                        >
                          {user.status === Status.ACTIVE
                            ? 'Active'
                            : user.status === Status.SUSPEND
                              ? 'Suspended'
                              : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell>{HumanizeDate(user.lastLoginAt)}</TableCell>
                      <TableCell>
                        <div className="flex justify-center gap-1">
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleViewUser(user.id)}
                            title="View user details"
                          >
                            <View className="size-5" />
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleEditUser(user.id)}
                            title="Edit user"
                          >
                            <Edit className="size-5" />
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => handleResetPassword(user.id)}
                            title="Reset password"
                            className="text-orange-500 hover:bg-orange-500/10 hover:text-orange-600"
                          >
                            <KeyRound className="size-5" />
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            className={cn(
                              user.status === Status.ACTIVE
                                ? 'hover:bg-red-600 hover:text-white text-green-500'
                                : 'hover:bg-green-600 hover:text-white text-red-500',
                            )}
                            onClick={() => handleStatusToggleClick(user)}
                            disabled={user.status === Status.SUSPEND}
                            title={
                              user.status === Status.SUSPEND
                                ? 'Use unsuspend to activate suspended users'
                                : user.status === Status.ACTIVE
                                  ? 'Deactivate user'
                                  : 'Activate user'
                            }
                          >
                            {user.status === Status.ACTIVE ? (
                              <PowerOff className="size-5" />
                            ) : (
                              <Power className="size-5" />
                            )}
                          </Button>
                          {(user.status === Status.ACTIVE ||
                            user.status === Status.SUSPEND) && (
                            <Button
                              size="sm"
                              variant="ghost"
                              className={cn(
                                user.status === Status.ACTIVE
                                  ? 'text-red-400 hover:bg-red-600/20 hover:text-red-300'
                                  : 'text-green-400 hover:bg-green-600/20 hover:text-green-300',
                              )}
                              onClick={() => handleConfirmSuspendClick(user)}
                              title={
                                user.status === Status.ACTIVE
                                  ? 'Suspend user'
                                  : 'Activate suspended user'
                              }
                            >
                              <Ban className="size-5" />
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Fragment>
          )}
          <div className="my-6 flex justify-end">
            <Pagination
              total={users?.total}
              perPage={users?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      {isModalOpen === ModalType.ADD_USER && (
        <ActionModal
          isOpen={isModalOpen === ModalType.ADD_USER}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={() => fetchingData()}
          selectedUserId={''}
        />
      )}
      {isModalOpen === ModalType.EDIT_USER && (
        <ActionModal
          isOpen={isModalOpen === ModalType.EDIT_USER}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={() => fetchingData()}
          selectedUserId={selectedUserId}
        />
      )}
      {isModalOpen === ModalType.VIEW_USER && (
        <ViewModal
          isOpen={isModalOpen === ModalType.VIEW_USER}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          selectedUserId={selectedUserId}
        />
      )}
      {isModalOpen === ModalType.RESET_PASSWORD && (
        <PasswordResetDialog
          isOpen={isModalOpen === ModalType.RESET_PASSWORD}
          onClose={() => {
            setIsModalOpen(ModalType.NONE);
            setSelectedUserId('');
          }}
          selectedUserId={selectedUserId}
          onSuccess={() => {
            // Optionally refresh data or show additional success message
          }}
        />
      )}

      <ConfirmDialog
        isOpen={confirmDialog.isOpen}
        message={confirmDialog.message}
        loading={confirmDialog.loading}
        loadingText="Updating..."
        buttonText={
          selectedUserForStatusChange?.status === Status.ACTIVE
            ? 'Deactivate'
            : 'Activate'
        }
        onClose={closeConfirmDialog}
        onConfirm={handleConfirmStatusChange}
      />

      {suspendModalOpen && (
        <Dialog open={suspendModalOpen} onOpenChange={handleCloseSuspendModal}>
          <DialogContent className="xl:w-1/3">
            <DialogHeader>
              <DialogTitle>Confirmation</DialogTitle>
            </DialogHeader>

            <div>
              Are you sure you want to{' '}
              {selectedUserForSuspend?.status === Status.ACTIVE
                ? 'suspend'
                : 'activate'}{' '}
              {selectedUserForSuspend?.fullName}?
            </div>

            {selectedUserForSuspend?.status === Status.ACTIVE && (
              <div className="space-y-1">
                <Label>Choose Suspension Reason*</Label>
                <Select
                  value={suspensionReason}
                  onValueChange={value => setSuspensionReason(value)}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="Select a reason" />
                  </SelectTrigger>
                  <SelectContent>
                    {suspensionReasons.map(reason => (
                      <SelectItem key={reason.id} value={reason.name}>
                        {reason.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}

            <div className="flex justify-end gap-2">
              <Button
                onClick={handleCloseSuspendModal}
                variant="outline"
                disabled={isSuspending}
              >
                Cancel
              </Button>
              <Button
                onClick={suspendUser}
                disabled={isSuspending}
                variant={
                  selectedUserForSuspend?.status === Status.ACTIVE
                    ? 'destructive'
                    : 'default'
                }
              >
                {isSuspending ? 'Updating...' : 'Confirm'}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
};

export default ClientUsersList;
