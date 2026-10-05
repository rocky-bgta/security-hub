import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';

import { Badge } from 'common/Badge';
import { Card, CardContent } from 'common/Card';
import { Progress } from 'common/Progress';
import {
  IAssignLicenseFormData,
  IInvoice,
  IPaymentComment,
  ISelectedProduct,
} from 'models/Form';
import { useState } from 'react';

import { useAPI } from 'hooks/UseAPI';
import { useUploader } from 'hooks/UseUploader';
import { FileType } from 'models/Global';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { cn, isSuccessResponse } from 'utils/Helper';
import Step1 from './Step1';
import Step2 from './Step2';
import Step3 from './Step3';
import Step4 from './Step4';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
}

const steps = [
  {
    id: 1,
    title: 'Select MSP Partner',
    description: 'Choose MSP and assign product',
  },
  {
    id: 2,
    title: 'Select Products',
    description: 'Choose products and packages',
  },
  {
    id: 3,
    title: 'Generate Invoice',
    description: 'Review invoice',
  },
  { id: 4, title: 'Review & Confirm', description: 'Send confirmation email' },
];

const MspAssignProduct = ({ isOpen, onClose }: IProps) => {
  const { uploadFile } = useUploader();
  const apiClient = useAPI();
  const [currentStep, setCurrentStep] = useState<number>(1);
  const [formData, setFormData] = useState<IAssignLicenseFormData>({
    selectUserType: {
      selectRole: '',
      countryFilter: '',
      provinceFilter: '',
      selectedUser: '',
      selectedMSP: '',
      selectedClient: '',
    },
    productSelections: {
      selectAll: false,
      products: [],
    },
    invoice: {
      subtotal: 0,
      discountType: 'PERCENTAGE',
      discountValue: '',
      discountPercentage: 0,
      discountAmount: 0,
      couponCode: '',
      vatRate: '',
      vatAmount: 0,
      totalAmount: 0,
      paymentStatus: 'PENDING',
      completedPayment: {
        paymentMethod: 'BANK_TRANSFER',
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
  });
  const [emailSent, setEmailSent] = useState<boolean>(false);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [paymentComment, setPaymentComment] = useState<IPaymentComment>({
    invoiceId: '',
    comment: '',
    actionTakenId: '',
    nextStepId: '',
    userName: '',
    userRole: '',
    approved: false,
  });

  const progress = (currentStep / steps.length) * 100;

  const handleNext = () => {
    if (currentStep < steps.length) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrevious = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const updateFormData = (section: keyof IAssignLicenseFormData, data: any) => {
    setFormData(prev => ({
      ...prev,
      [section]: data,
    }));
  };

  const handleSubmit = async () => {
    try {
      setSubmitting(true);

      if (formData.invoice?.paymentStatus === 'COMPLETED') {
        if (
          formData.invoice?.completedPayment?.paymentMethod === 'BANK_TRANSFER'
        ) {
          const { url, error } = await uploadFile(
            formData.invoice?.completedPayment?.bankTransferDetails
              ?.transactionReceiptFile as unknown as File,
            FileType.CONTENT,
          );
          if (error) {
            toast.error(error);
            return { success: false };
          }
          formData.invoice.completedPayment!.bankTransferDetails!.transactionReceiptUrl =
            url;
        }
        delete formData.invoice?.completedPayment?.bankTransferDetails
          ?.transactionReceiptFile;

        if (
          formData.invoice?.completedPayment?.paymentMethod === 'CHECK_PAYMENT'
        ) {
          const { url, error } = await uploadFile(
            formData.invoice?.completedPayment?.checkPaymentDetails
              ?.checkImageFile as unknown as File,
            FileType.CONTENT,
          );
          if (error) {
            toast.error(error);
            return { success: false };
          }
          formData.invoice.completedPayment!.checkPaymentDetails!.checkImageUrl =
            url;
        }
        delete formData.invoice?.completedPayment?.checkPaymentDetails
          ?.checkImageFile;
      }

      const payload = {
        clientAdminId: formData?.selectUserType?.selectedClient,
        productSelections:
          formData?.productSelections?.products?.map(
            (product: ISelectedProduct) => ({
              productId: product.productId,
              productName: product.productName,
              packageId: product.packageId,
              packageName: product.packageName,
              licenseCount: product.licenseCount,
              pricePerLicense: product.pricePerLicense,
              validityPeriod: product.validityPeriod,
              validityUnit: product.validityUnit,
              userRangeId: product.userRangeId,
            }),
          ) || [],
        invoice: {
          subtotal: formData.invoice?.subtotal,
          discountType: formData.invoice?.discountType,
          discountPercentage: formData.invoice?.discountPercentage,
          discountAmount: formData.invoice?.discountAmount,
          couponCode: formData.invoice?.couponCode,
          vatRate: Number(formData.invoice?.vatRate),
          vatAmount: formData.invoice?.vatAmount,
          totalAmount: formData.invoice?.totalAmount,
          paymentStatus: formData.invoice?.paymentStatus,
          completedPayment:
            formData.invoice?.paymentStatus === 'COMPLETED'
              ? {
                  paymentMethod:
                    formData.invoice?.completedPayment?.paymentMethod,
                  commentLog: {
                    comment: paymentComment.comment,
                    actionTakenId: paymentComment.actionTakenId,
                    nextStepId: paymentComment.nextStepId,
                  },
                  ...(formData.invoice?.completedPayment?.paymentMethod ===
                    'BANK_TRANSFER' && {
                    bankTransferDetails: {
                      bankName:
                        formData.invoice.completedPayment?.bankTransferDetails
                          ?.bankName,
                      accountNumber:
                        formData.invoice.completedPayment?.bankTransferDetails
                          ?.accountNumber,
                      bankBranchName:
                        formData.invoice.completedPayment?.bankTransferDetails
                          ?.bankBranchName,
                      transactionNumber:
                        formData.invoice.completedPayment?.bankTransferDetails
                          ?.transactionNumber,
                      paymentDate:
                        formData.invoice.completedPayment?.bankTransferDetails
                          ?.paymentDate,
                      paymentAmount: Number(
                        formData.invoice.completedPayment?.bankTransferDetails
                          ?.paymentAmount,
                      ),
                      transactionReceiptUrl:
                        formData.invoice.completedPayment?.bankTransferDetails
                          ?.transactionReceiptUrl,
                    },
                  }),
                  ...(formData.invoice?.completedPayment?.paymentMethod ===
                    'CHECK_PAYMENT' && {
                    checkPaymentDetails: {
                      checkNumber:
                        formData.invoice?.completedPayment?.checkPaymentDetails
                          ?.checkNumber,
                      bankName:
                        formData.invoice?.completedPayment?.checkPaymentDetails
                          ?.bankName,
                      branchName:
                        formData.invoice?.completedPayment?.checkPaymentDetails
                          ?.branchName,
                      paymentDate:
                        formData.invoice?.completedPayment?.checkPaymentDetails
                          ?.paymentDate,
                      paymentAmount: Number(
                        formData.invoice?.completedPayment?.checkPaymentDetails
                          ?.paymentAmount,
                      ),
                      checkImageUrl:
                        formData.invoice?.completedPayment?.checkPaymentDetails
                          ?.checkImageUrl,
                    },
                  }),
                }
              : null,
        },
      };

      const response = await apiClient.post(API_END_POINTS.ASSIGN_PRODUCT, {
        data: payload,
      });
      if (isSuccessResponse(response.statusCode)) {
        toast.success(response.message || 'Product assigned successfully');
        onClose();
        resetFormData();
        setSubmitting(false);
        setCurrentStep(1);
        setEmailSent(true);
      } else {
        toast.error(response.message || 'Failed to assign product');
        setSubmitting(false);
      }
    } catch (error) {
      console.error('Error assigning product:', error);
      toast.error('Failed to assign product');
    } finally {
      setSubmitting(false);
    }
  };

  const resetFormData = () => {
    setFormData({
      selectUserType: {
        selectRole: '',
        countryFilter: '',
        provinceFilter: '',
        selectedUser: '',
        selectedMSP: '',
        selectedClient: '',
      },
      productSelections: {
        selectAll: false,
        products: [],
      },
      invoice: {
        subtotal: 0,
        discountType: 'PERCENTAGE',
        discountValue: '',
        discountPercentage: 0,
        discountAmount: 0,
        couponCode: '',
        vatRate: '',
        vatAmount: 0,
        totalAmount: 0,
        paymentStatus: 'PENDING',
        completedPayment: {
          paymentMethod: 'BANK_TRANSFER',
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
    });
  };

  const handleClose = () => {
    onClose();
    resetFormData();
    setCurrentStep(1);
    setEmailSent(false);
    setPaymentComment({
      invoiceId: '',
      comment: '',
      actionTakenId: '',
      nextStepId: '',
      userName: '',
      userRole: '',
      approved: false,
    });
    setSubmitting(false);
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <Step1
            data={
              formData.selectUserType || {
                selectRole: '',
                countryFilter: '',
                provinceFilter: '',
                selectedUser: '',
                selectedMSP: '',
                selectedClient: '',
              }
            }
            onUpdate={data => updateFormData('selectUserType', data)}
            onNext={handleNext}
          />
        );
      case 2:
        return (
          <Step2
            data={
              formData.productSelections || { selectAll: false, products: [] }
            }
            onUpdate={data => updateFormData('productSelections', data)}
            onNext={handleNext}
            onPrevious={handlePrevious}
          />
        );
      case 3:
        return (
          <Step3
            data={
              (formData.invoice as IInvoice) || {
                subtotal: 0,
                discountType: 'PERCENTAGE',
                discountValue: 0,
                discountPercentage: 0,
                discountAmount: 0,
                couponCode: '',
                vatRate: 0,
                vatAmount: 0,
                totalAmount: 0,
              }
            }
            formData={formData}
            onUpdate={(data: IInvoice) => updateFormData('invoice', data)}
            onNext={handleNext}
            onPrevious={handlePrevious}
            paymentComment={paymentComment}
            onUpdatePaymentComment={(data: IPaymentComment) =>
              setPaymentComment(data)
            }
          />
        );
      case 4:
        return (
          <Step4
            emailSent={emailSent}
            onSubmit={handleSubmit}
            onPrevious={handlePrevious}
            submitting={submitting}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div>
      <Dialog open={isOpen} onOpenChange={handleClose}>
        <DialogContent className="!content-max-h-[90vh] !content-w-4/5 !content-overflow-y-auto content-text-white">
          <DialogHeader>
            <DialogTitle className="content-text-white">
              Assign Product & Packages
            </DialogTitle>
            <DialogDescription>
              Select an MSP Partner or Client Organization to assign products
              and service packages.
            </DialogDescription>
          </DialogHeader>

          <div className="content-flex content-items-center content-justify-between">
            <h2 className="content-mb-2 content-text-xl content-text-white">
              Progress
            </h2>
            <Badge variant="secondary" className="content-text-sm">
              Step {currentStep} of {steps.length}
            </Badge>
          </div>
          <Progress value={progress} className="content-mb-4" />
          <div className="content-grid content-grid-cols-4 content-gap-2">
            {steps.map(step => (
              <div
                key={step.id}
                className={cn(
                  'content-rounded-lg content-p-2 content-text-center content-transition-all content-duration-200',
                  step.id === currentStep
                    ? 'content-border-2 content-border-primary content-bg-transparent'
                    : step.id < currentStep
                      ? 'content-border content-border-primary content-bg-primary/20'
                      : 'content-border content-border-primary content-bg-transparent',
                )}
              >
                <div
                  className={cn(
                    'content-text-xs content-font-medium',
                    step.id === currentStep
                      ? 'content-text-primary'
                      : step.id < currentStep
                        ? 'content-text-primary'
                        : 'content-text-primary',
                  )}
                >
                  {step.title}
                </div>
              </div>
            ))}
          </div>

          {renderStep()}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default MspAssignProduct;
