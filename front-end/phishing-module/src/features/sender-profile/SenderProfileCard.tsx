import { Button } from 'common/Button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from 'common/Dropdown';
import { MoreHorizontal } from 'lucide-react';
import { ISenderProfile, ProfileType } from 'models/SenderProfile';
import React from 'react';
import ProfileTypeBadge from './ProfileTypeBadge';
import VerificationStatusIcon from './VerificationStatusIcon';
import { Card } from 'common/Card';

interface SenderProfileCardProps {
  profile: ISenderProfile;
  onEdit: (profile: ISenderProfile) => void;
  onDuplicate: (profile: ISenderProfile) => void;
  onDelete: (profile: ISenderProfile) => void;
  onTest: (profile: ISenderProfile) => void;
}

/**
 * Card component for displaying sender profile in grid view
 * Based on Task-06 Sender Profile Management (AC-01)
 */
export const SenderProfileCard: React.FC<SenderProfileCardProps> = ({
  profile,
  onEdit,
  onDuplicate,
  onDelete,
  onTest,
}) => {
  const actions = [
    { label: 'Test Connection', onClick: () => onTest(profile), icon: 'test' },
    ...(profile.canEdit
      ? [{ label: 'Edit', onClick: () => onEdit(profile), icon: 'edit' }]
      : []),
    {
      label: 'Duplicate',
      onClick: () => onDuplicate(profile),
      icon: 'duplicate',
    },
    {
      label: 'Delete',
      onClick: () => onDelete(profile),
      icon: 'delete',
      danger: true,
    },
  ];

  return (
    <Card>
      {/* Header */}
      <div className="border-b border-card-border p-4">
        <div className="flex items-start justify-between">
          <div className="min-w-0 flex-1">
            <div className="mb-1 flex items-center gap-2">
              <h3 className="truncate text-base font-medium text-card-foreground">
                {profile.profileName}
              </h3>
              <VerificationStatusIcon profile={profile} />
            </div>
            {profile.displayName && (
              <p className="truncate text-sm text-card-foreground/50">
                {profile.displayName}
              </p>
            )}
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="size-8">
                <MoreHorizontal className="size-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {actions.map(action => (
                <DropdownMenuItem
                  key={action.label}
                  onSelect={() => action.onClick()}
                >
                  {action.label}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Body */}
      <div className="space-y-3 p-4">
        {/* From Address */}
        <div className="flex items-center gap-2">
          <svg
            className="size-4 text-card-foreground/50"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
            />
          </svg>
          <span className="truncate text-sm text-card-foreground">
            {profile.fromAddress}
          </span>
        </div>

        {/* Host:Port */}
        <div className="flex items-center gap-2">
          <svg
            className="size-4 text-card-foreground/50"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M5 12h14M5 12a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v4a2 2 0 01-2 2M5 12a2 2 0 00-2 2v4a2 2 0 002 2h14a2 2 0 002-2v-4a2 2 0 00-2-2"
            />
          </svg>
          <span className="text-sm text-card-foreground">
            {profile.host}:{profile.port}
          </span>
          {profile.useTls && (
            <span className="text-xs font-medium text-primary">(TLS)</span>
          )}
        </div>

        {/* Last tested */}
        {profile.lastTestedAt && (
          <div className="flex items-center gap-2">
            <svg
              className="size-4 text-card-foreground/50"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <span className="text-xs text-card-foreground/50">
              Last tested: {new Date(profile.lastTestedAt).toLocaleDateString()}
            </span>
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="rounded-b-lg border-t border-card-border px-4 py-3">
        <div className="flex items-center justify-between">
          <ProfileTypeBadge type={profile.profileType} />
          {profile.profileType === ProfileType.MANAGED && (
            <span className="text-xs text-card-foreground/50">Read-only</span>
          )}
        </div>
      </div>
    </Card>
  );
};

export default SenderProfileCard;
