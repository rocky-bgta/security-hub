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
import ActionStates from 'features/states/ActionStates';
import DeleteStates from 'features/states/DeleteStates';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IState } from 'models/States';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime } from 'utils/Helper';

const States = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<IState>>([]);
  const [selectedState, setSelectedState] = useState<IState | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response: IResponse<Array<IState>> = await apiClient.get(
        API_END_POINTS.GET_STATE_LIST,
      );
      setData(response.data.sort((a, b) => a.displayOrder - b.displayOrder));
    } catch (error) {
      console.error('Error fetching state data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitState = (state: IState) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, state];
      } else {
        return [...prevData.map(r => (r.id === state.id ? state : r))];
      }
    });

    setSelectedState(null);
    setActionType(null);
  };

  const handleDeleteState = () => {
    toast.success(
      `State ${selectedState?.name} has been successfully deleted.`,
    );
    setData(prevData => [...prevData.filter(r => r.id !== selectedState?.id)]);
    setSelectedState(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          States Management
        </h1>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add State
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">State List</CardTitle>
          <CardDescription>View and manage all states</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  State Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  State Code
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Display Order
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
                    colSpan={5}
                    className="text-center text-muted-foreground"
                  >
                    {loading ? 'Loading...' : 'No state found'}
                  </TableCell>
                </TableRow>
              ) : (
                data?.map(state => (
                  <TableRow key={state.id}>
                    <TableCell className="font-medium text-foreground">
                      {state.name}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {state.code}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {state.displayOrder}
                    </TableCell>
                    <TableCell>
                      <Badge variant={state.active ? 'default' : 'secondary'}>
                        {state.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(state.createdAt as string)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(state.updatedAt as string)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedState(state);
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
                            setSelectedState(state);
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

      <ActionStates
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedState(null);
        }}
        state={selectedState}
        onSubmit={handleSubmitState}
      />

      {selectedState && (
        <DeleteStates
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          state={selectedState}
          onDeleteState={handleDeleteState}
        />
      )}
    </div>
  );
};

export default States;
