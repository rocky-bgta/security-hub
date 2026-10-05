import {
  Edit,
  Eye,
  FileText,
  Link,
  Plus,
  Search,
  Trash2,
  Video,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from 'common/AlertDialog';
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
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  cn,
  formateDate,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import { IKnowledgeHub, KnowledgeResourceTypes } from 'models/KnowledgeHub';
import { IGetListParams, IList, ModalType, Status } from 'models/Global';
import Pagination from 'common/Pagination';
import useDebounce from 'hooks/UseDebounce';
import ActionKnowledgeHub from 'features/knowledge-hub/ActionKnowledgeHub';
import TableLoader from 'components/skeleton/TableLoader';
import ViewDialog from 'features/knowledge-hub/ViewDialog';
import { InitGetListParams } from 'utils/Constants';

const ManageResources = () => {
  const apiClient = useAPI();
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [isShowModal, setIsShowModal] = useState<ModalType>(ModalType.NONE);
  const [knowledgeHubData, setKnowledgeHubData] = useState<
    IList<IKnowledgeHub>
  >({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    resourceType: '',
    categoryId: '',
    status: '',
  });
  const [categories, setCategories] = useState<
    Array<{ id: string; name: string }>
  >([]);
  const searchDebounce = useDebounce(queryString, 1000);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

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

  const getStatusBadge = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return <Badge className="">{status}</Badge>;
      case 'completed':
        return <Badge className="">{status}</Badge>;
      case 'pending':
        return <Badge className="">{status}</Badge>;
      case 'overdue':
        return <Badge className="">{status}</Badge>;
      case 'failed':
        return <Badge className="">{status}</Badge>;
      case 'suspended':
        return <Badge className="">{status}</Badge>;
      case 'inactive':
        return (
          <Badge className="bg-muted text-muted-foreground">{status}</Badge>
        );
      default:
        return <Badge className="">{status}</Badge>;
    }
  };

  useEffect(() => {
    if (searchDebounce) {
      fetchKnowledgeHub();
    }
  }, [searchDebounce]);

  const fetchKnowledgeHub = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_KNOWLEDGE_HUB_LIST + queryString,
      );
      if (isSuccessResponse(response.statusCode)) {
        setKnowledgeHubData(response.data);
      }
    } catch (error) {
      console.error('Error fetching knowledge hub:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleView = (id: string) => {
    setSelectedId(id);
    setIsShowModal(ModalType.VIEW);
  };

  const handleEdit = (id: string) => {
    setSelectedId(id);
    setIsShowModal(ModalType.EDIT);
  };

  const handleDelete = (id: string) => {
    setSelectedId(id);
    setIsShowModal(ModalType.DELETE);
  };

  const handleDeleteConfirm = async () => {
    if (selectedId) {
      try {
        setDeleteLoading(true);
        const response = await apiClient.del(
          API_END_POINTS.DELETE_KNOWLEDGE_HUB.replace(':id', selectedId),
        );
        if (isSuccessResponse(response.statusCode)) {
          toast.success('Knowledge Hub Deleted');
          setIsShowModal(ModalType.NONE);
          setSelectedId(null);
          fetchKnowledgeHub();
        }
      } catch (error) {
        console.error('Error deleting knowledge hub:', error);
      } finally {
        setDeleteLoading(false);
      }
    }
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'Document':
        return <FileText className="size-4" />;
      case 'Video':
        return <Video className="size-4" />;
      case 'Link':
        return <Link className="size-4" />;
      default:
        return <FileText className="size-4" />;
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
          <h1 className="text-3xl font-bold tracking-tight">
            Knowledge Hub Resources
          </h1>
          <p className="text-muted-foreground">
            Manage learning resources and materials
          </p>
        </div>
        <Button onClick={() => setIsShowModal(ModalType.ADD)}>
          <Plus className="mr-2 size-4" />
          Add New Resource
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Resources List</CardTitle>
          <CardDescription>
            View and manage all learning resources
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex flex-col gap-4 sm:flex-row">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder="Search resources..."
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
              value={queryParams.resourceType}
              onValueChange={value =>
                setQueryParams(prevState => ({
                  ...prevState,
                  resourceType: value === 'all' ? '' : value,
                }))
              }
            >
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Filter by type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Types</SelectItem>
                {KnowledgeResourceTypes.map(type => (
                  <SelectItem key={type} value={type}>
                    {type}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select
              value={queryParams.categoryId}
              onValueChange={value =>
                setQueryParams(prevState => ({
                  ...prevState,
                  categoryId: value === 'all' ? '' : value,
                }))
              }
            >
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="Filter by Category" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Categories</SelectItem>
                {categories.map(category => (
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
                <SelectItem value={Status.ACTIVE}>Active</SelectItem>
                <SelectItem value={Status.INACTIVE}>Inactive</SelectItem>
                <SelectItem value={Status.DRAFT}>Draft</SelectItem>
              </SelectContent>
            </Select>
            <Button
              variant="outline"
              size="sm"
              onClick={() =>
                setQueryParams(prevState => ({
                  ...prevState,
                  search: '',
                  resourceType: '',
                  categoryId: '',
                  status: '',
                }))
              }
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
                  <TableHead>Type</TableHead>
                  <TableHead>Category</TableHead>
                  <TableHead>Published Date</TableHead>
                  <TableHead>Expire Date</TableHead>
                  {/* <TableHead>Like Count</TableHead>
                <TableHead>Dislike Count</TableHead> */}
                  <TableHead>Status</TableHead>
                  <TableHead className="text-center">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {knowledgeHubData?.items?.map((resource: IKnowledgeHub) => (
                  <TableRow key={resource?.id}>
                    <TableCell>
                      <div className="font-medium">{resource?.name}</div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        {getTypeIcon(resource?.resourceType)}
                        <Badge variant="outline">
                          {resource?.resourceType}
                        </Badge>
                      </div>
                    </TableCell>
                    <TableCell>{resource?.category?.name}</TableCell>
                    <TableCell>
                      {formateDate(resource?.publishedDate)}
                    </TableCell>
                    <TableCell>{formateDate(resource?.expireDate)}</TableCell>
                    {/* <TableCell>{resource.likeCount}</TableCell>
                  <TableCell>{resource.dislikeCount}</TableCell> */}
                    <TableCell>
                      <Badge
                        variant="secondary"
                        className={cn(
                          resource?.status === Status.ACTIVE
                            ? 'bg-green-500 text-white'
                            : 'bg-red-500 text-white',
                          'capitalize',
                        )}
                      >
                        {resource?.status?.toLowerCase()}
                      </Badge>
                    </TableCell>
                    <TableCell className="flex justify-center">
                      <div className="flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleView(resource?.id)}
                        >
                          <Eye className="size-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleEdit(resource?.id)}
                        >
                          <Edit className="size-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDelete(resource?.id)}
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
          <div className="mt-4">
            <Pagination
              total={knowledgeHubData?.total}
              perPage={knowledgeHubData?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      {isShowModal === ModalType.ADD && (
        <ActionKnowledgeHub
          isOpen={isShowModal === ModalType.ADD}
          onClose={() => setIsShowModal(ModalType.NONE)}
          onSubmit={() => {
            fetchKnowledgeHub();
          }}
        />
      )}
      {isShowModal === ModalType.EDIT && (
        <ActionKnowledgeHub
          isOpen={isShowModal === ModalType.EDIT}
          onClose={() => setIsShowModal(ModalType.NONE)}
          id={selectedId ?? ''}
          onSubmit={() => {
            fetchKnowledgeHub();
          }}
        />
      )}

      {isShowModal === ModalType.VIEW && (
        <ViewDialog
          isOpen={isShowModal === ModalType.VIEW}
          onClose={() => setIsShowModal(ModalType.NONE)}
          selectedId={selectedId ?? ''}
          getTypeIcon={getTypeIcon}
          getStatusBadge={getStatusBadge}
        />
      )}

      {/* Delete Confirmation Dialog */}
      <AlertDialog
        open={isShowModal === ModalType.DELETE}
        onOpenChange={open =>
          !deleteLoading &&
          setIsShowModal(open ? ModalType.DELETE : ModalType.NONE)
        }
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Knowledge Hub</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete this knowledge hub? This action
              cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel
              disabled={deleteLoading}
              onClick={() => {
                setIsShowModal(ModalType.NONE);
                setSelectedId(null);
              }}
            >
              Cancel
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteConfirm}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={deleteLoading}
            >
              {deleteLoading ? 'Deleting...' : 'Delete'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
};

export default ManageResources;
