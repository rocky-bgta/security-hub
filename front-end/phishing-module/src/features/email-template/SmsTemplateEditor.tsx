import { Button } from 'common/Button';
import { Progress } from 'components/common/Progress';
import { Textarea } from 'components/common/Textarea';
import { AlertCircle } from 'lucide-react';
import { useCallback, useMemo, useRef } from 'react';
import { toast } from 'react-toastify';
import {
  calcSmsSegments,
  expandSmsPhishingLink,
  getSmsCharInfo,
  getSmsEffectiveLength,
  SMS_MAX_CHARACTERS,
  truncateSmsBodyToEffectiveLimit,
} from 'utils/smsSegments';
import { cn } from 'utils/Helper';
import { type SmsDeliveryMode } from './SmsDeliveryRulesCard';
import { PLACEHOLDER_GROUPS } from './templatePlaceholders';

export type { SmsDeliveryMode };

interface SmsTemplateEditorProps {
  value: string;
  onChange: (value: string) => void;
  error?: string;
  deliveryMode: SmsDeliveryMode;
  /** When set, {{PHISHING_LINK}} counts as this URL length toward the 160 limit */
  phishingPreviewUrl?: string;
}

const SmsTemplateEditor = ({
  value,
  onChange,
  error,
  deliveryMode,
  phishingPreviewUrl,
}: SmsTemplateEditorProps) => {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const isSingleMode = deliveryMode === 'single';
  const charInfo = useMemo(
    () => getSmsCharInfo(value, phishingPreviewUrl),
    [value, phishingPreviewUrl],
  );
  const segmentInfo = useMemo(
    () => calcSmsSegments(expandSmsPhishingLink(value, phishingPreviewUrl)),
    [value, phishingPreviewUrl],
  );
  const isSingleAtLimit = isSingleMode && charInfo.chars >= SMS_MAX_CHARACTERS;

  const wouldExceedSingleLimit = useCallback(
    (token: string) => {
      const textarea = textareaRef.current;
      const start = textarea?.selectionStart ?? value.length;
      const end = textarea?.selectionEnd ?? value.length;
      const nextValue = value.slice(0, start) + token + value.slice(end);
      return (
        getSmsEffectiveLength(nextValue, phishingPreviewUrl) >
        SMS_MAX_CHARACTERS
      );
    },
    [phishingPreviewUrl, value],
  );

  const handleTextChange = useCallback(
    (next: string) => {
      if (!isSingleMode) {
        onChange(next);
        return;
      }
      onChange(
        truncateSmsBodyToEffectiveLimit(next, phishingPreviewUrl),
      );
    },
    [isSingleMode, onChange, phishingPreviewUrl],
  );

  const insertAtCursor = useCallback(
    (token: string, label: string) => {
      if (isSingleMode && wouldExceedSingleLimit(token)) {
        toast.error(
          `Cannot insert field — message would exceed ${SMS_MAX_CHARACTERS} characters (including the phishing link URL)`,
        );
        return;
      }

      const textarea = textareaRef.current;
      if (!textarea) {
        handleTextChange(value + token);
        toast.success(`Inserted ${label}`);
        return;
      }

      const start = textarea.selectionStart;
      const end = textarea.selectionEnd;
      const newValue = value.slice(0, start) + token + value.slice(end);
      handleTextChange(newValue);

      const cursorPos = start + token.length;
      requestAnimationFrame(() => {
        textarea.focus();
        textarea.setSelectionRange(cursorPos, cursorPos);
      });

      toast.success(`Inserted ${label}`);
    },
    [handleTextChange, isSingleMode, value, wouldExceedSingleLimit],
  );

  const progressBarClass = charInfo.isOverLimit
    ? '[&>div]:bg-destructive'
    : charInfo.isNearLimit
      ? '[&>div]:bg-amber-500'
      : '[&>div]:bg-primary';

  return (
    <div className="mt-2 space-y-3">
      <div className="grid gap-4 lg:grid-cols-[1fr_280px]">
        <div className="space-y-2">
          <Textarea
            ref={textareaRef}
            value={value}
            onChange={e => handleTextChange(e.target.value)}
            rows={8}
            placeholder="Type your SMS message here..."
            className={cn(
              'text-sm',
              isSingleMode &&
                (error || charInfo.isOverLimit) &&
                'border-vibrant-red',
            )}
            aria-invalid={isSingleMode && (charInfo.isOverLimit || !!error)}
          />
        </div>

        <div
          className={cn(
            'rounded-md border border-card-border bg-card-background p-3',
            isSingleAtLimit && 'pointer-events-none opacity-50',
          )}
        >
          <p className="mb-3 text-sm font-medium text-primary">Insert Fields</p>
          <div className="space-y-4">
            {PLACEHOLDER_GROUPS.map(group => (
              <div key={group.title}>
                <p className="mb-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">
                  {group.title}
                </p>
                <div className="flex flex-wrap gap-1.5">
                  {group.fields.map(field => (
                    <Button
                      key={field.token}
                      type="button"
                      size="sm"
                      variant="outline"
                      className="h-7 px-2 text-xs"
                      title={`${field.token} — ${field.description}`}
                      disabled={
                        isSingleAtLimit ||
                        (isSingleMode && wouldExceedSingleLimit(field.token))
                      }
                      onClick={() => insertAtCursor(field.token, field.label)}
                    >
                      {field.label}
                    </Button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {isSingleMode ? (
        <div className="space-y-2">
          <div className="flex items-center justify-between text-xs">
            <span
              className={cn(
                'text-muted-foreground',
                charInfo.isNearLimit && 'text-amber-500',
                charInfo.isOverLimit && 'text-vibrant-red',
              )}
            >
              <strong className="font-semibold">{charInfo.chars}</strong> /{' '}
              {charInfo.maxChars} characters
              {charInfo.isOverLimit && (
                <span className="ml-2 inline-flex items-center gap-1 text-vibrant-red">
                  <AlertCircle className="size-3" />
                  Over limit
                </span>
              )}
            </span>
            {!charInfo.isOverLimit && (
              <span className="text-muted-foreground">
                {charInfo.remaining} remaining
              </span>
            )}
          </div>
          <Progress
            value={charInfo.progressPercent}
            className={cn('h-2', progressBarClass)}
          />
        </div>
      ) : (
        <div className="flex items-center justify-between rounded-md border border-card-border px-3 py-2 text-xs text-muted-foreground">
          <span>
            Characters:{' '}
            <strong className="text-primary">{segmentInfo.chars}</strong>
          </span>
          <span>
            Segments:{' '}
            <strong className="text-primary">{segmentInfo.segments}</strong>
          </span>
        </div>
      )}

      {isSingleMode && (
        <p className="text-xs text-muted-foreground">
          Click a field to insert at cursor. The phishing link URL counts toward
          the 160-character limit (not just the placeholder). Insert Fields are
          disabled once the limit is reached.
        </p>
      )}

      {!isSingleMode && (
        <p className="text-xs text-muted-foreground">
          Click a field to insert at cursor. SMS templates must include{' '}
          {'{{PHISHING_LINK}}'}.
        </p>
      )}

      {isSingleMode && (error || charInfo.isOverLimit) && (
        <p className="text-sm text-vibrant-red">
          {error ?? 'SMS message cannot exceed 160 characters'}
        </p>
      )}

      {!isSingleMode && error && (
        <p className="text-sm text-vibrant-red">{error}</p>
      )}
    </div>
  );
};

export default SmsTemplateEditor;
