import Border from 'components/UserBorder';
import CertificateStatisticsLoader from 'components/skeleton/dashboard/CertificateStatistics';
import { useAPI } from 'hooks/UseAPI';
import {
  IPhishingCampaignStatistics,
  IPhishingStatisticMetric,
  PhishingCampaignChannel,
} from 'models/Phishing';
import { IResponse } from 'models/Global';
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  channel: PhishingCampaignChannel;
  title: string;
  metrics: IPhishingStatisticMetric[];
}

const PhishingStatisticsCard = ({ channel, title, metrics }: IProps) => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(false);
  const [statistics, setStatistics] = useState<IPhishingCampaignStatistics>();

  useEffect(() => {
    fetchPhishingStatistics();
  }, [channel]);

  const fetchPhishingStatistics = async () => {
    setLoading(true);

    try {
      const response: IResponse<IPhishingCampaignStatistics> =
        await apiClient.get(API_END_POINTS.PHISHING_CAMPAIGN_STATISTICS(channel));

      if (isSuccessResponse(response.statusCode)) {
        setStatistics(response.data);
      }
    } catch (error) {
      console.error(`Error fetching ${channel} phishing statistics:`, error);
    } finally {
      setLoading(false);
    }
  };

  const chartData = useMemo(
    () =>
      metrics.map(metric => ({
        name: metric.name,
        value: statistics?.[metric.valueKey] ?? 0,
        colors: metric.colors,
      })),
    [metrics, statistics],
  );

  const totalCampaigns = statistics?.totalCampaigns ?? 0;
  const totalValue = chartData.reduce((acc, curr) => acc + curr.value, 0);

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="content-flex content-items-center content-rounded content-bg-[#2B414F] content-px-4 content-py-2 content-shadow-lg">
          <div
            className="content-mr-2 content-size-3 content-rounded-full"
            style={{ backgroundColor: payload[0].payload.colors }}
          />
          <p className="content-text-center content-font-semibold content-text-white">
            <span className="content-mr-4 content-text-sm content-text-white content-text-opacity-50">
              {payload[0].name}
            </span>{' '}
            {`${totalValue === 0 ? 0 : payload[0].value}`}
          </p>
        </div>
      );
    }
    return null;
  };

  return (
    <Border>
      <div className="content-p-4 sm:content-p-6">
        <Link
          to={routes.phishingList.path}
          className="content-text-lg content-font-semibold content-text-white content-underline sm:content-text-xl"
        >
          {title}
        </Link>

        {loading ? (
          <CertificateStatisticsLoader />
        ) : (
          <div className="content-flex content-flex-col content-items-center content-gap-4 sm:content-gap-6 md:content-flex-row md:content-justify-evenly">
            <div className="content-relative content-mx-auto content-flex content-h-80 content-w-full content-max-w-[320px] content-items-center content-justify-center md:content-mx-0">
              <ResponsiveContainer width="100%" height={320}>
                <PieChart>
                  {totalValue === 0 ? (
                    <Pie
                      data={metrics.map(metric => ({
                        name: 'No Data',
                        value: 1,
                        colors: metric.colors,
                      }))}
                      cx="50%"
                      cy="50%"
                      innerRadius={50}
                      outerRadius={100}
                      stroke="none"
                      dataKey="value"
                    >
                      {metrics.map((entry, index) => (
                        <Cell
                          key={`cell-no-data-${index}`}
                          fill={entry.colors}
                        />
                      ))}
                    </Pie>
                  ) : (
                    <Pie
                      data={chartData}
                      cx="50%"
                      cy="50%"
                      innerRadius={50}
                      outerRadius={100}
                      stroke="none"
                      dataKey="value"
                    >
                      {chartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.colors} />
                      ))}
                    </Pie>
                  )}

                  <Tooltip content={CustomTooltip} />
                </PieChart>
              </ResponsiveContainer>
            </div>

            <div className="content-flex content-flex-col content-items-start content-justify-center content-space-y-4">
              <span className="content-font-semibold content-text-white">
                Total Simulated Campaigns {totalCampaigns}
              </span>

              <div className="content-flex content-flex-col content-gap-4">
                {chartData.map((entry, index) => (
                  <div
                    key={`legend-${index}`}
                    className="content-flex content-items-center content-justify-between content-gap-4"
                  >
                    <div className="content-flex content-items-center content-gap-3">
                      <div
                        className="content-size-4 content-rounded-full"
                        style={{ backgroundColor: entry.colors }}
                      />
                      <span className="content-text-sm content-text-white content-text-opacity-75">
                        {entry.name}
                      </span>
                    </div>
                    <span className="content-text-sm content-text-white content-text-opacity-75">
                      {entry.value}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </Border>
  );
};

export default PhishingStatisticsCard;
