import AspireAdminCertificateAnalytics from 'features/certificate/aspire-admin/CertificateAnalytics';
import MSPAdminCertificateAnalytics from 'features/certificate/msp-admin/CertificateAnalytics';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';

const CertificateAnalytics = () => {
  const { role } = useAuth();
  if (role === ROLE.ASPIRE_ADMIN) return <AspireAdminCertificateAnalytics />;
  if (role === ROLE.MSP_ADMIN) return <MSPAdminCertificateAnalytics />;
  return null;
};

export default CertificateAnalytics;
