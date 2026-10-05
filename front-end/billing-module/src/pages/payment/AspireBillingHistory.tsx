import dayjs from 'dayjs';

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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import SpinnerLoader from 'common/loader/SpinnerLoader';
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
import { Download, Eye, History, Mail } from 'lucide-react';
import { IGetListParams, IList, Status } from 'models/Global';
import { IPaymentReport } from 'models/Payment';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { ICountryDropdown } from 'models/Country';
import SearchSelect from 'components/SearchSelect';
import TableLoader from 'components/skeleton/TableLoader';
import AspireViewPaymentHistory from 'features/payment/aspire-admin/AspireViewPaymentHistory';

interface dateRange {
  startDate: string;
  endDate: string;
}

const getStatusBadge = (status: string) => {
  switch (status) {
    case 'paid':
      return <Badge variant="default">Paid</Badge>;
    case 'pending':
      return <Badge variant="secondary">Pending</Badge>;
    case 'failed':
      return <Badge variant="destructive">Failed</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const AspireBillingHistory = () => {
  const [selectedDate, setSelectedDate] = useState<dateRange>({
    startDate: '',
    endDate: '',
  });
  const [selectedReportId, setSelectedReportId] = useState<string>();
  const [showViewPaymentHistory, setShowViewPaymentHistory] =
    useState<boolean>(false);
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
    countryId: '',
    search: '',
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
    fetchCountryList();
    fetchMspList();
  }, []);

  // Fetch MSP list when country changes
  useEffect(() => {
    if (queryParams.countryFilter) {
      fetchMspList(queryParams.countryFilter);
      // Reset MSP and Client when country changes
      setQueryParams(prev => ({
        ...prev,
        mspId: '',
        clientId: '',
      }));
      setClientList([]);
    }
  }, [queryParams.countryFilter]);

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
    setQueryParams({
      ...InitGetListParams,
      status: '' as Status,
      startDate: '',
      endDate: '',
      countryId: '',
      clientName: '',
      mspId: '',
      clientId: '',
    });
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

  const handleDownloadReceipt = async (id: string) => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.DOWNLOAD_INVOICE_PDF.replace(':id', id),
        {
          responseType: 'blob',
        },
      );

      const blob = new Blob([response], {
        type: 'application/pdf',
      });

      const url = window.URL.createObjectURL(blob);

      const link = document.createElement('a');
      link.href = url;
      link.download = `invoice-${id}.pdf`;
      document.body.appendChild(link);
      link.click();

      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading receipt:', error);
    }
  };

  return (
    <Fragment>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Billing History
          </h1>
          <p className="text-base text-white text-opacity-75">
            Manage and track all client billing transactions and payment
            history.
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <History className="size-5" />
                Billing Transactions
              </CardTitle>
              <CardDescription>
                Complete history of all billing transactions and invoices.
              </CardDescription>
            </div>
            <Button onClick={handleExportHistory} variant="outline">
              <Download className="mr-2 size-4" />
              {downloadLoading ? 'Exporting...' : 'Export CSV'}
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <Card>
            <CardHeader>
              <CardTitle>Filters</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="mb-4 grid grid-cols-4 gap-2">
                <div className="col-span-1 flex flex-col items-start gap-2">
                  <Label htmlFor="countryFilter">Select Country</Label>
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
                    disabled={!queryParams.countryFilter}
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
                placeholder="Search by invoice number, client name, or msp name"
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
                  <SelectItem value={Status.PENDING}>Pending</SelectItem>
                  <SelectItem value={Status.FAILED}>Failed</SelectItem>
                  <SelectItem value={Status.CANCELLED}>Cancelled</SelectItem>
                  <SelectItem value={Status.PARTIAL}>Partial</SelectItem>
                  <SelectItem value={Status.OVERDUE}>Overdue</SelectItem>
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
                  <TableHead>SL No.</TableHead>
                  <TableHead>Invoice Number</TableHead>
                  <TableHead>MSP Name</TableHead>
                  <TableHead>Client Name</TableHead>
                  <TableHead>Invoice Date</TableHead>
                  <TableHead>Payment Date</TableHead>
                  <TableHead>Total Amount</TableHead>
                  {/* <TableHead>Amount Paid</TableHead> */}
                  <TableHead>Status</TableHead>
                  <TableHead className="text-center">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <>
                  {data?.items?.length > 0 ? (
                    data?.items?.map((payment, index) => (
                      <TableRow key={payment.invoiceId}>
                        <TableCell className="font-medium">
                          {index + 1}
                        </TableCell>
                        <TableCell className="font-medium">
                          {payment.invoiceId}
                        </TableCell>
                        <TableCell>{payment.mspName || 'N/A'}</TableCell>
                        <TableCell>{payment.clientName || 'N/A'}</TableCell>
                        <TableCell>
                          {payment?.invoiceDate
                            ? dayjs(payment.invoiceDate).format('DD-MM-YYYY')
                            : 'N/A'}
                        </TableCell>
                        <TableCell>
                          {payment?.date
                            ? dayjs(payment.date).format('DD-MM-YYYY')
                            : 'N/A'}
                        </TableCell>
                        <TableCell>
                          {payment.amount
                            ? payment.totalAmount.toFixed(2)
                            : 'N/A'}
                        </TableCell>
                        {/* <TableCell>
                          {payment.amount ? payment.amount.toFixed(2) : 'N/A'}
                        </TableCell> */}
                        <TableCell>{getStatusBadge(payment.status)}</TableCell>
                        <TableCell>
                          <div className="flex items-center gap-1">
                            <Button
                              title="View Invoice"
                              variant="ghost"
                              size="sm"
                              onClick={() => {
                                setSelectedReportId(payment.id);
                                setShowViewPaymentHistory(true);
                              }}
                            >
                              <Eye className="size-4" />
                            </Button>

                            <Button
                              onClick={() =>
                                handleDownloadReceipt(payment.invoiceId)
                              }
                              title="Download Receipt"
                              variant="ghost"
                              size="sm"
                            >
                              <Download className="size-4" />
                            </Button>
                            {/* {payment.status !== 'Paid' && (
                              <Button variant="ghost" size="sm">
                                <Mail className="h-4 w-4" />
                              </Button>
                            )} */}
                          </div>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={10} className="p-4 text-center">
                        <div className="text-center text-muted-foreground">
                          No payment history available yet.
                        </div>
                      </TableCell>
                    </TableRow>
                  )}
                </>
              </TableBody>
            </Table>
          )}
          <div className="pt-6">
            <Pagination
              total={data.total}
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
    </Fragment>
  );
};

export default AspireBillingHistory;
