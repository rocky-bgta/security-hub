import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { ArrowRight, CheckCircle, Mail } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';

const PaymentSuccess = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const amount = parseFloat(searchParams.get('amount') || '0');
  const transactionId = searchParams.get('Transactionid') || '';
  const orderNumber = searchParams.get('orderNumber') || '';

  const goToHome = () => {
    navigate('/');
  };
  return (
    <div className="flex min-h-[88vh] items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="mb-6 text-center">
          <div className="mb-4 inline-flex size-20 items-center justify-center rounded-full bg-green-100">
            <CheckCircle className="size-12 text-green-600" />
          </div>
          <div className="space-y-2">
            <h1 className="text-2xl font-bold text-white">
              Payment Successful!
            </h1>
            <p className="text-white">Thank you for your purchase</p>
          </div>
        </div>

        <Card className="mb-6">
          <CardHeader className="pb-4 text-center">
            <CardTitle className="text-lg">Payment Confirmation</CardTitle>
            <CardDescription>
              Your order has been processed successfully
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between border-b border-gray-100 py-2">
              <span className="text-white">Amount Paid</span>
              <span className="text-lg font-semibold">
                ${amount.toFixed(2)}
              </span>
            </div>

            <div className="flex items-center justify-between border-b border-gray-100 py-2">
              <span className="text-white">Order Number</span>
              <Badge variant="outline" className="font-mono">
                {orderNumber}
              </Badge>
            </div>

            <div className="flex items-center justify-between border-b border-gray-100 py-2">
              <span className="text-white">Transaction ID</span>
              <span className="font-mono text-sm text-white">
                {transactionId}
              </span>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-3">
          <Button onClick={goToHome} className="w-full">
            Go To Home
            <ArrowRight className="ml-2 size-4" />
          </Button>
        </div>

        <div className="mt-6 text-center">
          <div className="mb-2 flex items-center justify-center text-sm text-white">
            <Mail className="mr-1 size-4" />
            Confirmation email sent
          </div>
          <p className="text-xs text-gray-400">
            If you have any questions, please contact our support team.
          </p>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;
