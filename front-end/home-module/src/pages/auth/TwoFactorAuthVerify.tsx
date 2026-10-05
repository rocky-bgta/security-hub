import {
  ArrowLeft,
  Lock,
  Shield,
  ShieldCheck,
  Smartphone,
} from 'lucide-react';
import {
  ClipboardEvent,
  KeyboardEvent,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import Loader from 'common/loader/Loader';
import useAPI from 'hooks/UseAPI';
import useAuth from 'hooks/UseAuth';
import useStore from 'hooks/UseStore';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { routes } from 'routes/Route';
import { PUBLIC_URL } from 'utils/Constants';
import { cn, isSuccessResponse } from 'utils/Helper';
import { getMfaMethodUi, IMfaMethodUi } from 'utils/MfaMethods';

const Logo = PUBLIC_URL + '/images/aspire-logo.png';
const AuthBG = PUBLIC_URL + '/images/auth-bg.webp';

const CODE_LENGTH = 6;
const RESEND_TIME = 60;

const FEATURES = [
  {
    icon: Shield,
    title: 'Extra Protection',
    description:
      'Multi-factor authentication adds another layer of security to your account.',
  },
  {
    icon: Lock,
    title: 'Verified Access',
    description:
      'Only you can approve sign-ins with a code from your trusted device.',
  },
  {
    icon: Smartphone,
    title: 'Flexible Methods',
    description:
      'Choose authenticator apps, SMS, or email — whichever works best for you.',
  },
] as const;

const TwoFactorAuthVerify = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [selectedMethod, setSelectedMethod] = useState<IMfaMethodUi | null>(
    null,
  );
  const [showOtherMethods, setShowOtherMethods] = useState<boolean>(false);

  const [code, setCode] = useState<Array<string>>(Array(CODE_LENGTH).fill(''));
  const [disableCode, setDisableCode] = useState<boolean>(false);
  const [sessionId, setSessionId] = useState<string>('');
  const [secondsLeft, setSecondsLeft] = useState<number>(RESEND_TIME);
  const [errorMessage, setErrorMessage] = useState<string>('');

  const inputsRef = useRef<(HTMLInputElement | null)[]>([]);
  const timerRef = useRef<NodeJS.Timeout | null>(null);
  const hasAllDigits = code.every(digit => digit !== '');

  const navigate = useNavigate();
  const location = useLocation();
  const { login, setAuthenticationInProgress } = useAuth();
  const { userInfo } = useStore();
  const apiClient = useAPI();

  const tempToken = new URLSearchParams(location.search).get('token') || '';
  const email = new URLSearchParams(location.search).get('email') || '';
  const from =
    location.state?.from?.pathname ||
    sessionStorage.getItem('redirectAfterLogin') ||
    routes.dashboard.path;

  useEffect(() => {
    setAuthenticationInProgress(true);

    return () => {
      setAuthenticationInProgress(false);
    };
  }, [setAuthenticationInProgress]);

  useEffect(() => {
    if (selectedMethod) {
      setLoading(false);
      return;
    }

    const enrolled = userInfo?.methods ?? [];
    if (!enrolled.length) {
      setLoading(false);
      return;
    }

    const defaultMethod =
      enrolled.find(item => item.isDefault) ?? enrolled[0];
    const methodUi = getMfaMethodUi(defaultMethod.method);
    if (methodUi) {
      setSelectedMethod(methodUi);
    }
    setLoading(false);
  }, [selectedMethod, userInfo]);

  const otherMethods = useMemo(() => {
    if (!selectedMethod) return [];
    return (userInfo?.methods ?? [])
      .filter(item => item.method !== selectedMethod.method)
      .map(item => getMfaMethodUi(item.method))
      .filter((item): item is IMfaMethodUi => Boolean(item));
  }, [selectedMethod, userInfo?.methods]);

  const startTimer = () => {
    if (timerRef.current) clearInterval(timerRef.current);

    setSecondsLeft(RESEND_TIME);

    timerRef.current = setInterval(() => {
      setSecondsLeft(prev => {
        if (prev <= 1) {
          clearInterval(timerRef.current!);
          timerRef.current = null;
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const generateOTP = useCallback(async () => {
    if (!selectedMethod) return;

    if (selectedMethod.method === 'AUTHENTICATOR') {
      return;
    }

    const URL = API_END_POINTS.GENERATE_OTP;
    const payload = { data: { method: selectedMethod.method, tempToken } };

    try {
      const response = await apiClient.post(URL, payload);

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to generate OTP');
      }
      setSessionId(response.data.sessionId);
    } catch (error) {
      console.error('Error generating OTP:', error);
      const message = (error as Error).message ?? 'Failed to generate OTP';
      toast.error(message);
      if (message.toLowerCase().includes('expired')) {
        navigate(routes.login.path, { replace: true });
      }
    } finally {
      setLoading(false);
    }
  }, [apiClient, navigate, selectedMethod, tempToken]);

  const verifyCode = useCallback(async () => {
    if (!hasAllDigits) return;

    const finalCode = code.join('');
    setDisableCode(true);
    setErrorMessage('');

    try {
      const payload =
        selectedMethod?.method === 'AUTHENTICATOR'
          ? {
              data: {
                method: selectedMethod?.method,
                tempToken,
                code: finalCode,
                sessionId: 'b0536656-7e9b-4e87-be2d-5140c4db28ea',
              },
            }
          : {
              data: {
                method: selectedMethod?.method,
                sessionId,
                tempToken,
                code: finalCode,
              },
            };
      const response = await apiClient.post(API_END_POINTS.VERIFY_OTP, payload);

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      login({
        ...response.data.tokenResponse,
      });

      navigate(from, { replace: true });
    } catch (error) {
      console.error('OTP verification failed: ', error);
      const message = (error as Error).message ?? 'OTP verification failed';
      toast.error(message);
      if (message.toLowerCase().includes('expired')) {
        navigate(routes.login.path, { replace: true });
      } else {
        setErrorMessage(message);
        setDisableCode(false);
      }
    }
  }, [
    apiClient,
    code,
    from,
    hasAllDigits,
    login,
    navigate,
    selectedMethod,
    sessionId,
    tempToken,
  ]);

  useEffect(() => {
    inputsRef.current[0]?.focus();
    setCode(Array(CODE_LENGTH).fill(''));
    setDisableCode(false);
    setErrorMessage('');
    generateOTP();
    startTimer();

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [selectedMethod, generateOTP]);

  const handleChange = (value: string, index: number) => {
    if (!/^\d?$/.test(value)) return;

    const newCode = [...code];
    newCode[index] = value;
    setCode(newCode);

    if (value && index < CODE_LENGTH - 1) {
      inputsRef.current[index + 1]?.focus();
    }
    setErrorMessage('');
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>, index: number) => {
    if (e.key === 'Backspace' && !code[index] && index > 0) {
      inputsRef.current[index - 1]?.focus();
    }

    if (e.key === 'ArrowLeft' && index > 0) {
      inputsRef.current[index - 1]?.focus();
    }

    if (e.key === 'ArrowRight' && index < CODE_LENGTH - 1) {
      inputsRef.current[index + 1]?.focus();
    }

    if (e.key === 'Enter' && hasAllDigits) {
      verifyCode();
    }
  };

  const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
    const pasted = e.clipboardData.getData('text').slice(0, CODE_LENGTH);
    if (!/^\d+$/.test(pasted)) return;

    const newCode = pasted.split('');
    setCode(newCode);

    inputsRef.current[newCode.length - 1]?.focus();
  };

  const handleResendCode = async () => {
    if (secondsLeft > 0) return;

    try {
      const response = await apiClient.post(API_END_POINTS.RESEND_VERIFY_CODE, {
        data: { method: selectedMethod?.method, tempToken },
      });

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }

      setSessionId(response.data.sessionId);
      setCode(Array(CODE_LENGTH).fill(''));
      setDisableCode(false);
      setErrorMessage('');
      inputsRef.current[0]?.focus();
      startTimer();
      toast.success(response.message);
    } catch (error) {
      console.error('OTP resend failed: ', error);
      const message = (error as Error).message;
      toast.error(message);
      if (message.toLowerCase().includes('expired')) {
        navigate(routes.login.path, { replace: true });
      }
    }
  };

  if (loading) return <Loader />;

  if (!selectedMethod) {
    navigate(routes.login.path);
    return null;
  }

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
                Stay <span className="home-text-[#2D55FB]">Secure</span>
              </h1>
              <p className="home-mb-8 home-max-w-md home-text-base home-text-white/80">
                Protect your Security Awareness Training and Phishing Simulation
                account with multi-factor authentication.
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

          <div className="home-w-full home-max-w-xl home-overflow-hidden home-rounded-2xl home-bg-white home-shadow-soft-shadow">
            <div className="home-px-6 home-py-8 sm:home-px-8 sm:home-py-10 lg:home-px-20">
              <div className="home-mb-8 home-text-center">
                <div className="home-mx-auto home-mb-5 home-flex home-size-14 home-items-center home-justify-center home-rounded-full home-bg-blue-50">
                  <selectedMethod.icon className="home-size-5 home-text-[#084c94]" />
                </div>
                <h2 className="home-text-2xl home-font-semibold home-text-background sm:home-text-3xl">
                  Enter Verification Code
                </h2>
                <p className="home-mx-auto home-mt-1 home-max-w-sm home-text-sm home-leading-relaxed home-text-steel-gray">
                  {selectedMethod.method === 'AUTHENTICATOR'
                    ? 'Check your authenticator app for the verification code.'
                    : `We sent a 6-digit verification code to ${
                        selectedMethod.method === 'EMAIL'
                          ? email
                          : 'your phone number.'
                      }`}
                </p>
              </div>

              <div className="home-flex home-flex-col home-items-center home-justify-center home-gap-4">
                <p className="home-text-sm home-font-medium home-text-background">
                  Enter Verification Code
                </p>
                <div className="home-flex home-gap-2">
                  {code.map((digit, index) => (
                    <Input
                      key={index}
                      ref={el => {
                        inputsRef.current[index] = el;
                      }}
                      type="text"
                      inputMode="numeric"
                      maxLength={1}
                      value={digit}
                      onChange={e => handleChange(e.target.value, index)}
                      onKeyDown={e => handleKeyDown(e, index)}
                      onPaste={handlePaste}
                      disabled={disableCode}
                      className="home-size-12 home-rounded-lg home-text-center home-text-xl home-text-black focus-visible:home-ring-[#084c9485]"
                    />
                  ))}
                </div>

                {errorMessage && (
                  <p className="home-flex home-w-full home-items-center home-rounded-md home-border home-border-red-200 home-bg-red-700 home-p-2.5 home-text-xs home-text-red-200">
                    <span className="home-mr-1 home-flex home-size-4 home-items-center home-justify-center home-rounded-full home-border-2 home-border-red-200 home-text-[9px] home-text-red-200">
                      !
                    </span>
                    {errorMessage}
                  </p>
                )}

                <Button
                  type="button"
                  className="home-h-12 home-w-full !home-bg-[#084c94] !home-text-base home-font-semibold home-text-white hover:!home-bg-[#084c94]/90"
                  disabled={disableCode || !hasAllDigits}
                  onClick={verifyCode}
                >
                  {disableCode ? 'Verifying...' : 'Verify Code'}
                </Button>

                {selectedMethod.method !== 'AUTHENTICATOR' && (
                  <p className="home-text-xs home-text-steel-gray">
                    <span
                      className={cn(
                        'home-underline',
                        secondsLeft === 0
                          ? 'home-cursor-pointer home-text-[#084c94]'
                          : 'home-cursor-default home-text-gray-400',
                      )}
                      onClick={handleResendCode}
                    >
                      Resend code
                    </span>{' '}
                    {secondsLeft > 0 && (
                      <span>
                        in 00:
                        {String(secondsLeft).padStart(2, '0')}s
                      </span>
                    )}
                  </p>
                )}

                {otherMethods.length > 0 && (
                  <div className="home-w-full home-pt-1">
                    {showOtherMethods ? (
                      <div className="home-flex home-flex-col home-gap-2">
                        <p className="home-text-center home-text-sm home-font-medium home-text-background">
                          Choose a verification method
                        </p>
                        {otherMethods.map(item => (
                          <Button
                            key={item.method}
                            type="button"
                            variant="outline"
                            className="home-h-auto home-w-full home-items-center !home-justify-start home-rounded-xl home-border-gray-200 home-bg-white home-p-3 !home-text-background hover:!home-bg-gray-50 hover:!home-text-background"
                            onClick={() => {
                              setShowOtherMethods(false);
                              setSelectedMethod(item);
                            }}
                          >
                            <div className="home-mr-1 home-flex home-size-10 home-shrink-0 home-items-center home-justify-center home-rounded-lg home-bg-blue-50">
                              <item.icon className="home-size-5 home-text-[#084c94]" />
                            </div>
                            <div className="home-text-left">
                              <h4 className="home-text-base home-font-semibold home-text-background">
                                {item.header}
                              </h4>
                              <p className="home-text-xs home-font-normal home-text-steel-gray">
                                {item.description}
                              </p>
                            </div>
                          </Button>
                        ))}
                        <button
                          type="button"
                          className="home-text-center home-text-sm home-font-medium home-text-[#084c94] hover:home-underline"
                          onClick={() => setShowOtherMethods(false)}
                        >
                          Cancel
                        </button>
                      </div>
                    ) : (
                      <p className="home-text-center home-text-sm">
                        <button
                          type="button"
                          className="home-font-medium home-text-[#084c94] hover:home-underline"
                          onClick={() => setShowOtherMethods(true)}
                        >
                          Try another method
                        </button>
                      </p>
                    )}
                  </div>
                )}
              </div>

              <div className="home-relative home-my-5 home-flex home-items-center home-justify-center">
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

            <div className="home-mx-6 home-mb-6 home-flex home-items-start home-gap-3 home-rounded-xl home-bg-blue-50 home-px-4 home-py-3.5 sm:home-mx-8">
              <ShieldCheck className="home-mt-0.5 home-size-5 home-shrink-0 home-text-[#084c94]" />
              <p className="home-text-xs home-leading-relaxed home-text-steel-gray">
                Your data is protected with enterprise-grade encryption and
                security standards.
              </p>
            </div>
          </div>
        </section>
      </div>
    </main>
  );
};

export default TwoFactorAuthVerify;
