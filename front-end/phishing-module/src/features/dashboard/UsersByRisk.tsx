import { PieChart, Pie, Cell, ResponsiveContainer } from 'recharts';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  IUserRiskDistribution,
  RISK_COLORS,
  RiskLevel,
  getRiskLevelLabel,
} from 'models/Dashboard';
import { ActivityIcon } from 'lucide-react';

interface UserRiskDistributionProps {
  data: IUserRiskDistribution | null;
}

const RADIAN = Math.PI / 180;

type PieLabelProps = {
  cx?: number;
  cy?: number;
  midAngle?: number;
  innerRadius?: number;
  outerRadius?: number;
  value?: number;
};

const renderSegmentLabel = ({
  cx = 0,
  cy = 0,
  midAngle = 0,
  outerRadius = 0,
  value = 0,
}: PieLabelProps) => {
  const radius = outerRadius + 14;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);
  const isRightSide = x > cx;
  const labelX = isRightSide ? x + 6 : x - 6;
  const textAnchor = isRightSide ? 'start' : 'end';

  return (
    <text
      x={labelX}
      y={y}
      fill="#ffffff"
      textAnchor={textAnchor}
      dominantBaseline="central"
      fontSize={12}
      fontWeight={700}
    >
      {`${value.toFixed(0)}%`}
    </text>
  );
};

type LegendItem = {
  name: string;
  color: string;
};

const CustomLegend = ({ items }: { items: LegendItem[] }) => {
  return (
    <div className="flex h-full flex-wrap items-center justify-center gap-x-4 gap-y-3 pt-2 lg:flex-col lg:items-start lg:justify-center lg:gap-y-4 lg:pt-0">
      {items.map((entry, index) => (
        <div key={index} className="flex items-center gap-2">
          <div
            className="size-3 rounded-full"
            style={{
              backgroundColor: entry.color,
            }}
          />
          <span className="text-sm text-slate-300">{entry.name}</span>
        </div>
      ))}
    </div>
  );
};

/**
 * Users by Risk pie chart component
 */
const UsersByRisk = ({ data }: UserRiskDistributionProps) => {
  const sourceData = data;

  const chartData = [
    {
      name: getRiskLevelLabel(RiskLevel.CRITICAL),
      value: sourceData?.criticalRiskPercentage || 0,
      count: sourceData?.criticalRiskCount || 0,
    },
    {
      name: getRiskLevelLabel(RiskLevel.HIGH),
      value: sourceData?.highRiskPercentage || 0,
      count: sourceData?.highRiskCount || 0,
    },
    {
      name: getRiskLevelLabel(RiskLevel.MEDIUM),
      value: sourceData?.mediumRiskPercentage || 0,
      count: sourceData?.mediumRiskCount || 0,
    },
    {
      name: getRiskLevelLabel(RiskLevel.LOW),
      value: sourceData?.lowRiskPercentage || 0,
      count: sourceData?.lowRiskCount || 0,
    },
  ].filter(item => item.value > 0);

  // Map risk levels to colors
  const colorMap: Record<string, string> = {
    [getRiskLevelLabel(RiskLevel.CRITICAL)]: RISK_COLORS[RiskLevel.CRITICAL],
    [getRiskLevelLabel(RiskLevel.HIGH)]: RISK_COLORS[RiskLevel.HIGH],
    [getRiskLevelLabel(RiskLevel.MEDIUM)]: RISK_COLORS[RiskLevel.MEDIUM],
    [getRiskLevelLabel(RiskLevel.LOW)]: RISK_COLORS[RiskLevel.LOW],
  };

  const legendItems: LegendItem[] = chartData.map(item => ({
    name: item.name,
    color: colorMap[item.name] || '#666',
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Users by Risk</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="relative">
          {chartData.length === 0 ? (
            <div className="flex h-[297px] w-full items-center justify-center text-gray-400">
              <div>
                <ActivityIcon className="mx-auto mb-2 size-12 opacity-50" />
                <p>No data to show</p>
              </div>
            </div>
          ) : (
            <>
              <div className="h-[297px] w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={chartData}
                      cx="50%"
                      cy="95%"
                      startAngle={180}
                      endAngle={0}
                      innerRadius={0}
                      outerRadius={150}
                      paddingAngle={0}
                      dataKey="value"
                      label={renderSegmentLabel}
                      labelLine={false}
                    >
                      {chartData.map((entry, index) => (
                        <Cell
                          key={`cell-${index}`}
                          fill={colorMap[entry.name] || '#666'}
                        />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
              </div>

              <div className="absolute -top-3 right-0">
                <CustomLegend items={legendItems} />
              </div>
            </>
          )}
        </div>
      </CardContent>
    </Card>
  );
};

export default UsersByRisk;
