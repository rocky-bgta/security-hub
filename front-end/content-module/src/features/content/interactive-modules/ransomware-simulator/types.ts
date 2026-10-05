export type SimulationPhase =
  | 'landing'
  | 'idle'
  | 'running'
  | 'paused'
  | 'encrypting'
  | 'completed'
  | 'decrypting'
  | 'recovered';

export type SimulationSpeed = 'slow' | 'normal' | 'fast';

export type EncryptionAlgorithm = 'AES256' | 'RSA2048' | 'ChaCha20';

export type FileStatus = 'safe' | 'encrypted' | 'decrypting' | 'decrypted';

export type ExtensionValue = string | (() => string);

export interface ISimulationStep {
  id: number;
  name: string;
  emoji: string;
  description: string;
}

export interface ISimulatedFile {
  name: string;
  icon: string;
  type: string;
  content: string;
}

export interface IFileRuntimeState {
  name: string;
  displayName: string;
  status: FileStatus;
}

export interface IToastState {
  message: string;
  visible: boolean;
}

export type WalkthroughTargetId =
  | 'landing-cta'
  | 'family-select'
  | 'algorithm-select'
  | 'speed-select'
  | 'start-btn'
  | 'attack-flow'
  | 'encryption-dashboard'
  | 'decrypt-cta'
  | 'show-ransom-btn'
  | 'decrypt-form';

export interface IWalkthroughStep {
  id: string;
  targetId: WalkthroughTargetId;
  title: string;
  blurb: string;
  whatToDo: string;
  whereToClick: string;
  whyItMatters: string;
  expectedOutcome: string;
  /** Auto-advance when this simulation phase is reached */
  autoAdvanceOnPhase?: SimulationPhase;
  /** Allow Next without completing the action */
  allowSkipAction?: boolean;
}

export const ENCRYPTION_ALGORITHM_OPTIONS: Array<{
  value: EncryptionAlgorithm;
  label: string;
}> = [
  { value: 'AES256', label: 'AES-256' },
  { value: 'RSA2048', label: 'RSA-2048' },
  { value: 'ChaCha20', label: 'ChaCha20' },
];

export const SIMULATION_SPEED_OPTIONS: Array<{
  value: SimulationSpeed;
  label: string;
}> = [
  { value: 'slow', label: 'Slow' },
  { value: 'normal', label: 'Normal' },
  { value: 'fast', label: 'Fast' },
];

export const RECOMMENDED_FAMILY = 'WannaCry Ransomware';
export const RECOMMENDED_SPEED: SimulationSpeed = 'normal';
export const INITIAL_RANSOM_BTC = 5;
export const DOUBLED_RANSOM_BTC = 10;
export const INITIAL_COUNTDOWN_SECONDS = 95;
export const BTC_ADDRESS = '1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa';
