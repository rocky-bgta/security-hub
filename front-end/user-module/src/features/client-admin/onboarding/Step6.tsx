import { CheckCircle, Send } from 'lucide-react';

import { Alert, AlertDescription } from 'common/Alert';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';

interface IProps {
  emailSent?: boolean;
  submitting?: boolean;
  onSubmit?: () => void;
  onPrevious: () => void;
}

const Step6 = ({ emailSent, submitting, onSubmit, onPrevious }: IProps) => {
  const handleSendEmail = () => {
    onSubmit?.();
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl font-bold">Confirmation Email</CardTitle>
        <p className="text-gray-400">
          Send confirmation email with login credentials to client
        </p>
      </CardHeader>
      <CardContent className="space-y-6">
        {emailSent && (
          <Alert className="border-green-400 bg-transparent">
            <CheckCircle className="size-4 !text-green-400" />
            <AlertDescription className="text-green-400">
              <strong>
                Email sent successfully to the Client Admin with login
                credentials.
              </strong>
              <br />
              Payment successful. Please check your email for login credentials.
            </AlertDescription>
          </Alert>
        )}

        <div className="flex justify-between">
          <Button
            variant="outline"
            onClick={onPrevious}
            className="px-8 py-2"
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
            <Send className="ml-2 size-4" />
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step6;
