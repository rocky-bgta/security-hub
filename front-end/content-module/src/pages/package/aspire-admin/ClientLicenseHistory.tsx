import { History } from 'lucide-react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import Pagination from 'common/Pagination';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import SearchSelect from 'components/SearchSelect';
import Border from 'components/UserBorder';
import AssignedPackagesLoader from 'components/skeleton/AssignedPackages';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { IClientDropdownData } from 'models/Client';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { ILicenseHistory } from 'models/License';
import { IProduct } from 'models/Product';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { HumanizeDate, objectToQueryString } from 'utils/Helper';
import { IMSPDropdownData } from 'models/Msp';
import { ICountry } from 'models/Configuration';
import { IPackage } from 'models/Package';
import { Input } from 'common/Input';

const ClientLicenseHistory = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [licenseData, setLicenseData] = useState<IList<ILicenseHistory>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 10,
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
    clientAdminId: '',
    productId: '',
    countryId: '',
    mspId: '',
    packageId: '',
  });

  const [clientList, setClientList] = useState<IClientDropdownData[]>([]);
  const [productList, setProductList] = useState<IProduct[]>([]);
  const [mspList, setMspList] = useState<IMSPDropdownData[]>([]);
  const [countryList, setCountryList] = useState<ICountry[]>([]);
  const [packageList, setPackageList] = useState<IPackage[]>([]);
  const searchDebounce = useDebounce(queryString, 500);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchLicenseData();
    }
  }, [searchDebounce]);

  const fetchLicenseData = async () => {
    setLoading(true);

    try {
      const response: IResponse<IList<ILicenseHistory>> = await apiClient.get(
        API_END_POINTS.CLIENT_LICENSE_HISTORY_LIST + queryString,
      );
      setLicenseData(response.data);
    } catch (error) {
      console.error('Error fetching license history data:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCountryList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.COUNTRY_LIST + '?pageSize=1000',
      );
      setCountryList(
        response.data.map((country: ICountry) => ({
          id: country.id,
          name: country.name,
        })),
      );
    } catch (error) {
      console.error('Error fetching country list:', error);
    }
  };
  const fetchMspList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.MSP_LIST + 'pageSize=1000',
      );
      setMspList(response.data.items);
    } catch (error) {
      console.error('Error fetching msp list:', error);
    }
  };

  const fetchClientList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_LIST + '?pageSize=1000&status=ACTIVE',
      );
      setClientList(response.data.clientAdmins);
    } catch (error) {
      console.error('Error fetching client list:', error);
    }
  };

  const fetchProductList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.PRODUCT_LIST + 'pageSize=1000',
      );
      setProductList(response.data.items);
    } catch (error) {
      console.error('Error fetching product list:', error);
    }
  };

  const fetchPackageList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.PRODUCT_DETAILS + queryParams.productId,
      );
      setPackageList(response.data.packages);
    } catch (error) {
      console.error('Error fetching package list:', error);
    }
  };

  useEffect(() => {
    fetchClientList();
    fetchProductList();
    fetchMspList();
    fetchCountryList();
  }, []);

  useEffect(() => {
    if (queryParams.productId) {
      fetchPackageList();
    }
  }, [queryParams.productId]);

  const handlePageChange = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page,
    }));
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge variant="default">Active</Badge>;
      case 'EXPIRING_SOON':
        return <Badge variant="destructive">Expiring Soon</Badge>;
      case 'EXPIRED':
        return <Badge variant="secondary">Expired</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };
  const handleResetFilter = () => {
    setQueryParams(prev => ({
      ...prev,
      search: '',
      clientAdminId: '',
      productId: '',
      countryId: '',
      mspId: '',
      packageId: '',
    }));
  };

  return (
    <div className="content-space-y-6 content-text-white">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-tracking-tight content-text-white">
            Client License History
          </h1>
          <p className="content-text-muted-foreground">
            Track license allocations, renewals, and expirations
          </p>
        </div>
      </div>

      <Border>
        <CardHeader>
          <div className="content-flex content-items-center content-justify-between">
            <div>
              <CardTitle className="content-mb-2 content-flex content-items-center content-gap-2">
                <History className="content-size-5" />
                License History ({licenseData?.total})
              </CardTitle>
              <CardDescription>
                Track license allocations, renewals, and expirations
              </CardDescription>
            </div>
            {/* <Button
              onClick={() => handleExportReport('License History')}
              variant="outline"
              className="content-border-transparent content-bg-primary"
            >
              <Download className="content-mr-2 content-h-4 content-w-4" />
              Export History
            </Button> */}
          </div>

          <div className="!content-mt-4 content-flex content-items-center content-gap-2">
            <Input
              id="search"
              value={queryParams.search}
              onChange={e =>
                setQueryParams(prev => ({ ...prev, search: e.target.value }))
              }
              placeholder="Search"
            />
            <SearchSelect
              value={queryParams.countryId}
              onValueChange={value =>
                setQueryParams(prev => ({ ...prev, countryId: value }))
              }
              items={countryList.map(country => ({
                value: country.id,
                label: country.name,
              }))}
              placeholder="Select Country"
            />
            <SearchSelect
              value={queryParams.mspId}
              onValueChange={value =>
                setQueryParams(prev => ({ ...prev, mspId: value }))
              }
              items={mspList.map(msp => ({
                value: msp.id,
                label: msp.organizationName,
              }))}
              placeholder="Select MSP"
            />
            <SearchSelect
              value={queryParams.clientAdminId}
              onValueChange={value =>
                setQueryParams(prev => ({ ...prev, clientAdminId: value }))
              }
              items={clientList.map(client => ({
                value: client.id,
                label: client.organizationName,
              }))}
              placeholder="Select Client"
            />
            <SearchSelect
              value={queryParams.productId}
              onValueChange={value =>
                setQueryParams(prev => ({ ...prev, productId: value }))
              }
              items={productList.map(product => ({
                value: product.productId,
                label: product.productName,
              }))}
              placeholder="Select Product"
            />

            <SearchSelect
              value={queryParams.packageId}
              onValueChange={value =>
                setQueryParams(prev => ({ ...prev, packageId: value }))
              }
              items={packageList.map((pkg: IPackage) => ({
                value: pkg.id,
                label: pkg.packageName,
              }))}
              placeholder="Select Package"
              disabled={!queryParams.productId}
            />
            <Button onClick={handleResetFilter} variant="outline">
              Reset Filters
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <AssignedPackagesLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Package Name</TableHead>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Msp Name</TableHead>
                  <TableHead>Client Name</TableHead>
                  <TableHead>Client Email</TableHead>
                  <TableHead>Assigned Date</TableHead>
                  <TableHead>License Count</TableHead>
                  <TableHead>Used License Count</TableHead>
                  <TableHead>Available License Count</TableHead>
                  <TableHead>Expiry Date</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {licenseData?.total === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="content-text-center">
                      License history not available
                    </TableCell>
                  </TableRow>
                ) : (
                  licenseData?.items?.map(
                    (license: ILicenseHistory, index: number) => (
                      <TableRow key={index}>
                        <TableCell className="content-font-medium">
                          {license.packageName}
                        </TableCell>
                        <TableCell>{license.productName}</TableCell>
                        <TableCell>{license.mspName || '-'}</TableCell>
                        <TableCell>{license.clientName || '-'}</TableCell>
                        <TableCell>{license.email || '-'}</TableCell>
                        <TableCell>
                          {HumanizeDate(license.assignedAt)}
                        </TableCell>
                        <TableCell>{license.licenseCount}</TableCell>
                        <TableCell>{license.usedLicenseCount}</TableCell>
                        <TableCell>
                          {license.licenseCount - license.usedLicenseCount}
                        </TableCell>
                        <TableCell>
                          {HumanizeDate(license.expiryDate)}
                        </TableCell>
                        <TableCell>
                          {getStatusBadge(license.licenseStatus)}
                        </TableCell>
                      </TableRow>
                    ),
                  )
                )}
              </TableBody>
            </Table>
          )}
          <div className="content-mt-4 content-flex content-items-center content-justify-end">
            <Pagination
              total={licenseData?.total}
              perPage={licenseData?.pageSize}
              onPageChange={handlePageChange}
            />
          </div>
        </CardContent>
      </Border>
    </div>
  );
};

export default ClientLicenseHistory;
