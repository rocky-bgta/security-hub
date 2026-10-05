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
import { DollarSign, Download, Eye } from 'lucide-react';
import { ICountryDropdown } from 'models/Country';
import { IGetListParams, IList, Status } from 'models/Global';
import { IPaymentReport } from 'models/Payment';
import { useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  formatCurrency,
  formatDate,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import AspireViewPendingPayment from './AspireViewPendingPayment';
import dayjs from 'dayjs';

interface dateRange {
  startDate: string;
  endDate: string;
}

const getStatusBadge = (status: string) => {
  switch (status) {
    case Status.PAID:
      return <Badge variant="default">Paid</Badge>;
    case Status.PENDING:
      return <Badge variant="secondary">Pending</Badge>;
    case Status.OVERDUE:
      return <Badge variant="destructive">Overdue</Badge>;
    case Status.CANCELLED:
      return <Badge variant="destructive">Cancelled</Badge>;
    case Status.ON_PROGRESS:
      return <Badge variant="secondary">On Progress</Badge>;
    case Status.PARTIAL:
      return <Badge variant="secondary">Partial</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const AspirePendingPayment = () => {
  const [selectedId, setSelectedId] = useState<string>('');
  const [selectedDate, setSelectedDate] = useState<dateRange>({
    startDate: '',
    endDate: '',
  });
  const [pendingInvoices, setPendingInvoices] = useState<IList<IPaymentReport>>(
    {
      ...InitGetListParams,
      total: 0,
      items: [],
    },
  );
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    startDate: '',
    endDate: '',
    status: '' as Status,
    countryId: '',
    mspId: '',
    clientId: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [openDetailsModal, setOpenDetailsModal] = useState<boolean>(false);
  const [countryList, setCountryList] = useState<ICountryDropdown[]>([]);
  const [mspList, setMspList] = useState<any[]>([]);
  const [clientList, setClientList] = useState<any[]>([]);
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);
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
    fetchProductData();
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

  const fetchProductData = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.INVOICE_LIST + queryString,
      );
      setPendingInvoices({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching pending invoices:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setQueryParams({
      ...InitGetListParams,
      status: '' as Status,
      startDate: '',
      endDate: '',
      countryId: '',
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

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleExportPendingPayment = async () => {
    setDownloadLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.PENDING_PAYMENT_EXPORT_CSV +
          'status=PENDING&status=ON_PROGRESS',
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
      a.download = `pending_payments_${dayjs().format('DD-MM-YYYY')}.csv`;
      document.body.appendChild(a);
      a.click();

      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading pending payments:', error);
    } finally {
      setDownloadLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Client Pending Payments
          </h1>
          <p className="text-white text-opacity-75">
            View and manage pending payment invoices
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2 text-white">
                <DollarSign className="size-5" />
                Pending Payments Overview
              </CardTitle>
              <CardDescription className="text-gray-400">
                Invoices awaiting payment from clients
              </CardDescription>
            </div>
            <div className="flex items-center gap-2">
              <Button
                disabled={downloadLoading}
                variant="outline"
                size="sm"
                onClick={handleExportPendingPayment}
              >
                <Download className="size-4" />
                {downloadLoading ? 'Exporting...' : 'Export CSV'}
              </Button>
            </div>
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
                  <SelectItem value={Status.PAID}>Paid</SelectItem>
                  <SelectItem value={Status.PENDING}>Pending</SelectItem>
                  <SelectItem value={Status.CANCELLED}>Cancelled</SelectItem>
                  <SelectItem value={Status.ON_PROGRESS}>
                    On Progress
                  </SelectItem>
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
                  <TableHead>Invoice No.</TableHead>
                  <TableHead>MSP Name</TableHead>
                  <TableHead>Client Name</TableHead>
                  <TableHead className="text-center">
                    Sub Total Amount
                  </TableHead>
                  <TableHead className="text-center">Vat Amount</TableHead>
                  <TableHead className="text-center">Discount Amount</TableHead>
                  <TableHead className="text-center">Total Amount</TableHead>
                  <TableHead>Due Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <>
                  {pendingInvoices?.items?.length > 0 ? (
                    <>
                      {' '}
                      {pendingInvoices?.items?.map(invoice => (
                        <TableRow key={invoice.id}>
                          <TableCell className="font-medium">
                            {invoice.id}
                          </TableCell>
                          <TableCell>{invoice.mspName}</TableCell>
                          <TableCell>{invoice.clientName}</TableCell>
                          <TableCell className="text-center">
                            {invoice.subtotal.toFixed(2)}
                          </TableCell>
                          <TableCell className="text-center">
                            {invoice.vatAmount.toFixed(2)}
                          </TableCell>
                          <TableCell className="text-center">
                            {invoice.discountAmount.toFixed(2)}
                          </TableCell>
                          <TableCell className="text-center">
                            {invoice.totalAmount.toFixed(2)}
                          </TableCell>
                          <TableCell>{formatDate(invoice.createdAt)}</TableCell>
                          <TableCell>
                            {getStatusBadge(invoice.status)}
                          </TableCell>
                          <TableCell>
                            <Button
                              size="sm"
                              variant="ghost"
                              onClick={() => {
                                setSelectedId(invoice.id);
                                setOpenDetailsModal(true);
                              }}
                              title="View Invoice"
                            >
                              <Eye className="mr-1 size-3" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))}
                    </>
                  ) : (
                    <TableRow>
                      <TableCell colSpan={6} className="text-center">
                        <div className="text-center text-muted-foreground">
                          No pending payments found.
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
              total={pendingInvoices?.total}
              perPage={pendingInvoices?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>

      {selectedId && (
        <AspireViewPendingPayment
          id={selectedId}
          isOpen={openDetailsModal}
          onClose={() => setOpenDetailsModal(false)}
        />
      )}
    </div>
  );
};

export default AspirePendingPayment;
