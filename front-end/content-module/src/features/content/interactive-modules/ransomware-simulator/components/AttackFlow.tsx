import { SIMULATION_STEPS } from '../data/simulationSteps';
import { cn } from 'utils/Helper';

import SimulatorPanel from './SimulatorPanel';

interface IProps {
  currentStep: number;
}

const AttackFlow = ({ currentStep }: IProps) => {
  return (
    <SimulatorPanel title="Attack Flow" data-walkthrough-id="attack-flow">
      <div className="content-grid content-grid-cols-2 content-gap-1.5 sm:content-grid-cols-3 lg:content-grid-cols-4 xl:content-grid-cols-7">
        {SIMULATION_STEPS.map((step, index) => {
          const isActive = index <= currentStep;
          const isCurrent = index === currentStep;
          return (
            <div
              key={step.id}
              title={step.description}
              className={cn(
                'content-rounded-md content-border content-p-2 content-transition-all',
                isCurrent &&
                  'content-border-emerald-500 content-bg-emerald-500/15 content-ring-1 content-ring-emerald-500/40',
                isActive &&
                  !isCurrent &&
                  'content-border-emerald-500/50 content-bg-emerald-500/10',
                !isActive &&
                  'content-border-white/10 content-bg-white/5 content-opacity-70',
              )}
            >
              <div className="content-mb-0.5 content-text-sm" aria-hidden>
                {step.emoji}
              </div>
              <p className="content-text-[11px] content-font-semibold content-leading-tight content-text-white md:content-text-xs">
                {step.name}
              </p>
            </div>
          );
        })}
      </div>
    </SimulatorPanel>
  );
};

export default AttackFlow;
