import { AlertCircle, CheckCircle, Clock } from 'lucide-react';
import { ISenderProfile, getVerificationStatus } from 'models/SenderProfile';
import React from 'react';

interface VerificationStatusIconProps {
  profile: ISenderProfile;
  showLabel?: boolean;
  className?: string;
}

/**
 * Icon component for displaying verification status
 * Based on Task-06 Sender Profile Management
 */
export const VerificationStatusIcon: React.FC<VerificationStatusIconProps> = ({
  profile,
  showLabel = false,
  className = '',
}) => {
  const status = getVerificationStatus(profile);
  return (
    <div className={`flex items-center gap-1 ${className}`}>
      {status.icon === 'success' && (
        <CheckCircle className={`size-5 ${status.color}`} />
      )}
      {status.icon === 'failed' && (
        <AlertCircle className={`size-5 ${status.color}`} />
      )}
      {status.icon === 'pending' && (
        <Clock className={`size-5 ${status.color}`} />
      )}
      {showLabel && (
        <span className={`text-sm ${status.color}`}>{status.label}</span>
      )}
    </div>
  );
};

export default VerificationStatusIcon;
