import { X } from 'lucide-react';

import { Button } from 'common/Button';
import { BTC_ADDRESS } from '../types';

interface IProps {
  open: boolean;
  extension: string;
  ransomAmount: number;
  countdownTime: number;
  countdownExpired: boolean;
  onMinimize: () => void;
  onDecryptNow: () => void;
}

const formatCountdown = (seconds: number): string => {
  const minutes = Math.floor(seconds / 60);
  const secs = seconds % 60;
  return `${minutes}:${secs < 10 ? '0' : ''}${secs}`;
};

const RansomNote = ({
  open,
  extension,
  ransomAmount,
  countdownTime,
  countdownExpired,
  onMinimize,
  onDecryptNow,
}: IProps) => {
  if (!open) return null;

  return (
    <div
      className="content-pointer-events-auto content-absolute content-inset-0 content-z-[1000] content-flex content-items-center content-justify-center content-bg-black/80 content-p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="ransom-note-title"
    >
      <div className="content-relative content-max-h-full content-w-full content-max-w-lg content-overflow-y-auto content-rounded-xl content-border content-border-red-500/50 content-bg-[#1a1a1a] content-p-6 content-shadow-2xl">
        <button
          type="button"
          onClick={onMinimize}
          className="content-absolute content-right-3 content-top-3 content-rounded-md content-p-1 content-text-white/70 hover:content-bg-white/10 hover:content-text-white"
          aria-label="Minimize ransom note"
        >
          <X className="content-size-5" />
        </button>

        <h2
          id="ransom-note-title"
          className="content-mb-4 content-text-center content-text-2xl content-font-bold content-text-red-500"
        >
          YOUR FILES HAVE BEEN ENCRYPTED!
        </h2>

        <p className="content-mb-4 content-text-sm content-text-gray-300">
          All of your important files have been encrypted with a strong
          algorithm. File extension applied:{' '}
          <span className="content-font-mono content-text-amber-400">
            {extension || 'no extension'}
          </span>
        </p>

        <div className="content-mb-4 content-rounded-lg content-border content-border-red-500/30 content-bg-red-500/10 content-p-4 content-text-center">
          <p className="content-mb-1 content-text-sm content-text-gray-400">
            Ransom amount
          </p>
          <p className="content-text-3xl content-font-bold content-text-amber-400">
            {ransomAmount.toFixed(2)} BTC
          </p>
          <p className="content-mt-2 content-break-all content-font-mono content-text-xs content-text-gray-400">
            {BTC_ADDRESS}
          </p>
        </div>

        <p
          className="content-mb-6 content-text-center content-text-sm content-font-semibold content-text-red-400"
          aria-live="polite"
        >
          {countdownExpired
            ? 'Time expired! The price has now doubled to 10.00 BTC.'
            : `Time remaining: ${formatCountdown(countdownTime)}`}
        </p>

        <p className="content-mb-6 content-text-xs content-text-gray-500">
          Educational note: Never pay ransoms in real incidents. Restore from
          offline backups and report the attack to your security team.
        </p>

        <div className="content-flex content-flex-col content-gap-2 sm:content-flex-row">
          <Button
            type="button"
            variant="secondary"
            onClick={onMinimize}
            className="content-flex-1"
          >
            Minimize
          </Button>
          <Button
            type="button"
            onClick={onDecryptNow}
            data-walkthrough-id="decrypt-cta"
            className="content-flex-1 content-bg-blue-600 hover:content-bg-blue-600/90"
          >
            Decrypt Files Now
          </Button>
        </div>
      </div>
    </div>
  );
};

export default RansomNote;
