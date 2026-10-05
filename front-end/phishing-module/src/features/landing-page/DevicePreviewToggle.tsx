import { DEVICE_SIZES, DeviceType } from 'models/LandingPage';
import React from 'react';

interface DevicePreviewToggleProps {
  selectedDevice: DeviceType;
  onDeviceChange: (device: DeviceType) => void;
}

/**
 * Toggle component for switching between device preview sizes
 * Based on Task-04 Landing Page Library
 */
export const DevicePreviewToggle: React.FC<DevicePreviewToggleProps> = ({
  selectedDevice,
  onDeviceChange,
}) => {
  const devices: DeviceType[] = ['desktop', 'tablet', 'mobile'];

  const getIcon = (device: DeviceType) => {
    switch (device) {
      case 'desktop':
        return (
          <svg
            className="size-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
            />
          </svg>
        );
      case 'tablet':
        return (
          <svg
            className="size-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 18h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z"
            />
          </svg>
        );
      case 'mobile':
        return (
          <svg
            className="size-5"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"
            />
          </svg>
        );
    }
  };

  return (
    <div className="flex items-center rounded-lg bg-gray-100 p-1">
      {devices.map(device => (
        <button
          key={device}
          onClick={() => onDeviceChange(device)}
          className={`flex items-center justify-center rounded-md px-3 py-2 transition-colors ${
            selectedDevice === device
              ? 'bg-white text-blue-600 shadow-sm'
              : 'text-gray-500 hover:text-gray-700'
          }`}
          title={DEVICE_SIZES[device].label}
        >
          {getIcon(device)}
          <span className="ml-2 hidden text-sm font-medium sm:inline">
            {DEVICE_SIZES[device].label}
          </span>
        </button>
      ))}
    </div>
  );
};

export default DevicePreviewToggle;
