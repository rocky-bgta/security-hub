import { Plus, Tag as TagIcon, X } from 'lucide-react';
import { Badge } from 'components/common/Badge';
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

export default function StepTags(props: {
  tags: string[];
  setTags: (t: string[]) => void;
  tagInput: string;
  setTagInput: (v: string) => void;
  addTag: () => void;
}) {
  const removeTag = (t: string) =>
    props.setTags(props.tags.filter(x => x !== t));
  const suggestions = [
    'Audit_2026',
    'Finance_Dept',
    'Q2_Campaign',
    'HighRisk',
    'Awareness',
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <TagIcon className="size-5 text-primary" /> Step 5 - Tags & Metadata
        </CardTitle>
        <CardDescription>
          Assign custom tags for filtering and reporting. Alphanumeric or
          underscore only.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex gap-2">
          <Input
            placeholder="e.g. Audit_2026"
            value={props.tagInput}
            onChange={e => props.setTagInput(e.target.value)}
            onKeyDown={e =>
              e.key === 'Enter' && (e.preventDefault(), props.addTag())
            }
          />
          <Button onClick={props.addTag}>
            <Plus className="mr-1 size-4" /> Add
          </Button>
        </div>

        <div>
          <p className="mb-2 text-xs text-muted-foreground">Suggestions</p>
          <div className="flex flex-wrap gap-1">
            {suggestions.map(s => (
              <Button
                key={s}
                size="sm"
                variant="outline"
                onClick={() => {
                  if (!props.tags.includes(s))
                    props.setTags([...props.tags, s]);
                }}
              >
                + {s}
              </Button>
            ))}
          </div>
        </div>

        <Separator />

        <div>
          <p className="mb-2 text-sm font-medium">Assigned tags</p>
          {props.tags.length === 0 ? (
            <p className="text-xs text-muted-foreground">No tags yet.</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {props.tags.map(t => (
                <Badge
                  key={t}
                  variant="outline"
                  className="gap-1 border-card-border py-1 pl-2 pr-1"
                >
                  {t}
                  <button
                    onClick={() => removeTag(t)}
                    className="ml-1 rounded-full p-0.5 hover:bg-muted"
                    aria-label={`remove ${t}`}
                  >
                    <X className="size-3" />
                  </button>
                </Badge>
              ))}
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
