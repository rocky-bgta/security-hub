import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import ChartLegend from 'components/ChartLegend';
import { IoFilter } from 'react-icons/io5';
import { Link } from 'react-router-dom';
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';

interface IProps {
  name: string;
  value: number;
  color: string;
}

const data: IProps[] = [
  { name: 'Available', value: 124, color: '#A0AEC0' },
  { name: 'Allocated', value: 200, color: '#F7C948' },
  { name: 'Active', value: 300, color: '#37BE99' },
  { name: 'Expired', value: 100, color: '#F65E5B' },
];

const CustomTooltip = ({
  active,
  payload,
}: {
  active: boolean;
  payload: any;
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
          {`${payload[0].value}`}
        </p>
      </div>
    );
  }
  return null;
};

const LicenseDistribution = () => {
  return (
    <Card>
      <CardHeader className="home-flex !home-flex-row home-justify-between home-space-y-0">
        <CardTitle>
          <Link
            to="/#"
            className="home-text-xl home-font-semibold home-text-white home-underline"
          >
            License Distribution
          </Link>
        </CardTitle>
        <Button variant="outline" className="home-border-secondary">
          <IoFilter className="home-text-xl" />
          Filter
        </Button>
      </CardHeader>
      <CardContent className="home-flex home-items-center home-justify-evenly home-gap-6 home-pb-0">
        <ResponsiveContainer width="60%" height={360}>
          <PieChart>
            <Pie
              data={data}
              dataKey="value"
              nameKey="name"
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={120}
              paddingAngle={6}
              cornerRadius={8}
              startAngle={90}
              endAngle={-270}
              stroke="none"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip content={<CustomTooltip active={false} payload={[]} />} />
          </PieChart>
        </ResponsiveContainer>

        <ChartLegend data={data} />
      </CardContent>
    </Card>
  );
};

export default LicenseDistribution;
