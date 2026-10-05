import dayjs from 'dayjs';
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
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { Download, Eye, History } from 'lucide-react';
import { IGetListParams, IList, RoleType, Status } from 'models/Global';
import { IPaymentReport, ISummaryData } from 'models/Payment';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  humanizeText,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import SearchSelect from 'components/SearchSelect';
import { ICountryDropdown } from 'models/Country';
import {
  Select,
  SelectValue,
  SelectTrigger,
  SelectContent,
  SelectItem,
} from 'common/Select';
import TableLoader from 'components/skeleton/TableLoader';
import AspireViewPaymentHistory from 'features/payment/aspire-admin/AspireViewPaymentHistory';
import { getStatusBadge } from 'pages/payment/ClientPaymentHistory';

interface dateRange {
  startDate: string;
  endDate: string;
}

const AspireAdminClientPaymentHistory = () => {
  const [selectedDate, setSelectedDate] = useState<dateRange>({
    startDate: '',
    endDate: '',
  });
  const [selectedReportId, setSelectedReportId] = useState<string>();
  const [reportDetails, setReportDetails] = useState<IPaymentReport>();
  const [data, setData] = useState<IList<IPaymentReport>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    startDate: '',
    endDate: '',
    status: '' as Status,
    roleType: RoleType.CLIENT,
    countryId: '',
    mspId: '',
    clientId: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);
  const [reportDetailsLoading, setReportDetailsLoading] =
    useState<boolean>(false);
  const [countryList, setCountryList] = useState<ICountryDropdown[]>([]);
  const [mspList, setMspList] = useState<any[]>([]);
  const [clientList, setClientList] = useState<any[]>([]);
  const [showViewPaymentHistory, setShowViewPaymentHistory] =
    useState<boolean>(false);
  const [summaryData, setSummaryData] = useState<ISummaryData>();

  const apiClient = useAPI();

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

  const fetchSummaryData = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.SUMMARY_DATA + '?roleType=' + RoleType.CLIENT,
      );
      setSummaryData(response.data);
    } catch (error) {
      console.error('Error fetching summary data:', error);
    }
  };

  const fetchClientList = async (mspId?: string) => {
    if (!mspId) {
      setClientList([]);
      return;
    }

    try {
      const response = await apiClient.get(
        `${API_END_POINTS.CLIENT_LIST}mspId=${mspId}`,
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
    fetchSummaryData();
    fetchCountryList();
    fetchMspList();
  }, []);

  // Fetch MSP list when country changes
  useEffect(() => {
    if (queryParams.countryId) {
      fetchMspList(queryParams.countryId);
      // Reset MSP and Client when country changes
      setQueryParams(prev => ({
        ...prev,
        mspId: '',
        clientId: '',
      }));
      setClientList([]);
    }
  }, [queryParams.countryId]);

  // Fetch client list when MSP changes
  useEffect(() => {
    if (queryParams.mspId) {
      fetchClientList(queryParams.mspId);
      // Reset client when MSP changes
      setQueryParams(prev => ({
        ...prev,
        clientId: '',
      }));
    } else {
      setClientList([]);
    }
  }, [queryParams.mspId]);

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchPaymentReportData();
    }
  }, [searchDebounce]);

  useEffect(() => {
    if (selectedDate.startDate && selectedDate.endDate) {
      setQueryParams(prevState => ({
        ...prevState,
        startDate: selectedDate.startDate,
        endDate: selectedDate.endDate,
      }));
    }
  }, [selectedDate]);

  const fetchPaymentReportData = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.PAYMENT_HISTORY + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching payment history:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedReportId) {
      fetchPaymentReportDetails();
    }
  }, [selectedReportId]);

  const fetchPaymentReportDetails = async () => {
    setReportDetailsLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.PAYMENT_HISTORY_DETAILS + selectedReportId,
      );
      setReportDetails(response.data);
    } catch (error) {
      console.error('Error fetching payment report details:', error);
    } finally {
      setReportDetailsLoading(false);
    }
  };

  const handleExportHistory = async () => {
    setDownloadLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.PAYMENT_HISTORY_EXPORT_CSV,
        {
          responseType: 'blob',
        },
      );
      const blob = new Blob([response], {
        type: 'text/csv;charset=utf-8;',
      });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `payment_history_${dayjs().format('DD-MM-YYYY')}.csv`;
      a.click();
    } catch (error) {
      console.error('Error fetching coupon list:', error);
    } finally {
      setDownloadLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    setQueryParams(prevState => ({
      ...prevState,
      search: '',
      status: '' as Status,
      startDate: '',
      endDate: '',
    }));
    setSelectedDate({
      startDate: '',
      endDate: '',
    });
    setMspList([]);
    setClientList([]);
    fetchMspList();
  };

  const handleResetFilter = () => {
    setQueryParams(prev => ({
      ...prev,
      countryId: '',
      mspId: '',
      clientId: '',
    }));
    setMspList([]);
    setClientList([]);
    fetchMspList();
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Client Payment History
          </h1>
          <p className="text-base text-white text-opacity-75">
            Manage billing information and payment history
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <History className="size-5" />
                Payment History Overview
              </CardTitle>
              <CardDescription>
                View past payment records and invoices
              </CardDescription>
            </div>
            <Button onClick={handleExportHistory} variant="outline">
              <Download className="mr-2 size-4" />
              {downloadLoading ? 'Exporting...' : 'Export History'}
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="mb-6 grid grid-cols-1 gap-4 md:grid-cols-3">
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium">
                  Total Payments
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-green-600">
                  {summaryData?.totalPayments.toFixed(2) || 0}
                </div>
                <p className="text-xs text-muted-foreground">Total payments</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium">
                  Total Outstanding
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-red-600">
                  {summaryData?.outstandingAmount.toFixed(2) || 0}
                </div>
                <p className="text-xs text-muted-foreground">
                  Total outstanding amount
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium">
                  Paid Invoices
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-green-600">
                  {summaryData?.paidInvoicesCount.toFixed(2) || 0}
                </div>
                <p className="text-xs text-muted-foreground">Paid invoices</p>
              </CardContent>
            </Card>
            {/* <Card>
              <CardHeader className="pb-2">
                <CardTitle className="text-sm font-medium">
                  Not Yet Paid Invoices
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-red-600">
                  {summaryData?.totalInvoicesCount
                    ? summaryData?.totalInvoicesCount -
                      summaryData?.paidInvoicesCount
                    : 0}
                </div>
                <p className="text-xs text-muted-foreground">
                  Not yet paid invoices
                </p>
              </CardContent>
            </Card> */}
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Filters</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="mb-4 grid grid-cols-4 gap-2">
                <div className="col-span-1 flex flex-col items-start gap-2">
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
                <div className="col-span-1 flex flex-col items-start gap-2">
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
                <div className="col-span-1 flex flex-col items-start gap-2">
                  <Label htmlFor="clientId">Select Client</Label>
                  <SearchSelect
                    value={queryParams.clientId || ''}
                    onValueChange={value =>
                      setQueryParams(prev => ({ ...prev, clientId: value }))
                    }
                    items={clientList.map(client => ({
                      value: client.id,
                      label: client.name,
                    }))}
                    placeholder="Select Client"
                    disabled={!queryParams.mspId}
                  />
                </div>
                <div className="col-span-1 flex items-end justify-end gap-2">
                  <Button
                    variant="outline"
                    className="w-full"
                    onClick={handleResetFilter}
                  >
                    Reset Filters
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="my-6 grid w-full grid-cols-6 gap-2">
            <div className="col-span-2 flex flex-col items-start gap-2">
              <Label htmlFor="search">Search</Label>
              <Input
                className="w-full"
                id="search"
                type="text"
                placeholder="Search by Invoice No."
                value={queryParams.search || ''}
                onChange={e =>
                  setQueryParams(prev => ({
                    ...prev,
                    search: e.target.value,
                  }))
                }
              />
            </div>
            <div className="col-span-1 flex flex-col items-start gap-2">
              <Label htmlFor="status">Status</Label>
              <Select
                value={queryParams.status}
                onValueChange={value =>
                  setQueryParams(prev => ({
                    ...prev,
                    status:
                      value === Status.ALL ? ('' as Status) : (value as Status),
                  }))
                }
              >
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="Select Status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={Status.ALL}>All</SelectItem>
                  <SelectItem value={Status.SUCCESS}>Success</SelectItem>
                  <SelectItem value={Status.CANCELLED}>Cancelled</SelectItem>
                  <SelectItem value={Status.PENDING}>Pending</SelectItem>
                  <SelectItem value={Status.FAILED}>Failed</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="col-span-1 flex flex-col items-start gap-2">
              <Label htmlFor="startDate">Start Date</Label>
              <Input
                className="w-full"
                id="startDate"
                type="date"
                value={selectedDate.startDate}
                onChange={e =>
                  setSelectedDate(prev => ({
                    ...prev,
                    startDate: e.target.value,
                  }))
                }
                onClick={e => {
                  const input = e.target as HTMLInputElement;
                  input.showPicker();
                }}
                // min={selectedDate.endDate}
                max={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div className="col-span-1 flex flex-col items-start gap-2">
              <Label htmlFor="endDate">End Date</Label>
              <Input
                className="w-full"
                id="endDate"
                type="date"
                value={selectedDate.endDate}
                onChange={e =>
                  setSelectedDate(prev => ({
                    ...prev,
                    endDate: e.target.value,
                  }))
                }
                onClick={e => {
                  const input = e.target as HTMLInputElement;
                  input.showPicker();
                }}
                min={selectedDate.startDate}
                max={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div className="col-span-1 flex items-end justify-end">
              <Button
                variant="outline"
                className="w-full"
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
                  <TableHead>Invoice Number</TableHead>
                  <TableHead>MSP Name</TableHead>
                  <TableHead>Client Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead className="text-center">
                    Sub Total Amount
                  </TableHead>
                  <TableHead className="text-center">Discount Amount</TableHead>
                  <TableHead className="text-center">Vat Amount</TableHead>
                  <TableHead className="text-center">Total Amount</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Payment Date</TableHead>
                  <TableHead>Payment Method</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data?.items?.length > 0 ? (
                  <>
                    {data?.items?.map(payment => (
                      <TableRow key={payment.invoiceId}>
                        <TableCell className="font-medium">
                          {payment.invoiceId}
                        </TableCell>
                        <TableCell>{payment.mspName || 'N/A'}</TableCell>
                        <TableCell>{payment.clientName || 'N/A'}</TableCell>
                        <TableCell>{payment.email || 'N/A'}</TableCell>
                        <TableCell className="text-center">
                          ${payment.subtotal.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-center">
                          ${payment.discountAmount.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-center">
                          ${payment.vatAmount.toFixed(2)}
                        </TableCell>
                        <TableCell className="text-center">
                          ${payment.totalAmount.toFixed(2)}
                        </TableCell>
                        <TableCell>{getStatusBadge(payment.status)}</TableCell>
                        <TableCell>
                          {payment?.date
                            ? dayjs(payment.date).format('DD-MM-YYYY')
                            : 'N/A'}
                        </TableCell>
                        <TableCell>
                          {humanizeText(payment.paymentMethod)}
                        </TableCell>
                        <TableCell>
                          {' '}
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => {
                              setSelectedReportId(payment.id);
                              setShowViewPaymentHistory(true);
                            }}
                            title="View Details"
                          >
                            <Eye className="size-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </>
                ) : (
                  <TableRow>
                    <TableCell colSpan={12} className="p-4 text-center">
                      <div className="text-center text-muted-foreground">
                        No payment history available yet.
                      </div>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}
          <div className="pt-6">
            <Pagination
              total={data?.total}
              perPage={data?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      {selectedReportId && (
        <AspireViewPaymentHistory
          id={selectedReportId || ''}
          onClose={() => setShowViewPaymentHistory(false)}
          isOpen={showViewPaymentHistory}
        />
      )}
    </div>
  );
};

export default AspireAdminClientPaymentHistory;
