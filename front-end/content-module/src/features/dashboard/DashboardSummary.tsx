import { IUserDashboardSummary } from 'models/Course';
import { IResponse } from 'models/Global';
import { useEffect, useState } from 'react';
import { Fragment } from 'react/jsx-runtime';
import { API_END_POINTS } from 'routes/APIEndpoints';

import {
  CompletedIcon,
  CourseIcon,
  ExamIcon,
  InProgressIcon,
  PendingIcon,
} from 'assets/icons';
import DashboardCard from 'components/DashboardCard';
import TrackersCardLoader from 'components/skeleton/TrackersCard';
import { useAPI } from 'hooks/UseAPI';

const DashboardSummary = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const [dashboardSummary, setDashboardSummary] =
    useState<IUserDashboardSummary>();

  const apiClient = useAPI();

  useEffect(() => {
    fetchDashboardSummary();
  }, []);

  const fetchDashboardSummary = async () => {
    setLoading(true);

    try {
      const response: IResponse<IUserDashboardSummary> = await apiClient.get(
        API_END_POINTS.USER_DASHBOARD_SUMMARY,
      );
      setDashboardSummary(response.data);
    } catch (error) {
      console.error('Error fetching dashboard summary:', error);
    } finally {
      setLoading(false);
    }
  };

  const modules = [
    {
      icon: <CourseIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Assigned Courses',
      value: dashboardSummary?.total ?? 0,
      // tooltip: [
      //   ...(cardCourseList?.groupedPackages?.COMPLETED ?? []),
      //   ...(cardCourseList?.groupedPackages?.IN_PROGRESS ?? []),
      //   ...(cardCourseList?.groupedPackages?.NOT_STARTED ?? []),
      // ],
    },
    {
      icon: <CompletedIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Completed Courses',
      value: dashboardSummary?.completed ?? 0,
      // tooltip: cardCourseList?.groupedPackages?.COMPLETED,
    },
    {
      icon: <InProgressIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Courses In Progress',
      value: dashboardSummary?.inProgress ?? 0,
      // tooltip: cardCourseList?.groupedPackages?.IN_PROGRESS,
    },
    {
      icon: <PendingIcon width={28} height={28} fill="#13cd9c" />,
      label: 'Not started',
      value: dashboardSummary?.notStarted ?? 0,
      // tooltip: cardCourseList?.groupedPackages?.NOT_STARTED,
    },
    {
      icon: <ExamIcon width={28} height={28} fill="none" stroke="#13cd9c" />,
      label: 'Assessment',
      value: dashboardSummary?.exam ?? 0,
      // tooltip: cardCourseList?.groupedPackages?.NOT_STARTED,
    },
  ];
  return (
    <Fragment>
      {loading ? (
        <TrackersCardLoader />
      ) : (
        <div className="content-grid content-grid-cols-1 content-gap-3 sm:content-grid-cols-2 sm:content-gap-4 lg:content-grid-cols-3 lg:content-gap-6 xl:content-grid-cols-5">
          {modules.map((module, idx) => (
            <DashboardCard
              key={idx}
              icon={module.icon}
              label={module.label}
              value={module.value}
              // tooltip={module.tooltip}
            />
          ))}
        </div>
      )}
    </Fragment>
  );
};

export default DashboardSummary;
