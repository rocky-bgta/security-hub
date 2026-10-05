import { Shield } from 'lucide-react';
import QRCode from 'qrcode';
import {
  ClipboardEvent,
  KeyboardEvent,
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react';
import { toast } from 'react-toastify';

import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import ConfirmDialog from 'components/ConfirmDialog';
import useAPI from 'hooks/UseAPI';
import useStore from 'hooks/UseStore';
import {
  IMfaMethodsResponse,
  IUserMfaMethodItem,
  IUserMfaMethodsResponse,
} from 'models/Login';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn, isSuccessResponse } from 'utils/Helper';
import { getMfaMethodUi, IMfaMethodUi } from 'utils/MfaMethods';

const CODE_LENGTH = 6;
const RESEND_TIME = 60;

const MFASettings = () => {
  const [loading, setLoading] = useState<boolean>(true);
  const [availableMethods, setAvailableMethods] = useState<Array<IMfaMethodUi>>(
    [],
  );
  const [enrolledMethods, setEnrolledMethods] = useState<
    Array<IUserMfaMethodItem>
  >([]);
  const [enrollingMethod, setEnrollingMethod] = useState<IMfaMethodUi | null>(
    null,
  );
  const [methodToRemove, setMethodToRemove] = useState<IMfaMethodUi | null>(
    null,
  );
  const [removing, setRemoving] = useState<boolean>(false);
  const [updatingDefault, setUpdatingDefault] = useState<string>('');

  const apiClient = useAPI();
  const { userInfo } = useStore();

  const enrolledByMethod = enrolledMethods.reduce<
    Record<string, IUserMfaMethodItem>
  >((acc, item) => {
    acc[item.method] = item;
    return acc;
  }, {});

  const fetchMethods = useCallback(async () => {
    setLoading(true);
    try {
      const [availableResponse, enrolledResponse] = await Promise.all([
        apiClient.get<IMfaMethodsResponse>(API_END_POINTS.FETCH_MFA_METHODS),
        apiClient.get<IUserMfaMethodsResponse>(
          API_END_POINTS.FETCH_USER_MFA_METHODS,
        ),
      ]);

      if (!isSuccessResponse(availableResponse.statusCode)) {
        throw new Error(
          availableResponse.message || 'Failed to fetch MFA methods',
        );
      }
      if (!isSuccessResponse(enrolledResponse.statusCode)) {
        throw new Error(
          enrolledResponse.message || 'Failed to fetch enrolled MFA methods',
        );
      }

      setAvailableMethods(
        (availableResponse.data.methods ?? [])
          .map(method => getMfaMethodUi(method))
          .filter((item): item is IMfaMethodUi => Boolean(item)),
      );
      setEnrolledMethods(enrolledResponse.data.methods ?? []);
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : 'Failed to load MFA methods',
      );
    } finally {
      setLoading(false);
    }
  }, [apiClient]);

  useEffect(() => {
    fetchMethods();
  }, [fetchMethods]);

  const handleSetDefault = async (method: string) => {
    setUpdatingDefault(method);
    try {
      const response = await apiClient.put<IUserMfaMethodsResponse>(
        API_END_POINTS.SET_DEFAULT_MFA_METHOD,
        { data: { method } },
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to set default method');
      }
      setEnrolledMethods(response.data.methods ?? []);
      toast.success(response.message || 'Default method updated');
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : 'Failed to set default method',
      );
    } finally {
      setUpdatingDefault('');
    }
  };

  const handleRemove = async () => {
    if (!methodToRemove) return;

    setRemoving(true);
    try {
      const response = await apiClient.del<IUserMfaMethodsResponse>(
        API_END_POINTS.REMOVE_MFA_METHOD.replace(
          ':method',
          methodToRemove.method,
        ),
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to remove MFA method');
      }
      setEnrolledMethods(response.data.methods ?? []);
      toast.success(response.message || 'MFA method removed');
      setMethodToRemove(null);
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : 'Failed to remove MFA method',
      );
    } finally {
      setRemoving(false);
    }
  };

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle className="home-flex home-items-center home-gap-2 home-text-xl home-text-white">
            <Shield className="home-size-5" />
            Multi-Factor Authentication
          </CardTitle>
          <CardDescription className="home-text-cloudy-white">
            Add extra verification methods to protect your account. The default
            method is used first when you sign in.
          </CardDescription>
        </CardHeader>
        <CardContent className="home-space-y-3">
          {loading ? (
            <p className="home-text-sm home-text-cloudy-white">
              Loading MFA methods...
            </p>
          ) : availableMethods.length === 0 ? (
            <p className="home-text-sm home-text-cloudy-white">
              No MFA methods are available.
            </p>
          ) : (
            availableMethods.map(item => {
              const enrolled = enrolledByMethod[item.method];
              const isLastEnrolled =
                enrolledMethods.length === 1 && Boolean(enrolled);
              const Icon = item.icon;

              return (
                <div
                  key={item.method}
                  className="home-flex home-flex-col home-gap-3 home-rounded-xl home-border home-border-card-border home-p-4 sm:home-flex-row sm:home-items-center sm:home-justify-between"
                >
                  <div className="home-flex home-items-start home-gap-3">
                    <div className="home-flex home-size-10 home-shrink-0 home-items-center home-justify-center home-rounded-lg home-bg-blue-50">
                      <Icon className="home-size-5 home-text-[#084c94]" />
                    </div>
                    <div>
                      <div className="home-flex home-flex-wrap home-items-center home-gap-2">
                        <h4 className="home-text-base home-font-semibold home-text-white">
                          {item.header}
                        </h4>
                        {enrolled?.isDefault && (
                          <Badge variant="secondary">Default</Badge>
                        )}
                      </div>
                      <p className="home-mt-0.5 home-text-xs home-text-cloudy-white">
                        {item.description}
                      </p>
                    </div>
                  </div>
                  <div className="home-flex home-flex-wrap home-items-center home-gap-2 sm:home-justify-end">
                    {enrolled && !enrolled.isDefault && (
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        disabled={updatingDefault === item.method}
                        onClick={() => handleSetDefault(item.method)}
                      >
                        {updatingDefault === item.method
                          ? 'Updating...'
                          : 'Set as default'}
                      </Button>
                    )}
                    {enrolled ? (
                      <Button
                        type="button"
                        variant="destructive"
                        size="sm"
                        disabled={isLastEnrolled}
                        title={
                          isLastEnrolled
                            ? 'The last remaining method cannot be removed'
                            : undefined
                        }
                        onClick={() => setMethodToRemove(item)}
                      >
                        Remove
                      </Button>
                    ) : (
                      <Button
                        type="button"
                        size="sm"
                        onClick={() => setEnrollingMethod(item)}
                      >
                        Add
                      </Button>
                    )}
                  </div>
                </div>
              );
            })
          )}
        </CardContent>
      </Card>

      <MfaEnrollDialog
        method={enrollingMethod}
        email={userInfo?.email || userInfo?.username || ''}
        phoneNumber={userInfo?.phoneNumber || ''}
        onClose={() => setEnrollingMethod(null)}
        onSuccess={() => {
          setEnrollingMethod(null);
          fetchMethods();
        }}
      />

      <ConfirmDialog
        isOpen={Boolean(methodToRemove)}
        message={`Remove ${methodToRemove?.header ?? 'this method'}? You will no longer be able to use it when signing in.`}
        buttonText="Remove"
        loadingText="Removing..."
        loading={removing}
        onClose={() => {
          if (!removing) setMethodToRemove(null);
        }}
        onConfirm={handleRemove}
      />
    </>
  );
};

