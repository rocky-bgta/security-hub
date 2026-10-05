import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Label } from 'common/Label';
import { useEffect, useState } from 'react';

import CustomCheckbox from 'common/CustomCheckbox';
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
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IDropdownOption } from 'models/DropDown';
import { CourseStatus, IGetListParams, IList, RiskGroup, Status } from 'models/Global';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  humanizeText,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import SearchSelect from 'components/SearchSelect';

interface IProps {
  data: string[];
  onUpdate: (data: string[]) => void;
  onNext: () => void;
  onPrevious: () => void;
  selectedSubPackage: any;
}

interface IUser {
  id: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  department: string;
  status: Status;
  clientAdminId: string;
  riskGroup: RiskGroup;
  lastLoginAt: string | null;
}

const AspireAdminAssignSubPackageStep1 = ({
  data,
  onUpdate,
  onNext,
  onPrevious,
  selectedSubPackage,
}: IProps) => {
  const apiClient = useAPI();

  const [selectedUsers, setSelectedUsers] = useState<string[]>(data);
  const [selectedGroup, setSelectedGroup] = useState<string>('');
  const [selectedDepartment, setSelectedDepartment] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState<IList<IUser>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    group: '',
    department: '',
    clientAdminId: selectedSubPackage.clientAdminId,
    subPackageId: selectedSubPackage.id,
    status: CourseStatus.ACTIVE,
  });
  const [departmentList, setDepartmentList] = useState<Array<IDropdownOption>>(
    [],
  );
  const searchBounced = useDebounce(queryString, 500);

  useEffect(() => {
    fetchDepartmentList();
  }, []);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchBounced) {
      fetchUsers();
    }
  }, [searchBounced]);

  const fetchDepartmentList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_DEPARTMENT_LIST +
        '&clientAdminId=' +
        selectedSubPackage.clientAdminId +
        '&active=true&isSystemDefined=true&pageSize=1000',
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

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.UNASSIGNED_USER_LIST + queryString,
      );
      setUsers(response.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    if (selectedUsers.length === 0) {
      toast.error('Please select at least one user');
      return false;
    }
    return true;
  };

  const handleSave = () => {
    if (!validateForm()) {
      return;
    }
    onNext();
    onUpdate(selectedUsers);
  };

  // toggle single user
  const toggleUser = (id: string) => {
    setSelectedUsers(prev =>
      prev.includes(id) ? prev.filter(uid => uid !== id) : [...prev, id],
    );
  };

  // toggle all users
  const toggleAll = () => {
    if (selectedUsers.length === users?.items?.length) {
      setSelectedUsers([]); // unselect all
    } else {
      setSelectedUsers(users.items.map(u => u.id)); // select all
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <Card className="content-w-full">
      <CardHeader>
        <div className="content-flex content-items-center content-justify-between">
          <CardTitle>Select User for Assign Packages</CardTitle>
          <Button
            variant="outline"
            onClick={() => {
              setSelectedGroup('');
              setSelectedDepartment('');
              setQueryParams(prev => ({
                ...prev,
                search: '',
                group: '',
                department: '',
              }));
            }}
            className="content-px-8 content-py-2"
          >
            Reset
          </Button>
        </div>
      </CardHeader>
      <CardContent className="content-space-y-6">
        <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-2">
          <div className="content-col-span-full content-grid content-grid-cols-2 content-gap-3">
            <div className="content-col-span-1">
              <Label htmlFor="filterByClient">Search Uses</Label>
              <Input
                type="text"
                placeholder="Search users..."
                value={queryParams.search}
                onChange={e =>
                  setQueryParams(prev => ({ ...prev, search: e.target.value }))
                }
                className="content-w-full"
              />
            </div>

            <div className="content-col-span-1">
              <Label htmlFor="filterByClient">Filter by Group</Label>
              <Select
                value={selectedGroup}
                onValueChange={e => {
                  setSelectedGroup(e);
                  setQueryParams(prev => ({ ...prev, group: e }));
                }}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Filter by Group" />
                </SelectTrigger>
                <SelectContent>
                  {/* <SelectItem value="CRITICAL_RISK">Critical Risk</SelectItem> */}
                  <SelectItem value="HIGH_RISK">High Risk</SelectItem>
                  <SelectItem value="MEDIUM_RISK">Medium Risk</SelectItem>
                  <SelectItem value="LOW_RISK">Low Risk</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="content-col-span-1">
              <Label htmlFor="filterByDepartment">Filter by Department</Label>
              <SearchSelect
                items={departmentList.map(dept => ({
                  value: dept.name,
                  label: dept.name,
                }))}
                placeholder="Filter by Department"
                value={selectedDepartment}
                onValueChange={value => {
                  setSelectedDepartment(value);
                  setQueryParams(prev => ({ ...prev, department: value }));
                }}
              />
            </div>
          </div>

          <div className="content-col-span-2">
            {loading ? (
              <AssignedPackagesLoader count={5} />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="content-relative">
                      <CustomCheckbox
                        checked={selectedUsers.length === users.items.length}
                        onChange={toggleAll}
                      />
                    </TableHead>
                    <TableHead>Name</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Department</TableHead>
                    <TableHead>Group</TableHead>
                    <TableHead>Status</TableHead>
                  </TableRow>
                </TableHeader>

                <TableBody>
                  {users?.items?.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={5} className="content-text-center">
                        No users found
                      </TableCell>
                    </TableRow>
                  )}
                  {users?.items?.map(user => (
                    <TableRow key={user.id}>
                      <TableCell className="content-relative">
                        <CustomCheckbox
                          checked={selectedUsers.includes(user.id)}
                          onChange={() => toggleUser(user.id)}
                        />
                      </TableCell>
                      <TableCell className="content-font-medium">
                        {user.fullName}
                      </TableCell>
                      <TableCell>{user.email}</TableCell>
                      <TableCell>{humanizeText(user.department)}</TableCell>
                      <TableCell>
                        <div
                          className={
                            user.riskGroup === RiskGroup.HIGH_RISK
                              ? 'content-w-fit content-rounded content-bg-rose-600/10 content-px-2 content-py-1 content-font-medium content-text-rose-600'
                              : user.riskGroup === RiskGroup.CRITICAL_RISK
                                ? 'content-w-fit content-rounded content-bg-red-600/10 content-px-2 content-py-1 content-font-medium content-text-red-600'
                                : user.riskGroup === RiskGroup.MEDIUM_RISK
                                  ? 'content-w-fit content-rounded content-bg-yellow-600/10 content-px-2 content-py-1 content-font-medium content-text-yellow-600'
                                  : 'content-w-fit content-rounded content-bg-green-600/10 content-px-2 content-py-1 content-font-medium content-text-green-600'
                          }
                        >
                          {humanizeText(user.riskGroup)}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div
                          className={
                            user.status === Status.ACTIVE
                              ? 'content-w-fit content-rounded content-bg-primary/10 content-px-2 content-py-1 content-font-medium content-text-primary'
                              : 'content-w-fit content-rounded content-bg-vibrant-red/10 content-px-2 content-py-1 content-font-medium content-text-vibrant-red'
                          }
                        >
                          {user.status === Status.ACTIVE
                            ? 'Active'
                            : 'Inactive'}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </div>
          <div className="content-col-span-full content-flex">
            <div className="content-w-1/2">
              <Pagination
                onPageChange={onPageChangeHandler}
                perPage={users?.pageSize}
                total={users?.total}
              />
            </div>

            {users?.total > users?.pageSize && (
              <div className="content-flex content-items-center content-gap-3">
                <Label htmlFor="filterByClient" className="content-text-nowrap">
                  page size
                </Label>
                <Select
                  value={users?.pageSize.toString()}
                  onValueChange={e =>
                    setQueryParams(prev => ({ ...prev, pageSize: Number(e) }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder={`${users?.pageSize} per page`} />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="10">10</SelectItem>
                    <SelectItem value="20">20</SelectItem>
                    <SelectItem value="40">40</SelectItem>
                    <SelectItem value="80">80</SelectItem>
                    <SelectItem value="100">100</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            )}
          </div>
        </div>

        <div className="content-flex content-justify-between content-pt-6">
          <Button
            variant="outline"
            onClick={onPrevious}
            className="content-px-8 content-py-2"
          >
            Previous
          </Button>
          <Button onClick={handleSave}>Save & Continue</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default AspireAdminAssignSubPackageStep1;
