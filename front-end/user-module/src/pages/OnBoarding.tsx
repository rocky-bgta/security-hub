import Loader from 'common/loader/Loader';
import { useAuth } from 'hooks/UseAuth';
import { Navigate } from 'react-router-dom';
import { routes } from 'routes/Routes';
import { ROLE } from 'utils/Role';
import ClientOnBoarding from './client-admin/OnBoarding';
import MspClientOnBoarding from './msp-admin/client/OnBoarding';

const OnBoarding = () => {
    const { loading, role, isAuthenticated } = useAuth();

    if (loading) {
        return <Loader />;
    }

    if (!isAuthenticated) {
        return <Navigate to={routes.login.path} replace />;
    }

    if (role === ROLE.SUPER_ADMIN || role === ROLE.ASPIRE_ADMIN) {
        return <ClientOnBoarding />;
    }
    if (role === ROLE.MSP_ADMIN) {
        return <MspClientOnBoarding />;
    }

    return <Loader />;
};

export default OnBoarding;
