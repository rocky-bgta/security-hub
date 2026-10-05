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
import ActionCountry from 'features/country/ActionCountry';
import DeleteCountry from 'features/country/DeleteCountry';
import { useAPI } from 'hooks/UseAPI';
import { ICountry } from 'models/Country';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formateDateAndTime } from 'utils/Helper';

const Country = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<Array<ICountry>>([]);

  const [selectedCountry, setSelectedCountry] = useState<ICountry | null>(null);
  const [actionType, setActionType] = useState<'create' | 'edit' | null>(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState<boolean>(false);

  const apiClient = useAPI();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response: IResponse<Array<ICountry>> = await apiClient.get(
          API_END_POINTS.GET_COUNTRY_LIST,
        );
        setData(response.data.sort((a, b) => a.displayOrder - b.displayOrder));
      } catch (error) {
        console.error('Error fetching country data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [apiClient]);

  const handleSubmitCountry = (country: ICountry) => {
    setData(prevData => {
      if (actionType === 'create') {
        return [...prevData, country];
      } else {
        return [...prevData.map(r => (r.id === country.id ? country : r))];
      }
    });

    setSelectedCountry(null);
    setActionType(null);
  };

  const handleDeleteCountry = () => {
    toast.success(
      `Country ${selectedCountry?.name} has been successfully deleted.`,
    );
    setData(prevData => [
      ...prevData.filter(r => r.id !== selectedCountry?.id),
    ]);
    setSelectedCountry(null);
    setShowDeleteDialog(false);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            Country Management
          </h1>
          <p className="text-muted-foreground">
            Manage countries and their details
          </p>
        </div>
        <Button
          onClick={() => setActionType('create')}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          <Plus className="mr-2 size-4" />
          Add Country
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            Country List
          </CardTitle>
          <CardDescription>View and manage all countries</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="font-semibold text-foreground">
                  Country Name
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Country Code
                </TableHead>
                <TableHead className="font-semibold text-foreground">
                  Phone Code
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
                    colSpan={8}
                    className="text-center text-muted-foreground"
                  >
                    {loading ? 'Loading...' : 'No countries found'}
                  </TableCell>
                </TableRow>
              ) : (
                data.map(country => (
                  <TableRow key={country.id}>
                    <TableCell className="font-medium text-foreground">
                      {country.name}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {country.code}
                    </TableCell>
                    <TableCell className="max-w-xs text-muted-foreground">
                      {country.phoneCode}
                    </TableCell>
                    <TableCell>{country.displayOrder}</TableCell>
                    <TableCell>
                      <Badge variant={country.active ? 'default' : 'secondary'}>
                        {country.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(country.createdAt)}
                    </TableCell>
                    <TableCell className="text-muted-foreground">
                      {formateDateAndTime(country.updatedAt)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center justify-center gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            setSelectedCountry(country);
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
                            setSelectedCountry(country);
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

      <ActionCountry
        isOpen={actionType !== null}
        onClose={() => {
          setActionType(null);
          setSelectedCountry(null);
        }}
        country={selectedCountry}
        onSubmit={handleSubmitCountry}
      />

      {selectedCountry && (
        <DeleteCountry
          isOpen={showDeleteDialog}
          setIsOpen={setShowDeleteDialog}
          country={selectedCountry}
          onDeleteCountry={handleDeleteCountry}
        />
      )}
    </div>
  );
};

export default Country;
