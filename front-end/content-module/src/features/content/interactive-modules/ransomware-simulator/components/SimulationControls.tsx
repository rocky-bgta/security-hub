import { Button } from 'common/Button';

import SimulatorPanel from './SimulatorPanel';

interface IProps {
  canStart: boolean;
  canPause: boolean;
  showRansomButton: boolean;
  onStart: () => void;
  onPause: () => void;
  onReset: () => void;
  onShowRansom: () => void;
}

const SimulationControls = ({
  canStart,
  canPause,
  showRansomButton,
  onStart,
  onPause,
  onReset,
  onShowRansom,
}: IProps) => {
  return (
    <SimulatorPanel title="Simulation Control">
      <div className="content-flex content-flex-wrap content-gap-2">
        <Button
          type="button"
          size="sm"
          onClick={onStart}
          disabled={!canStart}
          data-walkthrough-id="start-btn"
          className="content-bg-emerald-600 hover:content-bg-emerald-600/90"
        >
          Start
        </Button>
        <Button
          type="button"
          size="sm"
          variant="secondary"
          onClick={onPause}
          disabled={!canPause}
          className="content-bg-amber-600 content-text-white hover:content-bg-amber-600/90"
        >
          Pause
        </Button>
        <Button type="button" size="sm" variant="destructive" onClick={onReset}>
          Reset
        </Button>
        {showRansomButton ? (
          <Button
            type="button"
            size="sm"
            variant="outline"
            onClick={onShowRansom}
            data-walkthrough-id="show-ransom-btn"
          >
            Show Ransom Note
          </Button>
        ) : null}
      </div>
    </SimulatorPanel>
  );
};

export default SimulationControls;
