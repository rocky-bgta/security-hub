import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import useStore from 'hooks/UseStore';
import { Info, Plus, Send, X } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

interface IProps {
  data: CompletionDaysState;
  isLoading: boolean;
  onUpdate: (data: any) => void;
  onSubmit?: () => void;
  onPrevious: () => void;
}

type EmailSection = 'secondaryEmails' | 'thirdLevelEmails' | 'fourthHREmails';

interface CompletionDaysState {
  durationUnit: string;
  durationValue: number;
  enableFirstUserNotificationEmail: boolean;
  secondaryEmails: string[];
  thirdLevelEmails: string[];
  fourthHREmails: string[];
}

interface EmailSectionConfig {
  key: EmailSection;
  label: string;
  id: string;
  info: string;
}

interface EmailSectionProps {
  config: EmailSectionConfig;
  emails: string[];
  onAdd: () => void;
  onRemove: (index: number) => void;
  onUpdate: (index: number, value: string) => void;
}

const EMAIL_SECTIONS: EmailSectionConfig[] = [
  {
    key: 'secondaryEmails',
    label: 'Second Manager Notification Emails',
    id: 'manager-email',
    info: 'Second Manager Notification Emails are the email addresses that will receive a notification if a user has not completed the assigned product within 50% of the Completion Due Days. This notification informs the second manager that the user has not yet completed the product.',
  },
  {
    key: 'thirdLevelEmails',
    label: 'Third C-Level Notification Emails',
    id: 'thirdLevelEmails',
    info: 'Third C-Level Notification Emails are the email addresses that will receive a notification if a user has not completed the assigned product within 75% of the Completion Due Days. This helps escalate the reminder to the third-level manager.',
  },
  {
    key: 'fourthHREmails',
    label: 'Fourth HR Notification Emails',
    id: 'hr-email',
    info: 'Fourth HR Notification Emails are the email addresses that will receive a notification if the full Completion Due Days have passed and the user still has not completed the assigned product.',
  },
];

const INITIAL_STATE: CompletionDaysState = {
  durationUnit: '',
  durationValue: 0,
  enableFirstUserNotificationEmail: true,
  secondaryEmails: [''],
  thirdLevelEmails: [''],
  fourthHREmails: [''],
};

