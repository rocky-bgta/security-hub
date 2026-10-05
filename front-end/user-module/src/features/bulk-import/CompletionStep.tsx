import { Button } from 'common/Button';
import { Card, CardContent } from 'common/Card';
import { groupFailedUsersByReason } from 'features/bulk-import/utils';
import { CheckCircle } from 'lucide-react';
import { IBulkImportOnboardResult } from 'models/BulkImport';

interface IProps {
  result: IBulkImportOnboardResult;
  onDone: () => void;
}

const CompletionStep = ({ result, onDone }: IProps) => {
  const failureGroups = groupFailedUsersByReason(result.failedUsers);

  return (
    <Card>
      <CardContent className="space-y-8 p-6">
        <div className="flex flex-col items-center space-y-2 py-4">
          <CheckCircle className="size-14 text-green-500" />
          <p className="text-xl font-semibold text-green-500">Completed</p>
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg border border-card-border p-4 text-center">
            <p className="text-sm text-muted-foreground">Total</p>
            <p className="mt-1 text-3xl font-bold text-foreground">
              {result.totalUsers}
            </p>
          </div>
          <div className="rounded-lg border border-card-border p-4 text-center">
            <p className="text-sm text-muted-foreground">
              Successfully Onboarded
            </p>
            <p className="mt-1 text-3xl font-bold text-green-500">
              {result.successful}
            </p>
          </div>
          <div className="rounded-lg border border-card-border p-4 text-center">
            <p className="text-sm text-muted-foreground">Failed</p>
            <p className="mt-1 text-3xl font-bold text-red-500">
              {result.failed}
            </p>
          </div>
        </div>

        {failureGroups.length > 0 && (
          <div className="space-y-3">
            <h3 className="font-semibold text-foreground">Failure Reasons</h3>
            <ul className="space-y-2 text-sm text-muted-foreground">
              {failureGroups.map(group => (
                <li key={group.label}>
                  {group.count} {group.count === 1 ? 'User' : 'Users'} —{' '}
                  {group.label}
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="flex justify-end">
          <Button onClick={onDone}>Done</Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default CompletionStep;
