import { zodResolver } from '@hookform/resolvers/zod';
import {
  ArrowLeft,
  Check,
  Lock,
  LockKeyhole,
  Mail,
  Shield,
  ShieldCheck,
  X,
} from 'lucide-react';
import { useMemo, useState } from 'react';
import {
  Controller,
  FieldValues,
  SubmitHandler,
  useForm,
} from 'react-hook-form';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'react-toastify';
import { z } from 'zod';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import useAPI from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Route';
import { PUBLIC_URL } from 'utils/Constants';
import { cn, isSuccessResponse } from 'utils/Helper';

interface IPasswordErrors {
  charLength: boolean;
  maxLength: boolean;
  uppercase: boolean;
  lowercase: boolean;
  number: boolean;
  specialChar: boolean;
  confirmMatch: boolean;
}

const SPECIAL_CHAR_REGEX = /[!@#$%^&*()_+={}[\]|;:"'<>,.?/-]/;

const passwordRules = {
  minLength: /^.{8,}$/,
  maxLength: /^.{1,64}$/,
  uppercase: /[A-Z]/,
  lowercase: /[a-z]/,
  number: /[0-9]/,
  specialChar: SPECIAL_CHAR_REGEX,
};

const Logo = PUBLIC_URL + '/images/aspire-logo.png';
const AuthBG = PUBLIC_URL + '/images/auth-bg.webp';

const FEATURES = [
  {
    icon: Shield,
    title: 'Secure Setup',
    description:
      'Your password is encrypted and protected with enterprise-grade security standards.',
  },
  {
    icon: Lock,
    title: 'Strong Credentials',
    description:
      'Create a strong password to keep your account safe from unauthorized access.',
  },
  {
    icon: Mail,
    title: 'Ready to Start',
    description:
      'Once your password is set, you can sign in and access your training platform.',
  },
] as const;

const PASSWORD_REQUIREMENTS = [
  { key: 'charLength' as const, label: 'At least 8 characters' },
  { key: 'maxLength' as const, label: 'At most 64 characters' },
  { key: 'uppercase' as const, label: 'One uppercase letter' },
  { key: 'lowercase' as const, label: 'One lowercase letter' },
  { key: 'number' as const, label: 'One number' },
  {
    key: 'specialChar' as const,
    label: 'One special character (e.g., !@#$%^&*)',
  },
];

const setPasswordSchema = z
  .object({
    newPassword: z
      .string()
      .min(8, 'Password must be at least 8 characters')
      .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
      .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
      .regex(/[0-9]/, 'Password must contain at least one number')
      .regex(
        SPECIAL_CHAR_REGEX,
        'Password must contain at least one special character',
      )
      .max(64, 'Password cannot exceed 64 characters'),
    confirmNewPassword: z
      .string()
      .min(8, 'Confirm new password is required')
      .max(64, 'Confirm password cannot exceed 64 characters')
      .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
      .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
      .regex(/[0-9]/, 'Password must contain at least one number')
      .regex(
        SPECIAL_CHAR_REGEX,
        'Password must contain at least one special character',
      ),
  })
  .refine(data => data.newPassword === data.confirmNewPassword, {
    message: 'New passwords do not match',
    path: ['confirmNewPassword'],
  });

type TSetPasswordForm = z.infer<typeof setPasswordSchema>;

const SetPassword = () => {
  const [isLoading, setIsLoading] = useState(false);
  const apiClient = useAPI();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const isForcedChange = searchParams.get('credentialChangeNeeded') === 'true';

  const {
    control,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<TSetPasswordForm>({
    resolver: zodResolver(setPasswordSchema),
    mode: 'onChange',
    defaultValues: {
      newPassword: '',
      confirmNewPassword: '',
    },
  });

  const newPassword = watch('newPassword');
  const confirmNewPassword = watch('confirmNewPassword');

  const passwordErrors = useMemo<IPasswordErrors>(() => {
    const pwd = newPassword ?? '';
    return {
      charLength: passwordRules.minLength.test(pwd),
      maxLength: passwordRules.maxLength.test(pwd),
      uppercase: passwordRules.uppercase.test(pwd),
      lowercase: passwordRules.lowercase.test(pwd),
      number: passwordRules.number.test(pwd),
      specialChar: passwordRules.specialChar.test(pwd),
      confirmMatch:
        !confirmNewPassword ||
        confirmNewPassword === '' ||
        confirmNewPassword === pwd,
    };
  }, [newPassword, confirmNewPassword]);

  const onSubmit = async (data: TSetPasswordForm) => {
    if (!token) {
      toast.error('Invalid or missing token');
      return;
    }

    try {
      setIsLoading(true);
      const response = await apiClient.post(API_END_POINTS.RESET_PASSWORD, {
        data: {
          token,
          newPassword: data.newPassword,
        },
      });

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      toast.success('Password set successful!');
      navigate(routes.login.path);
    } catch (error) {
      console.error('Error setting password:', error);
      const message = (error as Error).message ?? 'Failed to set password';
      toast.error(message);
      if (message.toLowerCase().includes('expired')) {
        navigate(routes.requestResetPassword.path, { replace: true });
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <main className="home-relative home-flex home-min-h-screen home-flex-col home-font-outfit lg:home-h-screen lg:home-overflow-hidden">
      <div className="home-flex home-min-h-0 home-flex-1 home-flex-col lg:home-h-full lg:home-flex-row">
        {/* Left branding panel */}
        <aside className="home-relative home-hidden home-min-h-[280px] home-overflow-hidden lg:home-flex lg:home-h-full lg:home-w-2/5 lg:home-shrink-0">
          <div
            className="home-absolute home-inset-0 home-bg-cover home-bg-center home-bg-no-repeat"
            style={{ backgroundImage: `url(${AuthBG})` }}
            aria-hidden="true"
          />
          <div
            className="home-absolute home-inset-0 home-bg-black/40"
            aria-hidden="true"
          />

          <div className="home-relative home-z-10 home-flex home-size-full home-flex-col home-gap-8 home-p-10 xl:home-px-14">
            <img
              src={Logo}
              alt="Aspire Tech Logo"
              className="home-w-24 home-shrink-0 home-object-contain"
            />

            <div className="home-my-8 home-flex home-flex-col home-justify-center">
              <h1 className="home-mb-4 home-text-4xl home-font-bold home-leading-tight home-text-white xl:home-text-5xl">
                Get <span className="home-text-[#2D55FB]">Started</span>
              </h1>
              <p className="home-mb-8 home-max-w-md home-text-base home-text-white/80">
                Set your password to access your Security Awareness Training and
                Phishing Simulation platform.
              </p>

              <ul className="home-flex home-flex-col home-gap-8">
                {FEATURES.map(({ icon: Icon, title, description }) => (
                  <li
                    key={title}
                    className="home-flex home-items-start home-gap-4"
                  >
                    <div className="home-flex home-size-14 home-shrink-0 home-items-center home-justify-center home-rounded-full home-bg-[#2D55FB] home-bg-opacity-70 home-backdrop-blur-sm">
                      <Icon className="home-size-5 home-text-white" />
                    </div>
                    <div>
                      <p className="home-font-semibold home-text-white">
                        {title}
                      </p>
                      <p className="home-mt-1 home-text-sm home-text-cloudy-white">
                        {description}
                      </p>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </aside>

        {/* Right form panel */}
        <section className="home-flex home-min-h-0 home-flex-1 home-flex-col home-items-center home-justify-center home-overflow-y-auto home-bg-background home-px-4 home-py-8 sm:home-px-6 lg:home-h-full lg:home-bg-gray-100 lg:home-p-10">
          <img
            src={Logo}
            alt="Aspire Tech Logo"
            className="home-mb-6 home-w-28 home-shrink-0 home-object-contain lg:home-hidden"
          />

          <form
            className="home-w-full home-max-w-xl home-rounded-2xl home-bg-white home-shadow-soft-shadow"
            onSubmit={handleSubmit(onSubmit as SubmitHandler<FieldValues>)}
          >
            <div className="home-px-6 home-py-8 sm:home-px-8 sm:home-py-6 lg:home-px-20">
              <div className="home-mb-4 home-text-center">
                <div className="home-mx-auto home-mb-5 home-flex home-size-14 home-items-center home-justify-center home-rounded-full home-bg-blue-50">
                  <LockKeyhole
                    className="home-size-6 home-text-[#084c94] lg:home-size-7"
                    strokeWidth={2}
                  />
                </div>
                <h2 className="home-text-2xl home-font-semibold home-text-background sm:home-text-3xl">
                  {isForcedChange ? 'Change Password' : 'Set Password'}
                </h2>
                <p className="home-mx-auto home-mt-1 home-max-w-sm home-text-sm home-leading-relaxed home-text-steel-gray">
                  {isForcedChange
                    ? 'For security reasons, you must change your password before continuing.'
                    : 'Choose a strong password to secure your account and get started.'}
                </p>
              </div>

              <div className="home-flex home-flex-col home-gap-4">
                <div className="home-space-y-1">
                  <Label
                    htmlFor="newPassword"
                    className="home-font-semibold !home-text-background"
                  >
                    New Password
                  </Label>
                  <Controller
                    name="newPassword"
                    control={control}
                    render={({ field }) => (
                      <div className="home-relative">
                        <Lock className="home-pointer-events-none home-absolute home-left-3 home-top-1/2 home-z-10 home-size-4 -home-translate-y-1/2 home-text-steel-gray" />
                        <Input
                          id="newPassword"
                          type="password"
                          placeholder="Enter new password"
                          {...field}
                          className={cn(
                            'home-pl-10 home-text-black focus-visible:home-ring-[#084c9485]',
                            errors.newPassword &&
                              'has-error !home-border-vibrant-red !home-placeholder-vibrant-red focus-visible:home-ring-vibrant-red',
                          )}
                          maxLength={65}
                        />
                      </div>
                    )}
                  />
                  {errors.newPassword && (
                    <p className="home-mt-1.5 home-text-xs home-text-vibrant-red">
                      {errors.newPassword.message}
                    </p>
                  )}
                  <ul className="home-mt-2 home-space-y-1.5 home-rounded-xl home-bg-gray-50 home-px-3 home-py-2.5">
                    {PASSWORD_REQUIREMENTS.map(({ key, label }) => {
                      const isMet = passwordErrors[key];
                      return (
                        <li
                          key={key}
                          className={cn(
                            'home-flex home-items-center home-gap-2 home-text-xs',
                            isMet
                              ? 'home-text-green-600'
                              : 'home-text-steel-gray',
                          )}
                        >
                          {isMet ? (
                            <Check className="home-size-3.5 home-shrink-0" />
                          ) : (
                            <X className="home-size-3.5 home-shrink-0" />
                          )}
                          {label}
                        </li>
                      );
                    })}
                  </ul>
                </div>

                <div className="home-space-y-1">
                  <Label
                    htmlFor="confirmNewPassword"
                    className="home-font-semibold !home-text-background"
                  >
                    Confirm New Password
                  </Label>
                  <Controller
                    name="confirmNewPassword"
                    control={control}
                    render={({ field }) => (
                      <div className="home-relative">
                        <Lock className="home-pointer-events-none home-absolute home-left-3 home-top-1/2 home-z-10 home-size-4 -home-translate-y-1/2 home-text-steel-gray" />
                        <Input
                          id="confirmNewPassword"
                          type="password"
                          placeholder="Confirm new password"
                          {...field}
                          className={cn(
                            'home-pl-10 home-text-black focus-visible:home-ring-[#084c9485]',
                            (errors.confirmNewPassword ||
                              (!passwordErrors.confirmMatch &&
                                confirmNewPassword !== '')) &&
                              'has-error !home-border-vibrant-red !home-placeholder-vibrant-red focus-visible:home-ring-vibrant-red',
                          )}
                          maxLength={65}
                        />
                      </div>
                    )}
                  />
                  {(errors.confirmNewPassword ||
                    (!passwordErrors.confirmMatch &&
                      confirmNewPassword !== '')) && (
                    <p className="home-mt-1.5 home-text-xs home-text-vibrant-red">
                      {errors.confirmNewPassword?.message ??
                        'Passwords do not match'}
                    </p>
                  )}
                </div>

                <Button
                  type="submit"
                  disabled={isLoading}
                  className="home-h-12 home-w-full !home-bg-[#084c94] !home-text-base home-font-semibold home-text-white hover:!home-bg-[#084c94]/90"
                >
                  <LockKeyhole className="home-size-4" />
                  {isLoading
                    ? 'Submitting...'
                    : isForcedChange
                      ? 'Change Password'
                      : 'Set Password'}
                </Button>

                {!isForcedChange && (
                  <>
                    <div className="home-relative home-flex home-items-center home-justify-center">
                      <div className="home-absolute home-inset-x-0 home-border-t home-border-gray-200" />
                      <span className="home-relative home-bg-white home-px-3 home-text-sm home-text-steel-gray">
                        or
                      </span>
                    </div>

                    <Link
                      className="home-flex home-items-center home-justify-center home-gap-1.5 home-text-sm home-font-medium home-text-[#084c94] hover:home-underline"
                      to={routes.login.path}
                    >
                      <ArrowLeft className="home-size-4" />
                      Return to Sign In
                    </Link>
                  </>
                )}
              </div>
            </div>

            <div className="home-mx-6 home-mb-6 home-flex home-items-start home-gap-3 home-rounded-xl home-bg-blue-50 home-px-4 home-py-3.5 sm:home-mx-8">
              <ShieldCheck className="home-mt-0.5 home-size-5 home-shrink-0 home-text-[#084c94]" />
              <p className="home-text-xs home-leading-relaxed home-text-steel-gray">
                Your data is protected with enterprise-grade encryption and
                security standards.
              </p>
            </div>
          </form>
        </section>
      </div>
    </main>
  );
};

export default SetPassword;
