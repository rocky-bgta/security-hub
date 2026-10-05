import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from 'common/Dialog';
import { Lock } from 'lucide-react';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  message?: string;
}

const TrialUpgradePopup = ({
  isOpen,
  onClose,
  message = 'First, you need to buy a product to unlock this feature.',
}: IProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogTitle>
        <DialogDescription></DialogDescription>
      </DialogTitle>
      <DialogContent className="!content-w-[400px] !content-max-w-[90vw] !content-px-8 content-py-8">
        <div className="content-flex content-flex-col content-items-center content-text-center">
          {/* Icon Container */}
          <div className="content-mb-5 content-flex content-size-16 content-items-center content-justify-center content-rounded-full content-bg-amber-500/10">
            <Lock className="content-size-8 content-text-amber-500" />
          </div>

          {/* Title */}
          <h2 className="content-mb-3 content-text-xl content-font-semibold content-text-white">
            Feature Locked
          </h2>

          {/* Message */}
          <p className="content-mb-6 content-text-sm content-leading-relaxed content-text-gray-400">
            {message}
          </p>

          {/* Button */}
          <Button
            onClick={onClose}
            className="content-w-full content-max-w-[200px]"
          >
            Buy Product
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default TrialUpgradePopup;
