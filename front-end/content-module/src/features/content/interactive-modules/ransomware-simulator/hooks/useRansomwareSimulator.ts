import { useCallback, useEffect, useRef, useState } from 'react';

import { SIMULATED_FILES } from '../data/files';
import { resolveExtension } from '../data/ransomwareFamilies';
import {
  DISK_STATUS_BY_STEP,
  SIMULATION_STEPS,
} from '../data/simulationSteps';
import {
  DOUBLED_RANSOM_BTC,
  EncryptionAlgorithm,
  FileStatus,
  IFileRuntimeState,
  INITIAL_COUNTDOWN_SECONDS,
  INITIAL_RANSOM_BTC,
  RECOMMENDED_FAMILY,
  RECOMMENDED_SPEED,
  SimulationPhase,
  SimulationSpeed,
} from '../types';
import { buildEncryptedDisplayName } from '../utils/files';
import {
  DECRYPTION_TICK_MS,
  ENCRYPTION_TICK_MS,
  getStepIntervalMs,
  RANSOM_NOTE_DELAY_MS,
  TOAST_DEFAULT_MS,
} from '../utils/intervals';
import { generateEncryptedData, generateRSAKey } from '../utils/keys';

const createInitialFiles = (): Array<IFileRuntimeState> =>
  SIMULATED_FILES.map(file => ({
    name: file.name,
    displayName: file.name,
    status: 'safe' as FileStatus,
  }));

