import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from 'common/Dialog';
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
import IconBackButton from 'components/IconBackButton';
import { useAPI } from 'hooks/UseAPI';
import useDebounce from 'hooks/UseDebounce';
import {
  DollarSign,
  Eye,
  TrendingDown,
  TrendingUp,
} from 'lucide-react';
import { IGetListParams, IList } from 'models/Global';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

const CreditUseHistory = ({ hostPath = routes }) => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [data, setData] = useState<IList<any>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryString, setQueryString] = useState<string>('');
  const [queryParams, setQueryParams] = useState<IGetListParams>({
    ...InitGetListParams,
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [creditDetails, setCreditDetails] = useState<any>(null);
  const [detailsLoading, setDetailsLoading] = useState<boolean>(true);

  const apiClient = useAPI();

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchCreditUseHistory();
    }
  }, [searchDebounce]);

  const fetchCreditUseHistory = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.CREDIT_USE_HISTORY + `${id}?` + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.totalCount,
        items: response.data.transactions || [],
      });
      setCreditDetails(response.data?.credits[0] || null);
    } catch (error) {
      console.error('Error fetching credit use history details', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCreditUseHistoryDetails();
  }, []);

  const fetchCreditUseHistoryDetails = async () => {
    setDetailsLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.CREDIT_USER_SUMMARY + id + `?type=USAGE`,
      );
      setCreditDetails(response.data || null);
    } catch (error) {
      console.error('Error fetching credit use history details', error);
    } finally {
      setDetailsLoading(false);
    }
  };

  const onPageChangeHandler = (page: number) => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: page - 1,
    }));
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const getTransactionIcon = (type: string) => {
    switch (type) {
      case 'TRANSFER_IN':
        return <TrendingUp className="size-4 text-[#13CD9C]" />;
      case 'TRANSFER_OUT':
        return <TrendingDown className="size-4 text-destructive" />;
      case 'MANUAL_ADJUSTMENT':
        return <DollarSign className="size-4 text-yellow-500" />;
      default:
        return <DollarSign className="size-4 text-muted-foreground" />;
    }
  };

  return (
    <div className="space-y-6 p-6">
      <IconBackButton
        onClick={() => navigate(hostPath.manageCredits.path)}
        label="Back to MSP List"
      />
      <div className="mb-6 flex items-center justify-between">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="mb-2 text-3xl font-bold text-foreground">
              {creditDetails?.name || 'Aspire Digital Ltd.'}
            </h1>
            <div className="flex items-center gap-4">
              <Badge className="bg-gradient-to-r from-pink-500 to-purple-600 text-white">
                {creditDetails?.tier || 'Gold Partner'}
              </Badge>
              <Badge
                variant={creditDetails?.active ? 'default' : 'destructive'}
              >
                {creditDetails?.active ? 'Active' : 'Inactive'}
              </Badge>
            </div>
          </div>
        </div>
      </div>

      <div className="mb-6 grid grid-cols-1 gap-6 md:grid-cols-3">
        <Card className="p-6">
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <TrendingUp className="size-5 text-primary" />
              <h3 className="font-semibold text-card-foreground">
                Total Used Credit
              </h3>
            </div>
          </div>
          <div className="space-y-2">
            <div className="text-3xl font-bold text-primary">
              {formatCurrency(creditDetails?.totalUsedCredit)}
            </div>
            <div className="text-sm text-muted-foreground">
              Total: {formatCurrency(creditDetails?.totalUsedCredit)}
            </div>
          </div>
        </Card>

        <Card className="p-6">
          <div className="mb-4 flex items-center gap-2">
            <TrendingUp className="size-5 text-secondary-foreground" />
            <h3 className="font-semibold text-card-foreground">
              Total Paid Credit
            </h3>
          </div>
          <div className="space-y-2">
            <div className={`text-3xl font-bold`}>
              {formatCurrency(creditDetails?.totalPaidCredit)}
            </div>
            <div className="text-sm text-muted-foreground">
              Total: {formatCurrency(creditDetails?.totalPaidCredit)}
            </div>
          </div>
        </Card>

        <Card className="relative p-6">
          <div className="mb-4 flex items-center gap-2">
            <TrendingDown className="size-5 text-muted-foreground" />
            <h3 className="font-semibold text-card-foreground">
              Total Due Credit
            </h3>
          </div>
          <div className="space-y-4">
            <div className="text-lg font-semibold text-yellow-500">
              {formatCurrency(creditDetails?.totalDueCredit)}
            </div>
            <div className="text-sm text-muted-foreground">
              Total: {formatCurrency(creditDetails?.totalDueCredit)}
            </div>
          </div>
        </Card>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2 text-white">
            <TrendingDown className="size-5" />
            Transaction History
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Type</TableHead>
                <TableHead>Reference</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Description</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Initiated By</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Action</TableHead>
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
                  {data.items.length > 0 ? (
                    <>
                      {' '}
                      {data.items.map(item => (
                        <TableRow key={item.id}>
                          <TableCell>{getTransactionIcon(item.type)}</TableCell>
                          <TableCell>{item.referenceId || 'N/A'}</TableCell>
                          <TableCell>{formatCurrency(item.amount)}</TableCell>
                          <TableCell>
                            {item.description
                              ? item.description.length > 20
                                ? item.description.substring(0, 20) + '...'
                                : item.description
                              : 'N/A'}
                          </TableCell>
                          <TableCell>{formatDate(item.createdAt)}</TableCell>
                          <TableCell>{item.initiatedBy || 'N/A'}</TableCell>
                          <TableCell>
                            {item?.referenceType?.replace(/_/g, ' ')}
                          </TableCell>
                          <TableCell>
                            <Dialog>
                              <DialogTrigger asChild>
                                <Button
                                  className="bg-transparent text-white text-opacity-75"
                                  size="sm"
                                  variant="outline"
                                  disabled={!item.description}
                                >
                                  <Eye className="mr-1 size-3" />
                                  View Details
                                </Button>
                              </DialogTrigger>
                              <DialogContent className="max-h-[80vh] max-w-2xl overflow-y-auto">
                                <DialogHeader>
                                  <DialogTitle>
                                    Transaction Description
                                  </DialogTitle>
                                </DialogHeader>
                                <div className="whitespace-pre-line leading-relaxed">
                                  {item.description}
                                </div>
                              </DialogContent>
                            </Dialog>
                          </TableCell>
                        </TableRow>
                      ))}
                    </>
                  ) : (
                    <div className="py-8 text-center text-muted-foreground">
                      No pending payments found.
                    </div>
                  )}
                </>
              )}
            </TableBody>
          </Table>
          <div className="pt-6">
            <Pagination
              total={data.total}
              perPage={data.pageSize}
              onPageChange={onPageChangeHandler}
            />
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default CreditUseHistory;
