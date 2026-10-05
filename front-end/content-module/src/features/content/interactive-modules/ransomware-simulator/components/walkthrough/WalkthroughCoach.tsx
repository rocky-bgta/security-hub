import { CSSProperties, useRef } from 'react';

import { Button } from 'common/Button';

import OverlayPortal from '../OverlayPortal';
import type { WalkthroughApi } from '../../hooks/useWalkthrough';
import {
  useWalkthroughAnchor,
  WalkthroughPlacement,
} from '../../hooks/useWalkthroughAnchor';
import { WalkthroughTargetId } from '../../types';
import WalkthroughFeedback from './WalkthroughFeedback';

interface IProps {
  walkthrough: WalkthroughApi;
  targetId: WalkthroughTargetId | null;
  onRestart?: () => void;
}

const TOOLTIP_BG = '#1a2332';

const caretStyleFor = (
  placement: WalkthroughPlacement,
  offset: number,
): CSSProperties => {
  const base: CSSProperties = {
    position: 'absolute',
    width: 0,
    height: 0,
    borderStyle: 'solid',
  };

  if (placement === 'below') {
    return {
      ...base,
      left: offset,
      top: 0,
      transform: 'translate(-50%, -100%)',
      borderWidth: '0 6px 6px 6px',
      borderColor: `transparent transparent ${TOOLTIP_BG} transparent`,
    };
  }
  if (placement === 'above') {
    return {
      ...base,
      left: offset,
      bottom: 0,
      transform: 'translate(-50%, 100%)',
      borderWidth: '6px 6px 0 6px',
      borderColor: `${TOOLTIP_BG} transparent transparent transparent`,
    };
  }
  if (placement === 'right') {
    return {
      ...base,
      left: 0,
      top: offset,
      transform: 'translate(-100%, -50%)',
      borderWidth: '6px 6px 6px 0',
      borderColor: `transparent ${TOOLTIP_BG} transparent transparent`,
    };
  }
  return {
    ...base,
    right: 0,
    top: offset,
    transform: 'translate(100%, -50%)',
    borderWidth: '6px 0 6px 6px',
    borderColor: `transparent transparent transparent ${TOOLTIP_BG}`,
  };
};

const WalkthroughCoach = ({ walkthrough, targetId, onRestart }: IProps) => {
  const {
    guideActive,
    completed,
    completeDismissed,
    currentStep,
    progressLabel,
    feedback,
    skipTour,
    restartTour,
    goNext,
    goBack,
    canGoBack,
    dismissComplete,
  } = walkthrough;

  const tooltipRef = useRef<HTMLDivElement>(null);
  const anchor = useWalkthroughAnchor({
    active: Boolean(guideActive && currentStep),
    targetId,
    tooltipRef,
  });

  if (completed && !guideActive && !completeDismissed) {
    return (
      <div
        className="content-pointer-events-auto content-w-64 content-max-w-[min(16rem,calc(100%-1.5rem))] content-rounded-lg content-bg-muted content-p-3 content-shadow-lg"
        role="dialog"
        aria-label="Guide complete"
      >
        <p className="content-mb-1 content-text-sm content-font-semibold content-text-white">
          Guide complete
        </p>
        <p className="content-mb-2 content-text-xs content-text-muted-foreground">
          You finished the walkthrough. Continue when you are ready, or restart
          the guide.
        </p>
        <div className="content-flex content-gap-2">
          <Button
            type="button"
            size="sm"
            onClick={onRestart ?? restartTour}
          >
            Restart
          </Button>
          <Button type="button" size="sm" variant="ghost" onClick={dismissComplete}>
            Dismiss
          </Button>
        </div>
      </div>
    );
  }

  if (!guideActive && !completed) {
    return (
      <div className="content-pointer-events-auto">
        <Button type="button" size="sm" onClick={restartTour}>
          Start walkthrough
        </Button>
      </div>
    );
  }

  if (!currentStep) return null;

  return (
    <OverlayPortal>
      <div
        ref={tooltipRef}
        className="content-pointer-events-auto content-absolute content-z-[1200] content-w-[min(17.5rem,calc(100%-1rem))] content-max-w-[17.5rem] content-rounded-lg content-bg-muted content-p-3 content-shadow-lg"
        style={
          anchor
            ? { top: anchor.top, left: anchor.left }
            : { visibility: 'hidden', top: 0, left: 0 }
        }
        role="dialog"
        aria-label={currentStep.title}
      >
        {anchor ? (
          <span
            aria-hidden
            style={caretStyleFor(anchor.placement, anchor.caretOffset)}
          />
        ) : null}

        <div className="content-mb-1 content-flex content-items-center content-justify-between content-gap-2">
          <p className="content-text-[11px] content-font-medium content-uppercase content-tracking-wide content-text-muted-foreground">
            {progressLabel}
          </p>
          <button
            type="button"
            onClick={skipTour}
            className="content-text-[11px] content-text-muted-foreground hover:content-text-white"
          >
            Skip
          </button>
        </div>

        <h3 className="content-text-sm content-font-semibold content-leading-tight content-text-white">
          {currentStep.title}
        </h3>
        <p className="content-mt-1 content-text-xs content-leading-snug content-text-muted-foreground">
          {currentStep.blurb}
        </p>
        <WalkthroughFeedback message={feedback} />

        {canGoBack || currentStep.allowSkipAction ? (
          <div className="content-mt-2 content-flex content-gap-2">
            <Button
              type="button"
              size="sm"
              variant="ghost"
              onClick={goBack}
              disabled={!canGoBack}
              className="content-h-7 content-px-2 content-text-xs"
            >
              Back
            </Button>
            {currentStep.allowSkipAction ? (
              <Button
                type="button"
                size="sm"
                onClick={goNext}
                className="content-h-7 content-px-2 content-text-xs"
              >
                Next
              </Button>
            ) : null}
          </div>
        ) : null}
      </div>
    </OverlayPortal>
  );
};

export default WalkthroughCoach;
