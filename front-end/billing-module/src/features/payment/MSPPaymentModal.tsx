import { Alert, AlertDescription } from 'common/Alert';
import { Button } from 'common/Button';
import { Checkbox } from 'common/CheckBox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { AlertTriangle, CreditCard } from 'lucide-react';
import { IInvoiceDetails } from 'models/Payment';
import { useEffect, useState } from 'react';

interface PaymentDetails {
  id: string;
  invoiceNo: string;
  clientAdminId: string;
  productSelections: any[];
  totalAmount: number;
}

interface PaymentModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  paymentDetails: IInvoiceDetails | null;
  availableCredit: number;
  onPayment: (paymentData: PaymentData) => void;
}

export interface PaymentData {
  paymentSources: {
    method: string;
    amount: number;
    online: boolean;
  }[];
  totalAmount: number;
}

const PAYMENT_METHODS = {
  CREDIT: 'CREDIT',
  PAYPAL: 'PAYPAL',
  STRIPE: 'STRIPE',
} as const;

type PaymentMethod = (typeof PAYMENT_METHODS)[keyof typeof PAYMENT_METHODS];

const MSPPaymentModal = ({
  open,
  onOpenChange,
  paymentDetails,
  availableCredit,
  onPayment,
}: PaymentModalProps) => {
  const [selectedMethods, setSelectedMethods] = useState<PaymentMethod[]>([]);
  const [creditAmount, setCreditAmount] = useState(0);
  const [secondMethodAmount, setSecondMethodAmount] = useState(0);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [warnings, setWarnings] = useState<string[]>([]);

  const totalBill = paymentDetails?.totalAmount ?? 0;

  // Reset form when modal opens/closes
  useEffect(() => {
    if (open) {
      setSelectedMethods([]);
      setCreditAmount(0);
      setSecondMethodAmount(0);
      setWarnings([]);
    }
  }, [open]);

  // Validate payment method combinations
  const validateMethodCombination = (methods: PaymentMethod[]) => {
    if (methods.length === 3) {
      return false; // All three not allowed
    }
    return true;
  };

  // Handle payment method selection
  const handleMethodChange = (method: PaymentMethod, checked: boolean) => {
    let newMethods: PaymentMethod[];

    if (checked) {
      newMethods = [...selectedMethods, method];
    } else {
      newMethods = selectedMethods.filter(m => m !== method);
      // Reset amounts if removing credit
      if (method === PAYMENT_METHODS.CREDIT) {
        setCreditAmount(0);
        setSecondMethodAmount(totalBill);
      }
    }

    // Validate combination
    if (!validateMethodCombination(newMethods)) {
      setWarnings(['Cannot select all three payment methods at once']);
      return;
    }

    setSelectedMethods(newMethods);
    setWarnings([]);

    // Auto-calculate amounts for different scenarios
    if (newMethods.length === 1) {
      if (newMethods[0] === PAYMENT_METHODS.CREDIT) {
        setCreditAmount(Math.min(availableCredit, totalBill));
        setSecondMethodAmount(0);
      } else {
        setCreditAmount(0);
        setSecondMethodAmount(totalBill);
      }
    } else if (
      newMethods.length === 2 &&
      newMethods.includes(PAYMENT_METHODS.CREDIT)
    ) {
      // Credit + another method
      const maxCredit = Math.min(availableCredit, totalBill);
      setCreditAmount(maxCredit);
      setSecondMethodAmount(totalBill - maxCredit);
    }
  };

  // Handle credit amount change
  const handleCreditAmountChange = (value: number) => {
    const maxCredit = Math.min(availableCredit, totalBill);
    const newCreditAmount = Math.max(0, Math.min(value, maxCredit));
    setCreditAmount(newCreditAmount);

    if (selectedMethods.length === 2) {
      setSecondMethodAmount(totalBill - newCreditAmount);
    }
  };

  // Handle second method amount change
  const handleSecondMethodAmountChange = (value: number) => {
    const newSecondAmount = Math.max(0, value);
    setSecondMethodAmount(newSecondAmount);

    if (selectedMethods.includes(PAYMENT_METHODS.CREDIT)) {
      const newCreditAmount = totalBill - newSecondAmount;
      setCreditAmount(Math.max(0, Math.min(newCreditAmount, availableCredit)));
    }
  };

  // Validate payment amounts
  const validatePayment = () => {
    const newWarnings: string[] = [];

    // Check if only credit is selected and insufficient
    if (
      selectedMethods.length === 1 &&
      selectedMethods[0] === PAYMENT_METHODS.CREDIT
    ) {
      if (availableCredit < totalBill) {
        newWarnings.push(
          "You don't have enough credit. Please choose another payment method.",
        );
      }
    }

    // Check if total payment matches bill for multiple methods
    if (selectedMethods.length === 2) {
      const totalPayment = creditAmount + secondMethodAmount;
      if (Math.abs(totalPayment - totalBill) > 0.01) {
        newWarnings.push('Total payment amount must match the bill.');
      }
    }

    // Check if credit amount exceeds available
    if (
      selectedMethods.includes(PAYMENT_METHODS.CREDIT) &&
      creditAmount > availableCredit
    ) {
      newWarnings.push('Credit amount cannot exceed available credit.');
    }

    setWarnings(newWarnings);
    return newWarnings.length === 0;
  };

  // Handle payment submission
  const handlePayment = () => {
    if (!validatePayment() || selectedMethods.length === 0) return;

    const paymentSources = [];

    if (selectedMethods.includes(PAYMENT_METHODS.CREDIT) && creditAmount > 0) {
      paymentSources.push({
        method: PAYMENT_METHODS.CREDIT,
        amount: creditAmount,
        online: true,
      });
    }

    const secondMethod = selectedMethods.find(
      m => m !== PAYMENT_METHODS.CREDIT,
    );

    if (
      secondMethod &&
      secondMethodAmount > 0 &&
      selectedMethods.length === 2
    ) {
      paymentSources.push({
        method: secondMethod,
        amount: secondMethodAmount,
        online: true,
      });
    }

    // If only non-credit method selected
    if (
      selectedMethods.length === 1 &&
      !selectedMethods.includes(PAYMENT_METHODS.CREDIT)
    ) {
      paymentSources.push({
        method: selectedMethods[0],
        amount: totalBill,
        online: true,
      });
    }

    console.log('Payment Sources:', paymentSources);

    onPayment({
      paymentSources,
      totalAmount: totalBill,
    });
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const getSecondMethodName = () => {
    return selectedMethods.find(m => m !== PAYMENT_METHODS.CREDIT) || '';
  };

  const isCreditSelected = selectedMethods.includes(PAYMENT_METHODS.CREDIT);
  const isMultipleMethodsSelected = selectedMethods.length === 2;
  const canProceed = selectedMethods.length > 0 && warnings.length === 0;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] max-w-lg overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Complete Payment</DialogTitle>
          <DialogDescription>
            Select your payment method(s) and complete the payment for{' '}
            {paymentDetails?.id}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          {/* Payment Method Selection */}
          <div className="space-y-3">
            <Label className="text-base font-medium">Payment Methods</Label>

            <div className="space-y-3">
              {Object.entries(PAYMENT_METHODS).map(([key, value]) => (
                <div key={value} className="flex items-center space-x-3">
                  <Checkbox
                    id={value}
                    checked={selectedMethods.includes(value)}
                    onCheckedChange={checked =>
                      handleMethodChange(value, checked as boolean)
                    }
                    disabled={
                      (!selectedMethods.includes(value) &&
                        selectedMethods.length === 2) ||
                      (value === PAYMENT_METHODS.PAYPAL &&
                        selectedMethods.includes(PAYMENT_METHODS.STRIPE)) ||
                      (value === PAYMENT_METHODS.STRIPE &&
                        selectedMethods.includes(PAYMENT_METHODS.PAYPAL))
                    }
                  />
                  <Label
                    htmlFor={value}
                    className="flex cursor-pointer items-center gap-2"
                  >
                    {value === PAYMENT_METHODS.CREDIT && (
                      <CreditCard className="size-4" />
                    )}
                    {value}
                    {value === PAYMENT_METHODS.CREDIT && (
                      <span className="text-sm text-muted-foreground">
                        (Available: {formatCurrency(availableCredit)})
                      </span>
                    )}
                  </Label>
                </div>
              ))}
            </div>
          </div>

          {/* Credit Amount Input */}
          {isCreditSelected && (
            <div className="space-y-2">
              <Label htmlFor="credit-amount">Credit Amount to Use</Label>
              <Input
                id="credit-amount"
                type="number"
                min="0"
                max={Math.min(availableCredit, totalBill)}
                step="0.01"
                value={creditAmount}
                onChange={e =>
                  handleCreditAmountChange(parseFloat(e.target.value) || 0)
                }
                placeholder="0.00"
              />
              <div className="text-sm text-muted-foreground">
                Max: {formatCurrency(Math.min(availableCredit, totalBill))}
              </div>
            </div>
          )}

          {/* Second Method Amount Input */}
          {isMultipleMethodsSelected && (
            <div className="space-y-2">
              <Label htmlFor="second-amount">
                {getSecondMethodName()} Amount
              </Label>
              <Input
                id="second-amount"
                type="number"
                value={secondMethodAmount}
                readOnly
                className="rounded-none border-none bg-muted outline-none focus-visible:ring-0 focus-visible:ring-offset-0"
                placeholder="0.00"
              />
              <div className="text-sm text-muted-foreground">
                Auto-calculated based on credit amount used
              </div>
            </div>
          )}

          {/* Payment Summary */}
          {selectedMethods.length > 0 && (
            <div className="space-y-2 rounded-lg bg-muted p-4">
              <h4 className="font-medium">Payment Summary</h4>
              <div className="space-y-1 text-sm">
                <div className="flex justify-between">
                  <span>Total Bill:</span>
                  <span className="font-medium">
                    {formatCurrency(totalBill)}
                  </span>
                </div>

                {isCreditSelected && creditAmount > 0 && (
                  <>
                    <div className="flex justify-between">
                      <span>Credit Used:</span>
                      <span>{formatCurrency(creditAmount)}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Credit Remaining:</span>
                      <span>
                        {formatCurrency(availableCredit - creditAmount)}
                      </span>
                    </div>
                  </>
                )}

                {isMultipleMethodsSelected && (
                  <div className="flex justify-between">
                    <span>{getSecondMethodName()}:</span>
                    <span>{formatCurrency(secondMethodAmount)}</span>
                  </div>
                )}

                {selectedMethods.length === 1 && !isCreditSelected && (
                  <div className="flex justify-between">
                    <span>{selectedMethods[0]}:</span>
                    <span>{formatCurrency(totalBill)}</span>
                  </div>
                )}

                <div className="mt-2 border-t pt-2">
                  <div className="flex justify-between font-medium">
                    <span>Total Payment:</span>
                    <span>
                      {formatCurrency(
                        isMultipleMethodsSelected
                          ? creditAmount + secondMethodAmount
                          : selectedMethods.length === 1 && isCreditSelected
                            ? creditAmount
                            : totalBill,
                      )}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Warnings */}
          {warnings.length > 0 && (
            <Alert variant="destructive">
              <AlertTriangle className="size-4" />
              <AlertDescription>
                <ul className="space-y-1">
                  {warnings.map((warning, index) => (
                    <li key={index}>{warning}</li>
                  ))}
                </ul>
              </AlertDescription>
            </Alert>
          )}

          {/* Action Buttons */}
          <div className="flex gap-2 pt-4">
            <Button
              variant="outline"
              onClick={() => onOpenChange(false)}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button
              onClick={handlePayment}
              className="flex-1"
              disabled={!canProceed || paymentLoading}
            >
              {paymentLoading ? 'Processing...' : 'Confirm Payment'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default MSPPaymentModal;
