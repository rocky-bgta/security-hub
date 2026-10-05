import { FormEvent, useEffect, useState } from 'react';
import { Loader2 } from 'lucide-react';

import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Textarea } from 'common/Textarea';
import type {
  IVishingAttackTemplate,
  IVishingAttackTemplateRequest,
} from 'models/Vishing';
import {
  VishingAttackTemplateFormSchema,
  defaultVishingAttackTemplateForm,
  type TVishingAttackTemplateForm,
} from 'schemas/VishingAttackTemplateSchema';
import { cn } from 'utils/Helper';
import {
  ScriptVariablePicker,
  useScriptVariableInsert,
} from './ScriptVariablePicker';

interface AttackTemplateModalProps {
  isOpen: boolean;
  isSubmitting: boolean;
  template: IVishingAttackTemplate | null;
  onClose: () => void;
  onSubmit: (
    payload: IVishingAttackTemplateRequest,
  ) => Promise<IVishingAttackTemplate | null>;
}

const AttackTemplateModal = ({
  isOpen,
  isSubmitting,
  template,
  onClose,
  onSubmit,
}: AttackTemplateModalProps) => {
  const isEdit = !!template;
  const [form, setForm] = useState<TVishingAttackTemplateForm>(
    defaultVishingAttackTemplateForm,
  );
  const [errors, setErrors] = useState<
    Partial<Record<keyof TVishingAttackTemplateForm, string>>
  >({});

  const updateField = <K extends keyof TVishingAttackTemplateForm>(
    key: K,
    value: TVishingAttackTemplateForm[K],
  ) => {
    setForm(prev => ({ ...prev, [key]: value }));
    setErrors(prev => ({ ...prev, [key]: undefined }));
  };

  const { textareaProps, insertVariable, resetCaret } = useScriptVariableInsert(
    form.script,
    value => updateField('script', value),
  );

  useEffect(() => {
    if (!isOpen) return;
    resetCaret();

    if (template) {
      setForm({
        name: template.name ?? '',
        script: template.script ?? '',
      });
    } else {
      setForm(defaultVishingAttackTemplateForm);
    }
    setErrors({});
  }, [isOpen, template, resetCaret]);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    const parsed = VishingAttackTemplateFormSchema.safeParse({
      name: form.name.trim(),
      script: form.script.trim(),
    });

    if (!parsed.success) {
      const fieldErrors = parsed.error.flatten().fieldErrors;
      setErrors({
        name: fieldErrors.name?.[0],
        script: fieldErrors.script?.[0],
      });
      return;
    }

    const saved = await onSubmit(parsed.data);
    if (saved) {
      onClose();
    }
  };

  return (
    <Dialog
      open={isOpen}
      onOpenChange={open => {
        if (!open && !isSubmitting) onClose();
      }}
    >
      <DialogContent className="max-w-3xl p-0">
        <form onSubmit={handleSubmit}>
          <div className="border-b px-6 py-5">
            <DialogHeader>
              <DialogTitle>
                {isEdit ? 'Edit Attack Template' : 'Add Attack Template'}
              </DialogTitle>
              <DialogDescription>
                Define a reusable vishing call script with dynamic placeholders.
              </DialogDescription>
            </DialogHeader>
          </div>

          <div className="max-h-[70vh] space-y-5 overflow-y-auto px-6 py-5">
            <div className="space-y-1.5">
              <Label htmlFor="attack-template-name">
                Template name <span className="text-destructive">*</span>
              </Label>
              <Input
                id="attack-template-name"
                value={form.name}
                onChange={event => updateField('name', event.target.value)}
                placeholder="e.g. MFA Reset"
                disabled={isSubmitting}
                className={cn(errors.name && 'border-destructive')}
              />
              {errors.name && (
                <p className="text-sm text-destructive">{errors.name}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="attack-template-script">
                Script <span className="text-destructive">*</span>
              </Label>
              <Textarea
                id="attack-template-script"
                value={form.script}
                onChange={event => updateField('script', event.target.value)}
                rows={10}
                className={cn(
                  'font-mono text-sm',
                  errors.script && 'border-destructive',
                )}
                placeholder="Hello {{FIRST_NAME}}, this is..."
                disabled={isSubmitting}
                {...textareaProps}
              />
              {errors.script && (
                <p className="text-sm text-destructive">{errors.script}</p>
              )}
            </div>

            <ScriptVariablePicker
              script={form.script}
              onInsert={insertVariable}
              disabled={isSubmitting}
              gridClassName="md:grid-cols-4"
            />
          </div>

          <DialogFooter className="border-t bg-muted/30 px-6 py-4">
            <Button
              type="button"
              variant="ghost"
              onClick={onClose}
              disabled={isSubmitting}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting && <Loader2 className="size-4 animate-spin" />}
              {isEdit ? 'Save changes' : 'Create template'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default AttackTemplateModal;
