import { useEffect, useState } from 'react';
import { IoFilter } from 'react-icons/io5';
import { Link } from 'react-router-dom';
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import ChartLegend from 'components/ChartLegend';
import FilterDropDown from 'components/FilterDropDown';
import useAPI from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  name: string;
  value: number;
  color: string;
}

interface CustomTooltipProps {
  active?: boolean;
  payload?: ReadonlyArray<{
    name: string;
    value: number;
    payload: { color: string };
  }>;
  totalValue?: number;
}

const CustomTooltip = ({
  active,
  payload,
  totalValue = 0,
}: CustomTooltipProps) => {
  if (active && payload && payload.length) {
    return (
      <div className="home-flex home-items-center home-rounded home-bg-[#2B414F] home-px-4 home-py-2 home-shadow-lg">
        <div
          className="home-mr-2 home-size-3 home-rounded-full"
          style={{ backgroundColor: payload[0].payload.color }}
        />
        <p className="home-text-center home-font-semibold home-text-white">
          <span className="home-mr-4 home-text-center home-text-sm home-text-white home-text-opacity-50">
            {payload[0].name}
          </span>{' '}
          {`${totalValue === 0 ? 0 : payload[0].value}`}
        </p>
      </div>
    );
  }
  return null;
};

const LicenseDistribution = () => {
  const apiClient = useAPI();
  const [filterState, setFilterState] = useState({
    selectedType: 'CLIENT' as 'CLIENT' | 'MSP',
    selectedClient: '',
    selectedMsp: '',
    isFilterVisible: false,
  });
  const [licenseDistribution, setLicenseDistribution] = useState<{
    totalAvailableLicenses: number;
    totalAllocatedLicenses: number;
    totalActiveLicenses: number;
    totalExpiredLicenses: number;
  }>({
    totalAvailableLicenses: 0,
    totalAllocatedLicenses: 0,
    totalActiveLicenses: 0,
    totalExpiredLicenses: 0,
  });

  const data: IProps[] = [
    {
      name: 'Available',
      value: licenseDistribution?.totalAvailableLicenses ?? 0,
      color: '#A0AEC0',
    },
    {
      name: 'Allocated',
      value: licenseDistribution?.totalAllocatedLicenses ?? 0,
      color: '#F7C948',
    },
    {
      name: 'Active',
      value: licenseDistribution?.totalActiveLicenses ?? 0,
      color: '#37BE99',
    },
    {
      name: 'Expired',
      value: licenseDistribution?.totalExpiredLicenses ?? 0,
      color: '#F65E5B',
    },
  ];

  useEffect(() => {
    const fetchLicenseDistribution = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.LICENSE_DISTRIBUTION.replace(
            ':clientAdminId',
            filterState.selectedType === 'CLIENT'
              ? filterState.selectedClient
              : filterState.selectedMsp,
          ),
        );
        if (isSuccessResponse(response.statusCode)) {
          setLicenseDistribution(response.data);
        }
      } catch (error) {
        console.error('Error fetching license distribution:', error);
      }
    };

    if (filterState.selectedClient || filterState.selectedMsp) {
      setTimeout(() => {
        fetchLicenseDistribution();
      }, 0);
    }
  }, [
    apiClient,
    filterState.selectedClient,
    filterState.selectedMsp,
    filterState.selectedType,
  ]);

  const handleTypeChange = (type: 'CLIENT' | 'MSP') => {
    setFilterState(prev => ({
      ...prev,
      selectedType: type,
      selectedClient: type === 'CLIENT' ? prev.selectedClient : '',
      selectedMsp: type === 'MSP' ? prev.selectedMsp : '',
    }));
  };

  const handleClientChange = (client: string) => {
    setFilterState(prev => ({ ...prev, selectedClient: client }));
  };

  const handleMspChange = (msp: string) => {
    setFilterState(prev => ({ ...prev, selectedMsp: msp }));
  };

  const handleCloseFilter = () => {
    setFilterState(prev => ({ ...prev, isFilterVisible: false }));
  };

  const handleOpenFilter = () => {
    setFilterState(prev => ({ ...prev, isFilterVisible: true }));
  };

  const totalValue =
    licenseDistribution?.totalAvailableLicenses +
    licenseDistribution?.totalAllocatedLicenses +
    licenseDistribution?.totalActiveLicenses +
    licenseDistribution?.totalExpiredLicenses;

  return (
    <Card>
      <CardHeader className="home-flex home-flex-col home-gap-3 !home-space-y-0 md:home-gap-4 lg:!home-flex-row lg:home-justify-between">
        <CardTitle>
          <Link
            to="/#"
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            License Distribution
          </Link>
        </CardTitle>
        <Button variant="outline" onClick={handleOpenFilter}>
          <IoFilter className="home-text-xl" />
          Filter
        </Button>
        <FilterDropDown
          selectedType={filterState.selectedType}
          selectedClient={filterState.selectedClient}
          selectedMsp={filterState.selectedMsp}
          onClientChange={handleClientChange}
          onMspChange={handleMspChange}
          onTypeChange={handleTypeChange}
          onClose={handleCloseFilter}
          isVisible={filterState.isFilterVisible}
        />
      </CardHeader>
      <CardContent className="home-flex home-flex-col home-items-center home-gap-4 md:home-gap-6 lg:home-flex-row lg:home-justify-evenly lg:home-pb-0">
        <div className="home-h-[280px] home-w-full md:home-h-[320px] lg:home-h-[360px] lg:home-w-3/5">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              {totalValue === 0 ? (
                <Pie
                  data={[
                    { name: 'No Data', value: 1, color: '#A0AEC0' },
                    { name: 'No Data', value: 1, color: '#F7C948' },
                    { name: 'No Data', value: 1, color: '#37BE99' },
                    { name: 'No Data', value: 1, color: '#F65E5B' },
                  ]}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  // outerRadius={120}
                  paddingAngle={6}
                  cornerRadius={8}
                  startAngle={90}
                  endAngle={-270}
                  stroke="none"
                >
                  {['#A0AEC0', '#F7C948', '#37BE99', '#F65E5B'].map(
                    (entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry} />
                    ),
                  )}
                </Pie>
              ) : (
                <Pie
                  data={data}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  // outerRadius={120}
                  paddingAngle={6}
                  cornerRadius={8}
                  startAngle={90}
                  endAngle={-270}
                  stroke="none"
                >
                  {data.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
              )}
              <Tooltip
                content={props => (
                  <CustomTooltip {...props} totalValue={totalValue} />
                )}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <ChartLegend data={data} />
      </CardContent>
    </Card>
  );
};

export default LicenseDistribution;
