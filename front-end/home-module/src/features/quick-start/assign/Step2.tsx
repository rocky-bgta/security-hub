import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
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
import useAPI from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import useStore from 'hooks/UseStore';
import { IGetListParams, IList, Status } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { humanizeText, objectToQueryString } from 'utils/Helper';
import AssignedPackagesLoader from './AssignedPackagesLoader';

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

enum RiskGroup {
  CRITICAL_RISK = 'CRITICAL_RISK',
  HIGH_RISK = 'HIGH_RISK',
  LOW_RISK = 'LOW_RISK',
  MEDIUM_RISK = 'MEDIUM_RISK',
  ALL = 'ALL',
}

const Step2 = ({
  data,
  onUpdate,
  onNext,
  onPrevious,
  selectedSubPackage,
}: IProps) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();

  const [selectedUsers, setSelectedUsers] = useState<string[]>(data);
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
    clientAdminId: userInfo?.userId,
    subPackageId: selectedSubPackage?.id,
  });

  const searchBounced = useDebounce(queryString, 500);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchBounced) {
      fetchUsers();
    }
  }, [searchBounced]);

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
      // return;
    }
    onNext();
    onUpdate(selectedUsers);
  };

  // toggle single user
  const toggleUser = (id: string) => {
    if (!selectedUsers.includes(id) && selectedUsers.length === 5) {
      toast.warning('You can select up to 5 users only');
      return;
    }
    setSelectedUsers(prev =>
      prev.includes(id) ? prev.filter(uid => uid !== id) : [...prev, id],
    );
  };

  // toggle all users
  const toggleAll = () => {
    if (selectedUsers.length === users?.items?.length) {
      setSelectedUsers([]); // unselect all
    } else {
      setSelectedUsers(users.items.slice(0, 5).map(u => u.id)); // select 5
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div className="home-space-y-6">
      <div className="home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-2">
        <div className="home-col-span-2">
          {loading ? (
            <AssignedPackagesLoader count={5} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="home-relative">
                    <Checkbox
                      checked={selectedUsers.length === users.items.length}
                      onCheckedChange={toggleAll}
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
                    <TableCell colSpan={6} className="home-text-center">
                      No users found
                    </TableCell>
                  </TableRow>
                )}
                {users?.items?.map(user => (
                  <TableRow key={user.id}>
                    <TableCell className="home-relative">
                      <Checkbox
                        checked={selectedUsers.includes(user.id)}
                        onCheckedChange={() => toggleUser(user.id)}
                      />
                    </TableCell>
                    <TableCell className="home-font-medium">
                      {user.fullName}
                    </TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.department}</TableCell>
                    <TableCell>
                      <div
                        className={
                          user.riskGroup === RiskGroup.HIGH_RISK
                            ? 'home-w-fit home-rounded home-bg-rose-600/10 home-px-2 home-py-1 home-font-medium home-text-rose-600'
                            : user.riskGroup === RiskGroup.CRITICAL_RISK
                              ? 'home-w-fit home-rounded home-bg-red-600/10 home-px-2 home-py-1 home-font-medium home-text-red-600'
                              : user.riskGroup === RiskGroup.MEDIUM_RISK
                                ? 'home-w-fit home-rounded home-bg-yellow-600/10 home-px-2 home-py-1 home-font-medium home-text-yellow-600'
                                : 'home-w-fit home-rounded home-bg-green-600/10 home-px-2 home-py-1 home-font-medium home-text-green-600'
                        }
                      >
                        {humanizeText(user.riskGroup)}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div
                        className={
                          user.status === Status.ACTIVE
                            ? 'home-w-fit home-rounded home-bg-primary/10 home-px-2 home-py-1 home-font-medium home-text-primary'
                            : 'home-w-fit home-rounded home-bg-vibrant-red/10 home-px-2 home-py-1 home-font-medium home-text-vibrant-red'
                        }
                      >
                        {user.status === Status.ACTIVE ? 'Active' : 'Inactive'}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </div>
        <div className="home-col-span-full home-flex">
          <div className="home-w-1/2">
            <Pagination
              onPageChange={onPageChangeHandler}
              perPage={users?.pageSize}
              total={users?.total}
            />
          </div>

          {users?.total > users?.pageSize && (
            <div className="home-flex home-items-center home-gap-3">
              <Label htmlFor="filterByClient" className="home-text-nowrap">
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

      <div className="home-flex home-justify-end home-gap-4 home-pt-6">
        <Button
          variant="outline"
          onClick={onPrevious}
          className="home-px-8 home-py-2"
        >
          Previous Step
        </Button>
        <Button onClick={handleSave} disabled={selectedUsers.length === 0}>
          Next Step
        </Button>
      </div>
    </div>
  );
};

export default Step2;
