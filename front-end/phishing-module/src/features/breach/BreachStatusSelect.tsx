import { BreachStatus, getStatusColor, getStatusLabel } from 'models/Breach';
import React from 'react';

interface BreachStatusSelectProps {
  value: BreachStatus;
  onChange: (status: BreachStatus) => void;
  disabled?: boolean;
}

/**
 * Dropdown for breach status selection
 */
const BreachStatusSelect: React.FC<BreachStatusSelectProps> = ({
  value,
  onChange,
  disabled = false,
}) => {
  const statuses = [
    BreachStatus.ACTION_REQUIRED,
    BreachStatus.IN_PROGRESS,
    BreachStatus.RESOLVED,
  ];

  return (
    <select
      value={value}
      onChange={e => onChange(e.target.value as BreachStatus)}
      disabled={disabled}
      className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:cursor-not-allowed disabled:bg-gray-100"
      style={{ color: getStatusColor(value) }}
    >
      {statuses.map(status => (
        <option
          key={status}
          value={status}
          style={{ color: getStatusColor(status) }}
        >
          {getStatusLabel(status)}
        </option>
      ))}
    </select>
  );
};

export default BreachStatusSelect;
