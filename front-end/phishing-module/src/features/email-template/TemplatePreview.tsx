import { Badge } from 'common/Badge';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Eye, Mail, MessageSquare, Paperclip } from 'lucide-react';
import type { SmsDeliveryMode } from 'features/email-template/SmsTemplateEditor';
import { cn } from 'utils/Helper';
import {
  calcSmsSegments,
  expandSmsPhishingLink,
  getSmsCharInfo,
} from 'utils/smsSegments';

interface TemplatePreviewProps {
  templateName: string;
  emailSubject: string;
  emailBody: string;
  attachments?: File[];
  variant?: 'email' | 'sms';
  smsDeliveryMode?: SmsDeliveryMode;
  /** When set, {{PHISHING_LINK}} is replaced with this URL in SMS preview */
  phishingPreviewUrl?: string;
  className?: string;
}

/**
 * Real-time template preview component
 */
const TemplatePreview = ({
  templateName,
  emailSubject,
  emailBody,
  attachments = [],
  variant = 'email',
  smsDeliveryMode = 'single',
  phishingPreviewUrl,
  className,
}: TemplatePreviewProps) => {
  const hasContent = templateName || emailSubject || emailBody;
  const isSms = variant === 'sms';
  const smsPreviewBody = isSms
    ? expandSmsPhishingLink(emailBody, phishingPreviewUrl)
    : emailBody;
  const charInfo = isSms
    ? getSmsCharInfo(emailBody, phishingPreviewUrl)
    : null;
  const segmentInfo = isSms ? calcSmsSegments(smsPreviewBody) : null;
  const isMultiSms = isSms && smsDeliveryMode === 'multi';

  return (
    <Card className={cn('sticky top-4', className)}>
      <CardHeader className="pb-3">
        <CardTitle className="flex items-center gap-2 text-lg">
          <Eye className="size-5 text-primary" />
          Preview
        </CardTitle>
      </CardHeader>
      <CardContent>
        {hasContent ? (
          <div className="space-y-4">
            {templateName && (
              <div>
                <span className="text-xs text-muted-foreground">
                  Template Name:
                </span>
                <p className="font-medium text-primary">{templateName}</p>
              </div>
            )}

            {isSms ? (
              <div className="rounded-xl border border-card-border bg-muted/20 p-4">
                <div className="mb-3 flex items-center gap-2 text-xs text-muted-foreground">
                  <MessageSquare className="size-4" />
                  SMS Preview
                </div>
                {emailSubject && (
                  <p className="mb-3 text-xs font-medium text-muted-foreground">
                    {emailSubject}
                  </p>
                )}
                {smsPreviewBody ? (
                  <div className="flex justify-start">
                    <div className="max-w-[90%] rounded-2xl rounded-tl-sm bg-primary/15 px-4 py-3 text-sm text-primary">
                      <p className="whitespace-pre-wrap break-words font-mono">
                        {smsPreviewBody}
                      </p>
                    </div>
                  </div>
                ) : null}
                {smsPreviewBody && (
                  <p className="mt-3 text-xs text-muted-foreground">
                    {isMultiSms && segmentInfo ? (
                      <>
                        {segmentInfo.chars} characters · {segmentInfo.segments}{' '}
                        {segmentInfo.segments === 1 ? 'segment' : 'segments'}
                      </>
                    ) : (
                      charInfo && (
                        <>
                          {charInfo.chars} / {charInfo.maxChars} characters
                        </>
                      )
                    )}
                  </p>
                )}
              </div>
            ) : (
              <>
                {emailSubject && (
                  <div className="rounded-md p-3">
                    <div className="mb-1 flex items-center gap-2">
                      <Mail className="size-4 text-muted-foreground" />
                      <span className="text-xs text-muted-foreground">
                        Subject:
                      </span>
                    </div>
                    <p className="font-medium text-primary">{emailSubject}</p>
                  </div>
                )}

                {emailBody && (
                  <div className="max-h-[400px] overflow-auto border border-primary">
                    <iframe
                      srcDoc={emailBody}
                      style={{
                        width: '100%',
                        height: '400px',
                        border: '1px solid #ddd',
                        background: 'white',
                      }}
                      title="Email template preview"
                    />
                  </div>
                )}

                {attachments.length > 0 && (
                  <div>
                    <div className="mb-2 flex items-center gap-2">
                      <Paperclip className="size-4 text-muted-foreground" />
                      <span className="text-xs text-muted-foreground">
                        Attachments ({attachments.length})
                      </span>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {attachments.map(att => (
                        <Badge key={att.name} variant="outline">
                          {att.name}
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Eye className="mb-4 size-12 text-muted-foreground/30" />
            <p className="text-muted-foreground">
              {isSms
                ? 'Start filling in the form to see a preview of your SMS template'
                : 'Start filling in the form to see a preview of your email template'}
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default TemplatePreview;
