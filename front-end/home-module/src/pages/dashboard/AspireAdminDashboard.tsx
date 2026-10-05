import ContentDistribution from 'features/aspire-admin-dashboard/ContentDistribution';
import DashboardSummary from 'features/aspire-admin-dashboard/DashboardSummary';
import DomainManagement from 'features/aspire-admin-dashboard/DomainManagement';
import LicenseDistribution from 'features/aspire-admin-dashboard/LicenseDistribution';
import OnboardedClient from 'features/aspire-admin-dashboard/OnboardedClient';
import OnboardedMSP from 'features/aspire-admin-dashboard/OnboardedMSP';
import PhishingContent from 'features/aspire-admin-dashboard/PhishingContent';
import PhishingPerformance from 'features/aspire-admin-dashboard/PhishingPerformance';
import SalesProgress from 'features/aspire-admin-dashboard/SalesProgress';
import TrainingProgress from 'features/aspire-admin-dashboard/TrainingProgress';
import UserActivities from 'features/aspire-admin-dashboard/UserActivities';
import useStore from 'hooks/UseStore';

const AspireAdminDashboard = () => {
  const { userInfo } = useStore();
  return (
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
      <div className="home-mt-4 home-grid home-grid-cols-1 home-gap-4 md:home-mt-6 md:home-gap-6 lg:home-grid-cols-2">
        <div className="home-col-span-full home-grid home-grid-cols-1 home-gap-4 md:home-gap-6 lg:home-grid-cols-2 xl:home-grid-cols-3">
          <LicenseDistribution />
          <PhishingContent />
          <PhishingPerformance />
        </div>
        <TrainingProgress />
        <UserActivities />
        <div className="home-col-span-full home-flex home-flex-col home-gap-4 md:home-gap-6">
          <ContentDistribution />
          <SalesProgress />
        </div>
        <div className="home-col-span-full home-space-y-4 md:home-space-y-6">
          <OnboardedMSP />
          <OnboardedClient />
          <DomainManagement />
        </div>
      </div>
    </div>
  );
};

export default AspireAdminDashboard;
