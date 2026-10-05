import { zodResolver } from '@hookform/resolvers/zod';
import {
  BarChart3,
  CircleQuestionMark,
  Globe,
  Lock,
  Mail,
  Shield,
  ShieldCheck,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { Controller, SubmitHandler, useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import useAPI from 'hooks/UseAPI';
import useAuth from 'hooks/UseAuth';
import useStore from 'hooks/UseStore';
import { IResponse } from 'models/Context';
import { ILoginCredentials, ILoginResponse } from 'models/Login';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Route';
import { DefaultLoginValues, loginSchema } from 'schemas/LoginSchema';
import { LocalStorageKey, PUBLIC_URL } from 'utils/Constants';
import { cn, getDeviceInfo, isSuccessResponse } from 'utils/Helper';
import { getToken, removeToken, setToken } from 'utils/TokenStorage';

const Logo = PUBLIC_URL + '/images/aspire-logo.png';
const AuthBG = PUBLIC_URL + '/images/auth-bg.webp';
const HELP_URL = 'https://securityawarenesstraining.ai/company/contact';

const FEATURES = [
  {
    icon: Shield,
    title: 'Secure Access',
    description:
      'Your data is protected with enterprise-grade security and encryption.',
  },
  {
    icon: BarChart3,
    title: 'Smart Learning',
    description:
      'Access personalized learning experiences and track your progress.',
  },
  {
    icon: Globe,
    title: 'Anywhere, Anytime',
    description: 'Learn and grow from any device, anytime, anywhere.',
  },
] as const;

const Login = () => {
  const {
    control,
    handleSubmit,
    reset,
    watch,
    clearErrors,
    formState: { errors },
  } = useForm<ILoginCredentials>({
    resolver: zodResolver(loginSchema),
    defaultValues: DefaultLoginValues,
    reValidateMode: 'onChange',
    mode: 'onChange',
  });

  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [rememberMe, setRememberMe] = useState<boolean>(false);
  const [submitError, setSubmitError] = useState<string>('');
  const navigate = useNavigate();
  const location = useLocation();

  const { login, setAuthenticationInProgress } = useAuth();
  const { setUserInfo } = useStore();
  const apiClient = useAPI();

  const usernameValue = watch('username') ?? '';

  const from =
    location.state?.from?.pathname ||
    sessionStorage.getItem('redirectAfterLogin') ||
    routes.dashboard.path;

  useEffect(() => {
    const rememberedUsername = getToken(LocalStorageKey.REMEMBERED_USERNAME);

    if (rememberedUsername) {
      reset({ ...DefaultLoginValues, username: rememberedUsername });
      setRememberMe(true);
    }
  }, [reset]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const reason = params.get('reason') ?? '';

    if (['session_expired', 'session_invalid'].includes(reason as string)) {
      if (reason === 'session_expired') {
        toast.error('Your session has expired. Please log in again.');
      } else if (reason === 'session_invalid') {
        toast.error('Your session is no longer valid. Please log in again.');
      }

      params.delete('reason');
      navigate({ search: params.toString() }, { replace: true });
    }
  }, [location.search, navigate]);

  const handleRememberMeChange = (checked: boolean) => {
    setRememberMe(checked);
    if (!checked) {
      removeToken(LocalStorageKey.REMEMBERED_USERNAME);
      return;
    }

    const email = usernameValue.trim();
    if (email) {
      setToken(LocalStorageKey.REMEMBERED_USERNAME, email);
    }
  };

  const persistRememberedUsername = (
    username: string,
    shouldRemember: boolean,
  ) => {
    if (shouldRemember) {
      setToken(LocalStorageKey.REMEMBERED_USERNAME, username.trim());
    } else {
      removeToken(LocalStorageKey.REMEMBERED_USERNAME);
    }
  };

  const handleFormSubmit: SubmitHandler<ILoginCredentials> = async (
    data: ILoginCredentials,
  ) => {
    setIsLoading(true);
    setAuthenticationInProgress(true);
    setSubmitError('');

    data.deviceInfo = getDeviceInfo();

    try {
      const response: IResponse<ILoginResponse> = await apiClient.post(
        API_END_POINTS.LOGIN,
        { data },
      );

      if (isSuccessResponse(response.statusCode)) {
        sessionStorage.removeItem('redirectAfterLogin');
        persistRememberedUsername(data.username, rememberMe);

        if (response.data.mfaSetupRequired) {
          navigate(
            routes.twoFactorAuthSetup.path +
              '?token=' +
              response.data.tempToken +
              '&email=' +
              data.username,
          );
          return;
        }

        if (response.data.mfaVerificationRequired) {
          setUserInfo(prev => ({
            ...prev,
            methods: response.data.methods ?? [],
          }));

          navigate(
            routes.twoFactorAuthVerify.path +
              '?token=' +
              response.data.tempToken +
              '&email=' +
              data.username,
          );
          return;
        }

        if (response.data.credentialChangeNeeded) {
          toast.info('You must change your password before continuing.');
          navigate(
            routes.setPassword.path +
              '?token=' +
              response.data.tempToken +
              '&email=' +
              data.username +
              '&credentialChangeNeeded=true',
          );
          return;
        }

        login({
          ...response.data,
        });

        navigate(from, { replace: true });
        return;
      }

      setAuthenticationInProgress(false);
      setSubmitError(
        response.message || 'The username and password do not match.',
      );
    } catch (error) {
      setAuthenticationInProgress(false);
      setSubmitError(
        error instanceof Error ? error.message : 'Password verification failed',
      );
      console.error('Password verification failed', error);
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

          <div className="home-relative home-z-10 home-flex home-size-full home-flex-col home-gap-8 home-p-10  xl:home-px-14">
            <img
              src={Logo}
              alt="Aspire Tech Logo"
              className="home-w-24 home-shrink-0 home-object-contain"
            />

            <div className="home-my-8 home-flex home-flex-col home-justify-center">
              <h1 className="home-mb-4 home-text-4xl home-font-bold home-leading-tight home-text-white xl:home-text-5xl">
                Welcome <span className="home-text-[#2D55FB]">Back!</span>
              </h1>
              <p className="home-mb-8 home-max-w-md home-text-base home-text-white/80">
                Sign in to access your subscribed Security Awareness Training
                and Phishing Simulation platform powered by AI.
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
          {/* Mobile logo */}
          <img
            src={Logo}
            alt="Aspire Tech Logo"
            className="home-mb-6 home-w-28 home-shrink-0 home-object-contain lg:home-hidden"
          />

          <form
            className="home-w-full home-max-w-xl home-overflow-hidden home-rounded-2xl home-bg-white home-shadow-soft-shadow"
            onSubmit={handleSubmit(handleFormSubmit)}
          >
            <div className="home-px-6 home-py-8 sm:home-px-8 sm:home-py-10 lg:home-px-20">
              <div className="home-mb-8 home-text-center">
                <h2 className="home-text-2xl home-font-semibold home-text-background sm:home-text-3xl">
                  Sign In
                </h2>
                <p className="home-mx-auto home-mt-1 home-max-w-sm home-text-sm home-leading-relaxed home-text-steel-gray">
                  Enter your credentials to continue
                </p>
              </div>

              <div className="home-flex home-flex-col home-gap-4">
                {/* Email */}
                <div className="home-space-y-1">
                  <Label
                    htmlFor="username"
                    className="home-font-semibold !home-text-background"
                  >
                    Email Address
                  </Label>
                  <Controller
                    name="username"
                    control={control}
                    render={({ field: { onChange, value } }) => (
                      <div className="home-relative">
                        <Mail className="home-pointer-events-none home-absolute home-left-3 home-top-1/2 home-size-4 -home-translate-y-1/2 home-text-steel-gray" />
                        <Input
                          id="username"
                          name="username"
                          value={value}
                          maxLength={255}
                          placeholder="Enter your email address"
                          onChange={e => {
                            clearErrors('username');
                            setSubmitError('');
                            onChange(e.target.value);
                          }}
                          className={cn(
                            'home-pl-10 home-text-black',
                            errors?.username && 'has-error',
                          )}
                        />
                      </div>
                    )}
                  />
                  <div className="home-mt-1.5 home-flex home-items-center home-justify-between home-gap-2">
                    <p className="home-text-xs home-text-steel-gray">
                      Enter a valid email address (e.g. name@domain.com)
                    </p>
                  </div>
                  {errors?.username && (
                    <p className="home-mt-1 home-text-xs home-text-vibrant-red">
                      {errors.username.message}
                    </p>
                  )}
                </div>

                {/* Password */}
                <div className="home-space-y-1">
                  <Label
                    htmlFor="password"
                    className="home-font-semibold !home-text-background"
                  >
                    Password
                  </Label>
                  <Controller
                    name="password"
                    control={control}
                    render={({ field: { onChange, value } }) => (
                      <div className="home-relative">
                        <Lock className="home-pointer-events-none home-absolute home-left-3 home-top-1/2 home-z-10 home-size-4 -home-translate-y-1/2 home-text-background" />
                        <Input
                          id="password"
                          name="password"
                          type="password"
                          value={value}
                          maxLength={65}
                          placeholder="Enter your password"
                          onChange={e => {
                            clearErrors('password');
                            setSubmitError('');
                            onChange(e.target.value);
                          }}
                          className={cn(
                            'home-pl-10 home-text-black',
                            errors?.password && 'has-error',
                          )}
                        />
                      </div>
                    )}
                  />
                  <div className="home-mt-1.5 home-flex home-items-center home-justify-between home-gap-2">
                    <p className="home-text-xs home-text-steel-gray">
                      8 to 64 characters
                    </p>
                  </div>
                  {errors?.password && (
                    <p className="home-mt-1 home-text-xs home-text-vibrant-red">
                      {errors.password.message}
                    </p>
                  )}
                </div>

                {/* Remember Me + links */}
                <div className="home-flex home-flex-wrap home-items-center home-justify-between home-gap-3">
                  <div className="home-flex home-items-center home-gap-2">
                    <Checkbox
                      id="remember-me"
                      checked={rememberMe}
                      onCheckedChange={checked =>
                        handleRememberMeChange(checked === true)
                      }
                      className="!home-border-[#084c94] data-[state=checked]:!home-bg-[#084c94] data-[state=checked]:home-text-white"
                    />
                    <Label
                      htmlFor="remember-me"
                      className="home-cursor-pointer home-font-normal !home-text-background"
                    >
                      Remember Me
                    </Label>
                  </div>

                  <div className="home-flex home-items-center home-gap-2 home-text-sm">
                    <Link
                      className="home-text-[#084c94] hover:home-underline"
                      to={routes.requestResetPassword.path}
                    >
                      Forgot Password?
                    </Link>
                    <span className="home-text-steel-gray">|</span>
                    <a
                      href={HELP_URL}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="home-flex home-items-center home-gap-1 home-text-[#084c94] hover:home-underline"
                    >
                      <CircleQuestionMark className="home-size-4" />
                      Help
                    </a>
                  </div>
                </div>

                {submitError && (
                  <p
                    role="alert"
                    className="home-rounded-md home-bg-vibrant-red/10 home-px-3 home-py-2 home-text-sm home-text-vibrant-red"
                  >
                    {submitError}
                  </p>
                )}

                <Button
                  type="submit"
                  disabled={isLoading}
                  className="home-h-12 home-w-full !home-bg-[#084c94] !home-text-base home-font-semibold home-text-white hover:!home-bg-[#084c94]/90"
                >
                  <Lock className="home-size-4" />
                  {isLoading ? 'Signing in...' : 'Sign In'}
                </Button>
              </div>
            </div>

            {/* Security banner */}
            <div className="home-mx-6 home-mb-6 home-flex home-items-start home-gap-3 home-rounded-xl home-bg-blue-50 home-px-4 home-py-3.5 sm:home-mx-8">
              <ShieldCheck className="home-mt-0.5 home-size-5 home-shrink-0 home-text-[#084c94]" />
              <p className="home-text-xs home-leading-relaxed home-text-steel-gray">
                Your security is important to us. All data is encrypted.
              </p>
            </div>
          </form>
        </section>
      </div>
    </main>
  );
};

export default Login;
