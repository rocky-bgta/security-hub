import { useCallback, useMemo, useState } from 'react';

import { WALKTHROUGH_STEPS } from '../data/walkthroughSteps';
import { SimulationPhase, WalkthroughTargetId } from '../types';

interface IUseWalkthroughOptions {
  phase: SimulationPhase;
  showDecryptionModal: boolean;
}

export const useWalkthrough = ({
  phase,
  showDecryptionModal,
}: IUseWalkthroughOptions) => {
  const [guideActive, setGuideActive] = useState(true);
  const [stepIndex, setStepIndex] = useState(0);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [completed, setCompleted] = useState(false);
  const [completeDismissed, setCompleteDismissed] = useState(false);

  const totalSteps = WALKTHROUGH_STEPS.length;

  // Derive step from simulation progress so we auto-advance without effects
  const derivedIndex = useMemo(() => {
    if (!guideActive || completed) return stepIndex;

    let minIndex = stepIndex;

    if (phase !== 'landing' && minIndex < 1) minIndex = 1;

    if (
      (phase === 'encrypting' ||
        phase === 'completed' ||
        phase === 'decrypting' ||
        phase === 'recovered') &&
      minIndex < 6
    ) {
      minIndex = 6; // observe-encryption
    }

    if (
      (phase === 'completed' ||
        phase === 'decrypting' ||
        phase === 'recovered') &&
      minIndex < 7
    ) {
      minIndex = 7; // open-decrypt
    }

    if (showDecryptionModal && minIndex < 8) {
      minIndex = 8; // decrypt form
    }

    if (phase === 'running' && minIndex < 5) {
      minIndex = 5; // observe-flow
    }

    if (phase === 'recovered') {
      return totalSteps - 1;
    }

    return Math.max(stepIndex, minIndex);
  }, [guideActive, completed, stepIndex, phase, showDecryptionModal, totalSteps]);

  const effectiveIndex = guideActive ? derivedIndex : stepIndex;
  const currentStep = guideActive ? WALKTHROUGH_STEPS[effectiveIndex] : null;

  const progressLabel = useMemo(() => {
    if (!guideActive || completed) return null;
    return `Step ${effectiveIndex + 1} of ${totalSteps}`;
  }, [guideActive, completed, effectiveIndex, totalSteps]);

  const clearFeedback = useCallback(() => setFeedback(null), []);

  const skipTour = useCallback(() => {
    setGuideActive(false);
    setFeedback(null);
    setCompleted(false);
    setCompleteDismissed(false);
  }, []);

  const dismissComplete = useCallback(() => {
    setCompleteDismissed(true);
    setGuideActive(false);
    setFeedback(null);
  }, []);

  const restartTour = useCallback(() => {
    setGuideActive(true);
    setStepIndex(0);
    setFeedback(null);
    setCompleted(false);
    setCompleteDismissed(false);
  }, []);

  const goNext = useCallback(() => {
    setFeedback(null);
    setStepIndex(prev => {
      const base = Math.max(prev, derivedIndex);
      if (base >= totalSteps - 1) {
        setGuideActive(false);
        setCompleted(true);
        setCompleteDismissed(false);
        return base;
      }
      return base + 1;
    });
  }, [totalSteps, derivedIndex]);

  const goBack = useCallback(() => {
    setFeedback(null);
    setStepIndex(prev => Math.max(0, Math.min(prev, derivedIndex) - 1));
  }, [derivedIndex]);

  const advanceToTarget = useCallback((targetId: WalkthroughTargetId) => {
    const index = WALKTHROUGH_STEPS.findIndex(s => s.targetId === targetId);
    if (index >= 0) {
      setStepIndex(index);
      setFeedback(null);
    }
  }, []);

  const notifyAction = useCallback(
    (action: string) => {
      if (!guideActive) return;

      if (action === 'enterWorkspace') {
        setStepIndex(1);
        setFeedback(null);
        return;
      }
      if (action === 'startSimulation') {
        setStepIndex(5);
        setFeedback(null);
        return;
      }
      if (action === 'openDecryption') {
        setStepIndex(8);
        setFeedback(null);
        return;
      }
      if (action === 'decryptSuccess') {
        setGuideActive(false);
        setCompleted(true);
        setCompleteDismissed(false);
        setFeedback(null);
      }
    },
    [guideActive],
  );

  const notifyDecryptError = useCallback(
    (error: 'empty' | 'invalid') => {
      if (!guideActive) return;
      if (error === 'empty') {
        setFeedback('Paste the key shown above — an empty field cannot unlock files.');
        return;
      }
      setFeedback(
        'That key does not match. Use the exact demo key, or restore from backups in real life.',
      );
    },
    [guideActive],
  );

  const reportOffTarget = useCallback(
    (clickedTarget?: string) => {
      if (!guideActive || !currentStep) return;
      if (clickedTarget && clickedTarget === currentStep.targetId) return;

      setFeedback(`Click the highlighted control: ${currentStep.whereToClick}`);
    },
    [guideActive, currentStep],
  );

  return {
    guideActive,
    completed,
    completeDismissed,
    stepIndex: effectiveIndex,
    totalSteps,
    currentStep,
    progressLabel,
    feedback,
    clearFeedback,
    skipTour,
    restartTour,
    dismissComplete,
    goNext,
    goBack,
    notifyAction,
    notifyDecryptError,
    reportOffTarget,
    advanceToTarget,
    canGoBack: effectiveIndex > 0 && effectiveIndex <= 4,
    canGoNext: Boolean(currentStep?.allowSkipAction),
  };
};

export type WalkthroughApi = ReturnType<typeof useWalkthrough>;
