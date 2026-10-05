import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Textarea } from 'common/Textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAuth } from 'hooks/UseAuth';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { PaymentMethod } from 'models/Payment';
import { useEffect, useState } from 'react';
import { PUBLIC_URL } from 'utils/Constants';
import { ROLE } from 'utils/Role';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { toast } from 'react-toastify';
import { BanknoteIcon, CreditCardIcon, Loader2Icon } from 'lucide-react';
import { IDropdownOption } from 'models/Global';
import { useUploader } from 'hooks/UseUploader';

const STRIPE_IMAGE = PUBLIC_URL + '/images/stripe-icon.svg';
const PAYPAL_IMAGE = PUBLIC_URL + '/images/paypal-icon.svg';

// Validation rules
const validationRules = {
  bankTransfer: {
    bankName: { min: 2, max: 100 },
    accountNumber: { min: 5, max: 50 },
    bankBranchName: { min: 2, max: 100 },
    transactionNumber: { min: 5, max: 50 },
    paymentAmount: { min: 0.01, max: 999999999.99 },
  },
  checkPayment: {
    checkNumber: { min: 3, max: 50 },
    bankName: { min: 2, max: 100 },
    branchName: { min: 2, max: 100 },
    paymentAmount: { min: 0.01, max: 999999999.99 },
  },
  paymentComment: {
    comment: { min: 10, max: 1000 },
  },
};

interface IBankTransferDetails {
  bankName: string;
  accountNumber: string;
  bankBranchName: string;
  transactionNumber: string;
  paymentDate: string;
  paymentAmount: number | string;
  transactionReceiptFile: File | null;
  transactionReceiptUrl?: string;
}

interface ICheckPaymentDetails {
  checkNumber: string;
  bankName: string;
  branchName: string;
  paymentDate: string;
  paymentAmount: number | string;
  checkImageFile: File | null;
  checkImageUrl?: string;
}

interface ICommentLog {
  comment: string;
  actionTakenId: string;
  nextStepId: string;
}

interface IProps {
  open: boolean;
  onClose: () => void;
  invoiceNo: string;
  invoiceId: string;
  loading: boolean;
  onPayment: (paymentMethod: string) => void;
  onManualPaymentSuccess?: () => void;
  data: any;
  totalAmount?: number;
}

