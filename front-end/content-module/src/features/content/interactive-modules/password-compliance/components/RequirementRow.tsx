import { CheckCircle2, Circle, XCircle } from 'lucide-react';

import { CheckStatus } from '../types';
import { cn } from 'utils/Helper';

interface IProps {
  label: string;
  status: CheckStatus;
}

const RequirementRow = ({ label, status }: IProps) => {
  const icon =
    status === 'pass' ? (
      <CheckCircle2
        className="content-size-3.5 content-shrink-0 content-text-green-500"
        aria-hidden
      />
    ) : status === 'fail' ? (
      <XCircle
        className="content-size-3.5 content-shrink-0 content-text-red-500"
        aria-hidden
      />
    ) : (
      <Circle
        className="content-size-3.5 content-shrink-0 content-text-muted-foreground"
        aria-hidden
      />
    );

  return (
    <li
      className={cn(
        'content-flex content-items-center content-gap-2 content-text-sm content-transition-colors',
        status === 'pass' && 'content-text-green-500',
        status === 'fail' && 'content-text-red-500',
        (status === 'unchecked' || status === 'na') &&
          'content-text-muted-foreground',
      )}
    >
      {icon}
      <span>{label}</span>
      <span className="content-sr-only">
        {status === 'pass'
          ? 'passed'
          : status === 'fail'
            ? 'failed'
            : 'not checked'}
      </span>
    </li>
  );
};

export default RequirementRow;
