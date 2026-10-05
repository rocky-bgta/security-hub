import {
  Activity,
  CheckCircle2,
  Clock,
  RefreshCw,
  Send,
  ShieldCheck,
} from 'lucide-react';
import { Badge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { cn } from 'utils/Helper';

export default function LiveMonitoring(props: {
  campaignName: string;
  owner: string;
  gateway: string;
  senderId: string;
  totalRecipients: number;
  schMode: string;
  onReset: () => void;
}) {
  const sent = Math.max(1, Math.round(props.totalRecipients * 0.6));
  const delivered = Math.round(sent * 0.92);
  const clicked = Math.round(delivered * 0.18);
  const reported = Math.round(delivered * 0.05);

  const metrics = [
    { label: 'Sent', value: sent, icon: Send, tone: 'text-primary' },
    {
      label: 'Delivered',
      value: delivered,
      icon: CheckCircle2,
      tone: 'text-emerald-500',
    },
    {
      label: 'Clicked',
      value: clicked,
      icon: Activity,
      tone: 'text-amber-500',
    },
    {
      label: 'Reported',
      value: reported,
      icon: ShieldCheck,
      tone: 'text-sky-500',
    },
  ];

  return (
    <div className="space-y-6">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Activity className="size-5" />
          <div>
            <h1 className="text-2xl font-semibold tracking-tight">
              Live Monitoring
            </h1>
            <p className="text-sm text-muted-foreground">
              Real-time SMS delivery and engagement tracking.
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Badge className="gap-1 bg-emerald-500/15 text-emerald-600 hover:bg-emerald-500/20">
            <span className="size-2 animate-pulse rounded-full bg-emerald-500" />{' '}
            LIVE
          </Badge>
          <Button variant="outline" size="sm" onClick={props.onReset}>
            <RefreshCw className="mr-1 size-3" /> New Campaign
          </Button>
        </div>
      </header>

      <Card>
        <CardContent className="grid gap-3 p-4 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <span className="text-xs text-muted-foreground">Campaign</span>
            <div className="font-medium">{props.campaignName || '-'}</div>
          </div>
          <div>
            <span className="text-xs text-muted-foreground">Owner</span>
            <div className="font-medium">{props.owner || '-'}</div>
          </div>
          <div>
            <span className="text-xs text-muted-foreground">
              Gateway / Sender
            </span>
            <div className="font-medium">
              {props.gateway || '-'} / {props.senderId || '-'}
            </div>
          </div>
          <div>
            <span className="text-xs text-muted-foreground">Mode</span>
            <div className="font-medium capitalize">{props.schMode}</div>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        {metrics.map(m => (
          <Card key={m.label}>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <span className="text-xs uppercase tracking-wide text-muted-foreground">
                  {m.label}
                </span>
                <m.icon className={cn('size-4', m.tone)} />
              </div>
              <div className="mt-2 text-3xl font-semibold">{m.value}</div>
              <div className="mt-1 text-xs text-muted-foreground">
                of {props.totalRecipients} recipients
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <Clock className="size-4" /> Activity Log
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          {[
            {
              t: 'just now',
              msg: 'Campaign launched and execution engine activated.',
            },
            {
              t: '1s ago',
              msg: `First batch dispatched via ${props.gateway || 'gateway'}.`,
            },
            {
              t: '5s ago',
              msg: `${delivered} delivery confirmations received.`,
            },
            {
              t: '12s ago',
              msg: `${clicked} link clicks tracked on landing page.`,
            },
            {
              t: '18s ago',
              msg: `${reported} users reported the SMS as suspicious.`,
            },
          ].map((row, i) => (
            <div
              key={i}
              className="flex items-start gap-3 rounded-md border border-card-border p-2"
            >
              <span className="mt-0.5 inline-block size-2 rounded-full bg-primary" />
              <span className="flex-1">{row.msg}</span>
              <span className="text-xs text-muted-foreground">{row.t}</span>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
