import { useState } from 'react';
import { Link } from 'react-router-dom';
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

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { cn } from 'utils/Helper';

// Sample data for each timeframe
const dataByRange = {
  '30days': [
    { name: 'Week 1', value: 25, value2: 20 },
    { name: 'Week 2', value: 30, value2: 28 },
    { name: 'Week 3', value: 28, value2: 27 },
    { name: 'Week 4', value: 40, value2: 35 },
  ],
  '12months': [
    { name: 'Jan', value: 120, value2: 100 },
    { name: 'Feb', value: 200, value2: 180 },
    { name: 'Mar', value: 150, value2: 130 },
    { name: 'Apr', value: 180, value2: 160 },
    { name: 'May', value: 320, value2: 290 },
    { name: 'Jun', value: 53.2, value2: 60 },
    { name: 'Jul', value: 70, value2: 75 },
    { name: 'Aug', value: 20, value2: 25 },
    { name: 'Sep', value: 10, value2: 12 },
    { name: 'Oct', value: 30, value2: 40 },
    { name: 'Nov', value: 150, value2: 130 },
    { name: 'Dec', value: 300, value2: 280 },
  ],
};

const ranges = [
  { key: '30days', label: 'Monthly' },
  { key: '12months', label: 'Yearly' },
];

// Format Y-axis ticks (e.g. 120 -> "120h")
const tickFormatter = (value: number) => `${Math.floor(value)}h`;

// Tooltip formatter: split into hours and minutes
const formatTooltip = (value: number) => {
  const hours = Math.floor(value);
  const minutes = Math.round((value - hours) * 60);
  return `${hours}h ${minutes}m`;
};

const CustomTooltip = ({
  active,
  payload,
}: {
  active: boolean;
  payload: Array<{ color: string; value: number }>;
}) => {
  if (active && payload && payload.length) {
    return (
      <div className="home-rounded home-bg-[#2B414F] home-p-2 home-shadow-lg">
        <div>
          <p className="home-text-sm home-text-white home-text-opacity-50">
            Time spend
          </p>
          <div className="home-flex home-items-center home-gap-1 home-font-semibold home-text-white">
            <div
              className="home-size-3 home-rounded-full"
              style={{ backgroundColor: payload[0]?.color }}
            ></div>
            {`${formatTooltip(payload[0].value)}`}
          </div>
          <div className="home-flex home-items-center home-gap-1 home-font-semibold home-text-white">
            <div
              className="home-size-3 home-rounded-full"
              style={{ backgroundColor: payload[1]?.color }}
            ></div>
            {`${formatTooltip(payload[1].value)}`}
          </div>
        </div>
      </div>
    );
  }
  return null;
};

const CustomLegend = ({
  payload,
}: {
  payload: Array<{ color: string; value: string }>;
}) => {
  return (
    <div className="home-mt-4 home-flex home-flex-wrap home-items-center home-justify-center home-gap-2">
      {payload.map((entry, index) => (
        <div
          key={index}
          className="home-flex home-items-center home-gap-2 home-rounded-lg home-border home-border-card-border home-bg-[#FFFFFF0D] home-px-4 home-py-2"
        >
          <div
            className="home-size-3 home-rounded-full"
            style={{ backgroundColor: entry.color }}
          />
          <span className="home-text-sm home-font-semibold home-text-cloudy-white">
            {entry.value}
          </span>
        </div>
      ))}
    </div>
  );
};

const TrainingProgress = () => {
  const [range, setRange] = useState<string>('12months');
  const data = dataByRange[range as keyof typeof dataByRange];

  return (
    <Card>
      <CardHeader className="home-flex home-flex-col home-gap-3 !home-space-y-0 md:home-gap-4 lg:!home-flex-row lg:home-justify-between">
        <CardTitle>
          <Link
            to="/#"
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            Training Progress
          </Link>
        </CardTitle>
        <div className="home-flex home-w-full home-gap-2 md:home-w-auto">
          {ranges.map(r => (
            <Button
              key={r.key}
              onClick={() => setRange(r.key)}
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
        <div className="-home-ml-5 home-h-[280px] home-w-full md:home-h-[320px] lg:home-ml-0 lg:home-h-[360px]">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data}>
              <CartesianGrid stroke="#fff" strokeOpacity={0.1} />
              <XAxis
                dataKey="name"
                tick={{ fill: '#FFFFFFBF' }}
                fontSize={14}
              />
              <YAxis
                tickFormatter={tickFormatter}
                tick={{ fill: '#FFFFFFBF' }}
                fontSize={14}
              />
              <Tooltip
                content={<CustomTooltip active={false} payload={[]} />}
              />
              <Legend content={() => <CustomLegend payload={[]} />} />
              <Line
                type="monotone"
                dataKey="value"
                stroke="#13cd9c"
                strokeWidth={2}
                dot={false}
                connectNulls
                name="Completion"
              />
              <Line
                type="monotone"
                dataKey="value2"
                stroke="#ff6347"
                strokeWidth={2}
                dot={false}
                connectNulls
                name="Active User"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};

export default TrainingProgress;
