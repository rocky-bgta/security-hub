import { StatusBadge } from 'components/StatusBadge';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import Pagination from 'common/Pagination';
import { Progress } from 'common/Progress';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import SearchSelect from 'components/SearchSelect';
import TableLoader from 'components/skeleton/TableLoader';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { Download, Search } from 'lucide-react';
import { ICountryDropdown } from 'models/Country';
import { IGetListParams, IList, Status } from 'models/Global';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  humanizeText,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import { IMsp, IMspTier } from 'models/Msp';

interface IActiveLicenseSummary {
  licenseCount: number;
  usedLicenseCount: number;
}

const MSPManageLicense = () => {
  const apiClient = useAPI();

  const [loading, setLoading] = useState<boolean>(true);
  const [mspList, setMspList] = useState<IList<IMsp>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [activeLicenseSummary, setActiveLicenseSummary] =
    useState<IActiveLicenseSummary>({
      licenseCount: 0,
      usedLicenseCount: 0,
    });
  const [countryList, setCountryList] = useState<ICountryDropdown[]>([]);
  const [mspTierList, setMspTierList] = useState<IMspTier[]>([]);
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    status: '' as Status,
    countryId: '',
    mspTier: '',
  });
  const searchDebounce = useDebounce(queryString, 500);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchMSPList();
    }
  }, [searchDebounce]);

  useEffect(() => {
    fetchActiveLicenseSummary();
    fetchCountryList();
    fetchMSPTierList();
  }, []);

  const fetchMSPList = async () => {
    setLoading(true);
    try {
      const endpoint = API_END_POINTS.MSP_LIST_WITH_FILTERS + queryString;

      const response = await apiClient.get(endpoint);
      if (isSuccessResponse(response.statusCode))
        setMspList({
          ...InitGetListParams,
          total: response.data.total,
          items: response.data.items,
        });
    } catch (error) {
      console.error('Error fetching MSP list:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchActiveLicenseSummary = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.MSP_ACTIVE_LICENSE_SUMMARY,
      );
      if (isSuccessResponse(response.statusCode)) {
        setActiveLicenseSummary(response.data);
      }
    } catch (error) {
      console.error('Error fetching active license summary:', error);
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

  const fetchMSPTierList = async () => {
    try {
      const response = await apiClient.get(
        `${API_END_POINTS.MSP_TIER_LIST}status=true&offset=0&limit=100`,
      );
      if (isSuccessResponse(response.statusCode)) {
        setMspTierList(response.data.items);
      }
    } catch (error) {
      console.error('Error fetching MSP tier list:', error);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleResetFilters = () => {
    setQueryParams({
      ...InitGetListParams,
      search: '',
      status: '' as Status,
      countryId: '',
      mspTier: '',
    });
  };

  const handleGenerateReport = () => {};

  const getLicenseUtilization = (msp: IMsp) => {
    return msp.licenseCount > 0
      ? (msp.usedLicenseCount / msp.licenseCount) * 100
      : 0;
  };

  const getUtilizationColor = (utilization: number) => {
    if (utilization >= 90) return 'text-[#ef4444]';
    if (utilization >= 70) return 'text-[#eab308]';
    return 'text-[#16a34a]';
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-foreground">Manage Licenses</h2>
          <p className="text-muted-foreground">
            Allocate, remove, and track license usage for all MSPs
          </p>
        </div>
        <Button onClick={handleGenerateReport} variant="outline">
          <Download className="mr-2 size-4" />
          Generate Report
        </Button>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Total Licenses
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {activeLicenseSummary.licenseCount.toLocaleString()}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Used Licenses</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-[#eab308]">
              {activeLicenseSummary.usedLicenseCount.toLocaleString()}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Available Licenses
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-[#16a34a]">
              {(
                activeLicenseSummary.licenseCount -
                activeLicenseSummary.usedLicenseCount
              ).toLocaleString()}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Utilization Rate
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">
              {activeLicenseSummary.licenseCount > 0
                ? Math.round(
                    (activeLicenseSummary.usedLicenseCount /
                      activeLicenseSummary.licenseCount) *
                      100,
                  )
                : 0}
              %
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>MSP License Management</CardTitle>
            <div className="flex items-center gap-2">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search MSPs..."
                  value={queryParams.search}
                  onChange={e =>
                    setQueryParams(prevState => ({
                      ...prevState,
                      search: e.target.value,
                    }))
                  }
                  className="w-64 pl-9"
                />
              </div>
              <Select
                value={queryParams.status}
                onValueChange={value =>
                  setQueryParams(prevState => ({
                    ...prevState,
                    status: value as Status,
                  }))
                }
              >
                <SelectTrigger className="h-10 w-[180px] rounded-md">
                  <SelectValue placeholder="Filter by status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={Status.ALL}>All Status</SelectItem>
                  <SelectItem value={Status.ACTIVE}>Active</SelectItem>
                  <SelectItem value={Status.PENDING}>Pending</SelectItem>
                  <SelectItem value={Status.SUSPENDED}>Suspended</SelectItem>
                  <SelectItem value={Status.INACTIVE}>Inactive</SelectItem>
                </SelectContent>
              </Select>
              <SearchSelect
                items={countryList.map(country => ({
                  value: country.id,
                  label: country.name,
                }))}
                value={queryParams.countryId}
                onValueChange={(value: string) =>
                  setQueryParams(prevState => ({
                    ...prevState,
                    countryId: value,
                  }))
                }
                placeholder="Select Country"
              />
              <SearchSelect
                items={mspTierList?.map(tier => ({
                  value: tier.id,
                  label: tier.tierName,
                }))}
                value={queryParams.mspTier}
                onValueChange={(value: string) =>
                  setQueryParams(prevState => ({
                    ...prevState,
                    mspTier: value,
                  }))
                }
                placeholder="Select MSP Tier"
              />
              <Button variant="outline" onClick={handleResetFilters}>
                Reset Filters
              </Button>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <TableLoader />
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>MSP Name</TableHead>
                    <TableHead>Tier</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Total Licenses</TableHead>
                    <TableHead>Used Licenses</TableHead>
                    <TableHead>Available</TableHead>
                    <TableHead>Utilization</TableHead>
                    {/* <TableHead>Actions</TableHead> */}
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {mspList?.items?.length > 0 ? (
                    mspList?.items?.map(msp => {
                      const utilization = getLicenseUtilization(msp);

                      return (
                        <TableRow key={msp.id}>
                          <TableCell className="font-medium">
                            {msp.organizationName}
                          </TableCell>
                          <TableCell>
                            <Badge variant="outline">
                              {humanizeText(msp.mspTier || '')}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            <StatusBadge status={humanizeText(msp.status)} />
                          </TableCell>
                          <TableCell>
                            {msp.licenseCount.toLocaleString()}
                          </TableCell>
                          <TableCell>
                            {msp.usedLicenseCount.toLocaleString()}
                          </TableCell>
                          <TableCell className="font-medium text-[#16a34a]">
                            {(
                              msp.licenseCount - msp.usedLicenseCount
                            ).toLocaleString()}
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center gap-2">
                              <Progress value={utilization} className="w-16" />
                              <span
                                className={`text-sm font-medium ${getUtilizationColor(utilization)}`}
                              >
                                {Math.round(utilization)}%
                              </span>
                            </div>
                          </TableCell>
                          {/* <TableCell>
                            <div className="flex gap-2">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleAllocateLicenses(msp)}
                                className="text-[#16a34a] hover:text-[#16a34a]"
                              >
                                <Plus className="mr-1 h-4 w-4" />
                                Allocate
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleRemoveLicenses(msp)}
                                className="text-[#ef4444] hover:text-[#ef4444]"
                              >
                                <Minus className="mr-1 h-4 w-4" />
                                Remove
                              </Button>
                            </div>
                          </TableCell> */}
                        </TableRow>
                      );
                    })
                  ) : (
                    <TableRow>
                      <TableCell
                        colSpan={8}
                        className="text-center text-muted-foreground"
                      >
                        No MSPs found
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
              {mspList.total > 0 && (
                <div className="mt-4">
                  <Pagination
                    total={mspList.total}
                    perPage={queryParams.pageSize || 10}
                    onPageChange={onPageChangeHandler}
                  />
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default MSPManageLicense;
