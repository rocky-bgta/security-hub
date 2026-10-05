import { useEffect, useState } from 'react';

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Label } from 'common/Label';
import SearchSelect from 'components/SearchSelect';
import { IDropdownData } from 'models/Dropdown';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { IMSPDropdownData } from 'models/MSP';

interface IProps {
  data: string;
  dropdownData: IDropdownData;
  onUpdate: (mspId: string, mspName?: string) => void;
  onNext?: () => void;
  onPrevious: () => void;
  isEditing?: boolean;
}

interface IMSPPartner {
  selectedMSP: string;
  countryFilter: string;
  provinceFilter: string;
}

interface MSPPartnerErrors {
  selectedMSP?: string;
}

const Step3 = ({
  data,
  dropdownData,
  onUpdate,
  onNext,
  onPrevious,
  isEditing = false,
}: IProps) => {
  const apiClient = useAPI();
  const [formData, setFormData] = useState<IMSPPartner>({
    selectedMSP: data || '',
    countryFilter: '',
    provinceFilter: '',
  });
  const [errors, setErrors] = useState<MSPPartnerErrors>({});
  const [mspList, setMSPList] = useState<Array<IMSPDropdownData>>([]);

  useEffect(() => {
    fetchMSPList();
  }, []);

  const fetchMSPList = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.MSP_LIST);
      if (isSuccessResponse(response.statusCode)) {
        setMSPList(
          response.data.items?.map((msp: IMSPDropdownData) => ({
            id: msp.id,
            organizationName: msp.organizationName,
            country: msp.country,
            stateProvince: msp.stateProvince,
          })) || [],
        );
      }
    } catch (error) {
      console.error('Error fetching MSP list:', error);
    }
  };

  const handleInputChange = (field: keyof IMSPPartner, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field as keyof MSPPartnerErrors]) {
      setErrors(prev => ({ ...prev, [field as keyof MSPPartnerErrors]: '' }));
    }

    if (field === 'countryFilter') {
      setFormData(prev => ({ ...prev, provinceFilter: '' }));
    }
  };

  const validateForm = () => {
    const newErrors: MSPPartnerErrors = {};

    if (!formData?.selectedMSP) {
      newErrors.selectedMSP = 'Please select an MSP partner';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = () => {
    if (validateForm()) {
      const selectedMSP = mspList.find(
        msp => msp?.id === formData?.selectedMSP,
      );
      const mspName = selectedMSP?.organizationName || '';

      onUpdate(formData.selectedMSP, mspName);
      onNext?.();
    }
  };

  const filteredMSPs = mspList.filter(msp => {
    const countryMatch =
      formData?.countryFilter === ''
        ? true
        : !formData?.countryFilter || msp.country === formData?.countryFilter;

    const provinceMatch =
      formData?.provinceFilter === ''
        ? true
        : !formData?.provinceFilter ||
          msp.stateProvince === formData?.provinceFilter;

    return countryMatch && provinceMatch;
  });
  const selectedMSP = mspList.find(msp => msp?.id === formData?.selectedMSP);

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl font-bold">
          MSP Partner Selection
        </CardTitle>
        <p className="text-gray-400">
          Choose your managed service provider partner
        </p>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 gap-4 rounded-md border border-card-border p-4 md:grid-cols-5">
          <div className="col-span-5">
            <p className="font-medium text-primary">Filter by Country</p>
          </div>
          <div className="col-span-2 space-y-2">
            <Label htmlFor="countryFilter">Filter by Country</Label>
            <SearchSelect
              value={formData.countryFilter}
              onValueChange={value => handleInputChange('countryFilter', value)}
              placeholder="Select country"
              items={dropdownData.countries.map(country => ({
                value: country.id,
                label: country.name,
              }))}
            />
          </div>

          <div className="col-span-2 space-y-2">
            <Label htmlFor="provinceFilter">Filter by Province/State</Label>
            <SearchSelect
              value={formData.provinceFilter}
              onValueChange={value =>
                handleInputChange('provinceFilter', value)
              }
              placeholder="Select province/state"
              items={dropdownData.states
                .filter(item => item.countryId === formData.countryFilter)
                .map(item => ({
                  value: item.id,
                  label: item.name,
                }))}
              disabled={formData.countryFilter === ''}
            />
          </div>
          <div className="col-span-1 flex items-end justify-end">
            <Button
              variant="outline"
              className="w-full"
              onClick={() =>
                setFormData({
                  ...formData,
                  countryFilter: '',
                  provinceFilter: '',
                })
              }
            >
              Clear Filters
            </Button>
          </div>
        </div>
        <div className="space-y-6 rounded-md border border-card-border p-4">
          <div className="col-span-2">
            <p className="font-medium text-primary">Select MSP Partner</p>
          </div>
          <div className="space-y-2">
            <Label>Select MSP Partner *</Label>
            <SearchSelect
              value={formData.selectedMSP}
              onValueChange={value => handleInputChange('selectedMSP', value)}
              placeholder="Search and Select an MSP Partner"
              items={filteredMSPs.map(msp => ({
                value: msp.id,
                label: msp.organizationName,
              }))}
              hasError={!!errors.selectedMSP}
            />
            {errors.selectedMSP && (
              <p className="text-sm text-red-500">{errors.selectedMSP}</p>
            )}
          </div>
        </div>

        {selectedMSP && (
          <Card className="mt-4 pt-4">
            <CardContent>
              <h3>Selected MSP Partner</h3>
              <div className="mt-2">
                <p>
                  <strong>Name:</strong> {selectedMSP.organizationName}
                </p>
                <p>
                  <strong>Location: </strong>
                  {
                    dropdownData.states.find(
                      state => state.id === selectedMSP.stateProvince,
                    )?.name
                  }
                  ,{' '}
                  {
                    dropdownData.countries.find(
                      country => country.id === selectedMSP.country,
                    )?.name
                  }
                </p>
              </div>
            </CardContent>
          </Card>
        )}

        <div className="flex justify-between pt-6">
          <Button variant="outline" onClick={onPrevious} className="px-8 py-2">
            Previous
          </Button>
          <Button onClick={handleSave}>
            {isEditing ? 'Save Changes' : 'Save & Continue'}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step3;
