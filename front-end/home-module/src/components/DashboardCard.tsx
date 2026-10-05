import { Link } from 'react-router-dom';

import { Card, CardContent } from 'common/Card';
import { IDashboardCardTooltip } from 'models/Dashboard';

interface IProps {
  icon: React.ReactNode;
  label: string;
  value: number;
  tooltip?: IDashboardCardTooltip[];
}

const DashboardCard = ({ icon, label, value, tooltip = [] }: IProps) => {
  return (
    <Card>
      <CardContent className="home-flex home-items-center home-gap-x-3 !home-py-4 home-px-4 md:home-gap-x-4 md:!home-py-5">
        <div className="home-flex home-items-center home-justify-center home-rounded-full home-border home-border-primary home-p-2.5 md:home-p-3.5">
          {icon}
        </div>
        <div className="home-min-w-0">
          <p className="home-text-sm home-text-cloudy-white">{label}</p>
          <p className="home-text-xl home-font-bold home-text-white md:home-text-2xl">
            {value}
          </p>
        </div>
        {tooltip?.length > 0 && (
          <div className="home-tooltip-text dashboard-card-tooltip">
            {tooltip.slice(0, 3).map((item, index) => (
              <span key={index} className="home-block home-text-xs">
                {item.packageName}
              </span>
            ))}
            {tooltip.length > 3 && (
              <Link
                to="#"
                className="home-text-sm home-font-medium home-text-primary"
              >
                See More
              </Link>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default DashboardCard;
