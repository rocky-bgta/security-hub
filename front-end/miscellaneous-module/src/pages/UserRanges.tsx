import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import ActionUserRange from 'features/user-range/ActionUserRange';
import DeleteUserRange from 'features/user-range/DeleteUserRange';
import { useAPI } from 'hooks/UseAPI';
import { IUserRange } from 'models/UserRange';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import Pagination from 'common/Pagination';
import { InitGetListParams } from 'utils/Constants';
import useDebounce from 'hooks/UseDebounce';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

const UserRanges = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<IList<IUserRange>>({
    items: [],
    total: 0,
    pageSize: 10,
    offset: 0,
  });

  const [selectedUserRange, setSelectedUserRange] = useState<IUserRange | null>(
    null,
  );
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    offset: 0,
    pageSize: 10,
    isActive: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);

  const apiClient = useAPI();
  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    fetchData();
  }, [searchDebounce]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const response: IResponse<IUserRange[] | IList<IUserRange>> =
        await apiClient.get(API_END_POINTS.GET_USER_RANGE_LIST + queryString);

      // Handle both array response and IList response
      if (Array.isArray(response.data)) {
        // If API returns a simple array, convert it to IList format
        setData({
          ...InitGetListParams,
          total: response.data.length,
          items: response.data,
        });
      } else {
        // If API returns IList format
        setData({
          ...InitGetListParams,
          total: response.data.total,
          items: response.data.items,
        });
      }
    } catch (error) {
      console.error('Error fetching user range data:', error);
      toast.error('Failed to fetch user ranges');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitUserRange = (userRange: IUserRange) => {
    setData(prevData => {
      if (actionType === 'create') {
        return {
          ...prevData,
          items: [...prevData.items, userRange],
          total: prevData.total + 1,
        };
      } else {
        return {
          ...prevData,
          items: prevData.items.map(r =>
            r.id === userRange.id ? userRange : r,
          ),
        };
      }
    });

    setSelectedUserRange(null);
    setActionType(null);
    fetchData(); // Refresh data from server
  };

  const handleDeleteUserRange = async () => {
    try {
      const response = await apiClient.del(
        API_END_POINTS.DELETE_USER_RANGE.replace(
          ':id',
          selectedUserRange?.id || '',
        ),
      );
      if (isSuccessResponse(response.statusCode)) {
        toast.success('User range deleted successfully');
        fetchData();
        setSelectedUserRange(null);
        setShowDeleteDialog(false);
      } else {
        toast.error(response.data.message);
      }
    } catch (error) {
      console.error('Error deleting user range:', error);
      toast.error('Failed to delete user range');
    }
  };

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            User Range Management
          </h1>
          <p className="text-muted-foreground">
            Manage user ranges and their details
          </p>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add User Range
        </Button>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              User Range List
            </CardTitle>
            <CardDescription>View and manage all user ranges</CardDescription>
          </div>
          <div className="flex w-64 items-center gap-2">
            <Select
              value={queryParams.isActive}
              onValueChange={value =>
                setQueryParams({
                  ...queryParams,
                  isActive: value === 'all' ? '' : value,
                })
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="Select status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="true">Active</SelectItem>
                <SelectItem value="false">Inactive</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Range Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Min Users
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Max Users
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Description
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Status
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Created
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Updated
                </TableHead>
                <TableHead className="text-center font-semibold text-foreground">
                  Actions
                </TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading || data.items.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={8}
                    className="text-center text-muted-foreground"
                  >
                    {loading ? 'Loading...' : 'No user ranges found'}
                  </TableCell>
                </TableRow>
              ) : (
                data.items.map(userRange => (
                  <TableRow key={userRange.id}>
                    <TableCell className="font-medium text-foreground">
                      {userRange.rangeName}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {userRange.minUsers}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {userRange.maxUsers}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {userRange.description || '-'}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={userRange.isActive ? 'default' : 'secondary'}
                      >
                        {userRange.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(userRange.createdAt)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(userRange.updatedAt)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedUserRange(userRange);
                            setActionType('edit');
                          }}
                          className="hover:bg-muted"
                        >
                          <Edit className="size-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedUserRange(userRange);
                            setShowDeleteDialog(true);
                          }}
                          className="hover:bg-destructive/20 hover:text-destructive"
                        >
                          <Trash2 className="size-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>

          <div className="flex justify-end pt-6">
            <Pagination
              total={data.total}
              perPage={data.pageSize}
              onPageChange={handlePageChange}
            />
          </div>
        </CardContent>
      </Card>

      <ActionUserRange
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedUserRange(null);
        }}
        userRange={selectedUserRange}
        onSubmit={handleSubmitUserRange}
      />

      {selectedUserRange && (
        <DeleteUserRange
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          userRange={selectedUserRange}
          onDeleteUserRange={handleDeleteUserRange}
        />
      )}
    </div>
  );
};

export default UserRanges;
