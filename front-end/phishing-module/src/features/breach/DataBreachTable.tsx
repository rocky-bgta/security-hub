import {
  BreachStatus,
  getStatusColor,
  getStatusLabel,
  IBreachRecord,
} from 'models/Breach';
import React from 'react';
import SeverityBadge from './SeverityBadge';

interface DataBreachTableProps {
  breaches: IBreachRecord[];
  loading: boolean;
  onRowClick: (breach: IBreachRecord) => void;
  onStatusChange: (id: string, status: BreachStatus) => void;
}

/**
 * Table component for displaying data breaches
 */
const DataBreachTable: React.FC<DataBreachTableProps> = ({
  breaches,
  loading,
  onRowClick,
  onStatusChange,
}) => {
  const formatDate = (dateString: string) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
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

  if (breaches.length === 0) {
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
            d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
          />
        </svg>
        <p className="mt-2 text-sm">No data breaches found</p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Domain
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Breach Name
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Date
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Compromised Data
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Recipients
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Severity
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">
              Status
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 bg-white">
          {breaches.map(breach => (
            <tr
              key={breach.id}
              onClick={() => onRowClick(breach)}
              className="hover: cursor-pointer transition-colors"
            >
              <td className="whitespace-nowrap px-6 py-4">
                <div className="text-sm font-medium text-gray-900">
                  {breach.domain}
                </div>
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <div className="text-sm text-gray-900">
                  {breach.breachName || '-'}
                </div>
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <div className="text-sm text-gray-500">
                  {formatDate(breach.dateOfBreach)}
                </div>
              </td>
              <td className="px-6 py-4">
                <div className="flex max-w-xs flex-wrap gap-1">
                  {breach.compromisedDataTypes
                    ?.slice(0, 3)
                    .map((type, index) => (
                      <span
                        key={index}
                        className="inline-flex items-center rounded bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-800"
                      >
                        {type}
                      </span>
                    ))}
                  {breach.compromisedDataTypes?.length > 3 && (
                    <span className="inline-flex items-center rounded bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-500">
                      +{breach.compromisedDataTypes.length - 3}
                    </span>
                  )}
                </div>
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <div className="text-sm text-gray-900">
                  {breach.recipientCount}
                </div>
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <SeverityBadge severity={breach.severity} size="sm" />
              </td>
              <td className="whitespace-nowrap px-6 py-4">
                <select
                  value={breach.status}
                  onChange={e => {
                    e.stopPropagation();
                    onStatusChange(breach.id, e.target.value as BreachStatus);
                  }}
                  onClick={e => e.stopPropagation()}
                  className="rounded-md border-gray-300 text-sm focus:border-blue-500 focus:ring-blue-500"
                  style={{ color: getStatusColor(breach.status) }}
                >
                  {Object.values(BreachStatus).map(status => (
                    <option key={status} value={status}>
                      {getStatusLabel(status)}
                    </option>
                  ))}
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default DataBreachTable;
