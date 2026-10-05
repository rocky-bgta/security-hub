import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';

import { RANSOMWARE_FAMILY_NAMES } from '../data/ransomwareFamilies';
import {
  ENCRYPTION_ALGORITHM_OPTIONS,
  EncryptionAlgorithm,
  SIMULATION_SPEED_OPTIONS,
  SimulationSpeed,
} from '../types';
import SimulatorPanel from './SimulatorPanel';

interface IProps {
  ransomwareFamily: string;
  onFamilyChange: (value: string) => void;
  encryptionAlgorithm: EncryptionAlgorithm;
  onAlgorithmChange: (value: EncryptionAlgorithm) => void;
  simulationSpeed: SimulationSpeed;
  onSpeedChange: (value: SimulationSpeed) => void;
  locked: boolean;
}

const SimulationSettings = ({
  ransomwareFamily,
  onFamilyChange,
  encryptionAlgorithm,
  onAlgorithmChange,
  simulationSpeed,
  onSpeedChange,
  locked,
}: IProps) => {
  return (
    <SimulatorPanel title="Simulation Settings">
      <div className="content-space-y-2.5">
        <div data-walkthrough-id="family-select">
          <Label
            htmlFor="ransomware-family"
            className="content-mb-1 content-block content-text-xs content-text-white md:content-text-sm"
          >
            Ransomware Family
          </Label>
          <Select
            value={ransomwareFamily}
            onValueChange={onFamilyChange}
            disabled={locked}
          >
            <SelectTrigger id="ransomware-family" className="content-w-full">
              <SelectValue placeholder="Select family" />
            </SelectTrigger>
            <SelectContent className="content-max-h-72">
              {RANSOMWARE_FAMILY_NAMES.map(name => (
                <SelectItem key={name} value={name}>
                  {name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div data-walkthrough-id="algorithm-select">
          <Label
            htmlFor="encryption-algorithm"
            className="content-mb-1 content-block content-text-xs content-text-white md:content-text-sm"
          >
            Encryption Algorithm
          </Label>
          <Select
            value={encryptionAlgorithm}
            onValueChange={value =>
              onAlgorithmChange(value as EncryptionAlgorithm)
            }
            disabled={locked}
          >
            <SelectTrigger id="encryption-algorithm" className="content-w-full">
              <SelectValue placeholder="Select algorithm" />
            </SelectTrigger>
            <SelectContent>
              {ENCRYPTION_ALGORITHM_OPTIONS.map(option => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div data-walkthrough-id="speed-select">
          <Label
            htmlFor="simulation-speed"
            className="content-mb-1 content-block content-text-xs content-text-white md:content-text-sm"
          >
            Simulation Speed
          </Label>
          <Select
            value={simulationSpeed}
            onValueChange={value => onSpeedChange(value as SimulationSpeed)}
            disabled={locked}
          >
            <SelectTrigger id="simulation-speed" className="content-w-full">
              <SelectValue placeholder="Select speed" />
            </SelectTrigger>
            <SelectContent>
              {SIMULATION_SPEED_OPTIONS.map(option => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
    </SimulatorPanel>
  );
};

export default SimulationSettings;
