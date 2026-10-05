import { X } from 'lucide-react';
import { Button } from 'common/Button';
import type { ImageDialogState } from './types';

interface ImageEditDialogProps {
  state: ImageDialogState;
  onUpdate: (updates: Partial<ImageDialogState>) => void;
  onSave: () => void;
  onClose: () => void;
}

const ImageEditDialog = ({
  state,
  onUpdate,
  onSave,
  onClose,
}: ImageEditDialogProps) => (
  <div
    className="home-fixed home-inset-0 home-z-[100] home-flex home-items-center home-justify-center home-bg-black/60"
    onClick={onClose}
  >
    <div
      className="home-mx-4 home-w-full home-max-w-md home-rounded-lg home-border home-border-card-border home-bg-card-background home-shadow-2xl"
      onClick={e => e.stopPropagation()}
    >
      <div className="home-flex home-items-center home-justify-between home-border-b home-border-card-border home-px-4 home-py-3">
        <h3 className="home-text-sm home-font-semibold home-text-primary">
          Replace Image
        </h3>
        <button
          onClick={onClose}
          className="home-text-muted-foreground hover:home-text-primary"
        >
          <X className="home-size-4" />
        </button>
      </div>

      <div className="home-space-y-4 home-p-4">
        {state.src && (
          <div className="home-flex home-justify-center home-rounded-md home-border home-border-card-border home-bg-gray-50 home-p-3 dark:home-bg-gray-900">
            <img
              src={state.src}
              alt={state.alt || 'Preview'}
              className="home-max-h-40 home-max-w-full home-rounded home-object-contain"
            />
          </div>
        )}

        <div>
          <label className="home-mb-1.5 home-block home-text-xs home-font-medium home-text-primary">
            Image URL
          </label>
          <input
            type="text"
            value={state.src}
            onChange={e => onUpdate({ src: e.target.value })}
            className="home-w-full home-rounded-md home-border home-border-card-border home-bg-transparent home-px-3 home-py-2 home-text-sm home-text-primary placeholder:home-text-muted-foreground focus:home-border-blue-500 focus:home-outline-none focus:home-ring-1 focus:home-ring-blue-500"
            placeholder="https://example.com/image.jpg"
          />
        </div>

        <div>
          <label className="home-mb-1.5 home-block home-text-xs home-font-medium home-text-primary">
            Alt Text
          </label>
          <input
            type="text"
            value={state.alt}
            onChange={e => onUpdate({ alt: e.target.value })}
            className="home-w-full home-rounded-md home-border home-border-card-border home-bg-transparent home-px-3 home-py-2 home-text-sm home-text-primary placeholder:home-text-muted-foreground focus:home-border-blue-500 focus:home-outline-none focus:home-ring-1 focus:home-ring-blue-500"
            placeholder="Image description"
          />
        </div>
      </div>

      <div className="home-flex home-justify-end home-gap-2 home-border-t home-border-card-border home-px-4 home-py-3">
        <Button type="button" variant="outline" size="sm" onClick={onClose}>
          Cancel
        </Button>
        <Button type="button" size="sm" onClick={onSave}>
          Apply
        </Button>
      </div>
    </div>
  </div>
);

export default ImageEditDialog;
