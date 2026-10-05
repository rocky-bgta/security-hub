import { InfoIcon } from 'lucide-react';
import React from 'react';
import { useNavigate } from 'react-router-dom';
import { routes } from 'routes/Routes';

interface DomainWarningProps {
  email: string;
  onDismiss?: () => void;
}

/**
 * Warning banner for unverified domains
 * Based on Task-06 Sender Profile Management (AC-14)
 */
export const DomainWarning: React.FC<DomainWarningProps> = ({
  email,
  onDismiss,
}) => {
  const navigate = useNavigate();
  const domain = email.split('@')[1] || '';

  return (
    <div className="rounded-lg border border-yellow-200 bg-yellow-50 p-4">
      <div className="flex items-start">
        <div className="shrink-0">
          <InfoIcon className="size-5 text-yellow-600" />
        </div>
        <div className="ml-3 flex-1">
          <h3 className="text-sm font-medium text-yellow-800">
            Domain Not Verified
          </h3>
          <div className="mt-1 text-sm text-yellow-700">
            <p>
              The domain <strong>{domain}</strong> is not verified. Emails may
              be rejected or marked as spam. Consider verifying this domain
              before using it in campaigns.
            </p>
          </div>
          <div className="mt-3 flex gap-3">
            <button
              type="button"
              onClick={() => navigate(routes.phishingDomainManagement.path)}
              className="text-sm font-medium text-yellow-800 underline hover:text-yellow-900"
            >
              Verify Domain
            </button>
            {onDismiss && (
              <button
                type="button"
                onClick={onDismiss}
                className="text-sm text-yellow-700 hover:text-yellow-800"
              >
                Dismiss
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DomainWarning;
