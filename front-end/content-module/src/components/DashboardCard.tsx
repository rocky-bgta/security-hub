import Border from 'components/UserBorder';
import { IDashboardCardTooltip } from 'models/Course';
import { Link } from 'react-router-dom';

interface IProps {
  icon: React.ReactNode;
  label: string;
  value: number;
  tooltip?: IDashboardCardTooltip[];
}

const DashboardCard = ({ icon, label, value, tooltip }: IProps) => {
  return (
    <Border>
      <div
        className={`content-flex content-items-center content-gap-x-3 content-px-3 content-py-4 sm:content-gap-x-4 sm:content-px-4 sm:content-py-5 ${tooltip ? 'content-tooltip' : ''}`}
      >
        <div className="content-flex content-items-center content-justify-center content-rounded-full content-border content-border-primary content-p-2.5 sm:content-p-3.5">
          {icon}
        </div>
        <div className="content-min-w-0">
          <p title={label} className="content-truncate content-text-sm content-text-white content-text-opacity-75">
            {label}
          </p>
          <p className="content-text-xl content-font-bold content-text-white sm:content-text-2xl">
            {value}
          </p>
        </div>
        {tooltip && tooltip.length > 0 && (
          <div className="content-tooltip-text dashboard-card-tooltip">
            {tooltip.slice(0, 3).map((item, index) => (
              <span key={index} className="content-block content-text-xs">
                {item.packageName}
              </span>
            ))}
            {tooltip.length > 3 && (
              <Link
                to="#"
                className="content-text-sm content-font-medium content-text-primary"
              >
                See More
              </Link>
            )}
          </div>
        )}
      </div>
    </Border>
  );
};

export default DashboardCard;
