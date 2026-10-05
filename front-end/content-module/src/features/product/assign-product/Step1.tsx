import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import SearchSelect from 'components/SearchSelect';
import { useAPI } from 'hooks/UseAPI';
import { Info } from 'lucide-react';
import { IClientDropdownData } from 'models/Client';
import { ICountry } from 'models/Configuration';
import { IUserTypeDetails } from 'models/Form';
import { IMSPDropdownData, IMSPErrors } from 'models/Msp';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  data: IUserTypeDetails;
  onUpdate: (data: IUserTypeDetails) => void;
  onNext: () => void;
}

interface IState {
  id: string;
  name: string;
  countryId: string;
  code: string;
}

enum UserType {
  CLIENT = 'CLIENT',
  MSP = 'MSP',
}

const Step1 = ({ data, onUpdate, onNext }: IProps) => {
  const apiClient = useAPI();
  const [formData, setFormData] = useState<IUserTypeDetails>({
    ...data,
  });
  const [selectedType, setSelectedType] = useState<UserType>(
    (data.selectRole as UserType) || UserType.CLIENT,
  );
  const [errors, setErrors] = useState<IMSPErrors>({});
  const [countryList, setCountryList] = useState<
    Array<{ id: string; name: string }>
  >([]);
  const [provinceList, setProvinceList] = useState<
    Array<{ id: string; name: string }>
  >([]);
  const [mspPartners, setMSPPartners] = useState<Array<IMSPDropdownData>>([]);
  const [clients, setClients] = useState<Array<IClientDropdownData>>([]);

  useEffect(() => {
    fetchCountryList();
    fetchMSPPartners();
    fetchClients();
  }, []);

  useEffect(() => {
    if (formData.countryFilter) {
      fetchProvinceList(formData.countryFilter);
    } else {
      // Clear province list when country filter is cleared
      setProvinceList([]);
      setFormData(prev => ({ ...prev, provinceFilter: '' }));
    }
  }, [formData.countryFilter]);

  // Clear relevant fields when user type changes
  useEffect(() => {
    setFormData(prev => ({
      ...prev,
      selectRole: selectedType,
      selectedMSP: selectedType === UserType.CLIENT ? prev.selectedMSP : '',
      selectedClient: selectedType === UserType.MSP ? '' : prev.selectedClient,
    }));
  }, [selectedType]);

  const fetchMSPPartners = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.MSP_LIST);
      if (isSuccessResponse(response.statusCode)) {
        setMSPPartners(
          response.data.items?.map((msp: IMSPDropdownData) => ({
            id: msp.id,
            organizationName: msp.organizationName,
            country: msp.country,
            stateProvince: msp.stateProvince,
          })) || [],
        );
      }
    } catch (error) {
      console.error('Error fetching MSP partners:', error);
      setErrors(prev => ({ ...prev, api: 'Failed to load MSP partners' }));
    }
  };

  const fetchClients = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_LIST + '?offset=0&pageSize=1000',
      );
      if (isSuccessResponse(response.statusCode)) {
        setClients(
          response.data.clientAdmins?.map((client: IClientDropdownData) => ({
            id: client.id,
            organizationName: client.organizationName + ' - ' + client.email,
            country: client.country,
            state: client.state,
            mspId: client.mspId,
          })) || [],
        );
      }
    } catch (error) {
      console.error('Error fetching clients:', error);
      setErrors(prev => ({ ...prev, api: 'Failed to load clients' }));
    }
  };

  const fetchCountryList = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.COUNTRY_LIST);
      if (isSuccessResponse(response.statusCode)) {
        setCountryList(
          response.data?.map((country: ICountry) => ({
            id: country.id,
            name: country.name,
          })) || [],
        );
      }
    } catch (error) {
      console.error('Error fetching country list:', error);
      setErrors(prev => ({ ...prev, api: 'Failed to load countries' }));
    }
  };

  const fetchProvinceList = async (countryId: string) => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.PROVINCE_LIST.replace(':countryId', countryId),
      );
      if (isSuccessResponse(response.statusCode)) {
        setProvinceList(
          response.data?.map((province: IState) => ({
            id: province.id,
            name: province.name,
          })) || [],
        );
      }
    } catch (error) {
      console.error('Error fetching province list:', error);
      setErrors(prev => ({ ...prev, api: 'Failed to load provinces' }));
    }
  };

  const handleInputChange = (
    field: keyof IUserTypeDetails,
    value: string | number | boolean,
  ) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field as keyof IMSPErrors]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: IMSPErrors = {};

    if (selectedType === UserType.CLIENT && !formData.selectedClient) {
      newErrors.selectedClient =
        'Please select a client organization to continue.';
    }

    if (selectedType === UserType.MSP && !formData.selectedMSP) {
      newErrors.selectedMSP = 'Please select an MSP partner';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (validateForm()) {
      onUpdate(formData);
      onNext();
    }
  };

  const filteredMSPs = mspPartners.filter(msp => {
    const countryMatch =
      formData.countryFilter === '' ||
      !formData.countryFilter ||
      msp.country === formData.countryFilter;

    const provinceMatch =
      formData.provinceFilter === '' ||
      !formData.provinceFilter ||
      msp.stateProvince === formData.provinceFilter;

    return countryMatch && provinceMatch;
  });

  const filteredClients = clients.filter(client => {
    const mspMatch =
      formData.selectedMSP === ''
        ? true
        : !formData.selectedMSP || client.mspId === formData.selectedMSP;

    return mspMatch;
  });

  const selectedMSP = mspPartners.find(msp => msp.id === formData.selectedMSP);
  const selectedClient = clients.find(
    client => client.id === formData.selectedClient,
  );

  return (
    <Card className="content-w-full">
      <CardHeader>
        <CardTitle>
          {selectedType === UserType.CLIENT
            ? 'Client Selection'
            : 'MSP Partner Selection'}
        </CardTitle>
      </CardHeader>
      <CardContent className="content-space-y-6">
        {errors.mspName && (
          <div className="content-rounded content-bg-red-100 content-p-4 content-text-red-700">
            {errors.mspName}
          </div>
        )}

        <div className="content-grid content-grid-cols-1 content-gap-4 md:content-grid-cols-2">
          <div className="content-space-y-2">
            <Label htmlFor="userType">Client Type</Label>
            <Select
              value={selectedType}
              onValueChange={e => setSelectedType(e as UserType)}
            >
              <SelectTrigger>
                <SelectValue placeholder="Select User Type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={UserType.CLIENT}>Client</SelectItem>
                <SelectItem value={UserType.MSP}>MSP Partner</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="countryFilter">Country</Label>
            <SearchSelect
              value={formData.countryFilter}
              onValueChange={value => handleInputChange('countryFilter', value)}
              placeholder="Search and select a country"
              items={countryList.map(country => ({
                value: country.id,
                label: country.name,
              }))}
            />
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="provinceFilter">Province/State</Label>
            <SearchSelect
              value={formData.provinceFilter}
              onValueChange={value =>
                handleInputChange('provinceFilter', value)
              }
              placeholder="Search and select a province or state"
              items={provinceList.map(province => ({
                value: province.id,
                label: province.name,
              }))}
              disabled={!formData.countryFilter}
            />
          </div>

          {selectedType === UserType.CLIENT && (
            <div className="content-space-y-2">
              <Label>MSP Partner (Optional)</Label>
              <SearchSelect
                value={formData.selectedMSP}
                onValueChange={value => handleInputChange('selectedMSP', value)}
                placeholder="Search and select an MSP partner"
                items={filteredMSPs.map(msp => ({
                  value: msp.id,
                  label: msp.organizationName,
                }))}
              />
            </div>
          )}

          {selectedType === UserType.CLIENT && (
            <Fragment>
              <div className="content-col-span-2">
                <h2 className="content-text-2xl content-font-bold content-text-white">
                  Package Assignment
                </h2>
              </div>

              <div className="content-col-span-2 content-space-y-2">
                <Label className="content-flex content-items-center content-gap-2">
                  Client Organization
                  <span title="Select a client organization to assign the selected products and packages.">
                    <Info className="content-size-4" />
                  </span>
                </Label>
                <SearchSelect
                  value={formData.selectedClient}
                  onValueChange={value =>
                    handleInputChange('selectedClient', value)
                  }
                  placeholder="Search and select a client organization"
                  items={filteredClients.map(client => ({
                    value: client.id,
                    label: client.organizationName,
                  }))}
                />
                {errors.selectedClient && (
                  <p className="content-text-sm content-text-red-500">
                    {errors.selectedClient}
                  </p>
                )}
              </div>
            </Fragment>
          )}

          {selectedType === UserType.MSP && (
            <Fragment>
              <div className="content-col-span-2">
                <h2 className="content-text-2xl content-font-bold content-text-white">
                  Select MSP Partner for Assign Product And Packages
                </h2>
              </div>

              <div className="content-col-span-2 content-space-y-2">
                <Label>Select MSP Partner</Label>
                <SearchSelect
                  value={formData.selectedMSP}
                  onValueChange={value =>
                    handleInputChange('selectedMSP', value)
                  }
                  placeholder="Search and Select an MSP Partner"
                  items={filteredMSPs.map(msp => ({
                    value: msp.id,
                    label: msp.organizationName,
                  }))}
                />
                {errors.selectedMSP && (
                  <p className="content-text-sm content-text-red-500">
                    {errors.selectedMSP}
                  </p>
                )}
              </div>
            </Fragment>
          )}
        </div>

        {selectedType === UserType.CLIENT && selectedClient && (
          <Card className="content-mt-4 content-pt-4">
            <CardContent>
              <h3 className="content-mb-2 content-font-semibold">
                Selected Client
              </h3>
              <div className="content-mt-2">
                <p>
                  <strong>Name:</strong> {selectedClient.organizationName}
                </p>
              </div>
            </CardContent>
          </Card>
        )}

        {selectedType === UserType.MSP && selectedMSP && (
          <Card className="content-mt-4 content-pt-4">
            <CardContent>
              <h3 className="content-mb-2 content-font-semibold">
                Selected MSP Partner
              </h3>
              <div className="content-mt-2">
                <p>
                  <strong>Name:</strong> {selectedMSP.organizationName}
                </p>
                <p>
                  <strong>Location:</strong> {selectedMSP.country}
                  {selectedMSP.stateProvince &&
                    `, ${selectedMSP.stateProvince}`}
                </p>
              </div>
            </CardContent>
          </Card>
        )}

        <div className="content-flex content-justify-end content-pt-6">
          <Button onClick={handleSave}>Save and Continue</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step1;
