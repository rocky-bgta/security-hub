import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { ArrowRight, Check, Lock, X } from 'lucide-react';
import { useState } from 'react';
import { cn } from 'utils/Helper';

interface PasswordSetupStepProps {
  onNext: (password: string) => void;
  email: string;
}

const PasswordSetupStep = ({ onNext, email }: PasswordSetupStepProps) => {
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const passwordRequirements = [
    { label: 'At least 8 characters', test: (pwd: string) => pwd.length >= 8 },
    { label: 'One uppercase letter', test: (pwd: string) => /[A-Z]/.test(pwd) },
    { label: 'One lowercase letter', test: (pwd: string) => /[a-z]/.test(pwd) },
    { label: 'One number', test: (pwd: string) => /\d/.test(pwd) },
    {
      label: 'One special character',
      test: (pwd: string) => /[!@#$%^&*(),.?":{}|<>]/.test(pwd),
    },
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!password || !confirmPassword) {
      //   toast({
      //     title: 'Required Fields',
      //     description: 'Please fill in all password fields',
      //     variant: 'destructive',
      //   });

      return;
    }

    if (password !== confirmPassword) {
      //   toast({
      //     title: "Passwords Don't Match",
      //     description: 'Please ensure both passwords are identical',
      //     variant: 'destructive',
      //   });
      return;
    }

    const isValid = passwordRequirements.every(req => req.test(password));
    if (!isValid) {
      //   toast({
      //     title: 'Password Requirements',
      //     description: 'Please ensure your password meets all requirements',
      //     variant: 'destructive',
      //   });
      return;
    }

    setIsLoading(true);

    setTimeout(() => {
      setIsLoading(false);
      onNext(password);
      //   toast({
      //     title: 'Password Set Successfully',
      //     description: 'Your password has been created',
      //   });
    }, 1500);
  };

  return (
    <div className="space-y-6">
      <div className="text-center">
        <div className="mb-6 flex items-center justify-center">
          <Lock className="size-8 text-white" />
        </div>
        <h3 className="mb-2 text-xl font-semibold">Set Your Password</h3>
        <p className="text-muted-foreground">
          Create a secure password for {email}
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <div className="relative">
              <Input
                id="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Enter your password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                className="h-12 pr-12"
                required
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="confirmPassword">Confirm Password</Label>
            <div className="relative">
              <Input
                id="confirmPassword"
                type={showConfirmPassword ? 'text' : 'password'}
                placeholder="Confirm your password"
                value={confirmPassword}
                onChange={e => setConfirmPassword(e.target.value)}
                className="h-12 pr-12"
                required
              />
            </div>
          </div>
        </div>

        {/* Password Requirements */}
        <div className="rounded-lg bg-secondary/50 p-4">
          <h4 className="mb-3 text-sm font-medium">Password Requirements:</h4>
          <div className="space-y-2">
            {passwordRequirements.map((req, index) => {
              const isValid = req.test(password);
              return (
                <div key={index} className="flex items-center gap-2">
                  {isValid ? (
                    <Check className="size-4 text-primary" />
                  ) : (
                    <X className="size-4 text-muted-foreground" />
                  )}
                  <span
                    className={cn(
                      'text-sm',
                      isValid ? 'text-primary' : 'text-muted-foreground',
                    )}
                  >
                    {req.label}
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        <Button
          type="submit"
          className="h-12 w-full bg-primary transition-opacity hover:opacity-90"
          disabled={isLoading}
        >
          {isLoading ? (
            <div className="flex items-center gap-2">
              <div className="size-4 animate-spin rounded-full border-2 border-primary-foreground border-t-transparent" />
              Setting Password...
            </div>
          ) : (
            <div className="flex items-center gap-2">
              Set Password
              <ArrowRight className="size-4" />
            </div>
          )}
        </Button>
      </form>
    </div>
  );
};

export default PasswordSetupStep;
