import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import useAPI from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

// TypeScript interfaces
interface ContentDistributionData {
  month: string;
  totalContent: number;
  usedContent: number;
}

interface TooltipPayload {
  dataKey: string;
  name?: string;
  value: number;
  color: string;
  payload?: ContentDistributionData;
}

interface CustomTooltipProps {
  active?: boolean;
  payload?: TooltipPayload[];
}

interface CustomXAxisTickProps {
  x?: number;
  y?: number;
  payload?: {
    value: string;
  };
}

const CustomTooltip = ({ active, payload }: CustomTooltipProps) => {
  if (active && payload && payload.length > 0) {
    return (
      <div className="home-rounded-md home-border home-border-card-border home-bg-[#2A3541] home-p-3 home-shadow-lg">
        {payload.map((entry, index) => (
          <div
            key={index}
            className="home-mb-2 home-flex home-items-center home-gap-2 last:home-mb-0"
          >
            <div
              className="home-size-3 home-rounded-full"
              style={{ backgroundColor: entry.color }}
            />
            <span className="home-text-sm home-capitalize home-text-white">
              {entry.name ?? entry.dataKey}: {entry.value} Topics
            </span>
          </div>
        ))}
      </div>
    );
  }
  return null;
};

const CustomXAxisTick = (props: CustomXAxisTickProps) => {
  const { x = 0, y = 0, payload } = props;
  const label = payload?.value ?? '';

  return (
    <g transform={`translate(${x},${y})`}>
      <text
        x={0}
        y={4}
        dy={16}
        textAnchor="middle"
        fill="#FFFFFFBF"
        fontSize="14"
      >
        {label}
      </text>
    </g>
  );
};

const ContentDistribution = () => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(false);
  const [contentDistribution, setContentDistribution] = useState<
    ContentDistributionData[]
  >([]);

  useEffect(() => {
    const fetchContentDistribution = async () => {
      try {
        setLoading(true);
        const response = await apiClient.get(
          API_END_POINTS.CONTENT_DISTRIBUTION,
        );
        if (isSuccessResponse(response.statusCode) && response.data?.data) {
          setContentDistribution(response.data.data);
        }
      } catch (error) {
        console.error('Error fetching content distribution:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchContentDistribution();
  }, [apiClient]);

  // Calculate max value for Y-axis domain
  const maxValue = Math.max(
    ...contentDistribution.map(item =>
      Math.max(item.totalContent, item.usedContent),
    ),
    10,
  );
  const yAxisMax = Math.ceil(maxValue / 10) * 10 + 2;

  return (
    <Card>
      <CardHeader className="home-flex home-flex-col home-gap-3 !home-space-y-0 md:home-gap-4 lg:!home-flex-row lg:home-justify-between">
        <CardTitle>
          <Link
            to="/#"
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            Content Distribution
          </Link>
        </CardTitle>
      </CardHeader>
      <CardContent>
        {loading ? (
          <div className="home-flex home-h-[360px] home-items-center home-justify-center">
            <p className="home-text-white">Loading...</p>
          </div>
        ) : (
          <div className="-home-ml-5 home-h-[280px] home-w-full md:home-h-[320px] lg:home-h-[360px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={contentDistribution}>
                <CartesianGrid stroke="#fff" strokeOpacity={0.1} />
                <XAxis
                  dataKey="month"
                  tick={<CustomXAxisTick />}
                  interval={0}
                  height={40}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fill: '#FFFFFFBF' }}
                  domain={[0, yAxisMax]}
                  axisLine={false}
                  tickLine={false}
                />
                <Tooltip
                  content={<CustomTooltip />}
                  cursor={false}
                  isAnimationActive={false}
                  shared={false}
                />
                <Legend
                  content={(props: unknown) => {
                    const { payload } = props as { payload?: TooltipPayload[] };
                    return (
                      <div className="home-ml-10 home-flex home-justify-between home-py-2 md:home-gap-4 lg:home-mx-0 lg:home-justify-center">
                        {payload?.map((entry, index) => (
                          <div
                            key={index}
                            className="home-flex home-items-center home-gap-2 home-rounded-lg home-border home-border-card-border home-bg-[#FFFFFF0D] home-px-4 home-py-2"
                          >
                            <div
                              className="home-size-3 home-rounded-full"
                              style={{ backgroundColor: entry.color }}
                            />
                            <span className="home-text-xs home-capitalize home-text-white lg:home-text-sm">
                              {entry.value}
                            </span>
                          </div>
                        ))}
                      </div>
                    );
                  }}
                />
                <Bar
                  dataKey="totalContent"
                  name="Total Content"
                  fill="#35CE96"
                  barSize={20}
                  fontSize={14}
                />
                <Bar
                  dataKey="usedContent"
                  name="Used Content"
                  fill="#A0AEC0"
                  barSize={20}
                  fontSize={14}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default ContentDistribution;
