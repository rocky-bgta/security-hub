import { Button } from 'common/Button';
import { Card, CardContent } from 'common/Card';
import { AlertCircle, CheckCircle } from 'lucide-react';

interface IProps {
  message: string;
  totalValid: number;
  totalInvalid: number;
  onCancel: () => void;
  onReview: () => void;
}

const ValidationSummaryStep = ({
  message,
  totalValid,
  totalInvalid,
  onCancel,
  onReview,
}: IProps) => {
  return (
    <Card>
      <CardContent className="space-y-6 p-6">
        <div className="flex items-start gap-3 rounded-lg border border-green-500/30 bg-green-500/10 p-4">
          <CheckCircle className="mt-0.5 size-6 shrink-0 text-green-500" />
          <div>
            <h3 className="font-semibold text-green-400">{message}</h3>
            <p className="mt-1 text-sm text-muted-foreground">
              We have checked your file and found the following results.
            </p>
            <p className="text-sm text-muted-foreground">
              Please review the results before proceeding.
            </p>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <div className="rounded-lg border border-card-border p-6 text-center">
            <CheckCircle className="mx-auto mb-3 size-10 text-green-500" />
            <p className="text-sm text-muted-foreground">Valid Users</p>
            <p className="mt-1 text-4xl font-bold text-green-500">
              {totalValid}
            </p>
          </div>
          <div className="rounded-lg border border-card-border p-6 text-center">
            <AlertCircle className="mx-auto mb-3 size-10 text-red-500" />
            <p className="text-sm text-muted-foreground">Invalid Users</p>
            <p className="mt-1 text-4xl font-bold text-red-500">
              {totalInvalid}
            </p>
          </div>
        </div>

        <p className="text-sm text-muted-foreground">
          Valid users will be onboarded. Please review and fix invalid users
          before proceeding.
        </p>

        <div className="flex justify-end space-x-4 pt-2">
          <Button variant="outline" onClick={onCancel}>
            Cancel
          </Button>
          <Button onClick={onReview}>Review Users</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default ValidationSummaryStep;
