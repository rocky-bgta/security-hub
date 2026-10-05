import {
  CheckStatus,
  IComplianceInput,
  IFramework,
  IFrameworkEvaluation,
  RequirementKey,
  StrengthLabel,
} from '../types';

/** Matches PasswordComplianceChecker.html special-character rule exactly */
export const SPECIAL_CHAR_REGEX =
  /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/;

const uncheckedResult = (): IFrameworkEvaluation => ({
  isCompliant: false,
  checks: {
    length: 'unchecked',
    lowercase: 'unchecked',
    uppercase: 'unchecked',
    number: 'unchecked',
    special: 'unchecked',
    mfa: 'unchecked',
    change: 'unchecked',
  },
});

export const evaluateFramework = (
  framework: IFramework,
  input: IComplianceInput,
): IFrameworkEvaluation => {
  if (!input.password) {
    return uncheckedResult();
  }

  const checks: Record<RequirementKey, CheckStatus> = {
    length: 'na',
    lowercase: 'na',
    uppercase: 'na',
    number: 'na',
    special: 'na',
    mfa: 'na',
    change: 'na',
  };

  let isCompliant = true;

  checks.length =
    input.password.length >= framework.minLength ? 'pass' : 'fail';
  if (checks.length === 'fail') isCompliant = false;

  if (framework.requiresLowercase) {
    checks.lowercase = /[a-z]/.test(input.password) ? 'pass' : 'fail';
    if (checks.lowercase === 'fail') isCompliant = false;
  }

  if (framework.requiresUppercase) {
    checks.uppercase = /[A-Z]/.test(input.password) ? 'pass' : 'fail';
    if (checks.uppercase === 'fail') isCompliant = false;
  }

  if (framework.requiresNumber) {
    checks.number = /[0-9]/.test(input.password) ? 'pass' : 'fail';
    if (checks.number === 'fail') isCompliant = false;
  }

  if (framework.requiresSpecial) {
    checks.special = SPECIAL_CHAR_REGEX.test(input.password) ? 'pass' : 'fail';
    if (checks.special === 'fail') isCompliant = false;
  }

  if (framework.requiresMFA) {
    checks.mfa = input.usesMfa ? 'pass' : 'fail';
    if (checks.mfa === 'fail') isCompliant = false;
  }

  if (framework.maxChangeDays) {
    checks.change =
      input.changeFrequencyDays !== null &&
      input.changeFrequencyDays <= framework.maxChangeDays
        ? 'pass'
        : 'fail';
    if (checks.change === 'fail') isCompliant = false;
  }

  return { isCompliant, checks };
};

export const calculatePasswordStrength = (password: string): number => {
  if (!password) return 0;

  let strength = 0;

  if (password.length >= 8) strength += 25;
  if (password.length >= 12) strength += 15;
  if (password.length >= 16) strength += 10;

  if (/[a-z]/.test(password)) strength += 10;
  if (/[A-Z]/.test(password)) strength += 10;
  if (/[0-9]/.test(password)) strength += 10;
  if (SPECIAL_CHAR_REGEX.test(password)) strength += 20;

  return Math.min(strength, 100);
};

export const getStrengthLabel = (strength: number): StrengthLabel => {
  if (strength === 0) return 'None';
  if (strength < 30) return 'Weak';
  if (strength < 70) return 'Medium';
  return 'Strong';
};

export const calculateComplianceScore = (
  evaluations: Array<IFrameworkEvaluation>,
): number => {
  if (!evaluations.length) return 0;
  const compliant = evaluations.filter(e => e.isCompliant).length;
  return Math.round((compliant / evaluations.length) * 100);
};

export const getScoreToneClass = (score: number): string => {
  if (score < 30) return 'content-text-red-500';
  if (score < 70) return 'content-text-yellow-500';
  return 'content-text-green-500';
};

export const getStrengthBarClass = (strength: number): string => {
  if (strength === 0) return 'content-bg-steel-gray';
  if (strength < 30) return 'content-bg-red-500';
  if (strength < 70) return 'content-bg-yellow-500';
  return 'content-bg-green-500';
};
