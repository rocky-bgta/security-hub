import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import {
  ArrowRight,
  Building,
  CreditCard,
  Download,
  Wallet,
} from 'lucide-react';
import { useState } from 'react';

import { ProductSelectionData } from './ProductSelectionStep';

interface PaymentOptionsStepProps {
  onNext: (data: PaymentData) => void;
  onPayLater: () => void;
  productData?: ProductSelectionData;
}

export interface PaymentData {
  paymentMethod: 'credit-card' | 'bank-transfer' | 'paypal';
  paymentDetails?: any;
  invoiceGenerated: boolean;
}

export const PaymentOptionsStep: React.FC<PaymentOptionsStepProps> = ({
  onNext,
  onPayLater,
  productData,
}) => {
  const [paymentMethod, setPaymentMethod] = useState<
    'credit-card' | 'bank-transfer' | 'paypal'
  >('credit-card');
  const [cardDetails, setCardDetails] = useState({
    cardNumber: '',
    expiryDate: '',
    cvv: '',
    cardholderName: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [invoiceGenerated, setInvoiceGenerated] = useState(false);

  const calculateTax = (amount: number) => amount * 0.08; // 8% tax
  const totalAmount = productData?.totalAmount || 0;
  const taxAmount = calculateTax(totalAmount);
  const grandTotal = totalAmount + taxAmount;

  const generateInvoice = () => {
    setInvoiceGenerated(true);
    // toast({
    //   title: 'Invoice Generated',
    //   description: 'Your invoice has been generated successfully',
    // });
  };

  const downloadInvoice = () => {
    // Simulate invoice download
    // toast({
    //   title: 'Download Started',
    //   description: 'Invoice PDF download has started',
    // });
  };

  const handlePayNow = async () => {
    if (paymentMethod === 'credit-card') {
      if (
        !cardDetails.cardNumber ||
        !cardDetails.expiryDate ||
        !cardDetails.cvv ||
        !cardDetails.cardholderName
      ) {
        // toast({
        //   title: 'Payment Details Required',
        //   description: 'Please fill in all credit card details',
        //   variant: 'destructive',
        // });
        return;
      }
    }

    setIsLoading(true);

    setTimeout(() => {
      setIsLoading(false);
      onNext({
        paymentMethod,
        paymentDetails: paymentMethod === 'credit-card' ? cardDetails : null,
        invoiceGenerated: true,
      });
      //   toast({
      //     title: 'Payment Successful',
      //     description: 'Payment has been processed successfully',
      //   });
    }, 2000);
  };

  const handlePayLater = () => {
    onPayLater();
    // toast({
    //   title: 'Payment Deferred',
    //   description: 'You can complete payment later from your dashboard',
    // });
  };

  return (
    <div className="space-y-6">
      <div className="text-center">
        <div className="mb-6 flex items-center justify-center">
          <CreditCard className="size-8 text-white" />
        </div>
        <h3 className="mb-2 text-xl font-semibold">Payment Options</h3>
        <p className="text-muted-foreground">
          Choose your preferred payment method
        </p>
      </div>

      {/* Invoice Summary */}
      {productData && productData.selectedProducts.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <span>Invoice Summary</span>
              <div className="flex gap-2">
                {!invoiceGenerated && (
                  <Button variant="outline" size="sm" onClick={generateInvoice}>
                    Generate Invoice
                  </Button>
                )}
                {invoiceGenerated && (
                  <Button variant="outline" size="sm" onClick={downloadInvoice}>
                    <Download className="mr-2 size-4" />
                    Download PDF
                  </Button>
                )}
              </div>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {productData.selectedProducts.map(product => (
              <div
                key={product.id}
                className="flex items-center justify-between border-b border-card-border py-2 last:border-0"
              >
                <div>
                  <p className="font-medium">{product.name}</p>
                  <p className="text-sm text-muted-foreground">
                    {product.licenses} license(s) × {product.validityPeriod}{' '}
                    {product.validityType}
                  </p>
                </div>
                <p className="font-medium">${product.totalPrice}</p>
              </div>
            ))}

            <div className="space-y-2 pt-4">
              <div className="flex justify-between">
                <span>Subtotal:</span>
                <span>${totalAmount}</span>
              </div>
              <div className="flex justify-between">
                <span>Tax (8%):</span>
                <span>${taxAmount.toFixed(2)}</span>
              </div>
              <div className="flex justify-between border-t border-card-border pt-2 text-lg font-bold">
                <span>Grand Total:</span>
                <span className="text-primary">${grandTotal.toFixed(2)}</span>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Payment Method Selection */}
      <Card>
        <CardHeader>
          <CardTitle>Select Payment Method</CardTitle>
        </CardHeader>
        <CardContent>
          <RadioGroup
            value={paymentMethod}
            onValueChange={(value: any) => setPaymentMethod(value)}
          >
            <div className="flex items-center space-x-2 rounded-lg border p-4">
              <RadioGroupItem value="credit-card" id="credit-card" />
              <Label
                htmlFor="credit-card"
                className="flex flex-1 cursor-pointer items-center gap-2"
              >
                <CreditCard className="size-5" />
                <div>
                  <p className="font-medium">Credit Card</p>
                  <p className="text-sm text-muted-foreground">
                    Pay securely with your credit card
                  </p>
                </div>
              </Label>
            </div>

            <div className="flex items-center space-x-2 rounded-lg border p-4">
              <RadioGroupItem value="bank-transfer" id="bank-transfer" />
              <Label
                htmlFor="bank-transfer"
                className="flex flex-1 cursor-pointer items-center gap-2"
              >
                <Building className="size-5" />
                <div>
                  <p className="font-medium">Bank Transfer</p>
                  <p className="text-sm text-muted-foreground">
                    Direct transfer from your bank account
                  </p>
                </div>
              </Label>
            </div>

            <div className="flex items-center space-x-2 rounded-lg border p-4">
              <RadioGroupItem value="paypal" id="paypal" />
              <Label
                htmlFor="paypal"
                className="flex flex-1 cursor-pointer items-center gap-2"
              >
                <Wallet className="size-5" />
                <div>
                  <p className="font-medium">PayPal</p>
                  <p className="text-sm text-muted-foreground">
                    Pay using your PayPal account
                  </p>
                </div>
              </Label>
            </div>
          </RadioGroup>
        </CardContent>
      </Card>

      {/* Credit Card Details */}
      {paymentMethod === 'credit-card' && (
        <Card>
          <CardHeader>
            <CardTitle>Credit Card Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="cardholderName">Cardholder Name</Label>
              <Input
                id="cardholderName"
                value={cardDetails.cardholderName}
                onChange={e =>
                  setCardDetails(prev => ({
                    ...prev,
                    cardholderName: e.target.value,
                  }))
                }
                placeholder="John Doe"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="cardNumber">Card Number</Label>
              <Input
                id="cardNumber"
                value={cardDetails.cardNumber}
                onChange={e =>
                  setCardDetails(prev => ({
                    ...prev,
                    cardNumber: e.target.value
                      .replace(/\D/g, '')
                      .replace(/(\d{4})(?=\d)/g, '$1 ')
                      .trim(),
                  }))
                }
                placeholder="1234 5678 9012 3456"
                maxLength={19}
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="expiryDate">Expiry Date</Label>
                <Input
                  id="expiryDate"
                  value={cardDetails.expiryDate}
                  onChange={e =>
                    setCardDetails(prev => ({
                      ...prev,
                      expiryDate: e.target.value
                        .replace(/\D/g, '')
                        .replace(/(\d{2})(\d)/, '$1/$2')
                        .slice(0, 5),
                    }))
                  }
                  placeholder="MM/YY"
                  maxLength={5}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="cvv">CVV</Label>
                <Input
                  id="cvv"
                  value={cardDetails.cvv}
                  onChange={e =>
                    setCardDetails(prev => ({
                      ...prev,
                      cvv: e.target.value.replace(/\D/g, '').slice(0, 3),
                    }))
                  }
                  placeholder="123"
                  maxLength={3}
                  required
                />
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Payment Actions */}
      <div className="flex gap-4">
        <Button
          variant="outline"
          className="h-12 flex-1"
          onClick={handlePayLater}
        >
          Pay Later
        </Button>

        <Button
          className="h-12 flex-1 bg-primary transition-opacity hover:opacity-90"
          onClick={handlePayNow}
          disabled={isLoading}
        >
          {isLoading ? (
            <div className="flex items-center gap-2">
              <div className="size-4 animate-spin rounded-full border-2 border-primary-foreground border-t-transparent" />
              Processing Payment...
            </div>
          ) : (
            <div className="flex items-center gap-2">
              Pay Now ${grandTotal.toFixed(2)}
              <ArrowRight className="size-4" />
            </div>
          )}
        </Button>
      </div>
    </div>
  );
};
