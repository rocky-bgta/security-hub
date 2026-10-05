import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import TimeRangeSelector, {
  TimeRange,
} from 'components/common/TimeRangeSelector';
import { useAPI } from 'hooks/UseAPI';
import type { IBreachEmailActivityResponse } from 'models/BreachMonitor';
import { IResponse } from 'models/Global';
import { useEffect, useState } from 'react';
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { objectToQueryString } from 'utils/Helper';

const BreachActivityChart = () => {
  const [range, setRange] = useState<TimeRange>('30');
  const [breachedEmailActivity, setBreachedEmailActivity] =
    useState<IBreachEmailActivityResponse>();

  const apiclient = useAPI();

  useEffect(() => {
    const fetchBreachedEmailActivity = async () => {
      try {
        const response: IResponse<IBreachEmailActivityResponse> =
          await apiclient.get(
            API_END_POINTS.GET_BREACH_MONITOR_BREACHED_EMAIL_ACTIVITY +
              objectToQueryString({ days: range }),
          );
        setBreachedEmailActivity(response.data);

        console.log(response.data);
      } catch (error) {
        console.error('Error fetching breached email activity:', error);
      }
    };

    fetchBreachedEmailActivity();
  }, [apiclient, range]);

  return (
    <Card>
      <CardHeader>
        <div className="mb-10 flex items-center justify-between">
          <CardTitle className="text-xl">Breach Activity</CardTitle>
          <TimeRangeSelector value={range} onChange={setRange} />
        </div>
      </CardHeader>
      <CardContent className="h-72">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={breachedEmailActivity?.buckets}>
            <CartesianGrid strokeDasharray="3 3" stroke="#252d37" />
            <XAxis
              dataKey="date"
              tick={{ fontSize: 11, fill: '#818898' }}
              tickFormatter={(v: string) => v.slice(5)}
            />
            <YAxis tick={{ fontSize: 11, fill: '#818898' }} />
            <Tooltip
              contentStyle={{
                background: '#181d25',
                border: '1px solid #252d37',
                borderRadius: 8,
                fontSize: 12,
              }}
            />
            <Legend wrapperStyle={{ fontSize: 12 }} />
            <Line
              type="monotone"
              dataKey="critical"
              stroke="#dc2828"
              strokeWidth={2}
              dot={{ r: 3 }}
              activeDot={{ r: 5, cursor: 'pointer' }}
            />
            <Line
              type="monotone"
              dataKey="high"
              stroke="#f99a15"
              strokeWidth={2}
              dot={{ r: 3 }}
              activeDot={{ r: 5, cursor: 'pointer' }}
            />
            <Line
              type="monotone"
              dataKey="medium"
              stroke="#b0e708"
              strokeWidth={2}
              dot={{ r: 3 }}
              activeDot={{ r: 5, cursor: 'pointer' }}
            />
            <Line
              type="monotone"
              dataKey="low"
              stroke="#21c489"
              strokeWidth={2}
              dot={{ r: 3 }}
              activeDot={{ r: 5, cursor: 'pointer' }}
            />
          </LineChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
};

export default BreachActivityChart;
