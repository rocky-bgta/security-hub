import CertificateTemplateList from 'features/certificate/aspire-admin/CertificateTemplateList';
import CertificateTemplate from 'features/certificate/client-admin/CertificateTemplate';
import MSPAdminCertificateTemplate from 'features/certificate/msp-admin/CertificateTemplate';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';

const CertificateTemplates = () => {
  const { role } = useAuth();
  if (role === ROLE.ASPIRE_ADMIN) return <CertificateTemplateList />;
  if (role === ROLE.CLIENT_ADMIN) return <CertificateTemplate />;
  if (role === ROLE.MSP_ADMIN) return <MSPAdminCertificateTemplate />;
  return null;
};

export default CertificateTemplates;
