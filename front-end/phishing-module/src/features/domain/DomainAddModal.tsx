import { zodResolver } from '@hookform/resolvers/zod';
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
import { Globe, PlusIcon, RefreshCw } from 'lucide-react';
import {
  useEffect,
  useState,
  type ClipboardEvent,
  type KeyboardEvent,
} from 'react';
import { useForm } from 'react-hook-form';
import {
  DefaultDomainAddValues,
  DomainAddSchema,
  EMAIL_ADDRESS_MAX_LENGTH,
  EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE,
  EMAIL_ADDRESS_MIN_LENGTH,
  EMAIL_ADDRESS_MIN_LENGTH_ERROR_MESSAGE,
  TDomainAddForm,
} from 'schemas/DomainSchema';

interface DomainAddModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAdd: (data: { domain: string }) => Promise<boolean>;
}

const DomainAddModal = ({ isOpen, onClose, onAdd }: DomainAddModalProps) => {
  const [submitting, setSubmitting] = useState(false);
  const [domainLengthCapMessage, setDomainLengthCapMessage] = useState<
    string | null
  >(null);

  const domainForm = useForm<TDomainAddForm>({
    resolver: zodResolver(DomainAddSchema),
    defaultValues: DefaultDomainAddValues,
    reValidateMode: 'onChange',
  });

  // Reset modal state when closed
  useEffect(() => {
    if (!isOpen) {
      setDomainLengthCapMessage(null);
      domainForm.reset();
    }
  }, [isOpen, domainForm]);

  const handleAdd = async (data: TDomainAddForm) => {
    setSubmitting(true);
    const success = await onAdd(data);
    setSubmitting(false);

    if (success) {
      onClose();
    }
  };

  // eslint-disable-next-line react-hooks/incompatible-library
  const watchedDomain = domainForm.watch('domain');
  const trimmedDomainLen = (watchedDomain ?? '').trim().length;
  const meetsDomainLengthCriteria =
    trimmedDomainLen >= EMAIL_ADDRESS_MIN_LENGTH &&
    trimmedDomainLen <= EMAIL_ADDRESS_MAX_LENGTH;

  const domainFieldError = domainForm.formState.errors.domain;
  const isZodDomainLengthError =
    domainFieldError?.message === EMAIL_ADDRESS_MIN_LENGTH_ERROR_MESSAGE ||
    domainFieldError?.message === EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE;

  const showDomainLengthCriteriaHint =
    Boolean(domainLengthCapMessage) ||
    (!meetsDomainLengthCriteria && isZodDomainLengthError);

  const handleDomainKeyDown = (e: KeyboardEvent<HTMLInputElement>): void => {
    const target = e.currentTarget;
    const { value, selectionStart, selectionEnd } = target;
    const start = selectionStart ?? 0;
    const end = selectionEnd ?? value.length;
    const selectedLen = end - start;

    if (e.ctrlKey || e.metaKey || e.altKey) return;
    if (
      e.key === 'Backspace' ||
      e.key === 'Delete' ||
      e.key.startsWith('Arrow') ||
      e.key === 'Tab' ||
      e.key === 'Home' ||
      e.key === 'End'
    ) {
      return;
    }

    if (e.key.length !== 1) return;

    const newLength = value.length - selectedLen + 1;
    if (newLength > EMAIL_ADDRESS_MAX_LENGTH) {
      e.preventDefault();
      setDomainLengthCapMessage(EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE);
    }
  };

  const handleDomainPaste = (e: ClipboardEvent<HTMLInputElement>): void => {
    const target = e.currentTarget;
    const { value, selectionStart, selectionEnd } = target;
    const start = selectionStart ?? 0;
    const end = selectionEnd ?? value.length;
    const selectedLen = end - start;
    const pasted = e.clipboardData.getData('text');
    const newLength = value.length - selectedLen + pasted.length;
    if (newLength > EMAIL_ADDRESS_MAX_LENGTH) {
      setDomainLengthCapMessage(EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <PlusIcon className="size-5 text-primary" />
            Add New Domain
          </DialogTitle>
          <DialogDescription>
            Enter a domain that you want to add.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={domainForm.handleSubmit(handleAdd)}>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="domain">Domain</Label>
              <div className="relative">
                <Globe className="absolute left-3 top-3 size-4 text-muted-foreground" />
                <Input
                  id="domain"
                  placeholder="yourdomain.com"
                  className="pl-9"
                  maxLength={EMAIL_ADDRESS_MAX_LENGTH}
                  onKeyDown={handleDomainKeyDown}
                  onPaste={handleDomainPaste}
                  {...domainForm.register('domain', {
                    onChange: e => {
                      if (e.target.value.length < EMAIL_ADDRESS_MAX_LENGTH) {
                        setDomainLengthCapMessage(null);
                      }
                    },
                  })}
                />
              </div>
              {showDomainLengthCriteriaHint && (
                <p className="text-xs text-vibrant-red">
                  Minimum {EMAIL_ADDRESS_MIN_LENGTH} characters, maximum{' '}
                  {EMAIL_ADDRESS_MAX_LENGTH} characters.
                </p>
              )}
              {domainForm.formState.errors.domain && (
                <p className="text-sm text-vibrant-red">
                  {domainForm.formState.errors.domain.message}
                </p>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? (
                <>
                  <RefreshCw className="mr-2 size-4 animate-spin" />
                  Adding...
                </>
              ) : (
                <>
                  <Globe className="mr-2 size-4" />
                  Add Domain
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default DomainAddModal;
