import { MouseEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import Border from 'components/UserBorder';
import CertificateStatisticsLoader from 'components/skeleton/dashboard/CertificateStatistics';
import { useAPI } from 'hooks/UseAPI';
import { IUserCertificateStatistics } from 'models/Certificate';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Routes';
import { isSuccessResponse } from 'utils/Helper';

interface PyramidData {
  level: string;
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

const CertificateStatistics = ({
  width = 240,
  height = 240,
  animationDuration = 0,
}: IProps) => {
  const [certificateData, setCertificateData] =
    useState<IUserCertificateStatistics>();
  const [loading, setLoading] = useState<boolean>(false);
  const apiClient = useAPI();

  const chartData: PyramidData[] = [
    {
      level: 'Valid Certifications',
      value: certificateData?.validCount ?? 0,
      color: '#2AA684',
    },
    {
      level: 'Expiring Soon',
      value: certificateData?.expiringSoonCount ?? 0,
      color: '#F7C948',
    },
    {
      level: 'Expired',
      value: certificateData?.expiredCount ?? 0,
      color: '#F65E5B',
    },
  ];
  const [animationProgress, setAnimationProgress] = useState(0);
  const [tooltip, setTooltip] = useState<TooltipData>({
    x: 0,
    y: 0,
    data: chartData[0],
    visible: false,
  });
  const spacing = 8;
  const totalValue =
    (certificateData?.validCount ?? 0) +
    (certificateData?.expiringSoonCount ?? 0) +
    (certificateData?.expiredCount ?? 0);

  useEffect(() => {
    fetchCertificateData();
  }, []);

  const fetchCertificateData = async () => {
    setLoading(true);

    try {
      const response: IResponse<IUserCertificateStatistics> =
        await apiClient.get(API_END_POINTS.USER_CERTIFICATES_STATS);
      if (isSuccessResponse(response.statusCode)) {
        setCertificateData(response.data);
      }
    } catch (error) {
      console.error('Error Certificate Statistics:', error);
    } finally {
      setLoading(false);
    }
  };

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
    <Border>
      <div className="content-p-4 sm:content-p-6">
        <Link
          to={routes.certificates.path}
          className="content-text-lg content-font-semibold content-text-white content-underline sm:content-text-xl"
        >
          Certificate Statistics
        </Link>
        {loading ? (
          <CertificateStatisticsLoader />
        ) : (
          <div className="content-mt-6 content-flex content-flex-col content-items-center content-gap-4 sm:content-mt-10 sm:content-gap-6 md:content-flex-row md:content-justify-evenly">
            <div className="content-relative content-mx-auto content-w-full content-max-w-[240px] md:content-mx-0">
              <svg
                width={width}
                height={height}
                viewBox={`0 0 ${width} ${height}`}
                className="content-h-auto content-w-full content-overflow-visible"
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
                        className="content-cursor-pointer content-transition-all content-duration-300 hover:content-brightness-110"
                        onMouseEnter={e => handleMouseEnter(e, segment)}
                        onMouseLeave={handleMouseLeave}
                      />
                    </g>
                  );
                })}
              </svg>

              {tooltip.visible && (
                <div
                  className="content-pointer-events-none content-absolute content-z-10 content-flex content-w-48 content-items-center content-gap-2 content-rounded-lg content-bg-[#2B414F] content-p-3 content-text-white content-shadow-lg content-transition-all content-duration-200"
                  style={{
                    left: tooltip.x,
                    top: tooltip.y,
                    transform: 'translate(-50%, -100%)',
                  }}
                >
                  <div
                    className={`content-size-4 content-rounded-full`}
                    style={{ backgroundColor: tooltip.data.color }}
                  ></div>
                  <div>
                    {tooltip.data.level}: {tooltip.data.value.toLocaleString()}
                  </div>
                </div>
              )}
            </div>

            <div className="content-flex content-flex-col content-space-y-4 sm:content-space-y-6">
              <div className="content-font-semibold content-text-white">
                Total Certifications {totalValue}
              </div>

              <div className="content-space-y-4">
                {chartData.map((segment, index) => (
                  <div
                    key={index}
                    className="content-flex content-items-center content-justify-between content-gap-4"
                    style={{
                      opacity: animationProgress,
                    }}
                  >
                    <div className="content-flex content-items-center content-gap-3">
                      <div
                        className="content-size-4 content-rounded-full content-transition-transform"
                        style={{ backgroundColor: segment.color }}
                      />
                      <span className="content-text-sm content-text-white content-text-opacity-75">
                        {segment.level}
                      </span>
                    </div>
                    <span className="content-text-sm content-text-white content-text-opacity-75">
                      {segment.value}
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
export default CertificateStatistics;
