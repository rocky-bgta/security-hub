import {
  ProfileType,
  getProfileTypeColor,
  getProfileTypeLabel,
} from 'models/SenderProfile';
import React from 'react';

interface ProfileTypeBadgeProps {
  type: ProfileType;
  className?: string;
}

/**
 * Badge component for displaying profile type
 * Based on Task-06 Sender Profile Management
 */
export const ProfileTypeBadge: React.FC<ProfileTypeBadgeProps> = ({
  type,
  className = '',
}) => {
  const colorClass = getProfileTypeColor(type);
  const label = getProfileTypeLabel(type);

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${colorClass} ${className}`}
    >
      {type === ProfileType.MANAGED && (
        <svg className="mr-1 size-3" fill="currentColor" viewBox="0 0 20 20">
          <path
            fillRule="evenodd"
            d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z"
            clipRule="evenodd"
          />
        </svg>
      )}
      {label}
    </span>
  );
};

export default ProfileTypeBadge;
