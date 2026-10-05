import Loader from 'common/loader/Loader';
import { useAuth } from 'hooks/UseAuth';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';
import MspAdminClientPendingPayment from '../../features/payment/msp/ClientPendingPayment';
import AspireAdminClientPendingPayment from '../../features/payment/aspire-admin/ClientPendingPayment';
import { Badge } from 'common/Badge';
import { Status } from 'models/Global';

export const getPaymentStatusBadge = (status: string) => {
  switch (status) {
    case Status.PAID:
      return <Badge variant="default">Paid</Badge>;
    case Status.PENDING:
      return <Badge variant="secondary">Pending</Badge>;
    case Status.CANCELLED:
      return <Badge variant="destructive">Failed</Badge>;
    case Status.ON_PROGRESS:
      return <Badge variant="outline">On Progress</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const ClientPendingPayment = () => {
  const { loading, role, isAuthenticated } = useAuth();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login.path} replace />;
  }

  if (role === ROLE.ASPIRE_ADMIN || role === ROLE.SUPER_ADMIN || role === ROLE.FINANCE_ADMIN) {
    return <AspireAdminClientPendingPayment />;
  }
  if (role === ROLE.MSP_ADMIN) {
    return <MspAdminClientPendingPayment />;
  }
};

export default ClientPendingPayment;
