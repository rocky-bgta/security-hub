import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Copy, Sparkles } from 'lucide-react';
import { toast } from 'react-toastify';
import { PLACEHOLDER_GROUPS } from './templatePlaceholders';

interface EmailTemplateFieldsHelpModalProps {
  isOpen: boolean;
  onClose: () => void;
}

async function copyToClipboard(text: string) {
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(text);
      return true;
    }
  } catch {
    // fall through to fallback copy
  }

  try {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', 'true');
    textarea.style.position = 'fixed';
    textarea.style.top = '-9999px';
    textarea.style.left = '-9999px';

    document.body.appendChild(textarea);
    textarea.select();
    const ok = document.execCommand('copy');
    document.body.removeChild(textarea);
    return ok;
  } catch {
    return false;
  }
}

const EmailTemplateFieldsHelpModal = ({
  isOpen,
  onClose,
}: EmailTemplateFieldsHelpModalProps) => {
  const handleCopy = async (token: string) => {
    const ok = await copyToClipboard(token);
    if (ok) toast.success(`Copied ${token}`);
    else toast.error('Failed to copy placeholder. Please copy manually.');
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Sparkles className="size-5 text-primary" />
            Available Fields
          </DialogTitle>
        </DialogHeader>

        <p className="text-sm text-muted-foreground">
          Use these tokens inside your email subject/body. Click the copy icon
          to insert a field quickly.
        </p>

        <div className="mt-6 space-y-6">
          {PLACEHOLDER_GROUPS.map(group => (
            <section key={group.title}>
              <h3 className="mb-3 text-sm font-semibold text-primary">
                {group.title}
              </h3>

              <div className="space-y-2">
                {group.fields.map(field => (
                  <div
                    key={field.token}
                    className="flex items-start justify-between gap-3 rounded-md border border-card-border bg-card-background px-3 py-2"
                  >
                    <div className="min-w-0">
                      <code className="break-words rounded bg-muted px-2 py-0.5 font-mono text-xs text-foreground">
                        {field.token}
                      </code>
                      <p className="mt-1 text-xs text-muted-foreground">
                        &rarr; {field.description}
                      </p>
                    </div>

                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => handleCopy(field.token)}
                      aria-label={`Copy ${field.token}`}
                      title={`Copy ${field.token}`}
                    >
                      <Copy className="size-4" />
                    </Button>
                  </div>
                ))}
              </div>
            </section>
          ))}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default EmailTemplateFieldsHelpModal;
