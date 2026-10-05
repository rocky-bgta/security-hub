import AlertsNotifications from 'features/client-admin-dashboard/AlertsNotifications';
import AdminCertificateStatistics from 'features/client-admin-dashboard/Certificates';
import LicenseStatistics from 'features/client-admin-dashboard/LicenseStatistics';
import AdminPhishingStatistics from 'features/client-admin-dashboard/Phishing';
import RiskByPhishing from 'features/client-admin-dashboard/RiskByContent';
import UserActivities from 'features/client-admin-dashboard/UserActivities';
import DashboardSummary from 'features/msp-amin-dashboard/DashboardSummary';
import LicenseDistribution from 'features/msp-amin-dashboard/LicenseDistribution';

const MspAdminDashboard = () => {
  return (
    <div>
      <div className="home-mb-6">
        <h1 className="home-text-2xl home-font-semibold home-text-white">
          Hello MSP Provider
          <span role="img" aria-label="waving hand">
            🖐
          </span>
        </h1>
        <p className="home-text-sm home-text-cloudy-white">Welcome back!</p>
      </div>
      <DashboardSummary />
      <div className="home-mt-6 home-grid home-grid-cols-2 home-gap-6">
        <LicenseDistribution />
        <LicenseStatistics />
        <UserActivities />
        <AdminPhishingStatistics />
        <AdminCertificateStatistics />
        <RiskByPhishing />
        <AlertsNotifications />
      </div>
    </div>
  );
};

export default MspAdminDashboard;
