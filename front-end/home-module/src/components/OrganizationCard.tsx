import { Eye } from 'lucide-react';

import { Button } from 'common/Button';
import { cn } from 'utils/Helper';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  icon?: string;
  brandName?: string;
  licenseCount?: number;
  status?: string;
  onViewClick?: () => void;
}

const OrganizationCard = ({
  icon,
  brandName,
  licenseCount,
  status = 'ACTIVE',
  onViewClick,
}: IProps) => {
  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'active':
        return 'home-bg-primary';
      case 'inactive':
        return 'home-bg-vibrant-red';
      case 'pending':
        return 'home-bg-yellow-500';
      default:
        return 'home-bg-gray-500';
    }
  };

  return (
    <div className="home-border home-border-card-border home-p-3 md:home-p-4">
      <div className="home-mb-4 home-flex home-flex-col home-gap-3 sm:home-flex-row sm:home-items-center sm:home-justify-between">
        <div className="home-flex home-min-w-0 home-items-center home-gap-3">
          {/* Brand Icon */}
          <div className="home-flex home-size-12 home-shrink-0 home-items-center home-justify-center home-rounded-lg home-bg-[#FFFFFF40] md:home-size-14">
            {icon ? (
              <img
                src={FILE_PATH_PREFIX + icon}
                alt={brandName}
                className="home-size-12 home-rounded home-object-cover md:home-size-14"
              />
            ) : (
              <div className="home-flex home-size-10 home-items-center home-justify-center md:home-size-12">
                <span className="home-text-sm home-font-bold home-text-white md:home-text-base">
                  {brandName?.charAt(0) || 'B'}
                </span>
              </div>
            )}
          </div>

          {/* Brand Info */}
          <div className="home-min-w-0">
            <h3 className="home-line-clamp-1 home-break-all home-text-base home-font-semibold home-text-white md:home-text-lg">
              {brandName}
            </h3>
            <p className="home-text-xs home-text-cloudy-white md:home-text-sm">
              {licenseCount} Licenses
            </p>
          </div>
        </div>

        {/* Status Badge */}
        <span
          className={cn(
            'home-inline-flex home-w-fit home-rounded-full home-px-2.5 home-py-1 home-text-xs home-font-medium home-capitalize home-text-white md:home-px-3 md:home-text-sm',
            getStatusColor(status),
          )}
        >
          {status.toLowerCase()}
        </span>
      </div>

      {/* View Button */}
      <Button
        variant="outline"
        className="home-w-full hover:!home-text-primary"
        onClick={onViewClick}
      >
        <Eye size={18} />
        View
      </Button>
    </div>
  );
};

export default OrganizationCard;
