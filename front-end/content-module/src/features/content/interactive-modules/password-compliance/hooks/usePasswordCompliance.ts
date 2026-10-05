import { useMemo, useState } from 'react';

import { ALL_FRAMEWORKS, FRAMEWORKS } from '../data/frameworks';
import {
  ChangeFrequencyValue,
  FrameworkCategory,
  IFrameworkEvaluation,
} from '../types';
import {
  calculateComplianceScore,
  calculatePasswordStrength,
  evaluateFramework,
  getStrengthLabel,
} from '../utils/compliance';

const parseChangeFrequency = (
  value: ChangeFrequencyValue,
): number | null => {
  if (!value || value === 'never') return null;
  return parseInt(value, 10);
};

export const usePasswordCompliance = () => {
  const [password, setPassword] = useState('');
  const [usesMfa, setUsesMfa] = useState(false);
  const [changeFrequency, setChangeFrequency] =
    useState<ChangeFrequencyValue>('');
  const [activeTab, setActiveTab] = useState<FrameworkCategory>('security');

  const changeFrequencyDays = parseChangeFrequency(changeFrequency);
  const hasPassword = password.length > 0;

  const input = useMemo(
    () => ({
      password,
      usesMfa,
      changeFrequencyDays,
    }),
    [password, usesMfa, changeFrequencyDays],
  );

  const evaluationsByCategory = useMemo(() => {
    const evaluateList = (list: typeof FRAMEWORKS.security) =>
      list.map(fw => evaluateFramework(fw, input));

    return {
      security: evaluateList(FRAMEWORKS.security),
      privacy: evaluateList(FRAMEWORKS.privacy),
      other: evaluateList(FRAMEWORKS.other),
    };
  }, [input]);

  const allEvaluations: Array<IFrameworkEvaluation> = useMemo(
    () => [
      ...evaluationsByCategory.security,
      ...evaluationsByCategory.privacy,
      ...evaluationsByCategory.other,
    ],
    [evaluationsByCategory],
  );

  const strength = hasPassword ? calculatePasswordStrength(password) : 0;
  const strengthLabel = getStrengthLabel(strength);
  const complianceScore = hasPassword
    ? calculateComplianceScore(allEvaluations)
    : 0;

  return {
    password,
    setPassword,
    usesMfa,
    setUsesMfa,
    changeFrequency,
    setChangeFrequency,
    activeTab,
    setActiveTab,
    hasPassword,
    strength,
    strengthLabel,
    complianceScore,
    frameworks: FRAMEWORKS,
    evaluationsByCategory,
    totalFrameworkCount: ALL_FRAMEWORKS.length,
  };
};

export type UsePasswordComplianceReturn = ReturnType<
  typeof usePasswordCompliance
>;
