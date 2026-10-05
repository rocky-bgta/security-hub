import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  ArrowRight,
  Check,
  Copy,
  QrCode,
  Shield,
  Smartphone,
} from 'lucide-react';
import { useState } from 'react';

interface MFASetupStepProps {
  onNext: () => void;
  userType: 'msp' | 'admin';
}

const MFASetupStep = ({ onNext, userType }: MFASetupStepProps) => {
  const [verificationCode, setVerificationCode] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [secretKeyCopied, setSecretKeyCopied] = useState(false);

  const secretKey = 'JBSWY3DPEHPK3PXP'; // Example secret key
  const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=otpauth://totp/AspireASAT:user@example.com?secret=${secretKey}&issuer=AspireASAT`;

  const copySecretKey = async () => {
    try {
      await navigator.clipboard.writeText(secretKey);
      setSecretKeyCopied(true);
      //   toast({
      //     title: 'Secret Key Copied',
      //     description: 'Secret key has been copied to clipboard',
      //   });
      setTimeout(() => setSecretKeyCopied(false), 2000);
    } catch (err) {
      //   toast({
      //     title: 'Copy Failed',
      //     description: 'Unable to copy secret key',
      //     variant: 'destructive',
      //   });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!verificationCode || verificationCode.length !== 6) {
      //   toast({
      //     title: 'Invalid Code',
      //     description: 'Please enter a 6-digit verification code',
      //     variant: 'destructive',
      //   });
      return;
    }

    setIsLoading(true);

    setTimeout(() => {
      setIsLoading(false);
      onNext();
      //   toast({
      //     title: 'MFA Setup Complete',
      //     description:
      //       'Multi-factor authentication has been successfully configured',
      //   });
    }, 2000);
  };

  return (
    <div className="space-y-6">
      <div className="text-center">
        <div className="mb-6 flex items-center justify-center">
          <Shield className="size-8 text-white" />
        </div>
        <h3 className="mb-2 text-xl font-semibold">
          Set Up Multi-Factor Authentication
        </h3>
        <p className="text-muted-foreground">
          Secure your account with an additional layer of protection
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* QR Code Section */}
        <div className="space-y-4">
          <div className="mb-3 flex items-center gap-2">
            <QrCode className="size-5 text-primary" />
            <h4 className="font-medium">Scan QR Code</h4>
          </div>
          <div className="mx-auto w-fit rounded-lg bg-white p-4">
            <img src={qrCodeUrl} alt="MFA QR Code" className="size-48" />
          </div>
        </div>

        {/* Manual Setup Section */}
        <div className="space-y-4">
          <div className="mb-3 flex items-center gap-2">
            <Smartphone className="size-5 text-primary" />
            <h4 className="font-medium">Manual Setup</h4>
          </div>

          <div className="space-y-3">
            <p className="text-sm text-muted-foreground">
              Use Google Authenticator, Authy, or any TOTP app
            </p>

            <div className="space-y-2">
              <Label>Secret Key</Label>
              <div className="flex gap-2">
                <Input
                  value={secretKey}
                  readOnly
                  className="font-mono text-sm"
                />
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={copySecretKey}
                  className="px-3"
                >
                  {secretKeyCopied ? (
                    <Check className="size-4 text-primary" />
                  ) : (
                    <Copy className="size-4" />
                  )}
                </Button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="verificationCode">Verification Code</Label>
          <Input
            id="verificationCode"
            type="text"
            placeholder="Enter 6-digit code"
            value={verificationCode}
            onChange={e =>
              setVerificationCode(e.target.value.replace(/\D/g, '').slice(0, 6))
            }
            className="h-12 text-center text-lg tracking-widest"
            maxLength={6}
            required
          />
          <p className="text-sm text-muted-foreground">
            Enter the 6-digit code from your authenticator app
          </p>
        </div>

        <Button
          type="submit"
          className="h-12 w-full bg-primary transition-opacity hover:opacity-90"
          disabled={isLoading || verificationCode.length !== 6}
        >
          {isLoading ? (
            <div className="flex items-center gap-2">
              <div className="size-4 animate-spin rounded-full border-2 border-primary-foreground border-t-transparent" />
              Verifying...
            </div>
          ) : (
            <div className="flex items-center gap-2">
              Verify and Continue
              <ArrowRight className="size-4" />
            </div>
          )}
        </Button>
      </form>
    </div>
  );
};

export default MFASetupStep;
