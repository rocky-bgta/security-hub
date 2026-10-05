import {
  IRecipientBreach,
  RecipientBreachStatus,
  getRecipientStatusColor,
  getRecipientStatusLabel,
} from 'models/Breach';
import React from 'react';
import RecipientBreachActions from './RecipientBreachActions';

interface RecipientBreachTableProps {
  recipients: IRecipientBreach[];
  loading: boolean;
  onNotify: (id: string, notes?: string) => void;
  onResetPassword: (id: string, notes?: string) => void;
  onResolve: (id: string, notes?: string) => void;
  actionLoading?: boolean;
}

/**
 * Table component for displaying recipient breaches
 */
const RecipientBreachTable: React.FC<RecipientBreachTableProps> = ({
  recipients,
  loading,
  onNotify,
  onResetPassword,
  onResolve,
  actionLoading = false,
}) => {
  const formatDate = (dateString: string | null) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (loading) {
    return (
      <div className="animate-pulse">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="mb-2 h-16 rounded bg-gray-100" />
        ))}
      </div>
    );
  }

  if (recipients.length === 0) {
    return (
      <div className="py-12 text-center text-gray-500">
        <svg
          className="mx-auto size-12 text-gray-400"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"
          />
        </svg>
        <p className="mt-2 text-sm">No affected recipients found</p>
      </div>
    );
  }

  const StatusBadge: React.FC<{ status: RecipientBreachStatus }> = ({
    status,
  }) => (
    <span
      className="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
      style={{
        backgroundColor: `${getRecipientStatusColor(status)}20`,
        color: getRecipientStatusColor(status),
      }}
    >
      {getRecipientStatusLabel(status)}
    </span>
  );

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              User
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Email
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Tags
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Breach Count
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Status
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Notified At
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Actions
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 bg-white">
          {recipients.map(recipient => (
            <tr key={recipient.id} className="hover:">
              <td className="whitespace-nowrap px-6 py-4">
                <div className="flex items-center">
                  <div className="flex size-10 shrink-0 items-center justify-center rounded-full bg-gray-200">
                    <span className="text-sm font-medium text-gray-600">
                      {recipient.firstName?.[0] ||
                        recipient.email?.[0]?.toUpperCase()}
                    </span>
                  </div>
                  <div className="ml-4">
                    <div className="text-sm font-medium text-gray-900">
                      {recipient.fullName ||
                        `${recipient.firstName} ${recipient.lastName}`}
                    </div>
                  </div>
                </div>
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <div className="text-sm text-gray-900">{recipient.email}</div>
              </td>
              <td className="px-6 py-4">
                <div className="flex max-w-xs flex-wrap gap-1">
                  {recipient.tags?.slice(0, 2).map((tag, index) => (
                    <span
                      key={index}
                      className="inline-flex items-center rounded bg-purple-100 px-2 py-0.5 text-xs font-medium text-purple-800"
                    >
                      {tag}
                    </span>
                  ))}
                  {recipient.tags?.length > 2 && (
                    <span className="inline-flex items-center rounded bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-500">
                      +{recipient.tags.length - 2}
                    </span>
                  )}
                </div>
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <div className="flex items-center">
                  <span
                    className={`text-sm font-semibold ${
                      recipient.breachCount > 1
                        ? 'text-red-600'
                        : 'text-gray-900'
                    }`}
                  >
                    {recipient.breachCount}
                  </span>
                  {recipient.breachCount > 1 && (
                    <span
                      className="ml-1 text-red-500"
                      title="Multiple breaches"
                    >
                      ⚠️
                    </span>
                  )}
                </div>
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <StatusBadge status={recipient.status} />
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <div className="text-sm text-gray-500">
                  {formatDate(recipient.notifiedAt)}
                </div>
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <RecipientBreachActions
                  recipient={recipient}
                  onNotify={onNotify}
                  onResetPassword={onResetPassword}
                  onResolve={onResolve}
                  actionLoading={actionLoading}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default RecipientBreachTable;
