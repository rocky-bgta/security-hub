import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { cn, isSuccessResponse, objectToQueryString } from 'utils/Helper';
import useAPI from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Route';
import Loader from 'common/loader/Loader';

const ranges = [
  { key: '7Day', label: '7 days', days: 7 },
  { key: '1Month', label: '30 days', days: 30 },
  { key: '12Month', label: '12 months', days: 365 },
];

// Format Y-axis ticks (e.g. 120 -> "120")
const tickFormatter = (value: number) => `${Math.floor(value)}`;

// Tooltip formatter: show login count
const formatTooltip = (value: number) => {
  return `${value} logins`;
};

const CustomTooltip = ({
  active,
  payload,
}: {
  active: boolean;
  payload: any;
}) => {
  if (active && payload && payload.length) {
    return (
      <div className="home-rounded home-bg-[#2B414F] home-p-2 home-shadow-lg">
        <div>
          <p className="home-text-sm home-text-white home-text-opacity-50">
            Login Count
          </p>
          <p className="home-font-semibold home-text-white">
            {formatTooltip(payload[0].value)}
          </p>
        </div>
      </div>
    );
  }
  return null;
};

const UserActivities = () => {
  const apiClient = useAPI();
  const [range, setRange] = useState<string>('7Day');
  const [chartData, setChartData] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const formatChartData = (apiData: any[], rangeKey: string) => {
    if (!apiData || apiData.length === 0) return [];

    if (rangeKey === '7Day') {
      // Show daily data
      return apiData.map(item => ({
        name: new Date(item.date).toLocaleDateString('en-US', {
          weekday: 'short',
        }),
        value: item.loginCount,
      }));
    } else if (rangeKey === '1Month') {
      const suffixes = ['th', 'st', 'nd', 'rd'];
      // Sort data by date first
      const sortedData = [...apiData].sort(
        (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime(),
      );

      // Then group by order
      const weeks: { [key: string]: number } = {};
      sortedData.forEach((item, index) => {
        const weekNumber = index + 1; // Start from Week 1
        const suffix =
          weekNumber % 100 > 10 && weekNumber % 100 < 20
            ? 'th'
            : suffixes[weekNumber % 10] || 'th';
        const weekKey = `${weekNumber}${suffix} Week`;
        weeks[weekKey] = item.loginCount;
      });

      // Convert to array for chart
      return Object.entries(weeks).map(([name, value]) => ({ name, value }));
    } else {
      // Group by months
      const months: { [key: string]: number } = {};
      apiData.forEach(item => {
        const date = new Date(item.date);
        const monthName = date.toLocaleDateString('en-US', { month: 'short' });
        months[monthName] = (months[monthName] || 0) + item.loginCount;
      });

      // Sort months from Jan → Dec
      const monthOrder = [
        'Jan',
        'Feb',
        'Mar',
        'Apr',
        'May',
        'Jun',
        'Jul',
        'Aug',
        'Sep',
        'Oct',
        'Nov',
        'Dec',
      ];

      return Object.entries(months)
        .sort(([a], [b]) => monthOrder.indexOf(a) - monthOrder.indexOf(b))
        .map(([name, value]) => ({ name, value }));
    }
  };

  const fetchUserActivities = async () => {
    try {
      setIsLoading(true);
      const queryString = objectToQueryString({
        filterType: range,
      });

      const response = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_USER_ACTIVITIES + queryString,
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      setChartData(formatChartData(response.data, range));
    } catch (error) {
      console.error('Error fetching user activities:', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUserActivities();
  }, [range]);

  const handleRangeChange = (newRange: string) => {
    setRange(newRange);
  };

  return (
    <Card>
      <CardHeader className="home-flex home-flex-col home-gap-3 !home-space-y-0 md:home-gap-4 lg:!home-flex-row lg:home-justify-between">
        <CardTitle>
          <Link
            to={routes?.userActivities?.path ?? '/'}
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            User Activities
          </Link>
        </CardTitle>
        <div className="home-flex home-w-full home-flex-wrap home-gap-2 md:home-w-auto">
          {ranges.map(r => (
            <Button
              key={r.key}
              onClick={() => handleRangeChange(r.key)}
              size="sm"
              className={cn(
                'hover:home-bg-primary hover:!home-text-white',
                r.key === range
                  ? ''
                  : 'home-bg-transparent !home-text-cloudy-white',
              )}
            >
              {r.label}
            </Button>
          ))}
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="home-flex home-h-[280px] home-items-center home-justify-center md:home-h-[320px] lg:home-h-[360px]">
            <Loader mode="container" />
          </div>
        ) : chartData.length === 0 ? (
          <div className="home-flex home-h-[280px] home-items-center home-justify-center md:home-h-[320px] lg:home-h-[360px]">
            <p className="home-text-white home-text-opacity-50">
              No data available
            </p>
          </div>
        ) : (
          <div className="-home-ml-5 home-h-[280px] home-w-full md:home-h-[320px] lg:home-ml-0 lg:home-h-[360px]">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData}>
              <CartesianGrid stroke="#fff" strokeOpacity={0.1} />
              <XAxis dataKey="name" tick={{ fill: '#ffffffbf' }} />
              <YAxis
                tickFormatter={tickFormatter}
                tick={{ fill: '#ffffffbf' }}
              />
              <Tooltip
                content={<CustomTooltip active={false} payload={[]} />}
              />
              <Line
                type="monotone"
                dataKey="value"
                stroke="#13cd9c"
                strokeWidth={2}
                dot={false}
                connectNulls
              />
            </LineChart>
          </ResponsiveContainer>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default UserActivities;
