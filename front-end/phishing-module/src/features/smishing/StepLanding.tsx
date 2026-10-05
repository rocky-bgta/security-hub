import { useState } from 'react';
import { Eye, Globe, ShieldCheck } from 'lucide-react';
import { Badge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import { Separator } from 'components/common/Separator';
import { Switch } from 'components/common/Switch';
import { LANDING_PAGES } from 'features/smishing/data';
import { cn } from 'utils/Helper';

export default function StepLanding(props: {
  selectedLanding: string;
  setSelectedLanding: (v: string) => void;
  trackingEnabled: boolean;
  setTrackingEnabled: (v: boolean) => void;
}) {
  const [previewId, setPreviewId] = useState<string | null>(null);
  const previewLanding = LANDING_PAGES.find(l => l.id === previewId);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Globe className="size-5 text-primary" /> Step 3 - Landing Page
          Configuration
        </CardTitle>
        <CardDescription>
          Select a phishing landing page. No credentials are stored - only
          &quot;Data Submitted&quot; events are logged.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="rounded-md border border-primary/30 bg-primary/5 p-3 text-xs">
          <div className="flex items-center gap-2 font-medium text-primary">
            <ShieldCheck className="size-4" /> Privacy enforced
          </div>
          <p className="mt-1 text-muted-foreground">
            The system blocks credential storage. Each recipient gets a unique
            trackable URL.
          </p>
        </div>

        <div className="grid gap-3 sm:grid-cols-2">
          {LANDING_PAGES.map(lp => (
            <div
              key={lp.id}
              className={cn(
                'rounded-md border border-card-border p-3 transition hover:bg-muted/40',
                props.selectedLanding === lp.id &&
                  'border-primary bg-primary/5',
              )}
            >
              <div className="flex items-start justify-between">
                <div>
                  <div className="text-sm font-medium">{lp.name}</div>
                  <div className="text-xs text-muted-foreground">
                    {lp.category}
                  </div>
                </div>
                <Badge
                  variant={lp.risk === 'Critical' ? 'destructive' : 'secondary'}
                >
                  {lp.risk}
                </Badge>
              </div>
              <div className="mt-3 flex gap-2">
                <Button
                  size="sm"
                  variant={
                    props.selectedLanding === lp.id ? 'default' : 'outline'
                  }
                  onClick={() => props.setSelectedLanding(lp.id)}
                >
                  {props.selectedLanding === lp.id ? 'Selected' : 'Select'}
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => setPreviewId(lp.id)}
                >
                  <Eye className="mr-1 size-3" /> Preview
                </Button>
              </div>
            </div>
          ))}
        </div>

        <Separator />

        <div className="flex items-center justify-between rounded-md border border-card-border p-3">
          <div>
            <p className="text-sm font-medium">
              Generate unique trackable URL per recipient
            </p>
            <p className="text-xs text-muted-foreground">
              Tracks clicks and submission events securely.
            </p>
          </div>
          <Switch
            checked={props.trackingEnabled}
            onCheckedChange={props.setTrackingEnabled}
          />
        </div>

        <Dialog open={!!previewId} onOpenChange={o => !o && setPreviewId(null)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>{previewLanding?.name}</DialogTitle>
              <DialogDescription>
                {previewLanding?.category} - Risk: {previewLanding?.risk}
              </DialogDescription>
            </DialogHeader>
            <div className="rounded-md border border-card-border bg-muted/30 p-6 text-center text-sm text-muted-foreground">
              Mock preview - fake login form rendered for the recipient. No
              credentials are stored; only an event is logged.
            </div>
          </DialogContent>
        </Dialog>
      </CardContent>
    </Card>
  );
}
