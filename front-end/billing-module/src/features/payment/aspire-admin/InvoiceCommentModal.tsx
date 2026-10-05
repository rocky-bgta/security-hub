import { useEffect, useState } from 'react';
import { Send, CheckCircle2, Clock, AlertCircle, Loader2 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Button } from 'common/Button';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Checkbox } from 'common/CheckBox';
import { Textarea } from 'common/Textarea';
import { Card } from 'common/Card';
import { useAPI } from 'hooks/UseAPI';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { formatDateTime, humanizeText, isSuccessResponse } from 'utils/Helper';
import {
  IInvoiceDetails,
  IPaymentComment,
  PaymentMethod,
} from 'models/Payment';
import { IDropdownOption } from 'models/Global';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';
import { useStore } from 'hooks/UseStore';
import { toast } from 'react-toastify';

interface IProps {
  open: boolean;
  onClose: () => void;
  id: string;
  reload: () => void;
}

const InvoiceCommentModal = ({ open, onClose, id, reload }: IProps) => {
  const { role } = useAuth();
  const { userInfo } = useStore();
  const apiClient = useAPI();

  const isFinanceAdmin = role === ROLE.FINANCE_ADMIN;

  const [comment, setComment] = useState<string>('');
  const [actionTaken, setActionTaken] = useState<string>('');
  const [nextStep, setNextStep] = useState<string>('');
  const [isApproved, setIsApproved] = useState<boolean>(false);
  const [comments, setComments] = useState<Array<IPaymentComment>>([]);
  const [loadingComments, setLoadingComments] = useState<boolean>(false);
  const [actionTakenOptions, setActionTakenOptions] = useState<
    Array<IDropdownOption>
  >([]);
  const [nextStepOptions, setNextStepOptions] = useState<
    Array<IDropdownOption>
  >([]);
  const [commentSubmitting, setCommentSubmitting] = useState<boolean>(false);
  const [invoiceDetails, setInvoiceDetails] = useState<IInvoiceDetails | null>(
    null,
  );

  useEffect(() => {
    if (id) {
      getComments();
      getActionTakenOptions();
      getNextStepOptions();
    }
  }, [id]);

  useEffect(() => {
    if (invoiceDetails) {
      manualPayment();
    }
  }, [invoiceDetails]);

  const getComments = async () => {
    setLoadingComments(true);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_COMMENTS.replace(':invoiceId', id),
      );
      if (isSuccessResponse(response.statusCode)) {
        setComments(response.data);
      }
    } catch (error) {
      console.error('Error fetching comments:', error);
    } finally {
      setLoadingComments(false);
    }
  };

  const getActionTakenOptions = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_ACTION_TAKEN_OPTIONS,
      );
      if (isSuccessResponse(response.statusCode)) {
        setActionTakenOptions(
          response.data.map((item: { id: string; name: string }) => ({
            value: item.id,
            label: item.name,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching action taken options:', error);
    }
  };

  const getNextStepOptions = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_NEXT_STEP_OPTIONS,
      );
      if (isSuccessResponse(response.statusCode)) {
        setNextStepOptions(
          response.data.map((item: { id: string; name: string }) => ({
            value: item.id,
            label: item.name,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching next step options:', error);
    }
  };

  const handleSubmit = async () => {
    if (!comment || !actionTaken || !nextStep) {
      toast.error('Please fill in all required fields');
      return;
    }
    const payload = {
      invoiceId: id,
      comment: comment.trim(),
      actionTakenId: actionTaken,
      nextStepId: nextStep,
      userName: userInfo?.email ?? '',
      userRole: role ?? '',
      approved: isApproved,
    };

    if (isApproved) {
      await fetchInvoiceDetails();
    }

    try {
      setCommentSubmitting(true);
      const response = await apiClient.post(API_END_POINTS.CREATE_COMMENT, {
        data: payload,
      });
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Comment submitted successfully');
        getComments();
        setComment('');
        setActionTaken('');
        setNextStep('');
        setIsApproved(false);
      } else {
        toast.error(response.data?.data?.error || 'Failed to submit comment');
      }
    } catch (error) {
      console.error('Error submitting comment:', error);
    } finally {
      setCommentSubmitting(false);
    }
  };

  const fetchInvoiceDetails = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.INVOICE_DETAILS + id);
      if (isSuccessResponse(response.statusCode)) {
        setInvoiceDetails(response.data);
      }
    } catch (error) {
      console.error('Error fetching invoice details:', error);
    }
  };

  const manualPayment = async () => {
    const payload = {
      clientId: invoiceDetails?.clientAdminId,
      invoiceId: invoiceDetails?.id,
      amount: invoiceDetails?.totalAmount,
      currency: 'USD',
      packageItems: invoiceDetails?.productSelections,
      clientRegion: invoiceDetails?.countryName,
      date: new Date().toISOString(),
      paymentSources: [
        {
          method: invoiceDetails?.paymentMethod,
          amount: invoiceDetails?.totalAmount,
          online: false,
          transactionId: invoiceDetails?.paymentDetails?.transactionNumber,
          metadata:
            invoiceDetails?.paymentMethod === PaymentMethod.BANK_TRANSFER
              ? {
                  bankName: invoiceDetails?.paymentDetails?.bankName,
                  accountNumber: invoiceDetails?.paymentDetails?.accountNumber,
                  bankBranchName:
                    invoiceDetails?.paymentDetails?.bankBranchName,
                  transactionNumber:
                    invoiceDetails?.paymentDetails?.transactionNumber,
                  paymentDate: invoiceDetails?.paymentDetails?.paymentDate,
                  paymentAmount: invoiceDetails?.paymentDetails?.paymentAmount,
                  transactionReceiptUrl:
                    invoiceDetails?.paymentDetails?.transactionReceiptUrl,
                }
              : invoiceDetails?.paymentMethod === PaymentMethod.CHECK_PAYMENT
                ? {
                    checkNumber: invoiceDetails?.paymentDetails?.checkNumber,
                    bankName: invoiceDetails?.paymentDetails?.bankName,
                    branchName: invoiceDetails?.paymentDetails?.branchName,
                    paymentDate: invoiceDetails?.paymentDetails?.paymentDate,
                    paymentAmount:
                      invoiceDetails?.paymentDetails?.paymentAmount,
                    checkImageUrl:
                      invoiceDetails?.paymentDetails?.checkImageUrl,
                  }
                : null,
        },
      ],
      couponCode: '',
      subtotal: invoiceDetails?.totalAmount,
      vatAmount: invoiceDetails?.vatAmount,
      discountPercentage: invoiceDetails?.discountPercentage,
    };

    try {
      const response = await apiClient.post(API_END_POINTS.MANUAL_PAYMENT, {
        data: payload,
      });
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Payment processed successfully');
        reload();
      } else {
        toast.error(
          response.data?.data?.error || 'Failed to submit manual payment',
        );
      }
    } catch (error) {
      console.error('Error submitting comment:', error);
    }
  };

  const getStatusIcon = (action: string) => {
    if (
      action.toLowerCase().includes('confirmed') ||
      action.toLowerCase().includes('approved')
    ) {
      return <CheckCircle2 className="size-4 text-primary" />;
    } else if (
      action.toLowerCase().includes('pending') ||
      action.toLowerCase().includes('review')
    ) {
      return <Clock className="size-4 text-muted-foreground" />;
    } else if (action.toLowerCase().includes('rejected')) {
      return <AlertCircle className="size-4 text-destructive" />;
    }
    return <Clock className="size-4 text-primary" />;
  };

  if (!open) return null;

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-h-[90vh] w-2/3 overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Invoice Comments Logs</DialogTitle>
          <DialogDescription>
            Track invoice status and add updates for the invoice.
          </DialogDescription>
        </DialogHeader>
        {/* Comments History */}
        <div className="max-h-[500px] overflow-y-auto">
          <Card className="mt-4 p-4">
            <div className="space-y-2">
              {loadingComments ? (
                <div className="flex h-full items-center justify-center">
                  <p>Loading comments...</p>
                </div>
              ) : comments.length === 0 ? (
                <div className="flex h-full items-center justify-center">
                  <p>No comments found</p>
                </div>
              ) : (
                comments.map((item, index) => (
                  <div
                    key={index}
                    className={`${item.approved ? 'bg-primary/30' : 'bg-card-background'} rounded-md p-4`}
                  >
                    <div className="mb-3 flex items-start justify-between">
                      <div className="flex items-center gap-3">
                        <div className="flex size-8 items-center justify-center rounded-full bg-gradient-to-br from-primary to-primary/80 text-sm font-medium text-primary-foreground">
                          {item.userName
                            .split(' ')
                            .map(n => n[0])
                            .join('')}
                        </div>
                        <div>
                          <div className="font-medium text-foreground">
                            {item.userName}
                          </div>
                          <div className="text-xs text-muted-foreground">
                            {humanizeText(item.userRole)}
                          </div>
                        </div>
                      </div>
                      <div className="text-xs text-muted-foreground">
                        {formatDateTime(item.createdAt)}
                      </div>
                    </div>

                    <p className="mb-3 text-sm text-foreground">
                      {item.comment}
                    </p>

                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <div className="mb-1 text-xs font-medium text-muted-foreground">
                          Action Taken
                        </div>
                        <div className="flex items-center gap-2">
                          {getStatusIcon(item.actionName)}
                          <span className="text-sm text-foreground">
                            {item.actionName}
                          </span>
                        </div>
                      </div>
                      <div>
                        <div className="mb-1 text-xs font-medium text-muted-foreground">
                          Next Step
                        </div>
                        <div className="text-sm text-foreground">
                          {item.nextStepName}
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </Card>
        </div>
        {/* Add Comment Section */}
        {loadingComments ? (
          <div className="flex h-full items-center justify-center">
            <Loader2 className="size-4 animate-spin text-primary" />
          </div>
        ) : comments.filter(item => item.approved).length === 0 ? (
          <Card className="mt-4 p-4">
            <div className="space-y-4">
              <div>
                <Label htmlFor="comment">
                  Comment <span className="text-destructive">*</span>
                </Label>
                <Textarea
                  id="comment"
                  value={comment}
                  onChange={e => setComment(e.target.value)}
                  placeholder="Add your comment here..."
                  rows={3}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="actionTaken">
                    Action Taken <span className="text-destructive">*</span>
                  </Label>
                  <Select
                    value={actionTaken}
                    onValueChange={value => setActionTaken(value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select action taken" />
                    </SelectTrigger>
                    <SelectContent>
                      {actionTakenOptions.map(option => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <Label htmlFor="nextStep">
                    Next Step <span className="text-destructive">*</span>
                  </Label>
                  <Select
                    value={nextStep}
                    onValueChange={value => setNextStep(value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select next step" />
                    </SelectTrigger>
                    <SelectContent>
                      {nextStepOptions.map(option => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>

              {/* Approval Checkbox - Only for Finance Admin */}
              {isFinanceAdmin && (
                <Label
                  htmlFor="isApproved"
                  className="flex items-center gap-2 rounded-md bg-primary/10 p-3"
                >
                  <Checkbox
                    id="isApproved"
                    checked={isApproved}
                    onCheckedChange={checked => setIsApproved(checked as boolean)}
                  />
                  <span className="text-sm text-foreground">
                    Mark as Approved
                  </span>
                </Label>
              )}

              {/* Action Buttons */}
              <div className="flex justify-end gap-3 pt-2">
                <Button variant="outline" onClick={onClose} size="sm">
                  Cancel
                </Button>
                <Button
                  onClick={handleSubmit}
                  size="sm"
                  disabled={commentSubmitting}
                >
                  {commentSubmitting ? (
                    <div className="flex items-center gap-2">
                      <div className="size-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
                      Submitting...
                    </div>
                  ) : (
                    <>
                      <Send className="size-4" />
                      Submit
                    </>
                  )}
                </Button>
              </div>
            </div>
          </Card>
        ) : (
          <div className="flex h-full items-center justify-center rounded-md bg-primary/10 p-4 text-primary">
            <p>This invoice is already approved</p>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default InvoiceCommentModal;
