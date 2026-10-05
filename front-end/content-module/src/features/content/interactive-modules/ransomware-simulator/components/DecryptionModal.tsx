import { X } from 'lucide-react';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';

interface IProps {
  open: boolean;
  decryptionKey: string;
  keyInput: string;
  onKeyInputChange: (value: string) => void;
  decryptionProgress: number;
  isDecrypting: boolean;
  copyFeedback: boolean;
  onCopyKey: () => void;
  onDecrypt: () => void;
  onClose: () => void;
}

const DecryptionModal = ({
  open,
  decryptionKey,
  keyInput,
  onKeyInputChange,
  decryptionProgress,
  isDecrypting,
  copyFeedback,
  onCopyKey,
  onDecrypt,
  onClose,
}: IProps) => {
  if (!open) return null;

  return (
    <div
      className="content-pointer-events-auto content-absolute content-inset-0 content-z-[1001] content-flex content-items-center content-justify-center content-bg-black/70 content-p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="decrypt-modal-title"
    >
      <div
        className="content-relative content-w-full content-max-w-lg content-rounded-xl content-border content-border-steel-gray content-bg-muted content-p-6"
        data-walkthrough-id="decrypt-form"
      >
        <button
          type="button"
          onClick={onClose}
          className="content-absolute content-right-3 content-top-3 content-rounded-md content-p-1 content-text-muted-foreground hover:content-bg-steel-gray/40 hover:content-text-white"
          aria-label="Close decryption modal"
        >
          <X className="content-size-5" />
        </button>

        <h2
          id="decrypt-modal-title"
          className="content-mb-2 content-text-xl content-font-bold content-text-white"
        >
          Decrypt Your Files
        </h2>
        <p className="content-mb-4 content-text-sm content-text-muted-foreground">
          This demo shows a recovery key so you can practice the flow. In a real
          attack you should restore from backups — not pay.
        </p>

        <div className="content-mb-4">
          <Label className="content-mb-2 content-block content-text-sm content-text-white">
            Demo decryption key
          </Label>
          <div className="content-flex content-gap-2">
            <code className="content-block content-max-h-24 content-flex-1 content-overflow-auto content-break-all content-rounded-md content-border content-border-steel-gray content-bg-background content-p-2 content-font-mono content-text-xs content-text-emerald-400">
              {decryptionKey}
            </code>
            <Button type="button" variant="outline" onClick={onCopyKey}>
              {copyFeedback ? 'Copied!' : 'Copy'}
            </Button>
          </div>
        </div>

        <div className="content-mb-4">
          <Label
            htmlFor="decryption-key-input"
            className="content-mb-2 content-block content-text-sm content-text-white"
          >
            Paste decryption key
          </Label>
          <Input
            id="decryption-key-input"
            value={keyInput}
            onChange={e => onKeyInputChange(e.target.value)}
            placeholder="Paste the key here..."
            disabled={isDecrypting}
            autoComplete="off"
            spellCheck={false}
          />
        </div>

        {isDecrypting || decryptionProgress > 0 ? (
          <div className="content-mb-4">
            <div className="content-mb-2 content-h-2 content-overflow-hidden content-rounded-full content-bg-steel-gray/60">
              <div
                className="content-h-full content-rounded-full content-bg-blue-500 content-transition-all"
                style={{ width: `${decryptionProgress}%` }}
              />
            </div>
            <p className="content-text-sm content-text-muted-foreground">
              Decrypting files... {decryptionProgress}%
            </p>
          </div>
        ) : null}

        <Button
          type="button"
          onClick={onDecrypt}
          disabled={isDecrypting}
          className="content-w-full content-bg-blue-600 hover:content-bg-blue-600/90"
        >
          Decrypt Files
        </Button>
      </div>
    </div>
  );
};

export default DecryptionModal;
