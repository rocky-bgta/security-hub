import { Edit, Eye, Play, Plus, Trash2, Trophy } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'components/common/Table';
import ConfirmDialog from 'components/ConfirmDialog';
import TableLoader from 'components/skeleton/TableLoader';
import Pagination from 'common/Pagination';
import ActionLeaderboardModal from 'features/leader-board/ActionLeaderboardModal';
import ViewLeaderboardModal from 'features/leader-board/ViewLeaderboardModal';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { ModalType, Status, VideoType, IList, IGetListParams } from 'models/Global';
import { ILeaderBoard } from 'models/LeaderBoard';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  formateDateAndTime,
  isSuccessResponse,
  objectToQueryString,
  sliceWords,
} from 'utils/Helper';

const MSPAdminLeaderboard = () => {
  const apiClient = useAPI();
  const [leaderboardData, setLeaderboardData] = useState<IList<ILeaderBoard>>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [modalType, setModalType] = useState<ModalType>(ModalType.NONE);
  const [selectedEntry, setSelectedEntry] = useState<ILeaderBoard | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [openConfirmDialog, setOpenConfirmDialog] = useState(false);
  const [selectedEntryId, setSelectedEntryId] = useState('');
  const [loading, setLoading] = useState(true);
  const [queryString, setQueryString] = useState('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    pageSize: 10,
    offset: 0,
  });
  const searchDebounce = useDebounce(queryString, 500);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchLeaderboardData();
    }
  }, [searchDebounce]);

  const fetchLeaderboardData = async () => {
    setLoading(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_LEADERBOARD_LIST + queryString,
      );
      if (isSuccessResponse(response.statusCode)) {
        setLeaderboardData(response.data);
      }
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditEntry = (entry: ILeaderBoard) => {
    setSelectedEntry(entry);
    setModalType(ModalType.EDIT);
  };

  const handleViewEntry = (entry: ILeaderBoard) => {
    setSelectedEntry(entry);
    setModalType(ModalType.VIEW);
  };

  const handleCloseActionModal = () => {
    setModalType(ModalType.NONE);
    setSelectedEntry(null);
  };

  const handleRemoveEntry = async (entryId: string) => {
    try {
      setIsDeleting(true);
      const response = await apiClient.del(
        API_END_POINTS.DELETE_LEADERBOARD.replace(':id', entryId),
      );
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Leader entry has been successfully removed.');
        fetchLeaderboardData();
        setOpenConfirmDialog(false);
        setSelectedEntryId('');
      } else {
        toast.error('You can only remove your own leaderboards');
        setOpenConfirmDialog(false);
      }
    } catch {
      toast.error('Failed to remove leader entry.');
    } finally {
      setIsDeleting(false);
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
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Leader Message Management
          </h1>
          <p className="text-muted-foreground">
            Manage leader message with videos, titles, and achievements
          </p>
        </div>

        <Button onClick={() => setModalType(ModalType.ADD)}>
          <Plus className="mr-2 size-4" />
          Add New Leader Message
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl font-bold text-white">
              {leaderboardData?.total}
            </div>
            <div className="text-sm text-muted-foreground">Total Leaders</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl font-bold text-white">
              {
                leaderboardData?.items?.filter(
                  (entry: ILeaderBoard) => entry.status === Status.ACTIVE,
                ).length
              }
            </div>
            <div className="text-sm text-muted-foreground">Active Leaders</div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl font-bold text-white">
              {
                leaderboardData?.items?.filter(
                  entry => entry.status === Status.INACTIVE,
                ).length
              }
            </div>
            <div className="text-sm text-muted-foreground">
              Inactive Leaders
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl font-bold text-white">
              {
                leaderboardData?.items?.filter(
                  entry => entry.videoType === VideoType.UPLOAD_FILE,
                ).length
              }
            </div>
            <div className="text-sm text-muted-foreground">Uploaded Videos</div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Trophy className="size-5" />
            Leader Message Entries
          </CardTitle>
          <CardDescription>
            View and manage all leader message entries with videos and
            achievements
          </CardDescription>
          <div className="mt-4 p-3">
            <p className="text-sm text-white">
              <strong>Note:</strong> Maximum of 2 leader messages can be active
              at the same time.
            </p>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <TableLoader count={10} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Title</TableHead>
                  <TableHead className="text-nowrap">Leader Name</TableHead>
                  <TableHead>Designation</TableHead>
                  <TableHead>Video</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-nowrap">Created By</TableHead>
                  <TableHead className="text-nowrap">Created Date</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {leaderboardData?.total === 0 && (
                  <TableRow>
                    <TableCell
                      colSpan={7}
                      className="text-center text-muted-foreground"
                    >
                      There are no leader message data found
                    </TableCell>
                  </TableRow>
                )}
                {leaderboardData?.items?.length > 0 &&
                  leaderboardData?.items?.map((entry: ILeaderBoard) => (
                    <TableRow key={entry.id}>
                      <TableCell className="w-1/4">
                        <div className="font-medium">
                          {sliceWords(entry?.title ?? '', 15)}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="font-medium">
                          {sliceWords(entry?.name ?? '', 5)}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm text-muted-foreground">
                          {sliceWords(entry?.designation ?? '', 5)}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Play className="size-4 text-primary" />
                          <Badge
                            variant={
                              entry?.videoType === VideoType.UPLOAD_FILE
                                ? 'default'
                                : 'secondary'
                            }
                            className="text-xs"
                          >
                            {entry?.videoType === VideoType.UPLOAD_FILE
                              ? 'Uploaded'
                              : 'URL'}
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Badge
                            variant={
                              entry?.status === Status.ACTIVE
                                ? 'default'
                                : 'destructive'
                            }
                          >
                            {entry?.status === Status.ACTIVE
                              ? 'Active'
                              : 'Inactive'}
                          </Badge>
                        </div>
                      </TableCell>
                      <TableCell>
                        {entry?.isDefault ? (
                          <div className="text-sm text-yellow-500">
                            System Generated
                          </div>
                        ) : (
                          <div className="text-sm text-primary">
                            Admin Generated
                          </div>
                        )}
                      </TableCell>
                      <TableCell>
                        <div className="text-sm text-muted-foreground">
                          {formateDateAndTime(entry?.createdDate ?? '')}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex gap-1">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleViewEntry(entry)}
                          >
                            <Eye className="size-3" />
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            disabled={entry?.isDefault}
                            onClick={() => handleEditEntry(entry)}
                          >
                            <Edit className="size-3" />
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            disabled={isDeleting || entry?.isDefault}
                            onClick={() => {
                              setOpenConfirmDialog(true);
                              setSelectedEntryId(entry?.id ?? '');
                            }}
                          >
                            <Trash2 className="size-3" />
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
              total={leaderboardData?.total}
              perPage={leaderboardData?.pageSize ?? 0}
              onPageChange={handlePageChange}
            />
          </div>
        </CardContent>
      </Card>

      <ActionLeaderboardModal
        isOpen={modalType === ModalType.ADD || modalType === ModalType.EDIT}
        entry={modalType === ModalType.EDIT ? selectedEntry : null}
        onClose={handleCloseActionModal}
        onSubmit={fetchLeaderboardData}
      />

      <ViewLeaderboardModal
        isOpen={modalType === ModalType.VIEW}
        entry={selectedEntry}
        onClose={() => {
          setModalType(ModalType.NONE);
          setSelectedEntry(null);
        }}
      />

      <ConfirmDialog
        isOpen={openConfirmDialog}
        onClose={() => setOpenConfirmDialog(false)}
        onConfirm={() => handleRemoveEntry(selectedEntryId)}
        message="Are you sure you want to remove this leader message entry?"
        loading={isDeleting}
        loadingText="Removing..."
        buttonText="Remove"
      />
    </div>
  );
};

export default MSPAdminLeaderboard;
