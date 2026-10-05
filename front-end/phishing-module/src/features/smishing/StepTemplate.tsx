import {
  AlertCircle,
  FileText,
  MessageSquare,
  Plus,
  RefreshCw,
} from 'lucide-react';
import { Badge } from 'components/common/Badge';
import { Button } from 'components/common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Label } from 'components/common/Label';
import { Textarea } from 'components/common/Textarea';
import { PLACEHOLDERS, SMS_TEMPLATES } from 'features/smishing/data';
import { cn } from 'utils/Helper';

export default function StepTemplate(props: {
  mode: 'library' | 'custom';
  setMode: (v: 'library' | 'custom') => void;
  selectedTpl: string;
  pickTemplate: (id: string) => void;
  smsBody: string;
  setSmsBody: (v: string) => void;
  insertPlaceholder: (p: string) => void;
  seg: { chars: number; segments: number; perSegment: number };
}) {
  const overLimit = props.seg.chars > 160;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <MessageSquare className="size-5 text-primary" /> Step 2 - SMS
          Template Crafting
        </CardTitle>
        <CardDescription>
          Choose a pre-defined template or craft a custom SMS with dynamic
          placeholders.
        </CardDescription>
      </CardHeader>
      <CardContent className="grid gap-6 lg:grid-cols-2">
        <div className="space-y-3">
          <div className="flex gap-2">
            <Button
              variant={props.mode === 'library' ? 'default' : 'outline'}
              size="sm"
              onClick={() => props.setMode('library')}
            >
              <FileText className="mr-1 size-4" /> Template Library
            </Button>
            <Button
              variant={props.mode === 'custom' ? 'default' : 'outline'}
              size="sm"
              onClick={() => props.setMode('custom')}
            >
              <Plus className="mr-1 size-4" /> Custom SMS
            </Button>
            <Button variant="ghost" size="icon" title="Refresh">
              <RefreshCw className="size-4" />
            </Button>
          </div>

          {props.mode === 'library' ? (
            <div className="grid gap-2">
              {SMS_TEMPLATES.map(t => (
                <button
                  key={t.id}
                  onClick={() => props.pickTemplate(t.id)}
                  className={cn(
                    'rounded-md border border-card-border p-3 text-left transition hover:bg-muted/40',
                    props.selectedTpl === t.id && 'border-primary bg-primary/5',
                  )}
                >
                  <div className="text-sm font-medium">{t.name}</div>
                  <div className="mt-1 line-clamp-2 text-xs text-muted-foreground">
                    {t.body}
                  </div>
                </button>
              ))}
            </div>
          ) : (
            <div className="rounded-md border border-card-border bg-muted/30 p-3 text-xs text-muted-foreground">
              Custom mode active. Compose your SMS in the editor on the right.
            </div>
          )}
        </div>

        <div className="space-y-3">
          <Label>SMS Editor</Label>
          <Textarea
            value={props.smsBody}
            onChange={e => props.setSmsBody(e.target.value)}
            rows={6}
            placeholder="Type your SMS here..."
            className="font-mono text-sm"
          />
          <div className="flex flex-wrap gap-1">
            {PLACEHOLDERS.map(p => (
              <Button
                key={p}
                size="sm"
                variant="outline"
                onClick={() => props.insertPlaceholder(p)}
              >
                {p}
              </Button>
            ))}
          </div>
          <div className="flex items-center justify-between rounded-md border border-card-border p-2 text-xs">
            <span>
              Characters: <strong>{props.seg.chars}</strong> / 160
            </span>
            <span>
              Segments: <strong>{props.seg.segments}</strong>
            </span>
            {overLimit && (
              <Badge variant="destructive" className="gap-1">
                <AlertCircle className="size-3" /> Multi-segment
              </Badge>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
