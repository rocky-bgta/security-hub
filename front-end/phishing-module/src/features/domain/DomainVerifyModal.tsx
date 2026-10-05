import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { ArrowLeft, Mail, RefreshCw, ShieldCheck } from 'lucide-react';
import {
  useEffect,
  useState,
  type ClipboardEvent,
  type KeyboardEvent,
} from 'react';
import { useForm } from 'react-hook-form';
import {
  DefaultDomainVerificationValues,
  DefaultGenerateVerificationValues,
  DomainVerificationSchema,
  EMAIL_ADDRESS_MAX_LENGTH,
  EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE,
  EMAIL_ADDRESS_MIN_LENGTH,
  EMAIL_ADDRESS_MIN_LENGTH_ERROR_MESSAGE,
  GenerateVerificationSchema,
  TDomainVerificationForm,
  TGenerateVerificationForm,
} from 'schemas/DomainSchema';
import { VERIFICATION_CODE_EXPIRY_MINUTES } from 'utils/Constants';
import { extractDomainFromEmail } from 'utils/Helper';

interface DomainVerifyModalProps {
  isOpen: boolean;
  onClose: () => void;
  onGenerateCode: (data: TGenerateVerificationForm) => Promise<boolean>;
  onVerify: (data: TDomainVerificationForm) => Promise<boolean>;
  onResendCode: (data: TGenerateVerificationForm) => Promise<boolean>;
}

type VerificationStep = 'email' | 'code';

/**
 * Two-step modal for domain verification
 * Step 1: Enter email address to receive verification code
 * Step 2: Enter verification code to verify domain
 */
