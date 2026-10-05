import Loader from 'common/loader/Loader';
import AspirePendingPayment from 'features/payment/aspire-admin/PendingPayment';
import ClientPendingPayment from 'features/payment/client/PendingPayment';
import MSPPendingPayment from 'features/payment/msp/MSPPendingPayment';
import { useAuth } from 'hooks/UseAuth';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

const PendingPayment = () => {
  const { loading, role, isAuthenticated } = useAuth();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login.path} replace />;
  }

  if (role === ROLE.ASPIRE_ADMIN || role === ROLE.SUPER_ADMIN) {
    return <AspirePendingPayment />;
  }
  if (role === ROLE.MSP_ADMIN) {
    return <MSPPendingPayment />;
  }
  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientPendingPayment />;
  }
};

export default PendingPayment;
