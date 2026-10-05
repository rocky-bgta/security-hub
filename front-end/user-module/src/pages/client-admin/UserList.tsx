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
import { Input } from 'common/Input';
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
import ConfirmDialog from 'components/ConfirmDialog';
import IconBackButton from 'components/IconBackButton';
import UserTableLoader from 'components/skeleton/UserTableLoader';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  Edit,
  Filter,
  KeyRound,
  Power,
  PowerOff,
  RefreshCw,
  RotateCcw,
  Search,
  UploadCloud,
  UserPlus,
  Users,
  View,
} from 'lucide-react';
import { IGetListParams, IList, RiskGroup, Status } from 'models/Global';
import { ISelectOption, TMultiValue, TSingleValue } from 'models/Input';
import { Fragment, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  cn,
  HumanizeDate,
  humanizeText,
  isSuccessResponse,
  objectToQueryStringWithArray,
} from 'utils/Helper';
import { useStore } from 'hooks/UseStore';
import ActionModal from 'features/user-onboard/from-client-admin/ActionModal';
import ViewModal from 'features/user-onboard/from-client-admin/ViewModal';
import PasswordResetDialog from 'features/user-onboard/from-client-admin/PasswordResetDialog';
import { IDropdownOption } from 'models/Dropdown';
import { routes } from 'routes/Routes';
import { useNavigate, useSearchParams } from 'react-router-dom';

interface IProps {
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

const UserList = () => {
  const { userInfo } = useStore();
  const [searchParams] = useSearchParams();
  const fromCampaign = searchParams.get('fromCampaign');
  const returnTo = searchParams.get('returnTo');
  const showCampaignBack = Boolean(fromCampaign && returnTo);
  const [departmentFilter, setDepartmentFilter] = useState<
    Array<ISelectOption>
  >([]);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [isModalOpen, setIsModalOpen] = useState<ModalType>(ModalType.NONE);
  const [selectedUserId, setSelectedUserId] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [users, setUsers] = useState<IList<IProps>>({
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
    clientAdminId: userInfo.userId,
  });
  const [departmentList, setDepartmentList] = useState<Array<ISelectOption>>(
    [],
  );
  const [confirmDialog, setConfirmDialog] = useState({
    isOpen: false,
    message: '',
    loading: false,
  });
  const [selectedUserForStatusChange, setSelectedUserForStatusChange] =
    useState<IProps | null>(null);
  const searchDebounce = useDebounce(queryString, 1000);
  const apiClient = useAPI();
  const navigate = useNavigate();

  useEffect(() => {
    const resp = objectToQueryStringWithArray(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchingData();
    }
  }, [searchDebounce]);

  const fetchingData = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.END_USER_LIST + queryString,
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

  useEffect(() => {
    if (userInfo) {
      fetchDepartmentList();
    }
  }, [userInfo]);

  const fetchDepartmentList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_DEPARTMENT_LIST +
        'clientAdminId=' +
        userInfo.userId +
        '&isSystemDefined=true&pageSize=1000&active=true',
      );
      setDepartmentList(
        response.data?.items?.map((item: IDropdownOption) => ({
          id: item.name,
          label: item.name,
          value: item.name,
        })),
      );
    } catch (error) {
      console.error(error);
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

  const closeConfirmDialog = () => {
    setConfirmDialog({ isOpen: false, message: '', loading: false });
    setSelectedUserForStatusChange(null);
  };

  const handleStatusToggleClick = (user: IProps) => {
    setSelectedUserForStatusChange(user);
    setConfirmDialog({
      isOpen: true,
      message: `Are you sure you want to ${user.status === Status.ACTIVE ? 'deactivate' : 'activate'
        } ${user.fullName}?`,
      loading: false,
    });
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
        <div className="space-y-3">
          {showCampaignBack && (
            <IconBackButton
              label="Back"
              onClick={() => {
                if (returnTo) window.location.assign(returnTo);
              }}
            />
          )}
          <div>
            <h2>User Management</h2>
            <p className="text-muted-foreground">
              Add, import, and sync users across your organization
            </p>
          </div>
        </div>
        <div className="flex items-center gap-4">
          <Button
            variant="outline"
            onClick={() => navigate(routes.syncUser.path)}
          >
            <RefreshCw className="mr-2 size-4" />
            Sync Users
          </Button>
          <Button
            variant="outline"
            onClick={() => navigate(routes.userBulkImport.path)}
          >
            <UploadCloud className="mr-2 size-4" />
            Bulk Import
          </Button>
          <Button onClick={() => setIsModalOpen(ModalType.ADD_USER)}>
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
          <div className="mb-4 grid grid-cols-7 items-center gap-2">
            <div className="col-span-3 flex">
              <div className="relative w-full">
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
                  className="w-full bg-transparent pl-9"
                />
              </div>
            </div>
            <div className="col-span-2 flex gap-2">
              <CustomSelect
                data={departmentList.map(item => ({
                  id: item.id,
                  label: item.label,
                  value: item.value,
                }))}
                isMulti
                isSearchable
                customClassName="w-full bg-transparent"
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
                  (setStatusFilter(e),
                    e === 'ALL'
                      ? setQueryParams(prevState => ({
                        ...prevState,
                        status: '' as Status,
                      }))
                      : setQueryParams(prevState => ({
                        ...prevState,
                        status: e as Status,
                      })));
                }}
              >
                <SelectTrigger className="w-full">
                  <Filter className="mr-1 size-4 text-white" />
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Status</SelectItem>
                  <SelectItem value="ACTIVE">Active</SelectItem>
                  <SelectItem value="INACTIVE">Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="col-span-1">
              <Button
                variant="outline"
                className="w-full"
                onClick={() => {
                  setQueryParams({
                    ...queryParams,
                    status: '' as Status,
                    department: '',
                    search: '',
                  });
                  setStatusFilter('ALL');
                  setDepartmentFilter([]);
                }}
              >
                <RotateCcw className="size-5" />
                Reset
              </Button>
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
                  {users?.items?.map((user: IProps) => (
                    <TableRow key={user.id}>
                      <TableCell className="font-medium">
                        {user.fullName}
                      </TableCell>
                      <TableCell>{user.email}</TableCell>
                      {/* <TableCell>{user.rolesName[0]}</TableCell> */}
                      <TableCell>{humanizeText(user.department)}</TableCell>
                      {/* <TableCell>
                        {user.department.charAt(0).toUpperCase() +
                          user.department.slice(1).toLowerCase()}
                      </TableCell> */}
                      <TableCell>
                        <Badge
                          variant={
                            user.status === 'ACTIVE' ? 'default' : 'destructive'
                          }
                        >
                          {user.status === 'ACTIVE' ? 'Active' : 'Inactive'}
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
                            title={
                              user.status === Status.ACTIVE
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
    </div>
  );
};

export default UserList;
