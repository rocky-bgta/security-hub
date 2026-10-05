import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import ChartLegend from 'components/ChartLegend';
import { isSuccessResponse } from 'utils/Helper';
import { useEffect } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import useAPI from 'hooks/UseAPI';
import { useState } from 'react';

interface IUserActivity {
  totalActive: number;
  totalInactive: number;
  totalSuspended: number;
}

const CustomTooltip = ({
  active,
  payload,
}: {
  active?: boolean;
  payload?: Array<{ name: string; value: number; payload: { color: string } }>;
}) => {
  if (active && payload && payload.length) {
    return (
      <div className="home-flex home-items-center home-rounded home-bg-[#2B414F] home-px-4 home-py-2 home-shadow-lg">
        <div
          className="home-mr-2 home-size-3 home-rounded-full"
          style={{ backgroundColor: payload[0].payload.color }}
        />
        <p className="home-text-center home-font-semibold home-text-white">
          <span className="home-mr-4 home-text-center home-text-sm home-text-white home-text-opacity-50">
            {payload[0].name}
          </span>{' '}
          {`${payload[0].value}`}
        </p>
      </div>
    );
  }
  return null;
};

const UserActivities = () => {
  const apiClient = useAPI();
  const [userActivities, setUserActivities] = useState<IUserActivity>({
    totalActive: 0,
    totalInactive: 0,
    totalSuspended: 0,
  });

  useEffect(() => {
    const fetchUserActivities = async () => {
      try {
        const response = await apiClient.get(API_END_POINTS.USER_ACTIVITIES);
        if (isSuccessResponse(response.statusCode)) {
          setUserActivities(response.data);
        }
      } catch (error) {
        console.error('Error fetching user activities:', error);
      }
    };

    setTimeout(() => {
      fetchUserActivities();
    }, 0);
  }, [apiClient]);

  const userActivitiesData = [
    { name: 'Active', value: userActivities.totalActive, color: '#37BE99' },
    { name: 'Inactive', value: userActivities.totalInactive, color: '#F7C948' },
    {
      name: 'Suspended',
      value: userActivities.totalSuspended,
      color: '#F65E5B',
    },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle>
          <p className="home-text-xl home-font-semibold home-text-white">
            User Activities
          </p>
        </CardTitle>
      </CardHeader>
      <CardContent className="home-flex home-flex-col home-items-center home-gap-4 md:home-gap-6 lg:home-flex-row lg:home-justify-evenly lg:home-pb-0">
        <div className="home-h-[280px] home-w-full md:home-h-[320px] lg:home-h-[360px] lg:home-w-3/5">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={userActivitiesData}
                cx="50%"
                cy="50%"
                innerRadius={68}
                stroke="none"
                dataKey="value"
              >
                {userActivitiesData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip
                content={<CustomTooltip active={false} payload={[]} />}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <ChartLegend data={userActivitiesData} />
      </CardContent>
    </Card>
  );
};

export default UserActivities;
