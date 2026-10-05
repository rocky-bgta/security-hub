import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { ArrowRight, Mail } from 'lucide-react';
import { useState } from 'react';

interface EmailCaptureStepProps {
  onNext: (email: string) => void;
}

const EmailCaptureStep = ({ onNext }: EmailCaptureStepProps) => {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!email) {
      console.log('Please enter an email address.');

      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      console.log('Please enter a valid email address.');

      return;
    }

    setIsLoading(true);

    // Simulate API call
    setTimeout(() => {
      setIsLoading(false);
      onNext(email);

      console.log('Email captured:', email);
    }, 2000);
  };

  return (
    <div className="space-y-6">
      <div className="text-center">
        <div className="mb-6 flex items-center justify-center">
          <Mail className="size-8 text-white" />
        </div>
        <h3 className="mb-2 text-xl font-semibold">Enter Your Email</h3>
        <p className="text-muted-foreground">
          We'll send you a verification link to get started
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="email">Email Address</Label>
          <Input
            id="email"
            type="email"
            placeholder="Enter email address"
            value={email}
            onChange={e => setEmail(e.target.value)}
            className="h-12"
            required
          />
        </div>

        <Button
          type="submit"
          className="h-12 w-full bg-primary text-white transition-opacity hover:opacity-90"
          disabled={isLoading}
        >
          {isLoading ? (
            <div className="flex items-center gap-2">
              <div className="size-4 animate-spin rounded-full border-2 border-primary-foreground border-t-transparent" />
              Sending Verification Email...
            </div>
          ) : (
            <div className="flex items-center gap-2">
              Send Verification Email
              <ArrowRight className="size-4" />
            </div>
          )}
        </Button>
      </form>
    </div>
  );
};

export default EmailCaptureStep;
