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
import { Switch } from 'common/Switch';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import IconBackButton from 'components/IconBackButton';
import { CreditTransferModal } from 'features/credit/CreditTransferModal';
import { DepositModal } from 'features/credit/DepositModal';
import { EditExpireDateModal } from 'features/credit/EditExpireDateModal';
import { WithdrawModal } from 'features/credit/WithdrawModal';
import { useAPI } from 'hooks/UseAPI';
import { useAuth } from 'hooks/UseAuth';
import useDebounce from 'hooks/UseDebounce';
import {
  ArrowRightLeft,
  Calendar,
  CreditCard,
  DollarSign,
  Edit,
  Eye,
  Loader2,
  Plus,
  TrendingDown,
  TrendingUp,
  User,
} from 'lucide-react';
import { IGetListParams, IList, Status } from 'models/Global';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';
import { ROLE } from 'utils/Role';

const CreditDetails = ({ hostPath = routes }) => {
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
    status: Status.ALL,
    search: '',
  });
  const searchDebounce = useDebounce(queryString, 1000);
  const [loading, setLoading] = useState<boolean>(true);
  const [creditDetails, setCreditDetails] = useState<any>(null);
  const [detailsLoading, setDetailsLoading] = useState<boolean>(false);
  const [statusLoading, setStatusLoading] = useState<boolean>(false);
  const [openDepositModal, setOpenDepositModal] = useState<boolean>(false);
  const [openWithdrawModal, setOpenWithdrawModal] = useState<boolean>(false);
  const [showTransferModal, setShowTransferModal] = useState<boolean>(false);
  const [updateModal, setUpdateModal] = useState<boolean>(false);

  const { role } = useAuth();

  const apiClient = useAPI();

  const isAdmin = role === ROLE.ASPIRE_ADMIN || role === ROLE.SUPER_ADMIN;

  useEffect(() => {
    const resp = objectToQueryString(queryParams);
    setQueryString(resp);
  }, [queryParams]);

  useEffect(() => {
    if (searchDebounce) {
      fetchCreditTransactionList();
    }
  }, [searchDebounce]);

  const fetchCreditTransactionList = async () => {
    setLoading(true);

    try {
      const response = await apiClient.get(
        API_END_POINTS.CREDIT_TRANSACTION_LIST + id + '?' + queryString,
      );
      setData({
        ...InitGetListParams,
        total: response.data.totalCount,
        items: response.data.transactions || [],
      });
    } catch (error) {
      console.error('Error fetching credit transaction list:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCreditDetails();
  }, []);

  const fetchCreditDetails = async () => {
    setDetailsLoading(true);

    try {
      const response = await apiClient.get(API_END_POINTS.CREDIT_DETAILS + id);
      setCreditDetails(response.data?.credits[0] || null);
    } catch (error) {
      console.error('Error fetching credit details', error);
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

  const handleCreditStatusToggle = async () => {
    if (!creditDetails?.id) return;
    setStatusLoading(true);

    try {
      await apiClient.put(
        API_END_POINTS.CREDIT_STATUS_UPDATE + creditDetails?.id,
        {
          data: {
            active: !creditDetails?.active,
          },
        },
      );
      toast.success(
        `Credit status ${creditDetails?.active ? 'Inactivated' : 'Activated'} successfully!`,
      );
      fetchCreditDetails();
    } catch (error) {
      console.error('Error toggling credit status:', error);
      toast.error('Failed to update credit status');
    } finally {
      setStatusLoading(false);
    }
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

        {isAdmin ? (
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              onClick={() => setOpenDepositModal(true)}
              disabled={!creditDetails?.active}
              className="bg-primary px-4 py-2 font-semibold text-primary-foreground shadow-lg hover:bg-primary/90"
            >
              <Plus className="mr-1 size-4" />
              Deposit
            </Button>
            <Button
              size="sm"
              onClick={() => setOpenWithdrawModal(true)}
              disabled={
                !creditDetails?.active || creditDetails?.availableCredits <= 0
              }
              variant={'outline'}
            >
              <Plus className="mr-1 size-4" />
              Withdraw
            </Button>
          </div>
        ) : (
          <Button
            onClick={() => setShowTransferModal(true)}
            disabled={!creditDetails?.active}
            variant={'outline'}
          >
            <ArrowRightLeft className="mr-2 size-4" />
            Transfer Credit
          </Button>
        )}
      </div>

      <div className="mb-6 grid grid-cols-1 gap-6 md:grid-cols-3">
        <Card className="p-6">
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <CreditCard className="size-5 text-primary" />
              <h3 className="font-semibold text-card-foreground">
                Available Credit
              </h3>
            </div>
          </div>
          <div className="space-y-2">
            <div className="text-3xl font-bold text-primary">
              {formatCurrency(creditDetails?.availableCredits)}
            </div>
            <div className="text-sm text-muted-foreground">
              Total: {formatCurrency(creditDetails?.availableCredits)}
            </div>
          </div>
        </Card>

        <Card className="p-6">
          <div className="mb-4 flex items-center gap-2">
            <User className="size-5 text-secondary-foreground" />
            <h3 className="font-semibold text-card-foreground">
              Account Balance
            </h3>
          </div>
          <div className="space-y-2">
            <div
              className={`text-3xl font-bold ${creditDetails?.totalCredits >= 0 ? 'text-primary' : 'text-destructive'}`}
            >
              {formatCurrency(creditDetails?.totalCredits)}
            </div>
            <div className="text-sm text-muted-foreground">
              Total: {formatCurrency(creditDetails?.totalCredits)}
            </div>
          </div>
        </Card>

        <Card className="relative p-6">
          {isAdmin && (
            <Button
              size="sm"
              variant="outline"
              className="absolute right-4 top-4"
              onClick={() => setUpdateModal(true)}
              disabled={!creditDetails?.active}
            >
              <Edit className="mr-1 size-4" />
              Edit
            </Button>
          )}

          <div className="mb-4 flex items-center gap-2">
            <Calendar className="size-5 text-muted-foreground" />
            <h3 className="font-semibold text-card-foreground">
              Credit Expiry
            </h3>
          </div>
          <div className="space-y-4">
            <div className="text-lg font-semibold text-yellow-500">
              {creditDetails?.expirationDate
                ? formatDate(creditDetails?.expirationDate)
                : 'N/A'}
            </div>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Badge
                  variant={creditDetails?.active ? 'default' : 'destructive'}
                >
                  {creditDetails?.active ? 'Active' : 'Inactive'}
                </Badge>
              </div>
              {isAdmin && (
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground">Status:</span>
                  <Switch
                    checked={creditDetails?.active || false}
                    onCheckedChange={handleCreditStatusToggle}
                    disabled={creditDetails?.status || statusLoading}
                  />
                  {statusLoading && (
                    <Loader2 className="size-4 animate-spin text-primary" />
                  )}
                </div>
              )}
            </div>
          </div>
        </Card>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="flex items-center gap-2 text-white">
            <DollarSign className="size-5" />
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

      {openWithdrawModal && (
        <WithdrawModal
          id={id || ''}
          amount={creditDetails?.availableCredits || 0}
          isOpen={openWithdrawModal}
          onClose={() => setOpenWithdrawModal(false)}
          customerName={creditDetails.name}
          onSubmit={() => {
            fetchCreditDetails();
            fetchCreditTransactionList();
          }}
        />
      )}

      {openDepositModal && (
        <DepositModal
          id={id || ''}
          amount={creditDetails?.availableCredits || 0}
          isOpen={openDepositModal}
          onClose={() => setOpenDepositModal(false)}
          customerName={creditDetails.name}
          onSubmit={() => {
            fetchCreditDetails();
            fetchCreditTransactionList();
          }}
        />
      )}

      {updateModal && (
        <EditExpireDateModal
          id={creditDetails?.id || ''}
          isOpen={updateModal}
          onClose={() => setUpdateModal(false)}
          customerName={creditDetails.name}
          onSubmit={fetchCreditDetails}
        />
      )}

      {showTransferModal && (
        <CreditTransferModal
          id={id}
          amount={creditDetails?.availableCredits || 0}
          isOpen={showTransferModal}
          onClose={() => setShowTransferModal(false)}
          onSubmit={() => {
            fetchCreditDetails();
            fetchCreditTransactionList();
          }}
          fromClientId={creditDetails.id}
        />
      )}
    </div>
  );
};

export default CreditDetails;
