const SCROLL_PADDING = 16;
const SCROLL_SETTLE_TIMEOUT_MS = 450;
const HOST_ANIMATION_DELAY_MS = 1100;

let hostReadyWait: Promise<void> | null = null;

type ScrollTarget = HTMLElement | Window;

interface IInFlight {
  id: number;
  el: HTMLElement;
  scroller: ScrollTarget;
}

let inFlight: IInFlight | null = null;
let flightSeq = 0;

const getSimulatorScroller = (): HTMLElement | null => {
  const root = document.querySelector(
    '[data-ransomware-simulator-root]',
  ) as HTMLElement | null;
  if (!root) return null;
  return root.querySelector(
    '[data-ransomware-simulator-scroll]',
  ) as HTMLElement | null;
};

const startFlight = (el: HTMLElement, scroller: ScrollTarget): void => {
  flightSeq += 1;
  inFlight = { id: flightSeq, el, scroller };
};

const isIdentityTransform = (transform: string): boolean => {
  if (!transform || transform === 'none') return true;
  const values = transform.match(/-?\d*\.?\d+(?:e[-+]?\d+)?/gi);
  if (!values) return true;
  const n = values.map(Number);
  if (transform.startsWith('matrix3d') && n.length >= 16) {
    return Math.abs(n[0] - 1) < 0.02 && Math.abs(n[5] - 1) < 0.02;
  }
  if (n.length >= 6) {
    return Math.abs(n[0] - 1) < 0.02 && Math.abs(n[3] - 1) < 0.02;
  }
  return true;
};

const hostNeedsEntranceDelay = (root: HTMLElement): boolean => {
  let node: HTMLElement | null = root;
  while (node && node !== document.documentElement) {
    const style = window.getComputedStyle(node);
    if (!isIdentityTransform(style.transform)) return true;
    const animations = node.getAnimations?.() ?? [];
    if (
      animations.some(
        animation =>
          animation.playState === 'running' || animation.playState === 'idle',
      )
    ) {
      return true;
    }
    node = node.parentElement;
  }
  return false;
};

const nextFrame = (): Promise<void> =>
  new Promise(resolve => {
    requestAnimationFrame(() => resolve());
  });

const settleHost = async (): Promise<void> => {
  await nextFrame();
  await nextFrame();

  const root = document.querySelector(
    '[data-ransomware-simulator-root]',
  ) as HTMLElement | null;
  if (!root || !hostNeedsEntranceDelay(root)) return;

  await new Promise<void>(resolve => {
    window.setTimeout(resolve, HOST_ANIMATION_DELAY_MS);
  });
};

export const whenWalkthroughHostReady = (): Promise<void> => {
  if (!hostReadyWait) {
    hostReadyWait = settleHost().finally(() => {
      hostReadyWait = null;
    });
  }
  return hostReadyWait;
};

export const whenWalkthroughScrollSettled = (): Promise<void> => {
  const flight = inFlight;
  if (!flight) return Promise.resolve();

  return new Promise(resolve => {
    let done = false;
    const finish = () => {
      if (done) return;
      done = true;
      flight.scroller.removeEventListener('scrollend', finish);
      window.removeEventListener('scrollend', finish, true);
      window.clearTimeout(timeout);
      if (inFlight?.id === flight.id) {
        inFlight = null;
      }
      resolve();
    };

    flight.scroller.addEventListener('scrollend', finish);
    window.addEventListener('scrollend', finish, true);
    const timeout = window.setTimeout(finish, SCROLL_SETTLE_TIMEOUT_MS);
  });
};

export const scrollWalkthroughTargetIntoView = (el: HTMLElement): boolean => {
  if (inFlight?.el === el) return true;

  const scroller = getSimulatorScroller();
  const root = scroller?.closest(
    '[data-ransomware-simulator-root]',
  ) as HTMLElement | null;

  if (!scroller || !root?.contains(el)) {
    startFlight(el, window);
    el.scrollIntoView({
      behavior: 'smooth',
      block: 'center',
      inline: 'nearest',
    });
    return true;
  }

  const elRect = el.getBoundingClientRect();
  const box = scroller.getBoundingClientRect();
  const inView =
    elRect.top >= box.top + SCROLL_PADDING &&
    elRect.bottom <= box.bottom - SCROLL_PADDING;

  if (inView) {
    inFlight = null;
    return false;
  }

  startFlight(el, scroller);
  const delta = elRect.top - box.top - (box.height / 2 - elRect.height / 2);
  scroller.scrollTo({
    top: scroller.scrollTop + delta,
    behavior: 'smooth',
  });
  return true;
};

export const bringWalkthroughTargetIntoView = async (
  el: HTMLElement,
): Promise<void> => {
  const scrolled = scrollWalkthroughTargetIntoView(el);
  if (scrolled) {
    await whenWalkthroughScrollSettled();
  }
};
