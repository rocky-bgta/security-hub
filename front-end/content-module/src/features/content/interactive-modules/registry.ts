import PasswordCompliance from './password-compliance/PasswordCompliance';
import PhishingDetection from './phishing-detection/PhishingDetection';
import RansomwareSimulator from './ransomware-simulator/RansomwareSimulator';
import {
  InteractiveModuleComponent,
  InteractiveModuleId,
} from './types';

export const interactiveModuleRegistry: Partial<
  Record<InteractiveModuleId, InteractiveModuleComponent>
> = {
  [InteractiveModuleId.PASSWORD_COMPLIANCE]: PasswordCompliance,
  [InteractiveModuleId.RANSOMWARE_SIMULATOR]: RansomwareSimulator,
  [InteractiveModuleId.PHISHING_DETECTION]: PhishingDetection,
};

export const getInteractiveModule = (
  id: InteractiveModuleId,
): InteractiveModuleComponent | null =>
  interactiveModuleRegistry[id] ?? null;

export { InteractiveModuleId };
export { default as PasswordCompliance } from './password-compliance/PasswordCompliance';
export { default as PhishingDetection } from './phishing-detection/PhishingDetection';
export { default as RansomwareSimulator } from './ransomware-simulator/RansomwareSimulator';
