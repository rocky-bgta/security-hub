import { Monitor, Cpu, MemoryStick, HardDrive } from 'lucide-react';

import SimulatorPanel from './SimulatorPanel';

const SystemInfoCard = () => {
  const rows = [
    { icon: Monitor, label: 'Operating System', value: 'Windows 11 Pro' },
    { icon: Cpu, label: 'CPU', value: 'Intel Core i7-12700K' },
    { icon: MemoryStick, label: 'Memory', value: '32 GB DDR4' },
    { icon: HardDrive, label: 'Disk', value: 'C: (NVMe SSD)' },
  ];

  return (
    <SimulatorPanel title="System Information">
      <dl className="content-space-y-1.5">
        {rows.map(row => (
          <div
            key={row.label}
            className="content-flex content-items-center content-justify-between content-gap-3"
          >
            <dt className="content-flex content-items-center content-gap-2 content-text-xs content-text-muted-foreground md:content-text-sm">
              <row.icon className="content-size-3.5" />
              {row.label}
            </dt>
            <dd className="content-text-xs content-font-medium content-text-white md:content-text-sm">
              {row.value}
            </dd>
          </div>
        ))}
      </dl>
    </SimulatorPanel>
  );
};

export default SystemInfoCard;
