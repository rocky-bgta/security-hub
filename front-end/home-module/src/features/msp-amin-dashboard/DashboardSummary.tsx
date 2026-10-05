import {
  IDashboardCardTooltipGrouped,
  IUserDashboardSummary,
} from 'models/Dashboard';
import { useState } from 'react';
import { Fragment } from 'react/jsx-runtime';

import {
  LicensesIcon,
  PackageIcon,
  TopicsIcon,
  TotalClientsIcon,
} from 'assets/icons';
import DashboardCard from 'components/DashboardCard';
import TrackersCardLoader from 'components/skeleton/TrackersCardLoader';

const DashboardSummary = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const [tooltipLoading, setTooltipLoading] = useState<boolean>(false);
  const [cardCourseList, setCardCourseList] =
    useState<IDashboardCardTooltipGrouped>();
  const [dashboardSummary, setDashboardSummary] =
    useState<IUserDashboardSummary>();

  const modules = [
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
      icon: <TopicsIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Total Courses',
      value: dashboardSummary?.totalProduct ?? 0,
      tooltip: cardCourseList?.groupedPackages?.IN_PROGRESS,
    },
    {
      icon: <TotalClientsIcon width={28} height={28} fill="#13cd9c" />,
      label: ' Total Clients',
      value: dashboardSummary?.totalClient ?? 0,
      tooltip: cardCourseList?.groupedPackages?.COMPLETED,
    },

    {
      icon: <LicensesIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Total Licenses',
      value: dashboardSummary?.totalLicense ?? 0,
      tooltip: cardCourseList?.groupedPackages?.NOT_STARTED,
    },
  ];
  return (
    <Fragment>
      {loading ? (
        <TrackersCardLoader />
      ) : (
        <div className="home-grid home-grid-cols-4 home-gap-6">
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
