import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import { Input } from 'common/Input';
import { ArrowRightIcon, Loader2 } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import {
  SMS_CAMPAIGN_TYPE_OPTIONS,
  CampaignChannel,
  CampaignType,
  CampaignValidityUnit,
  PhishingAssignedFor,
} from 'models/Campaign';
import { useForm } from 'react-hook-form';
import {
  SmsCampaignSetupSchema,
  TSmsCampaignSetupForm,
  smsCampaignSetupDefaultValues,
} from 'schemas/SmsCampaignSchema';
import {
  Select,
  SelectValue,
  SelectTrigger,
  SelectItem,
  SelectContent,
} from 'common/Select';
import { Label } from 'common/Label';
import { RadioGroup, RadioGroupItem } from 'common/Radio';
import { PackageSelectField } from 'features/campaign/CampaignWizard/PackageSelectField';

interface Step1SetupProps {
  initialData?: Partial<TSmsCampaignSetupForm>;
  onSubmit: (data: TSmsCampaignSetupForm) => void;
  onBack?: () => void;
  isLoading?: boolean;
}

const MAX_EXPIRY_DAYS = 365;
const MAX_EXPIRY_MONTHS = 12;

export const Step1Setup = ({
  initialData,
  onSubmit,
  onBack,
  isLoading,
}: Step1SetupProps) => {

  const [clickChecked, setClickChecked] = useState(true);
  const [compromisedChecked, setCompromisedChecked] = useState(true);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<TSmsCampaignSetupForm>({
    resolver: zodResolver(SmsCampaignSetupSchema),
    defaultValues: { ...smsCampaignSetupDefaultValues, ...initialData },
  });

  useEffect(() => {
    if (initialData?.assignedFor) {
      setClickChecked(
        initialData.assignedFor ===
          PhishingAssignedFor.PHISHING_TRAINING_FOR_CLICKS ||
          initialData.assignedFor ===
            PhishingAssignedFor.PHISHING_TRAINING_FOR_ALL,
      );
      setCompromisedChecked(
        initialData.assignedFor ===
          PhishingAssignedFor.PHISHING_TRAINING_FOR_COMPROMISES ||
          initialData.assignedFor ===
            PhishingAssignedFor.PHISHING_TRAINING_FOR_ALL,
      );
    }
  }, [initialData?.assignedFor]);

  const campaignName = watch('campaignName');
  const selectedType = watch('campaignType');
  const productPackageId = watch('productPackageId');
  const validityUnit = watch('expireDate.validityUnit');
  const charCount = campaignName?.length || 0;

  const validityPeriodMax =
    validityUnit === CampaignValidityUnit.MONTHS
      ? MAX_EXPIRY_MONTHS
      : MAX_EXPIRY_DAYS;

  useEffect(() => {
    let assignedFor: PhishingAssignedFor;
    if (clickChecked && compromisedChecked) {
      assignedFor = PhishingAssignedFor.PHISHING_TRAINING_FOR_ALL;
    } else if (clickChecked) {
      assignedFor = PhishingAssignedFor.PHISHING_TRAINING_FOR_CLICKS;
    } else {
      assignedFor = PhishingAssignedFor.PHISHING_TRAINING_FOR_COMPROMISES;
    }
    setValue('assignedFor', assignedFor);
  }, [clickChecked, compromisedChecked, setValue]);

  const isOverLimit = charCount > 50;

  const handlePackageChange = useCallback(
    ({
      productPackageId: nextProductPackageId,
    }: {
      productPackageId: string;
    }) => {
      setValue('productPackageId', nextProductPackageId, {
        shouldValidate: Boolean(nextProductPackageId),
      });
    },
    [setValue],
  );

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="mx-auto max-w-2xl">
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-foreground">Campaign Setup</h2>
        <p className="mt-2 text-muted-foreground">
          Enter a name, select the type, and choose how long the simulation
          stays active.
        </p>
      </div>

      {/* Campaign Name */}
      <div className="mb-6">
        <label
          htmlFor="campaignName"
          className="mb-2 block text-sm font-medium text-foreground"
        >
          Campaign Name <span className="text-vibrant-red">*</span>
        </label>
        <Input
          id="campaignName"
          type="text"
          {...register('campaignName')}
          className={`w-full ${
            errors.campaignName || isOverLimit
              ? 'border-vibrant-red bg-vibrant-red/10'
              : 'border-card-border'
          }`}
          placeholder="Enter campaign name"
          maxLength={50}
        />
        <div className="mt-1 flex justify-between">
          <span
            className={`text-sm ${errors.campaignName ? 'text-vibrant-red' : 'text-muted-foreground'}`}
          >
            {errors.campaignName?.message}
          </span>
          <span
            className={`text-sm ${isOverLimit ? 'font-medium text-vibrant-red' : 'text-muted-foreground'}`}
          >
            {charCount}/50
          </span>
        </div>
      </div>

      {/* Campaign Type */}
      <div className="mb-8">
        <label className="mb-3 block text-sm font-medium text-foreground">
          Campaign Type <span className="text-vibrant-red">*</span>
        </label>
        <div className="space-y-3">
          {SMS_CAMPAIGN_TYPE_OPTIONS.map(option => (
            <label
              key={option.value}
              className={`flex cursor-pointer items-start rounded-lg border p-4 transition-colors ${
                selectedType === option.value
                  ? 'border-primary bg-primary/10 ring-2 ring-primary/20'
                  : 'hover: border-card-border hover:border-card-border'
              }`}
            >
              <input
                type="radio"
                value={option.value}
                checked={selectedType === option.value}
                onChange={() => setValue('campaignType', option.value)}
                className="mt-1 size-4 border-card-border text-primary focus:ring-primary"
              />
              <div className="ml-3">
                <span className="block text-sm font-medium text-foreground">
                  {option.label}
                </span>
                <span className="mt-1 block text-sm text-muted-foreground">
                  {option.description}
                </span>
              </div>
              {option.value === CampaignType.SMISHING_WITH_TRAINING && (
                <span className="ml-auto rounded bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
                  Recommended
                </span>
              )}
            </label>
          ))}
        </div>
        {errors.campaignType && (
          <p className="mt-2 text-sm text-vibrant-red">
            {errors.campaignType.message}
          </p>
        )}
        {errors.assignedFor && (
          <p className="mt-2 text-sm text-vibrant-red">
            {errors.assignedFor.message}
          </p>
        )}
      </div>

      <PackageSelectField
        channel={CampaignChannel.SMS}
        productPackageId={productPackageId}
        onChange={handlePackageChange}
        packageError={errors.productPackageId?.message}
      />

      {/* Simulation expiry */}
      <div className="mb-8 grid grid-cols-2 items-start gap-5">
        <div>
          {' '}
          <Label>
            Simulation activity period{' '}
            <span className="text-vibrant-red">*</span>
          </Label>
          <Select
            value={validityUnit}
            onValueChange={value =>
              setValue('expireDate.validityUnit', value as CampaignValidityUnit)
            }
          >
            <SelectTrigger className="w-full">
              <SelectValue placeholder="Select validity unit" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={CampaignValidityUnit.DAYS}>Days</SelectItem>
              <SelectItem value={CampaignValidityUnit.MONTHS}>
                Months
              </SelectItem>
            </SelectContent>
          </Select>
          {errors.expireDate?.validityUnit && (
            <p className="mb-2 text-sm text-vibrant-red">
              {errors.expireDate.validityUnit.message}
            </p>
          )}
        </div>

        <div>
          <Label htmlFor="validityPeriod">
            Period length <span className="text-vibrant-red">*</span>
          </Label>
          <Input
            id="validityPeriod"
            type="number"
            min={1}
            max={validityPeriodMax}
            step={1}
            {...register('expireDate.validityPeriod', {
              valueAsNumber: true,
            })}
            placeholder="Enter period length"
            className={`w-full ${
              errors.expireDate?.validityPeriod
                ? 'border-vibrant-red bg-vibrant-red/10'
                : 'border-card-border'
            }`}
          />
          <p className="mt-1 text-sm text-muted-foreground">
            Between 1 and {validityPeriodMax}{' '}
            {validityUnit === CampaignValidityUnit.MONTHS ? 'months' : 'days'}.
          </p>
          {errors.expireDate?.validityPeriod && (
            <p className="mt-1 text-sm text-vibrant-red">
              {errors.expireDate.validityPeriod.message}
            </p>
          )}
        </div>
      </div>

      {/* Phishing Response Stage & Learning Mode */}
      {selectedType === CampaignType.SMISHING_WITH_TRAINING && (
        <div className="mb-8">
          <Label
            htmlFor="phishingResponseStage"
            className="block text-sm font-medium text-foreground"
          >
            Phishing Response Stage & Learning Mode
          </Label>
          <p className="mb-3 text-xs text-muted-foreground">
            Select at least one response stage and learning mode.
          </p>
          <div className="relative rounded-lg border border-card-border px-3 py-5">
            <span className="absolute right-3 top-3 rounded bg-primary/10 px-2 py-0.5 text-xs font-medium text-primary">
              Recommended
            </span>
            <div className="flex items-center gap-3 space-y-3">
              <div className="flex items-start space-x-2">
                <Checkbox
                  id="clickCheckbox"
                  checked={clickChecked}
                  onCheckedChange={checked => {
                    if (!checked && !compromisedChecked) return;
                    setClickChecked(!!checked);
                  }}
                  className="mt-1"
                />
                <div>
                  <Label htmlFor="clickCheckbox" className="cursor-pointer">
                    Click
                  </Label>
                  <p className="text-xs text-muted-foreground">
                    If a user clicks a campaign link, micro training will be
                    automatically assigned.
                  </p>
                </div>
              </div>

              <div className="flex items-start space-x-2">
                <Checkbox
                  id="compromisedCheckbox"
                  checked={compromisedChecked}
                  onCheckedChange={checked => {
                    if (!checked && !clickChecked) return;
                    setCompromisedChecked(!!checked);
                  }}
                  className="mt-1"
                />
                <div>
                  <Label
                    htmlFor="compromisedCheckbox"
                    className="cursor-pointer"
                  >
                    Compromised
                  </Label>
                  <p className="text-xs text-muted-foreground">
                    If a user submits credentials during a campaign, micro
                    training will be automatically assigned.
                  </p>
                </div>
              </div>
            </div>

            <div className="mt-5">
              <RadioGroup defaultValue="microContentName">
                <div className="flex items-start space-x-2">
                  <RadioGroupItem
                    id="microContentName"
                    value="microContentName"
                    className="mt-1"
                  />
                  <div>
                    <Label
                      htmlFor="microContentName"
                      className="cursor-pointer"
                    >
                      Micro Content
                    </Label>
                    <p className="text-xs text-muted-foreground">
                      Short training module, duration not exceeding 3 minutes.
                    </p>
                  </div>
                </div>
              </RadioGroup>
            </div>
          </div>
        </div>
      )}

      {/* Actions */}
      <div className="flex justify-between border-t border-card-border pt-6">
        {onBack ? (
          <Button variant="outline" type="button" onClick={onBack}>
            Cancel
          </Button>
        ) : (
          <div />
        )}
        <Button
          type="submit"
          variant="default"
          disabled={isLoading || isOverLimit}
        >
          {isLoading ? (
            <>
              <Loader2 className="-ml-1 mr-2 size-4 animate-spin" />
              Saving...
            </>
          ) : (
            <>
              Save & Continue
              <ArrowRightIcon className="ml-2 size-4" />
            </>
          )}
        </Button>
      </div>
    </form>
  );
};
