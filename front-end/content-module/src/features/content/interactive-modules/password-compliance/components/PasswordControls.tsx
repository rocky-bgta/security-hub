import { Label } from 'common/Label';
import { Switch } from 'common/Switch';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import {
  CHANGE_FREQUENCY_OPTIONS,
  ChangeFrequencyValue,
} from '../types';

interface IProps {
  password: string;
  onPasswordChange: (value: string) => void;
  usesMfa: boolean;
  onUsesMfaChange: (value: boolean) => void;
  changeFrequency: ChangeFrequencyValue;
  onChangeFrequencyChange: (value: ChangeFrequencyValue) => void;
}

const PasswordControls = ({
  password,
  onPasswordChange,
  usesMfa,
  onUsesMfaChange,
  changeFrequency,
  onChangeFrequencyChange,
}: IProps) => {
  return (
    <div className="content-space-y-3">
      <div>
        <Label
          htmlFor="password-compliance-input"
          className="content-mb-1 content-block content-text-sm content-font-medium content-text-white"
        >
          Enter Your Password
        </Label>
        <Input
          id="password-compliance-input"
          type="password"
          autoComplete="off"
          autoCorrect="off"
          autoCapitalize="off"
          spellCheck={false}
          placeholder="Type your password here..."
          value={password}
          onChange={e => onPasswordChange(e.target.value)}
          className="content-w-full"
          aria-describedby="password-compliance-privacy"
        />
      </div>

      <div className="content-grid content-grid-cols-1 content-gap-3 md:content-grid-cols-2">
        <div className="content-rounded-lg content-border content-border-white/10 content-bg-white/5 content-p-3">
          <div className="content-mb-1.5 content-flex content-items-center content-justify-between content-gap-3">
            <Label
              htmlFor="password-compliance-mfa"
              className="content-text-sm content-font-medium content-text-white"
            >
              Do you use MFA?
            </Label>
            <Switch
              id="password-compliance-mfa"
              checked={usesMfa}
              onCheckedChange={onUsesMfaChange}
              aria-describedby="password-compliance-mfa-help"
            />
          </div>
          <p
            id="password-compliance-mfa-help"
            className="content-text-sm content-text-muted-foreground"
          >
            Multi-Factor Authentication adds an extra layer of security
          </p>
        </div>

        <div className="content-rounded-lg content-border content-border-white/10 content-bg-white/5 content-p-3">
          <Label
            htmlFor="password-compliance-frequency"
            className="content-mb-1 content-block content-text-sm content-font-medium content-text-white"
          >
            How often do you change your password?
          </Label>
          <Select
            value={changeFrequency || undefined}
            onValueChange={value =>
              onChangeFrequencyChange(value as ChangeFrequencyValue)
            }
          >
            <SelectTrigger
              id="password-compliance-frequency"
              className="content-w-full"
            >
              <SelectValue placeholder="Select frequency" />
            </SelectTrigger>
            <SelectContent>
              {CHANGE_FREQUENCY_OPTIONS.filter(opt => opt.value !== '').map(
                option => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ),
              )}
            </SelectContent>
          </Select>
        </div>
      </div>
    </div>
  );
};

export default PasswordControls;
