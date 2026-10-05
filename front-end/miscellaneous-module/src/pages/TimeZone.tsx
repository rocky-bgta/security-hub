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
import ActionTimeZone from 'features/time-zone/ActionTimeZone';
import DeleteTimeZone from 'features/time-zone/DeleteTimeZone';
import { useAPI } from 'hooks/UseAPI';
import { Edit, Plus, Trash2 } from 'lucide-react';
import { IResponse } from 'models/Global';
import { ITimeZone } from 'models/TimeZone';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime } from 'utils/Helper';

const TimeZone = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<ITimeZone>>([]);
  const [selectedTimeZone, setSelectedTimeZone] = useState<ITimeZone | null>(
    null,
  );
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response: IResponse<Array<ITimeZone>> = await apiClient.get(
        API_END_POINTS.GET_TIME_ZONE_LIST,
      );
      setData(response.data.sort((a, b) => a.displayOrder - b.displayOrder));
    } catch (error) {
      console.error('Error fetching time zone data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitTimeZone = (timeZone: ITimeZone) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, timeZone];
      } else {
        return [...prevData.map(r => (r.id === timeZone.id ? timeZone : r))];
      }
    });

    setSelectedTimeZone(null);
    setActionType(null);
  };

  const handleDeleteTimeZone = () => {
    toast.success(
      `Time Zone ${selectedTimeZone?.displayName} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedTimeZone?.id),
    ]);
    setSelectedTimeZone(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Time Zone Management
          </h1>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Time Zone
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Time Zone List
          </CardTitle>
          <CardDescription>View and manage all time zones</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Time Zone Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Time Zone Code
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
                    {loading ? 'Loading...' : 'No time zone found'}
                  </TableCell>
                </TableRow>
              ) : (
                data?.map(timeZone => (
                  <TableRow key={timeZone.id}>
                    <TableCell className="font-medium text-foreground">
                      {timeZone.displayName}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {timeZone.timezoneId}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {timeZone.displayOrder}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={timeZone.active ? 'default' : 'secondary'}
                      >
                        {timeZone.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(timeZone.createdAt as string)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(timeZone.updatedAt as string)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedTimeZone(timeZone);
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
                            setSelectedTimeZone(timeZone);
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

      <ActionTimeZone
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedTimeZone(null);
        }}
        timeZone={selectedTimeZone}
        onSubmit={handleSubmitTimeZone}
      />

      {selectedTimeZone && (
        <DeleteTimeZone
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          timeZone={selectedTimeZone}
          onDeleteTimeZone={handleDeleteTimeZone}
        />
      )}
    </div>
  );
};

export default TimeZone;
