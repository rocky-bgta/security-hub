import { CloseIcon } from 'assets/icons';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import SearchSelect from 'components/SearchSelect';
import useAPI from 'hooks/UseAPI';
import { RefreshCcwIcon } from 'lucide-react';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface FilterDropDownProps {
  selectedType: 'CLIENT' | 'MSP';
  selectedClient: string;
  selectedMsp: string;
  onClientChange: (client: string) => void;
  onMspChange: (msp: string) => void;
  onTypeChange: (type: 'CLIENT' | 'MSP') => void;
  onClose: () => void;
  isVisible?: boolean;
}

const FilterDropDown = ({
  selectedClient,
  selectedMsp,
  onClientChange,
  onMspChange,
  onTypeChange,
  onClose,
  isVisible = true,
}: FilterDropDownProps) => {
  const apiClient = useAPI();
  const [country, setCountry] = useState<string>('');
  const [selectedType, setSelectedType] = useState<'CLIENT' | 'MSP'>('CLIENT');
  const [countryList, setCountryList] = useState<
    Array<{ id: string; name: string }>
  >([]);
  const [clientList, setClientList] = useState<
    Array<{ id: string; name: string }>
  >([]);
  const [mspList, setMspList] = useState<Array<{ id: string; name: string }>>(
    [],
  );

  useEffect(() => {
    const fetchCountryList = async () => {
      try {
        const response = await apiClient.get(API_END_POINTS.COUNTRY_LIST);
        if (isSuccessResponse(response.statusCode)) {
          setCountryList(response.data);
        }
      } catch (error) {
        console.error('Error fetching country list:', error);
      }
    };

    fetchCountryList();
  }, [apiClient]);

  useEffect(() => {
    const fetchClientList = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.CLIENT_LIST_BY_COUNTRY +
            `country=${country}&isClient=${selectedType === 'CLIENT' ? 'true' : 'false'}`,
        );
        if (isSuccessResponse(response.statusCode)) {
          if (selectedType === 'CLIENT') {
            setClientList(
              response.data.map(
                (client: { id: string; organizationName: string }) => ({
                  id: client.id,
                  name: client.organizationName,
                }),
              ),
            );
          } else {
            setMspList(
              response.data.map(
                (msp: { id: string; organizationName: string }) => ({
                  id: msp.id,
                  name: msp.organizationName,
                }),
              ),
            );
          }
        }
      } catch (error) {
        console.error('Error fetching client list:', error);
      }
    };

    if (country && selectedType) {
      fetchClientList();
    }
  }, [country, selectedType, apiClient]);

  const handleCountryChange = (country: string) => {
    setCountry(country);
  };

  const handleTypeChange = (type: 'CLIENT' | 'MSP') => {
    setSelectedType(type);
    onTypeChange(type);
  };

  const handleCloseFilter = () => {
    setSelectedType('CLIENT');
    onTypeChange('CLIENT');
    setClientList([]);
    setMspList([]);
    setCountry('');
  };

  if (!isVisible) return null;
  return (
    <div className="home-absolute home-right-0 home-top-10 home-z-10 home-w-[300px] home-bg-dark home-p-4">
      <div className="home-mb-5 home-flex home-items-center home-justify-between home-border-b home-pb-5">
        <div className="home-flex home-items-center home-gap-3">
          <p className="home-font-semibold">Filter</p>
          <span className="home-cursor-pointer" title="Reset Filter">
            <RefreshCcwIcon size={16} onClick={handleCloseFilter} />
          </span>
        </div>
        <CloseIcon
          width={20}
          height={20}
          onClick={onClose}
          className="home-cursor-pointer"
        />
      </div>
      <div className="home-space-y-2">
        <Label>Country *</Label>
        <SearchSelect
          value={country}
          onValueChange={handleCountryChange}
          placeholder="Select Country"
          items={countryList.map(country => ({
            value: country.id,
            label: country.name,
          }))}
        />
      </div>
      <div className="home-my-5 home-flex home-items-center home-gap-3">
        <RadioGroup
          defaultValue="client"
          className="home-grid-cols-2"
          onValueChange={handleTypeChange}
        >
          <div className="home-flex home-items-center home-space-x-2">
            <RadioGroupItem
              value="CLIENT"
              checked={selectedType === 'CLIENT'}
              id="client"
              className="!home-border-cloudy-white !home-text-cloudy-white"
            />
            <Label htmlFor="client" className="home-cursor-pointer">
              Client
            </Label>
          </div>
          <div className="home-flex home-items-center home-space-x-2">
            <RadioGroupItem
              value="MSP"
              checked={selectedType === 'MSP'}
              id="msp"
              className="!home-border-cloudy-white !home-text-cloudy-white"
            />
            <Label htmlFor="msp" className="home-cursor-pointer">
              MSP
            </Label>
          </div>
        </RadioGroup>
      </div>
      {selectedType === 'CLIENT' && (
        <div className="home-space-y-2">
          <Label htmlFor="client">Client *</Label>
          <SearchSelect
            value={selectedClient}
            onValueChange={onClientChange}
            placeholder="Select Client"
            disabled={clientList.length === 0}
            items={clientList.map(client => ({
              value: client.id,
              label: client.name,
            }))}
          />
        </div>
      )}
      {selectedType === 'MSP' && (
        <div className="home-space-y-2">
          <Label htmlFor="user">MSP Client *</Label>
          <SearchSelect
            value={selectedMsp}
            onValueChange={onMspChange}
            placeholder="Select MSP"
            disabled={mspList.length === 0}
            items={mspList.map(msp => ({
              value: msp.id,
              label: msp.name,
            }))}
          />
        </div>
      )}
    </div>
  );
};

export default FilterDropDown;
