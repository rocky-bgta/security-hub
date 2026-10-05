import { SyntheticEvent, useCallback, useEffect, useMemo, useRef } from 'react';
import { Variable } from 'lucide-react';

import {
  EmployeeDataField,
  getEmployeeDataFieldLabel,
} from 'models/EmailTemplate';
import { cn } from 'utils/Helper';

export const SCRIPT_VARIABLES = [
  ...Object.values(EmployeeDataField),
  'PHISHING_LINK',
] as const;

const PLACEHOLDER_PATTERN = /\{\{[A-Z0-9_]+\}\}/g;

export const getScriptVariableLabel = (field: string): string => {
  if (field === 'PHISHING_LINK') return 'Phishing Link';
  if (Object.values(EmployeeDataField).includes(field as EmployeeDataField)) {
    return getEmployeeDataFieldLabel(field as EmployeeDataField);
  }
  return field;
};

type Caret = { start: number; end: number };

/**
 * Wires a script textarea to the variable picker so a placeholder is inserted
 * at the caret, keeping the caret after the inserted text.
 */
export const useScriptVariableInsert = (
  script: string,
  setScript: (value: string) => void,
) => {
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const caretRef = useRef<Caret | null>(null);
  const pendingCaretRef = useRef<number | null>(null);

  useEffect(() => {
    const caret = pendingCaretRef.current;
    if (caret === null) return;
    pendingCaretRef.current = null;

    const textarea = textareaRef.current;
    if (!textarea) return;
    textarea.focus();
    textarea.setSelectionRange(caret, caret);
  }, [script]);

  const rememberCaret = useCallback(
    (event: SyntheticEvent<HTMLTextAreaElement>) => {
      const textarea = event.currentTarget;
      caretRef.current = {
        start: textarea.selectionStart,
        end: textarea.selectionEnd,
      };
    },
    [],
  );

  // Call when the editor is opened or reset so the next insert appends.
  const resetCaret = useCallback(() => {
    caretRef.current = null;
  }, []);

  const insertVariable = (variable: string) => {
    const placeholder = `{{${variable}}}`;
    const caret = caretRef.current;
    const start = Math.min(caret?.start ?? script.length, script.length);
    const end = Math.min(caret?.end ?? script.length, script.length);

    setScript(`${script.slice(0, start)}${placeholder}${script.slice(end)}`);

    const nextCaret = start + placeholder.length;
    caretRef.current = { start: nextCaret, end: nextCaret };
    pendingCaretRef.current = nextCaret;
  };

  const textareaProps = {
    ref: textareaRef,
    onSelect: rememberCaret,
    onKeyUp: rememberCaret,
    onClick: rememberCaret,
    onBlur: rememberCaret,
  };

  return { textareaProps, insertVariable, resetCaret };
};

interface ScriptVariablePickerProps {
  script: string;
  onInsert: (variable: string) => void;
  disabled?: boolean;
  className?: string;
  gridClassName?: string;
}

export const ScriptVariablePicker = ({
  script,
  onInsert,
  disabled,
  className,
  gridClassName,
}: ScriptVariablePickerProps) => {
  const usedCount = useMemo(
    () => (script.match(PLACEHOLDER_PATTERN) ?? []).length,
    [script],
  );

  return (
    <div className={cn('space-y-3', className)}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-sm font-medium">
          <Variable className="size-4 text-primary" /> Variables
        </div>
        <span className="text-xs text-muted-foreground">
          {usedCount} in script
        </span>
      </div>

      <p className="text-xs text-muted-foreground">
        Click a variable to insert it at the cursor position. The same variable
        can be used multiple times.
      </p>

      <div className={cn('grid grid-cols-2 gap-3', gridClassName)}>
        {SCRIPT_VARIABLES.map(variable => (
          <button
            key={variable}
            type="button"
            disabled={disabled}
            // Keep the textarea focused so the caret stays where the user left it.
            onMouseDown={event => event.preventDefault()}
            onClick={() => onInsert(variable)}
            className="min-w-0 rounded-md border border-card-border p-3 text-left transition-colors hover:border-primary/30 hover:bg-primary/10 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <span className="block truncate text-sm font-medium">
              {getScriptVariableLabel(variable)}
            </span>
            <span className="block truncate text-[11px] text-muted-foreground">
              {`{{${variable}}}`}
            </span>
          </button>
        ))}
      </div>
    </div>
  );
};
