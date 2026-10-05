import { IRecipientBreach } from 'models/Breach';
import { useState } from 'react';

interface RecipientBreachActionsProps {
  recipient: IRecipientBreach;
  onNotify: (id: string, notes?: string) => void;
  onResetPassword: (id: string, notes?: string) => void;
  onResolve: (id: string, notes?: string) => void;
  actionLoading?: boolean;
}

/**
 * Action buttons for recipient breaches
 */
const RecipientBreachActions: React.FC<RecipientBreachActionsProps> = ({
  recipient,
  onNotify,
  onResetPassword,
  onResolve,
  actionLoading = false,
}) => {
  const [showConfirm, setShowConfirm] = useState<string | null>(null);
  const [notes, setNotes] = useState('');

  const handleAction = (action: string) => {
    switch (action) {
      case 'notify':
        onNotify(recipient.id, notes);
        break;
      case 'reset':
        onResetPassword(recipient.id, notes);
        break;
      case 'resolve':
        onResolve(recipient.id, notes);
        break;
    }
    setShowConfirm(null);
    setNotes('');
  };

  const renderConfirmDialog = () => {
    if (!showConfirm) return null;

    const titles: Record<string, string> = {
      notify: 'Notify User',
      reset: 'Reset Password',
      resolve: 'Mark as Resolved',
    };

    const descriptions: Record<string, string> = {
      notify:
        'This will send a notification to the user about the data breach.',
      reset: 'This will trigger a password reset for the user.',
      resolve: 'This will mark the breach as resolved for this user.',
    };

    return (
      <div className="fixed inset-0 z-50 overflow-y-auto">
        <div className="flex min-h-screen items-center justify-center px-4">
          <div
            className="0 fixed inset-0 bg-opacity-75"
            onClick={() => setShowConfirm(null)}
          />
          <div className="relative w-full max-w-md rounded-lg bg-white p-6 shadow-xl">
            <h3 className="mb-2 text-lg font-semibold text-gray-900">
              {titles[showConfirm]}
            </h3>
            <p className="mb-4 text-sm text-gray-600">
              {descriptions[showConfirm]}
            </p>

            <div className="mb-4">
              <label className="mb-1 block text-sm font-medium text-gray-700">
                Notes (optional)
              </label>
              <textarea
                value={notes}
                onChange={e => setNotes(e.target.value)}
                placeholder="Add any notes..."
                rows={3}
                className="w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
              />
            </div>

            <div className="flex justify-end gap-3">
              <button
                onClick={() => setShowConfirm(null)}
                className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200"
              >
                Cancel
              </button>
              <button
                onClick={() => handleAction(showConfirm)}
                disabled={actionLoading}
                className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
              >
                {actionLoading ? 'Processing...' : 'Confirm'}
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <>
      <div className="flex items-center gap-2">
        {recipient.canNotify && (
          <button
            onClick={() => setShowConfirm('notify')}
            disabled={actionLoading}
            className="inline-flex items-center rounded bg-blue-50 px-2.5 py-1.5 text-xs font-medium text-blue-700 hover:bg-blue-100 disabled:opacity-50"
            title="Notify User"
          >
            <svg
              className="mr-1 size-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
              />
            </svg>
            Notify
          </button>
        )}

        {recipient.canResetPassword && (
          <button
            onClick={() => setShowConfirm('reset')}
            disabled={actionLoading}
            className="inline-flex items-center rounded bg-amber-50 px-2.5 py-1.5 text-xs font-medium text-amber-700 hover:bg-amber-100 disabled:opacity-50"
            title="Reset Password"
          >
            <svg
              className="mr-1 size-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
              />
            </svg>
            Reset
          </button>
        )}

        {recipient.canResolve && (
          <button
            onClick={() => setShowConfirm('resolve')}
            disabled={actionLoading}
            className="inline-flex items-center rounded bg-green-50 px-2.5 py-1.5 text-xs font-medium text-green-700 hover:bg-green-100 disabled:opacity-50"
            title="Mark as Resolved"
          >
            <svg
              className="mr-1 size-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M5 13l4 4L19 7"
              />
            </svg>
            Resolve
          </button>
        )}
      </div>

      {renderConfirmDialog()}
    </>
  );
};

export default RecipientBreachActions;
