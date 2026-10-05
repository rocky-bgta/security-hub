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
import ActionCompliance from 'features/compliance/ActionCompliance';
import DeleteCompliance from 'features/compliance/DeleteCompliance';
import { useAPI } from 'hooks/UseAPI';
import { ICompliance } from 'models/Compliance';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';

const Compliance = () => {
  const [data, setData] = useState<Array<ICompliance>>([]);

  const [selectedCompliance, setSelectedCompliance] =
    useState<ICompliance | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    fetchComplianceData();
  }, []);

  const fetchComplianceData = async () => {
    try {
      const response: IResponse<Array<ICompliance>> = await apiClient.get(
        API_END_POINTS.GET_COMPLIANCE_LIST,
      );
      setData(response.data);
    } catch (error) {
      console.error('Error fetching compliance data:', error);
    }
  };

  const handleSubmitCompliance = (compliance: ICompliance) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, compliance];
      } else {
        return [
          ...prevData.map(r => (r.id === compliance.id ? compliance : r)),
        ];
      }
    });

    setSelectedCompliance(null);
    setActionType(null);
  };

  const handleDeleteCompliance = () => {
    toast.success(
      `Compliance ${selectedCompliance?.complianceName} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedCompliance?.id),
    ]);
    setSelectedCompliance(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Compliance Management
          </h1>
          <p className="text-muted-foreground">
            Manage compliance requirements and statuses
          </p>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Compliance
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Compliance List
          </CardTitle>
          <CardDescription>
            View and manage all compliance requirements
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border border-card-border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="font-semibold text-foreground">
                    Compliance Name
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Description
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Acronym
                  </TableHead>
                  <TableHead className="font-semibold text-foreground">
                    Sort Order
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
                      colSpan={7}
                      className="text-center text-muted-foreground"
                    >
                      No compliance found
                    </TableCell>
                  </TableRow>
                ) : (
                  data.map(compliance => (
                    <TableRow key={compliance.id}>
                      <TableCell className="font-medium text-foreground">
                        {compliance.complianceName}
                      </TableCell>
                      <TableCell className="max-w-xs text-muted-foreground">
                        {compliance.description}
                      </TableCell>
                      <TableCell className="max-w-xs text-muted-foreground">
                        {compliance.acronym}
                      </TableCell>
                      <TableCell>{compliance.sortOrder}</TableCell>
                      <TableCell>
                        <Badge
                          variant={
                            compliance.isActive ? 'default' : 'secondary'
                          }
                          className={
                            compliance.isActive ? 'bg-primary text-white' : ''
                          }
                        >
                          {compliance.isActive ? 'Active' : 'Inactive'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-muted-foreground">
                        {new Date(
                          compliance.createdAt as string,
                        ).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedCompliance(compliance);
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
                              setSelectedCompliance(compliance);
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

      <ActionCompliance
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedCompliance(null);
        }}
        compliance={selectedCompliance}
        onSubmit={handleSubmitCompliance}
      />

      {selectedCompliance && (
        <DeleteCompliance
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          compliance={selectedCompliance}
          onDeleteCompliance={handleDeleteCompliance}
        />
      )}
    </div>
  );
};

export default Compliance;
