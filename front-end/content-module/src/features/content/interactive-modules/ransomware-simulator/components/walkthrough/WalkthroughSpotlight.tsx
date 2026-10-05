import { useEffect, useRef, useState } from 'react';

import { cn } from 'utils/Helper';

import { WalkthroughTargetId } from '../../types';
import { bringWalkthroughTargetIntoView, whenWalkthroughHostReady } from '../../utils/scrollWalkthroughTarget';

interface IProps {
  active: boolean;
  targetId: WalkthroughTargetId | null;
}

interface IRect {
  top: number;
  left: number;
  width: number;
  height: number;
}

const PADDING = 8;
const STEP_MOTION_MS = 200;
const TARGET_RETRY_MS = 400;

const roundRect = (r: IRect | null): IRect | null => {
  if (!r) return null;
  return {
    top: Math.round(r.top),
    left: Math.round(r.left),
    width: Math.round(r.width),
    height: Math.round(r.height),
  };
};

const rectsEqual = (a: IRect | null, b: IRect | null): boolean => {
  if (a === b) return true;
  if (!a || !b) return false;
  return (
    a.top === b.top &&
    a.left === b.left &&
    a.width === b.width &&
    a.height === b.height
  );
};

const getSimulatorRoot = (): HTMLElement | null =>
  document.querySelector(
    '[data-ransomware-simulator-root]',
  ) as HTMLElement | null;

const measureRoot = (): IRect | null => {
  const root = getSimulatorRoot();
  if (!root) return null;
  const r = root.getBoundingClientRect();
  return roundRect({ top: r.top, left: r.left, width: r.width, height: r.height });
};

const measureTarget = (targetId: WalkthroughTargetId): IRect | null => {
  const el = document.querySelector(
    `[data-walkthrough-id="${targetId}"]`,
  ) as HTMLElement | null;
  if (!el) return null;
  const r = el.getBoundingClientRect();
  return roundRect({
    top: r.top - PADDING,
    left: r.left - PADDING,
    width: r.width + PADDING * 2,
    height: r.height + PADDING * 2,
  });
};

const WalkthroughSpotlight = ({ active, targetId }: IProps) => {
  const [rect, setRect] = useState<IRect | null>(null);
  const [rootRect, setRootRect] = useState<IRect | null>(null);
  const [animate, setAnimate] = useState(false);
  const prevTargetRef = useRef<WalkthroughTargetId | null>(null);

  useEffect(() => {
    if (!active || !targetId) {
      prevTargetRef.current = null;
      setAnimate(false);
      setRect(null);
      setRootRect(null);
      return undefined;
    }

    let cancelled = false;
    let settled = false;
    let retry: number | undefined;
    let animateOff: number | undefined;
    const isStepChange =
      prevTargetRef.current !== null && prevTargetRef.current !== targetId;
    prevTargetRef.current = targetId;

    const applyMeasure = () => {
      if (cancelled) return;
      const nextRoot = measureRoot();
      const nextRect = measureTarget(targetId);
      setRootRect(prev => (rectsEqual(prev, nextRoot) ? prev : nextRoot));
      setRect(prev => (rectsEqual(prev, nextRect) ? prev : nextRect));
    };

    const onFollow = () => {
      if (!settled) return;
      applyMeasure();
    };

    const run = async (allowRetry: boolean) => {
      await whenWalkthroughHostReady();
      if (cancelled) return;

      const el = document.querySelector(
        `[data-walkthrough-id="${targetId}"]`,
      ) as HTMLElement | null;

      if (!el) {
        applyMeasure();
        if (allowRetry) {
          retry = window.setTimeout(() => {
            void run(false);
          }, TARGET_RETRY_MS);
        }
        return;
      }

      await bringWalkthroughTargetIntoView(el);
      if (cancelled) return;

      if (isStepChange) {
        setAnimate(true);
      }
      settled = true;
      applyMeasure();
      if (isStepChange) {
        animateOff = window.setTimeout(() => {
          if (!cancelled) setAnimate(false);
        }, STEP_MOTION_MS);
      }
    };

    const frame = requestAnimationFrame(() => {
      void run(true);
    });

    window.addEventListener('resize', onFollow);
    window.addEventListener('scroll', onFollow, true);
    const interval = setInterval(onFollow, 250);

    return () => {
      cancelled = true;
      cancelAnimationFrame(frame);
      if (retry !== undefined) window.clearTimeout(retry);
      if (animateOff !== undefined) window.clearTimeout(animateOff);
      window.removeEventListener('resize', onFollow);
      window.removeEventListener('scroll', onFollow, true);
      clearInterval(interval);
    };
  }, [active, targetId]);

  if (!active || !targetId || !rect || !rootRect) return null;

  return (
    <div
      className="content-pointer-events-none content-absolute content-inset-0 content-overflow-hidden"
      aria-hidden
    >
      <div
        className={cn(
          'content-absolute content-rounded-xl content-border-2 content-border-primary content-shadow-[0_0_0_9999px_rgba(0,0,0,0.55)]',
          'content-ring-4 content-ring-primary/30',
          animate && 'content-transition-[top,left,width,height] content-duration-200',
        )}
        style={{
          top: rect.top - rootRect.top,
          left: rect.left - rootRect.left,
          width: rect.width,
          height: rect.height,
        }}
      />
    </div>
  );
};

export default WalkthroughSpotlight;
