import { Badge } from 'common/Badge';
import Loader from 'common/loader/Loader';
import AspireAdminClientPaymentHistory from 'features/payment/aspire-admin/ClientPaymentHistory';
import MspAdminClientPaymentHistory from 'features/payment/msp/ClientPaymentHistory';
import { useAuth } from 'hooks/UseAuth';
import { Status } from 'models/Global';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

export const getStatusBadge = (status: string) => {
  switch (status) {
    case Status.PAID:
      return <Badge variant="default">Paid</Badge>;
    case Status.PENDING:
      return <Badge variant="secondary">Pending</Badge>;
    case Status.FAILED:
      return <Badge variant="destructive">Failed</Badge>;
    case Status.SUCCESS:
      return <Badge variant="default">Success</Badge>;
    default:
      return <Badge variant="outline">{status}</Badge>;
  }
};

const ClientPaymentHistory = () => {
  const { loading, role, isAuthenticated } = useAuth();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login.path} replace />;
  }

  if (role === ROLE.ASPIRE_ADMIN || role === ROLE.SUPER_ADMIN || role === ROLE.FINANCE_ADMIN) {
    return <AspireAdminClientPaymentHistory />;
  }
  if (role === ROLE.MSP_ADMIN) {
    return <MspAdminClientPaymentHistory />;
  }
};

export default ClientPaymentHistory;
