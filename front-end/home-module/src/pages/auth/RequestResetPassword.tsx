import { zodResolver } from '@hookform/resolvers/zod';
import {
  ArrowLeft,
  Info,
  Lock,
  LockKeyhole,
  Mail,
  Send,
  Shield,
  ShieldCheck,
} from 'lucide-react';
import { useState } from 'react';
import { Controller, SubmitHandler, useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
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

const Logo = PUBLIC_URL + '/images/aspire-logo.png';
const AuthBG = PUBLIC_URL + '/images/auth-bg.webp';

const FEATURES = [
  {
    icon: Shield,
    title: 'Secure Reset',
    description:
      'Password reset links are encrypted and expire automatically for your protection.',
  },
  {
    icon: Lock,
    title: 'Account Safety',
    description:
      'Only the email associated with your account can request a reset link.',
  },
  {
    icon: Mail,
    title: 'Quick Recovery',
    description:
      'Receive a secure link in your inbox and regain access in just a few steps.',
  },
] as const;

const forgetPasswordSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Email address is required')
    .max(254, 'Email must not exceed 254 characters')
    .email('Enter a valid email address'),
});

const RequestResetPassword = () => {
  const {
    control,
    handleSubmit,
    formState: { errors },
  } = useForm<z.infer<typeof forgetPasswordSchema>>({
    resolver: zodResolver(forgetPasswordSchema),
    mode: 'onChange',
  });
  const apiClient = useAPI();

  const [isLoading, setIsLoading] = useState<boolean>(false);

  const onSubmit: SubmitHandler<z.infer<typeof forgetPasswordSchema>> = async (
    data: z.infer<typeof forgetPasswordSchema>,
  ) => {
    try {
      setIsLoading(true);
      const response = await apiClient.post(API_END_POINTS.FORGET_PASSWORD, {
        data: {
          username: data.email,
        },
      });
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Password reset email sent');
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error forgetting password', error);
      toast.error('Error forgetting password');
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
                Reset with{' '}
                <span className="home-text-[#2D55FB]">Confidence</span>
              </h1>
              <p className="home-mb-8 home-max-w-md home-text-base home-text-white/80">
                Recover access to your Security Awareness Training and Phishing
                Simulation platform securely and quickly.
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
            className="home-w-full home-max-w-xl home-overflow-hidden home-rounded-2xl home-bg-white home-shadow-soft-shadow"
            onSubmit={handleSubmit(onSubmit)}
          >
            <div className="home-px-6 home-py-8 sm:home-px-8 sm:home-py-10 lg:home-px-20">
              <div className="home-mb-8 home-text-center">
                <div className="home-mx-auto home-mb-5 home-flex home-size-14 home-items-center home-justify-center home-rounded-full home-bg-blue-50">
                  <LockKeyhole
                    className="home-size-6 home-text-[#084c94] lg:home-size-7"
                    strokeWidth={2}
                  />
                </div>
                <h2 className="home-text-2xl home-font-semibold home-text-background sm:home-text-3xl">
                  Reset Your Password
                </h2>
                <p className="home-mx-auto home-mt-1 home-max-w-sm home-text-sm home-leading-relaxed home-text-steel-gray">
                  Enter your email address and we&apos;ll send you a secure link
                  to reset your password.
                </p>
              </div>

              <div className="home-flex home-flex-col home-gap-4">
                <div className="home-space-y-1">
                  <Label
                    htmlFor="email"
                    className="home-font-semibold !home-text-background"
                  >
                    Email Address
                  </Label>
                  <Controller
                    name="email"
                    control={control}
                    render={({ field: { onChange, value } }) => (
                      <div className="home-relative">
                        <Mail className="home-pointer-events-none home-absolute home-left-3 home-top-1/2 home-size-4 -home-translate-y-1/2 home-text-steel-gray" />
                        <Input
                          id="email"
                          key="email"
                          name="email"
                          type="email"
                          value={value}
                          placeholder="Enter your email address"
                          onChange={e => onChange(e.target.value)}
                          className={cn(
                            'home-pl-10 home-text-black focus-visible:home-ring-[#084c9485]',
                            errors?.email &&
                              'has-error !home-border-vibrant-red !home-placeholder-vibrant-red focus-visible:home-ring-vibrant-red',
                          )}
                          maxLength={255}
                        />
                      </div>
                    )}
                  />
                  {errors?.email ? (
                    <p className="home-mt-1.5 home-text-xs home-text-vibrant-red">
                      {errors.email.message}
                    </p>
                  ) : (
                    <p className="home-mt-1.5 home-flex home-items-center home-gap-1.5 home-text-xs home-text-steel-gray">
                      <Info className="home-size-3.5 home-shrink-0" />
                      Enter the email address associated with your account.
                    </p>
                  )}
                </div>

                <Button
                  type="submit"
                  disabled={isLoading}
                  className="home-h-12 home-w-full !home-bg-[#084c94] !home-text-base home-font-semibold home-text-white hover:!home-bg-[#084c94]/90"
                >
                  <Send className="home-size-4" />
                  {isLoading ? 'Sending...' : 'Send Secure Reset Link'}
                </Button>

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

export default RequestResetPassword;
