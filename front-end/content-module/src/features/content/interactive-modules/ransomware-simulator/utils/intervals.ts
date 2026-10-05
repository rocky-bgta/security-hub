import { SimulationSpeed } from '../types';

export const getStepIntervalMs = (speed: SimulationSpeed): number => {
  if (speed === 'slow') return 3000;
  if (speed === 'fast') return 1000;
  return 2000;
};

export const ENCRYPTION_TICK_MS = 200;
export const DECRYPTION_TICK_MS = 200;
export const RANSOM_NOTE_DELAY_MS = 1500;
export const TOAST_DEFAULT_MS = 3000;
