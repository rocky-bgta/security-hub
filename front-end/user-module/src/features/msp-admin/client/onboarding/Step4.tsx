import { useEffect, useState } from 'react';

import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { Separator } from 'common/Separator';
import {
  DiscountType,
  IBillingAction,
  IBillingNextStep,
  IClientOnboarding,
  IInvoice,
  IPaymentComment,
} from 'models/Client';
import {
  BanknoteIcon,
  CheckIcon,
  CreditCardIcon,
  ListCheckIcon,
  Loader2Icon,
} from 'lucide-react';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from 'hooks/UseAPI';
import { Textarea } from 'common/Textarea';
import { toast } from 'react-toastify';
import { isSuccessResponse } from 'utils/Helper';
import { IVatConfiguration } from 'models/Vat';
import { ICoupon } from 'models/Coupon';

interface IProps {
  data: IInvoice;
  formData: IClientOnboarding;
  onUpdate: (data: IInvoice, paymentComment: IPaymentComment) => void;
  paymentComment: IPaymentComment;
  onNext: () => void;
  onPrevious: () => void;
}

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

const Step4 = ({
  data,
  formData,
  onUpdate,
  paymentComment,
  onNext,
  onPrevious,
}: IProps) => {
  const apiClient = useAPI();
  const [invoiceData, setInvoiceData] = useState<IInvoice>({
    ...data,
  });
  const [couponDiscount, setCouponDiscount] = useState<number>(0);
  const [paymentCommentData, setPaymentCommentData] = useState<IPaymentComment>(
    {
      ...paymentComment,
    },
  );
  const [actionTakenList, setActionTakenList] = useState<Array<IBillingAction>>(
    [],
  );
  const [nextStepList, setNextStepList] = useState<Array<IBillingNextStep>>([]);
  const [validationErrors, setValidationErrors] = useState<
    Record<string, string>
  >({});
  const [couponLoading, setCouponLoading] = useState<boolean>(false);
  const [coupon, setCoupon] = useState<ICoupon | null>(null);

  useEffect(() => {
    fetchActionTakenList();
    fetchNextStepList();
    fetchVatRate();
  }, []);

  const fetchVatRate = async () => {
    try {
      const response: IResponse<IVatConfiguration> = await apiClient.get(
        API_END_POINTS.BILLING_VAT_RATE_BY_COUNTRY.replace(
          ':countryId',
          formData.billing.country,
        ),
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error('Failed to fetch VAT data');
      }

      const vatConfig = response.data;

      let vatRate = vatConfig.defaultVatRate;

      // If region based VAT
      if (vatConfig.regionBased && vatConfig.regions.length > 0) {
        const matchedRegion = vatConfig.regions.find(
          region => region.id === formData.billing.stateProvince,
        );

        if (matchedRegion) {
          vatRate = matchedRegion.vatRate;
        }
      }

      // finally update invoice data
      setInvoiceData(prev => ({
        ...prev,
        vatRate,
      }));
    } catch (error) {
      console.error('Error fetching vat rate:', error);
    }
  };

  const fetchActionTakenList = async () => {
    try {
      const response: IResponse<Array<IBillingAction>> = await apiClient.get(
        API_END_POINTS.BILLING_ACTION_LIST,
      );
      setActionTakenList(response.data);
    } catch (error) {
      console.error('Error fetching action taken list:', error);
    }
  };
  const fetchNextStepList = async () => {
    try {
      const response: IResponse<Array<IBillingNextStep>> = await apiClient.get(
        API_END_POINTS.BILLING_NEXT_STEP_LIST,
      );
      setNextStepList(response.data);
    } catch (error) {
      console.error('Error fetching next step list:', error);
    }
  };

  useEffect(() => {
    calculateTotal();
  }, [
    invoiceData.discountType,
    invoiceData.discountValue,
    coupon,
    invoiceData.vatRate,
    formData.productSelections,
  ]);

  const calculateSubtotal = () => {
    if (!formData.productSelections) return 0;

    let subtotal = 0;

    formData.productSelections.forEach(selectedProduct => {
      const totalLicensePrice =
        selectedProduct.pricePerLicense * selectedProduct.licenseCount;
      subtotal += totalLicensePrice * selectedProduct.validityPeriod;
    });

    return subtotal;
  };

  // Calculate subtotal for products/packages that match coupon restrictions
  const calculateCouponEligibleSubtotal = () => {
    if (!formData.productSelections || !coupon || !coupon.productRestrictions)
      return 0;

    let eligibleSubtotal = 0;

    formData.productSelections.forEach(selectedProduct => {
      // Check if this product/package is in the coupon's productRestrictions
      const isEligible = coupon.productRestrictions.some(
        restriction =>
          restriction.productId === selectedProduct.productId &&
          restriction.packageId === selectedProduct.packageId,
      );

      if (isEligible) {
        const totalLicensePrice =
          selectedProduct.pricePerLicense * selectedProduct.licenseCount;
        eligibleSubtotal += totalLicensePrice * selectedProduct.validityPeriod;
      }
    });

    return eligibleSubtotal;
  };

  const calculateTotal = () => {
    const subtotal = calculateSubtotal();

    // Step 1: Calculate coupon discount only for eligible products/packages (applied first)
    let couponAmount = 0;
    if (coupon && coupon.active) {
      const eligibleSubtotal = calculateCouponEligibleSubtotal();

      if (eligibleSubtotal > 0) {
        if (coupon.type === 'PERCENTAGE') {
          couponAmount = (eligibleSubtotal * coupon.value) / 100;
        } else if (coupon.type === 'FIXED') {
          couponAmount = coupon.value;
          // Ensure coupon doesn't exceed eligible subtotal
          couponAmount = Math.min(couponAmount, eligibleSubtotal);
        }
      }
    }

    // Step 2: Calculate amount after coupon deduction
    const afterCoupon = subtotal - couponAmount;

    // Step 3: Calculate discount based on type (applied to remaining amount after coupon)
    let discountAmount = 0;
    let discountPercentage = 0;

    if (invoiceData.discountType === DiscountType.PERCENTAGE) {
      discountPercentage = invoiceData.discountValue;
      discountAmount = (afterCoupon * discountPercentage) / 100;
    } else {
      // FLAT amount
      discountAmount = invoiceData.discountValue;
      discountPercentage =
        afterCoupon > 0 ? (discountAmount / afterCoupon) * 100 : 0;
    }

    // Step 4: Calculate final amount after both coupon and discount
    const afterDiscounts = afterCoupon - discountAmount;
    const vatAmount = (afterDiscounts * invoiceData.vatRate) / 100;
    const total = afterDiscounts + vatAmount;

    setInvoiceData(prev => ({
      ...prev,
      subtotal,
      discountPercentage,
      discountAmount,
      vatAmount,
      totalAmount: Math.max(0, total),
    }));

    // Update couponDiscount state for display purposes
    setCouponDiscount(couponAmount);
  };

  const applyCoupon = () => {
    fetchCouponByCode();
  };

  const fetchCouponByCode = async () => {
    try {
      setCouponLoading(true);
      const response = await apiClient.get(
        API_END_POINTS.GET_COUPON_BY_CODE.replace(
          ':code',
          invoiceData.couponCode,
        ),
      );

      if (isSuccessResponse(response.statusCode)) {
        const fetchedCoupon = response.data;

        // Check if coupon is active
        if (!fetchedCoupon.active) {
          toast.error('This coupon is not active');
          setCoupon(null);
          setCouponDiscount(0);
          return;
        }

        // Check if any selected product/package matches coupon restrictions
        const hasMatchingProduct = formData.productSelections?.some(
          selectedProduct =>
            fetchedCoupon.productRestrictions?.some(
              (restriction: { productId: string; packageId: string }) =>
                restriction.productId === selectedProduct.productId &&
                restriction.packageId === selectedProduct.packageId,
            ),
        );

        if (
          !hasMatchingProduct &&
          fetchedCoupon.productRestrictions?.length > 0
        ) {
          toast.warning(
            'This coupon is not applicable to your selected products/packages',
          );
        } else {
          toast.success('Coupon applied successfully');
        }
        setCoupon(fetchedCoupon);
      } else {
        toast.error('Invalid coupon code');
        setCoupon(null);
        setCouponDiscount(0);
      }
    } catch (error) {
      console.error('Error fetching coupon by code:', error);
      toast.error(
        'Failed to apply coupon. Please check the code and try again.',
      );
      setCoupon(null);
      setCouponDiscount(0);
    } finally {
      setCouponLoading(false);
    }
  };

  const handlePaymentStatusChange = (status: IInvoice['paymentStatus']) => {
    setInvoiceData(prev => ({
      ...prev,
      paymentStatus: status as 'COMPLETED' | 'PENDING',
      completedPayment: {
        ...prev.completedPayment!,
        paymentMethod: 'BANK_TRANSFER',
      },
    }));
  };

  const handlePaymentMethodChange = (
    method: IInvoice['completedPayment']['paymentMethod'],
  ) => {
    const totalAmount = invoiceData.totalAmount || 0;
    const methodStr = method as unknown as 'BANK_TRANSFER' | 'CHECK_PAYMENT';

    if (methodStr === 'BANK_TRANSFER') {
      setInvoiceData(prev => ({
        ...prev,
        completedPayment: {
          ...prev.completedPayment!,
          paymentMethod: methodStr,
          bankTransferDetails: {
            ...prev.completedPayment?.bankTransferDetails!,
            paymentAmount: totalAmount,
          },
        },
      }));
      // Clear validation errors when switching methods
      setValidationErrors(prev => {
        const newErrors = { ...prev };
        Object.keys(newErrors).forEach(key => {
          if (key.startsWith('cp-')) delete newErrors[key];
        });
        return newErrors;
      });
    } else if (methodStr === 'CHECK_PAYMENT') {
      setInvoiceData(prev => ({
        ...prev,
        completedPayment: {
          ...prev.completedPayment!,
          paymentMethod: methodStr,
          checkPaymentDetails: {
            ...prev.completedPayment?.checkPaymentDetails!,
            paymentAmount: totalAmount,
          },
        },
      }));
      // Clear validation errors when switching methods
      setValidationErrors(prev => {
        const newErrors = { ...prev };
        Object.keys(newErrors).forEach(key => {
          if (key.startsWith('bt-')) delete newErrors[key];
        });
        return newErrors;
      });
    }
  };

  const validateBankTransferField = (
    field: string,
    value: string | File | null,
  ): string => {
    if (value === null || value instanceof File) return '';

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
      // Validate that payment amount equals total amount
      const totalAmount = invoiceData.totalAmount || 0;
      if (Math.abs(numValue - totalAmount) > 0.01) {
        return `Payment amount must equal the total amount of $${totalAmount.toFixed(2)}`;
      }
      return '';
    }

    if (strValue.length === 0) return '';

    if (strValue.length < rules.min) {
      return `Minimum ${rules.min} characters required (current: ${strValue.length})`;
    }
    if (strValue.length > rules.max) {
      return `Maximum ${rules.max} characters allowed (current: ${strValue.length})`;
    }

    return '';
  };

  const handleBankTransferChange = (
    field: string,
    value: string | File | null,
  ) => {
    const errorKey = `bt-${field}`;
    const error = validateBankTransferField(field, value);

    setValidationErrors(prev => ({
      ...prev,
      [errorKey]: error,
    }));

    setInvoiceData(prev => ({
      ...prev,
      completedPayment: {
        ...prev.completedPayment!,
        bankTransferDetails: {
          ...prev.completedPayment?.bankTransferDetails!,
          [field]: value,
        },
        [field]: value,
      },
    }));
  };

  const validateCheckPaymentField = (
    field: string,
    value: string | File | null,
  ): string => {
    if (value === null || value instanceof File) return '';

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
      // Validate that payment amount equals total amount
      const totalAmount = invoiceData.totalAmount || 0;
      if (Math.abs(numValue - totalAmount) > 0.01) {
        return `Payment amount must equal the total amount of $${totalAmount.toFixed(2)}`;
      }
      return '';
    }

    if (strValue.length === 0) return '';

    if (strValue.length < rules.min) {
      return `Minimum ${rules.min} characters required (current: ${strValue.length})`;
    }
    if (strValue.length > rules.max) {
      return `Maximum ${rules.max} characters allowed (current: ${strValue.length})`;
    }

    return '';
  };

  const handleCheckPaymentChange = (
    field: string,
    value: string | File | null,
  ) => {
    const errorKey = `cp-${field}`;
    const error = validateCheckPaymentField(field, value);

    setValidationErrors(prev => ({
      ...prev,
      [errorKey]: error,
    }));

    setInvoiceData(prev => ({
      ...prev,
      completedPayment: {
        ...prev.completedPayment!,
        checkPaymentDetails: {
          ...prev.completedPayment?.checkPaymentDetails!,
          [field]: value,
        },
      },
    }));
  };

  const validateFile = (file: File | null, fieldName: string): string => {
    if (!file) {
      return `${fieldName} is required`;
    }

    // Check file size (5MB = 5 * 1024 * 1024 bytes)
    const maxSize = 5 * 1024 * 1024; // 5MB (5,242,880 bytes)
    if (file.size > maxSize) {
      toast.error(
        `File size must be 5MB or less (current: ${Math.round(file.size / (1024 * 1024))}MB)`,
      );
      return `File size must be 5MB or less (current: ${Math.round(file.size / (1024 * 1024))}MB)`;
    }

    // Check file type
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

    const error = validateFile(file, fieldName);

    setValidationErrors(prev => ({
      ...prev,
      [errorKey]: error,
    }));

    // Only set the file in state if validation passes
    if (!error && file) {
      // Valid file - set it in state
      if (paymentType === 'BANK_TRANSFER') {
        handleBankTransferChange(field, file);
      } else if (paymentType === 'CHECK_PAYMENT') {
        handleCheckPaymentChange(field, file);
      }
    } else if (error && file && inputElement) {
      // File exists but is invalid - clear the input and state
      inputElement.value = '';
      if (paymentType === 'BANK_TRANSFER') {
        handleBankTransferChange(field, null);
      } else if (paymentType === 'CHECK_PAYMENT') {
        handleCheckPaymentChange(field, null);
      }
    } else if (!file) {
      // File is null - clear from state (user removed the file)
      if (paymentType === 'BANK_TRANSFER') {
        handleBankTransferChange(field, null);
      } else if (paymentType === 'CHECK_PAYMENT') {
        handleCheckPaymentChange(field, null);
      }
    }
  };

  const validateForm = (): boolean => {
    if (!invoiceData.paymentStatus) return false;

    if (invoiceData.paymentStatus === 'PENDING') {
      return true;
    }

    if (invoiceData.paymentStatus === 'COMPLETED') {
      if (!invoiceData.completedPayment?.paymentMethod) return false;
      if (!paymentCommentData.actionTakenId) return false;
      if (!paymentCommentData.nextStepId) return false;

      // Validate payment comment
      const commentRules = validationRules.paymentComment.comment;
      if (
        !paymentCommentData.comment ||
        paymentCommentData.comment.trim().length < commentRules.min ||
        paymentCommentData.comment.trim().length > commentRules.max
      ) {
        return false;
      }

      if (invoiceData.completedPayment?.paymentMethod === 'BANK_TRANSFER') {
        const bt = invoiceData.completedPayment?.bankTransferDetails!;
        const rules = validationRules.bankTransfer;

        // Check required fields exist
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

        // Validate field lengths
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

        // Validate payment amount
        const amount = parseFloat(String(bt.paymentAmount));
        if (
          isNaN(amount) ||
          amount < rules.paymentAmount.min ||
          amount > rules.paymentAmount.max
        )
          return false;

        // Validate that payment amount equals total amount
        const totalAmount = invoiceData.totalAmount || 0;
        if (Math.abs(amount - totalAmount) > 0.01) {
          return false;
        }
      }

      if (invoiceData.completedPayment?.paymentMethod === 'CHECK_PAYMENT') {
        const cp = invoiceData.completedPayment?.checkPaymentDetails!;
        const rules = validationRules.checkPayment;

        // Check required fields exist
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

        // Validate field lengths
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

        // Validate payment amount
        const amount = parseFloat(String(cp.paymentAmount));
        if (
          isNaN(amount) ||
          amount < rules.paymentAmount.min ||
          amount > rules.paymentAmount.max
        )
          return false;

        // Validate that payment amount equals total amount
        const totalAmount = invoiceData.totalAmount || 0;
        if (Math.abs(amount - totalAmount) > 0.01) {
          return false;
        }
      }
    }

    return true;
  };

  const handleSave = () => {
    // Validate all fields before saving
    if (invoiceData.paymentStatus === 'COMPLETED') {
      if (invoiceData.completedPayment?.paymentMethod === 'BANK_TRANSFER') {
        const bt = invoiceData.completedPayment?.bankTransferDetails!;
        if (bt) {
          handleBankTransferChange('bankName', bt.bankName || '');
          handleBankTransferChange('accountNumber', bt.accountNumber || '');
          handleBankTransferChange('bankBranchName', bt.bankBranchName || '');
          handleBankTransferChange(
            'transactionNumber',
            bt.transactionNumber || '',
          );
          handleBankTransferChange(
            'paymentAmount',
            bt.paymentAmount ? String(bt.paymentAmount) : '',
          );
        }
      } else if (
        invoiceData.completedPayment?.paymentMethod === 'CHECK_PAYMENT'
      ) {
        const cp = invoiceData.completedPayment?.checkPaymentDetails!;
        if (cp) {
          handleCheckPaymentChange('checkNumber', cp.checkNumber || '');
          handleCheckPaymentChange('bankName', cp.bankName || '');
          handleCheckPaymentChange('branchName', cp.branchName || '');
          handleCheckPaymentChange(
            'paymentAmount',
            cp.paymentAmount ? String(cp.paymentAmount) : '',
          );
        }
      }

      // Validate payment comment
      if (paymentCommentData.comment) {
        const rules = validationRules.paymentComment.comment;
        let error = '';
        if (
          paymentCommentData.comment.trim().length > 0 &&
          paymentCommentData.comment.trim().length < rules.min
        ) {
          error = `Minimum ${rules.min} characters required`;
        } else if (paymentCommentData.comment.length > rules.max) {
          error = `Maximum ${rules.max} characters allowed`;
        }
        setValidationErrors(prev => ({
          ...prev,
          'payment-comment': error,
        }));
      }
    }

    if (!validateForm()) {
      const hasErrors = Object.values(validationErrors).some(error => error);
      if (hasErrors) {
        alert(
          'Please fix the validation errors in the form before proceeding.',
        );
      } else {
        alert(
          'Please complete all required fields for the selected payment option.',
        );
      }
      return;
    }

    const updatedInvoiceData = {
      ...invoiceData,
      completedPayment: {
        ...invoiceData.completedPayment!,
        bankTransferDetails: {
          ...invoiceData.completedPayment?.bankTransferDetails!,
        },
        checkPaymentDetails: {
          ...invoiceData.completedPayment?.checkPaymentDetails!,
        },
      },
    };

    onUpdate(updatedInvoiceData, paymentCommentData);
    onNext();
  };

  const getPaymentDisplayText = () => {
    if (!invoiceData.paymentStatus) return '';

    if (invoiceData.paymentStatus === 'PENDING') {
      return 'Pay Online';
    }

    if (invoiceData.paymentStatus === 'COMPLETED') {
      if (!invoiceData.completedPayment?.paymentMethod)
        return 'Completed Payment';
      return `Completed Payment - ${invoiceData.completedPayment?.paymentMethod === 'BANK_TRANSFER' ? 'Bank Transfer' : 'Check Payment'}`;
    }

    return '';
  };

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle className="text-2xl font-bold">Invoice Generation</CardTitle>
        <p className="text-gray-400">Generate and review your invoice</p>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>Discount Type</Label>
              <div className="flex gap-4">
                <RadioGroup
                  defaultValue={
                    invoiceData.discountType || DiscountType.PERCENTAGE
                  }
                  className="grid-cols-2"
                  onValueChange={(e: DiscountType) =>
                    setInvoiceData(prev => ({
                      ...prev,
                      discountType: e,
                      discountValue: 0,
                    }))
                  }
                >
                  <div className="flex items-center space-x-2">
                    <RadioGroupItem
                      value={DiscountType.PERCENTAGE}
                      checked={
                        invoiceData.discountType === DiscountType.PERCENTAGE
                      }
                      id="percentage"
                    />
                    <Label htmlFor="percentage" className="cursor-pointer">
                      Percentage Amount
                    </Label>
                  </div>
                  <div className="flex items-center space-x-2">
                    <RadioGroupItem
                      value={DiscountType.FLAT}
                      checked={invoiceData.discountType === DiscountType.FLAT}
                      id="flat"
                    />
                    <Label htmlFor="flat" className="cursor-pointer">
                      Flat Amount
                    </Label>
                  </div>
                </RadioGroup>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="discountValue">
                Discount{' '}
                {invoiceData.discountType === DiscountType.PERCENTAGE
                  ? '(%)'
                  : '($)'}
              </Label>
              <Input
                id="discountValue"
                type="number"
                value={invoiceData.discountValue}
                onChange={e =>
                  setInvoiceData(prev => ({
                    ...prev,
                    discountValue: parseFloat(e.target.value) || 0,
                  }))
                }
                placeholder="0"
                min="0"
                max={
                  invoiceData.discountType === DiscountType.PERCENTAGE
                    ? 100
                    : undefined
                }
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="coupon">Coupon Code</Label>
              <div className="flex space-x-2">
                <Input
                  id="coupon"
                  value={invoiceData.couponCode}
                  onChange={e =>
                    setInvoiceData(prev => ({
                      ...prev,
                      couponCode: e.target.value,
                    }))
                  }
                  placeholder="Enter coupon code"
                />
                <Button
                  variant="default"
                  onClick={applyCoupon}
                  disabled={couponLoading}
                >
                  {couponLoading ? (
                    <Loader2Icon className="size-4 animate-spin" />
                  ) : (
                    'Apply'
                  )}
                </Button>
              </div>
              {couponDiscount > 0 && (
                <div className="space-y-1">
                  <p className="text-sm text-green-600">
                    Coupon applied: ${couponDiscount.toFixed(2)} off
                  </p>
                </div>
              )}
              {coupon &&
                couponDiscount === 0 &&
                coupon.productRestrictions &&
                coupon.productRestrictions.length > 0 && (
                  <p className="text-sm text-yellow-600">
                    Coupon loaded but no matching products/packages selected
                  </p>
                )}
            </div>

            {/* <div className="space-y-2">  // VAT Rate is only change Super Admin
              <Label htmlFor="vat">VAT Rate (%)</Label>
              <Input
                id="vat"
                type="number"
                value={invoiceData.vatRate}
                onChange={e =>
                  setInvoiceData(prev => ({
                    ...prev,
                    vatRate: parseFloat(e.target.value) || 0,
                  }))
                }
                placeholder="0"
                min="0"
                max="100"
              />
            </div> */}

            {/* Step 1: Payment Status */}
            <div className="space-y-2">
              <Label>Payment Status</Label>
              <div className="flex gap-2">
                <div className="flex items-center space-x-2">
                  <Button
                    variant={
                      invoiceData.paymentStatus === 'PENDING'
                        ? 'default'
                        : 'outline'
                    }
                    size="sm"
                    onClick={() => handlePaymentStatusChange('PENDING')}
                  >
                    <ListCheckIcon className="size-4" />
                    Online Payment
                  </Button>
                </div>
                <div className="flex items-center space-x-2">
                  <div className="flex items-center space-x-2">
                    <Button
                      variant={
                        invoiceData.paymentStatus === 'COMPLETED'
                          ? 'default'
                          : 'outline'
                      }
                      size="sm"
                      onClick={() => handlePaymentStatusChange('COMPLETED')}
                    >
                      <CheckIcon className="size-4" />
                      Manual Payment
                    </Button>
                  </div>
                </div>
              </div>
            </div>

            {/* Step 2: Payment Method (only if Completed Payment is selected) */}
            {invoiceData.paymentStatus === 'COMPLETED' && (
              <div className="space-y-4">
                <Label>Payment Method</Label>
                <div className="flex gap-4">
                  <div
                    onClick={() => handlePaymentMethodChange('BANK_TRANSFER')}
                    className={`flex cursor-pointer flex-col items-center gap-1 rounded border border-card-border px-10 py-3 ${
                      invoiceData.completedPayment?.paymentMethod ===
                      'BANK_TRANSFER'
                        ? 'border-primary bg-primary text-white'
                        : ''
                    }`}
                  >
                    <BanknoteIcon className="size-10 text-white" />
                    <p className="text-sm font-medium">Bank Transfer</p>
                    <p className="text-sm font-normal">Direct transfer</p>
                  </div>

                  <div
                    onClick={() => handlePaymentMethodChange('CHECK_PAYMENT')}
                    className={`flex cursor-pointer flex-col items-center gap-1 rounded border border-card-border px-10 py-3 ${
                      invoiceData.completedPayment?.paymentMethod ===
                      'CHECK_PAYMENT'
                        ? 'border-primary bg-primary text-white'
                        : ''
                    }`}
                  >
                    <CreditCardIcon className="size-10 text-white" />
                    <p className="text-sm font-medium">Check Payment</p>
                    <p className="text-sm font-normal">Paper check</p>
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="space-y-4">
            <Card className="pt-4">
              <CardContent>
                <h3 className="mb-4 text-lg font-semibold">Invoice Preview</h3>

                <div className="mb-4">
                  <h4 className="font-medium">Bill To:</h4>
                  <p className="text-sm text-gray-400">
                    {formData.organization?.organizationName ||
                      'Organization Name'}
                  </p>
                  <p className="text-sm text-gray-400">
                    {formData.billing?.billingEmail || 'billing@company.com'}
                  </p>
                </div>

                <Separator className="my-4" />

                <div className="space-y-4">
                  <h4 className="font-medium">Items:</h4>
                  {formData.productSelections?.map(selectedProduct => {
                    const totalLicensePrice =
                      selectedProduct.pricePerLicense *
                      selectedProduct.licenseCount;
                    const totalPrice =
                      totalLicensePrice * selectedProduct.validityPeriod;

                    // Check if this product/package is eligible for coupon
                    const isCouponEligible =
                      coupon &&
                      coupon.productRestrictions?.some(
                        restriction =>
                          restriction.productId === selectedProduct.productId &&
                          restriction.packageId === selectedProduct.packageId,
                      );

                    return (
                      <div
                        key={selectedProduct.productId}
                        className="flex justify-between text-sm text-gray-400"
                      >
                        <div className="space-y-1">
                          <h4>{selectedProduct.productName}</h4>
                          <p>{selectedProduct.packageName}</p>
                          <p>
                            {selectedProduct.licenseCount} licenses ×{' '}
                            {selectedProduct.validityPeriod}{' '}
                            {selectedProduct.validityUnit.toLowerCase()}
                          </p>
                          {isCouponEligible && couponDiscount > 0 && (
                            <p className="text-xs text-green-500">
                              ✓ Coupon applicable
                            </p>
                          )}
                        </div>
                        <div className="text-right">
                          <p>${totalPrice.toFixed(2)}</p>
                        </div>
                      </div>
                    );
                  })}
                </div>

                <Separator className="my-4" />

                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span>Subtotal:</span>
                    <span>${invoiceData.subtotal.toFixed(2)}</span>
                  </div>
                  {couponDiscount > 0 && (
                    <>
                      <div className="flex justify-between text-green-600">
                        <span>Coupon ({invoiceData.couponCode}):</span>
                        <span>-${couponDiscount.toFixed(2)}</span>
                      </div>
                      <div className="flex justify-between font-medium">
                        <span>After Coupon:</span>
                        <span>
                          ${(invoiceData.subtotal - couponDiscount).toFixed(2)}
                        </span>
                      </div>
                    </>
                  )}
                  {invoiceData.discountValue > 0 && (
                    <>
                      <div className="flex justify-between text-green-600">
                        <span>
                          Discount{' '}
                          {invoiceData.discountType === DiscountType.PERCENTAGE
                            ? `(${invoiceData.discountValue}%)`
                            : '(Flat)'}
                          :
                        </span>
                        <span>-${invoiceData.discountAmount.toFixed(2)}</span>
                      </div>
                      <div className="flex justify-between font-medium">
                        <span>After Discount:</span>
                        <span>
                          $
                          {(
                            invoiceData.subtotal -
                            couponDiscount -
                            invoiceData.discountAmount
                          ).toFixed(2)}
                        </span>
                      </div>
                    </>
                  )}
                  {invoiceData.vatRate > 0 && (
                    <div className="flex justify-between text-yellow-600">
                      <span>VAT ({invoiceData.vatRate}%):</span>
                      <span>+${invoiceData.vatAmount.toFixed(2)}</span>
                    </div>
                  )}
                  <Separator />
                  <div className="flex justify-between text-lg font-semibold">
                    <span>Total:</span>
                    <span>${invoiceData.totalAmount.toFixed(2)}</span>
                  </div>
                </div>

                {/* Payment Status Display */}
                {invoiceData.paymentStatus && (
                  <div className="mt-4 rounded border border-card-border p-3">
                    <h4 className="mb-1 text-sm font-medium text-white">
                      Payment Status:
                    </h4>
                    <p className="text-sm text-white/50">
                      {getPaymentDisplayText()}
                    </p>
                    {invoiceData.paymentStatus === 'COMPLETED' &&
                      invoiceData.completedPayment?.paymentMethod ===
                        'BANK_TRANSFER' &&
                      invoiceData.completedPayment?.bankTransferDetails
                        ?.transactionNumber && (
                        <p className="mt-1 text-xs text-gray-500">
                          Transaction:{' '}
                          {
                            invoiceData.completedPayment.bankTransferDetails
                              .transactionNumber
                          }
                        </p>
                      )}
                    {invoiceData.paymentStatus === 'COMPLETED' &&
                      invoiceData.completedPayment?.paymentMethod ===
                        'CHECK_PAYMENT' &&
                      invoiceData.completedPayment.checkPaymentDetails
                        ?.checkNumber && (
                        <p className="mt-1 text-xs text-gray-500">
                          Check:{' '}
                          {
                            invoiceData.completedPayment.checkPaymentDetails
                              ?.checkNumber
                          }
                        </p>
                      )}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
          <div className="col-span-2">
            {/* Step 3: Bank Transfer Fields (only if Bank Transfer is selected) */}
            {invoiceData.paymentStatus === 'COMPLETED' &&
              invoiceData.completedPayment?.paymentMethod ===
                'BANK_TRANSFER' && (
                <div className="col-span-full rounded border border-card-border p-4">
                  <p className="text-lg font-medium text-primary">
                    Bank Transfer Details
                  </p>
                  <div className="mt-4 grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="bt-bankName">
                        Bank Name * (Min{' '}
                        {validationRules.bankTransfer.bankName.min}, Max{' '}
                        {validationRules.bankTransfer.bankName.max} characters)
                      </Label>
                      <Input
                        id="bt-bankName"
                        value={
                          invoiceData.completedPayment?.bankTransferDetails
                            ?.bankName || ''
                        }
                        onChange={e =>
                          handleBankTransferChange(
                            'bankName',
                            e.target.value as string,
                          )
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
                        {validationRules.bankTransfer.accountNumber.max}{' '}
                        characters)
                      </Label>
                      <Input
                        id="bt-accountNumber"
                        value={
                          invoiceData.completedPayment?.bankTransferDetails
                            ?.accountNumber || ''
                        }
                        onChange={e =>
                          handleBankTransferChange(
                            'accountNumber',
                            e.target.value as string,
                          )
                        }
                        placeholder="Enter account number"
                        maxLength={
                          validationRules.bankTransfer.accountNumber.max
                        }
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
                      <Label htmlFor="bt-bankBranch">
                        Bank Branch Name * (Min{' '}
                        {validationRules.bankTransfer.bankBranchName.min}, Max{' '}
                        {validationRules.bankTransfer.bankBranchName.max}{' '}
                        characters)
                      </Label>
                      <Input
                        id="bt-bankBranch"
                        value={
                          invoiceData.completedPayment?.bankTransferDetails
                            ?.bankBranchName || ''
                        }
                        onChange={e =>
                          handleBankTransferChange(
                            'bankBranchName',
                            e.target.value as string,
                          )
                        }
                        placeholder="Enter branch name"
                        maxLength={
                          validationRules.bankTransfer.bankBranchName.max
                        }
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
                      <Label htmlFor="bt-paymentAmount">
                        Payment Amount * (Must equal Total: $
                        {invoiceData.totalAmount.toFixed(2)})
                      </Label>
                      <Input
                        id="bt-paymentAmount"
                        type="number"
                        value={
                          invoiceData.completedPayment?.bankTransferDetails
                            ?.paymentAmount || ''
                        }
                        onChange={e =>
                          handleBankTransferChange(
                            'paymentAmount',
                            e.target.value as string,
                          )
                        }
                        placeholder={`Enter payment amount (Total: $${invoiceData.totalAmount.toFixed(2)})`}
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
                    <div className="space-y-2">
                      <Label htmlFor="bt-transactionNumber">
                        Transaction Number * (Min{' '}
                        {validationRules.bankTransfer.transactionNumber.min},
                        Max {validationRules.bankTransfer.transactionNumber.max}{' '}
                        characters)
                      </Label>
                      <Input
                        id="bt-transactionNumber"
                        value={
                          invoiceData.completedPayment?.bankTransferDetails
                            ?.transactionNumber || ''
                        }
                        onChange={e =>
                          handleBankTransferChange(
                            'transactionNumber',
                            e.target.value,
                          )
                        }
                        placeholder="Enter transaction number"
                        maxLength={
                          validationRules.bankTransfer.transactionNumber.max
                        }
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
                        value={
                          invoiceData.completedPayment?.bankTransferDetails
                            ?.paymentDate || ''
                        }
                        onChange={e =>
                          handleBankTransferChange(
                            'paymentDate',
                            e.target.value,
                          )
                        }
                        onClick={e => {
                          const input = e.target as HTMLInputElement;
                          input.showPicker();
                        }}
                      />
                    </div>

                    <div className="space-y-2">
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
                        className={`w-full rounded-md border px-3 py-2 focus:outline-none focus:ring-2 ${
                          validationErrors['bt-transactionReceiptFile']
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
                      {invoiceData.completedPayment?.bankTransferDetails
                        ?.transactionReceiptFile && (
                        <p className="text-xs text-green-600">
                          File selected:{' '}
                          {invoiceData.completedPayment.bankTransferDetails
                            .transactionReceiptFile instanceof File
                            ? invoiceData.completedPayment.bankTransferDetails
                                .transactionReceiptFile.name
                            : 'Uploaded'}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              )}

            {/* Step 3: Check Payment Fields (only if Check Payment is selected) */}
            {invoiceData.paymentStatus === 'COMPLETED' &&
              invoiceData.completedPayment?.paymentMethod ===
                'CHECK_PAYMENT' && (
                <div className="col-span-full space-y-4 rounded border border-card-border p-4">
                  <h4 className="text-lg font-medium text-primary">
                    Check Payment Details
                  </h4>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="cp-checkNumber">
                        Check Number * (Min{' '}
                        {validationRules.checkPayment.checkNumber.min}, Max{' '}
                        {validationRules.checkPayment.checkNumber.max}{' '}
                        characters)
                      </Label>
                      <Input
                        id="cp-checkNumber"
                        value={
                          invoiceData.completedPayment?.checkPaymentDetails
                            ?.checkNumber || ''
                        }
                        onChange={e =>
                          handleCheckPaymentChange(
                            'checkNumber',
                            e.target.value as string,
                          )
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
                        value={
                          invoiceData.completedPayment?.checkPaymentDetails
                            ?.bankName || ''
                        }
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
                        {validationRules.checkPayment.branchName.max}{' '}
                        characters)
                      </Label>
                      <Input
                        id="cp-branchName"
                        value={
                          invoiceData.completedPayment?.checkPaymentDetails
                            ?.branchName || ''
                        }
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
                        value={
                          invoiceData.completedPayment?.checkPaymentDetails
                            ?.paymentDate || ''
                        }
                        onChange={e =>
                          handleCheckPaymentChange(
                            'paymentDate',
                            e.target.value as string,
                          )
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
                        {invoiceData.totalAmount.toFixed(2)})
                      </Label>
                      <Input
                        id="cp-paymentAmount"
                        type="number"
                        value={
                          invoiceData.completedPayment?.checkPaymentDetails
                            ?.paymentAmount || ''
                        }
                        onChange={e =>
                          handleCheckPaymentChange(
                            'paymentAmount',
                            e.target.value as string,
                          )
                        }
                        placeholder={`Enter payment amount (Total: $${invoiceData.totalAmount.toFixed(2)})`}
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
                      <Label htmlFor="cp-checkImage">
                        Check Image/Receipt *
                      </Label>
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
                        className={`w-full rounded-md border px-3 py-2 focus:outline-none focus:ring-2 ${
                          validationErrors['cp-checkImageFile']
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
                      {invoiceData.completedPayment?.checkPaymentDetails
                        ?.checkImageFile && (
                        <p className="text-xs text-green-600">
                          File selected:{' '}
                          {invoiceData.completedPayment.checkPaymentDetails
                            .checkImageFile instanceof File
                            ? invoiceData.completedPayment.checkPaymentDetails
                                .checkImageFile.name
                            : 'Uploaded'}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              )}
            {invoiceData.paymentStatus === 'COMPLETED' && (
              <div className="mt-8 grid grid-cols-2 gap-4 rounded border border-card-border p-4">
                <p className="col-span-2 text-lg font-medium text-primary">
                  Payment Comment
                </p>
                <div className="space-y-2">
                  <Label htmlFor="bt-actionTaken">Action Taken *</Label>
                  <Select
                    value={paymentCommentData.actionTakenId}
                    onValueChange={value =>
                      setPaymentCommentData({
                        ...paymentCommentData,
                        actionTakenId: value,
                      })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select action taken" />
                    </SelectTrigger>
                    <SelectContent>
                      {actionTakenList.map(action => (
                        <SelectItem key={action.id} value={action.id}>
                          {action.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="bt-nextStep">Next Step *</Label>
                  <Select
                    value={paymentCommentData.nextStepId}
                    onValueChange={value =>
                      setPaymentCommentData({
                        ...paymentCommentData,
                        nextStepId: value,
                      })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select next step" />
                    </SelectTrigger>
                    <SelectContent>
                      {nextStepList.map(step => (
                        <SelectItem key={step.id} value={step.id}>
                          {step.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="col-span-2 space-y-2">
                  <Label htmlFor="bt-comment">
                    Comment * (Min {validationRules.paymentComment.comment.min},
                    Max {validationRules.paymentComment.comment.max} characters)
                  </Label>
                  <Textarea
                    id="bt-comment"
                    value={paymentCommentData.comment}
                    onChange={e => {
                      const value = e.target.value;
                      const rules = validationRules.paymentComment.comment;
                      let error = '';

                      if (
                        value.trim().length > 0 &&
                        value.trim().length < rules.min
                      ) {
                        error = `Minimum ${rules.min} characters required`;
                      } else if (value.length > rules.max) {
                        error = `Maximum ${rules.max} characters allowed`;
                      }

                      setValidationErrors(prev => ({
                        ...prev,
                        'payment-comment': error,
                      }));

                      setPaymentCommentData({
                        ...paymentCommentData,
                        comment: value,
                      });
                    }}
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
                    {paymentCommentData.comment.length} /{' '}
                    {validationRules.paymentComment.comment.max} characters
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="flex justify-between pt-6">
          <Button variant="outline" onClick={onPrevious} className="px-8 py-2">
            Previous
          </Button>
          <Button
            onClick={handleSave}
            disabled={!validateForm()}
            className={`px-8 py-2 ${
              !validateForm() ? 'cursor-not-allowed opacity-50' : ''
            }`}
          >
            Save & Continue
          </Button>
        </div>
      </CardContent>
    </Card>
  );
};

export default Step4;
