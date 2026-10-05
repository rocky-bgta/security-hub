import Loader from 'common/loader/Loader';
import MSPPaymentReportFeature from 'features/payment/msp/MSPPaymentReport';
import { useAuth } from 'hooks/UseAuth';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';

const MSPPaymentReport = () => {
  const { loading, role, isAuthenticated } = useAuth();

  if (loading) {
    return <Loader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={routes.login.path} replace />;
  }

  if (
    role === ROLE.ASPIRE_ADMIN ||
    role === ROLE.SUPER_ADMIN ||
    role === ROLE.MSP_ADMIN
  ) {
    return <MSPPaymentReportFeature />;
  }

  return null;
};

export default MSPPaymentReport;
