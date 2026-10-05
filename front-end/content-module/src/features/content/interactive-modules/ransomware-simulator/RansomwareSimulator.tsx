import { ReactNode, useCallback } from 'react';

import { cn } from 'utils/Helper';

import DecryptionModal from './components/DecryptionModal';
import FilePreviewModal from './components/FilePreviewModal';
import LandingPage from './components/LandingPage';
import RansomNote from './components/RansomNote';
import RansomNotification from './components/RansomNotification';
import SimulationWorkspace from './components/SimulationWorkspace';
import SimulatorToast from './components/SimulatorToast';
import WalkthroughCoach from './components/walkthrough/WalkthroughCoach';
import WalkthroughSpotlight from './components/walkthrough/WalkthroughSpotlight';
import { useRansomwareSimulator } from './hooks/useRansomwareSimulator';
import { useWalkthrough } from './hooks/useWalkthrough';

export interface RansomwareSimulatorProps {
  title?: string;
  description?: string;
  className?: string;
  onGuideComplete?: () => void;
  onGuideRestart?: () => void;
}

const SimulatorChrome = ({ children }: { children: ReactNode }) => (
  <div className="content-pointer-events-none content-absolute content-inset-0 content-z-[2000] content-overflow-hidden">
    <div className="content-absolute content-bottom-3 content-right-3 content-flex content-max-w-[calc(100%-1.5rem)] content-flex-col content-items-end content-gap-2">
      {children}
    </div>
  </div>
);

const RansomwareSimulator = ({
  title = 'Ransomware Simulator',
  description = 'Learn how ransomware attacks unfold — and why backups beat ransom payments',
  className,
  onGuideComplete,
  onGuideRestart,
}: RansomwareSimulatorProps) => {
  const sim = useRansomwareSimulator();
  const walkthrough = useWalkthrough({
    phase: sim.phase,
    showDecryptionModal: sim.showDecryptionModal,
  });

  const handleEnterWorkspace = useCallback(() => {
    sim.enterWorkspace();
    walkthrough.notifyAction('enterWorkspace');
  }, [sim, walkthrough]);

  const handleStart = useCallback(() => {
    sim.startSimulation();
    walkthrough.notifyAction('startSimulation');
  }, [sim, walkthrough]);

  const handleOpenDecrypt = useCallback(() => {
    sim.openDecryptionModal();
    walkthrough.notifyAction('openDecryption');
  }, [sim, walkthrough]);

  const handleDecrypt = useCallback(() => {
    const key = sim.keyInput.trim();
    if (!key) {
      walkthrough.notifyDecryptError('empty');
    } else if (key !== sim.decryptionKey) {
      walkthrough.notifyDecryptError('invalid');
    }
    const ok = sim.decryptFiles();
    if (ok) {
      walkthrough.notifyAction('decryptSuccess');
      onGuideComplete?.();
    }
  }, [sim, walkthrough, onGuideComplete]);

  const handleRestartGuide = useCallback(() => {
    sim.exitToLanding({ silent: true });
    walkthrough.restartTour();
    onGuideRestart?.();
  }, [sim, walkthrough, onGuideRestart]);

  // Wire start through workspace by wrapping sim for controls
  const workspaceSim = {
    ...sim,
    startSimulation: handleStart,
    openDecryptionModal: handleOpenDecrypt,
  };

  const spotlightTargetId = (() => {
    const target = walkthrough.currentStep?.targetId ?? null;
    if (
      target === 'decrypt-cta' &&
      !sim.showRansomNote &&
      sim.showRansomButton
    ) {
      return 'show-ransom-btn' as const;
    }
    return target;
  })();

  return (
    <div
      className={cn(
        'content-relative content-mx-auto content-h-full content-min-h-0 content-w-full content-max-w-7xl content-overflow-hidden',
        className,
      )}
      data-ransomware-simulator-root
    >
      <div
        className="content-h-full content-overflow-y-auto content-p-3 md:content-p-4"
        data-ransomware-simulator-scroll
      >
        <header className="content-mb-3 content-text-center">
          <h1 className="content-mb-1 content-text-xl content-font-bold content-text-white md:content-text-2xl">
            {title}
          </h1>
          {description ? (
            <p className="content-text-sm content-leading-snug content-text-muted-foreground">
              {description}
            </p>
          ) : null}
        </header>

        {sim.phase === 'landing' ? (
          <LandingPage onStart={handleEnterWorkspace} />
        ) : (
          <SimulationWorkspace sim={workspaceSim} />
        )}
      </div>

      <SimulatorChrome>
        <RansomNotification
          open={sim.showRansomNotification}
          onOpen={sim.openRansomNote}
          onClose={sim.closeRansomNotification}
        />
        <SimulatorToast message={sim.toastMessage} visible={sim.toastVisible} />
        <WalkthroughCoach
          walkthrough={walkthrough}
          targetId={spotlightTargetId}
          onRestart={handleRestartGuide}
        />
      </SimulatorChrome>

      <div
        className="content-pointer-events-none content-absolute content-inset-0 content-z-[2100] content-overflow-hidden"
        data-ransomware-simulator-overlay
      >
        <RansomNote
          open={sim.showRansomNote}
          extension={sim.currentRansomwareExtension}
          ransomAmount={sim.ransomAmount}
          countdownTime={sim.countdownTime}
          countdownExpired={sim.countdownExpired}
          onMinimize={sim.minimizeRansomNote}
          onDecryptNow={handleOpenDecrypt}
        />

        <DecryptionModal
          open={sim.showDecryptionModal}
          decryptionKey={sim.decryptionKey}
          keyInput={sim.keyInput}
          onKeyInputChange={sim.setKeyInput}
          decryptionProgress={sim.decryptionProgress}
          isDecrypting={sim.phase === 'decrypting'}
          copyFeedback={sim.copyFeedback}
          onCopyKey={sim.copyKeyToClipboard}
          onDecrypt={handleDecrypt}
          onClose={sim.closeDecryptionModal}
        />

        <FilePreviewModal
          open={sim.showPreview}
          fileName={sim.previewFileName}
          content={sim.previewContent}
          encrypted={sim.previewEncrypted}
          ransomwareFamily={sim.currentRansomwareFamily}
          encryptionAlgorithm={sim.encryptionAlgorithm}
          onClose={sim.closePreview}
        />

        <WalkthroughSpotlight
          active={walkthrough.guideActive}
          targetId={spotlightTargetId}
        />
      </div>
    </div>
  );
};

export default RansomwareSimulator;
