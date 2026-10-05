import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';

import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'common/Dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from 'common/Form';
import { Input } from 'common/Input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { Textarea } from 'common/Textarea';
import { useAPI } from 'hooks/UseAPI';
import { ITier } from 'models/Tier';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { TierFormData, tierSchema } from 'schemas/tierSchema';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  tier?: ITier | null;
  onSave: () => void;
}

const TierModal = ({ isOpen, onClose, tier, onSave }: IProps) => {
  const [loading, setLoading] = useState(false);
  const apiClient = useAPI();

  const form = useForm<TierFormData>({
    resolver: zodResolver(tierSchema),
    mode: 'onChange',
    defaultValues: {
      tierName: '',
      tierDescription: '',
      commissionPercentage: 0,
      salesThreshold: 0,
      eligibilityCriteria: '',
      tierBenefits: '',
      active: 'true',
    },
  });

  useEffect(() => {
    if (tier) {
      form.reset({
        tierName: tier.tierName ?? '',
        tierDescription: tier.tierDescription ?? '',
        commissionPercentage: tier.commissionPercentage ?? 0,
        salesThreshold: tier.salesThreshold ?? 0,
        eligibilityCriteria: tier.eligibilityCriteria ?? '',
        tierBenefits: tier.tierBenefits ?? '',
        active: String(tier.active ?? false),
      });
    } else {
      form.reset();
    }
  }, [tier, form]);

  const handleSubmit = async (data: TierFormData) => {
    setLoading(true);

    const formData = {
      ...data,
      active: data.active === 'true',
    };

    let response;

    try {
      if (tier) {
        response = await apiClient.put(API_END_POINTS.TIER_UPDATE + tier.id, {
          data: formData,
        });
      } else {
        response = await apiClient.post(API_END_POINTS.TIER_CREATE, {
          data: formData,
        });
      }
      if (response.status === 409) {
        toast.error(response.message);
        return;
      }
      toast.success(
        tier ? 'Tier updated successfully' : 'Tier created successfully',
      );
      onSave();
      form.reset();
      onClose();
    } catch (error) {
      console.error('Error saving tier:', error);
      toast.error('Failed to save tier');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    form.reset();
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="max-h-[80vh] w-2/3 overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{tier ? 'Edit Tier' : 'Add New Tier'}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(handleSubmit)}
            className="space-y-4"
          >
            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="tierName"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Tier Name *</FormLabel>
                    <FormControl>
                      <Input {...field} placeholder="Enter tier name" />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="active"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Status</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select status" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        <SelectItem value="true">Active</SelectItem>
                        <SelectItem value="false">Inactive</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="tierDescription"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Description</FormLabel>
                  <FormControl>
                    <Textarea {...field} placeholder="Enter tier description" />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="commissionPercentage"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Commission Percentage *</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        {...field}
                        onChange={e => field.onChange(Number(e.target.value))}
                        placeholder="0"
                        min="0"
                        max="100"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="salesThreshold"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Sales Threshold</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        {...field}
                        onChange={e => field.onChange(Number(e.target.value))}
                        placeholder="0"
                        min="0"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="eligibilityCriteria"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Eligibility Criteria</FormLabel>
                  <FormControl>
                    <Textarea
                      {...field}
                      placeholder="Enter eligibility criteria"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="tierBenefits"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Tier Benefits</FormLabel>
                  <FormControl>
                    <Textarea {...field} placeholder="Enter tier benefits" />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex justify-end space-x-2">
              <Button type="button" variant="outline" onClick={handleClose}>
                Cancel
              </Button>
              <Button type="submit" disabled={loading}>
                {tier
                  ? loading
                    ? 'Updating...'
                    : 'Update'
                  : loading
                    ? 'Creating...'
                    : 'Create'}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
};

export default TierModal;
