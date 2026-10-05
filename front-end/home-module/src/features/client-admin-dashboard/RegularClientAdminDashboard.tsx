import { Fragment } from 'react/jsx-runtime';

import AlertsNotifications from 'features/client-admin-dashboard/AlertsNotifications';
import AdminCertificateStatistics from 'features/client-admin-dashboard/Certificates';
import DashboardSummary from 'features/client-admin-dashboard/DashboardSummary';
import LicenseStatistics from 'features/client-admin-dashboard/LicenseStatistics';
import AdminPhishingStatistics from 'features/client-admin-dashboard/Phishing';
import RiskByContent from 'features/client-admin-dashboard/RiskByContent';
import UserActivities from 'features/client-admin-dashboard/UserActivities';
import useStore from 'hooks/UseStore';
import { ClientProductTag } from 'models/Global';

const RegularClientAdminDashboard = () => {
  const { userInfo } = useStore();

  const tags = userInfo.clientProductTags ?? [];
  const hasSecurity = tags.includes(ClientProductTag.SECURITY);
  const hasPhishing = tags.includes(ClientProductTag.PHISHING);
  const hasNoProductTags = tags.length === 0;
  // Empty → no product-specific cards; otherwise hide only for exclusive tags
  const showPhishingCard = !hasNoProductTags && !(hasSecurity && !hasPhishing);
  const showCertificateCards =
    !hasNoProductTags && !(hasPhishing && !hasSecurity);

  // Pair Alerts with RiskByContent when the preceding card count is odd
  // so the last row does not leave an empty column.
  const cardsBeforeAlerts =
    2 +
    (showPhishingCard ? 1 : 0) +
    (showCertificateCards ? 1 : 0) +
    1;
  const alertsFullWidth = cardsBeforeAlerts % 2 === 0;

  return (
    <Fragment>
      <div className="home-space-y-4 md:home-space-y-6">
        <div className="home-mb-4 md:home-mb-6">
          <h1 className="home-text-xl home-font-semibold home-text-white sm:home-text-2xl">
            Hello {userInfo?.fullName}{' '}
            <span role="img" aria-label="waving hand">
              🖐
            </span>
          </h1>
          <p className="home-text-sm home-text-cloudy-white">Welcome back!</p>
        </div>
        <DashboardSummary />
        <div className="home-mt-4 home-grid home-grid-cols-1 home-gap-4 md:home-mt-6 md:home-gap-6 lg:home-grid-cols-2 [&>*]:home-min-w-0">
          <LicenseStatistics />
          <UserActivities />

          {showPhishingCard && <AdminPhishingStatistics />}

          {showCertificateCards && <AdminCertificateStatistics />}
          <RiskByContent />

          <div className={alertsFullWidth ? 'lg:home-col-span-2' : ''}>
            <AlertsNotifications />
          </div>
        </div>
      </div>
    </Fragment>
  );
};

export default RegularClientAdminDashboard;
