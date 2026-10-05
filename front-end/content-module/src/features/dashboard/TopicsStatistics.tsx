import { SliderLeftIcon, SliderRightIcon } from 'assets/icons';
import Border from 'components/UserBorder';
import TopicsStatisticsLoader from 'components/skeleton/dashboard/TopicStatistic';
import { useAPI } from 'hooks/UseAPI';
import { ITopicsStatistics } from 'models/Course';
import { IList, IResponse } from 'models/Global';
import { useEffect, useState } from 'react';
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
import { API_END_POINTS } from 'routes/APIEndpoints';
import { InitGetListParams } from 'utils/Constants';
import { objectToQueryString } from 'utils/Helper';

interface IQueryParams {
  offset: number;
  pageSize: number;
}

const CustomTooltip = ({
  active,
  payload,
}: {
  active?: boolean;
  payload?: any[];
}) => {
  if (active && payload && payload.length > 0) {
    const hoveredEntry = payload[0];
    // const categoryName = hoveredEntry.payload?.courseName;

    return (
      <div className="content-rounded-md content-border content-border-gray-600 content-bg-[#2A3541] content-p-3 content-shadow-lg">
        {/* <p className="content-text-white content-text-sm content-font-semibold content-mb-2">
          {categoryName}
        </p> */}
        <div className="content-mb-2 content-flex content-items-center content-gap-2">
          <div
            className="content-size-3 content-rounded-full"
            style={{ backgroundColor: hoveredEntry.color }}
          />
          <span className="content-text-sm content-capitalize content-text-white">
            {hoveredEntry.dataKey}: {hoveredEntry.value} Topics
          </span>
        </div>
      </div>
    );
  }
  return null;
};

const CustomXAxisTick = (props: any) => {
  const { x, y, payload } = props;
  const words = payload.value.split(' ');

  if (words.length > 2) {
    return (
      <g transform={`translate(${x},${y})`}>
        <text
          x={0}
          y={20}
          dy={16}
          textAnchor="middle"
          fill="#FFFFFFBF"
          fontSize="14"
        >
          <tspan x="0" dy="0">
            {words.slice(0, 2).join(' ')}
          </tspan>
          <tspan x="0" dy="16">
            {words.slice(2).join(' ')}
          </tspan>
        </text>
      </g>
    );
  } else {
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
          {payload.value}
        </text>
      </g>
    );
  }
};

const TopicsStatistics = () => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(false);
  const [data, setData] = useState<IList<ITopicsStatistics>>({
    ...InitGetListParams,
    total: 0,
    items: [],
  });
  const [queryParams, setQueryParams] = useState<IQueryParams>({
    offset: 0,
    pageSize: 4,
  });

  useEffect(() => {
    setLoading(true);

    fetchTopicsStatistics();
  }, [queryParams]);

  const fetchTopicsStatistics = async () => {
    try {
      const response: IResponse<IList<ITopicsStatistics>> = await apiClient.get(
        API_END_POINTS.USER_TOPICS_STATISTICS +
          objectToQueryString(queryParams),
      );

      setData({
        ...data,
        total: response.data.total,
        items: response.data.items,
      });
    } catch (error) {
      console.error('Error fetching topic details:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleNextPage = () => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: prevState.offset + 1,
    }));
  };

  const handlePreviousPage = () => {
    setQueryParams(prevState => ({
      ...prevState,
      offset: prevState.offset - 1,
    }));
  };

  return (
    <div>
      <Border>
        <div className="content-relative content-p-4 sm:content-p-6">
          <div className="content-mb-4 content-flex content-items-center content-justify-between sm:content-mb-5">
            <h2 className="content-text-lg content-font-semibold content-text-white sm:content-text-xl">
              Topic Statistics
            </h2>
          </div>

          {loading ? (
            <TopicsStatisticsLoader />
          ) : (
            <div className="content-w-full content-overflow-x-auto">
            <ResponsiveContainer width="100%" height={350}>
              <BarChart data={data.items} margin={{ right: 20 }}>
                <CartesianGrid stroke="#fff" strokeOpacity={0.1} />
                <XAxis
                  dataKey="subPackageName"
                  tick={<CustomXAxisTick />}
                  interval={0}
                  height={80}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fill: '#FFFFFFBF' }}
                  domain={[0, 12]}
                  ticks={[0, 1, 5, 10]}
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
                  content={props => {
                    const { payload } = props;
                    return (
                      <div className="content-flex content-flex-wrap content-justify-center content-gap-2 sm:content-gap-4">
                        {payload?.map((entry, index) => (
                          <div
                            key={index}
                            className="content-flex content-items-center content-gap-2 content-rounded-lg content-border content-border-gray-500 content-px-2 content-py-1.5 sm:content-px-4 sm:content-py-2"
                            style={{
                              backgroundColor: 'rgba(255, 255, 255, 0.05)',
                            }}
                          >
                            <div
                              className="content-size-3 content-rounded-full"
                              style={{ backgroundColor: entry.color }}
                            />
                            <span className="content-text-sm content-capitalize content-text-white">
                              {entry.value}
                            </span>
                          </div>
                        ))}
                      </div>
                    );
                  }}
                />
                <Bar
                  dataKey="total"
                  fill="#1A88E0"
                  barSize={28}
                  radius={[45, 45, 0, 0]}
                />
                <Bar
                  dataKey="completed"
                  fill="#2AA684"
                  barSize={28}
                  radius={[45, 45, 0, 0]}
                />
                <Bar
                  dataKey="pending"
                  fill="#FFCE20"
                  barSize={28}
                  radius={[45, 45, 0, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
            </div>
          )}

          <div className="content-relative content-mt-4 content-flex content-justify-end sm:content-absolute sm:content-bottom-4 sm:content-right-4 sm:content-mt-0 content-items-center content-gap-2">
            <button
              className="content-z-50 content-flex content-size-8 content-items-center content-justify-center content-rounded-full content-bg-[#D9D9D9]/25 content-transition-all hover:content-bg-primary disabled:content-cursor-not-allowed disabled:content-bg-[#D9D9D9]/10 disabled:hover:content-bg-[#D9D9D9]/10"
              disabled={queryParams.offset === 0}
              onClick={handlePreviousPage}
            >
              <SliderLeftIcon stroke="white" />
            </button>
            <button
              className="content-z-50 content-flex content-size-8 content-items-center content-justify-center content-rounded-full content-bg-[#D9D9D9]/25 content-transition-all hover:content-bg-primary disabled:content-cursor-not-allowed disabled:content-bg-[#D9D9D9]/10 disabled:hover:content-bg-[#D9D9D9]/10"
              disabled={
                Math.ceil(data.total / queryParams.pageSize) - 1 <=
                queryParams.offset
              }
              onClick={handleNextPage}
            >
              <SliderRightIcon stroke="white" />
            </button>
          </div>
        </div>
      </Border>
    </div>
  );
};

export default TopicsStatistics;
