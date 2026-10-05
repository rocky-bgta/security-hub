import Loader from 'common/loader/Loader';
import AspirePaymentReport from 'features/payment/aspire-admin/PaymentReport';
import ClientPaymentReport from 'features/payment/client/PaymentReport';
import MSPPaymentReport from 'features/payment/msp/MSPPaymentReport';
import { useAuth } from 'hooks/UseAuth';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

const PaymentReport = () => {
  const { loading, role, isAuthenticated } = useAuth();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login.path} replace />;
  }

  if (role === ROLE.ASPIRE_ADMIN || role === ROLE.SUPER_ADMIN) {
    return <AspirePaymentReport />;
  }
  if (role === ROLE.MSP_ADMIN) {
    return <MSPPaymentReport />;
  }
  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientPaymentReport />;
  }
};

export default PaymentReport;
