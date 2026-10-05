import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { ArrowRight, CheckCircle, Edit2 } from 'lucide-react';
import { useState } from 'react';
import { BillingInfoData } from './BillingInfoStep';
import { MSPDetailsData } from './MSPDetailsStep';
import { PaymentData } from './PaymentOptionsStep';
import { ProductSelectionData } from './ProductSelectionStep';

interface FinalConfirmationStepProps {
  onComplete: () => void;
  mspDetails?: MSPDetailsData;
  billingInfo?: BillingInfoData;
  productData?: ProductSelectionData;
  paymentData?: PaymentData;
}

export const FinalConfirmationStep: React.FC<FinalConfirmationStepProps> = ({
  onComplete,
  mspDetails,
  billingInfo,
  productData,
  paymentData,
}) => {
  const [emailTemplate, setEmailTemplate] = useState('welcome');
  const [emailContent, setEmailContent] = useState(
    `
Dear {MSP_ADMIN_NAME},

Welcome to Aspire ASAT! Your MSP account has been successfully created.

Your Login Credentials:
Username: {MSP_ADMIN_EMAIL}
Temporary Password: {TEMP_PASSWORD}
Portal Link: {PORTAL_LINK}

Please log in and change your password on first access.

Best regards,
Aspire ASAT Team
  `.trim(),
  );

  const [showPreview, setShowPreview] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const emailTemplates = [
    { value: 'welcome', label: 'Welcome Email' },
    { value: 'payment-confirmation', label: 'Payment Confirmation' },
    { value: 'setup-complete', label: 'Setup Complete' },
    { value: 'custom', label: 'Custom Template' },
  ];

  const tempPassword = 'TempPass123!';
  const portalLink = 'https://portal.aspire-asat.com';

  const getProcessedEmailContent = () => {
    return emailContent
      .replace('{MSP_ADMIN_NAME}', mspDetails?.organizationName || 'MSP Admin')
      .replace(
        '{MSP_ADMIN_EMAIL}',
        mspDetails?.mspAdminEmail || 'admin@example.com',
      )
      .replace('{TEMP_PASSWORD}', tempPassword)
      .replace('{PORTAL_LINK}', portalLink);
  };

  const handleTemplateChange = (template: string) => {
    setEmailTemplate(template);

    switch (template) {
      case 'welcome':
        setEmailContent(
          `
Dear {MSP_ADMIN_NAME},

Welcome to Aspire ASAT! Your MSP account has been successfully created.

Your Login Credentials:
Username: {MSP_ADMIN_EMAIL}
Temporary Password: {TEMP_PASSWORD}
Portal Link: {PORTAL_LINK}

Please log in and change your password on first access.

Best regards,
Aspire ASAT Team
        `.trim(),
        );
        break;
      case 'payment-confirmation':
        setEmailContent(
          `
Dear {MSP_ADMIN_NAME},

Thank you for your payment! Your Aspire ASAT services are now active.

Account Details:
Username: {MSP_ADMIN_EMAIL}
Portal Link: {PORTAL_LINK}
Temporary Password: {TEMP_PASSWORD}

Your selected services are now ready to use.

Best regards,
Aspire ASAT Team
        `.trim(),
        );
        break;
      case 'setup-complete':
        setEmailContent(
          `
Dear {MSP_ADMIN_NAME},

Your Aspire ASAT account setup is complete!

Login Information:
Username: {MSP_ADMIN_EMAIL}
Temporary Password: {TEMP_PASSWORD}
Portal Access: {PORTAL_LINK}

Next steps:
1. Log in to your portal
2. Complete your profile
3. Start managing your services

Best regards,
Aspire ASAT Team
        `.trim(),
        );
        break;
      default:
        setEmailContent('');
    }
  };

  const handleSubmit = async () => {
    if (!emailContent.trim()) {
      //   toast({
      //     title: 'Email Content Required',
      //     description: 'Please provide email content before submitting',
      //     variant: 'destructive',
      //   });
      return;
    }

    setIsLoading(true);

    setTimeout(() => {
      setIsLoading(false);
      onComplete();
      //   toast({
      //     title: 'Onboarding Complete!',
      //     description:
      //       'MSP account created successfully. Confirmation email sent.',
      //   });
    }, 2000);
  };

  return (
    <div className="space-y-6">
      <div className="text-center">
        <div className="mb-6 flex items-center justify-center">
          <CheckCircle className="size-8 text-white" />
        </div>
        <h3 className="mb-2 text-xl font-semibold">Final Confirmation</h3>
        <p className="text-muted-foreground">
          Review all details and send confirmation email to complete onboarding
        </p>
      </div>

      {/* Summary Cards */}
      <div className="grid gap-4">
        {/* MSP Details Summary */}
        {mspDetails && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between text-lg">
                <span>MSP Organization Details</span>
                <Edit2 className="size-4 text-muted-foreground" />
              </CardTitle>
            </CardHeader>
            <CardContent className="grid gap-4 text-sm md:grid-cols-2">
              <div>
                <p>
                  <strong>Organization:</strong> {mspDetails.organizationName}
                </p>
                <p>
                  <strong>Admin Email:</strong> {mspDetails.mspAdminEmail}
                </p>
                <p>
                  <strong>Contact Email:</strong> {mspDetails.contactEmail}
                </p>
                <p>
                  <strong>Phone:</strong> {mspDetails.countryCode}{' '}
                  {mspDetails.phoneNumber}
                </p>
              </div>
              <div>
                <p>
                  <strong>Country:</strong> {mspDetails.country}
                </p>
                <p>
                  <strong>State:</strong> {mspDetails.state}
                </p>
                <p>
                  <strong>MSP Type:</strong> {mspDetails.mspType}
                </p>
                <p>
                  <strong>Organization Size:</strong>{' '}
                  {mspDetails.organizationSize}
                </p>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Billing Information Summary */}
        {billingInfo && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between text-lg">
                <span>Billing Information</span>
                <Edit2 className="size-4 text-muted-foreground" />
              </CardTitle>
            </CardHeader>
            <CardContent className="grid gap-4 text-sm md:grid-cols-2">
              <div>
                <p>
                  <strong>Billing Name:</strong> {billingInfo.billingName}
                </p>
                <p>
                  <strong>Billing Email:</strong> {billingInfo.billingEmail}
                </p>
                <p>
                  <strong>Address:</strong> {billingInfo.billingAddress1}
                </p>
              </div>
              <div>
                <p>
                  <strong>City:</strong> {billingInfo.city}
                </p>
                <p>
                  <strong>State:</strong> {billingInfo.state}
                </p>
                <p>
                  <strong>Country:</strong> {billingInfo.country}
                </p>
                <p>
                  <strong>Zip Code:</strong> {billingInfo.zipCode}
                </p>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Product Selection Summary */}
        {productData && productData.selectedProducts.length > 0 && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between text-lg">
                <span>Selected Products</span>
                <Edit2 className="size-4 text-muted-foreground" />
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
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
              <div className="pt-2 text-right">
                <p className="text-lg font-bold text-primary">
                  Total: ${productData.totalAmount}
                </p>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Payment Status */}
        {paymentData && (
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Payment Status</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-center gap-2">
                <CheckCircle className="size-5 text-primary" />
                <span className="capitalize">
                  {paymentData.paymentMethod === 'credit-card'
                    ? 'Credit Card'
                    : paymentData.paymentMethod === 'bank-transfer'
                      ? 'Bank Transfer'
                      : 'PayPal'}
                  Payment Completed
                </span>
              </div>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Email Configuration */}
      {/* <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Mail className="h-5 w-5" />
            Confirmation Email
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label>Email Template</Label>
            <Select value={emailTemplate} onValueChange={handleTemplateChange}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {emailTemplates.map(template => (
                  <SelectItem key={template.value} value={template.value}>
                    {template.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>Email Content</Label>
            <Textarea
              value={emailContent}
              onChange={e => setEmailContent(e.target.value)}
              rows={8}
              placeholder="Enter email content..."
            />
          </div>

          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowPreview(!showPreview)}
            >
              <Eye className="h-4 w-4 mr-2" />
              {showPreview ? 'Hide Preview' : 'Show Preview'}
            </Button>
          </div>

          {showPreview && (
            <Card className="bg-muted/50">
              <CardHeader>
                <CardTitle className="text-sm">Email Preview</CardTitle>
              </CardHeader>
              <CardContent>
                <pre className="whitespace-pre-wrap text-sm">
                  {getProcessedEmailContent()}
                </pre>
              </CardContent>
            </Card>
          )}
        </CardContent>
      </Card> */}

      {/* Login Credentials Card */}
      <Card className="bg-gradient-to-r from-primary/10 to-primary/10">
        <CardHeader>
          <CardTitle className="text-lg">Generated Login Credentials</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <div className="grid gap-4 text-sm md:grid-cols-2">
            <div>
              <p>
                <strong>Username:</strong> {mspDetails?.mspAdminEmail}
              </p>
              <p>
                <strong>Temporary Password:</strong> {tempPassword}
              </p>
            </div>
            <div>
              <p>
                <strong>Portal Link:</strong> {portalLink}
              </p>
              <p>
                <strong>Status:</strong> Active
              </p>
            </div>
          </div>
        </CardContent>
      </Card>

      <Button
        onClick={handleSubmit}
        className="h-12 w-full bg-primary transition-opacity hover:opacity-90"
        disabled={isLoading}
      >
        {isLoading ? (
          <div className="flex items-center gap-2">
            <div className="size-4 animate-spin rounded-full border-2 border-primary-foreground border-t-transparent" />
            Sending Confirmation Email...
          </div>
        ) : (
          <div className="flex items-center gap-2">
            Complete Registration
            <ArrowRight className="size-4" />
          </div>
        )}
      </Button>
    </div>
  );
};