interface IMfaEnrollDialogProps {
  method: IMfaMethodUi | null;
  email: string;
  phoneNumber: string;
  onClose: () => void;
  onSuccess: () => void;
}

const MfaEnrollDialog = ({
  method,
  email,
  phoneNumber,
  onClose,
  onSuccess,
}: IMfaEnrollDialogProps) => {
  const [code, setCode] = useState<Array<string>>(Array(CODE_LENGTH).fill(''));
  const [disableCode, setDisableCode] = useState<boolean>(false);
  const [sessionId, setSessionId] = useState<string>('');
  const [qrCode, setQrCode] = useState<string>('');
  const [secondsLeft, setSecondsLeft] = useState<number>(RESEND_TIME);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [preparing, setPreparing] = useState<boolean>(false);

  const inputsRef = useRef<(HTMLInputElement | null)[]>([]);
  const timerRef = useRef<NodeJS.Timeout | null>(null);
  const hasAllDigits = code.every(digit => digit !== '');
  const apiClient = useAPI();

  const startTimer = useCallback(() => {
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
  }, []);

  const generateOTP = useCallback(async () => {
    if (!method) return;

    setPreparing(true);
    const isAuthenticator = method.method === 'AUTHENTICATOR';
    const url = isAuthenticator
      ? API_END_POINTS.SETUP_AUTHENTICATOR
      : API_END_POINTS.GENERATE_OTP;
    const payload = isAuthenticator
      ? { data: {} }
      : { data: { method: method.method } };

    try {
      const response = await apiClient.post(url, payload);
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to generate OTP');
      }

      if (isAuthenticator) {
        const qrUrl = await QRCode.toDataURL(response.data.otpauthUri);
        setQrCode(qrUrl);
      } else {
        setSessionId(response.data.sessionId);
      }
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : 'Failed to generate OTP',
      );
    } finally {
      setPreparing(false);
    }
  }, [apiClient, method]);

  useEffect(() => {
    if (!method) {
      setCode(Array(CODE_LENGTH).fill(''));
      setDisableCode(false);
      setErrorMessage('');
      setQrCode('');
      setSessionId('');
      if (timerRef.current) {
        clearInterval(timerRef.current);
        timerRef.current = null;
      }
      return;
    }

    generateOTP();
    startTimer();

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [generateOTP, method, startTimer]);

  const verifyCode = async () => {
    if (!method || !hasAllDigits) return;

    const finalCode = code.join('');
    setDisableCode(true);
    setErrorMessage('');

    try {
      const isAuthenticator = method.method === 'AUTHENTICATOR';
      const url = isAuthenticator
        ? API_END_POINTS.VERIFY_AUTHENTICATOR
        : API_END_POINTS.VERIFY_OTP;
      const payload = isAuthenticator
        ? { data: { code: finalCode } }
        : {
            data: {
              method: method.method,
              sessionId,
              code: finalCode,
            },
          };

      const response = await apiClient.post(url, payload);
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'OTP verification failed');
      }

      toast.success(response.message || 'MFA method added');
      onSuccess();
    } catch (error) {
      const message =
        error instanceof Error ? error.message : 'OTP verification failed';
      toast.error(message);
      setErrorMessage(message);
      setDisableCode(false);
    }
  };

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
    if (!method || secondsLeft > 0) return;

    try {
      const response = await apiClient.post(API_END_POINTS.RESEND_VERIFY_CODE, {
        data: { method: method.method },
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
      toast.error(error instanceof Error ? error.message : 'OTP resend failed');
    }
  };

  const destination =
    method?.method === 'EMAIL'
      ? email || 'your email'
      : phoneNumber || 'your phone number';

  return (
    <Dialog
      open={Boolean(method)}
      onOpenChange={open => {
        if (!open) onClose();
      }}
    >
      <DialogContent className="!home-w-full !home-max-w-md" shouldPreventClose>
        <DialogHeader>
          <DialogTitle className="home-text-white">
            {method?.method === 'AUTHENTICATOR'
              ? 'Scan QR Code'
              : 'Enter Verification Code'}
          </DialogTitle>
          <DialogDescription className="home-text-cloudy-white">
            {method?.method === 'AUTHENTICATOR'
              ? 'Scan the QR code with your authenticator app, then enter the 6-digit code.'
              : `We sent a 6-digit verification code to ${destination}.`}
          </DialogDescription>
        </DialogHeader>

        <div className="home-flex home-flex-col home-items-center home-gap-4">
          {preparing && (
            <p className="home-text-sm home-text-cloudy-white">Preparing...</p>
          )}
          {qrCode && (
            <img
              src={qrCode}
              alt="QR Code"
              className="home-rounded-xl home-border home-border-gray-100 home-bg-white"
            />
          )}
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
                disabled={disableCode || preparing}
                className="home-size-12 home-rounded-lg home-text-center home-text-xl home-text-white focus-visible:home-ring-[#084c9485]"
              />
            ))}
          </div>

          {errorMessage && (
            <p className="home-w-full home-rounded-md home-border home-border-red-200 home-bg-red-700 home-p-2.5 home-text-xs home-text-red-200">
              {errorMessage}
            </p>
          )}

          <Button
            type="button"
            className="home-h-11 home-w-full"
            disabled={disableCode || preparing || !hasAllDigits}
            onClick={verifyCode}
          >
            {disableCode ? 'Verifying...' : 'Verify Code'}
          </Button>

          {method && method.method !== 'AUTHENTICATOR' && (
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
                <span>in 00:{String(secondsLeft).padStart(2, '0')}s</span>
              )}
            </p>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default MFASettings;
