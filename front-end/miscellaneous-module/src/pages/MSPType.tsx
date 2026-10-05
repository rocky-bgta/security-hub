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
import ActionMSPType from 'features/msp-type/ActionMSPType';
import DeleteMSPType from 'features/msp-type/DeleteMSPType';
import { useAPI } from 'hooks/UseAPI';
import { IMSPType } from 'models/MSPType';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime } from 'utils/Helper';

const MSPType = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<IMSPType>>([]);

  const [selectedMSPType, setSelectedMSPType] = useState<IMSPType | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response: IResponse<Array<IMSPType>> = await apiClient.get(
        API_END_POINTS.GET_MSP_TYPE_LIST,
      );
      setData(Array.isArray(response.data) ? response.data : []);
    } catch (error) {
      console.error('Error fetching MSP Type data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitMSPType = (mspType: IMSPType) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, mspType];
      } else {
        return prevData.map(r => (r.id === mspType.id ? mspType : r));
      }
    });

    setSelectedMSPType(null);
    setActionType(null);
  };

  const handleDeleteMSPType = () => {
    toast.success(
      `MSP Type ${selectedMSPType?.name} has been successfully deleted.`,
    );
    setData(prevData => prevData.filter(r => r.id !== selectedMSPType?.id));
    setSelectedMSPType(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            MSP Type Management
          </h1>
          <p className="text-muted-foreground">
            Manage MSP types and their details
          </p>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add MSP Type
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            MSP Type List
          </CardTitle>
          <CardDescription>View and manage all MSP types</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  MSP Type Name
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
                    {loading ? 'Loading...' : 'No MSP types found'}
                  </TableCell>
                </TableRow>
              ) : (
                data.map(mspType => (
                  <TableRow key={mspType.id}>
                    <TableCell className="font-medium text-foreground">
                      {mspType.name}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={mspType.isActive ? 'default' : 'secondary'}
                      >
                        {mspType.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(mspType.createdAt)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(mspType.updatedAt)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedMSPType(mspType);
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
                            setSelectedMSPType(mspType);
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

      <ActionMSPType
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedMSPType(null);
        }}
        mspType={selectedMSPType}
        onSubmit={handleSubmitMSPType}
      />

      {selectedMSPType && (
        <DeleteMSPType
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          mspType={selectedMSPType}
          onDeleteMSPType={handleDeleteMSPType}
        />
      )}
    </div>
  );
};

export default MSPType;
