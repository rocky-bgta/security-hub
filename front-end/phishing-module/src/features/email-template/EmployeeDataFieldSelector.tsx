import { Label } from 'common/Label';
import {
  EmployeeDataField,
  getEmployeeDataFieldLabel,
} from 'models/EmailTemplate';
import { cn } from 'utils/Helper';

interface EmployeeDataFieldSelectorProps {
  value: EmployeeDataField[];
  onChange: (fields: EmployeeDataField[]) => void;
  error?: string;
  className?: string;
}

const ALL_FIELDS = Object.values(EmployeeDataField);

/**
 * Checkbox group for selecting required employee data fields
 * Based on Task-03: At least one field must be selected
 */
const EmployeeDataFieldSelector = ({
  value,
  onChange,
  error,
  className,
}: EmployeeDataFieldSelectorProps) => {
  const handleToggle = (field: EmployeeDataField) => {
    if (value.includes(field)) {
      // Don't allow deselecting if it's the last one
      if (value.length === 1) {
        return;
      }
      onChange(value.filter(f => f !== field));
    } else {
      onChange([...value, field]);
    }
  };

  return (
    <div className={cn('space-y-3', className)}>
      <div className="flex items-center justify-between">
        <Label className="text-sm font-medium text-primary">
          Employee Data Required
          <span className="ml-1 text-vibrant-red">*</span>
        </Label>
        <span className="text-xs text-muted-foreground">
          {value.length} selected
        </span>
      </div>

      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        {ALL_FIELDS.map(field => {
          const isSelected = value.includes(field);
          const isEmailField = field === EmployeeDataField.EMAIL_ADDRESS;

          return (
            <label
              key={field}
              className={cn(
                'flex cursor-pointer items-center gap-2 rounded-md border p-3 transition-colors',
                isSelected
                  ? 'border-card-border bg-card-background'
                  : 'border-primary hover:bg-card-background',
                isEmailField &&
                  value.length === 1 &&
                  isSelected &&
                  'cursor-not-allowed opacity-60',
              )}
            >
              <input
                type="checkbox"
                checked={isSelected}
                onChange={() => handleToggle(field)}
                className="size-4 rounded border-card-border"
                disabled={isEmailField && value.length === 1 && isSelected}
              />
              <span
                className={cn(
                  'text-sm',
                  isSelected ? 'text-primary' : 'text-muted-foreground',
                )}
              >
                {getEmployeeDataFieldLabel(field)}
              </span>
            </label>
          );
        })}
      </div>

      {error && <p className="text-sm text-vibrant-red">{error}</p>}

      <p className="text-xs text-muted-foreground">
        These fields will be used for personalization in the email template. At
        least one field is required.
      </p>
    </div>
  );
};

export default EmployeeDataFieldSelector;
