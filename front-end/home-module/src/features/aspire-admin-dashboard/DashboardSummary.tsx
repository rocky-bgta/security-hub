import {
  IDashboardCardTooltipGrouped,
  IUserDashboardSummary,
} from 'models/Dashboard';
import { useEffect, useState } from 'react';
import { Fragment } from 'react/jsx-runtime';

import {
  LicensesIcon,
  PackageIcon,
  TopicsIcon,
  TotalClientsIcon,
  TotalMSPIcon,
} from 'assets/icons';
import DashboardCard from 'components/DashboardCard';
import TrackersCardLoader from 'components/skeleton/TrackersCardLoader';
import useAPI from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

const DashboardSummary = () => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(false);
  const [tooltipLoading, setTooltipLoading] = useState<boolean>(false);
  const [cardCourseList, setCardCourseList] =
    useState<IDashboardCardTooltipGrouped>();
  const [dashboardSummary, setDashboardSummary] =
    useState<IUserDashboardSummary>();

  const modules = [
    {
      icon: <TopicsIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Total Products',
      value: dashboardSummary?.totalProduct ?? 0,
      tooltip: cardCourseList?.groupedPackages?.IN_PROGRESS,
    },
    {
      icon: <LicensesIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Total Licenses',
      value: dashboardSummary?.totalLicense ?? 0,
      tooltip: cardCourseList?.groupedPackages?.COMPLETED,
    },
    {
      icon: <PackageIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Total Packages',
      value: dashboardSummary?.totalPackage ?? 0,
      tooltip: [
        ...(cardCourseList?.groupedPackages?.COMPLETED ?? []),
        ...(cardCourseList?.groupedPackages?.IN_PROGRESS ?? []),
        ...(cardCourseList?.groupedPackages?.NOT_STARTED ?? []),
      ],
    },
    {
      icon: <TotalClientsIcon width={28} height={28} fill="#13cd9c" />,
      label: ' Total Clients',
      value: dashboardSummary?.totalClient ?? 0,
      tooltip: cardCourseList?.groupedPackages?.NOT_STARTED,
    },
    {
      icon: <TotalMSPIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Total MSP',
      value: dashboardSummary?.totalMsp ?? 0,
      tooltip: cardCourseList?.groupedPackages?.NOT_STARTED,
    },
  ];

  const fetchDashboardSummary = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.DASHBOARD_SUMMARY);
      if (isSuccessResponse(response.statusCode)) {
        setDashboardSummary(response.data);
      }
    } catch (error) {
      console.error('Error fetching dashboard summary:', error);
    }
  };
  useEffect(() => {
    fetchDashboardSummary();
  }, []);

  return (
    <Fragment>
      {loading ? (
        <TrackersCardLoader />
      ) : (
        <div className="home-grid home-grid-cols-1 home-gap-4 md:home-grid-cols-2 md:home-gap-5 lg:home-grid-cols-5 lg:home-gap-6">
          {modules.map((module, idx) => (
            <DashboardCard
              key={idx}
              icon={module.icon}
              label={module.label}
              value={module.value}
              tooltip={module.tooltip}
            />
          ))}
        </div>
      )}
    </Fragment>
  );
};

export default DashboardSummary;
