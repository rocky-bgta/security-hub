import Loader from 'common/loader/Loader';
import AspireAdminMSPPaymentHistory from 'features/payment/aspire-admin/MSPPaymentHistory';
import MSPAdminMSPPaymentHistory from 'features/payment/msp/MSPPaymentHistory';
import { useAuth } from 'hooks/UseAuth';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

const MSPPaymentHistory = () => {
  const { loading, role, isAuthenticated } = useAuth();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login.path} replace />;
  }

  if (role === ROLE.ASPIRE_ADMIN || role === ROLE.SUPER_ADMIN) {
    return <AspireAdminMSPPaymentHistory />;
  }
  if (role === ROLE.MSP_ADMIN) {
    return <MSPAdminMSPPaymentHistory />;
  }

  return null;
};

export default MSPPaymentHistory;
