import { Edit, Plus } from 'lucide-react';
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
import ActionDepartment from 'features/department/ActionDepartment';
import DeleteDepartment from 'features/department/DeleteDepartment';
import { useAPI } from 'hooks/UseAPI';
import { IDepartment } from 'models/Department';
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

const DepartmentList = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<IList<IDepartment>>({
    items: [],
    total: 0,
    pageSize: 10,
    offset: 0,
  });

  const [selectedDepartment, setSelectedDepartment] =
    useState<IDepartment | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    offset: 0,
    pageSize: 10,
    isSystemDefined: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);

  const apiClient = useAPI();
  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchData();
    }
  }, [searchDebounce]);

  const fetchData = async () => {
    try {
      const response: IResponse<IList<IDepartment>> = await apiClient.get(
        API_END_POINTS.GET_DEPARTMENT_LIST + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching department data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitDepartment = (department: IDepartment) => {
    setData(prevData => {
      if (actionType === 'create') {
        return {
          ...prevData,
          items: [...prevData.items, department],
        };
      } else {
        return {
          ...prevData,
          items: prevData.items.map(r =>
            r.id === department.id ? department : r,
          ),
        };
      }
    });

    setSelectedDepartment(null);
    setActionType(null);
  };

  const handleDeleteDepartment = async () => {
    try {
      const response: IResponse<IDepartment> = await apiClient.delete(
        API_END_POINTS.DELETE_DEPARTMENT.replace(
          ':id',
          selectedDepartment?.id || '',
        ),
      );
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Department deleted successfully');
        fetchData();
        setSelectedDepartment(null);
        setShowDeleteDialog(false);
      } else {
        toast.error('Failed to delete department');
      }
    } catch (error) {
      console.error('Error deleting department:', error);
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
            Department Management
          </h1>
          <p className="text-muted-foreground">
            Manage departments and their details
          </p>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Department
        </Button>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              Department List
            </CardTitle>
            <CardDescription>View and manage all departments</CardDescription>
          </div>
          <div className="flex w-1/3 items-center gap-2">
            <Select
              value={
                queryParams.isSystemDefined === 'true'
                  ? 'system'
                  : queryParams.isSystemDefined === 'false'
                    ? 'client'
                    : 'all'
              }
              onValueChange={e =>
                setQueryParams(prevState => ({
                  ...prevState,
                  isSystemDefined:
                    e === 'all'
                      ? ''
                      : e === 'system'
                        ? 'true'
                        : e === 'client'
                          ? 'false'
                          : e,
                }))
              }
            >
              <SelectTrigger>
                <SelectValue placeholder="System Defined" />
              </SelectTrigger>
              <SelectContent className="w-full">
                <SelectItem value="all">All</SelectItem>
                <SelectItem value="system">System Created</SelectItem>
                <SelectItem value="client">Client Created</SelectItem>
              </SelectContent>
            </Select>
            <Input
              placeholder="Search departments"
              value={queryParams.search}
              onChange={e =>
                setQueryParams(prevState => ({
                  ...prevState,
                  search: e.target.value,
                }))
              }
              className="w-full"
            />
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Department Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Description
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  System Defined
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
                    colSpan={7}
                    className="text-center text-muted-foreground"
                  >
                    {loading ? 'Loading...' : 'No departments found'}
                  </TableCell>
                </TableRow>
              ) : (
                data.items.map(department => (
                  <TableRow key={department.id}>
                    <TableCell className="font-medium text-foreground">
                      {department.name}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {department.description || '-'}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          department.isSystemDefined ? 'default' : 'secondary'
                        }
                      >
                        {department.isSystemDefined ? 'Yes' : 'No'}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={department.active ? 'default' : 'secondary'}
                      >
                        {department.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(department.createdAt)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(department.updatedAt)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedDepartment(department);
                            setActionType('edit');
                          }}
                          className="hover:bg-muted"
                        >
                          <Edit className="size-4" />
                        </Button>
                        {/* <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedDepartment(department);
                            setShowDeleteDialog(true);
                          }}
                          className="hover:bg-destructive/20 hover:text-destructive"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button> */}
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

      <ActionDepartment
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedDepartment(null);
        }}
        department={selectedDepartment}
        onSubmit={handleSubmitDepartment}
      />

      {selectedDepartment && (
        <DeleteDepartment
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          department={selectedDepartment}
          onDeleteDepartment={handleDeleteDepartment}
        />
      )}
    </div>
  );
};

export default DepartmentList;
