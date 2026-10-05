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
import TableLoader from 'components/skeleton/TableLoader';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import { useDownloader } from 'hooks/UseDownloader';
import { DollarSign, Download, Filter } from 'lucide-react';
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

const CreditList = ({ hostPath = routes }) => {
  const navigate = useNavigate();
  const [pendingInvoices, setPendingInvoices] = useState<IList<IInvoice>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
    search: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
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
        items: response.data.items,
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
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">
            Credit Management
          </h1>
          <p className="text-white text-opacity-75">
            Manage MSP credits and view transaction history
          </p>
        </div>
        <Button onClick={handleExportHistory} variant="outline">
          <Download className="mr-2 size-4" />
          {downloadLoading ? 'Exporting...' : 'Export History'}
        </Button>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2 text-white">
            <DollarSign className="size-5" />
            MSP List
          </CardTitle>
          <div className="mb-6 flex gap-2">
            <div className="flex flex-col items-start gap-2">
              <Input
                className="w-64"
                id="search"
                type="search"
                placeholder="Search by MSP Name"
                value={queryParams.search || ''}
                onChange={e =>
                  setQueryParams(prev => ({
                    ...prev,
                    search: e.target.value,
                  }))
                }
                // min={selectedDate.endDate}
                max={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div className="flex flex-col items-start gap-2">
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="outline" className="w-32 justify-start">
                    <Filter className="mr-2 size-4" />
                    {queryParams.status === Status.ALL
                      ? 'All'
                      : queryParams.status === Status.ACTIVE
                        ? 'Active'
                        : queryParams.status === Status.INACTIVE
                          ? 'Inactive'
                          : queryParams.status === Status.SUSPENDED
                            ? 'Suspended'
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
                        status: Status.ACTIVE,
                      })
                    }
                  >
                    Active
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() =>
                      setQueryParams({
                        ...queryParams,
                        status: Status.INACTIVE,
                      })
                    }
                  >
                    Inactive
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() =>
                      setQueryParams({
                        ...queryParams,
                        status: Status.SUSPENDED,
                      })
                    }
                  >
                    Suspended
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <TableLoader />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>MSP Name</TableHead>
                  <TableHead>Tier</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Available Credit </TableHead>
                  <TableHead>Balance</TableHead>
                  <TableHead>View Details</TableHead>
                  <TableHead>View History</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {pendingInvoices?.items?.length > 0 ? (
                  <>
                    {' '}
                    {pendingInvoices?.items?.map((invoice, index) => (
                      <TableRow key={invoice.id}>
                        <TableCell>{invoice.clientName || 'N/A'}</TableCell>
                        <TableCell>Elite Partner</TableCell>
                        <TableCell>{getStatusBadge('Active')}</TableCell>
                        <TableCell>$25,000.00</TableCell>
                        <TableCell>$1,200.00</TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Button
                              className="bg-transparent text-white text-opacity-75"
                              size="sm"
                              variant="outline"
                              onClick={() => {
                                navigate(
                                  hostPath.creditDetails.path.replace(
                                    ':id',
                                    'b2c2965f-f071-4a39-a2c2-9d3e5b325599',
                                  ),
                                );
                              }}
                            >
                              View Details
                            </Button>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Button
                              className="bg-transparent text-white text-opacity-75"
                              size="sm"
                              variant="outline"
                              onClick={() => {
                                navigate(
                                  hostPath.creditUseHistory.path.replace(
                                    ':id',
                                    'b2c2965f-f071-4a39-a2c2-9d3e5b325599',
                                  ),
                                );
                              }}
                            >
                              View History
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </>
                ) : (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center">
                      <div className="text-center text-muted-foreground">
                        No pending payments found.
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
    </div>
  );
};

export default CreditList;
