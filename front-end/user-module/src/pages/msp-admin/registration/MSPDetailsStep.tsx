import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { CustomCountrySelect } from 'components/CustomCountryStateSelect';
import CustomPhoneInput from 'components/CustomPhoneInput';
import CustomTimeZoneSelect from 'components/CustomTimeZoneSelect';
import { ArrowRight, Building2, Globe, Upload } from 'lucide-react';
import { useState } from 'react';

interface MSPDetailsStepProps {
  onNext: (data: MSPDetailsData) => void;
  userType: 'msp' | 'admin';
}

export interface MSPDetailsData {
  organizationName: string;
  mspTier: string;
  contactEmail: string;
  phoneNumber: string;
  countryCode: string;
  country: string;
  state: string;
  timeZone: string;
  language: string;
  domain: string;
  organizationSize: string;
  mspType: string;
  mspAdminEmail: string;
  companyAddress: string;
  organizationLogo?: File;
  creditAllocation?: number;
  netTerm?: string;
}

export const MSPDetailsStep = ({ onNext, userType }: MSPDetailsStepProps) => {
  const [formData, setFormData] = useState<MSPDetailsData>({
    organizationName: '',
    mspTier: 'Silver Partner',
    contactEmail: '',
    phoneNumber: '',
    countryCode: '',
    country: '',
    state: '',
    timeZone: '',
    language: 'English',
    domain: '',
    organizationSize: '',
    mspType: '',
    mspAdminEmail: '',
    companyAddress: '',
    creditAllocation: userType === 'admin' ? 0 : undefined,
    netTerm: userType === 'admin' ? '' : undefined,
  });

  const [logoFile, setLogoFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const countries = [
    { code: 'US', name: 'United States', flag: '🇺🇸' },
    { code: 'CA', name: 'Canada', flag: '🇨🇦' },
    { code: 'GB', name: 'United Kingdom', flag: '🇬🇧' },
    { code: 'DE', name: 'Germany', flag: '🇩🇪' },
    { code: 'FR', name: 'France', flag: '🇫🇷' },
    { code: 'AU', name: 'Australia', flag: '🇦🇺' },
    { code: 'IN', name: 'India', flag: '🇮🇳' },
  ];

  const organizationSizes = ['Small (1-50)', 'Medium (51-200)', 'Large (200+)'];
  const mspTypes = ['Managed Services Provider', 'Partner', 'Reseller'];
  const netTerms = ['Net 30', 'Net 45', 'Net 60', 'Net 90'];

  const handleInputChange = (
    field: keyof MSPDetailsData,
    value: string | number,
  ) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
      // Auto-fill domain from email
      ...(field === 'mspAdminEmail' && {
        domain: value.toString().split('@')[1] || '',
      }),
    }));
  };

  const handleCountryChange = (countryCode: string) => {
    setFormData(prev => ({
      ...prev,
      country: countryCode,
      countryCode,
    }));
  };

  const handleLogoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        // toast({
        //   title: 'File Too Large',
        //   description: 'Logo file must be less than 5MB',
        //   variant: 'destructive',
        // });
        return;
      }
      setLogoFile(file);
      setFormData(prev => ({ ...prev, organizationLogo: file }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const requiredFields = [
      'organizationName',
      'contactEmail',
      'country',
      'state',
      'timeZone',
      'organizationSize',
      'mspType',
      'mspAdminEmail',
    ];

    const missingFields = requiredFields.filter(
      field => !formData[field as keyof MSPDetailsData],
    );

    if (missingFields.length > 0) {
      //   toast({
      //     title: 'Required Fields Missing',
      //     description: 'Please fill in all required fields',
      //     variant: 'destructive',
      //   });
      return;
    }

    setIsLoading(true);

    setTimeout(() => {
      setIsLoading(false);
      onNext(formData);
      //   toast({
      //     title: 'MSP Details Saved',
      //     description: 'Organization details have been saved successfully',
      //   });
    }, 1500);
  };

  return (
    <div className="space-y-6">
      <div className="text-center">
        <div className="mb-6 flex items-center justify-center">
          <Building2 className="size-8 text-white" />
        </div>
        <h3 className="mb-2 text-xl font-semibold">
          {userType === 'admin'
            ? 'MSP Organization Details'
            : 'Your Organization Details'}
        </h3>
        <p className="text-muted-foreground">
          Provide information about your managed service provider organization
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid gap-6 md:grid-cols-2">
          {/* Organization Information */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">
                Organization Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="organizationName">Organization Name *</Label>
                <Input
                  id="organizationName"
                  value={formData.organizationName}
                  onChange={e =>
                    handleInputChange('organizationName', e.target.value)
                  }
                  placeholder="Enter organization name"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="mspTier">MSP Tier</Label>
                <Input
                  id="mspTier"
                  value={formData.mspTier}
                  readOnly
                  className="bg-muted"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="contactEmail">Contact Email</Label>
                <Input
                  id="contactEmail"
                  type="email"
                  value={formData.contactEmail}
                  onChange={e =>
                    handleInputChange('contactEmail', e.target.value)
                  }
                  placeholder="contact@organization.com"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="mspAdminEmail">MSP Admin Email *</Label>
                <Input
                  id="mspAdminEmail"
                  type="email"
                  value={formData.mspAdminEmail}
                  onChange={e =>
                    handleInputChange('mspAdminEmail', e.target.value)
                  }
                  placeholder="admin@organization.com"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="domain">Domain</Label>
                <div className="relative">
                  <Globe className="absolute left-3 top-3 size-4 text-muted-foreground" />
                  <Input
                    id="domain"
                    value={formData.domain}
                    readOnly
                    className="bg-muted pl-10"
                    placeholder="Auto-filled from email"
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Contact & Location */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Contact & Location</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="phoneNumber">Phone</Label>
                <div className="">
                  <CustomPhoneInput
                    value={formData.phoneNumber}
                    handleChange={e => handleInputChange('phoneNumber', e)}
                    placeholder="Phone number"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="countryCode">Country</Label>
                <CustomCountrySelect
                  value={formData.country}
                  handleChange={handleCountryChange}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="timeZone">Time Zone *</Label>
                <CustomTimeZoneSelect
                  countryCode={formData.countryCode}
                  value={formData.timeZone}
                  handleChange={value => handleInputChange('timeZone', value)}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="state">State/Province/Division *</Label>
                <Input
                  id="state"
                  value={formData.state}
                  onChange={e => handleInputChange('state', e.target.value)}
                  placeholder="Enter state or province"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="language">Language *</Label>
                <Select
                  value={formData.language}
                  onValueChange={value => handleInputChange('language', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="English">English</SelectItem>
                    <SelectItem value="Spanish">Spanish</SelectItem>
                    <SelectItem value="French">French</SelectItem>
                    <SelectItem value="German">German</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="companyAddress">Company Address</Label>
                <Input
                  id="companyAddress"
                  value={formData.companyAddress}
                  onChange={e =>
                    handleInputChange('companyAddress', e.target.value)
                  }
                  placeholder="Full company address"
                />
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Organization Type & Size */}
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">Organization Details</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4 md:grid-cols-3">
            <div className="space-y-2">
              <Label htmlFor="organizationSize">Organization Size *</Label>
              <Select
                value={formData.organizationSize}
                onValueChange={value =>
                  handleInputChange('organizationSize', value)
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select size" />
                </SelectTrigger>
                <SelectContent>
                  {organizationSizes.map(size => (
                    <SelectItem key={size} value={size}>
                      {size}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="mspType">MSP Type *</Label>
              <Select
                value={formData.mspType}
                onValueChange={value => handleInputChange('mspType', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select type" />
                </SelectTrigger>
                <SelectContent>
                  {mspTypes.map(type => (
                    <SelectItem key={type} value={type}>
                      {type}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="logo">Organization Logo</Label>
              <div className="relative">
                <Input
                  id="logo"
                  type="file"
                  accept="image/*"
                  onChange={handleLogoUpload}
                  className="hidden"
                />
                <Button
                  type="button"
                  variant="outline"
                  className="w-full"
                  onClick={() => document.getElementById('logo')?.click()}
                >
                  <Upload className="mr-2 size-4" />
                  {logoFile ? logoFile.name : 'Upload Logo'}
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Admin-only fields */}
        {userType === 'admin' && (
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">
                Credit Allocation (Admin Only)
              </CardTitle>
            </CardHeader>
            <CardContent className="grid gap-4 md:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="creditAllocation">Credit Allocation</Label>
                <Input
                  id="creditAllocation"
                  type="number"
                  value={formData.creditAllocation || ''}
                  onChange={e =>
                    handleInputChange(
                      'creditAllocation',
                      parseInt(e.target.value) || 0,
                    )
                  }
                  placeholder="Enter credit amount"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="netTerm">Net Term (Optional)</Label>
                <Select
                  value={formData.netTerm}
                  onValueChange={value => handleInputChange('netTerm', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select net term" />
                  </SelectTrigger>
                  <SelectContent>
                    {netTerms.map(term => (
                      <SelectItem key={term} value={term}>
                        {term}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </CardContent>
          </Card>
        )}

        <Button
          type="submit"
          className="h-12 w-full bg-primary transition-opacity hover:opacity-90"
          disabled={isLoading}
        >
          {isLoading ? (
            <div className="flex items-center gap-2">
              <div className="size-4 animate-spin rounded-full border-2 border-primary-foreground border-t-transparent" />
              Saving Details...
            </div>
          ) : (
            <div className="flex items-center gap-2">
              Save and Continue
              <ArrowRight className="size-4" />
            </div>
          )}
        </Button>
      </form>
    </div>
  );
};
