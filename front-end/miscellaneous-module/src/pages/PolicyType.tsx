import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { IList, Status } from 'models/Global';
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
import ActionPolicyTypes from 'features/policy-type/ActionPolicyTypes';
import { useAPI } from 'hooks/UseAPI';
import { IPolicyTypes } from 'models/PolicyTypes';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime } from 'utils/Helper';
import DeletePolicyTypes from 'features/policy-type/DeletePolicyType';

const PolicyType = () => {
  const [loading, setLoading] = useState<boolean>(true);
  // const [data, setData] = useState<Array<IPolicyTypes>>([]);
  const [data, setData] = useState<IList<IPolicyTypes>>({
    items: [],
    offset: 0,
    pageSize: 0,
    total: 0,
  });
  const [selectedPolicyTypes, setSelectedPolicyTypes] =
    useState<IPolicyTypes | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  console.log('policy-types-data', data);

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      // const response: IResponse<Array<IPolicyTypes>> = await apiClient.get(
      //   API_END_POINTS.GET_POLICY_TYPE_LIST,
      // );
      const response = await apiClient.get(API_END_POINTS.GET_POLICY_TYPE_LIST);
      setData(response.data);
      // const list = Array.isArray(response?.data) ? response.data : [];
      // setData(list);
    } catch (error) {
      console.error('Error fetching policy type data:', error);
      // setData([]);
    } finally {
      setLoading(false);
    }
  };

  // const handleSubmitPolicyTypes = (policyTypes: IPolicyTypes) => {
  //   setData(prevData => {
  //     if (actionType === 'create') {
  //       return [...prevData, policyTypes];
  //     } else {
  //       return [
  //         ...prevData.map(r => (r.id === policyTypes.id ? policyTypes : r)),
  //       ];
  //     }
  //   });

  const handleSubmitPolicyTypes = (policyTypes: IPolicyTypes) => {
    setData(prevData => {
      if (actionType === 'create') {
        return {
          ...prevData,
          items: [...prevData.items, policyTypes],
          total: prevData.total + 1,
        };
      } else {
        return {
          ...prevData,
          items: prevData.items.map(r =>
            r.id === policyTypes.id ? policyTypes : r,
          ),
        };
      }
    });

    setSelectedPolicyTypes(null);
    setActionType(null);
  };

  const handleDeletePolicyTypes = () => {
    toast.success(
      `Policy Type ${selectedPolicyTypes?.name} has been successfully deleted.`,
    );
    setData(prevData => ({
      ...prevData,
      items: prevData.items.filter(r => r.id !== selectedPolicyTypes?.id),
      total: Math.max(0, prevData.total - 1),
    }));
    setSelectedPolicyTypes(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          Policy Type Management
        </h1>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Policy Type
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">Policy List</CardTitle>
          <CardDescription>View and manage all Policy Type</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Policy Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Policy Code
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
              {loading || data.items.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={5}
                    className="text-center text-muted-foreground"
                  >
                    {loading ? 'Loading...' : 'No policy type found'}
                  </TableCell>
                </TableRow>
              ) : (
                data.items?.map(policyTypes => (
                  <TableRow key={policyTypes.id}>
                    <TableCell className="font-medium text-foreground">
                      {policyTypes.name}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {policyTypes.code}
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          String(policyTypes?.status || '') === Status.ACTIVE ||
                          String(policyTypes?.status || '') === 'ACTIVE'
                            ? 'default'
                            : String(policyTypes?.status || '') ===
                                  Status.INACTIVE ||
                                String(policyTypes?.status || '') === 'INACTIVE'
                              ? 'secondary'
                              : 'outline'
                        }
                        className={
                          String(policyTypes?.status || '') ===
                            Status.INACTIVE ||
                          String(policyTypes?.status || '') === 'INACTIVE'
                            ? 'border border-card-border bg-muted text-muted-foreground'
                            : ''
                        }
                      >
                        {String(policyTypes?.status || '') === Status.ACTIVE ||
                        String(policyTypes?.status || '') === 'ACTIVE'
                          ? 'Active'
                          : String(policyTypes?.status || '') ===
                                Status.INACTIVE ||
                              String(policyTypes?.status || '') === 'INACTIVE'
                            ? 'Inactive'
                            : 'Draft'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(policyTypes.createdAt as string)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(policyTypes.updatedAt as string)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedPolicyTypes(policyTypes);
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
                            setSelectedPolicyTypes(policyTypes);
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

      <ActionPolicyTypes
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedPolicyTypes(null);
        }}
        policyTypes={selectedPolicyTypes}
        onSubmit={handleSubmitPolicyTypes}
      />

      {selectedPolicyTypes && (
        <DeletePolicyTypes
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          policyTypes={selectedPolicyTypes}
          onDeletePolicyTypes={handleDeletePolicyTypes}
        />
      )}
    </div>
  );
};

export default PolicyType;
