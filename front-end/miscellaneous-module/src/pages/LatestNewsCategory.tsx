import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { IList } from 'models/Global';
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
import ActionLatestNewsCategories from 'features/latest-news-categories/ActionLatestNewsCategories';
import DeletePolicyType from 'features/policy-type/DeletePolicyType';
import { useAPI } from 'hooks/UseAPI';
import { ILatestNewsCategories } from 'models/LatestNewsCategories';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';
import Category from './Category';
import Pagination from 'common/Pagination';
import { IGetListParams, IResponse, Status } from 'models/Global';
import { InitGetListParams } from 'utils/Constants';
import DeleteLatestNewsCategories from 'features/latest-news-categories/DeleteLatestNewsCategories';

const LatestNewsCategory = () => {
  const [loading, setLoading] = useState<boolean>(true);
  // const [data, setData] = useState<Array<IPolicyTypes>>([]);
  const [data, setData] = useState<IList<ILatestNewsCategories>>({
    items: [],
    offset: 0,
    pageSize: 0,
    total: 0,
  });
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    pageSize: 10,
    offset: 0,
  });
  const [queryString, setQueryString] = useState<string>(
    objectToQueryString(queryParams),
  );
  const [selectedLatestNewsCategories, setSelectedLatestNewsCategories] =
    useState<ILatestNewsCategories | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (queryString) {
      fetchData();
    }
  }, [queryString]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        `${API_END_POINTS.GET_NEWS_CATEGORY_LIST}${queryString}`,
      );
      setData(response.data || { items: [], offset: 0, pageSize: 0, total: 0 });
    } catch (error) {
      console.error('Error fetching latest news category data:', error);
      setData({ items: [], offset: 0, pageSize: 0, total: 0 });
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitLatestNewsCategories = (
    latestNewsCategories: ILatestNewsCategories,
  ) => {
    fetchData();
    setSelectedLatestNewsCategories(null);
    setActionType(null);
  };

  // page change function
  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleDeleteLatestNewsCategories = () => {
    toast.success(
      `Latest News Category ${selectedLatestNewsCategories?.name} has been successfully deleted.`,
    );
    fetchData();
    setSelectedLatestNewsCategories(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          Latest News Category
        </h1>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Category
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Latest News Category List
          </CardTitle>
          <CardDescription>
            View and manage all latest news category
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Category Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Category Description
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Status
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
                    colSpan={5}
                    className="text-center text-muted-foreground"
                  >
                    {loading
                      ? 'Loading...'
                      : 'No latest news category data found'}
                  </TableCell>
                </TableRow>
              ) : (
                data.items?.map(LatestNewsCategories => (
                  <TableRow key={LatestNewsCategories.id}>
                    <TableCell className="font-medium text-foreground">
                      {LatestNewsCategories.name}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {LatestNewsCategories.description}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          String(LatestNewsCategories?.status || '') ===
                            Status.ACTIVE ||
                          String(LatestNewsCategories?.status || '') ===
                            'ACTIVE'
                            ? 'default'
                            : String(LatestNewsCategories?.status || '') ===
                                  Status.INACTIVE ||
                                String(LatestNewsCategories?.status || '') ===
                                  'INACTIVE'
                              ? 'secondary'
                              : 'outline'
                        }
                        className={
                          String(LatestNewsCategories?.status || '') ===
                            Status.INACTIVE ||
                          String(LatestNewsCategories?.status || '') ===
                            'INACTIVE'
                            ? 'border border-card-border bg-muted text-muted-foreground'
                            : ''
                        }
                      >
                        {String(LatestNewsCategories?.status || '') ===
                          Status.ACTIVE ||
                        String(LatestNewsCategories?.status || '') === 'ACTIVE'
                          ? 'Active'
                          : String(LatestNewsCategories?.status || '') ===
                                Status.INACTIVE ||
                              String(LatestNewsCategories?.status || '') ===
                                'INACTIVE'
                            ? 'Inactive'
                            : 'Draft'}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedLatestNewsCategories(
                              LatestNewsCategories,
                            );
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
                            setSelectedLatestNewsCategories(
                              LatestNewsCategories,
                            );
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
        <div className="my-10 flex justify-end">
          <Pagination
            total={data?.total}
            perPage={data?.pageSize}
            onPageChange={onPageChangeHandler}
          />
        </div>
      </Card>

      <ActionLatestNewsCategories
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedLatestNewsCategories(null);
        }}
        latestNewsCategories={selectedLatestNewsCategories}
        onSubmit={handleSubmitLatestNewsCategories}
      />

      {selectedLatestNewsCategories && (
        <DeleteLatestNewsCategories
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          latestNewsCategories={selectedLatestNewsCategories}
          onDeleteLatestNewsCategories={handleDeleteLatestNewsCategories}
        />
      )}
    </div>
  );
};

export default LatestNewsCategory;
