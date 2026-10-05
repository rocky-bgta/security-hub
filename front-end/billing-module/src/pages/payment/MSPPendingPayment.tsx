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
  SelectTrigger,
  SelectItem,
  SelectContent,
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
import {
  DollarSign,
  Download,
  Eye,
  Mail,
  MessageSquareText,
  RefreshCcw,
} from 'lucide-react';
import { ICountryDropdown } from 'models/Country';
import {
  IGetListParams,
  IList,
  ModalType,
  RoleType,
  Status,
} from 'models/Global';
import { IPaymentReport } from 'models/Payment';
import { useCallback, useEffect, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { humanizeText, objectToQueryString } from 'utils/Helper';
import { getPaymentStatusBadge } from './ClientPendingPayment';
import dayjs from 'dayjs';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import AspireViewPendingPayment from 'features/payment/aspire-admin/AspireViewPendingPayment';
import InvoiceCommentModal from 'features/payment/aspire-admin/InvoiceCommentModal';
import InvoiceDetailsDialog from 'features/payment/PendingPaymentDetails';
import ConfirmDialog from 'components/ConfirmDialog';
import { toast } from 'react-toastify';

interface dateRange {
  startDate: string;
  endDate: string;
}

const getStatusParam = (currentQueryString: string) => {
  const currentStatus = new URLSearchParams(currentQueryString).get('status');
  if (!currentStatus || currentStatus === Status.ALL || currentStatus === '') {
    return `&status=${Status.PENDING}&status=${Status.ON_PROGRESS}`;
  }
  return '';
};

const MSPPendingPayment = () => {
  const { role } = useAuth();
  const isFinanceAdmin = role === ROLE.FINANCE_ADMIN;
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
    status: '' as Status,
    startDate: '',
    endDate: '',
    countryId: '',
    roleType: RoleType.MSP,
    search: '',
    mspId: '',
  });
  const searchDebounce = useDebounce(queryString, 500);
  const [loading, setLoading] = useState<boolean>(true);
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);
  const [selectedDate, setSelectedDate] = useState<dateRange>({
    startDate: '',
    endDate: '',
  });
  const [countryList, setCountryList] = useState<ICountryDropdown[]>([]);
  const [mspList, setMspList] = useState<any[]>([]);
  const [selectedReportId, setSelectedReportId] = useState<string>('');
  const [openModal, setOpenModal] = useState<ModalType>(ModalType.NONE);
  const [isSendingEmail, setIsSendingEmail] = useState<boolean>(false);
  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchProductData(searchDebounce);
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

  useEffect(() => {
    fetchCountryList();
    fetchMspList();
  }, []);

  // Fetch MSP list when country changes
  useEffect(() => {
    if (queryParams.countryId) {
      fetchMspList(queryParams.countryId);
      // Reset MSP when country changes
      setQueryParams(prev => ({
        ...prev,
        mspId: '',
      }));
    }
  }, [queryParams.countryId]);

  const fetchCountryList = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.COUNTRY_LIST);
      setCountryList(
        response.data.map((country: ICountryDropdown) => ({
          id: country.id,
          name: country.name,
        })),
      );
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

  const fetchProductData = useCallback(
    async (currentQueryString: string) => {
      setLoading(true);

      try {
        const statusParam = getStatusParam(currentQueryString);
        const response = await apiClient.get(
          API_END_POINTS.INVOICE_LIST + currentQueryString + statusParam,
        );
        setPendingInvoices({
          ...InitGetListParams,
          total: response?.data?.total ?? 0,
          items: response?.data?.items ?? [],
        });
      } catch (error) {
        console.error('Error fetching pending invoices:', error);
      } finally {
        setLoading(false);
      }
    },
    [apiClient],
  );

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const handleExportPendingPayment = async () => {
    setDownloadLoading(true);

    try {
      const statusParam = getStatusParam(queryString);
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
      a.download = `pending_payment_${dayjs().format('DD-MM-YYYY')}.csv`;
      document.body.appendChild(a);
      a.click();

      a.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading pending payment:', error);
    } finally {
      setDownloadLoading(false);
    }
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
    }));
  };

  const handleResetFilter = () => {
    setQueryParams(prev => ({
      ...prev,
      countryId: '',
      mspId: '',
    }));
    setMspList([]);
    fetchMspList();
  };

  const handleComment = (id: string) => {
    setSelectedReportId(id);
    setOpenModal(ModalType.COMMENT);
  };

  const handlePaymentNow = (id: string) => {
    setSelectedReportId(id);
    setOpenModal(ModalType.PAYMENT_NOW);
  };

  const handleViewInvoice = (id: string) => {
    setSelectedReportId(id);
    setOpenModal(ModalType.VIEW);
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
      await apiClient.post(
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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Pending Payment List
          </h1>
          <p className="text-white text-opacity-75">
            View and manage pending payment invoices
          </p>
        </div>
        <Button onClick={handleExportPendingPayment} variant="outline">
          <Download className="mr-2 size-4" />
          {downloadLoading ? 'Exporting...' : 'Export Pending Payment'}
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-white">
            <DollarSign className="size-5" />
            Pending Payment List
          </CardTitle>
          <CardDescription className="text-gray-400">
            Invoices awaiting payment from MSPs
          </CardDescription>
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
                {pendingInvoices?.items?.length > 0 ? (
                  pendingInvoices?.items?.map(payment => (
                    <TableRow key={payment.id}>
                      <TableCell className="font-medium">
                        {payment.id}
                      </TableCell>
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
                            (isFinanceAdmin && (
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
              total={pendingInvoices?.total}
              perPage={pendingInvoices?.pageSize}
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
          reload={() => fetchProductData(searchDebounce)}
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
        message="Are you sure you want to send the invoice in msp admin's email?"
        loading={isSendingEmail}
        loadingText="Sending..."
        onClose={() => setOpenModal(ModalType.NONE)}
        onConfirm={() => handleSendEmail(selectedReportId)}
      />
    </div>
  );
};

export default MSPPendingPayment;
