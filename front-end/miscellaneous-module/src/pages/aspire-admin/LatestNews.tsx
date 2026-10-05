import { useEffect, useState } from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Badge } from 'common/Badge';
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
import { Search, Plus, Edit, Trash2, Eye } from 'lucide-react';
import ConfirmDialog from 'components/ConfirmDialog';
import { IGetListParams, ModalType, Status } from 'models/Global';
import ActionLatestNews from 'features/latest-news/ActionLatestNews';
import ViewLatestNews from 'features/latest-news/ViewLatestNews';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from 'hooks/UseAPI';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
  sliceWords,
} from 'utils/Helper';
import { IList } from 'models/Global';
import { ILatestNews } from 'models/LatestNews';
import { ILatestNewsCategory } from 'models/Category';
import TableLoader from 'components/skeleton/TableLoader';
import Pagination from 'common/Pagination';
import { InitGetListParams } from 'utils/Constants';
import useDebounce from 'hooks/UseDebounce';

const LatestNews = () => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(ModalType.NONE);
  const [newsData, setNewsData] = useState<IList<ILatestNews>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [selectedNews, setSelectedNews] = useState<ILatestNews | null>(null);
  const [categories, setCategories] = useState<ILatestNewsCategory[]>([]);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    offset: 0,
    pageSize: 10,
    category: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchData();
    }
  }, [searchDebounce]);

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    const response = await apiClient.get(
      API_END_POINTS.GET_LATEST_NEWS_CATEGORY_LIST,
    );
    if (isSuccessResponse(response.statusCode)) {
      setCategories(response.data);
    }
  };

  const fetchData = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_LATEST_NEWS_LIST + queryString,
      );
      if (isSuccessResponse(response.statusCode)) {
        setNewsData(response.data);
      }
    } catch (error) {
      console.error('Error fetching poll survey data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteClick = (news: ILatestNews) => {
    if (!news) return;
    setSelectedNews(news);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!selectedNews) return;
    try {
      setIsDeleting(true);
      const response = await apiClient.del(
        API_END_POINTS.DELETE_LATEST_NEWS.replace(':id', selectedNews.id),
      );
      if (isSuccessResponse(response.statusCode)) {
        fetchData();
      }
    } catch (error) {
      console.error('Error deleting news:', error);
    } finally {
      setDeleteDialogOpen(false);
      setIsDeleting(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">News Management</h1>
          <p className="text-muted-foreground">
            Create and manage news posts for your platform
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(ModalType.ADD)}>
          <Plus className="mr-2 size-4" />
          Create News Post
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>News Posts</CardTitle>
          <CardDescription>View and manage all news posts</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex flex-col gap-4 sm:flex-row">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder="Search news posts..."
                value={queryParams.search}
                onChange={e =>
                  setQueryParams(prevState => ({
                    ...prevState,
                    search: e.target.value,
                  }))
                }
                className="pl-10"
              />
            </div>
            <Select
              value={queryParams.category}
              onValueChange={value =>
                setQueryParams(prevState => ({
                  ...prevState,
                  category: value === 'all' ? '' : value,
                }))
              }
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="Filter by Category" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Categories</SelectItem>
                {categories.map((category: ILatestNewsCategory) => (
                  <SelectItem key={category.id} value={category.id}>
                    {category.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              value={queryParams.status}
              onValueChange={value =>
                setQueryParams(prevState => ({
                  ...prevState,
                  status: value === 'all' ? '' : value,
                }))
              }
            >
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="ACTIVE">Active</SelectItem>
                <SelectItem value="DRAFT">Draft</SelectItem>
                <SelectItem value="INACTIVE">Inactive</SelectItem>
              </SelectContent>
            </Select>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                setQueryParams(prevState => ({
                  ...prevState,
                  search: '',
                  status: '',
                  category: '',
                }));
              }}
            >
              Reset
            </Button>
          </div>

          {loading ? (
            <TableLoader count={10} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Title</TableHead>
                  <TableHead>Category</TableHead>
                  <TableHead>Published Date</TableHead>
                  <TableHead>Like Count</TableHead>
                  <TableHead>Dislike Count</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-center">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {newsData.items.length === 0 && (
                  <TableRow>
                    <TableCell
                      colSpan={6}
                      className="text-center text-muted-foreground"
                    >
                      No news found
                    </TableCell>
                  </TableRow>
                )}

                {newsData.items.map((item: ILatestNews) => (
                  <TableRow key={item.id}>
                    <TableCell className="w-[25%]">
                      <div className="flex items-center gap-2">
                        <div className="font-medium">
                          {sliceWords(item.name, 10)}
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>{item?.category?.name ?? 'N/A'}</TableCell>
                    <TableCell>
                      {formateDateAndTime(item.publishedDate ?? '')}
                    </TableCell>
                    <TableCell>{item?.likeCount ?? 0}</TableCell>
                    <TableCell>{item?.dislikeCount ?? 0}</TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          item?.status === Status.ACTIVE
                            ? 'default'
                            : item?.status === Status.DRAFT
                              ? 'destructive'
                              : item?.status === Status.INACTIVE
                                ? 'secondary'
                                : 'outline'
                        }
                      >
                        {item?.status === Status.ACTIVE
                          ? 'Active'
                          : item?.status === Status.DRAFT
                            ? 'Draft'
                            : item?.status === Status.INACTIVE
                              ? 'Inactive'
                              : 'Draft'}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setSelectedNews(item);
                            setIsModalOpen(ModalType.VIEW);
                          }}
                          title="View"
                        >
                          <Eye className="size-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setIsModalOpen(ModalType.EDIT);
                            setSelectedNews(item);
                          }}
                          title="Edit"
                        >
                          <Edit className="size-4" />
                        </Button>
                        {/* <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handlePin(item.id)}
                        className={item.pinned ? 'text-primary' : ''}
                        title={item.pinned ? 'Unpin' : 'Pin'}
                      >
                        <Pin className="h-4 w-4" />
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleStatusToggle(item.id)}
                        title="Toggle Status"
                      >
                        <Power className="h-4 w-4" />
                      </Button> */}
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDeleteClick(item)}
                          title="Delete"
                        >
                          <Trash2 className="size-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <div className="flex justify-end pt-4">
            <Pagination
              total={newsData?.total}
              perPage={newsData?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      {isModalOpen === ModalType.ADD && (
        <ActionLatestNews
          isOpen={isModalOpen === ModalType.ADD}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={fetchData}
        />
      )}
      {isModalOpen === ModalType.EDIT && (
        <ActionLatestNews
          isOpen={isModalOpen === ModalType.EDIT}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          id={selectedNews?.id ?? ''}
          onSubmit={fetchData}
        />
      )}

      {isModalOpen === ModalType.VIEW && (
        <ViewLatestNews
          isOpen={isModalOpen === ModalType.VIEW}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          selectedNews={selectedNews ?? null}
        />
      )}

      <ConfirmDialog
        isOpen={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleDeleteConfirm}
        message={`Are you sure you want to delete "${selectedNews?.name}"?`}
        buttonText="Delete"
        loading={isDeleting}
        loadingText="Deleting..."
      />
    </div>
  );
};

export default LatestNews;
