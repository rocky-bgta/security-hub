import { CheckCircle2, Clock, AlertCircle } from 'lucide-react';

interface IProps {
  action: string;
  className?: string;
}

const ActionStatusIcon = ({ action, className = '' }: IProps) => {
  const normalizedAction = action.toLowerCase();

  if (
    normalizedAction.includes('confirmed') ||
    normalizedAction.includes('approved')
  ) {
    return <CheckCircle2 className={`size-4 text-primary ${className}`} />;
  }

  if (
    normalizedAction.includes('pending') ||
    normalizedAction.includes('review')
  ) {
    return <Clock className={`size-4 text-muted-foreground ${className}`} />;
  }

  if (normalizedAction.includes('rejected')) {
    return <AlertCircle className={`size-4 text-destructive ${className}`} />;
  }

  return <Clock className={`size-4 text-primary ${className}`} />;
};

export default ActionStatusIcon;
