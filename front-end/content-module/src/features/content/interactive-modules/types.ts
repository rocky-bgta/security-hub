import type { ComponentType } from 'react';

import { IPhishingEmail } from 'models/Content';

export enum InteractiveModuleId {
  PASSWORD_COMPLIANCE = 'password_compliance',
  RANSOMWARE_SIMULATOR = 'ransomware_simulator',
  PHISHING_DETECTION = 'phishing_detection',
}

export type InteractiveModuleComponent = ComponentType<{
  title?: string;
  description?: string;
  className?: string;
  emails?: Array<IPhishingEmail>;
  timeLimitSeconds?: number;
  onGameOver?: () => void;
}>;
