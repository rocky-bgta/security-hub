import { Link } from 'react-router-dom';
import {
  Bar,
  BarChart,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import ChartLegend from 'components/ChartLegend';

const data = [
  { name: 'Campaign Presets', value: 190, color: '#37BE99' },
  { name: 'Email Templates', value: 150, color: '#61FFFD' },
  { name: 'Sending Profiles', value: 100, color: '#F7C948' },
  { name: 'Landing Pages', value: 53, color: '#8F8F8F' },
];

interface ITooltipPayloadItem {
  value: number;
  payload: {
    color?: string;
    colors?: string;
  };
}

const CustomTooltip = ({
  active,
  payload,
  coordinate,
}: {
  active?: boolean;
  payload?: ITooltipPayloadItem[];
  coordinate?: { x: number; y: number };
}) => {
  if (active && payload && payload.length && coordinate) {
    const chartHeight = 290;
    const maxValue = Math.max(...data.map(d => d.value));
    const barHeight = (payload[0].value / maxValue) * (chartHeight - 80);
    const barTop = chartHeight - 50 - barHeight;
    const tooltipColor = payload[0].payload.color || payload[0].payload.colors;

    return (
      <div className="home-relative">
        <div
          className="home-absolute home-rounded home-px-3 home-py-1 home-text-sm home-font-medium home-text-white"
          style={{
            backgroundColor: tooltipColor,
            transform: 'translate(-50%, -100%)',
            top: barTop,
            left: coordinate.x,
            pointerEvents: 'none',
          }}
        >
          {payload[0].value}
          <div
            className="home-absolute home-left-1/2 home-top-full home-size-0 -home-translate-x-1/2 home-transform home-border-x-4 home-border-t-4 home-border-transparent"
            style={{ borderTopColor: tooltipColor }}
          ></div>
        </div>
      </div>
    );
  }
  return null;
};

const PhishingContent = () => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>
          <Link
            to="/#"
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            Phishing Content
          </Link>
        </CardTitle>
      </CardHeader>
      <CardContent className="home-flex home-flex-col home-items-center home-gap-4 md:home-gap-6 lg:home-flex-row lg:home-justify-evenly lg:home-pb-0">
        <div className="home-h-[280px] home-w-full md:home-h-[320px] lg:home-h-[360px] lg:home-w-3/5">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart
              data={data}
              margin={{
                top: 40,
                right: 20,
                left: -10,
                bottom: -10,
              }}
              barCategoryGap="80%"
            >
              <XAxis
                dataKey="name"
                axisLine={false}
                tickLine={false}
                tick={false}
              />
              <YAxis hide />
              <Tooltip content={<CustomTooltip />} cursor={false} />
              <Bar dataKey="value" radius={[4, 4, 4, 4]}>
                {data.map(entry => (
                  <Cell key={entry.name} fill={entry.color} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        <ChartLegend data={data} />
      </CardContent>
    </Card>
  );
};

export default PhishingContent;
