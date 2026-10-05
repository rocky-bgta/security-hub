import {
  BreachStatus,
  getCompromisedDataIcon,
  getStatusColor,
  getStatusLabel,
  IBreachRecord,
} from 'models/Breach';
import SeverityBadge from './SeverityBadge';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';

interface BreachDetailsModalProps {
  breach: IBreachRecord | null;
  isOpen: boolean;
  onClose: () => void;
  onStatusChange: (id: string, status: BreachStatus) => void;
  onViewRecipients: (breachId: string) => void;
  actionLoading?: boolean;
}

/**
 * Modal for viewing breach details
 */
const BreachDetailsModal = ({
  breach,
  isOpen,
  onClose,
  onStatusChange,
  onViewRecipients,
  actionLoading = false,
}: BreachDetailsModalProps) => {
  if (!isOpen || !breach) return null;

  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Breach Details</DialogTitle>
        </DialogHeader>

        <div className="space-y-6 px-6 py-4">
          {/* Basic Info */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-500">
                Breach Name
              </label>
              <p className="mt-1 text-sm text-gray-900">
                {breach.breachName || '-'}
              </p>
              <div>
                <label className="block text-sm font-medium text-gray-500">
                  Date of Breach
                </label>
                <p className="mt-1 text-sm text-gray-900">
                  {formatDate(breach.dateOfBreach)}
                </p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500">
                  Severity
                </label>
                <div className="mt-1">
                  <SeverityBadge severity={breach.severity} />
                </div>
              </div>
            </div>
          </div>

          {/* Content */}
          <div className="space-y-6 px-6 py-4">
            {/* Basic Info */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-500">
                  Breach Name
                </label>
                <p className="mt-1 text-sm text-gray-900">
                  {breach.breachName || '-'}
                </p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500">
                  Date of Breach
                </label>
                <p className="mt-1 text-sm text-gray-900">
                  {formatDate(breach.dateOfBreach)}
                </p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500">
                  Severity
                </label>
                <div className="mt-1">
                  <SeverityBadge severity={breach.severity} />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500">
                  Status
                </label>
                <div className="mt-1">
                  <select
                    value={breach.status}
                    onChange={e =>
                      onStatusChange(breach.id, e.target.value as BreachStatus)
                    }
                    disabled={actionLoading}
                    className="rounded-md border-gray-300 text-sm focus:border-blue-500 focus:ring-blue-500"
                    style={{ color: getStatusColor(breach.status) }}
                  >
                    {Object.values(BreachStatus).map(status => (
                      <option key={status} value={status}>
                        {getStatusLabel(status)}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            {/* Description */}
            {breach.description && (
              <div>
                <label className="block text-sm font-medium text-gray-500">
                  Description
                </label>
                <p className="mt-1 text-sm text-gray-900">
                  {breach.description}
                </p>
              </div>
            )}

            {/* Compromised Data */}
            <div>
              <label className="mb-2 block text-sm font-medium text-gray-500">
                Compromised Data Types
              </label>
              <div className="flex flex-wrap gap-2">
                {breach.compromisedDataTypes?.map((type, index) => (
                  <span
                    key={index}
                    className="inline-flex items-center rounded-lg bg-red-50 px-3 py-1.5 text-sm font-medium text-red-700"
                  >
                    <span className="mr-1.5">
                      {getCompromisedDataIcon(type)}
                    </span>
                    {type}
                  </span>
                ))}
                {(!breach.compromisedDataTypes ||
                  breach.compromisedDataTypes.length === 0) && (
                  <span className="text-sm text-gray-500">
                    No data types specified
                  </span>
                )}
              </div>
            </div>

            {/* Affected Recipients */}
            <div className="rounded-lg p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-500">
                    Affected Recipients
                  </p>
                  <p className="text-2xl font-bold text-gray-900">
                    {breach.recipientCount}
                  </p>
                </div>
                <button
                  onClick={() => onViewRecipients(breach.id)}
                  className="rounded-lg bg-blue-50 px-4 py-2 text-sm font-medium text-blue-600 transition-colors hover:bg-blue-100"
                >
                  View Recipients
                </button>
              </div>
            </div>

            {/* Source Info */}
            <div className="border-t border-gray-200 pt-4">
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span className="text-gray-500">Source:</span>{' '}
                  <span className="text-gray-900">
                    {breach.sourceApi || 'Manual Entry'}
                  </span>
                </div>
                <div>
                  <span className="text-gray-500">External ID:</span>{' '}
                  <span className="text-gray-900">
                    {breach.externalBreachId || '-'}
                  </span>
                </div>
                <div>
                  <span className="text-gray-500">Created:</span>{' '}
                  <span className="text-gray-900">
                    {formatDate(breach.createdAt)}
                  </span>
                </div>
                <div>
                  <span className="text-gray-500">Updated:</span>{' '}
                  <span className="text-gray-900">
                    {formatDate(breach.updatedAt)}
                  </span>
                </div>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="flex justify-end gap-3 border-t border-gray-200 px-6 py-4">
            <button
              onClick={onClose}
              className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 transition-colors hover:bg-gray-200"
            >
              Close
            </button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default BreachDetailsModal;