export const useRansomwareSimulator = () => {
  const [phase, setPhase] = useState<SimulationPhase>('landing');
  const [currentStep, setCurrentStep] = useState(-1);
  const [encryptionProgress, setEncryptionProgress] = useState(0);
  const [decryptionProgress, setDecryptionProgress] = useState(0);
  const [files, setFiles] = useState<Array<IFileRuntimeState>>(createInitialFiles);
  const [educationalText, setEducationalText] = useState(
    'Select a ransomware family and click Start to begin the simulation.',
  );
  const [diskStatus, setDiskStatus] = useState('Ready');
  const [fileStatusText, setFileStatusText] = useState('Files are safe');

  const [ransomwareFamily, setRansomwareFamily] = useState(RECOMMENDED_FAMILY);
  const [encryptionAlgorithm, setEncryptionAlgorithm] =
    useState<EncryptionAlgorithm>('AES256');
  const [simulationSpeed, setSimulationSpeed] =
    useState<SimulationSpeed>(RECOMMENDED_SPEED);

  const [currentRansomwareExtension, setCurrentRansomwareExtension] =
    useState('.ransomware');
  const [currentRansomwareFamily, setCurrentRansomwareFamily] = useState('');
  const [decryptionKey, setDecryptionKey] = useState('');
  const [keyInput, setKeyInput] = useState('');
  const [countdownTime, setCountdownTime] = useState(INITIAL_COUNTDOWN_SECONDS);
  const [ransomAmount, setRansomAmount] = useState(INITIAL_RANSOM_BTC);
  const [countdownExpired, setCountdownExpired] = useState(false);

  const [showRansomNote, setShowRansomNote] = useState(false);
  const [showRansomNotification, setShowRansomNotification] = useState(false);
  const [showDecryptionModal, setShowDecryptionModal] = useState(false);
  const [showRansomButton, setShowRansomButton] = useState(false);
  const [ransomNoteMinimized, setRansomNoteMinimized] = useState(false);

  const [previewFileName, setPreviewFileName] = useState<string | null>(null);
  const [previewContent, setPreviewContent] = useState('');
  const [previewEncrypted, setPreviewEncrypted] = useState(false);
  const [showPreview, setShowPreview] = useState(false);

  const [toastMessage, setToastMessage] = useState('');
  const [toastVisible, setToastVisible] = useState(false);
  const [lastDecryptError, setLastDecryptError] = useState<string | null>(null);
  const [copyFeedback, setCopyFeedback] = useState(false);

  const simulationIntervalRef = useRef<ReturnType<typeof setInterval> | null>(
    null,
  );
  const encryptionIntervalRef = useRef<ReturnType<typeof setInterval> | null>(
    null,
  );
  const decryptionIntervalRef = useRef<ReturnType<typeof setInterval> | null>(
    null,
  );
  const countdownIntervalRef = useRef<ReturnType<typeof setInterval> | null>(
    null,
  );
  const ransomDelayRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const toastTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const decryptCloseTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(
    null,
  );

  const phaseRef = useRef(phase);
  const currentStepRef = useRef(currentStep);
  const encryptionProgressRef = useRef(encryptionProgress);
  const decryptionProgressRef = useRef(decryptionProgress);
  const extensionRef = useRef(currentRansomwareExtension);
  const familyRef = useRef(currentRansomwareFamily);
  const algorithmRef = useRef(encryptionAlgorithm);
  const decryptionKeyRef = useRef(decryptionKey);
  const filesEncryptedBatchRef = useRef(false);

  useEffect(() => {
    phaseRef.current = phase;
  }, [phase]);
  useEffect(() => {
    currentStepRef.current = currentStep;
  }, [currentStep]);
  useEffect(() => {
    encryptionProgressRef.current = encryptionProgress;
  }, [encryptionProgress]);
  useEffect(() => {
    decryptionProgressRef.current = decryptionProgress;
  }, [decryptionProgress]);
  useEffect(() => {
    extensionRef.current = currentRansomwareExtension;
  }, [currentRansomwareExtension]);
  useEffect(() => {
    familyRef.current = currentRansomwareFamily;
  }, [currentRansomwareFamily]);
  useEffect(() => {
    algorithmRef.current = encryptionAlgorithm;
  }, [encryptionAlgorithm]);
  useEffect(() => {
    decryptionKeyRef.current = decryptionKey;
  }, [decryptionKey]);

  const clearAllIntervals = useCallback(() => {
    if (simulationIntervalRef.current) {
      clearInterval(simulationIntervalRef.current);
      simulationIntervalRef.current = null;
    }
    if (encryptionIntervalRef.current) {
      clearInterval(encryptionIntervalRef.current);
      encryptionIntervalRef.current = null;
    }
    if (decryptionIntervalRef.current) {
      clearInterval(decryptionIntervalRef.current);
      decryptionIntervalRef.current = null;
    }
    if (countdownIntervalRef.current) {
      clearInterval(countdownIntervalRef.current);
      countdownIntervalRef.current = null;
    }
    if (ransomDelayRef.current) {
      clearTimeout(ransomDelayRef.current);
      ransomDelayRef.current = null;
    }
    if (decryptCloseTimeoutRef.current) {
      clearTimeout(decryptCloseTimeoutRef.current);
      decryptCloseTimeoutRef.current = null;
    }
  }, []);

  const showToast = useCallback((message: string, duration = TOAST_DEFAULT_MS) => {
    setToastMessage(message);
    setToastVisible(true);
    if (toastTimeoutRef.current) clearTimeout(toastTimeoutRef.current);
    toastTimeoutRef.current = setTimeout(() => {
      setToastVisible(false);
    }, duration);
  }, []);

  const applyEncryptedRange = useCallback(
    (startIndex: number, endIndex: number) => {
      const extension = extensionRef.current;
      setFiles(prev =>
        prev.map((file, index) => {
          if (index < startIndex || index >= endIndex) return file;
          return {
            ...file,
            status: 'encrypted',
            displayName: buildEncryptedDisplayName(file.name, extension),
          };
        }),
      );
    },
    [],
  );

  const applyFileStatusRange = useCallback(
    (startIndex: number, endIndex: number, status: FileStatus) => {
      setFiles(prev =>
        prev.map((file, index) => {
          if (index < startIndex || index >= endIndex) return file;
          if (status === 'decrypted') {
            return {
              ...file,
              status: 'decrypted',
              displayName: file.name,
            };
          }
          if (status === 'decrypting') {
            return {
              ...file,
              status: 'decrypting',
            };
          }
          return file;
        }),
      );
    },
    [],
  );

  const startCountdown = useCallback(() => {
    if (countdownIntervalRef.current) {
      clearInterval(countdownIntervalRef.current);
    }
    countdownIntervalRef.current = setInterval(() => {
      setCountdownTime(prev => {
        const next = prev - 1;
        if (next <= 0) {
          if (countdownIntervalRef.current) {
            clearInterval(countdownIntervalRef.current);
            countdownIntervalRef.current = null;
          }
          setRansomAmount(DOUBLED_RANSOM_BTC);
          setCountdownExpired(true);
          return 0;
        }
        return next;
      });
    }, 1000);
  }, []);

  const startEncryption = useCallback(() => {
    setPhase('encrypting');
    setDiskStatus('Encrypting files...');
    setEducationalText(
      `Files are being encrypted using ${algorithmRef.current} algorithm. This is why regular backups are critical for ransomware protection.`,
    );

    const key = generateRSAKey();
    setDecryptionKey(key);
    decryptionKeyRef.current = key;
    filesEncryptedBatchRef.current = false;
    setEncryptionProgress(0);
    encryptionProgressRef.current = 0;

    if (encryptionIntervalRef.current) {
      clearInterval(encryptionIntervalRef.current);
    }

    encryptionIntervalRef.current = setInterval(() => {
      const next = encryptionProgressRef.current + 5;
      encryptionProgressRef.current = next;
      setEncryptionProgress(next);

      if (next > 0 && next <= 25 && !filesEncryptedBatchRef.current) {
        applyEncryptedRange(0, 2);
      } else if (next > 25 && next <= 50) {
        applyEncryptedRange(2, 4);
      } else if (next > 50 && next <= 75) {
        applyEncryptedRange(4, 6);
      } else if (next > 75 && next <= 100) {
        applyEncryptedRange(6, 8);
      }

      if (next >= 100) {
        if (encryptionIntervalRef.current) {
          clearInterval(encryptionIntervalRef.current);
          encryptionIntervalRef.current = null;
        }
        setPhase('completed');
        setDiskStatus('Encryption complete');
        setEducationalText(
          'Encryption complete! The files are now inaccessible. In a real attack, the attacker would demand a ransom for decryption. This demonstrates why backups and security awareness are essential.',
        );
        setFileStatusText('All files have been encrypted!');

        ransomDelayRef.current = setTimeout(() => {
          setShowRansomNote(true);
          setShowRansomButton(true);
          setRansomNoteMinimized(false);
          setShowRansomNotification(false);
          startCountdown();
        }, RANSOM_NOTE_DELAY_MS);
      }
    }, ENCRYPTION_TICK_MS);
  }, [applyEncryptedRange, startCountdown]);

  const enterWorkspace = useCallback(() => {
    setPhase('idle');
    showToast(
      'Ransomware simulator loaded. This is a safe educational environment.',
    );
  }, [showToast]);

  const startSimulation = useCallback(() => {
    const current = phaseRef.current;
    if (current !== 'idle' && current !== 'paused' && current !== 'completed') {
      return;
    }

    clearAllIntervals();
    setPhase('running');
    setRansomAmount(INITIAL_RANSOM_BTC);
    setCountdownExpired(false);
    setCountdownTime(INITIAL_COUNTDOWN_SECONDS);

    const family = ransomwareFamily;
    const extension = resolveExtension(family);
    setCurrentRansomwareFamily(family);
    setCurrentRansomwareExtension(extension);
    familyRef.current = family;
    extensionRef.current = extension;

    setEducationalText(
      `Simulation started. The ${family} is beginning its attack sequence.`,
    );
    setDiskStatus('Scanning system...');

    const speedMs = getStepIntervalMs(simulationSpeed);

    simulationIntervalRef.current = setInterval(() => {
      const nextStep = currentStepRef.current + 1;
      if (nextStep >= SIMULATION_STEPS.length) {
        if (simulationIntervalRef.current) {
          clearInterval(simulationIntervalRef.current);
          simulationIntervalRef.current = null;
        }
        startEncryption();
        return;
      }

      currentStepRef.current = nextStep;
      setCurrentStep(nextStep);
      setEducationalText(SIMULATION_STEPS[nextStep].description);
      if (DISK_STATUS_BY_STEP[nextStep]) {
        setDiskStatus(DISK_STATUS_BY_STEP[nextStep]);
      }
    }, speedMs);
  }, [
    clearAllIntervals,
    ransomwareFamily,
    simulationSpeed,
    startEncryption,
  ]);

  const pauseSimulation = useCallback(() => {
    if (phaseRef.current !== 'running' && phaseRef.current !== 'encrypting') {
      return;
    }
    setPhase('paused');
    if (simulationIntervalRef.current) {
      clearInterval(simulationIntervalRef.current);
      simulationIntervalRef.current = null;
    }
    if (encryptionIntervalRef.current) {
      clearInterval(encryptionIntervalRef.current);
      encryptionIntervalRef.current = null;
    }
    setEducationalText('Simulation paused. Click Start to continue.');
    setDiskStatus('Paused');
    showToast('Simulation paused');
  }, [showToast]);

  const resetSimulation = useCallback(() => {
    clearAllIntervals();
    setPhase('idle');
    setCurrentStep(-1);
    currentStepRef.current = -1;
    setEncryptionProgress(0);
    encryptionProgressRef.current = 0;
    setDecryptionProgress(0);
    decryptionProgressRef.current = 0;
    filesEncryptedBatchRef.current = false;
    setCountdownTime(INITIAL_COUNTDOWN_SECONDS);
    setRansomNoteMinimized(false);
    setRansomAmount(INITIAL_RANSOM_BTC);
    setCountdownExpired(false);
    setShowRansomNote(false);
    setShowRansomNotification(false);
    setShowDecryptionModal(false);
    setShowRansomButton(false);
    setShowPreview(false);
    setKeyInput('');
    setDecryptionKey('');
    decryptionKeyRef.current = '';
    setLastDecryptError(null);
    setFiles(createInitialFiles());
    setDiskStatus('Ready');
    setFileStatusText('Files are safe');
    setEducationalText(
      'Simulation reset. Click Start to begin the simulation again.',
    );
    showToast('Simulation has been reset');
  }, [clearAllIntervals, showToast]);

  const exitToLanding = useCallback(
    (options?: { silent?: boolean }) => {
      clearAllIntervals();
      setShowRansomNote(false);
      setShowRansomNotification(false);
      setShowDecryptionModal(false);
      setShowPreview(false);
      setPhase('landing');
      setCurrentStep(-1);
      currentStepRef.current = -1;
      setEncryptionProgress(0);
      setDecryptionProgress(0);
      setFiles(createInitialFiles());
      setDiskStatus('Ready');
      setFileStatusText('Files are safe');
      setShowRansomButton(false);
      setEducationalText(
        'Select a ransomware family and click Start to begin the simulation.',
      );
      if (!options?.silent) {
        showToast('Simulation exited');
      }
    },
    [clearAllIntervals, showToast],
  );

  const minimizeRansomNote = useCallback(() => {
    setShowRansomNote(false);
    setRansomNoteMinimized(true);
    setShowRansomNotification(true);
    showToast(
      'Ransom note minimized. Click the notification to view it again.',
    );
  }, [showToast]);

  const openRansomNote = useCallback(() => {
    setShowRansomNote(true);
    setRansomNoteMinimized(false);
    setShowRansomNotification(false);
  }, []);

  const closeRansomNotification = useCallback(() => {
    setShowRansomNotification(false);
  }, []);

  const openDecryptionModal = useCallback(() => {
    setShowRansomNote(false);
    setShowRansomNotification(false);
    if (countdownIntervalRef.current) {
      clearInterval(countdownIntervalRef.current);
      countdownIntervalRef.current = null;
    }
    setShowDecryptionModal(true);
  }, []);

  const closeDecryptionModal = useCallback(() => {
    setShowDecryptionModal(false);
  }, []);

  const decryptFiles = useCallback(() => {
    const key = keyInput.trim();
    if (!key) {
      setLastDecryptError('empty');
      showToast('Please enter a decryption key', 4000);
      return false;
    }
    if (key !== decryptionKeyRef.current) {
      setLastDecryptError('invalid');
      showToast('Invalid decryption key. Please try again.', 4000);
      return false;
    }

    setLastDecryptError(null);
    setPhase('decrypting');
    setDecryptionProgress(0);
    decryptionProgressRef.current = 0;

    if (decryptionIntervalRef.current) {
      clearInterval(decryptionIntervalRef.current);
    }

    decryptionIntervalRef.current = setInterval(() => {
      const next = decryptionProgressRef.current + 10;
      decryptionProgressRef.current = next;
      setDecryptionProgress(next);

      if (next > 0 && next <= 25) {
        applyFileStatusRange(0, 2, 'decrypting');
      } else if (next > 25 && next <= 50) {
        applyFileStatusRange(0, 4, 'decrypting');
        applyFileStatusRange(0, 2, 'decrypted');
      } else if (next > 50 && next <= 75) {
        applyFileStatusRange(0, 6, 'decrypting');
        applyFileStatusRange(0, 4, 'decrypted');
      } else if (next > 75 && next <= 100) {
        applyFileStatusRange(0, 8, 'decrypting');
        applyFileStatusRange(0, 6, 'decrypted');
      }

      if (next >= 100) {
        if (decryptionIntervalRef.current) {
          clearInterval(decryptionIntervalRef.current);
          decryptionIntervalRef.current = null;
        }
        applyFileStatusRange(0, 8, 'decrypted');
        setFileStatusText('All files have been decrypted!');
        setEducationalText(
          'Decryption complete! Your files are now accessible. This demonstrates why having a valid decryption key (or backups) is essential for recovering from a ransomware attack.',
        );
        setShowRansomButton(false);
        setPhase('recovered');

        decryptCloseTimeoutRef.current = setTimeout(() => {
          setShowDecryptionModal(false);
          setDecryptionProgress(0);
          decryptionProgressRef.current = 0;
          setKeyInput('');
          showToast('Files successfully decrypted!');
        }, 2000);
      }
    }, DECRYPTION_TICK_MS);

    return true;
  }, [applyFileStatusRange, keyInput, showToast]);

  const copyKeyToClipboard = useCallback(async () => {
    try {
      await navigator.clipboard.writeText(decryptionKeyRef.current);
      setCopyFeedback(true);
      showToast('Decryption key copied to clipboard');
      setTimeout(() => setCopyFeedback(false), 2000);
      return true;
    } catch {
      showToast(
        'Failed to copy key to clipboard. Please copy manually.',
        4000,
      );
      return false;
    }
  }, [showToast]);

  const previewFile = useCallback(
    (fileName: string) => {
      const meta = SIMULATED_FILES.find(f => f.name === fileName);
      const runtime = files.find(f => f.name === fileName);
      if (!meta || !runtime) return;

      setPreviewFileName(runtime.displayName);
      const isEncrypted = runtime.status === 'encrypted';
      setPreviewEncrypted(isEncrypted);
      if (isEncrypted) {
        setPreviewContent(generateEncryptedData());
      } else {
        setPreviewContent(meta.content);
      }
      setShowPreview(true);
    },
    [files],
  );

  const closePreview = useCallback(() => {
    setShowPreview(false);
  }, []);

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key !== 'Escape') return;
      if (showRansomNote) {
        minimizeRansomNote();
        return;
      }
      if (showDecryptionModal) {
        closeDecryptionModal();
        return;
      }
      if (showPreview) {
        closePreview();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [
    showRansomNote,
    showDecryptionModal,
    showPreview,
    minimizeRansomNote,
    closeDecryptionModal,
    closePreview,
  ]);

  useEffect(
    () => () => {
      clearAllIntervals();
      if (toastTimeoutRef.current) clearTimeout(toastTimeoutRef.current);
    },
    [clearAllIntervals],
  );

  const canStart =
    phase === 'idle' || phase === 'paused' || phase === 'completed';
  const canPause = phase === 'running' || phase === 'encrypting';
  const settingsLocked =
    phase === 'running' ||
    phase === 'encrypting' ||
    phase === 'paused' ||
    phase === 'completed' ||
    phase === 'decrypting' ||
    phase === 'recovered';

  return {
    phase,
    currentStep,
    encryptionProgress,
    decryptionProgress,
    files,
    educationalText,
    diskStatus,
    fileStatusText,
    ransomwareFamily,
    setRansomwareFamily,
    encryptionAlgorithm,
    setEncryptionAlgorithm,
    simulationSpeed,
    setSimulationSpeed,
    currentRansomwareExtension,
    currentRansomwareFamily,
    decryptionKey,
    keyInput,
    setKeyInput,
    countdownTime,
    ransomAmount,
    countdownExpired,
    showRansomNote,
    showRansomNotification,
    showDecryptionModal,
    showRansomButton,
    ransomNoteMinimized,
    previewFileName,
    previewContent,
    previewEncrypted,
    showPreview,
    toastMessage,
    toastVisible,
    lastDecryptError,
    copyFeedback,
    canStart,
    canPause,
    settingsLocked,
    enterWorkspace,
    startSimulation,
    pauseSimulation,
    resetSimulation,
    exitToLanding,
    minimizeRansomNote,
    openRansomNote,
    closeRansomNotification,
    openDecryptionModal,
    closeDecryptionModal,
    decryptFiles,
    copyKeyToClipboard,
    previewFile,
    closePreview,
    showToast,
    clearLastDecryptError: () => setLastDecryptError(null),
  };
};

export type RansomwareSimulatorApi = ReturnType<typeof useRansomwareSimulator>;
