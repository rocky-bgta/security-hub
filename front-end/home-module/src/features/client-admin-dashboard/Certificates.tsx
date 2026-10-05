import { MouseEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import ChartLegend from 'components/ChartLegend';
import { IUserCertificateStatistics } from 'models/Certificate';
import { isSuccessResponse } from 'utils/Helper';
import useAPI from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Route';
import Loader from 'common/loader/Loader';

interface PyramidData {
  key: keyof IUserCertificateStatistics;
  name: string;
  value: number;
  color: string;
  description?: string;
}

interface TooltipData {
  x: number;
  y: number;
  data: PyramidData;
  visible: boolean;
}

interface IProps {
  width?: number;
  height?: number;
  animationDuration?: number;
}

const AdminCertificateStatistics = ({
  width = 240,
  height = 240,
  animationDuration = 0,
}: IProps) => {
  const [loading, setLoading] = useState<boolean>(true);
  const apiClient = useAPI();
  const [chartData, setChartData] = useState<PyramidData[]>([
    {
      key: 'validCount',
      name: 'Valid Certifications',
      value: 0,
      color: '#2AA684',
    },
    {
      key: 'expiringSoonCount',
      name: 'Expiring Soon',
      value: 0,
      color: '#F7C948',
    },
    {
      key: 'expiredCount',
      name: 'Expired',
      value: 0,
      color: '#F65E5B',
    },
  ]);

  const [animationProgress, setAnimationProgress] = useState(0);
  const [tooltip, setTooltip] = useState<TooltipData>({
    x: 0,
    y: 0,
    data: chartData[0],
    visible: false,
  });
  const spacing = 8;
  const totalValue = chartData.reduce((sum, item) => sum + item.value, 0);

  useEffect(() => {
    const startTime = Date.now();
    const animate = () => {
      const elapsed = Date.now() - startTime;
      const progress = Math.min(elapsed / animationDuration, 1);
      const easeOutCubic = 1 - Math.pow(1 - progress, 3);
      setAnimationProgress(easeOutCubic);

      if (progress < 1) {
        requestAnimationFrame(animate);
      }
    };

    requestAnimationFrame(animate);
  }, [animationDuration]);

  const fetchCertificateData = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.CLIENT_ADMIN_CERTIFICATES_STATS + 'isClientAdmin=true',
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      const updatedChartData = [...chartData];
      updatedChartData.forEach(item => {
        item.value =
          response.data[item.key as keyof IUserCertificateStatistics];
      });
      setChartData(updatedChartData);
    } catch (error) {
      console.error('Error fetching certificate data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCertificateData();
  }, []);

  const handleMouseEnter = (event: MouseEvent, segmentData: PyramidData) => {
    const rect = event.currentTarget.getBoundingClientRect();
    setTooltip({
      x: event.clientX - rect.left + 10,
      y: event.clientY - rect.top - 10,
      data: segmentData,
      visible: true,
    });
  };

  const handleMouseLeave = () => {
    setTooltip(prev => ({ ...prev, visible: false }));
  };

  const handleMouseMove = (event: MouseEvent) => {
    if (tooltip.visible) {
      const rect = event.currentTarget.getBoundingClientRect();
      setTooltip(prev => ({
        ...prev,
        x: event.clientX - rect.left + 10,
        y: event.clientY - rect.top - 10,
      }));
    }
  };

  // Calculate segment dimensions based on pyramid structure and data values
  const calculateSegmentDimensions = (index: number) => {
    // Calculate height based on the segment's value proportion
    const availableHeight = height - (chartData.length - 1) * spacing;
    const segmentHeight =
      totalValue > 0
        ? (chartData[index].value / totalValue) * availableHeight
        : availableHeight / chartData.length;

    // Calculate cumulative Y position
    let cumulativeY = 0;
    for (let i = 0; i < index; i++) {
      const prevHeight =
        totalValue > 0
          ? (chartData[i].value / totalValue) * availableHeight
          : availableHeight / chartData.length;
      cumulativeY += prevHeight + spacing;
    }

    // Create true pyramid shape: linear taper from full width at top to point at bottom
    const totalHeight = height;

    // Width at the top edge of this segment (based on Y position)
    const topWidthRatio = 1 - cumulativeY / totalHeight;

    // Width at the bottom edge of this segment (based on Y + height position)
    const bottomY = cumulativeY + segmentHeight;
    const bottomWidthRatio = 1 - bottomY / totalHeight;

    const topWidth = width * topWidthRatio;
    const bottomWidth = width * Math.max(bottomWidthRatio, 0);

    return {
      segmentHeight,
      topWidth,
      bottomWidth,
      y: cumulativeY,
    };
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>
          <Link
            to={routes?.certificateStatistics?.path ?? '/'}
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            Certificate Statistics
          </Link>
        </CardTitle>
      </CardHeader>
      <CardContent className="home-flex home-flex-col home-items-center home-gap-4 home-pb-0 md:home-gap-6 lg:home-flex-row lg:home-justify-evenly">
        {loading ? (
          <Loader mode="container" />
        ) : (
          <div className="home-mt-6 home-flex home-w-full home-flex-col home-items-center home-justify-evenly home-gap-6 lg:home-mt-10 lg:home-flex-row">
            <div className="home-relative home-mx-auto home-w-full home-max-w-[240px] lg:home-mx-0">
              <svg
                width={width}
                height={height}
                className="home-h-auto home-w-full home-overflow-visible"
                onMouseMove={handleMouseMove}
              >
                {chartData.map((segment, index) => {
                  const { segmentHeight, topWidth, bottomWidth, y } =
                    calculateSegmentDimensions(index);

                  const animatedTopWidth = topWidth * animationProgress;
                  const animatedBottomWidth = bottomWidth * animationProgress;
                  const x = (width - animatedTopWidth) / 2;
                  const bottomX = (width - animatedBottomWidth) / 2;

                  const path = `
                M ${x} ${y}
                L ${x + animatedTopWidth} ${y}
                L ${bottomX + animatedBottomWidth} ${y + segmentHeight}
                L ${bottomX} ${y + segmentHeight}
                Z
              `;

                  return (
                    <g key={index}>
                      <path
                        d={path}
                        fill={segment.color}
                        stroke="none"
                        className="home-cursor-pointer home-transition-all home-duration-300 hover:home-brightness-110"
                        onMouseEnter={e => handleMouseEnter(e, segment)}
                        onMouseLeave={handleMouseLeave}
                      />
                    </g>
                  );
                })}
              </svg>

              {tooltip.visible && (
                <div
                  className="home-pointer-events-none home-absolute home-z-10 home-flex home-w-48 home-items-center home-gap-2 home-rounded-lg home-bg-[#2B414F] home-p-3 home-text-white home-shadow-lg home-transition-all home-duration-200"
                  style={{
                    left: tooltip.x,
                    top: tooltip.y,
                    transform: 'translate(-50%, -100%)',
                  }}
                >
                  <div
                    className={`home-size-4 home-rounded-full`}
                    style={{ backgroundColor: tooltip.data.color }}
                  ></div>
                  <div>
                    {tooltip.data.name}: {tooltip.data.value.toLocaleString()}
                  </div>
                </div>
              )}
            </div>

            <ChartLegend
              title={`Total Certifications ${totalValue}`}
              data={chartData}
            />
          </div>
        )}
      </CardContent>
    </Card>
  );
};
export default AdminCertificateStatistics;
