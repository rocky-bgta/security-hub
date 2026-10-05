import { cn } from 'utils/Helper';

interface IProps {
  title?: string;
  className?: string;
  data: Array<{
    name: string;
    value: number;
    color: string;
  }>;
}

const ChartLegend = ({ data = [], className, title }: IProps) => {
  return (
    <div
      className={cn(
        'home-flex home-w-full home-flex-col home-gap-4 md:home-w-auto md:home-gap-6',
        className,
      )}
    >
      {title && (
        <h4 className="home-text-base home-font-semibold home-text-white">
          {title}
        </h4>
      )}
      {data.map((entry, index) => (
        <div
          key={index}
          className="home-flex home-items-center home-justify-between home-gap-3"
        >
          <div className="home-flex home-items-center home-gap-3">
            <div
              className="home-size-4 home-rounded-full"
              style={{ backgroundColor: entry.color }}
            />
            <span className="home-text-sm home-text-cloudy-white">
              {entry.name}
            </span>
          </div>
          <span className="home-text-sm home-text-cloudy-white">
            {entry.value}
          </span>
        </div>
      ))}
    </div>
  );
};

export default ChartLegend;
