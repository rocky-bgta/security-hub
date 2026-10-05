import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import { CustomCountrySelect } from 'components/CustomCountryStateSelect';
import { ArrowRight, CreditCard, MapPin } from 'lucide-react';
import { useState } from 'react';

interface BillingInfoStepProps {
  onNext: (data: BillingInfoData) => void;
}

export interface BillingInfoData {
  billingEmail: string;
  billingName: string;
  billingAddress1: string;
  billingAddress2?: string;
  country: string;
  state: string;
  city: string;
  zipCode: string;
}

export const BillingInfoStep = ({ onNext }: BillingInfoStepProps) => {
  const [formData, setFormData] = useState<BillingInfoData>({
    billingEmail: '',
    billingName: '',
    billingAddress1: '',
    billingAddress2: '',
    country: '',
    state: '',
    city: '',
    zipCode: '',
  });

  const [isLoading, setIsLoading] = useState(false);

  const countries = [
    'United States',
    'Canada',
    'United Kingdom',
    'Germany',
    'France',
    'Australia',
    'India',
    'Japan',
    'Other',
  ];

  const handleInputChange = (field: keyof BillingInfoData, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const requiredFields: (keyof BillingInfoData)[] = [
      'billingEmail',
      'billingName',
      'billingAddress1',
      'country',
      'state',
      'city',
      'zipCode',
    ];

    const missingFields = requiredFields.filter(field => !formData[field]);

    if (missingFields.length > 0) {
      //   toast({
      //     title: 'Required Fields Missing',
      //     description: 'Please fill in all required billing fields',
      //     variant: 'destructive',
      //   });
      return;
    }

    // Email validation
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.billingEmail)) {
      //   toast({
      //     title: 'Invalid Email',
      //     description: 'Please enter a valid billing email address',
      //     variant: 'destructive',
      //   });
      return;
    }

    setIsLoading(true);

    setTimeout(() => {
      setIsLoading(false);
      onNext(formData);
      //   toast({
      //     title: 'Billing Information Saved',
      //     description: 'Your billing details have been saved successfully',
      //   });
    }, 1500);
  };

  return (
    <div className="space-y-6">
      <div className="text-center">
        <div className="mb-6 flex items-center justify-center">
          <CreditCard className="size-8 text-white" />
        </div>
        <h3 className="mb-2 text-xl font-semibold">Billing Information</h3>
        <p className="text-muted-foreground">
          Provide your billing details for payment processing
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid gap-6 md:grid-cols-2">
          {/* Billing Contact */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-lg">
                <CreditCard className="size-5" />
                Billing Contact
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="billingEmail">Billing Email *</Label>
                <Input
                  id="billingEmail"
                  type="email"
                  value={formData.billingEmail}
                  onChange={e =>
                    handleInputChange('billingEmail', e.target.value)
                  }
                  placeholder="billing@company.com"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="billingName">Billing Name *</Label>
                <Input
                  id="billingName"
                  value={formData.billingName}
                  onChange={e =>
                    handleInputChange('billingName', e.target.value)
                  }
                  placeholder="Full name for billing"
                  required
                />
              </div>
            </CardContent>
          </Card>

          {/* Billing Address */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-lg">
                <MapPin className="size-5" />
                Billing Address
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="billingAddress1">Address Line 1 *</Label>
                <Input
                  id="billingAddress1"
                  value={formData.billingAddress1}
                  onChange={e =>
                    handleInputChange('billingAddress1', e.target.value)
                  }
                  placeholder="Street address"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="billingAddress2">Address Line 2</Label>
                <Input
                  id="billingAddress2"
                  value={formData.billingAddress2}
                  onChange={e =>
                    handleInputChange('billingAddress2', e.target.value)
                  }
                  placeholder="Apartment, suite, etc. (optional)"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="country">Country *</Label>
                <CustomCountrySelect
                  value={formData.country}
                  handleChange={value => handleInputChange('country', value)}
                />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Location Details */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Location Details</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Label htmlFor="state">State/Province *</Label>
              <Input
                id="state"
                value={formData.state}
                onChange={e => handleInputChange('state', e.target.value)}
                placeholder="State or province"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="city">City *</Label>
              <Input
                id="city"
                value={formData.city}
                onChange={e => handleInputChange('city', e.target.value)}
                placeholder="City"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="zipCode">ZIP/Postal Code *</Label>
              <Input
                id="zipCode"
                value={formData.zipCode}
                onChange={e => handleInputChange('zipCode', e.target.value)}
                placeholder="ZIP or postal code"
                required
              />
            </div>
          </CardContent>
        </Card>

        {/* Form Validation Summary */}
        <div className="rounded-lg border p-4">
          <h4 className="mb-2 font-medium">Required Information</h4>
          <p className="text-sm text-muted-foreground">
            All fields marked with * are required for processing payments and
            generating invoices. Make sure all information is accurate to avoid
            billing issues.
          </p>
        </div>

        <Button
          type="submit"
          className="h-12 w-full bg-primary transition-opacity hover:opacity-90"
          disabled={isLoading}
        >
          {isLoading ? (
            <div className="flex items-center gap-2">
              <div className="size-4 animate-spin rounded-full border-2 border-primary-foreground border-t-transparent" />
              Saving Billing Information...
            </div>
          ) : (
            <div className="flex items-center gap-2">
              Save Billing Information
              <ArrowRight className="size-4" />
            </div>
          )}
        </Button>
      </form>
    </div>
  );
};
