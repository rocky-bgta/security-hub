import { ReactNode } from 'react';

import { Button } from 'common/Button';
import UserBlockWrapper from 'components/UserBlockWrapper';
import { IContent, IInteractiveContent, TContent } from 'models/Content';
import { cn } from 'utils/Helper';

interface InteractiveOverlayProps {
  activeContent: IInteractiveContent | null;
  contentRenderer?: (
    content: IContent<TContent>,
    handleNext: () => void,
    onComplete?: () => void,
  ) => ReactNode;
  onNext: () => void;
}

const InteractiveOverlay = ({
  activeContent,
  contentRenderer,
  onNext,
}: InteractiveOverlayProps) => {
  return (
    <div
      className={cn(
        'content-absolute content-inset-0 content-z-[100] content-flex content-w-full content-origin-center content-items-center content-justify-center content-bg-black content-bg-opacity-80 content-transition-transform content-duration-1000 content-ease-in-out',
        activeContent
          ? 'content-scale-100 content-opacity-100'
          : 'content-scale-0 content-opacity-0',
      )}
      onClick={e => e.stopPropagation()}
    >
      <div className="content-relative content-size-full content-overflow-auto">
        {activeContent && (
          <UserBlockWrapper content={activeContent?.contentBody}>
            {contentRenderer?.(activeContent.contentBody, onNext)}
          </UserBlockWrapper>
        )}

        {activeContent?.canSkip && (
          <div className="content-absolute content-bottom-4 content-left-4 content-z-10">
            <Button onClick={onNext} size="sm">
              <span>Skip</span>
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};

export default InteractiveOverlay;
