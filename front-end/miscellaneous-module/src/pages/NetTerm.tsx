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
import TableLoader from 'components/skeleton/TableLoader';
import ActionNetTerm from 'features/net-term/ActionNetTerm';
import DeleteNetTerm from 'features/net-term/DeleteNetTerm';
import { useAPI } from 'hooks/UseAPI';
import { INetTerm } from 'models/NetTerm';
import { IGetListParams, IList, IResponse, ModalType } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { formateDateAndTime, isSuccessResponse } from 'utils/Helper';
import useDebounce from 'hooks/UseDebounce';

const buildNetTermQuery = (params: IGetListParams): string => {
  const parts: string[] = [];
  if (params.search !== undefined && params.search !== '')
    parts.push(`search=${encodeURIComponent(params.search)}`);
  if (params.isActive !== undefined && params.isActive !== '')
    parts.push(`isActive=${params.isActive}`);
  parts.push(`offset=${params.offset ?? 0}`);
  parts.push(`limit=${params.pageSize ?? 10}`);
  return parts.join('&');
};

const NetTerm = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<IList<INetTerm>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    isActive: '',
  });
  const [queryString, setQueryString] = useState<string>(() =>
    buildNetTermQuery({
      ...InitGetListParams,
      search: '',
      isActive: '',
    }),
  );
  const searchDebounce = useDebounce(queryString, 1000);

  const [selectedNetTerm, setSelectedNetTerm] = useState<INetTerm | null>(null);
  const [actionType, setActionType] = useState<ModalType>(ModalType.NONE);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    setQueryString(buildNetTermQuery(queryParams));
  }, [queryParams]);

  useEffect(() => {
    fetchData();
  }, [searchDebounce]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const qs = buildNetTermQuery(queryParams);
      const response: IResponse<IList<INetTerm>> = await apiClient.get(
        API_END_POINTS.GET_NET_TERM_LIST + qs,
      );
      if (isSuccessResponse(response?.statusCode)) {
        setData({
          ...InitGetListParams,
          total: response.data.total,
          pageSize: response.data.pageSize,
          offset: response.data.offset,
          items: response.data.items ?? [],
        });
      }
    } catch (error) {
      console.error('Error fetching net term data:', error);
      toast.error('Failed to fetch net terms');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitNetTerm = (netTerm: INetTerm) => {
    setData(prevData => {
      if (actionType === ModalType.ADD) {
        return {
          ...prevData,
          items: [...prevData.items, netTerm],
          total: prevData.total + 1,
        };
      }
      return {
        ...prevData,
        items: prevData.items.map(r => (r.id === netTerm.id ? netTerm : r)),
      };
    });
    setSelectedNetTerm(null);
    setActionType(ModalType.NONE);
  };

  const handleDeleteNetTerm = () => {
    toast.success(
      `Net term ${selectedNetTerm?.netTermName} has been successfully deleted.`,
    );
    setData(prevData => ({
      ...prevData,
      items: prevData.items.filter(r => r.id !== selectedNetTerm?.id),
      total: Math.max(0, prevData.total - 1),
    }));
    setSelectedNetTerm(null);
    setShowDeleteDialog(false);
  };

  const onPageChange = (page: number) => {
    setQueryParams(prev => ({
      ...prev,
      offset: page - 1,
    }));
  };

  const handleResetFilters = () => {
    setQueryParams({
      ...InitGetListParams,
      search: '',
      isActive: '',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Net Term Configuration
          </h1>
          <p className="text-muted-foreground">
            Manage net term configurations and payment terms
          </p>
        </div>
        <Button
          onClick={() => setActionType(ModalType.ADD)}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Net Term
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-4">
            <div className="relative col-span-2">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder="Search net terms..."
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
                  queryParams.isActive === ''
                    ? 'all'
                    : (queryParams.isActive ?? 'all')
                }
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
                  <SelectItem value="all">All</SelectItem>
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
            Net Term List
          </CardTitle>
          <CardDescription>
            View and manage all net term configurations
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <TableLoader count={10} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="font-semibold text-foreground">
                    Net Term Name
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Days
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
                {!data?.items?.length ? (
                  <TableRow>
                    <TableCell
                      colSpan={6}
                      className="text-center text-muted-foreground"
                    >
                      No net terms found
                    </TableCell>
                  </TableRow>
                ) : (
                  data.items.map(item => (
                    <TableRow key={item.id}>
                      <TableCell className="font-medium text-foreground">
                        {item.netTermName}
                      </TableCell>
                      <TableCell>{item.netTermInDays}</TableCell>
                      <TableCell>
                        <Badge
                          variant={item.isActive ? 'default' : 'secondary'}
                        >
                          {item.isActive ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {formateDateAndTime(item.createdAt)}
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {formateDateAndTime(item.updatedAt)}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedNetTerm(item);
                              setActionType(ModalType.EDIT);
                            }}
                            className="hover:bg-muted"
                          >
                            <Edit className="size-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedNetTerm(item);
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
          )}
        </CardContent>
      </Card>

      <div className="flex justify-end">
        <Pagination
          total={data?.total ?? 0}
          perPage={data?.pageSize ?? 10}
          onPageChange={onPageChange}
        />
      </div>

      <ActionNetTerm
        isOpen={actionType === ModalType.ADD || actionType === ModalType.EDIT}
        onClose={() => {
          setActionType(ModalType.NONE);
          setSelectedNetTerm(null);
        }}
        netTerm={selectedNetTerm}
        onSubmit={handleSubmitNetTerm}
      />

      {selectedNetTerm && (
        <DeleteNetTerm
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          netTerm={selectedNetTerm}
          onDeleteNetTerm={handleDeleteNetTerm}
        />
      )}
    </div>
  );
};

export default NetTerm;
