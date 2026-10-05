import { Edit, Plus, Search, Trash2 } from 'lucide-react';
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
import { Input } from 'common/Input';
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
import ActionSubIndustries from 'features/sub-industries/ActionSubIndustries';
import DeleteSubIndustries from 'features/sub-industries/DeleteSubIndustries';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IGetListParams, IResponse } from 'models/Global';
import { IIndustries } from 'models/Industries';
import { ISubIndustries } from 'models/SubIndustries';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime, objectToQueryString } from 'utils/Helper';

const SubIndustries = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<ISubIndustries>>([]);
  const [industries, setIndustries] = useState<Array<IIndustries>>([]);
  const [selectedSubIndustries, setSelectedSubIndustries] =
    useState<ISubIndustries | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    search: '',
    active: '',
    industryId: '',
  });

  const apiClient = useAPI();
  const searchDebounce = useDebounce(queryString, 1000);

  useEffect(() => {
    const fetchIndustries = async () => {
      try {
        const response: IResponse<Array<IIndustries>> = await apiClient.get(
          API_END_POINTS.GET_INDUSTRIES_LIST,
        );
        setIndustries(Array.isArray(response.data) ? response.data : []);
      } catch (error) {
        console.error('Error fetching industries:', error);
      }
    };
    fetchIndustries();
  }, [apiClient]);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce !== undefined) {
      fetchData();
    }
  }, [searchDebounce]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response: IResponse<Array<ISubIndustries>> = await apiClient.get(
        API_END_POINTS.GET_SUB_INDUSTRIES_LIST + queryString,
      );
      setData(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching sub-industries data:', error);
      toast.error('Failed to fetch sub-industries');
    } finally {
      setLoading(false);
    }
  };

  const getIndustryName = (industryId: string) =>
    industries.find(industry => industry.id === industryId)?.name ?? '-';

  const handleSubmitSubIndustries = (subIndustries: ISubIndustries) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, subIndustries];
      } else {
        return [
          ...prevData.map(r => (r.id === subIndustries.id ? subIndustries : r)),
        ];
      }
    });

    setSelectedSubIndustries(null);
    setActionType(null);
    fetchData();
  };

  const handleDeleteSubIndustries = () => {
    toast.success(
      `Sub-industry ${selectedSubIndustries?.name} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedSubIndustries?.id),
    ]);
    setSelectedSubIndustries(null);
    setShowDeleteDialog(false);
    fetchData();
  };

  const handleResetFilters = () => {
    setQueryParams({
      search: '',
      active: '',
      industryId: '',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          Sub-Industries Management
        </h1>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Sub-Industry
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-5">
            <div className="relative col-span-2">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder="Search sub-industries..."
                value={queryParams.search ?? ''}
                onChange={e =>
                  setQueryParams({ ...queryParams, search: e.target.value })
                }
                className="pl-10"
              />
            </div>
            <div>
              <Select
                value={
                  queryParams.industryId === ''
                    ? 'all'
                    : (queryParams.industryId ?? 'all')
                }
                onValueChange={value =>
                  setQueryParams({
                    ...queryParams,
                    industryId: value === 'all' ? '' : value,
                  })
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Filter by industry" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Industries</SelectItem>
                  {industries.map(industry => (
                    <SelectItem key={industry.id} value={industry.id}>
                      {industry.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Select
                value={
                  queryParams.active === '' ? 'all' : (queryParams.active ?? 'all')
                }
                onValueChange={value =>
                  setQueryParams({
                    ...queryParams,
                    active: value === 'all' ? '' : value,
                  })
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Filter by status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="true">Active</SelectItem>
                  <SelectItem value="false">Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <Button variant="outline" onClick={handleResetFilters}>
              Reset
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Sub-Industries List
          </CardTitle>
          <CardDescription>View and manage all sub-industries</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Sub-Industry Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Industry
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Sub-Industry Code
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
              {loading ? (
                <TableRow>
                  <TableCell
                    colSpan={7}
                    className="text-center text-muted-foreground"
                  >
                    Loading...
                  </TableCell>
                </TableRow>
              ) : data.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={7}
                    className="text-center text-muted-foreground"
                  >
                    No sub-industries found
                  </TableCell>
                </TableRow>
              ) : (
                data.map(subIndustry => (
                  <TableRow key={subIndustry.id}>
                    <TableCell className="font-medium text-foreground">
                      {subIndustry.name}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {getIndustryName(subIndustry.industryId)}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {subIndustry.code}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={subIndustry.active ? 'default' : 'secondary'}
                      >
                        {subIndustry.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(subIndustry.createdAt as string)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(subIndustry.updatedAt as string)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedSubIndustries(subIndustry);
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
                            setSelectedSubIndustries(subIndustry);
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
        </CardContent>
      </Card>

      <ActionSubIndustries
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedSubIndustries(null);
        }}
        subIndustries={selectedSubIndustries}
        onSubmit={handleSubmitSubIndustries}
      />

      {selectedSubIndustries && (
        <DeleteSubIndustries
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          subIndustries={selectedSubIndustries}
          onDeleteSubIndustries={handleDeleteSubIndustries}
        />
      )}
    </div>
  );
};

export default SubIndustries;
