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
import ActionSuspendReason from 'features/suspend-reason/ActionSuspendReason';
import DeleteSuspendReason from 'features/suspend-reason/DeleteSuspendReason';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { ISuspendReason } from 'models/SuspendReason';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime } from 'utils/Helper';

const SuspendReason = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<ISuspendReason>>([]);
  const [selectedSuspendReason, setSelectedSuspendReason] =
    useState<ISuspendReason | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    fetchSuspendReasonData();
  }, []);

  const fetchSuspendReasonData = async () => {
    try {
      const response: IResponse<Array<ISuspendReason>> = await apiClient.get(
        API_END_POINTS.GET_SUSPEND_REASON_LIST,
      );
      setData(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching suspend reason data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitSuspendReason = (suspendReason: ISuspendReason) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, suspendReason];
      }
      return prevData.map(r => (r.id === suspendReason.id ? suspendReason : r));
    });

    setSelectedSuspendReason(null);
    setActionType(null);
  };

  const handleDeleteSuspendReason = () => {
    toast.success(
      `Suspend reason ${selectedSuspendReason?.name} has been successfully deleted.`,
    );
    setData(prevData =>
      prevData.filter(r => r.id !== selectedSuspendReason?.id),
    );
    setSelectedSuspendReason(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Suspend Reason Management
          </h1>
          <p className="text-muted-foreground">
            Manage suspend reasons for user suspension
          </p>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Suspend Reason
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Suspend Reason List
          </CardTitle>
          <CardDescription>View and manage all suspend reasons</CardDescription>
        </CardHeader>
        <CardContent>
          <div>
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
                  <TableHead className="font-semibold text-foreground">
                    Updated
                  </TableHead>
                  <TableHead className="text-center font-semibold text-foreground">
                    Actions
                  </TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading || data.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={6}
                      className="text-center text-muted-foreground"
                    >
                      {loading ? 'Loading...' : 'No suspend reasons found'}
                    </TableCell>
                  </TableRow>
                ) : (
                  data.map(suspendReason => (
                    <TableRow key={suspendReason.id}>
                      <TableCell className="font-medium text-foreground">
                        {suspendReason.name}
                      </TableCell>
                      <TableCell className="max-w-xs text-muted-foreground">
                        {suspendReason.description || '-'}
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant={
                            suspendReason.active ? 'default' : 'secondary'
                          }
                        >
                          {suspendReason.active ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {formateDateAndTime(suspendReason.createdAt as string)}
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {formateDateAndTime(suspendReason.updatedAt as string)}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedSuspendReason(suspendReason);
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
                              setSelectedSuspendReason(suspendReason);
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
          </div>
        </CardContent>
      </Card>

      <ActionSuspendReason
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedSuspendReason(null);
        }}
        suspendReason={selectedSuspendReason}
        onSubmit={handleSubmitSuspendReason}
      />

      {selectedSuspendReason && (
        <DeleteSuspendReason
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          suspendReason={selectedSuspendReason}
          onDeleteSuspendReason={handleDeleteSuspendReason}
        />
      )}
    </div>
  );
};

export default SuspendReason;
