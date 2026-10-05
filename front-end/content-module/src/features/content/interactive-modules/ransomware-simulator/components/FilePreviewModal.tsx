import { X } from 'lucide-react';

interface IProps {
  open: boolean;
  fileName: string | null;
  content: string;
  encrypted: boolean;
  ransomwareFamily: string;
  encryptionAlgorithm: string;
  onClose: () => void;
}

const FilePreviewModal = ({
  open,
  fileName,
  content,
  encrypted,
  ransomwareFamily,
  encryptionAlgorithm,
  onClose,
}: IProps) => {
  if (!open || !fileName) return null;

  return (
    <div
      className="content-pointer-events-auto content-absolute content-inset-0 content-z-[1002] content-flex content-items-center content-justify-center content-bg-black/70 content-p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="file-preview-title"
    >
      <div className="content-relative content-max-h-[85%] content-w-full content-max-w-2xl content-overflow-hidden content-rounded-xl content-border content-border-steel-gray content-bg-card">
        <div className="content-flex content-items-center content-justify-between content-border-b content-border-steel-gray content-px-4 content-py-3">
          <h2
            id="file-preview-title"
            className="content-truncate content-pr-4 content-text-base content-font-semibold content-text-white"
          >
            {fileName}
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="content-rounded-md content-p-1 content-text-muted-foreground hover:content-bg-steel-gray/40 hover:content-text-white"
            aria-label="Close preview"
          >
            <X className="content-size-5" />
          </button>
        </div>

        <div className="content-max-h-[60%] content-overflow-auto content-p-4">
          {encrypted ? (
            <div className="content-space-y-3">
              <div className="content-rounded-lg content-border content-border-red-500/40 content-bg-red-500/10 content-p-3 content-text-sm content-font-medium content-text-red-300">
                This file has been encrypted by {ransomwareFamily}!
              </div>
              <p className="content-text-sm content-text-muted-foreground">
                This file has been locked with {encryptionAlgorithm} encryption
                and cannot be accessed without the decryption key. All attempts
                to open or modify this file will fail.
              </p>
              <p className="content-text-sm content-text-muted-foreground">
                To recover your files in this demo, use the decryption key shown
                in the recovery panel. In real incidents, restore from backups.
              </p>
              <pre className="content-overflow-auto content-rounded-lg content-bg-black content-p-3 content-font-mono content-text-xs content-text-emerald-400">
                {content}
              </pre>
            </div>
          ) : (
            <pre className="content-whitespace-pre-wrap content-rounded-lg content-bg-background content-p-3 content-font-mono content-text-xs content-text-muted-foreground md:content-text-sm">
              {content}
            </pre>
          )}
        </div>
      </div>
    </div>
  );
};

export default FilePreviewModal;
