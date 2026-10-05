import { Card, CardContent } from 'common/Card';
import { Progress } from 'common/Progress';
import { useEffect, useState } from 'react';

interface IProps {
  totalUsers: number;
}

const OnboardingProgressStep = ({ totalUsers }: IProps) => {
  const [progress, setProgress] = useState(8);

  useEffect(() => {
    const interval = window.setInterval(() => {
      setProgress(prev => {
        if (prev >= 90) return prev;
        return Math.min(90, prev + Math.max(1, Math.round((90 - prev) * 0.08)));
      });
    }, 400);

    return () => window.clearInterval(interval);
  }, []);

  return (
    <Card>
      <CardContent className="space-y-8 p-6">
        <div className="flex flex-col items-center space-y-4 py-6">
          <p className="text-5xl font-semibold text-primary">{progress}%</p>
          <Progress value={progress} className="h-3 w-full max-w-xl" />
          <p className="text-lg font-medium text-foreground">
            Onboarding in progress...
          </p>
          <p className="text-sm text-muted-foreground">
            Please do not leave this page.
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-lg border border-card-border p-4 text-center">
            <p className="text-sm text-muted-foreground">Total Users</p>
            <p className="mt-1 text-3xl font-bold text-foreground">
              {totalUsers}
            </p>
          </div>
          <div className="rounded-lg border border-card-border p-4 text-center">
            <p className="text-sm text-muted-foreground">Successful</p>
            <p className="mt-1 text-3xl font-bold text-green-500">—</p>
          </div>
          <div className="rounded-lg border border-card-border p-4 text-center">
            <p className="text-sm text-muted-foreground">Failed</p>
            <p className="mt-1 text-3xl font-bold text-red-500">—</p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default OnboardingProgressStep;
