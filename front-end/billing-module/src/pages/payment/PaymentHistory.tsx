import Loader from 'common/loader/Loader';
import AspireAdminClientPaymentHistory from 'features/payment/aspire-admin/ClientPaymentHistory';
import ClientPaymentHistory from 'features/payment/client/PaymentHistory';
import MSPAdminMSPPaymentHistory from 'features/payment/msp/MSPPaymentHistory';
import { useAuth } from 'hooks/UseAuth';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

const PaymentHistory = () => {
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
    return <MSPAdminMSPPaymentHistory />;
  }
  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientPaymentHistory />;
  }

  return null;
};

export default PaymentHistory;
