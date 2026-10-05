import { HardDrive } from 'lucide-react';

import { cn } from 'utils/Helper';

import SimulatorPanel from './SimulatorPanel';

interface IProps {
  diskStatus: string;
  encryptionProgress: number;
}

const EncryptionDashboard = ({ diskStatus, encryptionProgress }: IProps) => {
  return (
    <SimulatorPanel
      title={
        <>
          <HardDrive className="content-size-4" />
          Encryption Dashboard
        </>
      }
      headerRight={
        <span className="content-shrink-0 content-text-xs content-text-muted-foreground">
          C: Drive
        </span>
      }
      data-walkthrough-id="encryption-dashboard"
    >
      <p className="content-mb-2 content-text-xs content-text-muted-foreground md:content-text-sm">
        Status:{' '}
        <span className="content-font-medium content-text-white">{diskStatus}</span>
      </p>

      <div className="content-mb-1 content-h-2 content-overflow-hidden content-rounded-full content-bg-steel-gray/60">
        <div
          className={cn(
            'content-h-full content-rounded-full content-transition-all content-duration-200',
            encryptionProgress >= 100
              ? 'content-bg-red-500'
              : 'content-bg-emerald-500',
          )}
          style={{ width: `${encryptionProgress}%` }}
        />
      </div>
      <p className="content-text-right content-text-xs content-font-medium content-text-white md:content-text-sm">
        {encryptionProgress}%
      </p>
    </SimulatorPanel>
  );
};

export default EncryptionDashboard;