const EmailSection = ({
  config,
  emails,
  onAdd,
  onRemove,
  onUpdate,
}: EmailSectionProps) => {
  return (
    <div className="home-mb-6">
      <div className="home-mb-3 home-flex home-items-center home-justify-between">
        <Label
          htmlFor={config.id}
          className="home-flex home-items-center home-gap-2"
        >
          {config.label}{' '}
          <span title={config.info}>
            <Info className="home-size-4" />
          </span>
        </Label>
        <Button
          variant="outline"
          size="sm"
          onClick={onAdd}
          className="home-ml-2"
        >
          <Plus className="home-mr-1 home-size-4" />
          Add Email
        </Button>
      </div>

      <div className="home-space-y-3">
        {emails.map((email, index) => (
          <div key={index} className="home-flex home-items-center home-gap-2">
            <div className="home-flex-1">
              <Input
                type="email"
                placeholder={`example@abc.com${index > 0 ? ` ${index + 1}` : ''}`}
                value={email}
                onChange={e => onUpdate(index, e.target.value)}
              />
            </div>
            {emails.length > 0 && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => onRemove(index)}
                className="!home-text-vibrant-red"
              >
                <X className="home-size-4" />
              </Button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

const Step3 = ({ data, onUpdate, onSubmit, onPrevious, isLoading }: IProps) => {
  const [completionDays, setCompletionDays] = useState<CompletionDaysState>(
    data || INITIAL_STATE,
  );

  const { userInfo } = useStore();

  useEffect(() => {
    onUpdate(completionDays);
  }, [completionDays, onUpdate]);

  const addEmailInput = useCallback((section: EmailSection) => {
    setCompletionDays(prev => ({
      ...prev,
      [section]: [...prev[section], ''],
    }));
  }, []);

  const removeEmailInput = useCallback(
    (section: EmailSection, index: number) => {
      setCompletionDays(prev => ({
        ...prev,
        [section]: prev[section].filter((_, i) => i !== index),
      }));
    },
    [],
  );

  const updateEmailInput = useCallback(
    (section: EmailSection, index: number, value: string) => {
      setCompletionDays(prev => ({
        ...prev,
        [section]: prev[section].map((email, i) =>
          i === index ? value : email,
        ),
      }));
    },
    [],
  );

  const validateEmailDomain = (value: string) => {
    const domain = value.split('@')[1];
    if (domain?.toLowerCase() !== userInfo?.email.split('@')[1].toLowerCase()) {
      return true;
    }

    return false;
  };

  const validateForm = () => {
    const errors: string[] = [];

    if (!completionDays.enableFirstUserNotificationEmail) {
      errors.push('First User Notification Email is required');
      return errors;
    }

    for (const section of EMAIL_SECTIONS) {
      let hasError = false;
      completionDays[section.key].forEach(value => {
        hasError = hasError || validateEmailDomain(value);
      });
      if (hasError) {
        errors.push(`Email domain must match ${userInfo?.email.split('@')[1]}`);
        return errors;
      }
    }

    if (!completionDays.durationUnit) {
      errors.push('Duration Unit is required');
      return errors;
    }

    if (
      completionDays.durationUnit === 'DAYS' &&
      completionDays.durationValue > 30
    ) {
      errors.push('Maximum duration for DAYS is 30');
      return errors;
    }

    if (
      completionDays.durationUnit === 'WEEKS' &&
      completionDays.durationValue > 4
    ) {
      errors.push('Duration Value must be less than or equal to 4 for Weeks');
      return errors;
    }

    if (
      completionDays.durationUnit === 'MONTHS' &&
      completionDays.durationValue !== 1
    ) {
      errors.push('Duration Value must be equal to 1 for Months');
      return errors;
    }

    if (!completionDays.durationValue) {
      errors.push('Duration Value is required');
      return errors;
    }

    return errors;
  };

  const errors = validateForm();
  const handleSendEmail = () => {
    if (errors.length > 0) {
      toast.error(errors.join(', '));
      return;
    }
    onSubmit?.();
  };

  const handleDurationUnitChange = useCallback((value: string) => {
    setCompletionDays(prev => ({ ...prev, durationUnit: value }));
  }, []);

  const handleDurationValueChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = parseInt(e.target.value) || 0;
      setCompletionDays(prev => ({ ...prev, durationValue: value }));
    },
    [],
  );

  return (
    <div className="home-grid home-grid-cols-2 home-gap-8 lg:home-grid-cols-5">
      <div className="home-space-y-6 lg:home-col-span-3">
        {/* First User Notification */}
        <div className="home-flex home-items-center home-gap-2">
          <Label className="home-flex home-items-center home-gap-2">
            First User Notification Email{' '}
            <span title="First User Notification Email is the email address that will receive the notification when the complete the assign process. This email will be used to send the notification email to the user.">
              <Info className="home-size-4" />
            </span>
          </Label>
          <div className="home-relative">
            <Checkbox
              checked={completionDays.enableFirstUserNotificationEmail}
            />
          </div>
        </div>

        {/* Email Sections */}
        {EMAIL_SECTIONS.map(section => (
          <EmailSection
            key={section.key}
            config={section}
            emails={completionDays[section.key]}
            onAdd={() => addEmailInput(section.key)}
            onRemove={index => removeEmailInput(section.key, index)}
            onUpdate={(index, value) =>
              updateEmailInput(section.key, index, value)
            }
          />
        ))}
      </div>

      {/* Completion Due Days */}
      <div className="home-flex home-flex-col lg:home-col-span-2">
        <div className="home-space-y-1">
          <Label>Completion Due Days</Label>
          <div className="home-flex home-gap-3">
            <Select
              value={completionDays.durationUnit}
              onValueChange={handleDurationUnitChange}
            >
              <SelectTrigger className="home-w-52">
                <SelectValue placeholder="Select Duration" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="DAYS">Day</SelectItem>
                <SelectItem value="WEEKS">Week</SelectItem>
                <SelectItem value="MONTHS">Month</SelectItem>
              </SelectContent>
            </Select>
            <Input
              type="number"
              placeholder="eg: 1"
              min="1"
              value={completionDays.durationValue || ''}
              onChange={handleDurationValueChange}
              className="home-w-1/2"
            />
          </div>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="home-col-span-full home-mt-6 home-flex home-justify-end home-gap-4">
        <Button
          variant="outline"
          onClick={onPrevious}
          className="home-px-8 home-py-2"
        >
          Previous Step
        </Button>
        <Button onClick={handleSendEmail} disabled={isLoading}>
          {isLoading ? 'Submitting...' : 'Submit And Send Email'}
          <Send className="home-ml-2 home-size-4" />
        </Button>
      </div>
    </div>
  );
};

export default Step3;
