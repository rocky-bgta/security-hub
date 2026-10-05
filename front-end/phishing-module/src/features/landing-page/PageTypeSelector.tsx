import { LandingPageType, PAGE_TYPE_INFO } from 'models/LandingPage';
import React from 'react';

interface PageTypeSelectorProps {
  selectedType: LandingPageType;
  onTypeChange: (type: LandingPageType) => void;
  disabled?: boolean;
}

/**
 * Page type selector component with 3 selectable cards
 * Based on Task-05 Landing Page Creation (AC-01)
 */
export const PageTypeSelector: React.FC<PageTypeSelectorProps> = ({
  selectedType,
  onTypeChange,
  disabled = false,
}) => {
  const pageTypes = Object.values(LandingPageType);

  const getIcon = (type: LandingPageType) => {
    switch (PAGE_TYPE_INFO[type].icon) {
      case 'document':
        return (
          <svg
            className="size-8"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
            />
          </svg>
        );
      case 'exclamation':
        return (
          <svg
            className="size-8"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        );
      case 'code':
        return (
          <svg
            className="size-8"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
            />
          </svg>
        );
      default:
        return null;
    }
  };

  return (
    <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
      {pageTypes.map(type => {
        const info = PAGE_TYPE_INFO[type];
        const isSelected = selectedType === type;

        return (
          <button
            key={type}
            type="button"
            onClick={() => !disabled && onTypeChange(type)}
            disabled={disabled}
            className={`relative flex flex-col items-center rounded-lg border-2 p-6 text-left transition-all ${
              isSelected
                ? 'border-primary bg-primary/10 shadow-md'
                : 'hover: border-card-border bg-card-background hover:border-card-border'
            } ${disabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'}`}
          >
            {/* Selection indicator */}
            {isSelected && (
              <div className="absolute right-2 top-2">
                <svg
                  className="size-6 text-primary"
                  fill="currentColor"
                  viewBox="0 0 20 20"
                >
                  <path
                    fillRule="evenodd"
                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                    clipRule="evenodd"
                  />
                </svg>
              </div>
            )}

            {/* Icon */}
            <div
              className={`mb-4 ${isSelected ? 'text-primary' : 'text-muted-foreground'}`}
            >
              {getIcon(type)}
            </div>

            {/* Title */}
            <h3
              className={`mb-2 text-lg font-semibold ${isSelected ? 'text-primary' : 'text-muted-foreground'}`}
            >
              {info.title}
            </h3>

            {/* Description */}
            <p className="text-center text-sm text-muted-foreground">
              {info.description}
            </p>
          </button>
        );
      })}
    </div>
  );
};

export default PageTypeSelector;
