import { useCallback, useEffect, useState } from 'react';
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

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { cn } from 'utils/Helper';
import useAPI from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { IoArrowBack, IoArrowForward } from 'react-icons/io5';

// TypeScript interfaces – matches API response shape
interface SeriesDataItem {
  month?: string;
  year?: string;
  [productName: string]: string | number | undefined;
}

interface TooltipProps {
  active?: boolean;
  payload?: Array<{
    dataKey: string;
    value: number;
    color: string;
    payload?: SeriesDataItem;
  }>;
}

interface CustomXAxisTickProps {
  x?: number;
  y?: number;
  payload?: {
    value: string;
  };
}

const CustomTooltip = ({ active, payload }: TooltipProps) => {
  if (active && payload && payload.length > 0) {
    return (
      <div className="home-rounded-md home-border home-border-card-border home-bg-[#2A3541] home-p-3 home-shadow-lg">
        {payload.map((entry, index) => (
          <div
            key={index}
            className="home-mb-2 home-flex home-items-center home-gap-2"
          >
            <div
              className="home-size-3 home-rounded-full"
              style={{ backgroundColor: entry.color }}
            />
            <span className="home-text-sm home-text-white">
              {entry.dataKey}: {entry.value}
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

const ranges = [
  { key: 'MONTHLY', label: 'Monthly' },
  { key: 'YEARLY', label: 'Yearly' },
] as const;

// Generate distinct colors for products (cycles if more products than palette size)
const generateColors = (count: number): string[] => {
  const colors = [
    '#35CE96',
    '#61FFFD',
    '#A0AEC0',
    '#FFCE20',
    '#FF6B6B',
    '#4ECDC4',
    '#45B7D1',
    '#FFA07A',
    '#98D8C8',
    '#F7DC6F',
    '#FF9999',
    '#9999FF',
    '#99FF99',
    '#FFB366',
    '#66B3FF',
    '#B366FF',
    '#66FFB3',
    '#FF66B3',
  ];
  return Array.from({ length: count }, (_, i) => colors[i % colors.length]);
};

const SalesProgress = () => {
  const apiClient = useAPI();
  const [timeframe, setTimeframe] = useState('YEARLY');
  const [loading, setLoading] = useState(false);
  const [chartData, setChartData] = useState([]);
  const [productCategories, setProductCategories] = useState([]);
  const [productColors, setProductColors] = useState({});
  const [legendStartIndex, setLegendStartIndex] = useState(0);
  const [legendItemsPerView, setLegendItemsPerView] = useState(5);

  useEffect(() => {
    const mobileQuery = window.matchMedia('(max-width: 767px)');
    const tabletQuery = window.matchMedia('(max-width: 1023px)');

    const updateLegendItemsPerView = () => {
      if (mobileQuery.matches) {
        setLegendItemsPerView(1);
        return;
      }

      if (tabletQuery.matches) {
        setLegendItemsPerView(2);
        return;
      }

      setLegendItemsPerView(5);
    };

    updateLegendItemsPerView();

    mobileQuery.addEventListener('change', updateLegendItemsPerView);
    tabletQuery.addEventListener('change', updateLegendItemsPerView);

    return () => {
      mobileQuery.removeEventListener('change', updateLegendItemsPerView);
      tabletQuery.removeEventListener('change', updateLegendItemsPerView);
    };
  }, []);

  const fetchSalesProgress = useCallback(async () => {
    try {
      setLoading(true);

      const response = await apiClient.get(
        API_END_POINTS.SALES_PROGRESS.replace(':timeFrame', timeframe),
      );

      if (response.statusCode === 200 && response.data) {
        const { seriesData, productCategories } = response.data;

        // Use all products from API (no filtering by sales)
        const colors = generateColors(productCategories.length);
        const colorMap: Record<string, string> = {};
        productCategories.forEach((product: string, index: number) => {
          colorMap[product] = colors[index];
        });

        // Transform data to have a common 'period' key
        const transformedData = seriesData.map(
          (item: { month?: string; year?: string }) => ({
            period: item.month || item.year,
            ...item,
          }),
        );

        setProductCategories(productCategories);
        setProductColors(colorMap);
        setChartData(transformedData);
        setLegendStartIndex(0);
      }
    } catch (error) {
      console.error('Error fetching sales progress:', error);
    } finally {
      setLoading(false);
    }
  }, [apiClient, timeframe]);

  useEffect(() => {
    fetchSalesProgress();
  }, [fetchSalesProgress]);

  // Calculate max value for Y-axis
  const maxValue = Math.max(
    ...chartData.flatMap(item =>
      productCategories.map(product => item[product] || 0),
    ),
    10,
  );

  // Legend pagination
  const visibleProducts = productCategories.slice(
    legendStartIndex,
    legendStartIndex + legendItemsPerView,
  );

  const canScrollLeft = legendStartIndex > 0;
  const canScrollRight =
    legendStartIndex + legendItemsPerView < productCategories.length;

  const handleLegendPrev = () => {
    if (canScrollLeft) {
      setLegendStartIndex(prev => Math.max(0, prev - 1));
    }
  };

  const handleLegendNext = () => {
    if (canScrollRight) {
      setLegendStartIndex(prev =>
        Math.min(productCategories.length - legendItemsPerView, prev + 1),
      );
    }
  };

  return (
    <Card>
      <CardHeader className="home-flex home-flex-col home-gap-3 !home-space-y-0 md:home-gap-4 lg:!home-flex-row lg:home-justify-between">
        <CardTitle>
          <Link
            to="/#"
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            Sales Progress by Products
          </Link>
        </CardTitle>
        <div className="home-flex home-w-full home-gap-2 md:home-w-auto">
          {ranges.map(r => (
            <Button
              key={r.key}
              onClick={() => setTimeframe(r.key)}
              size="sm"
              disabled={loading}
              className={cn(
                'hover:home-bg-primary hover:!home-text-white',
                r.key === timeframe
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
        {loading ? (
          <div className="home-flex home-h-[360px] home-items-center home-justify-center">
            <span className="home-text-cloudy-white">Loading...</span>
          </div>
        ) : (
          <div className="-home-ml-6 home-h-[280px] home-w-full md:home-h-[320px] lg:home-ml-0 lg:home-h-[360px]">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid stroke="#fff" strokeOpacity={0.1} />
                <XAxis
                  dataKey="period"
                  tick={<CustomXAxisTick />}
                  interval={0}
                  height={40}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fill: '#FFFFFFBF' }}
                  domain={[0, maxValue + 2]}
                  axisLine={false}
                  tickLine={false}
                />
                <Tooltip
                  content={<CustomTooltip />}
                  cursor={false}
                  isAnimationActive={true}
                  wrapperStyle={{ zIndex: 9999 }}
                  contentStyle={{ zIndex: 9999 }}
                />
                <Legend
                  content={() => (
                    <div className="home-ml-10 home-mt-4 home-flex home-items-center home-justify-center home-gap-2 lg:home-flex-wrap">
                      {/* Left Arrow */}
                      <button
                        onClick={handleLegendPrev}
                        disabled={!canScrollLeft}
                        className={cn(
                          'home-flex home-h-8 home-w-8 home-items-center home-justify-center home-rounded-full home-border home-border-card-border home-bg-[#FFFFFF0D] home-transition-all',
                          canScrollLeft
                            ? 'home-cursor-pointer hover:home-bg-[#FFFFFF1A]'
                            : 'home-cursor-not-allowed home-opacity-30',
                        )}
                      >
                        <IoArrowBack />
                      </button>

                      {/* Legend Items */}
                      <div className="home-flex home-flex-wrap home-items-center home-gap-2">
                        {visibleProducts.map(product => (
                          <div
                            key={product}
                            className="home-flex home-items-center home-gap-2 home-rounded-lg home-border home-border-card-border home-bg-[#FFFFFF0D] home-p-2"
                          >
                            <div
                              className="home-size-2 home-rounded-full"
                              style={{
                                backgroundColor: productColors[product],
                              }}
                            />
                            <span className="home-max-w-36 home-truncate home-text-nowrap home-text-xs home-text-cloudy-white md:home-max-w-44">
                              {product}
                            </span>
                          </div>
                        ))}
                      </div>

                      {/* Right Arrow */}
                      <button
                        onClick={handleLegendNext}
                        disabled={!canScrollRight}
                        className={cn(
                          'home-flex home-h-8 home-w-8 home-items-center home-justify-center home-rounded-full home-border home-border-card-border home-bg-[#FFFFFF0D] home-transition-all',
                          canScrollRight
                            ? 'home-cursor-pointer hover:home-bg-[#FFFFFF1A]'
                            : 'home-cursor-not-allowed home-opacity-30',
                        )}
                      >
                        <IoArrowForward />
                      </button>
                    </div>
                  )}
                />
                {productCategories.map(product => (
                  <Bar
                    key={product}
                    dataKey={product}
                    name={product}
                    fill={productColors[product]}
                    barSize={8}
                    radius={[45, 45, 0, 0]}
                  />
                ))}
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default SalesProgress;
