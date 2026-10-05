import { ChangeEvent, forwardRef, useImperativeHandle } from 'react';

import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { IDataValidationHandle, IQuizFields } from 'models/Content';

interface IProps {
  data: IQuizFields;
  updateData: (data: IQuizFields) => void;
}

const RansomwareSimulatorSettings = forwardRef<IDataValidationHandle, IProps>(
  ({ data, updateData }, ref) => {
    useImperativeHandle(ref, () => ({
      validateData: () => ({ success: true }),
    }));

    const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
      updateData({
        ...data,
        question: e.target.value,
      });
    };

    return (
      <div className="content-mt-5 content-space-y-2">
        <Label
          htmlFor="ransomware-simulator-instructions"
          className="content-text-sm content-font-medium content-text-white"
        >
          Instructions (optional)
        </Label>
        <Input
          id="ransomware-simulator-instructions"
          type="text"
          placeholder="Shown as the subtitle under the module title"
          value={data.question ?? ''}
          onChange={handleChange}
          className="content-w-full"
        />
        <p className="content-text-sm content-text-muted-foreground">
          Learners will interact with the Ransomware Simulator. No additional
          quiz configuration is required.
        </p>
      </div>
    );
  },
);

RansomwareSimulatorSettings.displayName = 'RansomwareSimulatorSettings';

export default RansomwareSimulatorSettings;
