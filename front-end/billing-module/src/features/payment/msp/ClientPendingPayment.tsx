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
import { useStore } from 'hooks/UseStore';
import {
  DollarSign,
  Download,
  Eye,
  History,
  Mail,
  MessageSquareText,
  RefreshCcw,
} from 'lucide-react';
import {
  IGetListParams,
  IList,
  ModalType,
  RoleType,
  Status,
} from 'models/Global';
import { IPaymentReport } from 'models/Payment';
import { Fragment, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import {
  humanizeText,
  isSuccessResponse,
  objectToQueryString,
} from 'utils/Helper';
import TableLoader from 'components/skeleton/TableLoader';
import { ICountryDropdown } from 'models/Country';
import SearchSelect from 'components/SearchSelect';
import InvoiceCommentModal from 'features/payment/aspire-admin/InvoiceCommentModal';
import { toast } from 'react-toastify';
import ConfirmDialog from 'components/ConfirmDialog';
import AspireViewPendingPayment from 'features/payment/aspire-admin/AspireViewPendingPayment';
import {
  Select,
  SelectTrigger,
  SelectItem,
  SelectContent,
  SelectValue,
} from 'common/Select';
import InvoiceDetailsDialog from 'features/payment/PendingPaymentDetails';
import { ROLE } from 'utils/Role';
import { useAuth } from 'hooks/UseAuth';
import { getPaymentStatusBadge } from '../../../pages/payment/ClientPendingPayment';

interface dateRange {
  startDate: string;
  endDate: string;
}

const MspAdminClientPendingPayment = () => {
  const { role } = useAuth();
  const { userInfo } = useStore();
  const isFinanceAdmin = role === ROLE.FINANCE_ADMIN;
  const isAspireAdmin = role === ROLE.ASPIRE_ADMIN;
  const apiClient = useAPI();
  const [selectedDate, setSelectedDate] = useState<dateRange>({
    startDate: '',
    endDate: '',
  });
  const [selectedReportId, setSelectedReportId] = useState<string>('');
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
    countryId: '',
    status: '' as Status,
    search: '',
    roleType: RoleType.CLIENT,
    mspId: userInfo?.userId || '',
    clientId: '',
  });
  const searchDebounce = useDebounce(queryString, 500);
  const [loading, setLoading] = useState<boolean>(true);
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);
  const [countryList, setCountryList] = useState<ICountryDropdown[]>([]);
  const [clientList, setClientList] = useState<any[]>([]);
  const [openModal, setOpenModal] = useState<ModalType>(ModalType.NONE);
  const [isSendingEmail, setIsSendingEmail] = useState<boolean>(false);
  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (selectedDate.startDate && selectedDate.endDate) {
      setQueryParams(prevState => ({
        ...prevState,
        startDate: selectedDate.startDate,
        endDate: selectedDate.endDate,
      }));
    }
  }, [selectedDate]);

  useEffect(() => {
    if (userInfo?.userId && searchDebounce) {
      fetchPaymentReportData();
    }
  }, [searchDebounce, userInfo?.userId]);

  useEffect(() => {
    fetchCountryList();
  }, []);

  useEffect(() => {
    if (!userInfo?.userId) return;

    fetchClientList(userInfo.userId);
  }, [userInfo?.userId]);

  useEffect(() => {
    if (!userInfo?.userId) return;

    setQueryParams(prevState =>
      prevState.mspId === userInfo.userId
        ? prevState
        : {
            ...prevState,
            mspId: userInfo.userId,
          },
    );
  }, [userInfo?.userId]);

  useEffect(() => {
    setQueryParams(prev => ({
      ...prev,
      clientId: '',
    }));
  }, [queryParams.countryId]);

  const fetchPaymentReportData = async () => {
    setLoading(true);

    try {
      let statusParam = '';
      const currentStatus = queryParams.status as string;
      if (
        !currentStatus ||
        currentStatus === Status.ALL ||
        currentStatus === ''
      ) {
        statusParam = `&status=${Status.PENDING}&status=${Status.ON_PROGRESS}`;
      }

      const response = await apiClient.get(
        API_END_POINTS.INVOICE_LIST + queryString + statusParam,
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

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleReset = () => {
    setSelectedDate({
      startDate: '',
      endDate: '',
    });
    setQueryParams(prevState => ({
      ...prevState,
      search: '',
      status: '' as Status,
      startDate: '',
      endDate: '',
    }));
  };

  const handleResetFilter = () => {
    setQueryParams(prev => ({
      ...prev,
      countryId: '',
      clientId: '',
      mspId: userInfo?.userId || '',
    }));
    if (userInfo?.userId) {
      fetchClientList(userInfo.userId);
    }
  };

  const handleViewInvoice = (id: string) => {
    setSelectedReportId(id);
    setOpenModal(ModalType.VIEW);
  };

  const handleComment = (id: string) => {
    setSelectedReportId(id);
    setOpenModal(ModalType.COMMENT);
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

  const handleSendEmail = async (id: string) => {
    try {
      setIsSendingEmail(true);
      const response = await apiClient.post(
        API_END_POINTS.SEND_INVOICE_EMAIL.replace(':id', id),
      );
      toast.success('Email sent successfully');
      setOpenModal(ModalType.NONE);
    } catch (error) {
      console.error('Error sending email:', error);
    } finally {
      setIsSendingEmail(false);
    }
  };

  const handleExportPendingPayment = async () => {
    setDownloadLoading(true);

    try {
      let statusParam = '';
      const currentStatus = queryParams.status as string;
      if (
        !currentStatus ||
        currentStatus === Status.ALL ||
        currentStatus === ''
      ) {
        statusParam = `&status=${Status.PENDING}&status=${Status.ON_PROGRESS}`;
      }
      const response = await apiClient.get(
        API_END_POINTS.PENDING_PAYMENT_EXPORT_CSV + queryString + statusParam,
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
      document.body.appendChild(a);
      a.click();

      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading payment history:', error);
    } finally {
      setDownloadLoading(false);
    }
  };

  const handlePaymentNow = (id: string) => {
    setSelectedReportId(id);
    setOpenModal(ModalType.PAYMENT_NOW);
  };

  return (
    <Fragment>
      <div className="flex items-center justify-between pb-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Pending Payment History
          </h1>
          <p className="text-base text-white text-opacity-75">
            Track and manage all pending and overdue payments from clients.
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="flex items-center gap-2">
                <History className="size-5" />
                Pending and overdue payments
              </CardTitle>
              <CardDescription>
                Payments that are due or overdue and require follow-up action.
              </CardDescription>
            </div>
            <Button variant="outline" onClick={handleExportPendingPayment}>
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
              <div className="mb-4 grid grid-cols-3 gap-2">
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
                    disabled={!userInfo?.userId}
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

          <div className="my-6 grid w-full grid-cols-1 gap-4 md:grid-cols-6">
            <div className="col-span-2 flex flex-col items-start gap-2">
              <Label htmlFor="search">Search</Label>
              <Input
                className="w-full"
                id="search"
                type="text"
                placeholder="Enter Search"
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
                value={
                  !queryParams.status || (queryParams.status as string) === ''
                    ? Status.ALL
                    : queryParams.status
                }
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
                  <SelectItem value={Status.PENDING}>Pending</SelectItem>
                  <SelectItem value={Status.ON_PROGRESS}>
                    On Progress
                  </SelectItem>
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
                <RefreshCcw className="mr-2 size-4" />
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
                  <TableHead className="text-center">Coupon Amount</TableHead>
                  <TableHead className="text-center">Discount Amount</TableHead>
                  <TableHead className="text-center">Vat Amount</TableHead>
                  <TableHead className="text-center">Total Amount</TableHead>
                  <TableHead>Payment Method</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Invoice Date</TableHead>
                  <TableHead className="text-center">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data?.items?.length > 0 ? (
                  data?.items?.map(payment => (
                    <TableRow key={payment.id}>
                      <TableCell className="font-medium">
                        {payment.id}
                      </TableCell>
                      <TableCell>{payment.mspName || 'N/A'}</TableCell>
                      <TableCell>{payment.clientName || 'N/A'}</TableCell>
                      <TableCell>{payment.email || 'N/A'}</TableCell>

                      <TableCell className="text-center">
                        ${payment.subtotal.toFixed(2)}
                      </TableCell>
                      <TableCell className="text-center">
                        ${payment.couponDiscountAmount.toFixed(2) || 0}
                      </TableCell>
                      <TableCell className="text-center">
                        ${payment.discountAmount.toFixed(2)}
                      </TableCell>
                      <TableCell className="text-center">
                        ${payment.vatAmount.toFixed(2)}
                      </TableCell>
                      <TableCell>
                        $
                        {payment.totalAmount
                          ? payment.totalAmount.toFixed(2)
                          : 'N/A'}
                      </TableCell>
                      <TableCell>
                        {payment.paymentMethod === null
                          ? 'Online Payment'
                          : humanizeText(payment.paymentMethod)}
                      </TableCell>
                      <TableCell className="text-nowrap">
                        {getPaymentStatusBadge(payment.status)}
                      </TableCell>
                      <TableCell>
                        {payment?.createdAt
                          ? dayjs(payment.createdAt).format('DD-MM-YYYY')
                          : 'N/A'}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center justify-center gap-1">
                          {payment.paymentMethod !== null ||
                            isFinanceAdmin ||
                            (isAspireAdmin && (
                              <Button
                                variant="ghost"
                                title="Payment Now"
                                size="sm"
                                onClick={() => handlePaymentNow(payment.id)}
                              >
                                <DollarSign className="size-4 text-primary" />
                              </Button>
                            ))}
                          <Button
                            variant="ghost"
                            title="View Invoice"
                            size="sm"
                            onClick={() => handleViewInvoice(payment.id)}
                          >
                            <Eye className="size-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            title="Add Comment"
                            size="sm"
                            disabled={payment.paymentMethod === null}
                            onClick={() => handleComment(payment.id)}
                          >
                            <MessageSquareText
                              className={`size-4 ${payment.paymentMethod === null ? 'text-muted-foreground' : 'text-primary'}`}
                            />
                          </Button>

                          <Button
                            variant="ghost"
                            title="Download Receipt"
                            size="sm"
                            onClick={() => handleDownloadReceipt(payment.id)}
                          >
                            <Download className="size-4" />
                          </Button>

                          <Button
                            variant="ghost"
                            title="Send Email"
                            size="sm"
                            onClick={() => {
                              setSelectedReportId(payment.id);
                              setOpenModal(ModalType.SEND_EMAIL);
                            }}
                          >
                            <Mail className="size-4" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
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
      {openModal === ModalType.VIEW && (
        <AspireViewPendingPayment
          isOpen={openModal === ModalType.VIEW}
          onClose={() => setOpenModal(ModalType.NONE)}
          id={selectedReportId || ''}
        />
      )}
      {openModal === ModalType.COMMENT && (
        <InvoiceCommentModal
          open={openModal === ModalType.COMMENT}
          onClose={() => setOpenModal(ModalType.NONE)}
          id={selectedReportId || ''}
          reload={fetchPaymentReportData}
        />
      )}

      {openModal === ModalType.PAYMENT_NOW && (
        <InvoiceDetailsDialog
          open={openModal === ModalType.PAYMENT_NOW}
          onClose={() => setOpenModal(ModalType.NONE)}
          paymentId={selectedReportId || ''}
        />
      )}

      <ConfirmDialog
        isOpen={openModal === ModalType.SEND_EMAIL}
        message="Are you sure you want to send the invoice in client's email?"
        loading={isSendingEmail}
        loadingText="Sending..."
        onClose={() => setOpenModal(ModalType.NONE)}
        onConfirm={() => handleSendEmail(selectedReportId)}
      />
    </Fragment>
  );
};

export default MspAdminClientPendingPayment;
