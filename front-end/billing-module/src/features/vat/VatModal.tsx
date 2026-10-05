import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
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
import { Switch } from 'common/Switch';
import { Plus, Trash2 } from 'lucide-react';
import { VatConfiguration } from 'models/Vat';
import { useEffect, useState } from 'react';
import { useFieldArray, useForm } from 'react-hook-form';
import * as z from 'zod';

import { API_END_POINTS } from 'routes/APIEndpoints';
import { useAPI } from 'hooks/UseAPI';
import { isSuccessResponse } from 'utils/Helper';
import { ICountryDropdown, IStateDropdown } from 'models/Country';
import SearchSelect from 'components/SearchSelect';

const regionSchema = z.object({
  id: z.string().min(1, 'Region code is required'),
  regionName: z.string().min(1, 'Region name is required'),
  vatRate: z
    .number()
    .min(1, 'VAT rate must be 1% or greater')
    .max(100, 'VAT rate must be 100% or less'),
});

const vatFormSchema = z.object({
  id: z.string().min(1, 'Country name is required'),
  countryName: z.string().min(1, 'Country name is required'),
  defaultVatRate: z
    .number()
    .min(1, 'VAT rate must be 1% or greater')
    .max(100, 'VAT rate must be 100% or less'),
  regionBased: z.boolean(),
  regions: z.array(regionSchema).refine(
    regions => {
      const regionCodes = regions.map(r => r.id);
      return new Set(regionCodes).size === regionCodes.length;
    },
    {
      message: 'Duplicate regions are not allowed',
    },
  ),
});

type VatFormData = z.infer<typeof vatFormSchema>;

interface VatFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: VatConfiguration) => void;
  editingData?: VatConfiguration | null;
  handleResetForm: () => void;
  isSubmitLoading: boolean;
}

