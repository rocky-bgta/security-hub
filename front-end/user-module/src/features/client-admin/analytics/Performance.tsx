import { ArrowDownRight, ArrowUpRight, Filter, Search } from 'lucide-react';
import { useState } from 'react';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Input } from 'common/Input';
import { TabsContent } from 'common/Tabs';

interface IProps {
  mspComparisonData: Array<{
    name: string;
    revenue: number;
    clients: number;
    growth: number;
    risk: string;
    rating: number;
  }>;
}

const Performance = ({ mspComparisonData }: IProps) => {
  const [searchTerm, setSearchTerm] = useState<string>('');
  const filteredMSPs = mspComparisonData.filter(msp =>
    msp.name.toLowerCase().includes(searchTerm.toLowerCase()),
  );

  return (
    <TabsContent value="performance" className="space-y-4">
      <div className="flex items-center gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            placeholder="Search MSPs..."
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
            className="pl-9"
          />
        </div>
        <Button variant="outline" size="sm">
          <Filter className="mr-2 size-4" />
          Filter
        </Button>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>MSP Performance Comparison</CardTitle>
          <CardDescription>
            Detailed performance metrics for all MSP partners
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {filteredMSPs.map(msp => (
              <div
                key={msp.name}
                className="grid grid-cols-1 gap-4 rounded-lg border p-4 md:grid-cols-6"
              >
                <div className="md:col-span-2">
                  <h4 className="font-medium">{msp.name}</h4>
                  <p className="text-sm text-muted-foreground">
                    {msp.clients} active clients
                  </p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Revenue</p>
                  <p className="font-medium">${msp.revenue.toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Growth</p>
                  <div className="flex items-center">
                    {msp.growth > 0 ? (
                      <ArrowUpRight className="mr-1 size-4 text-green-600" />
                    ) : (
                      <ArrowDownRight className="mr-1 size-4 text-red-600" />
                    )}
                    <span
                      className={
                        msp.growth > 0 ? 'text-green-600' : 'text-red-600'
                      }
                    >
                      {msp.growth}%
                    </span>
                  </div>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Rating</p>
                  <div className="flex items-center">
                    <span className="mr-1 font-medium">{msp.rating}</span>
                    <span className="text-yellow-500">★</span>
                  </div>
                </div>
                <div>
                  <Badge
                    variant={
                      msp.risk === 'Low'
                        ? 'default'
                        : msp.risk === 'Medium'
                          ? 'secondary'
                          : 'destructive'
                    }
                  >
                    {msp.risk} Risk
                  </Badge>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </TabsContent>
  );
};

export default Performance;
