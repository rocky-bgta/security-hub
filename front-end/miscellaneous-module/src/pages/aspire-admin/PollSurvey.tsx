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

import { Search, Plus, Edit, Trash2, Eye, Power } from 'lucide-react';
import ConfirmDialog from 'components/ConfirmDialog';
import { IGetListParams, IList, ModalType, Status } from 'models/Global';
import ViewPollSurvey from 'features/poll-survey/ViewPollSurvey';
import ActionPollSurvey from 'features/poll-survey/ActionPollSurvey';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  isSuccessResponse,
  objectToQueryString,
  sliceWords,
} from 'utils/Helper';
import { IPollSurvey } from 'models/PollsSurvey';
import Pagination from 'common/Pagination';
import TableLoader from 'components/skeleton/TableLoader';
import useDebounce from 'hooks/UseDebounce';

const PollSurvey = () => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<IList<IPollSurvey>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [isModalOpen, setIsModalOpen] = useState(ModalType.NONE);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedItem, setSelectedItem] = useState<IPollSurvey | null>(null);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    offset: 0,
    pageSize: 10,
    search: '',
    type: '',
    status: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);

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
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_POLL_SURVEY_LIST + queryString,
      );
      if (isSuccessResponse(response.statusCode)) {
        setData(response.data);
      }
    } catch (error) {
      console.error('Error fetching poll survey data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteConfirm = () => {
    if (!selectedItem) return;

    setData(prev => ({
      ...prev,
      items: prev.items.filter(item => item.id !== selectedItem.id),
    }));

    setDeleteDialogOpen(false);
    setSelectedItem(null);
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    setQueryParams({
      offset: 0,
      pageSize: 10,
      search: '',
      type: '',
      status: '',
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">
            Polls & Surveys Management
          </h1>
          <p className="text-muted-foreground">
            Create and manage polls and surveys for feedback collection
          </p>
        </div>
        <Button onClick={() => setIsModalOpen(ModalType.ADD)}>
          <Plus className="mr-2 size-4" />
          Create Poll/Survey
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Polls & Surveys List</CardTitle>
          <CardDescription>
            View and manage all polls and surveys
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex flex-col gap-4 sm:flex-row">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-3 size-4 text-muted-foreground" />
              <Input
                placeholder="Search polls and surveys..."
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
              value={queryParams.type}
              onValueChange={value =>
                setQueryParams(prevState => ({
                  ...prevState,
                  type: value === 'all' ? '' : value,
                }))
              }
            >
              <SelectTrigger className="w-[150px]">
                <SelectValue placeholder="Filter by type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Types</SelectItem>
                <SelectItem value="Poll">Poll</SelectItem>
                <SelectItem value="Survey">Survey</SelectItem>
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
            <Button onClick={handleReset}>Reset</Button>
          </div>
          {loading ? (
            <TableLoader count={10} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Title</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Type</TableHead>
                  {/* <TableHead>Target Audience</TableHead> */}
                  {/* <TableHead>Duration</TableHead> */}
                  {/* <TableHead>Responses</TableHead> */}
                  <TableHead>Status</TableHead>
                  <TableHead className="text-center">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data?.items?.length === 0 && (
                  <TableRow>
                    <TableCell
                      colSpan={6}
                      className="text-center text-muted-foreground"
                    >
                      No poll/survey found
                    </TableCell>
                  </TableRow>
                )}
                {data?.items?.map(item => (
                  <TableRow key={item.id}>
                    <TableCell className="w-1/4">
                      <div className="font-medium">
                        {sliceWords(item.title, 10)}
                      </div>
                    </TableCell>
                    <TableCell className="w-1/4">
                      <div className="text-sm">
                        {sliceWords(item.description ?? '', 10) || 'N/A'}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={item.type === 'POLL' ? 'default' : 'secondary'}
                      >
                        {item.type}
                      </Badge>
                    </TableCell>
                    {/* <TableCell>{item.questions.length}</TableCell>
                  <TableCell>
                    <div className="text-sm">
                      <div>{item.questions.length}</div>
                    </div>
                  </TableCell> */}
                    <TableCell>
                      <Badge
                        variant={
                          item.status === Status.ACTIVE
                            ? 'default'
                            : item.status === Status.INACTIVE
                              ? 'secondary'
                              : 'outline'
                        }
                      >
                        {item.status === Status.ACTIVE
                          ? 'Active'
                          : item.status === Status.INACTIVE
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
                            setIsModalOpen(ModalType.VIEW);
                            setSelectedItem(item);
                          }}
                          title="View Details"
                        >
                          <Eye className="size-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setIsModalOpen(ModalType.EDIT);
                            setSelectedItem(item);
                          }}
                          title="Edit"
                        >
                          <Edit className="size-4" />
                        </Button>
                        {/* <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleStatusToggle(item)}
                        title={
                          item.status === Status.ACTIVE
                            ? 'Close Poll/Survey'
                            : 'Activate Poll/Survey'
                        }
                      >
                        <Power className="h-4 w-4" />
                      </Button> 
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            setSelectedItem(item);
                            setDeleteDialogOpen(true);
                          }}
                          title="Delete"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button> */}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}

          <div className="flex justify-end pt-4">
            <Pagination
              total={data?.total}
              perPage={data?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      {isModalOpen === ModalType.VIEW && (
        <ViewPollSurvey
          isOpen={isModalOpen === ModalType.VIEW}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          selectedItem={selectedItem as IPollSurvey}
        />
      )}
      {isModalOpen === ModalType.ADD && (
        <ActionPollSurvey
          isOpen={isModalOpen === ModalType.ADD}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          onSubmit={fetchData}
        />
      )}
      {isModalOpen === ModalType.EDIT && (
        <ActionPollSurvey
          isOpen={isModalOpen === ModalType.EDIT}
          onClose={() => setIsModalOpen(ModalType.NONE)}
          id={selectedItem?.id}
          onSubmit={fetchData}
        />
      )}

      <ConfirmDialog
        isOpen={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleDeleteConfirm}
        message={`Are you sure you want to delete "${selectedItem?.title}"? This action cannot be undone.`}
        buttonText="Delete"
        loading={false}
        loadingText="Deleting..."
      />
    </div>
  );
};

export default PollSurvey;
