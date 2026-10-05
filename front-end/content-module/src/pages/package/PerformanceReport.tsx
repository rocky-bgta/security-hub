import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import AspireAdminPerformanceReport from './aspire-admin/PerformanceReport';
import ClientAdminPerformanceReport from './client-admin/PerformanceReport';

const PerformanceReport = () => {
  const { role } = useAuth();

  if (role === ROLE.ASPIRE_ADMIN) {
    return <AspireAdminPerformanceReport />;
  }

  if (role === ROLE.CLIENT_ADMIN) {
    return <ClientAdminPerformanceReport />;
  }
};

export default PerformanceReport;
