import { AlertCircle, CheckCircle2, Rocket, Send } from 'lucide-react';
import { Button } from 'components/common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Input } from 'components/common/Input';
import { Separator } from 'components/common/Separator';
import { LANDING_PAGES, type StepKey } from 'features/smishing/data';

export default function StepReview(props: {
  campaignName: string;
  owner: string;
  objective: string;
  smsBody: string;
  selectedLanding: string;
  gateway: string;
  senderId: string;
  tags: string[];
  trSelected: string[];
  totalRecipients: number;
  schMode: string;
  schDate: string;
  schTime: string;
  schTimezone: string;
  schPattern: string;
  testNumber: string;
  setTestNumber: (v: string) => void;
  testStatus: 'idle' | 'sending' | 'delivered' | 'failed';
  setTestStatus: (v: 'idle' | 'sending' | 'delivered' | 'failed') => void;
  sendTestSms: () => void;
  goToStep: (k: StepKey) => void;
  showTraining?: boolean;
}) {
  const sections: { step: StepKey; label: string; value: string }[] = [
    {
      step: 'setup',
      label: 'Setup',
      value: `${props.campaignName} • ${props.owner} • ${props.objective}`,
    },
    {
      step: 'template',
      label: 'SMS Template',
      value:
        props.smsBody.slice(0, 80) + (props.smsBody.length > 80 ? '…' : ''),
    },
    {
      step: 'landing',
      label: 'Landing Page',
      value:
        LANDING_PAGES.find(l => l.id === props.selectedLanding)?.name || '—',
    },
    {
      step: 'gateway',
      label: 'SMS Gateway',
      value: `${props.gateway || '—'} • Sender ${props.senderId || '—'}`,
    },
    { step: 'tags', label: 'Tags', value: props.tags.join(', ') || '—' },
    {
      step: 'audience',
      label: 'Audience',
      value: `${props.totalRecipients} recipient${props.totalRecipients === 1 ? '' : 's'}`,
    },
    ...(props.showTraining
      ? [
          {
            step: 'training' as StepKey,
            label: 'Training Modules',
            value: `${props.trSelected.length} selected`,
          },
        ]
      : []),
    {
      step: 'schedule',
      label: 'Schedule',
      value:
        props.schMode === 'immediate'
          ? 'Immediate'
          : `${props.schMode} • ${props.schDate || '?'} ${props.schTime || ''} ${props.schTimezone} • ${props.schPattern}`,
    },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Rocket className="size-5 text-primary" /> Step 9 - Final Review &
          Launch
        </CardTitle>
        <CardDescription>
          Review all campaign settings, send a mandatory Test SMS, then launch.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid gap-2">
          {sections.map(s => (
            <div
              key={s.step}
              className="flex items-start justify-between gap-3 rounded-md border border-card-border p-3 text-sm"
            >
              <div className="flex-1">
                <div className="text-xs uppercase tracking-wide text-muted-foreground">
                  Step {s.step} • {s.label}
                </div>
                <div className="mt-0.5 font-medium">{s.value}</div>
              </div>
              <Button
                size="sm"
                variant="ghost"
                onClick={() => props.goToStep(s.step)}
              >
                Edit
              </Button>
            </div>
          ))}
        </div>

        <Separator />

        <div className="space-y-3 rounded-md border border-primary/30 bg-primary/5 p-4">
          <div className="flex items-center gap-2">
            <Send className="size-4 text-primary" />
            <h3 className="text-sm font-semibold">
              Mandatory Test SMS Verification
            </h3>
          </div>
          <p className="text-xs text-muted-foreground">
            Send a Test SMS to a designated number. Launch is disabled until
            delivery is confirmed.
          </p>
          <div className="flex flex-wrap items-center gap-2">
            <Input
              placeholder="+15551234567"
              value={props.testNumber}
              onChange={e => props.setTestNumber(e.target.value)}
              className="max-w-xs"
              disabled={props.testStatus === 'sending'}
            />
            <Button
              onClick={props.sendTestSms}
              disabled={
                props.testStatus === 'sending' ||
                props.testStatus === 'delivered'
              }
            >
              <Send className="mr-1 size-4" />
              {props.testStatus === 'sending'
                ? 'Sending...'
                : props.testStatus === 'delivered'
                  ? 'Delivered'
                  : 'Send Test SMS'}
            </Button>
            {props.testStatus === 'delivered' && (
              <Button
                size="sm"
                variant="ghost"
                onClick={() => props.setTestStatus('idle')}
              >
                Re-test
              </Button>
            )}
          </div>

          {props.testStatus === 'delivered' && (
            <div className="flex items-center gap-2 text-xs text-primary">
              <CheckCircle2 className="size-4" /> Test SMS delivered. Launch
              enabled.
            </div>
          )}
          {props.testStatus === 'failed' && (
            <div className="flex items-center gap-2 text-xs text-destructive">
              <AlertCircle className="size-4" /> Test SMS failed. Retry to
              enable launch.
            </div>
          )}
          {props.testStatus === 'idle' && (
            <div className="flex items-center gap-2 text-xs text-muted-foreground">
              <AlertCircle className="size-4" /> Test SMS verification required
              before launch.
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
