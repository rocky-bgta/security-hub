import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/Dropdown';
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
import { CustomCountrySelect } from 'components/CustomCountryStateSelect';
import IconBackButton from 'components/IconBackButton';
import AspireAdminClientPendingDetails from 'features/payment/AspireAdminClientPendingDetails';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useDownloader } from 'hooks/UseDownloader';
import { Download, Eye, Filter } from 'lucide-react';
import { IGetListParams, IList, Status } from 'models/Global';
import { IInvoice } from 'models/Payment';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

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
    case 'overdue':
      return <Badge variant="destructive">Overdue</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const MSPPaymentReportDetails = ({ hostPath = routes }) => {
  const navigate = useNavigate();
  const [selectedId, setSelectedId] = useState<string>('');
  const [pendingInvoices, setPendingInvoices] = useState<IList<IInvoice>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    status: Status.PENDING,
    startDate: '',
    endDate: '',
    countryFilter: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [openDetailsModal, setOpenDetailsModal] = useState<boolean>(false);
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);
  const [selectedDate, setSelectedDate] = useState<dateRange>({
    startDate: '',
    endDate: '',
  });
  const { downloadFile, progress } = useDownloader();

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchProductData();
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

  const fetchProductData = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.INVOICE_LIST + queryString,
      );
      setPendingInvoices({
        ...InitGetListParams,
        total: response.data.total,
        items: response.data,
      });
    } catch (error) {
      console.error('Error fetching pending invoices:', error);
    } finally {
      setLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleExportHistory = async () => {
    setDownloadLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.PAYMENT_HISTORY_EXPORT,
      );
      downloadFile(response.data);
    } catch (error) {
      console.error('Error fetching coupon list:', error);
    } finally {
      setDownloadLoading(false);
    }
  };

  return (
    <div className="space-y-6 p-6">
      <div className="space-y-3">
        <IconBackButton
          onClick={() => navigate(hostPath.mspPaymentReport.path)}
          label="Back to MSP List"
        />
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Payment History
          </h1>
          <p className="text-white text-opacity-75">
            Payment history for TechFlow Solutions
          </p>
        </div>
      </div>

      <div className="mb-6 grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Total Payments
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">$45,000.00</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Available Credit
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-[#16a34a]">$5,000.00</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Balance Remaining
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-[#eab308]">$2,500.00</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">
              Payment Status
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">Pending</div>
          </CardContent>
        </Card>
      </div>
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                Transaction History
              </CardTitle>
            </div>
            <Button onClick={handleExportHistory} variant="outline">
              <Download className="mr-2 size-4" />
              {downloadLoading ? 'Exporting...' : 'Export History'}
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="mb-6 flex gap-2">
            <div className="flex flex-col items-start gap-2">
              <Label htmlFor="status">Status</Label>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" className="w-32 justify-start">
                    <Filter className="mr-2 size-4" />
                    {queryParams.status === Status.ALL
                      ? 'All'
                      : queryParams.status === Status.PAID
                        ? 'Paid'
                        : queryParams.status === Status.PENDING
                          ? 'Pending'
                          : queryParams.status === Status.FAILED
                            ? 'Failed'
                            : 'Filter'}
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent>
                  <DropdownMenuItem
                    onClick={() =>
                      setQueryParams({
                        ...queryParams,
                        status: Status.ALL,
                      })
                    }
                  >
                    All
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() =>
                      setQueryParams({
                        ...queryParams,
                        status: Status.PAID,
                      })
                    }
                  >
                    Paid
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() =>
                      setQueryParams({
                        ...queryParams,
                        status: Status.PENDING,
                      })
                    }
                  >
                    Pending
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() =>
                      setQueryParams({
                        ...queryParams,
                        status: Status.FAILED,
                      })
                    }
                  >
                    Failed
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
            <div className="flex flex-col items-start gap-2">
              <Label htmlFor="startDate">Start Date</Label>
              <Input
                className="w-64"
                id="startDate"
                type="date"
                value={selectedDate.startDate}
                onChange={e =>
                  setSelectedDate(prev => ({
                    ...prev,
                    startDate: e.target.value,
                  }))
                }
                // min={selectedDate.endDate}
                max={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div className="flex flex-col items-start gap-2">
              <Label htmlFor="endDate">End Date</Label>
              <Input
                className="w-64"
                id="endDate"
                type="date"
                value={selectedDate.endDate}
                onChange={e =>
                  setSelectedDate(prev => ({
                    ...prev,
                    endDate: e.target.value,
                  }))
                }
                min={selectedDate.startDate}
                max={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div className="flex flex-col items-start gap-2">
              <Label htmlFor="countryFilter">Filter by Country</Label>
              <div className="w-64">
                <CustomCountrySelect
                  className="h-10 rounded-md px-4 py-2"
                  value={queryParams.countryFilter || ''}
                  handleChange={value =>
                    setQueryParams(prev => ({ ...prev, countryFilter: value }))
                  }
                />
              </div>
            </div>
          </div>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>MSP Name</TableHead>
                <TableHead>Invoice No.</TableHead>
                <TableHead>Amount Due</TableHead>
                <TableHead>Due Date</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Days Past Due</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} className="p-4 text-center">
                    <SpinnerLoader />
                  </TableCell>
                </TableRow>
              ) : (
                <>
                  {pendingInvoices?.items?.length > 0 ? (
                    <>
                      {' '}
                      {pendingInvoices?.items?.map((invoice, index) => (
                        <TableRow key={invoice.id}>
                          <TableCell>{invoice.clientName || 'N/A'}</TableCell>
                          <TableCell className="font-medium">
                            {invoice.id}
                          </TableCell>
                          <TableCell>{invoice.totalAmount}</TableCell>
                          <TableCell>{invoice.createdAt}</TableCell>
                          <TableCell>
                            {getStatusBadge(invoice.status)}
                          </TableCell>
                          <TableCell>
                            {index === 0 ? '561 days' : 'N/A'}
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center gap-2">
                              <Button
                                className="bg-transparent text-white text-opacity-75"
                                size="sm"
                                variant="outline"
                                onClick={() => {
                                  setSelectedId(invoice.id);
                                  setOpenDetailsModal(true);
                                }}
                              >
                                <Eye className="mr-1 size-3" />
                                View
                              </Button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </>
                  ) : (
                    <TableRow>
                      <TableCell colSpan={6} className="p-4 text-center">
                        <div className="text-center text-muted-foreground">
                          No pending payments found.
                        </div>
                      </TableCell>
                    </TableRow>
                  )}
                </>
              )}
            </TableBody>
          </Table>
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
        <AspireAdminClientPendingDetails
          paymentId={selectedId}
          open={openDetailsModal}
          onClose={() => setOpenDetailsModal(false)}
        />
      )}
    </div>
  );
};

export default MSPPaymentReportDetails;
