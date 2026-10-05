import { RefObject, useEffect, useState } from 'react';

import { WalkthroughTargetId } from '../types';
import { bringWalkthroughTargetIntoView, whenWalkthroughHostReady } from '../utils/scrollWalkthroughTarget';

export type WalkthroughPlacement = 'below' | 'above' | 'right' | 'left';

export interface IWalkthroughAnchor {
  top: number;
  left: number;
  width: number;
  placement: WalkthroughPlacement;
  caretOffset: number;
}

interface IRect {
  top: number;
  left: number;
  right: number;
  bottom: number;
  width: number;
  height: number;
}

interface IOptions {
  active: boolean;
  targetId: WalkthroughTargetId | null;
  tooltipRef: RefObject<HTMLElement | null>;
}

const GAP = 10;
const EDGE = 8;
const FALLBACK_WIDTH = 280;
const FALLBACK_HEIGHT = 140;
const TARGET_RETRY_MS = 400;

const getSimulatorRoot = (): HTMLElement | null =>
  document.querySelector(
    '[data-ransomware-simulator-root]',
  ) as HTMLElement | null;

const toRect = (r: DOMRect): IRect => ({
  top: Math.round(r.top),
  left: Math.round(r.left),
  right: Math.round(r.right),
  bottom: Math.round(r.bottom),
  width: Math.round(r.width),
  height: Math.round(r.height),
});

const clamp = (value: number, min: number, max: number): number =>
  Math.min(Math.max(value, min), max);

const getLayoutBounds = (): IRect | null => {
  const root = getSimulatorRoot();
  if (!root) return null;
  const r = toRect(root.getBoundingClientRect());
  return {
    top: r.top + EDGE,
    left: r.left + EDGE,
    right: r.right - EDGE,
    bottom: r.bottom - EDGE,
    width: Math.max(0, r.width - EDGE * 2),
    height: Math.max(0, r.height - EDGE * 2),
  };
};

const toRootRelative = (anchor: IWalkthroughAnchor): IWalkthroughAnchor => {
  const root = getSimulatorRoot();
  if (!root) return anchor;
  const r = root.getBoundingClientRect();
  return {
    ...anchor,
    top: Math.round(anchor.top - r.top),
    left: Math.round(anchor.left - r.left),
  };
};

const anchorsEqual = (
  a: IWalkthroughAnchor | null,
  b: IWalkthroughAnchor | null,
): boolean => {
  if (a === b) return true;
  if (!a || !b) return false;
  return (
    a.top === b.top &&
    a.left === b.left &&
    a.width === b.width &&
    a.placement === b.placement &&
    a.caretOffset === b.caretOffset
  );
};

const computeAnchor = (
  target: IRect,
  bounds: IRect,
  tooltipHeight: number,
): IWalkthroughAnchor => {
  const width = Math.round(Math.min(FALLBACK_WIDTH, Math.max(120, bounds.width)));
  const height = Math.min(tooltipHeight, Math.max(80, bounds.height));
  const targetCenterX = target.left + target.width / 2;
  const targetCenterY = target.top + target.height / 2;

  const fitsBelow = target.bottom + GAP + height <= bounds.bottom;
  const fitsAbove = target.top - GAP - height >= bounds.top;
  const fitsRight = target.right + GAP + width <= bounds.right;
  const fitsLeft = target.left - GAP - width >= bounds.left;

  let placement: WalkthroughPlacement = 'below';
  let top = target.bottom + GAP;
  let left = targetCenterX - width / 2;

  if (fitsBelow) {
    placement = 'below';
    top = target.bottom + GAP;
    left = targetCenterX - width / 2;
  } else if (fitsAbove) {
    placement = 'above';
    top = target.top - GAP - height;
    left = targetCenterX - width / 2;
  } else if (fitsRight) {
    placement = 'right';
    left = target.right + GAP;
    top = targetCenterY - height / 2;
  } else if (fitsLeft) {
    placement = 'left';
    left = target.left - GAP - width;
    top = targetCenterY - height / 2;
  } else {
    placement = 'below';
    top = target.bottom + GAP;
    left = targetCenterX - width / 2;
  }

  const maxLeft = bounds.right - width;
  const maxTop = bounds.bottom - height;
  left = clamp(left, bounds.left, Math.max(bounds.left, maxLeft));
  top = clamp(top, bounds.top, Math.max(bounds.top, maxTop));

  let caretOffset = 0;
  if (placement === 'below' || placement === 'above') {
    caretOffset = clamp(targetCenterX - left, 16, width - 16);
  } else {
    caretOffset = clamp(targetCenterY - top, 16, height - 16);
  }

  return {
    top: Math.round(top),
    left: Math.round(left),
    width,
    placement,
    caretOffset: Math.round(caretOffset),
  };
};

export const useWalkthroughAnchor = ({
  active,
  targetId,
  tooltipRef,
}: IOptions): IWalkthroughAnchor | null => {
  const [anchor, setAnchor] = useState<IWalkthroughAnchor | null>(null);

  useEffect(() => {
    if (!active || !targetId) {
      setAnchor(null);
      return undefined;
    }

    let cancelled = false;
    let settled = false;
    let retry: number | undefined;

    const update = () => {
      if (cancelled) return;
      const el = document.querySelector(
        `[data-walkthrough-id="${targetId}"]`,
      ) as HTMLElement | null;
      const bounds = getLayoutBounds();
      if (!el || !bounds) {
        setAnchor(prev => (prev === null ? prev : null));
        return;
      }

      const tooltipRect = tooltipRef.current?.getBoundingClientRect();
      const next = toRootRelative(
        computeAnchor(
          toRect(el.getBoundingClientRect()),
          bounds,
          tooltipRect?.height || FALLBACK_HEIGHT,
        ),
      );
      setAnchor(prev => (anchorsEqual(prev, next) ? prev : next));
    };

    const onFollow = () => {
      if (!settled) return;
      update();
    };

    const run = async (allowRetry: boolean) => {
      await whenWalkthroughHostReady();
      if (cancelled) return;

      const el = document.querySelector(
        `[data-walkthrough-id="${targetId}"]`,
      ) as HTMLElement | null;

      if (!el) {
        update();
        if (allowRetry) {
          retry = window.setTimeout(() => {
            void run(false);
          }, TARGET_RETRY_MS);
        }
        return;
      }

      await bringWalkthroughTargetIntoView(el);
      if (cancelled) return;
      settled = true;
      update();
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
      window.removeEventListener('resize', onFollow);
      window.removeEventListener('scroll', onFollow, true);
      clearInterval(interval);
    };
  }, [active, targetId, tooltipRef]);

  if (!active || !targetId) return null;
  return anchor;
};
