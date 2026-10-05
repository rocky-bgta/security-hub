import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Pagination from 'common/Pagination';
import SearchSelect from 'components/SearchSelect';
import { VatDetailsModal } from 'features/vat/VatDetailsModal';
import { VatFormModal } from 'features/vat/VatModal';
import { VatTable } from 'features/vat/VatTable';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { Plus } from 'lucide-react';
import { ICountryDropdown } from 'models/Country';
import { IGetListParams, IList } from 'models/Global';
import { VatConfiguration } from 'models/Vat';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';

const VatList = () => {
  const [data, setData] = useState<IList<VatConfiguration>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    vatRate: '',
    countryFilter: '',
  });
  const searchDebounce = useDebounce(queryString, 1);
  const [loading, setLoading] = useState<boolean>(true);
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [selectedVat, setSelectedVat] = useState<VatConfiguration | null>(null);
  const [editingVat, setEditingVat] = useState<VatConfiguration | null>(null);
  const [countryList, setCountryList] = useState<Array<ICountryDropdown>>([]);
  const [isSubmitLoading, setIsSubmitLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchProductData();
    }
  }, [searchDebounce]);

  const fetchProductData = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.VAT_LIST + searchDebounce,
      );
      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching VAT list:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCountryList = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.COUNTRY_LIST);
      if (isSuccessResponse(response.statusCode)) {
        setCountryList(
          response.data.map((country: ICountryDropdown) => ({
            id: country.id,
            name: country.name,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching country list:', error);
    }
  };

  useEffect(() => {
    fetchCountryList();
  }, []);

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleAdd = () => {
    setEditingVat(null);
    setIsFormModalOpen(true);
  };

  const handleEdit = (vat: VatConfiguration) => {
    setEditingVat(vat);
    setIsFormModalOpen(true);
  };

  const handleViewDetails = (vat: VatConfiguration) => {
    setSelectedVat(vat);
    setIsDetailsModalOpen(true);
  };

  const handleDelete = async (vatId: string) => {
    try {
      await apiClient.del(API_END_POINTS.VAT_DELETE + vatId);
      toast.success('VAT configuration deleted successfully');
      fetchProductData();
    } catch (error) {
      console.error('Error deleting VAT configuration:', error);
    }
  };

  const handleFormSubmit = async (data: VatConfiguration) => {
    setIsSubmitLoading(true);
    if (editingVat) {
      try {
        const response = await apiClient.put(
          API_END_POINTS.VAT_UPDATE + editingVat.id,
          {
            data,
          },
        );
        if (isSuccessResponse(response.statusCode)) {
          toast.success('VAT configuration updated successfully');
          fetchProductData();
          handleResetForm();
        } else {
          toast.error(response.message);
        }
      } catch (error) {
        console.error('Error updating VAT configuration:', error);
        toast.error('Failed to update VAT configuration');
      }
    } else {
      const newVat: VatConfiguration = {
        ...data,
        createdAt: Date.now(),
        updatedAt: Date.now(),
      };
      const response = await apiClient.post(API_END_POINTS.VAT_CREATE, {
        data: newVat,
      });
      if (isSuccessResponse(response.statusCode)) {
        toast.success('VAT configuration added successfully');
        fetchProductData();
        handleResetForm();
      } else {
        toast.error(response.message);
      }
    }
    setIsFormModalOpen(false);
    setEditingVat(null);
    setIsSubmitLoading(false);
  };

  const handleEditFromDetails = (vat: VatConfiguration) => {
    setIsDetailsModalOpen(false);
    setEditingVat(vat);
    setIsFormModalOpen(true);
  };

  const handleResetForm = () => {
    setEditingVat(null);
    setIsFormModalOpen(false);
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          VAT Configuration Management
        </h1>
        <p className="text-muted-foreground">
          Create and manage VAT configurations for your products and services
        </p>
      </div>
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle>VAT Management</CardTitle>
          <Button onClick={handleAdd} className="flex items-center gap-2">
            <Plus className="size-4" />
            Add VAT Configuration
          </Button>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex gap-2">
            <div className="flex flex-col items-start gap-2">
              <Label htmlFor="countryFilter">Filter by Country</Label>
              <div className="w-64">
                <SearchSelect
                  value={queryParams.countryFilter || ''}
                  onValueChange={(value: string) =>
                    setQueryParams(prev => ({ ...prev, countryFilter: value }))
                  }
                  items={countryList.map(country => ({
                    value: country.id,
                    label: country.name,
                  }))}
                  placeholder="Select a country"
                />
              </div>
            </div>
            <div className="flex flex-col items-start gap-2">
              <Label htmlFor="vatRate">VAT Rate</Label>
              <Input
                className="w-64"
                id="vatRate"
                type="text"
                placeholder="Enter VAT Rate"
                value={queryParams.vatRate || ''}
                onChange={e =>
                  setQueryParams(prev => ({
                    ...prev,
                    vatRate: e.target.value,
                  }))
                }
              />
            </div>
          </div>
          <VatTable
            loading={loading}
            data={data.items}
            onEdit={handleEdit}
            onDelete={handleDelete}
            onViewDetails={handleViewDetails}
          />
          <div className="pt-6">
            <Pagination
              total={data.total}
              perPage={data?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>
      {isFormModalOpen && (
        <VatFormModal
          isOpen={isFormModalOpen}
          onClose={() => {
            setIsFormModalOpen(false);
            setEditingVat(null);
          }}
          onSubmit={handleFormSubmit}
          editingData={editingVat}
          handleResetForm={handleResetForm}
          isSubmitLoading={isSubmitLoading}
        />
      )}
      <VatDetailsModal
        isOpen={isDetailsModalOpen}
        onClose={() => {
          setIsDetailsModalOpen(false);
          setSelectedVat(null);
        }}
        data={selectedVat}
        onEdit={handleEditFromDetails}
      />
    </div>
  );
};

export default VatList;