export const VatFormModal = ({
  isOpen,
  onClose,
  onSubmit,
  editingData,
  handleResetForm,
  isSubmitLoading,
}: VatFormModalProps) => {
  const apiClient = useAPI();
  const form = useForm<VatFormData>({
    resolver: zodResolver(vatFormSchema),
    defaultValues: {
      id: '',
      countryName: '',
      defaultVatRate: 15,
      regionBased: false,
      regions: [],
    },
  });

  const [countryList, setCountryList] = useState<Array<ICountryDropdown>>([]);
  const [statesList, setStatesList] = useState<Array<IStateDropdown>>([]);

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'regions',
  });

  const regionBased = form.watch('regionBased');
  const regions = form.watch('regions');

  const fetchCountryList = async () => {
    try {
      const response = await apiClient.get(API_END_POINTS.COUNTRY_LIST);
      if (isSuccessResponse(response.statusCode)) {
        setCountryList(
          response.data.map((country: ICountryDropdown) => ({
            id: country.id,
            name: country.name,
            code: country.id,
          })),
        );
      }
    } catch (error) {
      console.error('Error fetching country list:', error);
    }
  };

  const fetchStatesList = async (countryId: string) => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.STATE_LIST_BY_COUNTRY.replace(':countryId', countryId),
      );
      if (isSuccessResponse(response.statusCode)) {
        setStatesList(response.data);
      }
    } catch (error) {
      console.error('Error fetching states list:', error);
    }
  };

  useEffect(() => {
    if (isOpen && form.watch('id')) {
      fetchStatesList(form.watch('id'));
    }
  }, [isOpen, form.watch('id')]);

  useEffect(() => {
    if (isOpen) {
      fetchCountryList();
    }
  }, [isOpen]);

  useEffect(() => {
    if (editingData) {
      form.reset({
        id: editingData.id,
        countryName: editingData.countryName,
        defaultVatRate: editingData.defaultVatRate,
        regionBased: editingData.regionBased,
        regions: editingData.regions,
      });
    }
  }, [editingData, form, statesList]);

  useEffect(() => {
    if (!regionBased) {
      form.setValue('regions', []);
    }
  }, [regionBased, form]);

  const handleSubmit = (data: VatFormData) => {
    const formattedData: VatConfiguration = {
      id: data.id,
      countryName: data.countryName,
      defaultVatRate: data.defaultVatRate,
      regionBased: data.regionBased,
      regions: data.regions.map(region => ({
        id: region.id,
        regionName: region.regionName,
        vatRate: region.vatRate,
      })),
      createdAt: editingData?.createdAt || Date.now(),
      updatedAt: Date.now(),
    };
    onSubmit(formattedData);
  };

  const handleResetFormHandler = () => {
    form.reset();
    handleResetForm();
    onClose();
    handleResetForm();
  };

  const addRegion = () => {
    append({
      id: '',
      regionName: '',
      vatRate: 15,
    });
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-h-[80vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {editingData ? 'Edit VAT Configuration' : 'Add VAT Configuration'}
          </DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(handleSubmit)}
            className="space-y-6"
          >
            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="id"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Country</FormLabel>
                    <FormControl>
                      <SearchSelect
                        value={field.value}
                        onValueChange={(value: string) => {
                          field.onChange(value);
                          form.setValue('id', value);
                          form.setValue(
                            'countryName',
                            countryList.find(country => country.id === value)
                              ?.name || '',
                          );
                        }}
                        items={countryList.map(country => ({
                          value: country.id,
                          label: country.name,
                        }))}
                        hasError={!!form.formState.errors.countryName}
                        placeholder="Select a country"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="defaultVatRate"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Default VAT Rate (1-100%)</FormLabel>
                    <FormControl>
                      <Input
                        {...field}
                        type="number"
                        onChange={e =>
                          field.onChange(parseFloat(e.target.value) || 0)
                        }
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="grid grid-cols-1 gap-4">
              <FormField
                control={form.control}
                name="regionBased"
                render={({ field }) => (
                  <FormItem className="flex flex-row items-center justify-between rounded-lg border border-card-border p-4">
                    <div className="space-y-0.5">
                      <FormLabel className="text-base">Region Based</FormLabel>
                      <div className="text-sm text-muted-foreground">
                        Enable region-specific VAT rates
                      </div>
                    </div>
                    <FormControl>
                      <Switch
                        checked={field.value}
                        onCheckedChange={field.onChange}
                      />
                    </FormControl>
                  </FormItem>
                )}
              />
            </div>

            {regionBased && (
              <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                  <CardTitle className="text-lg">Regions</CardTitle>
                  <Button type="button" onClick={addRegion} size="sm">
                    <Plus className="mr-2 size-4" />
                    Add Region
                  </Button>
                </CardHeader>
                <CardContent className="space-y-4">
                  {fields.map((field, index) => (
                    <div
                      key={field.id}
                      className="grid grid-cols-12 items-end gap-4"
                    >
                      <div className="col-span-6">
                        <FormField
                          control={form.control}
                          name={`regions.${index}.id`}
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>Region</FormLabel>
                              <FormControl>
                                <SearchSelect
                                  value={field.value}
                                  onValueChange={(value: string) => {
                                    field.onChange(value);
                                    form.setValue(`regions.${index}.id`, value);
                                    form.setValue(
                                      `regions.${index}.regionName`,
                                      statesList.find(
                                        state => state.id === value,
                                      )?.name || '',
                                    );
                                  }}
                                  items={statesList.map(state => ({
                                    value: state.id,
                                    label: state.name,
                                  }))}
                                  hasError={
                                    !!form.formState.errors.regions?.[index]?.id
                                  }
                                  placeholder="Select region"
                                />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                      </div>

                      <div className="col-span-5">
                        <FormField
                          control={form.control}
                          name={`regions.${index}.vatRate`}
                          render={({ field }) => (
                            <FormItem>
                              <FormLabel>VAT Rate (1-100%)</FormLabel>
                              <FormControl>
                                <Input
                                  {...field}
                                  type="number"
                                  onChange={e =>
                                    field.onChange(
                                      parseFloat(e.target.value) || 0,
                                    )
                                  }
                                />
                              </FormControl>
                              <FormMessage />
                            </FormItem>
                          )}
                        />
                      </div>

                      <div className="col-span-1 text-end">
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => remove(index)}
                        >
                          <Trash2 className="size-4" />
                        </Button>
                      </div>
                    </div>
                  ))}

                  {fields.length === 0 && (
                    <p className="py-4 text-center text-muted-foreground">
                      No regions added yet. Click "Add Region" to get started.
                    </p>
                  )}
                </CardContent>
              </Card>
            )}

            <div className="flex justify-end gap-3">
              <Button
                type="button"
                variant="outline"
                onClick={handleResetFormHandler}
              >
                Cancel
              </Button>
              <Button type="submit" disabled={isSubmitLoading}>
                {isSubmitLoading
                  ? 'Submitting...'
                  : editingData
                    ? 'Update VAT Configuration'
                    : 'Create VAT Configuration'}
              </Button>
            </div>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
};
