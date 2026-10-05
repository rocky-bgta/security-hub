import { Edit, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';

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
import ActionBillings from 'features/billing-action/ActionBillings';
import DeleteBillings from 'features/billing-action/DeleteBillings';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IBillingAction } from 'models/Billing';
import { API_END_POINTS } from 'routes/APIEndpoints';

const BillingActions = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<IBillingAction>>([]);
  const [selectedBilling, setSelectedBilling] = useState<IBillingAction | null>(
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
      const response: IResponse<Array<IBillingAction>> = await apiClient.get(
        API_END_POINTS.GET_BILLING_ACTION_LIST,
      );
      setData(response.data);
    } catch (error) {
      console.error('Error fetching billing data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitBillingAction = (billingAction: IBillingAction) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, billingAction];
      } else {
        return [
          ...prevData.map(r => (r.id === billingAction.id ? billingAction : r)),
        ];
      }
    });

    setSelectedBilling(null);
    setActionType(null);
  };

  const handleDeleteBillingAction = () => {
    toast.success(
      `Billing ${selectedBilling?.name} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedBilling?.id),
    ]);
    setSelectedBilling(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          Billing Actions Management
        </h1>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Billing Action
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Billing Actions List
          </CardTitle>
          <CardDescription>View and manage all billing actions</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Serial Number
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Action Name
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
                    {loading ? 'Loading...' : 'No billing actions found'}
                  </TableCell>
                </TableRow>
              ) : (
                data?.map((billing, index) => (
                  <TableRow key={billing.id}>
                    <TableCell className="font-medium text-foreground">
                      {index + 1}
                    </TableCell>
                    <TableCell className="font-medium text-foreground">
                      {billing.name}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedBilling(billing);
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
                            setSelectedBilling(billing);
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

      <ActionBillings
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedBilling(null);
        }}
        billingAction={selectedBilling}
        onSubmit={handleSubmitBillingAction}
      />

      {selectedBilling && (
        <DeleteBillings
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          billingAction={selectedBilling}
          onDeleteBillingAction={handleDeleteBillingAction}
        />
      )}
    </div>
  );
};

export default BillingActions;
