import { Card } from 'common/Card';
import {
  IUserRiskDistribution,
  RISK_COLORS,
  RiskLevel,
  getRiskLevelLabel,
} from 'models/Dashboard';
import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getSimulationLabel,
  getSimulationPaths,
  type SimulationChannel,
} from 'utils/SimulationChannel';

interface UserRiskDistributionProps {
  data?: IUserRiskDistribution;
  channel?: SimulationChannel;
}

/**
 * Donut chart for user risk distribution
 */
export const UserRiskDistribution: React.FC<UserRiskDistributionProps> = ({
  data,
  channel = 'phishing',
}) => {
  const navigate = useNavigate();
  // Handle risk level click
  const onSegmentClick = (level: RiskLevel) => {
    navigate(
      `${getSimulationPaths(channel).userRiskReport}?riskLevel=${level}`,
    );
  };

  if (!data) {
    return (
      <div className="flex h-64 items-center justify-center rounded-lg">
        <div className="flex animate-pulse flex-col items-center">
          <div className="size-32 rounded-full bg-gray-200" />
          <div className="mt-4 h-4 w-24 rounded bg-gray-200" />
        </div>
      </div>
    );
  }

  const segments = [
    {
      level: RiskLevel.LOW,
      count: data.lowRiskCount,
      percentage: data.lowRiskPercentage,
    },
    {
      level: RiskLevel.MEDIUM,
      count: data.mediumRiskCount,
      percentage: data.mediumRiskPercentage,
    },
    {
      level: RiskLevel.HIGH,
      count: data.highRiskCount,
      percentage: data.highRiskPercentage,
    },
    {
      level: RiskLevel.CRITICAL,
      count: data.criticalRiskCount,
      percentage: data.criticalRiskPercentage,
    },
  ].filter(s => s.count > 0);

  // SVG donut chart parameters
  const size = 200;
  const strokeWidth = 30;
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const centerX = size / 2;
  const centerY = size / 2;

  // Calculate stroke-dasharray for each segment
  let accumulatedOffset = 0;
  const segmentPaths = segments.map(segment => {
    const dashLength = (segment.percentage / 100) * circumference;
    const dashOffset = circumference - accumulatedOffset;
    accumulatedOffset += dashLength;

    return {
      ...segment,
      dashArray: `${dashLength} ${circumference - dashLength}`,
      dashOffset,
      color: RISK_COLORS[segment.level],
    };
  });

  return (
    <Card className="h-full p-6">
      <h3 className="mb-4 text-lg font-medium text-foreground">
        Human Risk Distribution
      </h3>

      <div className="flex flex-col items-center justify-center gap-8 md:flex-row">
        {/* Donut Chart */}
        <div className="relative">
          <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
            {/* Background circle */}
            <circle
              cx={centerX}
              cy={centerY}
              r={radius}
              fill="none"
              stroke="#e5e7eb"
              strokeWidth={strokeWidth}
            />

            {/* Segments */}
            {segmentPaths.map(segment => (
              <circle
                key={segment.level}
                cx={centerX}
                cy={centerY}
                r={radius}
                fill="none"
                stroke={segment.color}
                strokeWidth={strokeWidth}
                strokeDasharray={segment.dashArray}
                strokeDashoffset={segment.dashOffset}
                transform={`rotate(-90 ${centerX} ${centerY})`}
                className="cursor-pointer hover:opacity-80"
                onClick={() => onSegmentClick(segment.level)}
              >
                <title>{`${getRiskLevelLabel(segment.level)}: ${segment.count} (${segment.percentage}%)`}</title>
              </circle>
            ))}
          </svg>

          {/* Center text */}
          <div className="absolute inset-0 flex flex-col items-center justify-center">
            <span className="text-3xl font-bold text-foreground">
              {data.totalUsers}
            </span>
            <span className="text-sm text-muted-foreground">Total Users</span>
          </div>
        </div>

        {/* Legend */}
        <div className="space-y-3">
          {[
            RiskLevel.LOW,
            RiskLevel.MEDIUM,
            RiskLevel.HIGH,
            RiskLevel.CRITICAL,
          ].map(level => {
            const segment = segments.find(s => s.level === level) || {
              count: 0,
              percentage: 0,
            };
            return (
              <div
                key={level}
                className="hover: flex cursor-pointer items-center gap-3 rounded-lg px-3 py-2 transition-colors"
                onClick={() => segment.count > 0 && onSegmentClick?.(level)}
              >
                <div
                  className="size-4 shrink-0 rounded-full"
                  style={{ backgroundColor: RISK_COLORS[level] }}
                />
                <div className="flex-1">
                  <div className="flex items-center justify-between gap-4">
                    <span className="text-sm font-medium text-foreground">
                      {getRiskLevelLabel(level)}
                    </span>
                    <span className="text-sm font-semibold text-foreground">
                      {segment.count}
                    </span>
                  </div>
                  <div className="text-xs text-muted-foreground">
                    {segment.percentage.toFixed(1)}%
                  </div>
                </div>
              </div>
            );
          })}

          {/* Additional stats */}
          <div className="mt-3 border-t pt-3">
            <div className="flex justify-between gap-2 text-sm">
              <span className="text-muted-foreground">Primary Risk: </span>
              <span className="font-medium text-primary">
                {getSimulationLabel(channel)} Simulation
              </span>
            </div>
          </div>
        </div>
      </div>
    </Card>
  );
};

export default UserRiskDistribution;
