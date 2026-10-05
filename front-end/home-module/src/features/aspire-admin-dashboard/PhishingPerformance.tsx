import { Link } from 'react-router-dom';
import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from 'recharts';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';

const data = [
  { name: 'Reported', value: 176, color: '#37BE99' },
  { name: 'Ignored', value: 60, color: '#F7C948' },
  { name: 'Clicked', value: 85, color: '#F65E5B' },
];

interface ITooltipPayloadItem {
  name: string;
  value: number;
  payload: {
    color: string;
  };
}

const CustomTooltip = ({
  active,
  payload,
}: {
  active?: boolean;
  payload?: ITooltipPayloadItem[];
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

const CustomLegend = () => {
  return (
    <div className="home-flex home-flex-wrap home-items-center home-justify-center home-gap-2 home-py-2">
      {data.map((entry, index) => (
        <div
          key={index}
          className="home-flex home-items-center home-gap-2 home-rounded-lg home-border home-border-card-border home-bg-[#FFFFFF0D] home-px-4 home-py-2"
        >
          <div
            className="home-size-3 home-rounded-full"
            style={{ backgroundColor: entry.color }}
          />
          <span className="home-text-sm home-font-semibold home-text-cloudy-white">
            {entry.name}
          </span>
        </div>
      ))}
    </div>
  );
};

const PhishingPerformance = () => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>
          <Link
            to="/#"
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            Phishing Performance
          </Link>
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="home-h-[280px] home-w-full md:home-h-[320px] lg:home-h-[360px]">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={data}
                cx="50%"
                cy="50%"
                stroke="none"
                dataKey="value"
                labelLine={false}
                label={({ name, percent, x, y, fill }) => (
                  <text
                    x={x}
                    y={y + 8}
                    fill={fill}
                    textAnchor="middle"
                    dominantBaseline="central"
                    style={{
                      fontSize: '12px',
                    }}
                  >
                    {`${name}: ${((percent as number) * 100).toFixed(0)}%`}
                  </text>
                )}
              >
                {data.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip
                content={<CustomTooltip active={false} payload={[]} />}
              />
              <Legend content={CustomLegend} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
};

export default PhishingPerformance;
