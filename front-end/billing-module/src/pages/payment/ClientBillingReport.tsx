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
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useDownloader } from 'hooks/UseDownloader';
import { Download, Filter, History } from 'lucide-react';
import { IGetListParams, IList, Status } from 'models/Global';
import { IPaymentReport } from 'models/Payment';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
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
    case 'failed':
      return <Badge variant="destructive">Failed</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const ClientBillingReport = () => {
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
    status: Status.ALL,
    countryId: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);
  const [reportDetailsLoading, setReportDetailsLoading] =
    useState<boolean>(false);

  const { downloadFile, progress } = useDownloader();

  const apiClient = useAPI();

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
        items: response.data.data,
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

  const handleUpdateBilling = () => {};

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

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  return (
    <Fragment>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Payment Management
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
                All Clients Billings History
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
              <Label htmlFor="countryId">Filter by Country</Label>
              <div className="w-64">
                <CustomCountrySelect
                  className="h-10 rounded-md px-4 py-2"
                  value={queryParams.countryId || ''}
                  handleChange={value =>
                    setQueryParams(prev => ({ ...prev, countryId: value }))
                  }
                />
              </div>
            </div>
            <div className="flex flex-col items-start gap-2">
              <Label htmlFor="clientID">Client ID</Label>
              <Input
                className="w-64"
                id="clientID"
                type="text"
                placeholder="Enter Client ID"
                value={queryParams.clientId || ''}
                onChange={e =>
                  setQueryParams(prev => ({
                    ...prev,
                    clientId: e.target.value,
                  }))
                }
              />
            </div>
          </div>

          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Invoice Number</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Due Amount</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Payment Method</TableHead>
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
                  {data?.items?.length > 0 ? (
                    <>
                      {data?.items?.map(payment => (
                        <TableRow key={payment.invoiceId}>
                          <TableCell className="font-medium">
                            {payment.invoiceId}
                          </TableCell>
                          <TableCell>
                            {payment?.date
                              ? dayjs(payment.date).format('DD-MM-YYYY')
                              : 'N/A'}
                          </TableCell>
                          <TableCell>
                            {payment.amount ? payment.amount.toFixed(2) : 'N/A'}
                          </TableCell>
                          <TableCell>{payment.dueAmount}</TableCell>
                          <TableCell>
                            {getStatusBadge(payment.status)}
                          </TableCell>
                          <TableCell>{payment.paymentMethod}</TableCell>
                          <TableCell>
                            <Dialog>
                              <DialogTrigger asChild>
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() =>
                                    setSelectedReportId(payment.invoiceId)
                                  }
                                >
                                  View Details
                                </Button>
                              </DialogTrigger>
                              <DialogContent className="max-w-2xl">
                                <DialogHeader>
                                  <DialogTitle>Invoice Details</DialogTitle>
                                  <DialogDescription>
                                    Complete details for{' '}
                                    {reportDetails?.invoiceId}
                                  </DialogDescription>
                                </DialogHeader>
                                {reportDetailsLoading ? (
                                  <SpinnerLoader />
                                ) : (
                                  <Fragment>
                                    {reportDetails && (
                                      <div className="space-y-4">
                                        <div className="grid grid-cols-2 gap-4 text-sm">
                                          <div>
                                            <span className="font-medium">
                                              Invoice:
                                            </span>
                                            <p>{reportDetails?.invoiceId}</p>
                                          </div>
                                          <div>
                                            <span className="font-medium">
                                              Date:
                                            </span>
                                            <p>
                                              {reportDetails?.createdAt
                                                ? dayjs(
                                                    reportDetails?.createdAt,
                                                  ).format('DD-MM-YYYY')
                                                : 'N/A'}
                                            </p>
                                          </div>
                                          <div>
                                            <span className="font-medium">
                                              Amount:
                                            </span>
                                            <p>
                                              {reportDetails?.amount?.toFixed(
                                                2,
                                              )}
                                            </p>
                                          </div>
                                          <div>
                                            <span className="font-medium">
                                              Status:
                                            </span>
                                            <p>
                                              {getStatusBadge(
                                                reportDetails?.status,
                                              )}
                                            </p>
                                          </div>
                                          <div className="col-span-2">
                                            <span className="font-medium">
                                              Description:
                                            </span>
                                            <p>
                                              {reportDetails?.description ||
                                                'N/A'}
                                            </p>
                                          </div>
                                          <div className="col-span-2">
                                            <span className="font-medium">
                                              Transaction ID:
                                            </span>
                                            <p className="whitespace-normal break-all">
                                              {reportDetails?.transactionId}
                                            </p>
                                          </div>
                                        </div>
                                        <div className="flex gap-2">
                                          <Button
                                            size="sm"
                                            variant="outline"
                                            onClick={() =>
                                              downloadFile(
                                                reportDetails?.invoicePdfLink,
                                                'Receipt',
                                              )
                                            }
                                          >
                                            Download Receipt
                                          </Button>
                                          <Button
                                            size="sm"
                                            variant="outline"
                                            onClick={() =>
                                              downloadFile(
                                                reportDetails?.invoicePdfLink,
                                                'Invoice',
                                              )
                                            }
                                          >
                                            Download Invoice
                                          </Button>
                                        </div>
                                      </div>
                                    )}
                                  </Fragment>
                                )}
                              </DialogContent>
                            </Dialog>
                          </TableCell>
                        </TableRow>
                      ))}
                    </>
                  ) : (
                    <TableRow>
                      <TableCell colSpan={7} className="p-4 text-center">
                        <div className="text-center text-muted-foreground">
                          No payment history available yet.
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
              total={data?.total}
              perPage={data?.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>
    </Fragment>
  );
};

export default ClientBillingReport;
