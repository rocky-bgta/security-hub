import { Check } from 'lucide-react';

import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';

interface IDeepfakeReviewDialogProps {
  open: boolean;
  onOpenChange: (value: boolean) => void;
  background: string;
  face: string | null;
  provider: string;
  name: string;
  script: string;
  voiceEngine: string;
  language: string;
  model: string;
  onEditScript: () => void;
  onEditVoice: () => void;
  onEditFace: () => void;
  onConfirm: () => void;
}

const DeepfakeReviewDialog = ({
  open,
  onOpenChange,
  background,
  face,
  provider,
  name,
  script,
  voiceEngine,
  language,
  model,
  onEditScript,
  onEditVoice,
  onEditFace,
  onConfirm,
}: IDeepfakeReviewDialogProps) => {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Final Preview & Approval</DialogTitle>
          <DialogDescription>
            Review your deepfake setup — face, voice, script, provider, and
            model. Approve to unlock video generation, or jump back to adjust
            the script, voice, or face model.
          </DialogDescription>
        </DialogHeader>

        <div
          className="relative aspect-video w-full overflow-hidden rounded-lg border border-card-border"
          style={{ background }}
        >
          {face ? (
            <img
              src={face}
              alt="Final avatar"
              className="absolute bottom-0 left-1/2 h-[85%] -translate-x-1/2 object-contain"
            />
          ) : (
            <div className="absolute inset-0 grid place-items-center text-xs text-muted-foreground">
              Avatar preview
            </div>
          )}
          <div className="absolute left-3 top-3 rounded-md bg-background/70 px-2 py-1 text-[10px] uppercase tracking-wider backdrop-blur">
            Lip-sync · {provider || '—'}
          </div>
          <div className="absolute inset-x-3 bottom-3 rounded-md bg-background/70 px-3 py-2 text-xs backdrop-blur">
            <div className="truncate font-medium">
              {name || 'Untitled video'}
            </div>
            <div className="truncate text-muted-foreground">
              {script.slice(0, 80)}…
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2 text-xs sm:grid-cols-3">
          <div className="flex items-center justify-between gap-4 rounded-md border border-card-border px-3 py-2">
            <span className="text-muted-foreground">Voice</span>
            <span className="truncate font-medium">{voiceEngine || '—'}</span>
          </div>
          <div className="flex items-center justify-between gap-4 rounded-md border border-card-border px-3 py-2">
            <span className="text-muted-foreground">Language</span>
            <span className="truncate font-medium">{language}</span>
          </div>
          <div className="flex items-center justify-between gap-4 rounded-md border border-card-border px-3 py-2">
            <span className="text-muted-foreground">Model</span>
            <span className="truncate font-medium">{model || '—'}</span>
          </div>
        </div>

        <DialogFooter className="flex-col gap-2 sm:flex-row sm:justify-between">
          <div className="flex flex-wrap gap-2">
            <Button variant="outline" size="sm" onClick={onEditScript}>
              Edit script
            </Button>
            <Button variant="outline" size="sm" onClick={onEditVoice}>
              Edit voice
            </Button>
            <Button variant="outline" size="sm" onClick={onEditFace}>
              Edit face model
            </Button>
          </div>
          <Button
            onClick={onConfirm}
            className="bg-primary text-primary-foreground"
          >
            <Check className="mr-1 size-4" /> Approve
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default DeepfakeReviewDialog;
