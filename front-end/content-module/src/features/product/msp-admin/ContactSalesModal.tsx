import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';

import CustomPhoneInput from 'components/CustomPhoneInput';
import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { IResponse } from 'models/Global';
import { IMspCatalogProduct } from 'models/MspCatalog';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  ContactSalesSchema,
  DefaultContactSalesValues,
  HEAR_ABOUT_US_OPTIONS,
  TContactSalesFormFields,
} from 'schemas/ContactSalesSchema';
import { isSuccessResponse } from 'utils/Helper';

interface IOrganizationSize {
  id: string;
  name: string;
  range?: string;
}

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  product: IMspCatalogProduct;
}

const splitFullName = (fullName?: string) => {
  if (!fullName?.trim()) {
    return { firstName: '', lastName: '' };
  }

  const parts = fullName.trim().split(/\s+/);
  if (parts.length === 1) {
    return { firstName: parts[0], lastName: '' };
  }

  return {
    firstName: parts[0],
    lastName: parts.slice(1).join(' '),
  };
};

const ContactSalesModal = ({ isOpen, onClose, product }: IProps) => {
  const apiClient = useAPI();
  const { userInfo } = useStore();

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [organizationSizes, setOrganizationSizes] = useState<
    IOrganizationSize[]
  >([]);

  const defaultValues = useMemo(() => {
    const { firstName, lastName } = splitFullName(userInfo?.fullName);

    return {
      ...DefaultContactSalesValues,
      email: userInfo?.email ?? '',
      phone: userInfo?.phoneNumber ?? '',
      firstName,
      lastName,
    };
  }, [userInfo]);

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TContactSalesFormFields>({
    resolver: zodResolver(ContactSalesSchema),
    defaultValues,
    mode: 'onChange',
  });

  useEffect(() => {
    if (!isOpen) return;

    reset(defaultValues);

    const fetchOrganizationSizes = async () => {
      try {
        const response: IResponse<IOrganizationSize[]> = await apiClient.get(
          API_END_POINTS.GET_ACTIVE_ORGANIZATION_SIZE_LIST,
        );
        if (isSuccessResponse(response.statusCode)) {
          setOrganizationSizes(response.data ?? []);
        }
      } catch (error) {
        console.error('Error fetching organization sizes:', error);
      }
    };

    fetchOrganizationSizes();
  }, [apiClient, defaultValues, isOpen, reset]);

  const handleClose = () => {
    if (isSubmitting) return;
    reset(defaultValues);
    onClose();
  };

  const onSubmit = async (fields: TContactSalesFormFields) => {
    if (!userInfo?.userId) {
      toast.error('Unable to submit request. Please sign in again.');
      return;
    }

    setIsSubmitting(true);
    try {
      const response: IResponse<unknown> = await apiClient.post(
        API_END_POINTS.MSP_CONTACT_SALES.replace(':mspId', userInfo.userId),
        {
          ...fields,
          productId: product.productId,
        },
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Failed to submit your request.');
      }

      toast.success(
        response.message ||
          'Your request has been submitted. Our sales team will contact you soon.',
      );
      reset(defaultValues);
      onClose();
    } catch (error) {
      toast.error(
        (error as Error).message ||
          'Failed to submit your request. Please try again.',
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={open => !open && handleClose()}>
      <DialogContent className="content-max-h-[90vh] content-overflow-y-auto sm:content-max-w-[560px]">
        <DialogHeader>
          <DialogTitle className="content-text-white">
            Contact Sales Team
          </DialogTitle>
          <DialogDescription>
            Interested in {product.productName}? Fill out the form below and
            our sales team will get back to you.
          </DialogDescription>
        </DialogHeader>

        <form
          onSubmit={handleSubmit(onSubmit)}
          className="content-space-y-4 content-pt-2"
        >
          <div className="content-grid content-gap-4 sm:content-grid-cols-2">
            <div className="content-space-y-2">
              <Label htmlFor="firstName">
                First Name <span className="content-text-red-500">*</span>
              </Label>
              <Controller
                name="firstName"
                control={control}
                render={({ field }) => (
                  <Input
                    {...field}
                    id="firstName"
                    placeholder="Enter first name"
                    className="content-bg-transparent"
                  />
                )}
              />
              {errors.firstName && (
                <p className="content-text-sm content-text-red-500">
                  {errors.firstName.message}
                </p>
              )}
            </div>

            <div className="content-space-y-2">
              <Label htmlFor="lastName">
                Last Name <span className="content-text-red-500">*</span>
              </Label>
              <Controller
                name="lastName"
                control={control}
                render={({ field }) => (
                  <Input
                    {...field}
                    id="lastName"
                    placeholder="Enter last name"
                    className="content-bg-transparent"
                  />
                )}
              />
              {errors.lastName && (
                <p className="content-text-sm content-text-red-500">
                  {errors.lastName.message}
                </p>
              )}
            </div>
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="email">
              Email Address <span className="content-text-red-500">*</span>
            </Label>
            <Controller
              name="email"
              control={control}
              render={({ field }) => (
                <Input
                  {...field}
                  id="email"
                  type="email"
                  placeholder="Enter email address"
                  className="content-bg-transparent"
                />
              )}
            />
            {errors.email && (
              <p className="content-text-sm content-text-red-500">
                {errors.email.message}
              </p>
            )}
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="phone">
              Phone <span className="content-text-red-500">*</span>
            </Label>
            <Controller
              name="phone"
              control={control}
              render={({ field }) => (
                <CustomPhoneInput
                  value={field.value}
                  placeholder="Enter phone number"
                  handleChange={field.onChange}
                />
              )}
            />
            {errors.phone && (
              <p className="content-text-sm content-text-red-500">
                {errors.phone.message}
              </p>
            )}
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="companyName">
              Company Name <span className="content-text-red-500">*</span>
            </Label>
            <Controller
              name="companyName"
              control={control}
              render={({ field }) => (
                <Input
                  {...field}
                  id="companyName"
                  placeholder="Enter company name"
                  className="content-bg-transparent"
                />
              )}
            />
            {errors.companyName && (
              <p className="content-text-sm content-text-red-500">
                {errors.companyName.message}
              </p>
            )}
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="numberOfEmployees">
              Number of Employees{' '}
              <span className="content-text-red-500">*</span>
            </Label>
            <Controller
              name="numberOfEmployees"
              control={control}
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger
                    id="numberOfEmployees"
                    className="content-bg-transparent"
                  >
                    <SelectValue placeholder="Select number of employees" />
                  </SelectTrigger>
                  <SelectContent>
                    {organizationSizes.map(size => (
                      <SelectItem key={size.id} value={size.id}>
                        {size.range ? `${size.name} (${size.range})` : size.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.numberOfEmployees && (
              <p className="content-text-sm content-text-red-500">
                {errors.numberOfEmployees.message}
              </p>
            )}
          </div>

          <div className="content-space-y-2">
            <Label htmlFor="hearAboutUs">
              How did you hear about us?{' '}
              <span className="content-text-red-500">*</span>
            </Label>
            <Controller
              name="hearAboutUs"
              control={control}
              render={({ field }) => (
                <Select value={field.value} onValueChange={field.onChange}>
                  <SelectTrigger
                    id="hearAboutUs"
                    className="content-bg-transparent"
                  >
                    <SelectValue placeholder="Select an option" />
                  </SelectTrigger>
                  <SelectContent>
                    {HEAR_ABOUT_US_OPTIONS.map(option => (
                      <SelectItem key={option} value={option}>
                        {option}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.hearAboutUs && (
              <p className="content-text-sm content-text-red-500">
                {errors.hearAboutUs.message}
              </p>
            )}
          </div>

          <div className="content-flex content-flex-col content-gap-2 content-pt-2 sm:content-flex-row sm:content-justify-end">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isSubmitting}
              className="content-bg-transparent"
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? (
                <>
                  <Loader2 className="content-mr-2 content-size-4 content-animate-spin" />
                  Submitting...
                </>
              ) : (
                'Submit'
              )}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ContactSalesModal;
