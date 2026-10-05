import { useEffect, useState } from 'react';
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import ChartLegend from 'components/ChartLegend';
import useAPI from 'hooks/UseAPI';
import { IPhishingAssetCounts } from 'models/Dashboard';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import Loader from 'common/loader/Loader';

interface ChartDataItem {
  key: keyof IPhishingAssetCounts;
  name: string;
  value: number;
  color: string;
}

const CHART_COLORS = {
  emailTemplates: '#37BE99',
  senderProfiles: '#8F8F8F',
  landingPages: '#F7C948',
} as const;

const CustomTooltip = ({
  active,
  payload,
}: {
  active?: boolean;
  payload?: Array<{ name: string; value: number; payload: ChartDataItem }>;
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
          {payload[0].value}
        </p>
      </div>
    );
  }
  return null;
};

const AdminPhishingStatistics = () => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState<boolean>(true);
  const [data, setData] = useState<ChartDataItem[]>([
    {
      key: 'numberOfEmailTemplates',
      name: 'Email Templates',
      value: 0,
      color: CHART_COLORS.emailTemplates,
    },
    {
      key: 'numberOfSenderProfiles',
      name: 'Sender Profiles',
      value: 0,
      color: CHART_COLORS.senderProfiles,
    },
    {
      key: 'numberOfLandingPages',
      name: 'Landing Pages',
      value: 0,
      color: CHART_COLORS.landingPages,
    },
  ]);

  useEffect(() => {
    const fetchAssetCounts = async () => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.CLIENT_ADMIN_PHISHING_ASSET_COUNTS,
        );
        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }

        const updatedData = [...data];
        updatedData.forEach(item => {
          item.value = response.data[item.key as keyof IPhishingAssetCounts];
        });
        setData(updatedData);
      } catch (error) {
        console.error('Error fetching phishing asset counts:', error);
      } finally {
        setLoading(false);
      }
    };

    void fetchAssetCounts();
  }, [apiClient]);

  const totalValue = data.reduce((acc, curr) => acc + curr.value, 0);

  return (
    <Card>
      <CardHeader>
        <CardTitle>Phishing Assets</CardTitle>
      </CardHeader>
      <CardContent className="home-flex home-flex-col home-items-center home-gap-4 md:home-gap-6 lg:home-flex-row lg:home-justify-evenly">
        {loading ? (
          <Loader mode="container" />
        ) : (
          <>
            <div className="home-h-[280px] home-w-full md:home-h-[320px] lg:home-h-[360px] lg:home-w-3/5">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                {totalValue === 0 ? (
                  <Pie
                    data={[
                      {
                        name: 'No Data',
                        value: 1,
                        color: CHART_COLORS.emailTemplates,
                      },
                      {
                        name: 'No Data',
                        value: 1,
                        color: CHART_COLORS.senderProfiles,
                      },
                      {
                        name: 'No Data',
                        value: 1,
                        color: CHART_COLORS.landingPages,
                      },
                    ]}
                    cx="50%"
                    cy="50%"
                    innerRadius={50}
                    outerRadius={100}
                    stroke="none"
                    dataKey="value"
                  >
                    <Cell
                      key="cell-no-data-0"
                      fill={CHART_COLORS.emailTemplates}
                    />
                    <Cell
                      key="cell-no-data-1"
                      fill={CHART_COLORS.senderProfiles}
                    />
                    <Cell
                      key="cell-no-data-2"
                      fill={CHART_COLORS.landingPages}
                    />
                  </Pie>
                ) : (
                  <Pie
                    data={data}
                    cx="50%"
                    cy="50%"
                    innerRadius={50}
                    outerRadius={100}
                    stroke="none"
                    dataKey="value"
                  >
                    {data.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                )}
                <Tooltip content={<CustomTooltip />} />
              </PieChart>
            </ResponsiveContainer>
            </div>

            <ChartLegend data={data} />
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default AdminPhishingStatistics;
