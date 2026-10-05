import type { RansomwareSimulatorApi } from '../hooks/useRansomwareSimulator';
import AttackFlow from './AttackFlow';
import EducationalInfo from './EducationalInfo';
import EncryptionDashboard from './EncryptionDashboard';
import FileSystemPanel from './FileSystemPanel';
import SimulationControls from './SimulationControls';
import SimulationSettings from './SimulationSettings';
import SystemInfoCard from './SystemInfoCard';

interface IProps {
  sim: RansomwareSimulatorApi;
}

const SimulationWorkspace = ({ sim }: IProps) => {
  return (
    <div className="content-space-y-3">
      <div className="content-grid content-grid-cols-1 content-gap-3 md:content-grid-cols-2">
        <SystemInfoCard />
        <EncryptionDashboard
          diskStatus={sim.diskStatus}
          encryptionProgress={sim.encryptionProgress}
        />
        <FileSystemPanel
          files={sim.files}
          fileStatusText={sim.fileStatusText}
          onPreview={sim.previewFile}
        />
        <div className="content-space-y-3">
          <SimulationSettings
            ransomwareFamily={sim.ransomwareFamily}
            onFamilyChange={sim.setRansomwareFamily}
            encryptionAlgorithm={sim.encryptionAlgorithm}
            onAlgorithmChange={sim.setEncryptionAlgorithm}
            simulationSpeed={sim.simulationSpeed}
            onSpeedChange={sim.setSimulationSpeed}
            locked={sim.settingsLocked}
          />
          <SimulationControls
            canStart={sim.canStart}
            canPause={sim.canPause}
            showRansomButton={sim.showRansomButton}
            onStart={sim.startSimulation}
            onPause={sim.pauseSimulation}
            onReset={sim.resetSimulation}
            onShowRansom={sim.openRansomNote}
          />
        </div>
      </div>

      <EducationalInfo text={sim.educationalText} />
      <AttackFlow currentStep={sim.currentStep} />
    </div>
  );
};

export default SimulationWorkspace;
