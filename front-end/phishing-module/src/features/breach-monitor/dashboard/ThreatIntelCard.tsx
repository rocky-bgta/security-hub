import { AlertTriangle, CheckCircle2, Clock } from 'lucide-react';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { ThreatIntelSummary } from 'models/BreachMonitor';

const ThreatIntelCard = ({ data }: { data: ThreatIntelSummary }) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Threat Intelligence Summary</CardTitle>
      </CardHeader>
      <CardContent className="flex h-3/4 w-full items-center justify-center">
        <div className="grid w-full grid-cols-3 gap-4">
          <div className="text-center">
            <div className="mb-2 inline-flex rounded-full bg-[#dc2626]/10 p-2">
              <AlertTriangle className="size-5 text-[#dc2626]" />
            </div>
            <p className="text-2xl font-bold">{data.open}</p>
            <p className="text-xs text-muted-foreground">Open</p>
          </div>
          <div className="text-center">
            <div className="mb-2 inline-flex rounded-full bg-[#eab308]/10 p-2">
              <Clock className="size-5 text-[#eab308]" />
            </div>
            <p className="text-2xl font-bold">{data.inMitigation}</p>
            <p className="text-xs text-muted-foreground">In Mitigation</p>
          </div>
          <div className="text-center">
            <div className="mb-2 inline-flex rounded-full bg-[#22c55e]/10 p-2">
              <CheckCircle2 className="size-5 text-[#22c55e]" />
            </div>
            <p className="text-2xl font-bold">{data.completed}</p>
            <p className="text-xs text-muted-foreground">Completed</p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default ThreatIntelCard;
