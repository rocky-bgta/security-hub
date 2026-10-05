import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Eye, Mail } from 'lucide-react';

interface IProps {
  subject: string;
  html: string;
}

const isHtmlEmpty = (html: string): boolean => {
  const stripped = html.replace(/<[^>]*>/g, '').trim();
  return stripped.length === 0;
};

const NotificationTemplateEmailPreview = ({ subject, html }: IProps) => {
  const hasContent = Boolean(subject.trim() || !isHtmlEmpty(html));

  return (
    <Card className="sticky top-4">
      <CardHeader className="pb-3">
        <CardTitle className="flex items-center gap-2 text-lg">
          <Eye className="size-5 text-primary" />
          Live Preview
        </CardTitle>
      </CardHeader>
      <CardContent>
        {hasContent ? (
          <div className="space-y-4">
            {subject.trim() && (
              <div className="rounded-md border border-card-border p-3">
                <div className="mb-1 flex items-center gap-2">
                  <Mail className="size-4 text-muted-foreground" />
                  <span className="text-xs text-muted-foreground">Subject:</span>
                </div>
                <p className="font-medium text-primary">{subject}</p>
              </div>
            )}

            {!isHtmlEmpty(html) ? (
              <div className="overflow-hidden rounded-md border border-card-border">
                <iframe
                  srcDoc={html}
                  title="Email template preview"
                  className="h-[500px] w-full border-0 bg-white"
                />
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center rounded-md border border-dashed border-card-border py-12 text-center">
                <Eye className="mb-4 size-12 text-muted-foreground/30" />
                <p className="text-sm text-muted-foreground">
                  HTML content will appear here as you edit
                </p>
              </div>
            )}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <Eye className="mb-4 size-12 text-muted-foreground/30" />
            <p className="text-muted-foreground">
              Start editing to see a live preview of your email template
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default NotificationTemplateEmailPreview;
