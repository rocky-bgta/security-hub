import { CheckCircle2, Circle, Info, XCircle } from 'lucide-react';

import { Badge } from 'common/Badge';
import {
  CheckStatus,
  IFramework,
  IFrameworkEvaluation,
} from '../types';
import RequirementRow from './RequirementRow';
import { cn } from 'utils/Helper';

interface IProps {
  framework: IFramework;
  evaluation: IFrameworkEvaluation;
  hasPassword: boolean;
}

const statusBadge = (hasPassword: boolean, isCompliant: boolean) => {
  if (!hasPassword) {
    return (
      <Badge
        variant="outline"
        className="content-border-steel-gray content-bg-steel-gray/40 content-text-muted-foreground"
      >
        <Circle className="content-mr-1 content-size-3" aria-hidden />
        Not checked
      </Badge>
    );
  }

  if (isCompliant) {
    return (
      <Badge className="content-border-green-500/40 content-bg-green-500/15 content-text-green-400 hover:content-bg-green-500/20">
        <CheckCircle2 className="content-mr-1 content-size-3" aria-hidden />
        Compliant
      </Badge>
    );
  }

  return (
    <Badge
      variant="destructive"
      className="content-border-red-500/40 content-bg-red-500/15 content-text-red-400 hover:content-bg-red-500/20"
    >
      <XCircle className="content-mr-1 content-size-3" aria-hidden />
      Not compliant
    </Badge>
  );
};

const visibleStatus = (
  required: boolean,
  status: CheckStatus,
): CheckStatus | null => {
  if (!required) return null;
  return status === 'na' ? 'unchecked' : status;
};

const FrameworkCard = ({ framework, evaluation, hasPassword }: IProps) => {
  const { checks } = evaluation;

  const rows: Array<{ key: string; label: string; status: CheckStatus }> = [
    {
      key: 'length',
      label: `Minimum length: ${framework.minLength} characters`,
      status: hasPassword ? checks.length : 'unchecked',
    },
  ];

  const lowercase = visibleStatus(
    framework.requiresLowercase,
    hasPassword ? checks.lowercase : 'unchecked',
  );
  if (lowercase) {
    rows.push({
      key: 'lowercase',
      label: 'At least 1 lowercase letter',
      status: lowercase,
    });
  }

  const uppercase = visibleStatus(
    framework.requiresUppercase,
    hasPassword ? checks.uppercase : 'unchecked',
  );
  if (uppercase) {
    rows.push({
      key: 'uppercase',
      label: 'At least 1 uppercase letter',
      status: uppercase,
    });
  }

  const number = visibleStatus(
    framework.requiresNumber,
    hasPassword ? checks.number : 'unchecked',
  );
  if (number) {
    rows.push({
      key: 'number',
      label: 'At least 1 number',
      status: number,
    });
  }

  const special = visibleStatus(
    framework.requiresSpecial,
    hasPassword ? checks.special : 'unchecked',
  );
  if (special) {
    rows.push({
      key: 'special',
      label: 'At least 1 special character',
      status: special,
    });
  }

  const mfa = visibleStatus(
    framework.requiresMFA,
    hasPassword ? checks.mfa : 'unchecked',
  );
  if (mfa) {
    rows.push({ key: 'mfa', label: 'MFA required', status: mfa });
  }

  if (framework.maxChangeDays) {
    rows.push({
      key: 'change',
      label: `Password change every ${framework.maxChangeDays} days`,
      status: hasPassword ? checks.change : 'unchecked',
    });
  }

  return (
    <article
      className={cn(
        'content-flex content-h-full content-flex-col content-rounded-lg content-border content-border-white/10 content-bg-card-background/70 content-p-3 content-transition-colors',
        hasPassword &&
          evaluation.isCompliant &&
          'content-border-green-500/30',
        hasPassword &&
          !evaluation.isCompliant &&
          'content-border-red-500/20',
      )}
    >
      <div className="content-mb-2 content-flex content-items-start content-justify-between content-gap-2">
        <h3 className="content-text-sm content-font-semibold content-text-white md:content-text-base">
          {framework.name}
        </h3>
        {statusBadge(hasPassword, evaluation.isCompliant)}
      </div>

      <ul className="content-flex content-flex-grow content-flex-col content-gap-1.5">
        {rows.map(row => (
          <RequirementRow key={row.key} label={row.label} status={row.status} />
        ))}
        <li className="content-mt-2 content-flex content-items-start content-gap-1.5 content-text-xs content-text-muted-foreground">
          <Info className="content-mt-0.5 content-size-3 content-shrink-0" aria-hidden />
          <span>{framework.additional}</span>
        </li>
      </ul>
    </article>
  );
};

export default FrameworkCard;
