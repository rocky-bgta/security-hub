import {
  BreachSeverity,
  getSeverityBgColor,
  getSeverityColor,
  getSeverityLabel,
} from 'models/Breach';
import React from 'react';

interface SeverityBadgeProps {
  severity: BreachSeverity;
  size?: 'sm' | 'md' | 'lg';
}

/**
 * Color-coded severity badge component
 */
const SeverityBadge: React.FC<SeverityBadgeProps> = ({
  severity,
  size = 'md',
}) => {
  const sizeClasses = {
    sm: 'px-2 py-0.5 text-xs',
    md: 'px-2.5 py-1 text-sm',
    lg: 'px-3 py-1.5 text-base',
  };

  return (
    <span
      className={`inline-flex items-center rounded-full font-medium ${sizeClasses[size]}`}
      style={{
        backgroundColor: getSeverityBgColor(severity),
        color: getSeverityColor(severity),
      }}
    >
      <span
        className="mr-1.5 size-2 rounded-full"
        style={{ backgroundColor: getSeverityColor(severity) }}
      />
      {getSeverityLabel(severity)}
    </span>
  );
};

export default SeverityBadge;
