import { CheckCircle, Send } from 'lucide-react';

import { Alert, AlertDescription } from 'common/Alert';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
interface IProps {
  emailSent?: boolean;
  onSubmit?: () => void;
  onPrevious: () => void;
  submitting?: boolean;
}

const Step4 = ({ emailSent, onSubmit, onPrevious, submitting }: IProps) => {
  const handleSendEmail = () => {
    onSubmit?.();
  };

  return (
    <Card className="content-w-full">
      <CardHeader>
        <CardTitle className="content-text-2xl content-font-bold">
          Confirmation Email
        </CardTitle>
        <p className="content-text-gray-400">
          Send confirmation email with login credentials to client
        </p>
      </CardHeader>
      <CardContent className="content-space-y-6">
        {emailSent && (
          <Alert className="content-border-green-400 content-bg-transparent">
            <CheckCircle className="content-size-4 !content-text-green-400" />
            <AlertDescription className="content-text-green-400">
              <strong>Email sent successfully.</strong>
              <br />
              Payment successful.
            </AlertDescription>
          </Alert>
        )}

        <div className="content-flex content-justify-between">
          <Button
            variant="outline"
            onClick={onPrevious}
            className="content-px-8 content-py-2"
            disabled={submitting}
          >
            Previous
          </Button>
          <Button onClick={handleSendEmail} disabled={submitting || emailSent}>
            {submitting
              ? 'Sending...'
              : emailSent
                ? 'Email Sent'
                : 'Submit Email'}
            <Send className="content-ml-2 content-size-4" />
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step4;
