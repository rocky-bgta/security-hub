import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import CustomSelect from 'common/CustomSelect';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
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
import UserTableLoader from 'components/skeleton/UserTableLoader';
import ActionModal from 'features/system-user/ActionModal';
import ViewModal from 'features/system-user/ViewModal';
import { useAPI } from 'hooks/UseAPI';
import { useAuth } from 'hooks/UseAuth';
import useDebounce from 'hooks/UseDebounce';
import {
  Edit,
  Filter,
  Power,
  PowerOff,
  Search,
  UserPlus,
  View,
} from 'lucide-react';
import { IDropdownOption } from 'models/Dropdown';
import { IGetListParams, IList, IResponse, Status } from 'models/Global';
import { ISelectOption, TMultiValue, TSingleValue } from 'models/Input';
import { ISystemUser } from 'models/User';
import { Fragment, useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
import { ROLE } from 'utils/Role';

enum ModalType {
  ADD_USER = 'ADD_USER',
  EDIT_USER = 'EDIT_USER',
  VIEW_USER = 'VIEW_USER',
  NONE = 'NONE',
}

const SystemUsers = () => {
  const { role } = useAuth();
  const navigate = useNavigate();

  const [loading, setLoading] = useState<boolean>(true);
  const [departmentList, setDepartmentList] = useState<Array<IDropdownOption>>(
    [],
  );
  const [users, setUsers] = useState<IList<ISystemUser>>({
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
  });
  const [departmentFilter, setDepartmentFilter] = useState<
    Array<ISelectOption>
  >([]);
  const [isModalOpen, setIsModalOpen] = useState<ModalType>(ModalType.NONE);
  const [selectedUserId, setSelectedUserId] = useState<string>('');
  const [confirmModalOpen, setConfirmModalOpen] = useState<boolean>(false);
  const [selectedUserForStatusChange, setSelectedUserForStatusChange] =
    useState<ISystemUser | null>(null);
  const [isStatusUpdating, setIsStatusUpdating] = useState<boolean>(false);

  const queryDebounce = useDebounce(queryString, 1000);
  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryStringWithArray(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  const fetchSystemUsers = useCallback(async () => {
    try {
      const response: IResponse<IList<ISystemUser>> = await apiClient.get(
        API_END_POINTS.SYSTEM_USER_LIST + queryDebounce,
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
  }, [apiClient, queryDebounce]);

  useEffect(() => {
    if (queryDebounce) {
      fetchSystemUsers();
    }
  }, [fetchSystemUsers, queryDebounce]);

  useEffect(() => {
    const fetchDepartmentList = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.GET_DEPARTMENT_LIST +
            'active=true&isSystemDefined=true&pageSize=1000',
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(
            response.message || 'Failed to fetch department list',
          );
        }

        setDepartmentList(
          response.data.items.map((department: IDropdownOption) => ({
            id: department.name,
            name: department.name,
          })),
        );
      } catch (error) {
        console.error('Error fetching department list:', error);
      }
    };
    fetchDepartmentList();
  }, [apiClient]);

  if (role !== ROLE.ASPIRE_ADMIN) {
    navigate(routes.dashboard.path);
    return null;
  }

  const handleEditUser = async (id: string) => {
    setSelectedUserId(id);
    setIsModalOpen(ModalType.EDIT_USER);
  };

  const handleViewUser = async (id: string) => {
    setSelectedUserId(id);
    setIsModalOpen(ModalType.VIEW_USER);
  };

  const handleConfirmStatusChange = (user: ISystemUser) => {
    setSelectedUserForStatusChange(user);
    setConfirmModalOpen(true);
  };

  const handleCloseStatusModal = () => {
    setConfirmModalOpen(false);
    setSelectedUserForStatusChange(null);
  };

  const handleStatusChange = async () => {
    if (!selectedUserForStatusChange) return;

    const newStatus =
      selectedUserForStatusChange.status === Status.ACTIVE
        ? Status.INACTIVE
        : Status.ACTIVE;

    setIsStatusUpdating(true);

    try {
      const response = await apiClient.put(
        API_END_POINTS.UPDATE_SYSTEM_USER.replace(
          ':id',
          selectedUserForStatusChange.id,
        ),
        {
          data: {
            status: newStatus,
          },
        },
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to update user status');
      }

      toast.success(response.message);
      fetchSystemUsers();
    } catch (error) {
      console.error('Error updating system user status:', error);
      toast.error(
        error instanceof Error ? error.message : 'Failed to update user status',
      );
    } finally {
      setIsStatusUpdating(false);
      handleCloseStatusModal();
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    setQueryParams(prevState => ({
      ...prevState,
      search: '',
      department: [],
      status: Status.ALL,
    }));
    setDepartmentFilter([]);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2>System User Management</h2>
          <p className="text-muted-foreground">Manage system user accounts</p>
        </div>
        <div className="flex items-center gap-3">
          <Button onClick={() => setIsModalOpen(ModalType.ADD_USER)}>
            <UserPlus className="mr-2 size-4" />
            Add User
          </Button>
        </div>
      </div>

      <div>
        <div className="mb-4">
          <div className="grid grid-cols-1 gap-3 md:grid-cols-9">
            <div className="relative col-span-5">
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
                value={queryParams.status || 'ALL'}
                onValueChange={e => {
                  setQueryParams(prevState => ({
                    ...prevState,
                    status: e === 'ALL' ? Status.ALL : (e as Status),
                  }));
                }}
              >
                <SelectTrigger className="w-full">
                  <Filter className="mr-1 size-4 text-white" />
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All</SelectItem>
                  <SelectItem value={Status.ACTIVE}>Active</SelectItem>
                  <SelectItem value={Status.INACTIVE}>Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="col-span-1">
              <Button
                variant="destructive"
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
                  <TableHead>Designation</TableHead>
                  <TableHead>Department</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Supervisor Name</TableHead>
                  <TableHead>Last Login</TableHead>
                  <TableHead className="text-center">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {users?.total === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} className="p-4 text-center">
                      No system users found.
                    </TableCell>
                  </TableRow>
                )}
                {users?.items?.map((user: ISystemUser) => (
                  <TableRow key={user.id}>
                    <TableCell className="font-medium">
                      {user.firstName} {user.lastName}
                    </TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.designation}</TableCell>
                    <TableCell>{user.department}</TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          user.status === 'ACTIVE' ? 'default' : 'destructive'
                        }
                        className="capitalize"
                      >
                        {user.status.toLowerCase()}
                      </Badge>
                    </TableCell>
                    <TableCell>{HumanizeDate(user.supervisorName)}</TableCell>
                    <TableCell>{HumanizeDate(user.lastLoginAt)}</TableCell>
                    <TableCell>
                      <div className="flex justify-center gap-1">
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => handleViewUser(user.id)}
                          title="View details"
                        >
                          <View className="size-5" />
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => handleEditUser(user.id)}
                          title="Edit details"
                        >
                          <Edit className="size-5" />
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          className={cn(
                            user.status === Status.ACTIVE
                              ? 'hover:bg-red-600'
                              : 'hover:bg-green-600',
                          )}
                          onClick={() => handleConfirmStatusChange(user)}
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
      </div>

      {isModalOpen === ModalType.ADD_USER && (
        <ActionModal
          isOpen={isModalOpen === ModalType.ADD_USER}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={() => fetchSystemUsers()}
        />
      )}

      {isModalOpen === ModalType.EDIT_USER && (
        <ActionModal
          isOpen={isModalOpen === ModalType.EDIT_USER}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={() => fetchSystemUsers()}
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

      {confirmModalOpen && (
        <Dialog open={confirmModalOpen} onOpenChange={handleCloseStatusModal}>
          <DialogContent className="xl:w-1/3">
            <DialogHeader>
              <DialogTitle>Confirmation</DialogTitle>
            </DialogHeader>

            <div>
              Are you sure you want to{' '}
              {selectedUserForStatusChange?.status === Status.ACTIVE
                ? 'deactivate'
                : 'activate'}{' '}
              {selectedUserForStatusChange?.firstName}{' '}
              {selectedUserForStatusChange?.lastName}?
            </div>

            <div className="flex justify-end gap-2">
              <Button
                onClick={handleCloseStatusModal}
                variant="outline"
                disabled={isStatusUpdating}
              >
                Cancel
              </Button>
              <Button
                onClick={handleStatusChange}
                disabled={isStatusUpdating}
                variant={
                  selectedUserForStatusChange?.status === Status.ACTIVE
                    ? 'destructive'
                    : 'default'
                }
              >
                {isStatusUpdating ? 'Confirming...' : 'Confirm'}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
};

export default SystemUsers;