const DomainVerifyModal = ({
  isOpen,
  onClose,
  onGenerateCode,
  onVerify,
  onResendCode,
}: DomainVerifyModalProps) => {
  const [step, setStep] = useState<VerificationStep>('email');
  const [submitting, setSubmitting] = useState(false);
  const [emailAddress, setEmailAddress] = useState('');
  const [timeRemaining, setTimeRemaining] = useState(
    VERIFICATION_CODE_EXPIRY_MINUTES * 60,
  );
  const [timerActive, setTimerActive] = useState(false);
  /** Shown when the user tries to enter more than the max length (typing or paste). */
  const [emailLengthCapMessage, setEmailLengthCapMessage] = useState<
    string | null
  >(null);

  // Form for step 1 (email input)
  const emailForm = useForm<TGenerateVerificationForm>({
    resolver: zodResolver(GenerateVerificationSchema),
    defaultValues: DefaultGenerateVerificationValues,
    reValidateMode: 'onChange',
  });

  // Form for step 2 (verification code)
  const codeForm = useForm<TDomainVerificationForm>({
    resolver: zodResolver(DomainVerificationSchema),
    defaultValues: DefaultDomainVerificationValues,
  });

  // Timer countdown effect
  useEffect(() => {
    let interval: ReturnType<typeof setInterval> | null = null;

    if (timerActive && timeRemaining > 0) {
      interval = setInterval(() => {
        setTimeRemaining(prev => prev - 1);
      }, 1000);
    } else if (timeRemaining === 0) {
      setTimerActive(false);
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [timerActive, timeRemaining]);

  // Reset modal state when closed
  useEffect(() => {
    if (!isOpen) {
      setStep('email');
      setEmailAddress('');
      setTimeRemaining(VERIFICATION_CODE_EXPIRY_MINUTES * 60);
      setTimerActive(false);
      setEmailLengthCapMessage(null);
      emailForm.reset();
      codeForm.reset();
    }
  }, [isOpen, emailForm, codeForm]);

  // Format time as MM:SS
  const formatTime = (seconds: number): string => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  // Handle step 1 submission (generate verification code)
  const handleGenerateCode = async (data: TGenerateVerificationForm) => {
    setSubmitting(true);
    const success = await onGenerateCode(data);
    setSubmitting(false);

    if (success) {
      setEmailAddress(data.emailAddress);
      codeForm.setValue('emailAddress', data.emailAddress);
      setStep('code');
      setTimeRemaining(VERIFICATION_CODE_EXPIRY_MINUTES * 60);
      setTimerActive(true);
    }
  };

  // Handle step 2 submission (verify domain)
  const handleVerify = async (data: TDomainVerificationForm) => {
    setSubmitting(true);
    const success = await onVerify(data);
    setSubmitting(false);

    if (success) {
      onClose();
    }
  };

  // Handle resend code
  const handleResendCode = async () => {
    if (submitting) return;
    setSubmitting(true);
    const success = await onResendCode({ emailAddress });
    setSubmitting(false);

    if (success) {
      setTimeRemaining(VERIFICATION_CODE_EXPIRY_MINUTES * 60);
      setTimerActive(true);
    }
  };

  // Go back to step 1
  const handleBack = () => {
    setStep('email');
    setTimerActive(false);
  };

  const domainFromEmail = extractDomainFromEmail(emailAddress);

  const watchedEmailAddress = emailForm.watch('emailAddress');
  const trimmedEmailLen = (watchedEmailAddress ?? '').trim().length;
  const meetsEmailLengthCriteria =
    trimmedEmailLen >= EMAIL_ADDRESS_MIN_LENGTH &&
    trimmedEmailLen <= EMAIL_ADDRESS_MAX_LENGTH;

  const emailFieldError = emailForm.formState.errors.emailAddress;
  const isZodEmailLengthError =
    emailFieldError?.message === EMAIL_ADDRESS_MIN_LENGTH_ERROR_MESSAGE ||
    emailFieldError?.message === EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE;

  const showEmailLengthCriteriaHint =
    Boolean(emailLengthCapMessage) ||
    (!meetsEmailLengthCriteria && isZodEmailLengthError);

  const handleEmailKeyDown = (e: KeyboardEvent<HTMLInputElement>): void => {
    const target = e.currentTarget;
    const { value, selectionStart, selectionEnd } = target;
    const start = selectionStart ?? 0;
    const end = selectionEnd ?? value.length;
    const selectedLen = end - start;

    if (e.ctrlKey || e.metaKey || e.altKey) return;
    if (
      e.key === 'Backspace' ||
      e.key === 'Delete' ||
      e.key.startsWith('Arrow') ||
      e.key === 'Tab' ||
      e.key === 'Home' ||
      e.key === 'End'
    ) {
      return;
    }

    if (e.key.length !== 1) return;

    const newLength = value.length - selectedLen + 1;
    if (newLength > EMAIL_ADDRESS_MAX_LENGTH) {
      e.preventDefault();
      setEmailLengthCapMessage(EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE);
    }
  };

  const handleEmailPaste = (e: ClipboardEvent<HTMLInputElement>): void => {
    const target = e.currentTarget;
    const { value, selectionStart, selectionEnd } = target;
    const start = selectionStart ?? 0;
    const end = selectionEnd ?? value.length;
    const selectedLen = end - start;
    const pasted = e.clipboardData.getData('text');
    const newLength = value.length - selectedLen + pasted.length;
    if (newLength > EMAIL_ADDRESS_MAX_LENGTH) {
      setEmailLengthCapMessage(EMAIL_ADDRESS_MAX_LENGTH_ERROR_MESSAGE);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <ShieldCheck className="size-5 text-primary" />
            {step === 'email' ? 'Verify Domain' : 'Enter Verification Code'}
          </DialogTitle>
          <DialogDescription>
            {step === 'email'
              ? 'Enter an email address that belongs to the domain you want to verify.'
              : `A verification code has been sent to ${emailAddress}`}
          </DialogDescription>
        </DialogHeader>

        {/* Step 1: Email Input */}
        {step === 'email' && (
          <form onSubmit={emailForm.handleSubmit(handleGenerateCode)}>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="emailAddress">Email Address</Label>
                <div className="relative">
                  <Mail className="absolute left-3 top-3 size-4 text-muted-foreground" />
                  <Input
                    id="emailAddress"
                    placeholder="admin@yourdomain.com"
                    className="pl-9"
                    maxLength={EMAIL_ADDRESS_MAX_LENGTH}
                    onKeyDown={handleEmailKeyDown}
                    onPaste={handleEmailPaste}
                    {...emailForm.register('emailAddress', {
                      onChange: e => {
                        if (e.target.value.length < EMAIL_ADDRESS_MAX_LENGTH) {
                          setEmailLengthCapMessage(null);
                        }
                      },
                    })}
                  />
                </div>
                {showEmailLengthCriteriaHint && (
                  <p className="text-xs text-vibrant-red">
                    Minimum {EMAIL_ADDRESS_MIN_LENGTH} characters, maximum{' '}
                    {EMAIL_ADDRESS_MAX_LENGTH} characters.
                  </p>
                )}
                {emailForm.formState.errors.emailAddress && (
                  <p className="text-sm text-vibrant-red">
                    {emailForm.formState.errors.emailAddress.message}
                  </p>
                )}
              </div>

              <p className="text-sm text-muted-foreground">
                We will send a one-time verification code to this email address.
                The code will expire in {VERIFICATION_CODE_EXPIRY_MINUTES}{' '}
                minutes.
              </p>
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={onClose}>
                Cancel
              </Button>
              <Button type="submit" disabled={submitting}>
                {submitting ? (
                  <>
                    <RefreshCw className="mr-2 size-4 animate-spin" />
                    Sending...
                  </>
                ) : (
                  <>
                    <Mail className="mr-2 size-4" />
                    Send Verification Email
                  </>
                )}
              </Button>
            </DialogFooter>
          </form>
        )}

        {/* Step 2: Verification Code */}
        {step === 'code' && (
          <form onSubmit={codeForm.handleSubmit(handleVerify)}>
            <div className="space-y-4 py-4">
              {/* Display domain being verified */}
              <div className="rounded-md p-3">
                <p className="text-sm text-foreground">Verifying domain:</p>
                <p className="font-medium text-primary">{domainFromEmail}</p>
              </div>

              {/* Verification code input */}
              <div className="space-y-2">
                <Label htmlFor="verificationCode">Verification Code</Label>
                <Input
                  id="verificationCode"
                  placeholder="Enter 6-8 digit code"
                  maxLength={8}
                  {...codeForm.register('verificationCode')}
                />
                {codeForm.formState.errors.verificationCode && (
                  <p className="text-sm text-vibrant-red">
                    {codeForm.formState.errors.verificationCode.message}
                  </p>
                )}
              </div>

              {/* Timer and resend */}
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">
                  Code expires in:{' '}
                  <span
                    className={
                      timeRemaining < 60 ? 'text-vibrant-red' : 'text-primary'
                    }
                  >
                    {formatTime(timeRemaining)}
                  </span>
                </span>
                <Button
                  type="button"
                  variant="link"
                  size="sm"
                  onClick={handleResendCode}
                  disabled={
                    submitting ||
                    timeRemaining > (VERIFICATION_CODE_EXPIRY_MINUTES - 1) * 60
                  }
                  className="text-primary"
                >
                  Resend Code
                </Button>
              </div>
            </div>

            <DialogFooter className="flex-col gap-2 sm:flex-row">
              <Button
                type="button"
                variant="outline"
                onClick={handleBack}
                className="w-full sm:w-auto"
              >
                <ArrowLeft className="mr-2 size-4" />
                Back
              </Button>
              <Button
                type="submit"
                disabled={submitting}
                className="w-full sm:w-auto"
              >
                {submitting ? (
                  <>
                    <RefreshCw className="mr-2 size-4 animate-spin" />
                    Verifying...
                  </>
                ) : (
                  <>
                    <ShieldCheck className="mr-2 size-4" />
                    Verify Domain
                  </>
                )}
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default DomainVerifyModal;
