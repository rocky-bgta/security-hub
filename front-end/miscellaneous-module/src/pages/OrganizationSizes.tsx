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
import ActionOrganizationSize from 'features/organization-sizes/ActionOrganizationSize';
import DeleteOrganizationSize from 'features/organization-sizes/DeleteOrganizationSize';
import { useAPI } from 'hooks/UseAPI';
import { IResponse } from 'models/Global';
import { IOrganizationSize } from 'models/OrganizationSize';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime } from 'utils/Helper';

const OrganizationSizes = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<IOrganizationSize>>([]);
  const [selectedOrganizationSize, setSelectedOrganizationSize] =
    useState<IOrganizationSize | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const response: IResponse<Array<IOrganizationSize>> = await apiClient.get(
        API_END_POINTS.GET_ORGANIZATION_SIZE_LIST,
      );
      setData(response.data);
    } catch (error) {
      console.error('Error fetching organization size data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitOrganizationSize = (
    organizationSize: IOrganizationSize,
  ) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, organizationSize];
      } else {
        return [
          ...prevData.map(r =>
            r.id === organizationSize.id ? organizationSize : r,
          ),
        ];
      }
    });

    setSelectedOrganizationSize(null);
    setActionType(null);
  };

  const handleDeleteOrganizationSize = () => {
    toast.success(
      `Organization Size ${selectedOrganizationSize?.name} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedOrganizationSize?.id),
    ]);
    setSelectedOrganizationSize(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold text-foreground">
          Organization Sizes Management
        </h1>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Organization Size
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Organization Size List
          </CardTitle>
          <CardDescription>
            View and manage all organization sizes
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Organization Size Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Organization Size Range
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
                    {loading ? 'Loading...' : 'No organization sizes found'}
                  </TableCell>
                </TableRow>
              ) : (
                data?.map(organizationSize => (
                  <TableRow key={organizationSize.id}>
                    <TableCell className="font-medium text-foreground">
                      {organizationSize.name}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {organizationSize.range}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(organizationSize.createdAt as string)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(organizationSize.updatedAt as string)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedOrganizationSize(organizationSize);
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
                            setSelectedOrganizationSize(organizationSize);
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

      <ActionOrganizationSize
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedOrganizationSize(null);
        }}
        organizationSize={selectedOrganizationSize}
        onSubmit={handleSubmitOrganizationSize}
      />

      {selectedOrganizationSize && (
        <DeleteOrganizationSize
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          organizationSize={selectedOrganizationSize}
          onDeleteOrganizationSize={handleDeleteOrganizationSize}
        />
      )}
    </div>
  );
};

export default OrganizationSizes;
