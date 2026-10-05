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
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { cn, isSuccessResponse, objectToQueryString } from 'utils/Helper';

const ranges = [
  { key: '7Day', label: '7 Days', days: 7 },
  { key: '1Month', label: '30 Days', days: 30 },
  { key: '12Month', label: '12 Months', days: 365 },
];

// Format Y-axis ticks (e.g. 120 -> "120")
const tickFormatter = (value: number) => `${Math.floor(value)}`;

// Tooltip formatter: show login count
const formatTooltip = (value: number) => {
  return `${value} Logins`;
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
      <div className="content-rounded content-bg-[#2B414F] content-p-2 content-shadow-lg">
        <div>
          <p className="content-text-sm content-text-white content-text-opacity-50">
            Login Counts
          </p>
          <p className="content-font-semibold content-text-white">
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
  const [isLoading, setIsLoading] = useState(false);

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

      if (isSuccessResponse(response.statusCode)) {
        const formattedData = formatChartData(response.data, range);
        setChartData(formattedData);
      }
    } catch (error) {
      console.error('Error fetching user activities:', error);
      setChartData([]);
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
      <CardHeader className="content-flex !content-flex-col content-items-start content-gap-3 !content-p-4 sm:!content-p-6 sm:!content-flex-row sm:content-items-center sm:content-justify-between sm:content-gap-0 sm:content-space-y-0">
        <CardTitle>
          <Link
            to={routes.userActivityLogs.path}
            className="content-text-lg content-font-semibold content-text-white content-underline sm:content-text-xl"
          >
            User Activity
          </Link>
        </CardTitle>
        <div className="content-flex content-flex-wrap content-gap-2">
          {ranges.map(r => (
            <Button
              key={r.key}
              onClick={() => handleRangeChange(r.key)}
              size="sm"
              className={cn(
                'hover:content-bg-primary hover:!content-text-white',
                r.key === range
                  ? ''
                  : 'content-bg-transparent !content-text-cloudy-white',
              )}
            >
              {r.label}
            </Button>
          ))}
        </div>
      </CardHeader>
      <CardContent className="!content-p-4 sm:!content-p-6">
        {isLoading ? (
          <div className="content-flex content-h-[260px] content-items-center content-justify-center md:content-h-[320px]">
            <p className="content-text-white">Loading...</p>
          </div>
        ) : chartData.length === 0 ? (
          <div className="content-flex content-h-[260px] content-items-center content-justify-center md:content-h-[320px]">
            <p className="content-text-white content-text-opacity-50">
              No data available
            </p>
          </div>
        ) : (
          <div className="-content-ml-4 content-h-[260px] content-w-full md:content-ml-0 md:content-h-[320px]">
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
