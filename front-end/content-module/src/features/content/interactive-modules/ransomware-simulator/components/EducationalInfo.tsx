import { Info } from 'lucide-react';

import SimulatorPanel from './SimulatorPanel';

interface IProps {
  text: string;
}

const EducationalInfo = ({ text }: IProps) => {
  return (
    <SimulatorPanel
      title={
        <span className="content-flex content-items-center content-gap-1.5">
          <Info className="content-size-3.5 content-text-primary" />
          Educational Information
        </span>
      }
    >
      <p className="content-text-sm content-leading-snug content-text-muted-foreground">
        {text}
      </p>
    </SimulatorPanel>
  );
};

export default EducationalInfo;
