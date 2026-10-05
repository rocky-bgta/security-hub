import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Separator } from 'common/Separator';
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import {
  BanknoteIcon,
  CheckIcon,
  CreditCardIcon,
  ListCheckIcon,
  Loader2Icon,
} from 'lucide-react';
import { IClientDetails } from 'models/Client';
import { ICoupon } from 'models/Coupon';
import {
  IAssignLicenseFormData,
  IBillingAction,
  IBillingNextStep,
  IInvoice,
  IPaymentComment,
} from 'models/Form';
import { IResponse } from 'models/Global';
import { DiscountType, PaymentMethod, PaymentStatus } from 'models/Payment';
import { IVatConfiguration } from 'models/Vat';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';

const Step3 = ({
  data,
  formData,
  onUpdate,
  onPrevious,
  onNext,
  paymentComment,
  onUpdatePaymentComment,
}: {
  data: IInvoice;
  formData: IAssignLicenseFormData;
  onUpdate: (data: IInvoice) => void;
  onPrevious: () => void;
  onNext: () => void;
  paymentComment?: IPaymentComment;
  onUpdatePaymentComment?: (data: IPaymentComment) => void;
}) => {
  const [invoiceData, setInvoiceData] = useState<IInvoice>(
    data
      ? data
      : {
        subtotal: 0,
        discountType: DiscountType.PERCENTAGE,
        discountValue: '',
        discountPercentage: 0,
        discountAmount: 0,
        couponCode: '',
        vatRate: '',
        vatAmount: 0,
        totalAmount: 0,
        paymentStatus: PaymentStatus.PENDING,
        completedPayment: {
          paymentMethod: PaymentMethod.BANK_TRANSFER,
          bankTransferDetails: {
            bankName: '',
            accountNumber: '',
            bankBranchName: '',
            transactionNumber: '',
            paymentDate: '',
            paymentAmount: '',
            transactionReceiptUrl: '',
            transactionReceiptFile: null as File | null,
          },
          checkPaymentDetails: {
            checkNumber: '',
            bankName: '',
            branchName: '',
            paymentDate: '',
            paymentAmount: '',
            checkImageUrl: '',
            checkImageFile: null as File | null,
          },
        },
      },
  );

  const [couponDiscount, setCouponDiscount] = useState(0);
  const [coupon, setCoupon] = useState<ICoupon | null>(null);
  const [couponLoading, setCouponLoading] = useState<boolean>(false);
  const apiClient = useAPI();
  const [paymentCommentData, setPaymentCommentData] = useState<IPaymentComment>(
    paymentComment || {
      invoiceId: '',
      comment: '',
      actionTakenId: '',
      nextStepId: '',
      userName: '',
      userRole: '',
      approved: false,
    },
  );
  const [actionTakenList, setActionTakenList] = useState<Array<IBillingAction>>(
    [],
  );
  const [nextStepList, setNextStepList] = useState<Array<IBillingNextStep>>([]);
  const [clientDetails, setClientDetails] = useState<IClientDetails>({
    id: '',
    email: '',
    organizationName: '',
    contactEmail: '',
    billingCountry: '',
    billingStateProvince: '',
  });
  const [validationErrors, setValidationErrors] = useState<
    Record<string, string>
  >({});

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

  useEffect(() => {
    fetchActionTakenList();
    fetchNextStepList();
  }, []);

  useEffect(() => {
    if (clientDetails.billingCountry) {
      fetchVatRate();
    }
  }, [clientDetails.billingCountry, clientDetails.billingStateProvince]);

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
    if (formData?.selectUserType?.selectedClient) {
      fetchClientDetails();
    }
  }, [formData?.selectUserType?.selectedClient]);

  const fetchClientDetails = async () => {
    try {
      const response: IResponse<IClientDetails> = await apiClient.get(
        API_END_POINTS.CLIENT_DETAILS.replace(
          ':id',
          formData?.selectUserType?.selectedClient || '',
        ),
      );
      if (isSuccessResponse(response.statusCode)) {
        setClientDetails(response.data);
      }
    } catch (error) {
      console.error('Error fetching client details:', error);
    }
  };

  const fetchVatRate = async () => {
    if (!clientDetails.billingCountry) return;

    try {
      const response: IResponse<IVatConfiguration> = await apiClient.get(
        API_END_POINTS.BILLING_VAT_RATE_BY_COUNTRY.replace(
          ':countryId',
          clientDetails.billingCountry,
        ),
      );

      if (!isSuccessResponse(response.statusCode)) return;

      const vatConfig = response.data;

      let vatRate = vatConfig.defaultVatRate;

      // If region based VAT
      if (vatConfig.regionBased && vatConfig.regions.length > 0) {
        const matchedRegion = vatConfig.regions.find(
          region => region.id === clientDetails.billingStateProvince,
        );

        if (matchedRegion) {
          vatRate = matchedRegion.vatRate;
        }
      }

      // finally update invoice data
      setInvoiceData(prev => ({
        ...prev,
        vatRate: String(vatRate),
      }));
    } catch (error) {
      console.error('Error fetching vat rate:', error);
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
    if (!formData.productSelections?.products) return 0;

    let subtotal = 0;
    formData.productSelections.products.forEach(selectedProduct => {
      const monthlyTotal =
        selectedProduct.pricePerLicense * selectedProduct.licenseCount;
      const validityInMonths =
        selectedProduct.validityUnit === 'YEAR'
          ? selectedProduct.validityPeriod * 1
          : selectedProduct.validityPeriod;
      subtotal += monthlyTotal * validityInMonths;
    });

    return subtotal;
  };

  // Calculate subtotal for products/packages that match coupon restrictions
  const calculateCouponEligibleSubtotal = () => {
    if (
      !formData.productSelections?.products ||
      !coupon ||
      !coupon.productRestrictions
    )
      return 0;

    let eligibleSubtotal = 0;

    formData.productSelections.products.forEach(selectedProduct => {
      // Check if this product/package is in the coupon's productRestrictions
      // Note: Multiple packages of the same product can be selected
      const isEligible = coupon.productRestrictions.some(
        restriction =>
          restriction.productId === selectedProduct.productId &&
          restriction.packageId === selectedProduct.packageId,
      );

      if (isEligible) {
        const monthlyTotal =
          selectedProduct.pricePerLicense * selectedProduct.licenseCount;
        const validityInMonths =
          selectedProduct.validityUnit === 'YEAR'
            ? selectedProduct.validityPeriod * 12
            : selectedProduct.validityPeriod;
        eligibleSubtotal += monthlyTotal * validityInMonths;
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

    if (invoiceData.discountType === 'PERCENTAGE') {
      discountPercentage = Number(invoiceData.discountValue);
      discountAmount = (afterCoupon * discountPercentage) / 100;
    } else {
      discountAmount = Number(invoiceData.discountValue);
      discountPercentage =
        afterCoupon > 0 ? (discountAmount / afterCoupon) * 100 : 0;
    }

    // Step 4: Calculate final amount after both coupon and discount
    const afterDiscounts = afterCoupon - discountAmount;
    const vatAmount = (afterDiscounts * Number(invoiceData.vatRate)) / 100;
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
        const hasMatchingProduct = formData.productSelections?.products.some(
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

  const handlePaymentStatusChange = (status: PaymentStatus) => {
    setInvoiceData(prev => ({
      ...prev,
      paymentStatus: status as PaymentStatus,
    }));
  };

  const handlePaymentMethodChange = (method: PaymentMethod) => {
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
            paymentAmount: String(totalAmount),
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
            paymentAmount: String(totalAmount),
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
        ...prev.completedPayment,
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
    paymentType: string,
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

  const handleNext = () => {
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
        toast.error(
          'Please fix the validation errors in the form before proceeding.',
        );
      } else {
        toast.error(
          'Please complete all required fields for the selected payment option.',
        );
      }
      return;
    }

    onNext();
    onUpdate(invoiceData);
    if (onUpdatePaymentComment) {
      onUpdatePaymentComment(paymentCommentData);
    }
  };

  const getPaymentDisplayText = () => {
    if (!invoiceData.paymentStatus) return '';

    if (invoiceData.paymentStatus === 'PENDING') {
      return 'Online Payment';
    }

    if (invoiceData.paymentStatus === 'COMPLETED') {
      if (!invoiceData.completedPayment?.paymentMethod)
        return 'Completed Payment';
      return `Completed Payment - ${invoiceData.completedPayment?.paymentMethod === 'BANK_TRANSFER'
        ? 'Bank Transfer'
        : 'Check Payment'
        }`;
    }

    return '';
  };

  return (
    <div>
      <Card className="content-w-full">
        <CardHeader>
          <CardTitle className="content-text-2xl content-font-bold content-text-white">
            Invoice Generation
          </CardTitle>
          <p className="content-text-gray-400">
            Generate and review your invoice
          </p>
        </CardHeader>
        <CardContent className="content-space-y-6">
          <div className="content-lg:content-grid-cols-2 content-grid content-w-full content-grid-cols-1 content-gap-6">
            <div className="content-grid content-w-full content-grid-cols-2 content-gap-4">
              {/* Left Column - Invoice Settings */}
              <div className="content-space-y-4">
                {/* Discount Type */}
                <div className="content-space-y-2">
                  <Label className="content-text-white">Discount Type</Label>
                  <RadioGroup
                    value={invoiceData.discountType}
                    onValueChange={value =>
                      setInvoiceData(prev => ({
                        ...prev,
                        discountType: value as DiscountType,
                        discountValue: '',
                      }))
                    }
                    className="!content-flex content-w-full content-gap-4"
                  >
                    <div className="content-flex content-items-center content-space-x-2">
                      <RadioGroupItem
                        value={DiscountType.PERCENTAGE}
                        id="percentage"
                      />
                      <Label
                        htmlFor="percentage"
                        className="content-cursor-pointer content-text-white"
                      >
                        Percentage
                      </Label>
                    </div>
                    <div className="content-flex content-items-center content-space-x-2">
                      <RadioGroupItem value={DiscountType.FLAT} id="flat" />
                      <Label
                        htmlFor="flat"
                        className="content-cursor-pointer content-text-white"
                      >
                        Flat Amount
                      </Label>
                    </div>
                  </RadioGroup>
                </div>

                {/* Discount Value */}
                <div className="content-space-y-2">
                  <Label htmlFor="discountValue" className="content-text-white">
                    Discount{' '}
                    {invoiceData.discountType === 'PERCENTAGE' ? '(%)' : '($)'}
                  </Label>
                  <Input
                    id="discountValue"
                    type="number"
                    value={invoiceData.discountValue}
                    onChange={e =>
                      setInvoiceData(prev => ({
                        ...prev,
                        discountValue: e.target.value || '',
                      }))
                    }
                    placeholder="0"
                  />
                </div>

                {/* Coupon Code */}
                <div className="content-space-y-2">
                  <Label htmlFor="coupon" className="content-text-white">
                    Coupon Code
                  </Label>
                  <div className="content-flex content-space-x-2">
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
                      onClick={applyCoupon}
                      variant="default"
                      disabled={couponLoading}
                    >
                      {couponLoading ? (
                        <Loader2Icon className="content-size-4 content-animate-spin" />
                      ) : (
                        'Apply'
                      )}
                    </Button>
                  </div>
                  {couponDiscount > 0 && (
                    <div className="content-space-y-1">
                      <p className="content-text-sm content-text-green-400">
                        Coupon applied: ${couponDiscount.toFixed(2)} off
                      </p>
                    </div>
                  )}
                  {coupon &&
                    couponDiscount === 0 &&
                    coupon.productRestrictions &&
                    coupon.productRestrictions.length > 0 && (
                      <p className="content-text-sm content-text-yellow-500">
                        Coupon loaded but no matching products/packages selected
                      </p>
                    )}
                </div>

                {/* VAT Rate - Auto-fetched based on billing country */}
                {/* <div className="content-space-y-2">
                  <Label htmlFor="vat" className="content-text-white">
                    VAT Rate (%)
                  </Label>
                  <Input
                    id="vat"
                    type="number"
                    value={invoiceData.vatRate}
                    readOnly
                    className="content-bg-slate-700 content-cursor-not-allowed"
                    placeholder="0"
                  />
                  <p className="content-text-xs content-text-slate-400">
                    VAT rate is automatically calculated based on billing
                    country
                  </p>
                </div> */}
              </div>
              {/* Right Column - Invoice Preview */}
              <div className="content-space-y-4">
                <Card>
                  <CardContent className="content-pt-6">
                    <h3 className="content-mb-4 content-text-lg content-font-semibold content-text-white">
                      Invoice Preview
                    </h3>

                    <div className="content-mb-4">
                      <h4 className="content-font-medium content-text-white">
                        Bill To:
                      </h4>
                      <p className="content-text-sm content-text-slate-300">
                        {formData.selectUserType?.selectedMSP ||
                          'Organization Name'}
                      </p>
                      <p className="content-text-sm content-text-slate-400">
                        billing@company.com
                      </p>
                    </div>

                    <Separator className="content-my-4 content-bg-slate-600" />

                    <div className="content-space-y-4">
                      <h4 className="content-font-medium content-text-white">
                        Items:
                      </h4>
                      {formData.productSelections?.products.map(
                        (product, index) => {
                          const monthlyTotal =
                            product.pricePerLicense * product.licenseCount;
                          const validityInMonths =
                            product.validityUnit === 'YEAR'
                              ? product.validityPeriod * 1
                              : product.validityPeriod;
                          const totalPrice = monthlyTotal * validityInMonths;

                          // Check if this product/package is eligible for coupon
                          const isCouponEligible =
                            coupon &&
                            coupon.productRestrictions?.some(
                              restriction =>
                                restriction.productId === product.productId &&
                                restriction.packageId === product.packageId,
                            );

                          return (
                            <div
                              key={`${product.packageId}-${index}`}
                              className="content-flex content-justify-between content-text-sm"
                            >
                              <div className="content-space-y-1">
                                <h4 className="content-text-white">
                                  {product.productName}
                                </h4>
                                <p className="content-text-slate-400">
                                  {product.packageName} ({product.licenseCount}{' '}
                                  licenses × {product.pricePerLicense}{' '}
                                  license price per month)
                                </p>
                                {isCouponEligible && couponDiscount > 0 && (
                                  <p className="content-text-xs content-text-green-400">
                                    ✓ Coupon applicable
                                  </p>
                                )}
                              </div>
                              <div className="content-text-right">
                                <p className="content-text-white">
                                  ${totalPrice.toFixed(2)}
                                </p>
                              </div>
                            </div>
                          );
                        },
                      )}
                    </div>

                    <Separator className="content-my-4 content-bg-slate-600" />

                    <div className="content-space-y-2 content-text-sm">
                      <div className="content-flex content-justify-between content-text-slate-300">
                        <span>Subtotal:</span>
                        <span>${invoiceData.subtotal.toFixed(2)}</span>
                      </div>
                      {couponDiscount > 0 && (
                        <>
                          <div className="content-flex content-justify-between content-text-green-400">
                            <span>Coupon ({invoiceData.couponCode}):</span>
                            <span>-${couponDiscount.toFixed(2)}</span>
                          </div>
                          <div className="content-flex content-justify-between content-font-medium content-text-slate-300">
                            <span>After Coupon:</span>
                            <span>
                              $
                              {(invoiceData.subtotal - couponDiscount).toFixed(
                                2,
                              )}
                            </span>
                          </div>
                        </>
                      )}
                      {invoiceData.discountValue && (
                        <>
                          <div className="content-flex content-justify-between content-text-green-400">
                            <span>
                              Discount{' '}
                              {invoiceData.discountType === 'PERCENTAGE'
                                ? `(${invoiceData.discountValue}%)`
                                : '(Flat)'}
                              :
                            </span>
                            <span>
                              -${invoiceData.discountAmount.toFixed(2)}
                            </span>
                          </div>
                          <div className="content-flex content-justify-between content-font-medium content-text-slate-300">
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
                      {invoiceData.vatRate && (
                        <div className="content-flex content-justify-between content-text-yellow-500">
                          <span>VAT ({invoiceData.vatRate}%):</span>
                          <span>${invoiceData.vatAmount.toFixed(2)}</span>
                        </div>
                      )}
                      <Separator className="content-bg-slate-600" />
                      <div className="content-flex content-justify-between content-text-lg content-font-semibold content-text-white">
                        <span>Total:</span>
                        <span>${invoiceData.totalAmount.toFixed(2)}</span>
                      </div>
                    </div>

                    {/* Payment Status Display */}
                    {invoiceData.paymentStatus && (
                      <div className="content-mt-4 content-rounded content-border content-border-slate-600 content-bg-slate-800 content-p-3">
                        <h4 className="content-mb-1 content-text-sm content-font-medium content-text-white">
                          Payment Status:
                        </h4>
                        <p className="content-text-sm content-text-slate-300">
                          {getPaymentDisplayText()}
                        </p>
                        {invoiceData.paymentStatus === 'COMPLETED' &&
                          invoiceData.completedPayment?.paymentMethod ===
                          'BANK_TRANSFER' &&
                          invoiceData.completedPayment?.bankTransferDetails
                            ?.transactionNumber && (
                            <p className="content-mt-1 content-text-xs content-text-slate-400">
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
                            <p className="content-mt-1 content-text-xs content-text-slate-400">
                              Check:{' '}
                              {
                                invoiceData.completedPayment.checkPaymentDetails
                                  .checkNumber
                              }
                            </p>
                          )}
                      </div>
                    )}
                  </CardContent>
                </Card>
              </div>
            </div>

            {/* Payment Status */}
            <div className="content-space-y-2">
              <Label className="content-text-white">Payment Status</Label>
              <div className="content-flex content-gap-2">
                <Button
                  variant={
                    invoiceData.paymentStatus === PaymentStatus.PENDING
                      ? 'default'
                      : 'outline'
                  }
                  onClick={() =>
                    handlePaymentStatusChange(PaymentStatus.PENDING)
                  }
                >
                  <ListCheckIcon className="content-size-4" /> Online Payment
                </Button>
                <Button
                  variant={
                    invoiceData.paymentStatus === PaymentStatus.COMPLETED
                      ? 'default'
                      : 'outline'
                  }
                  onClick={() =>
                    handlePaymentStatusChange(PaymentStatus.COMPLETED)
                  }
                >
                  <CheckIcon className="content-size-4" /> Manual Payment
                </Button>
              </div>
            </div>

            {/* Payment Method Selection */}
            {invoiceData.paymentStatus === PaymentStatus.COMPLETED && (
              <div className="content-space-y-4">
                <Label className="content-text-white">Payment Method</Label>
                <div className="content-flex content-gap-4">
                  <div
                    onClick={() =>
                      handlePaymentMethodChange(PaymentMethod.BANK_TRANSFER)
                    }
                    className={`content-flex content-cursor-pointer content-flex-col content-items-center content-gap-2 content-rounded content-border content-p-4 content-transition-all ${invoiceData.completedPayment?.paymentMethod ===
                      'BANK_TRANSFER'
                      ? 'content-border-primary content-bg-primary/20'
                      : 'content-border-card-border hover:content-border-card-border/80'
                      }`}
                  >
                    <BanknoteIcon className="content-size-10 content-text-white" />
                    <p className="content-text-sm content-font-medium content-text-white">
                      Bank Transfer
                    </p>
                    <p className="content-text-xs content-text-slate-400">
                      Direct transfer
                    </p>
                  </div>

                  <div
                    onClick={() =>
                      handlePaymentMethodChange(PaymentMethod.CHECK_PAYMENT)
                    }
                    className={`content-flex content-cursor-pointer content-flex-col content-items-center content-gap-2 content-rounded content-border content-p-4 content-transition-all ${invoiceData.completedPayment?.paymentMethod ===
                      'CHECK_PAYMENT'
                      ? 'content-border-primary content-bg-primary/20'
                      : 'content-border-card-border hover:content-border-card-border/80'
                      }`}
                  >
                    <CreditCardIcon className="content-size-10 content-text-white" />
                    <p className="content-text-sm content-font-medium content-text-white">
                      Check Payment
                    </p>
                    <p className="content-text-xs content-text-slate-400">
                      Paper check
                    </p>
                  </div>
                </div>
              </div>
            )}
            {/* Bank Transfer Details */}
            {invoiceData.paymentStatus === PaymentStatus.COMPLETED &&
              invoiceData.completedPayment?.paymentMethod ===
              PaymentMethod.BANK_TRANSFER && (
                <div>
                  <h3 className="content-mb-4 content-text-lg content-font-medium content-text-primary">
                    Bank Transfer Details
                  </h3>
                  <div className="content-grid content-grid-cols-2 content-gap-4">
                    <div className="content-space-y-2">
                      <Label
                        htmlFor="bt-bankName"
                        className="content-text-white"
                      >
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
                          handleBankTransferChange('bankName', e.target.value)
                        }
                        placeholder="Enter bank name"
                        maxLength={validationRules.bankTransfer.bankName.max}
                        className={
                          validationErrors['bt-bankName']
                            ? 'content-focus:content-ring-red-500 content-border-red-500'
                            : ''
                        }
                      />
                      {validationErrors['bt-bankName'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['bt-bankName']}
                        </p>
                      )}
                    </div>

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="bt-accountNumber"
                        className="content-text-white"
                      >
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
                            e.target.value,
                          )
                        }
                        placeholder="Enter account number"
                        maxLength={
                          validationRules.bankTransfer.accountNumber.max
                        }
                        className={
                          validationErrors['bt-accountNumber']
                            ? 'content-focus:content-ring-red-500 content-border-red-500'
                            : ''
                        }
                      />
                      {validationErrors['bt-accountNumber'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['bt-accountNumber']}
                        </p>
                      )}
                    </div>

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="bt-bankBranch"
                        className="content-text-white"
                      >
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
                            e.target.value,
                          )
                        }
                        placeholder="Enter branch name"
                        maxLength={
                          validationRules.bankTransfer.bankBranchName.max
                        }
                        className={
                          validationErrors['bt-bankBranchName']
                            ? 'content-focus:content-ring-red-500 content-border-red-500'
                            : ''
                        }
                      />
                      {validationErrors['bt-bankBranchName'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['bt-bankBranchName']}
                        </p>
                      )}
                    </div>

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="bt-paymentAmount"
                        className="content-text-white"
                      >
                        Payment Amount * (Must equal Total: $
                        {invoiceData.totalAmount.toFixed(2)})
                      </Label>
                      <Input
                        id="bt-paymentAmount"
                        type="number"
                        value={
                          invoiceData?.completedPayment?.bankTransferDetails
                            ?.paymentAmount || ''
                        }
                        onChange={e =>
                          handleBankTransferChange(
                            'paymentAmount',
                            e.target.value,
                          )
                        }
                        placeholder={`Enter payment amount (Total: $${invoiceData.totalAmount.toFixed(2)})`}
                        min={validationRules.bankTransfer.paymentAmount.min}
                        max={validationRules.bankTransfer.paymentAmount.max}
                        step="0.01"
                        className={
                          validationErrors['bt-paymentAmount']
                            ? 'content-focus:content-ring-red-500 content-border-red-500'
                            : ''
                        }
                      />
                      {validationErrors['bt-paymentAmount'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['bt-paymentAmount']}
                        </p>
                      )}
                    </div>

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="bt-transactionNumber"
                        className="content-text-white"
                      >
                        Transaction Number * (Min{' '}
                        {validationRules.bankTransfer.transactionNumber.min},
                        Max {validationRules.bankTransfer.transactionNumber.max}{' '}
                        characters)
                      </Label>
                      <Input
                        id="bt-transactionNumber"
                        value={
                          invoiceData?.completedPayment?.bankTransferDetails
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
                            ? 'content-focus:content-ring-red-500 content-border-red-500'
                            : ''
                        }
                      />
                      {validationErrors['bt-transactionNumber'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['bt-transactionNumber']}
                        </p>
                      )}
                    </div>

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="bt-paymentDate"
                        className="content-text-white"
                      >
                        Payment Date *
                      </Label>
                      <Input
                        id="bt-paymentDate"
                        type="date"
                        value={
                          invoiceData?.completedPayment?.bankTransferDetails
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

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="bt-receipt"
                        className="content-text-white"
                      >
                        Transaction Receipt *
                      </Label>
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
                        className={`content-file:content-mr-4 content-file:content-rounded content-file:content-border-0 content-file:content-bg-primary content-file:content-px-4 content-file:content-py-2 content-file:content-text-sm content-file:content-text-white content-hover:content-file:content-bg-primary/80 content-w-full content-rounded-md content-border content-px-3 content-py-2 content-text-white ${validationErrors['bt-transactionReceiptFile']
                          ? 'content-focus:content-ring-red-500 content-border-red-500'
                          : 'content-border-card-border content-bg-transparent'
                          }`}
                      />
                      <p className="content-text-xs content-text-slate-400">
                        Upload image or PDF (Max 5MB)
                      </p>
                      {validationErrors['bt-transactionReceiptFile'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['bt-transactionReceiptFile']}
                        </p>
                      )}
                      {invoiceData.completedPayment?.bankTransferDetails
                        ?.transactionReceiptFile && (
                          <p className="content-text-xs content-text-green-400">
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

            {/* Check Payment Details */}
            {invoiceData.paymentStatus === PaymentStatus.COMPLETED &&
              invoiceData.completedPayment?.paymentMethod ===
              PaymentMethod.CHECK_PAYMENT && (
                <div>
                  <h3 className="content-mb-4 content-text-lg content-font-medium content-text-primary">
                    Check Payment Details
                  </h3>
                  <div className="content-grid content-grid-cols-2 content-gap-4">
                    <div className="content-space-y-2">
                      <Label
                        htmlFor="cp-checkNumber"
                        className="content-text-white"
                      >
                        Check Number * (Min{' '}
                        {validationRules.checkPayment.checkNumber.min}, Max{' '}
                        {validationRules.checkPayment.checkNumber.max}{' '}
                        characters)
                      </Label>
                      <Input
                        id="cp-checkNumber"
                        value={
                          invoiceData?.completedPayment?.checkPaymentDetails
                            ?.checkNumber || ''
                        }
                        onChange={e =>
                          handleCheckPaymentChange(
                            'checkNumber',
                            e.target.value,
                          )
                        }
                        placeholder="Enter check number"
                        maxLength={validationRules.checkPayment.checkNumber.max}
                        className={
                          validationErrors['cp-checkNumber']
                            ? 'content-focus:content-ring-red-500 content-border-red-500'
                            : ''
                        }
                      />
                      {validationErrors['cp-checkNumber'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['cp-checkNumber']}
                        </p>
                      )}
                    </div>

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="cp-bankName"
                        className="content-text-white"
                      >
                        Bank Name * (Min{' '}
                        {validationRules.checkPayment.bankName.min}, Max{' '}
                        {validationRules.checkPayment.bankName.max} characters)
                      </Label>
                      <Input
                        id="cp-bankName"
                        value={
                          invoiceData?.completedPayment?.checkPaymentDetails
                            ?.bankName || ''
                        }
                        onChange={e =>
                          handleCheckPaymentChange('bankName', e.target.value)
                        }
                        placeholder="Enter bank name"
                        maxLength={validationRules.checkPayment.bankName.max}
                        className={
                          validationErrors['cp-bankName']
                            ? 'content-focus:content-ring-red-500 content-border-red-500'
                            : ''
                        }
                      />
                      {validationErrors['cp-bankName'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['cp-bankName']}
                        </p>
                      )}
                    </div>

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="cp-branchName"
                        className="content-text-white"
                      >
                        Branch Name * (Min{' '}
                        {validationRules.checkPayment.branchName.min}, Max{' '}
                        {validationRules.checkPayment.branchName.max}{' '}
                        characters)
                      </Label>
                      <Input
                        id="cp-branchName"
                        value={
                          invoiceData?.completedPayment?.checkPaymentDetails
                            ?.branchName || ''
                        }
                        onChange={e =>
                          handleCheckPaymentChange('branchName', e.target.value)
                        }
                        placeholder="Enter branch name"
                        maxLength={validationRules.checkPayment.branchName.max}
                        className={
                          validationErrors['cp-branchName']
                            ? 'content-focus:content-ring-red-500 content-border-red-500'
                            : ''
                        }
                      />
                      {validationErrors['cp-branchName'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['cp-branchName']}
                        </p>
                      )}
                    </div>

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="cp-paymentDate"
                        className="content-text-white"
                      >
                        Payment Date *
                      </Label>
                      <Input
                        id="cp-paymentDate"
                        type="date"
                        value={
                          invoiceData?.completedPayment?.checkPaymentDetails
                            ?.paymentDate || ''
                        }
                        onChange={e =>
                          handleCheckPaymentChange(
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

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="cp-paymentAmount"
                        className="content-text-white"
                      >
                        Payment Amount * (Must equal Total: $
                        {invoiceData.totalAmount.toFixed(2)})
                      </Label>
                      <Input
                        id="cp-paymentAmount"
                        type="number"
                        value={
                          invoiceData?.completedPayment?.checkPaymentDetails
                            ?.paymentAmount || ''
                        }
                        onChange={e =>
                          handleCheckPaymentChange(
                            'paymentAmount',
                            e.target.value,
                          )
                        }
                        placeholder={`Enter payment amount (Total: $${invoiceData.totalAmount.toFixed(2)})`}
                        min={validationRules.checkPayment.paymentAmount.min}
                        max={validationRules.checkPayment.paymentAmount.max}
                        step="0.01"
                        className={
                          validationErrors['cp-paymentAmount']
                            ? 'content-focus:content-ring-red-500 content-border-red-500'
                            : ''
                        }
                      />
                      {validationErrors['cp-paymentAmount'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['cp-paymentAmount']}
                        </p>
                      )}
                    </div>

                    <div className="content-space-y-2">
                      <Label
                        htmlFor="cp-checkImage"
                        className="content-text-white"
                      >
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
                        className={`content-file:content-mr-4 content-file:content-rounded content-file:content-border-0 content-file:content-bg-primary content-file:content-px-4 content-file:content-py-2 content-file:content-text-sm content-file:content-text-white content-hover:content-file:content-bg-primary/80 content-w-full content-rounded-md content-border content-px-3 content-py-2 content-text-white ${validationErrors['cp-checkImageFile']
                          ? 'content-focus:content-ring-red-500 content-border-red-500'
                          : 'content-border-card-border content-bg-transparent'
                          }`}
                      />
                      <p className="content-text-xs content-text-slate-400">
                        Upload image or PDF (Max 5MB)
                      </p>
                      {validationErrors['cp-checkImageFile'] && (
                        <p className="content-text-sm content-text-red-500">
                          {validationErrors['cp-checkImageFile']}
                        </p>
                      )}
                      {invoiceData.completedPayment?.checkPaymentDetails
                        ?.checkImageFile && (
                          <p className="content-text-xs content-text-green-400">
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

            {/* Payment Comment Section */}
            {invoiceData.paymentStatus === PaymentStatus.COMPLETED && (
              <div className="content-mt-8 content-grid content-grid-cols-2 content-gap-4 content-rounded content-border content-border-card-border content-p-4">
                <p className="content-col-span-2 content-text-lg content-font-medium content-text-primary">
                  Payment Comment
                </p>
                <div className="content-space-y-2">
                  <Label
                    htmlFor="bt-actionTaken"
                    className="content-text-white"
                  >
                    Action Taken *
                  </Label>
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
                <div className="content-space-y-2">
                  <Label htmlFor="bt-nextStep" className="content-text-white">
                    Next Step *
                  </Label>
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
                <div className="content-col-span-2 content-space-y-2">
                  <Label htmlFor="bt-comment" className="content-text-white">
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
                        ? 'content-focus:content-ring-red-500 content-border-red-500'
                        : ''
                    }
                  />
                  {validationErrors['payment-comment'] && (
                    <p className="content-text-sm content-text-red-500">
                      {validationErrors['payment-comment']}
                    </p>
                  )}
                  <p className="content-text-xs content-text-slate-400">
                    {paymentCommentData.comment.length} /{' '}
                    {validationRules.paymentComment.comment.max} characters
                  </p>
                </div>
              </div>
            )}
          </div>
          <div className="content-flex content-justify-between content-pt-6">
            <Button
              variant="outline"
              onClick={onPrevious}
              className="content-px-8 content-py-2"
            >
              Previous
            </Button>
            <Button
              onClick={handleNext}
              disabled={!validateForm()}
              className={`content-px-8 content-py-2 ${!validateForm()
                ? 'content-cursor-not-allowed content-opacity-50'
                : ''
                }`}
            >
              Save & Continue
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default Step3;
