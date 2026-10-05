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
import ActionOrganizationType from 'features/organization-types/ActionOrganizationType';
import DeleteOrganizationType from 'features/organization-types/DeleteOrganizationType';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IOrganizationType } from 'models/OrganizationType';
import { API_END_POINTS } from 'routes/APIEndpoints';

const OrganizationTypes = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<IOrganizationType>>([]);
  const [selectedOrganizationType, setSelectedOrganizationType] =
    useState<IOrganizationType | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response: IResponse<Array<IOrganizationType>> = await apiClient.get(
        API_END_POINTS.GET_ORGANIZATION_TYPE_LIST,
      );
      setData(response.data);
    } catch (error) {
      console.error('Error fetching organization type data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitOrganizationType = (
    organizationType: IOrganizationType,
  ) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, organizationType];
      } else {
        return [
          ...prevData.map(r =>
            r.id === organizationType.id ? organizationType : r,
          ),
        ];
      }
    });

    setSelectedOrganizationType(null);
    setActionType(null);
  };

  const handleDeleteOrganizationType = () => {
    toast.success(
      `Organization Type ${selectedOrganizationType?.organizationType} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedOrganizationType?.id),
    ]);
    setSelectedOrganizationType(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          Organization Types Management
        </h1>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Organization Type
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Organization Type List
          </CardTitle>
          <CardDescription>
            View and manage all organization types
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Organization Type Name
                </TableHead>
                {/* <TableHead className="font-semibold text-foreground">
                  Created
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Updated
                </TableHead> */}
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
                    {loading ? 'Loading...' : 'No organization types found'}
                  </TableCell>
                </TableRow>
              ) : (
                data?.map(organizationType => (
                  <TableRow key={organizationType.id}>
                    <TableCell className="font-medium text-foreground">
                      {organizationType.organizationType}
                    </TableCell>
                    {/* <TableCell className="text-muted-foreground">
                      {formateDateAndTime(organizationType.createdAt)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(organizationType.updatedAt)}
                    </TableCell> */}
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedOrganizationType(organizationType);
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
                            setSelectedOrganizationType(organizationType);
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

      <ActionOrganizationType
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedOrganizationType(null);
        }}
        organizationType={selectedOrganizationType}
        onSubmit={handleSubmitOrganizationType}
      />

      {selectedOrganizationType && (
        <DeleteOrganizationType
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          organizationType={selectedOrganizationType}
          onDeleteOrganizationType={handleDeleteOrganizationType}
        />
      )}
    </div>
  );
};

export default OrganizationTypes;