const ClientPaymentModal = ({
  open,
  onClose,
  invoiceNo,
  invoiceId,
  loading,
  onPayment,
  onManualPaymentSuccess,
  data,
  totalAmount = 0,
}: IProps) => {
  const { role } = useAuth();
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const { uploadFile } = useUploader();
  const isAspireAdmin = role === ROLE.ASPIRE_ADMIN;

  const [paymentMethod, setPaymentMethod] = useState<string>('');
  const [showManualPayment, setShowManualPayment] = useState<boolean>(false);
  const [manualPaymentMethod, setManualPaymentMethod] = useState<
    'BANK_TRANSFER' | 'CHECK_PAYMENT'
  >('BANK_TRANSFER');
  const [manualPaymentLoading, setManualPaymentLoading] =
    useState<boolean>(false);
  const [uploadingFile, setUploadingFile] = useState<boolean>(false);

  // Action taken and next step options
  const [actionTakenList, setActionTakenList] = useState<
    Array<IDropdownOption>
  >([]);
  const [nextStepList, setNextStepList] = useState<Array<IDropdownOption>>([]);

  // Bank transfer details
  const [bankTransferDetails, setBankTransferDetails] =
    useState<IBankTransferDetails>({
      bankName: '',
      accountNumber: '',
      bankBranchName: '',
      transactionNumber: '',
      paymentDate: '',
      paymentAmount: totalAmount,
      transactionReceiptFile: null,
    });

  // Check payment details
  const [checkPaymentDetails, setCheckPaymentDetails] =
    useState<ICheckPaymentDetails>({
      checkNumber: '',
      bankName: '',
      branchName: '',
      paymentDate: '',
      paymentAmount: totalAmount,
      checkImageFile: null,
    });

  // Comment log
  const [commentLog, setCommentLog] = useState<ICommentLog>({
    comment: '',
    actionTakenId: '',
    nextStepId: '',
  });

  // Validation errors
  const [validationErrors, setValidationErrors] = useState<
    Record<string, string>
  >({});

  useEffect(() => {
    if (open) {
      setPaymentMethod(data?.paymentMethod || '');
      setShowManualPayment(false);
      setManualPaymentMethod('BANK_TRANSFER');
      resetManualPaymentForm();

      if (isAspireAdmin) {
        fetchActionTakenList();
        fetchNextStepList();
      }
    }
  }, [open]);

  useEffect(() => {
    // Update payment amount when totalAmount changes
    setBankTransferDetails(prev => ({
      ...prev,
      paymentAmount: totalAmount,
    }));
    setCheckPaymentDetails(prev => ({
      ...prev,
      paymentAmount: totalAmount,
    }));
  }, [totalAmount]);

  const resetManualPaymentForm = () => {
    setBankTransferDetails({
      bankName: '',
      accountNumber: '',
      bankBranchName: '',
      transactionNumber: '',
      paymentDate: '',
      paymentAmount: totalAmount,
      transactionReceiptFile: null,
    });
    setCheckPaymentDetails({
      checkNumber: '',
      bankName: '',
      branchName: '',
      paymentDate: '',
      paymentAmount: totalAmount,
      checkImageFile: null,
    });
    setCommentLog({
      comment: '',
      actionTakenId: '',
      nextStepId: '',
    });
    setValidationErrors({});
  };

  const fetchActionTakenList = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.BILLING_ACTION_LIST);
      if (isSuccessResponse(response.statusCode)) {
        setActionTakenList(
          response.data.map((item: { id: string; name: string }) => ({
            value: item.id,
            label: item.name,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching action taken list:', error);
    }
  };

  const fetchNextStepList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.BILLING_NEXT_STEP_LIST,
      );
      if (isSuccessResponse(response.statusCode)) {
        setNextStepList(
          response.data.map((item: { id: string; name: string }) => ({
            value: item.id,
            label: item.name,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching next step list:', error);
    }
  };

  const handlePayment = () => {
    if (paymentMethod) {
      onPayment(paymentMethod);
    }
  };

  const validateBankTransferField = (field: string, value: string): string => {
    const rules =
      validationRules.bankTransfer[
      field as keyof typeof validationRules.bankTransfer
      ];
    if (!rules) return '';

    const strValue = String(value).trim();

    if (field === 'paymentAmount') {
      if (strValue === '') return '';
      const numValue = parseFloat(strValue);
      if (isNaN(numValue)) {
        return 'Please enter a valid number';
      }
      if (numValue < rules.min || numValue > rules.max) {
        return `Amount must be between $${rules.min} and $${rules.max.toLocaleString()}`;
      }
      if (Math.abs(numValue - totalAmount) > 0.01) {
        return `Payment amount must equal the total amount of $${totalAmount.toFixed(2)}`;
      }
      return '';
    }

    if (strValue.length === 0) return '';

    if (strValue.length < rules.min) {
      return `Minimum ${rules.min} characters required`;
    }
    if (strValue.length > rules.max) {
      return `Maximum ${rules.max} characters allowed`;
    }

    return '';
  };

  const validateCheckPaymentField = (field: string, value: string): string => {
    const rules =
      validationRules.checkPayment[
      field as keyof typeof validationRules.checkPayment
      ];
    if (!rules) return '';

    const strValue = String(value).trim();

    if (field === 'paymentAmount') {
      if (strValue === '') return '';
      const numValue = parseFloat(strValue);
      if (isNaN(numValue)) {
        return 'Please enter a valid number';
      }
      if (numValue < rules.min || numValue > rules.max) {
        return `Amount must be between $${rules.min} and $${rules.max.toLocaleString()}`;
      }
      if (Math.abs(numValue - totalAmount) > 0.01) {
        return `Payment amount must equal the total amount of $${totalAmount.toFixed(2)}`;
      }
      return '';
    }

    if (strValue.length === 0) return '';

    if (strValue.length < rules.min) {
      return `Minimum ${rules.min} characters required`;
    }
    if (strValue.length > rules.max) {
      return `Maximum ${rules.max} characters allowed`;
    }

    return '';
  };

  const handleBankTransferChange = (field: string, value: string) => {
    const errorKey = `bt-${field}`;
    const error = validateBankTransferField(field, value);

    setValidationErrors(prev => ({
      ...prev,
      [errorKey]: error,
    }));

    setBankTransferDetails(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleCheckPaymentChange = (field: string, value: string) => {
    const errorKey = `cp-${field}`;
    const error = validateCheckPaymentField(field, value);

    setValidationErrors(prev => ({
      ...prev,
      [errorKey]: error,
    }));

    setCheckPaymentDetails(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const validateFile = (file: File | null, fieldName: string): string => {
    if (!file) {
      return `${fieldName} is required`;
    }

    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      return `File size must be 5MB or less`;
    }

    const allowedTypes = [
      'image/jpeg',
      'image/jpg',
      'image/png',
      'image/webp',
      'application/pdf',
    ];
    if (!allowedTypes.includes(file.type)) {
      return 'File must be an image (JPEG, PNG, WebP) or PDF';
    }

    return '';
  };

  const handleFileUpload = (
    field: string,
    file: File | null,
    paymentType: 'BANK_TRANSFER' | 'CHECK_PAYMENT',
    inputElement?: HTMLInputElement,
  ) => {
    const fieldName =
      field === 'transactionReceiptFile'
        ? 'Transaction Receipt'
        : 'Check Image/Receipt';
    const errorKey =
      paymentType === 'BANK_TRANSFER' ? `bt-${field}` : `cp-${field}`;

    const error = file ? validateFile(file, fieldName) : '';

    setValidationErrors(prev => ({
      ...prev,
      [errorKey]: error,
    }));

    if (!error && file) {
      if (paymentType === 'BANK_TRANSFER') {
        setBankTransferDetails(prev => ({
          ...prev,
          [field]: file,
        }));
      } else {
        setCheckPaymentDetails(prev => ({
          ...prev,
          [field]: file,
        }));
      }
    } else if (error && file && inputElement) {
      inputElement.value = '';
      toast.error(error);
    }
  };

  const handleCommentChange = (value: string) => {
    const rules = validationRules.paymentComment.comment;
    let error = '';

    if (value.trim().length > 0 && value.trim().length < rules.min) {
      error = `Minimum ${rules.min} characters required`;
    } else if (value.length > rules.max) {
      error = `Maximum ${rules.max} characters allowed`;
    }

    setValidationErrors(prev => ({
      ...prev,
      'payment-comment': error,
    }));

    setCommentLog(prev => ({
      ...prev,
      comment: value,
    }));
  };



  const validateManualPaymentForm = (): boolean => {
    // Validate comment log
    if (!commentLog.actionTakenId || !commentLog.nextStepId) {
      return false;
    }

    const commentRules = validationRules.paymentComment.comment;
    if (
      !commentLog.comment ||
      commentLog.comment.trim().length < commentRules.min ||
      commentLog.comment.trim().length > commentRules.max
    ) {
      return false;
    }

    if (manualPaymentMethod === 'BANK_TRANSFER') {
      const bt = bankTransferDetails;
      const rules = validationRules.bankTransfer;

      if (
        !bt.bankName ||
        !bt.accountNumber ||
        !bt.bankBranchName ||
        !bt.transactionNumber ||
        !bt.paymentDate ||
        !bt.paymentAmount ||
        !bt.transactionReceiptFile
      ) {
        return false;
      }

      if (
        bt.bankName.trim().length < rules.bankName.min ||
        bt.bankName.trim().length > rules.bankName.max
      )
        return false;
      if (
        bt.accountNumber.trim().length < rules.accountNumber.min ||
        bt.accountNumber.trim().length > rules.accountNumber.max
      )
        return false;
      if (
        bt.bankBranchName.trim().length < rules.bankBranchName.min ||
        bt.bankBranchName.trim().length > rules.bankBranchName.max
      )
        return false;
      if (
        bt.transactionNumber.trim().length < rules.transactionNumber.min ||
        bt.transactionNumber.trim().length > rules.transactionNumber.max
      )
        return false;

      const amount = parseFloat(String(bt.paymentAmount));
      if (
        isNaN(amount) ||
        amount < rules.paymentAmount.min ||
        amount > rules.paymentAmount.max
      )
        return false;
      if (Math.abs(amount - totalAmount) > 0.01) return false;
    }

    if (manualPaymentMethod === 'CHECK_PAYMENT') {
      const cp = checkPaymentDetails;
      const rules = validationRules.checkPayment;

      if (
        !cp.checkNumber ||
        !cp.bankName ||
        !cp.branchName ||
        !cp.paymentDate ||
        !cp.paymentAmount ||
        !cp.checkImageFile
      ) {
        return false;
      }

      if (
        cp.checkNumber.trim().length < rules.checkNumber.min ||
        cp.checkNumber.trim().length > rules.checkNumber.max
      )
        return false;
      if (
        cp.bankName.trim().length < rules.bankName.min ||
        cp.bankName.trim().length > rules.bankName.max
      )
        return false;
      if (
        cp.branchName.trim().length < rules.branchName.min ||
        cp.branchName.trim().length > rules.branchName.max
      )
        return false;

      const amount = parseFloat(String(cp.paymentAmount));
      if (
        isNaN(amount) ||
        amount < rules.paymentAmount.min ||
        amount > rules.paymentAmount.max
      )
        return false;
      if (Math.abs(amount - totalAmount) > 0.01) return false;
    }

    return true;
  };

  const handleManualPaymentSubmit = async () => {
    if (!validateManualPaymentForm()) {
      toast.error('Please complete all required fields');
      return;
    }

    setManualPaymentLoading(true);

    try {
      let fileUrl: string | null = null;

      // Upload file first
      if (manualPaymentMethod === 'BANK_TRANSFER') {
        if (bankTransferDetails.transactionReceiptFile) {
          const { url, error } = await uploadFile(bankTransferDetails.transactionReceiptFile);
          if (error) {
            toast.error(error);
            setManualPaymentLoading(false);
            return;
          }
          fileUrl = url;
        }
      } else if (manualPaymentMethod === 'CHECK_PAYMENT') {
        if (checkPaymentDetails.checkImageFile) {
          const { url, error } = await uploadFile(checkPaymentDetails.checkImageFile);
          if (error) {
            toast.error(error);
            setManualPaymentLoading(false);
            return;
          }
          fileUrl = url;
        }
      }

      // Prepare payload
      const payload: any = {
        paymentStatus: 'COMPLETED',
        completedPayment: {
          paymentMethod: manualPaymentMethod,
          commentLog: {
            invoiceId: invoiceId,
            comment: commentLog.comment.trim(),
            actionTakenId: commentLog.actionTakenId,
            nextStepId: commentLog.nextStepId,
            userName: userInfo?.email ?? '',
            userRole: role ?? '',
            approved: false,
          },
        },
        bankReceiptUrl: fileUrl,
      };

      if (manualPaymentMethod === 'BANK_TRANSFER') {
        payload.completedPayment.bankTransferDetails = {
          bankName: bankTransferDetails.bankName.trim(),
          accountNumber: bankTransferDetails.accountNumber.trim(),
          bankBranchName: bankTransferDetails.bankBranchName.trim(),
          transactionNumber: bankTransferDetails.transactionNumber.trim(),
          paymentDate: bankTransferDetails.paymentDate,
          paymentAmount: parseFloat(String(bankTransferDetails.paymentAmount)),
          transactionReceiptUrl: fileUrl,
        };
      } else if (manualPaymentMethod === 'CHECK_PAYMENT') {
        payload.completedPayment.checkPaymentDetails = {
          checkNumber: checkPaymentDetails.checkNumber.trim(),
          bankName: checkPaymentDetails.bankName.trim(),
          branchName: checkPaymentDetails.branchName.trim(),
          paymentDate: checkPaymentDetails.paymentDate,
          paymentAmount: parseFloat(String(checkPaymentDetails.paymentAmount)),
          checkImageUrl: fileUrl,
        };
      }

      const response = await apiClient.put(
        API_END_POINTS.UPDATE_INVOICE_PAYMENT.replace(':id', invoiceId),
        {
          data: payload,
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        toast.success('Manual payment processed successfully');
        onClose();
        onManualPaymentSuccess?.();
      } else {
        toast.error(response.message || 'Failed to process manual payment');
      }
    } catch (error) {
      console.error('Error processing manual payment:', error);
      toast.error('Failed to process manual payment');
    } finally {
      setManualPaymentLoading(false);
    }
  };

  const handleManualPaymentMethodChange = (
    method: 'BANK_TRANSFER' | 'CHECK_PAYMENT',
  ) => {
    setManualPaymentMethod(method);
    // Clear validation errors when switching methods
    setValidationErrors(prev => {
      const newErrors = { ...prev };
      Object.keys(newErrors).forEach(key => {
        if (
          key.startsWith('bt-') ||
          key.startsWith('cp-')
        ) {
          delete newErrors[key];
        }
      });
      return newErrors;
    });
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className={showManualPayment ? 'max-w-[70%]' : 'max-w-[40%]'}>
        <DialogHeader>
          <DialogTitle>
            {showManualPayment ? 'Manual Payment Entry' : 'Complete Payment'}
          </DialogTitle>
          <DialogDescription>
            {showManualPayment
              ? `Enter manual payment details for ${invoiceNo}`
              : `Select your payment method and complete the payment for ${invoiceNo}`}
          </DialogDescription>
        </DialogHeader>

        {!showManualPayment ? (
          <div className="space-y-4">
            <div className="flex flex-col items-start gap-3">
              <Label htmlFor="payment-method">Select Payment Method</Label>

              {/* List-based selection for Stripe and PayPal */}
              {paymentMethod === PaymentMethod.BANK_TRANSFER ? (
                <div className="w-full rounded-lg border border-card-border bg-card/50 p-4">
                  <p className="text-sm font-medium">Bank Transfer</p>
                </div>
              ) : paymentMethod === PaymentMethod.CHECK_PAYMENT ? (
                <div className="w-full rounded-lg border border-card-border bg-card/50 p-4">
                  <p className="text-sm font-medium">Check Payment</p>
                </div>
              ) : (
                <div className="flex w-full flex-col gap-3">
                  {/* Stripe Option */}
                  <button
                    type="button"
                    onClick={() => setPaymentMethod(PaymentMethod.STRIPE)}
                    className={`flex items-center justify-between gap-4 rounded-lg border p-4 transition-all duration-200 hover:border-primary/50 ${paymentMethod === PaymentMethod.STRIPE
                        ? 'border-primary bg-primary/10'
                        : 'border-card-border bg-transparent hover:bg-card/50'
                      }`}
                  >
                    <div className="flex items-center gap-4">
                      <div className="flex h-10 w-12 items-center justify-center">
                        <img
                          src={STRIPE_IMAGE}
                          alt="Stripe"
                          width={40}
                          height={40}
                        />
                      </div>
                      <span className="text-base font-normal">
                        Pay with Stripe
                      </span>
                    </div>
                    <div className="flex items-center">
                      {paymentMethod === PaymentMethod.STRIPE ? (
                        <div className="flex size-5 items-center justify-center rounded-full bg-primary">
                          <svg
                            className="size-3 text-primary-foreground"
                            fill="none"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="3"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path d="M5 13l4 4L19 7"></path>
                          </svg>
                        </div>
                      ) : (
                        <div className="size-5 rounded-full border-2 border-card-border"></div>
                      )}
                    </div>
                  </button>

                  {/* PayPal Option */}
                  <button
                    type="button"
                    onClick={() => setPaymentMethod(PaymentMethod.PAYPAL)}
                    className={`flex items-center justify-between gap-4 rounded-lg border p-4 transition-all duration-200 hover:border-primary/50 ${paymentMethod === PaymentMethod.PAYPAL
                        ? 'border-primary bg-primary/10'
                        : 'border-card-border bg-transparent hover:bg-card/50'
                      }`}
                  >
                    <div className="flex items-center gap-4">
                      <div className="flex h-10 w-12 items-center justify-center">
                        <img
                          src={PAYPAL_IMAGE}
                          alt="PayPal"
                          width={40}
                          height={40}
                        />
                      </div>
                      <span className="text-base font-normal">
                        Pay with PayPal
                      </span>
                    </div>
                    <div className="flex items-center">
                      {paymentMethod === PaymentMethod.PAYPAL ? (
                        <div className="flex size-5 items-center justify-center rounded-full bg-primary">
                          <svg
                            className="size-3 text-primary-foreground"
                            fill="none"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="3"
                            viewBox="0 0 24 24"
                            stroke="currentColor"
                          >
                            <path d="M5 13l4 4L19 7"></path>
                          </svg>
                        </div>
                      ) : (
                        <div className="size-5 rounded-full border-2 border-card-border"></div>
                      )}
                    </div>
                  </button>
                </div>
              )}

              <div className="flex gap-2 pt-4">
                <Button variant="outline" onClick={onClose} className="flex-1">
                  Cancel
                </Button>
                {isAspireAdmin && (
                  <Button
                    variant="secondary"
                    onClick={() => setShowManualPayment(true)}
                    className="flex-1"
                  >
                    Manual Payment
                  </Button>
                )}
                <Button
                  onClick={handlePayment}
                  className="flex-1"
                  disabled={!paymentMethod || loading}
                >
                  {loading ? 'Paying...' : 'Confirm Payment'}
                </Button>
              </div>
            </div>
          </div>
        ) : (
          /* Manual Payment Form for Aspire Admin */
          <div className="space-y-6 max-h-[70vh] overflow-y-auto pr-2">
            {/* Payment Method Selection */}
            <div className="space-y-4">
              <Label>Payment Method</Label>
              <div className="flex gap-4">
                <div
                  onClick={() => handleManualPaymentMethodChange('BANK_TRANSFER')}
                  className={`flex cursor-pointer flex-col items-center gap-1 rounded border border-card-border px-10 py-3 ${manualPaymentMethod === 'BANK_TRANSFER'
                    ? 'border-primary bg-primary text-white'
                    : ''
                    }`}
                >
                  <BanknoteIcon className="h-10 w-10 text-white" />
                  <p className="text-sm font-medium">Bank Transfer</p>
                  <p className="text-sm font-normal">Direct transfer</p>
                </div>

                <div
                  onClick={() => handleManualPaymentMethodChange('CHECK_PAYMENT')}
                  className={`flex cursor-pointer flex-col items-center gap-1 rounded border border-card-border px-10 py-3 ${manualPaymentMethod === 'CHECK_PAYMENT'
                    ? 'border-primary bg-primary text-white'
                    : ''
                    }`}
                >
                  <CreditCardIcon className="h-10 w-10 text-white" />
                  <p className="text-sm font-medium">Check Payment</p>
                  <p className="text-sm font-normal">Paper check</p>
                </div>
              </div>
            </div>

            {/* Bank Transfer Details */}
            {manualPaymentMethod === 'BANK_TRANSFER' && (
              <div className="rounded border border-card-border p-4">
                <p className="mb-4 text-lg font-medium text-primary">
                  Bank Transfer Details
                </p>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="bt-bankName">
                      Bank Name * (Min{' '}
                      {validationRules.bankTransfer.bankName.min}, Max{' '}
                      {validationRules.bankTransfer.bankName.max} characters)
                    </Label>
                    <Input
                      id="bt-bankName"
                      value={bankTransferDetails.bankName}
                      onChange={e =>
                        handleBankTransferChange('bankName', e.target.value)
                      }
                      placeholder="Enter bank name"
                      maxLength={validationRules.bankTransfer.bankName.max}
                      className={
                        validationErrors['bt-bankName']
                          ? 'border-red-500 focus:ring-red-500'
                          : ''
                      }
                    />
                    {validationErrors['bt-bankName'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['bt-bankName']}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="bt-accountNumber">
                      Account Number * (Min{' '}
                      {validationRules.bankTransfer.accountNumber.min}, Max{' '}
                      {validationRules.bankTransfer.accountNumber.max} characters)
                    </Label>
                    <Input
                      id="bt-accountNumber"
                      value={bankTransferDetails.accountNumber}
                      onChange={e =>
                        handleBankTransferChange('accountNumber', e.target.value)
                      }
                      placeholder="Enter account number"
                      maxLength={validationRules.bankTransfer.accountNumber.max}
                      className={
                        validationErrors['bt-accountNumber']
                          ? 'border-red-500 focus:ring-red-500'
                          : ''
                      }
                    />
                    {validationErrors['bt-accountNumber'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['bt-accountNumber']}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="bt-bankBranchName">
                      Bank Branch Name * (Min{' '}
                      {validationRules.bankTransfer.bankBranchName.min}, Max{' '}
                      {validationRules.bankTransfer.bankBranchName.max} characters)
                    </Label>
                    <Input
                      id="bt-bankBranchName"
                      value={bankTransferDetails.bankBranchName}
                      onChange={e =>
                        handleBankTransferChange('bankBranchName', e.target.value)
                      }
                      placeholder="Enter branch name"
                      maxLength={validationRules.bankTransfer.bankBranchName.max}
                      className={
                        validationErrors['bt-bankBranchName']
                          ? 'border-red-500 focus:ring-red-500'
                          : ''
                      }
                    />
                    {validationErrors['bt-bankBranchName'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['bt-bankBranchName']}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="bt-transactionNumber">
                      Transaction Number * (Min{' '}
                      {validationRules.bankTransfer.transactionNumber.min}, Max{' '}
                      {validationRules.bankTransfer.transactionNumber.max}{' '}
                      characters)
                    </Label>
                    <Input
                      id="bt-transactionNumber"
                      value={bankTransferDetails.transactionNumber}
                      onChange={e =>
                        handleBankTransferChange(
                          'transactionNumber',
                          e.target.value,
                        )
                      }
                      placeholder="Enter transaction number"
                      maxLength={validationRules.bankTransfer.transactionNumber.max}
                      className={
                        validationErrors['bt-transactionNumber']
                          ? 'border-red-500 focus:ring-red-500'
                          : ''
                      }
                    />
                    {validationErrors['bt-transactionNumber'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['bt-transactionNumber']}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="bt-paymentDate">Payment Date *</Label>
                    <Input
                      id="bt-paymentDate"
                      type="date"
                      value={bankTransferDetails.paymentDate}
                      onChange={e =>
                        handleBankTransferChange('paymentDate', e.target.value)
                      }
                      onClick={e => {
                        const input = e.target as HTMLInputElement;
                        input.showPicker();
                      }}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="bt-paymentAmount">
                      Payment Amount * (Must equal Total: $
                      {totalAmount.toFixed(2)})
                    </Label>
                    <Input
                      id="bt-paymentAmount"
                      type="number"
                      value={bankTransferDetails.paymentAmount}
                      onChange={e =>
                        handleBankTransferChange('paymentAmount', e.target.value)
                      }
                      placeholder={`Enter payment amount (Total: $${totalAmount.toFixed(2)})`}
                      min={validationRules.bankTransfer.paymentAmount.min}
                      max={validationRules.bankTransfer.paymentAmount.max}
                      step="0.01"
                      className={
                        validationErrors['bt-paymentAmount']
                          ? 'border-red-500 focus:ring-red-500'
                          : ''
                      }
                    />
                    {validationErrors['bt-paymentAmount'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['bt-paymentAmount']}
                      </p>
                    )}
                  </div>

                  <div className="col-span-2 space-y-2">
                    <Label htmlFor="bt-receipt">Transaction Receipt *</Label>
                    <input
                      id="bt-receipt"
                      type="file"
                      accept="image/*,.pdf"
                      onChange={e =>
                        handleFileUpload(
                          'transactionReceiptFile',
                          e.target.files?.[0] || null,
                          'BANK_TRANSFER',
                          e.target,
                        )
                      }
                      className={`w-full rounded-md border px-3 py-2 focus:outline-none focus:ring-2 ${validationErrors['bt-transactionReceiptFile']
                        ? 'border-red-500 focus:ring-red-500'
                        : 'border-card-border focus:ring-primary'
                        }`}
                    />
                    <p className="text-xs text-gray-500">
                      Upload image or PDF (Max 5MB)
                    </p>
                    {validationErrors['bt-transactionReceiptFile'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['bt-transactionReceiptFile']}
                      </p>
                    )}
                    {bankTransferDetails.transactionReceiptFile && (
                      <p className="text-xs text-green-600">
                        File selected:{' '}
                        {bankTransferDetails.transactionReceiptFile.name}
                      </p>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* Check Payment Details */}
            {manualPaymentMethod === 'CHECK_PAYMENT' && (
              <div className="rounded border border-card-border p-4">
                <p className="mb-4 text-lg font-medium text-primary">
                  Check Payment Details
                </p>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="cp-checkNumber">
                      Check Number * (Min{' '}
                      {validationRules.checkPayment.checkNumber.min}, Max{' '}
                      {validationRules.checkPayment.checkNumber.max} characters)
                    </Label>
                    <Input
                      id="cp-checkNumber"
                      value={checkPaymentDetails.checkNumber}
                      onChange={e =>
                        handleCheckPaymentChange('checkNumber', e.target.value)
                      }
                      placeholder="Enter check number"
                      maxLength={validationRules.checkPayment.checkNumber.max}
                      className={
                        validationErrors['cp-checkNumber']
                          ? 'border-red-500 focus:ring-red-500'
                          : ''
                      }
                    />
                    {validationErrors['cp-checkNumber'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['cp-checkNumber']}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="cp-bankName">
                      Bank Name * (Min{' '}
                      {validationRules.checkPayment.bankName.min}, Max{' '}
                      {validationRules.checkPayment.bankName.max} characters)
                    </Label>
                    <Input
                      id="cp-bankName"
                      value={checkPaymentDetails.bankName}
                      onChange={e =>
                        handleCheckPaymentChange('bankName', e.target.value)
                      }
                      placeholder="Enter bank name"
                      maxLength={validationRules.checkPayment.bankName.max}
                      className={
                        validationErrors['cp-bankName']
                          ? 'border-red-500 focus:ring-red-500'
                          : ''
                      }
                    />
                    {validationErrors['cp-bankName'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['cp-bankName']}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="cp-branchName">
                      Branch Name * (Min{' '}
                      {validationRules.checkPayment.branchName.min}, Max{' '}
                      {validationRules.checkPayment.branchName.max} characters)
                    </Label>
                    <Input
                      id="cp-branchName"
                      value={checkPaymentDetails.branchName}
                      onChange={e =>
                        handleCheckPaymentChange('branchName', e.target.value)
                      }
                      placeholder="Enter branch name"
                      maxLength={validationRules.checkPayment.branchName.max}
                      className={
                        validationErrors['cp-branchName']
                          ? 'border-red-500 focus:ring-red-500'
                          : ''
                      }
                    />
                    {validationErrors['cp-branchName'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['cp-branchName']}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="cp-paymentDate">Payment Date *</Label>
                    <Input
                      id="cp-paymentDate"
                      type="date"
                      value={checkPaymentDetails.paymentDate}
                      onChange={e =>
                        handleCheckPaymentChange('paymentDate', e.target.value)
                      }
                      onClick={e => {
                        const input = e.target as HTMLInputElement;
                        input.showPicker();
                      }}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="cp-paymentAmount">
                      Payment Amount * (Must equal Total: $
                      {totalAmount.toFixed(2)})
                    </Label>
                    <Input
                      id="cp-paymentAmount"
                      type="number"
                      value={checkPaymentDetails.paymentAmount}
                      onChange={e =>
                        handleCheckPaymentChange('paymentAmount', e.target.value)
                      }
                      placeholder={`Enter payment amount (Total: $${totalAmount.toFixed(2)})`}
                      min={validationRules.checkPayment.paymentAmount.min}
                      max={validationRules.checkPayment.paymentAmount.max}
                      step="0.01"
                      className={
                        validationErrors['cp-paymentAmount']
                          ? 'border-red-500 focus:ring-red-500'
                          : ''
                      }
                    />
                    {validationErrors['cp-paymentAmount'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['cp-paymentAmount']}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="cp-checkImage">Check Image/Receipt *</Label>
                    <input
                      id="cp-checkImage"
                      type="file"
                      accept="image/*,.pdf"
                      onChange={e =>
                        handleFileUpload(
                          'checkImageFile',
                          e.target.files?.[0] || null,
                          'CHECK_PAYMENT',
                          e.target,
                        )
                      }
                      className={`w-full rounded-md border px-3 py-2 focus:outline-none focus:ring-2 ${validationErrors['cp-checkImageFile']
                        ? 'border-red-500 focus:ring-red-500'
                        : 'border-card-border focus:ring-primary'
                        }`}
                    />
                    <p className="text-xs text-gray-500">
                      Upload image or PDF (Max 5MB)
                    </p>
                    {validationErrors['cp-checkImageFile'] && (
                      <p className="text-sm text-red-500">
                        {validationErrors['cp-checkImageFile']}
                      </p>
                    )}
                    {checkPaymentDetails.checkImageFile && (
                      <p className="text-xs text-green-600">
                        File selected: {checkPaymentDetails.checkImageFile.name}
                      </p>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* Payment Comment Section */}
            <div className="rounded border border-card-border p-4">
              <p className="mb-4 text-lg font-medium text-primary">
                Payment Comment
              </p>
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="actionTaken">Action Taken *</Label>
                  <Select
                    value={commentLog.actionTakenId}
                    onValueChange={value =>
                      setCommentLog(prev => ({
                        ...prev,
                        actionTakenId: value,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select action taken" />
                    </SelectTrigger>
                    <SelectContent>
                      {actionTakenList.map(action => (
                        <SelectItem key={action.value} value={action.value}>
                          {action.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="nextStep">Next Step *</Label>
                  <Select
                    value={commentLog.nextStepId}
                    onValueChange={value =>
                      setCommentLog(prev => ({
                        ...prev,
                        nextStepId: value,
                      }))
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select next step" />
                    </SelectTrigger>
                    <SelectContent>
                      {nextStepList.map(step => (
                        <SelectItem key={step.value} value={step.value}>
                          {step.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="col-span-2 space-y-2">
                  <Label htmlFor="comment">
                    Comment * (Min {validationRules.paymentComment.comment.min},
                    Max {validationRules.paymentComment.comment.max} characters)
                  </Label>
                  <Textarea
                    id="comment"
                    value={commentLog.comment}
                    onChange={e => handleCommentChange(e.target.value)}
                    placeholder="Enter comment"
                    maxLength={validationRules.paymentComment.comment.max}
                    className={
                      validationErrors['payment-comment']
                        ? 'border-red-500 focus:ring-red-500'
                        : ''
                    }
                  />
                  {validationErrors['payment-comment'] && (
                    <p className="text-sm text-red-500">
                      {validationErrors['payment-comment']}
                    </p>
                  )}
                  <p className="text-xs text-gray-500">
                    {commentLog.comment.length} /{' '}
                    {validationRules.paymentComment.comment.max} characters
                  </p>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2 pt-4">
              <Button
                variant="outline"
                onClick={() => {
                  setShowManualPayment(false);
                  resetManualPaymentForm();
                }}
                className="flex-1"
              >
                Back
              </Button>
              <Button
                onClick={handleManualPaymentSubmit}
                className="flex-1"
                disabled={
                  !validateManualPaymentForm() ||
                  manualPaymentLoading ||
                  uploadingFile
                }
              >
                {manualPaymentLoading || uploadingFile ? (
                  <>
                    <Loader2Icon className="mr-2 h-4 w-4 animate-spin" />
                    Processing...
                  </>
                ) : (
                  'Submit Manual Payment'
                )}
              </Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default ClientPaymentModal;
