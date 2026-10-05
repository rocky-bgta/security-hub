import { Calendar } from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import { RadioGroup, RadioGroupItem } from 'components/common/Radio';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import { TIMEZONES } from 'features/smishing/data';
import { cn } from 'utils/Helper';

export default function StepSchedule(props: {
  schMode: 'immediate' | 'scheduled' | 'recurring';
  setSchMode: (v: 'immediate' | 'scheduled' | 'recurring') => void;
  schDate: string;
  setSchDate: (v: string) => void;
  schTime: string;
  setSchTime: (v: string) => void;
  schTimezone: string;
  setSchTimezone: (v: string) => void;
  schPattern: 'all' | 'staggered';
  setSchPattern: (v: 'all' | 'staggered') => void;
  schBatchSize: string;
  setSchBatchSize: (v: string) => void;
  schInterval: string;
  setSchInterval: (v: string) => void;
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Calendar className="size-5 text-primary" /> Step 8 - Schedule
          Campaign
        </CardTitle>
        <CardDescription>
          Define when and how the smishing campaign is delivered.
        </CardDescription>
      </CardHeader>
      <CardContent className="grid gap-6 lg:grid-cols-2">
        <div className="space-y-4">
          <div className="space-y-2">
            <Label>When to send *</Label>
            <RadioGroup
              value={props.schMode}
              onValueChange={v =>
                props.setSchMode(v as 'immediate' | 'scheduled' | 'recurring')
              }
              className="grid gap-2"
            >
              {[
                {
                  id: 'immediate',
                  label: 'Send Immediately',
                  desc: 'Queue for instant execution',
                },
                {
                  id: 'scheduled',
                  label: 'Schedule for Later',
                  desc: 'Pick date, time and timezone',
                },
                {
                  id: 'recurring',
                  label: 'Recurring',
                  desc: 'Repeat on a defined cadence',
                },
              ].map(o => (
                <label
                  key={o.id}
                  className={cn(
                    'flex cursor-pointer items-center gap-3 rounded-md border border-card-border p-3 text-sm',
                    props.schMode === o.id && 'border-primary bg-primary/5',
                  )}
                >
                  <RadioGroupItem value={o.id} />
                  <div>
                    <div className="font-medium">{o.label}</div>
                    <div className="text-xs text-muted-foreground">
                      {o.desc}
                    </div>
                  </div>
                </label>
              ))}
            </RadioGroup>
          </div>

          {props.schMode !== 'immediate' && (
            <div className="grid gap-3 sm:grid-cols-3">
              <div className="space-y-2">
                <Label>Start date *</Label>
                <Input
                  type="date"
                  value={props.schDate}
                  onChange={e => props.setSchDate(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>Start time *</Label>
                <Input
                  type="time"
                  value={props.schTime}
                  onChange={e => props.setSchTime(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>Timezone *</Label>
                <Select
                  value={props.schTimezone}
                  onValueChange={props.setSchTimezone}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {TIMEZONES.map(t => (
                      <SelectItem key={t} value={t}>
                        {t}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          )}
        </div>

        <div className="space-y-4">
          <div className="space-y-2">
            <Label>Sending pattern *</Label>
            <RadioGroup
              value={props.schPattern}
              onValueChange={v => props.setSchPattern(v as 'all' | 'staggered')}
              className="grid gap-2"
            >
              <label
                className={cn(
                  'flex cursor-pointer items-center gap-3 rounded-md border border-card-border p-3 text-sm',
                  props.schPattern === 'all' && 'border-primary bg-primary/5',
                )}
              >
                <RadioGroupItem value="all" />
                <div>
                  <div className="font-medium">All at once</div>
                  <div className="text-xs text-muted-foreground">
                    Send all messages simultaneously.
                  </div>
                </div>
              </label>
              <label
                className={cn(
                  'flex cursor-pointer items-center gap-3 rounded-md border p-3 text-sm',
                  props.schPattern === 'staggered' &&
                    'border-primary bg-primary/5',
                )}
              >
                <RadioGroupItem value="staggered" />
                <div>
                  <div className="font-medium">Staggered</div>
                  <div className="text-xs text-muted-foreground">
                    Send in batches over time.
                  </div>
                </div>
              </label>
            </RadioGroup>
          </div>

          {props.schPattern === 'staggered' && (
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <Label>Batch size *</Label>
                <Input
                  type="number"
                  min={1}
                  value={props.schBatchSize}
                  onChange={e => props.setSchBatchSize(e.target.value)}
                />
                <p className="text-xs text-muted-foreground">
                  Messages per batch.
                </p>
              </div>
              <div className="space-y-2">
                <Label>Interval (minutes) *</Label>
                <Input
                  type="number"
                  min={1}
                  value={props.schInterval}
                  onChange={e => props.setSchInterval(e.target.value)}
                />
                <p className="text-xs text-muted-foreground">
                  Delay between batches.
                </p>
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
