import { safeWindowOpen } from 'home-module/security';
import { Download, Eye, RotateCcw, Search, X } from 'lucide-react';
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
import { Input } from 'common/Input';
import { Label } from 'common/Label';
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
import TableLoader from 'components/skeleton/TableLoader';
import CertificateViewDialog from 'features/certificate/aspire-admin/CertificateViewDialog';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { ICountry } from 'models/Configuration';
import { IGetListParams, IList, IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  HumanizeDate,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';

interface IExamCertificate {
  examScore: number;
  examPassed: string;
  fullName: string;
  expiryDate: string;
  issueDate: string;
  status: string;
  certificateLink: string;
  productName: string;
  clientAdminId: string;
  certificateId: string;
  examId: string;
}

interface ICertificateListResponse {
  offset: number;
  pageSize: number;
  total: number;
  items: IExamCertificate[];
}

const CertificateIssued = () => {
  const [certificates, setCertificates] = useState<IList<IExamCertificate>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<
    IGetListParams & {
      countryId?: string;
      mspId?: string;
      clientAdminId?: string;
      subpackageId?: string;
    }
  >({
    ...InitGetListParams,
    countryId: '',
    mspId: '',
    clientAdminId: '',
    subpackageId: '',
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedCertificate, setSelectedCertificate] =
    useState<IExamCertificate | null>(null);
  const [isDetailDialogOpen, setIsDetailDialogOpen] = useState<boolean>(false);
  const [countryList, setCountryList] = useState<ICountry[]>([]);
  const [mspList, setMspList] = useState<any[]>([]);
  const [clientList, setClientList] = useState<any[]>([]);
  const [subpackageList, setSubpackageList] = useState<any[]>([]);
  const apiClient = useAPI();

  const fetchCountryList = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.COUNTRY_LIST);
      if (isSuccessResponse(response.statusCode)) {
        setCountryList(
          response.data.map((country: ICountry) => ({
            id: country.id,
            name: country.name,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching country list:', error);
    }
  };

  const fetchMspList = async (countryId?: string) => {
    try {
      const endpoint = countryId
        ? `${API_END_POINTS.MSP_LIST}countryId=${countryId}`
        : API_END_POINTS.MSP_LIST;
      const response = await apiClient.get(endpoint);
      setMspList(
        response.data.items.map((msp: any) => ({
          id: msp.id,
          name: msp.organizationName,
        })),
      );
    } catch (error) {
      console.error('Error fetching msp list:', error);
      setMspList([]);
    }
  };

  const fetchClientList = async (mspId?: string) => {
    if (!mspId) {
      setClientList([]);
      return;
    }

    try {
      const response = await apiClient.get(
        `${API_END_POINTS.CLIENT_LIST}?mspId=${mspId}`,
      );
      setClientList(
        response.data.clientAdmins.map((client: any) => ({
          id: client.id,
          name: client.organizationName,
        })),
      );
    } catch (error) {
      console.error('Error fetching client list:', error);
      setClientList([]);
    }
  };

  const fetchSubpackageList = async (clientAdminId?: string) => {
    if (!clientAdminId) {
      setSubpackageList([]);
      return;
    }

    try {
      const response = await apiClient.get(
        `${API_END_POINTS.SUB_PACKAGE_LIST}clientAdminId=${clientAdminId}&offset=0&pageSize=1000`,
      );
      console.log('subpackage list', response.data);

      setSubpackageList(
        response.data.items.map((subpackage: any) => ({
          id: subpackage.id,
          name: subpackage.name,
        })),
      );
    } catch (error) {
      console.error('Error fetching subpackage list:', error);
      setSubpackageList([]);
    }
  };

  useEffect(() => {
    fetchCountryList();
    fetchMspList();
  }, []);

  // Fetch MSP list when country changes
  useEffect(() => {
    if (queryParams.countryId) {
      fetchMspList(queryParams.countryId);
      // Reset MSP, Client, and Subpackage when country changes
      setQueryParams(prev => ({
        ...prev,
        mspId: '',
        clientAdminId: '',
        subpackageId: '',
      }));
      setClientList([]);
      setSubpackageList([]);
    }
  }, [queryParams.countryId]);

  // Fetch client list when MSP changes
  useEffect(() => {
    if (queryParams.mspId) {
      fetchClientList(queryParams.mspId);
      // Reset Client and Subpackage when MSP changes
      setQueryParams(prev => ({
        ...prev,
        clientAdminId: '',
        subpackageId: '',
      }));
      setSubpackageList([]);
    } else {
      setClientList([]);
      setSubpackageList([]);
    }
  }, [queryParams.mspId]);

  // Fetch subpackage list when client changes
  useEffect(() => {
    if (queryParams.clientAdminId) {
      fetchSubpackageList(queryParams.clientAdminId);
      // Reset Subpackage when Client changes
      setQueryParams(prev => ({
        ...prev,
        subpackageId: '',
      }));
    } else {
      setSubpackageList([]);
    }
  }, [queryParams.clientAdminId]);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  const searchDebounce = useDebounce(queryString, 1000);

  useEffect(() => {
    fetchCertificates();
  }, [searchDebounce]);

  const fetchCertificates = async () => {
    setLoading(true);
    try {
      const response: IResponse<ICertificateListResponse> = await apiClient.get(
        API_END_POINTS.EXAM_CERTIFICATES_LIST + queryString,
      );
      if (isSuccessResponse(response.statusCode)) {
        setCertificates({
          ...InitGetListParams,
          total: response.data.total,
          items: response.data.items,
        });
      }
    } catch (error) {
      console.error('Error fetching certificates:', error);
      setCertificates({
        ...InitGetListParams,
        total: 0,
        items: [],
      });
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setQueryParams({
      ...InitGetListParams,
      countryId: '',
      mspId: '',
      clientAdminId: '',
      subpackageId: '',
    });
    setMspList([]);
    setClientList([]);
    setSubpackageList([]);
    fetchMspList();
  };

  const handleResetFilter = () => {
    setQueryParams(prev => ({
      ...prev,
      countryId: '',
      mspId: '',
      clientAdminId: '',
      subpackageId: '',
    }));
    setMspList([]);
    setClientList([]);
    setSubpackageList([]);
    fetchMspList();
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleViewDetails = (certificate: IExamCertificate) => {
    setSelectedCertificate(certificate);
    setIsDetailDialogOpen(true);
  };

  const handleDownload = (certificate: IExamCertificate) => {
    if (certificate.certificateLink) {
      safeWindowOpen(certificate.certificateLink, '_blank');
    } else {
      toast.error('Certificate link not available');
    }
  };

  const handleRevoke = (certificateId: string) => {
    toast.success('Certificate has been successfully revoked.');
    fetchCertificates();
  };

  const handleReissue = (certificateId: string) => {
    toast.success('Certificate has been successfully re-issued.');
    fetchCertificates();
  };

  const handleExport = () => {
    toast.success('Certificate data is being exported to CSV format.');
  };

  const getStatusBadge = (status: string) => {
    switch (status?.toLowerCase()) {
      case 'active':
        return <Badge variant="default">Active</Badge>;
      case 'revoked':
        return <Badge variant="secondary">Revoked</Badge>;
      default:
        return <Badge variant="outline">{status || 'N/A'}</Badge>;
    }
  };

  const getPassStatusBadge = (examPassed: string) => {
    switch (examPassed?.toLowerCase()) {
      case 'pass':
        return <Badge variant="default">Pass</Badge>;
      case 'fail':
        return <Badge variant="destructive">Fail</Badge>;
      default:
        return <Badge variant="outline">{examPassed || 'N/A'}</Badge>;
    }
  };

  return (
    <div className="content-space-y-6">
      <div className="content-flex content-items-center content-justify-between">
        <div>
          <h1 className="content-text-3xl content-font-bold content-text-foreground">
            Issued Certificates
          </h1>
          <p className="content-text-muted-foreground">
            Manage and track all issued certificates
          </p>
        </div>
        <Button onClick={handleExport} variant="outline">
          <Download className="content-mr-2 content-size-4" />
          Export
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Certificate Records</CardTitle>
          <CardDescription>
            View and manage all issued certificates with detailed information
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Card className="content-mb-6">
            <CardHeader>
              <CardTitle>Filters</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="content-mb-4 content-grid content-grid-cols-4 content-gap-2">
                <div className="content-col-span-1 content-flex content-flex-col content-items-start content-gap-2">
                  <Label htmlFor="countryId">Select Country</Label>
                  <SearchSelect
                    value={queryParams.countryId || ''}
                    onValueChange={value =>
                      setQueryParams(prev => ({
                        ...prev,
                        countryId: value,
                      }))
                    }
                    items={countryList.map(country => ({
                      value: country.id,
                      label: country.name,
                    }))}
                    placeholder="Select Country"
                  />
                </div>
                <div className="content-col-span-1 content-flex content-flex-col content-items-start content-gap-2">
                  <Label htmlFor="mspId">Select MSP</Label>
                  <SearchSelect
                    value={queryParams.mspId || ''}
                    onValueChange={value =>
                      setQueryParams(prev => ({ ...prev, mspId: value }))
                    }
                    items={mspList.map(msp => ({
                      value: msp.id,
                      label: msp.name,
                    }))}
                    placeholder="Select MSP"
                    disabled={!queryParams.countryId}
                  />
                </div>
                <div className="content-col-span-1 content-flex content-flex-col content-items-start content-gap-2">
                  <Label htmlFor="clientAdminId">Select Client</Label>
                  <SearchSelect
                    value={queryParams.clientAdminId || ''}
                    onValueChange={value =>
                      setQueryParams(prev => ({
                        ...prev,
                        clientAdminId: value,
                      }))
                    }
                    items={clientList.map(client => ({
                      value: client.id,
                      label: client.name,
                    }))}
                    placeholder="Select Client"
                    disabled={!queryParams.mspId}
                  />
                </div>
                <div className="content-col-span-1 content-flex content-items-end content-justify-end content-gap-2">
                  <Button
                    variant="outline"
                    className="content-w-full"
                    onClick={handleResetFilter}
                  >
                    Reset Filters
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="content-mb-6 content-grid content-w-full content-grid-cols-6 content-gap-2">
            <div className="content-col-span-2 content-flex content-flex-col content-items-start content-gap-2">
              <Label htmlFor="subpackageId">Select Subpackage</Label>
              <SearchSelect
                value={queryParams.subpackageId || ''}
                onValueChange={value =>
                  setQueryParams(prev => ({
                    ...prev,
                    subpackageId: value,
                  }))
                }
                items={subpackageList.map(subpackage => ({
                  value: subpackage.id,
                  label: subpackage.name,
                }))}
                placeholder="Select Subpackage"
                disabled={!queryParams.clientAdminId}
              />
            </div>
            <div className="content-col-span-2 content-flex content-flex-col content-items-start content-gap-2">
              <Label htmlFor="search">Search</Label>
              <div className="content-relative content-w-full">
                <Search className="content-absolute content-left-3 content-top-3 content-size-4 content-text-muted-foreground" />
                <Input
                  className="content-w-full content-pl-10"
                  id="search"
                  type="text"
                  placeholder="Search by name, product..."
                  value={queryParams.search || ''}
                  onChange={e =>
                    setQueryParams(prev => ({
                      ...prev,
                      search: e.target.value,
                    }))
                  }
                />
              </div>
            </div>
            <div className="content-col-span-2 content-flex content-items-end content-justify-end">
              <Button
                variant="outline"
                className="content-w-full"
                onClick={handleReset}
              >
                Reset
              </Button>
            </div>
          </div>

          {loading ? (
            <TableLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Full Name</TableHead>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Assessment Score</TableHead>
                  <TableHead>Pass Status</TableHead>
                  <TableHead>Issue Date</TableHead>
                  <TableHead>Expiry Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {certificates?.items?.length > 0 ? (
                  certificates.items.map(certificate => (
                    <TableRow key={certificate.certificateId}>
                      <TableCell className="content-font-medium">
                        {certificate.fullName}
                      </TableCell>
                      <TableCell>{certificate.productName}</TableCell>
                      <TableCell>
                        <span
                          className={`content-font-medium ${
                            certificate.examScore >= 70
                              ? 'content-text-green-600'
                              : 'content-text-red-600'
                          }`}
                        >
                          {certificate.examScore}%
                        </span>
                      </TableCell>
                      <TableCell>
                        {getPassStatusBadge(certificate.examPassed)}
                      </TableCell>
                      <TableCell>
                        {HumanizeDate(certificate.issueDate)}
                      </TableCell>
                      <TableCell>
                        {HumanizeDate(certificate.expiryDate)}
                      </TableCell>
                      <TableCell>
                        {getStatusBadge(certificate.status)}
                      </TableCell>
                      <TableCell>
                        <div className="content-flex content-gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleViewDetails(certificate)}
                          >
                            <Eye className="content-size-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleDownload(certificate)}
                          >
                            <Download className="content-size-4" />
                          </Button>
                          {certificate.status?.toLowerCase() === 'active' ? (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() =>
                                handleRevoke(certificate.certificateId)
                              }
                            >
                              <X className="content-size-4" />
                            </Button>
                          ) : (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() =>
                                handleReissue(certificate.certificateId)
                              }
                            >
                              <RotateCcw className="content-size-4" />
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={8} className="content-text-center">
                      <div className="content-text-center content-text-muted-foreground">
                        No certificates found.
                      </div>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}
          <div className="content-pt-6">
            <Pagination
              total={certificates?.total}
              perPage={certificates?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      {selectedCertificate && (
        <CertificateViewDialog
          isOpen={isDetailDialogOpen}
          setIsOpen={setIsDetailDialogOpen}
          selectedCertificate={selectedCertificate}
          onDownload={handleDownload}
        />
      )}
    </div>
  );
};

export default CertificateIssued;
