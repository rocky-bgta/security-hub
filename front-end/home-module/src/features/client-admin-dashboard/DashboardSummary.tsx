import { useEffect, useState } from 'react';
import { Fragment } from 'react/jsx-runtime';

import {
  IClientAdminDashboardSummary,
  IDashboardCardTooltipGrouped,
} from 'models/Dashboard';
import { ClientProductTag } from 'models/Global';

import {
  CertificationsIcon,
  LicensesIcon,
  PackageIcon,
  TopicsIcon,
} from 'assets/icons';
import DashboardCard from 'components/DashboardCard';
import TrackersCardLoader from 'components/skeleton/TrackersCardLoader';
import { API_END_POINTS } from 'routes/APIEndpoints';
import useAPI from 'hooks/UseAPI';
import useStore from 'hooks/UseStore';
import { isSuccessResponse } from 'utils/Helper';

const GRID_COLS_CLASS: Record<number, string> = {
  2: 'home-grid-cols-1 sm:home-grid-cols-2',
  3: 'home-grid-cols-1 sm:home-grid-cols-2 lg:home-grid-cols-3',
  4: 'home-grid-cols-1 sm:home-grid-cols-2 lg:home-grid-cols-4',
};

const DashboardSummary = () => {
  const apiClient = useAPI();
  const { userInfo } = useStore();
  const [loading, setLoading] = useState<boolean>(false);
  const [cardCourseList, setCardCourseList] =
    useState<IDashboardCardTooltipGrouped>();
  const [dashboardSummary, setDashboardSummary] =
    useState<IClientAdminDashboardSummary>();

  const tags = userInfo.clientProductTags ?? [];
  const hasSecurity = tags.includes(ClientProductTag.SECURITY);
  const hasPhishing = tags.includes(ClientProductTag.PHISHING);
  const hasNoProductTags = tags.length === 0;
  // Empty → only Product + License; Phishing-only hides Certificate
  const showTopicCard = !hasNoProductTags;
  const showCertificateCard =
    !hasNoProductTags && !(hasPhishing && !hasSecurity);

  const fetchDashboardSummary = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_DASHBOARD_SUMMARY,
      );
      if (isSuccessResponse(response.statusCode)) {
        setDashboardSummary(response.data);
      }
    } catch (error) {
      console.error('Error fetching dashboard summary:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardSummary();
  }, []);

  const modules = [
    {
      icon: <PackageIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Total Products',
      value: dashboardSummary?.totalProduct ?? 0,
      tooltip: [
        ...(cardCourseList?.groupedPackages?.COMPLETED ?? []),
        ...(cardCourseList?.groupedPackages?.IN_PROGRESS ?? []),
        ...(cardCourseList?.groupedPackages?.NOT_STARTED ?? []),
      ],
    },
    {
      icon: <LicensesIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Total Licenses',
      value: dashboardSummary?.totalLicense ?? 0,
      tooltip: cardCourseList?.groupedPackages?.COMPLETED,
    },
    ...(showTopicCard
      ? [
          {
            icon: <TopicsIcon width={28} height={28} fill="#13cd9c" />,
            label: 'Total Topics',
            value: dashboardSummary?.totalTopic ?? 0,
            tooltip: cardCourseList?.groupedPackages?.IN_PROGRESS,
          },
        ]
      : []),
    ...(showCertificateCard
      ? [
          {
            icon: <CertificationsIcon width={28} height={28} fill="#13cd9c" />,
            label: 'Total Certificates',
            value: dashboardSummary?.totalCertificate ?? 0,
            tooltip: cardCourseList?.groupedPackages?.NOT_STARTED,
          },
        ]
      : []),
  ];

  const gridColsClass =
    GRID_COLS_CLASS[modules.length] ??
    'home-grid-cols-1 sm:home-grid-cols-2';

  return (
    <Fragment>
      {loading ? (
        <TrackersCardLoader count={modules.length} />
      ) : (
        <div className={`home-grid ${gridColsClass} home-gap-4 md:home-gap-5 lg:home-gap-6`}>
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
