import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { ArrowLeft, XCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { routes } from 'routes/Routes';

const PaymentFailed = () => {
  const navigate = useNavigate();

  const goToPaymentList = () => {
    navigate(routes.pendingPayment.path);
  };

  return (
    <div className="flex min-h-[88vh] items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="mb-6 text-center">
          <div className="mb-4 inline-flex size-20 items-center justify-center rounded-full bg-red-100">
            <XCircle className="size-12 text-red-600" />
          </div>
          <div className="space-y-2">
            <h1 className="text-2xl font-bold text-white">Payment Cancelled</h1>
            <p className="text-white">Your payment process was not completed</p>
          </div>
        </div>

        <Card className="mb-6">
          <CardHeader className="pb-4 text-center">
            <CardTitle className="text-lg">Payment Status</CardTitle>
            <CardDescription>
              The payment process has been cancelled
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4 text-center">
            <p className="text-white">
              You can try again later or contact our support team if you need
              assistance.
            </p>
          </CardContent>
        </Card>

        <div className="space-y-3">
          <Button onClick={goToPaymentList} className="w-full">
            <ArrowLeft className="mr-2 size-4" />
            Go To Payment List
          </Button>
        </div>

        <div className="mt-6 text-center">
          <p className="text-xs text-gray-400">
            If you have any questions, please contact our support team.
          </p>
        </div>
      </div>
    </div>
  );
};

export default PaymentFailed;
