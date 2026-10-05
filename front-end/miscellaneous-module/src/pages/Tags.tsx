import { Info, Plus, Power, Trash2 } from 'lucide-react';
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
import ConfirmDialog from 'components/ConfirmDialog';
import TableLoader from 'components/skeleton/TableLoader';
import ActionTag from 'features/tag/ActionTag';
import { useAPI } from 'hooks/UseAPI';
import { IResponse, Status } from 'models/Global';
import { ITag } from 'models/Tag';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime, isSuccessResponse } from 'utils/Helper';

const isActiveStatus = (status?: string) =>
  String(status || '').toUpperCase() === Status.ACTIVE;

const Tags = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [statusLoading, setStatusLoading] = useState<boolean>(false);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const [data, setData] = useState<Array<ITag>>([]);
  const [selectedTag, setSelectedTag] = useState<ITag | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showStatusDialog, setShowStatusDialog] = useState<boolean>(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchTagData();
  }, []);

  const fetchTagData = async () => {
    setLoading(true);
    try {
      const response: IResponse<Array<ITag>> = await apiClient.get(
        API_END_POINTS.GET_TAG_LIST,
      );
      if (isSuccessResponse(response.statusCode)) {
        setData(Array.isArray(response.data) ? response.data : []);
      } else {
        toast.error('Failed to fetch product tags');
      }
    } catch (error) {
      console.error('Error fetching tag data:', error);
      toast.error('Failed to fetch product tags');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitTag = (tag: ITag) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, tag];
      }
      return prevData.map(item => (item.id === tag.id ? tag : item));
    });

    setSelectedTag(null);
    setActionType(null);
  };

  const handleStatusConfirm = async () => {
    if (!selectedTag) return;

    const nextStatus = isActiveStatus(selectedTag.status)
      ? Status.INACTIVE
      : Status.ACTIVE;

    setStatusLoading(true);
    try {
      const response: IResponse<ITag> = await apiClient.put(
        API_END_POINTS.UPDATE_TAG_STATUS.replace(':id', selectedTag.id),
        {
          data: { status: nextStatus },
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        setData(prevData =>
          prevData.map(item =>
            item.id === selectedTag.id
              ? {
                  ...item,
                  ...(response.data ?? {}),
                  status: response.data?.status || nextStatus,
                }
              : item,
          ),
        );
        toast.success(
          `Product tag ${nextStatus === Status.ACTIVE ? 'activated' : 'deactivated'} successfully`,
        );
        setShowStatusDialog(false);
        setSelectedTag(null);
      } else {
        toast.error('Failed to update product tag status');
      }
    } catch (error) {
      console.error('Error updating tag status:', error);
      toast.error('Failed to update product tag status');
    } finally {
      setStatusLoading(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!selectedTag) return;

    setDeleteLoading(true);
    try {
      const response: IResponse<null> = await apiClient.del(
        API_END_POINTS.DELETE_TAG.replace(':id', selectedTag.id),
      );

      if (isSuccessResponse(response.statusCode)) {
        setData(prevData => prevData.filter(item => item.id !== selectedTag.id));
        toast.success('Product tag deleted successfully');
        setShowDeleteDialog(false);
        setSelectedTag(null);
      } else {
        toast.error(response.message || 'Failed to delete product tag');
      }
    } catch (error) {
      console.error('Error deleting tag:', error);
      toast.error('Failed to delete product tag');
    } finally {
      setDeleteLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Product Tag Management
          </h1>
          <p className="text-muted-foreground">
            Manage product tags for organizing content and resources
          </p>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Product Tag
        </Button>
      </div>

      <div className="flex items-start gap-3 rounded-lg border border-primary/30 bg-primary/10 p-4">
        <Info className="mt-0.5 size-5 shrink-0 text-primary" />
        <p className="text-sm text-muted-foreground">
          Admins must create and keep these tags:{' '}
          <span className="font-medium text-foreground">
            Security, Phishing, Vishing, Smishing, Deepfake
          </span>
          . Do not change these names.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Product Tag List
          </CardTitle>
          <CardDescription>View and manage all product tags</CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <TableLoader count={10} />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="font-semibold text-foreground">
                    Name
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
                  <TableHead className="text-center font-semibold text-foreground">
                    Actions
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={6}
                      className="text-center text-muted-foreground"
                    >
                      No product tags found
                    </TableCell>
                  </TableRow>
                ) : (
                  data.map(tag => (
                    <TableRow key={tag.id}>
                      <TableCell className="font-medium text-foreground">
                        {tag.name}
                      </TableCell>
                      <TableCell className="max-w-xs text-muted-foreground">
                        {tag.description || '-'}
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant={
                            isActiveStatus(tag.status) ? 'default' : 'secondary'
                          }
                          className={
                            isActiveStatus(tag.status)
                              ? 'bg-primary text-white'
                              : ''
                          }
                        >
                          {isActiveStatus(tag.status) ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {formateDateAndTime(tag.createdAt)}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-2">
                          {/* <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedTag(tag);
                              setActionType('edit');
                            }}
                            className="hover:bg-muted"
                            title="Edit product tag"
                          >
                            <Edit className="size-4" />
                          </Button> */}
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedTag(tag);
                              setShowStatusDialog(true);
                            }}
                            className="hover:bg-muted"
                            title={
                              isActiveStatus(tag.status)
                                ? 'Deactivate product tag'
                                : 'Activate product tag'
                            }
                          >
                            <Power className="size-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedTag(tag);
                              setShowDeleteDialog(true);
                            }}
                            className="hover:bg-destructive/20 hover:text-destructive"
                            title="Delete product tag"
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

      <ActionTag
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedTag(null);
        }}
        tag={selectedTag}
        onSubmit={handleSubmitTag}
      />

      {selectedTag && (
        <ConfirmDialog
          isOpen={showStatusDialog}
          message={`Are you sure you want to ${
            isActiveStatus(selectedTag.status) ? 'deactivate' : 'activate'
          } the product tag "${selectedTag.name}"?`}
          loading={statusLoading}
          loadingText="Updating..."
          buttonText={
            isActiveStatus(selectedTag.status) ? 'Deactivate' : 'Activate'
          }
          onClose={() => {
            setShowStatusDialog(false);
            setSelectedTag(null);
          }}
          onConfirm={handleStatusConfirm}
        />
      )}

      {selectedTag && (
        <ConfirmDialog
          isOpen={showDeleteDialog}
          message={`Are you sure you want to delete the product tag "${selectedTag.name}"?`}
          loading={deleteLoading}
          loadingText="Deleting..."
          buttonText="Delete"
          onClose={() => {
            setShowDeleteDialog(false);
            setSelectedTag(null);
          }}
          onConfirm={handleDeleteConfirm}
        />
      )}
    </div>
  );
};

export default Tags;
