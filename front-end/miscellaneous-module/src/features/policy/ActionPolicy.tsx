import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
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
import TextEditor from 'common/TextEditor';
import SearchSelect from 'components/SearchSelect';
import { useAPI } from 'hooks/UseAPI';
import { useUploader } from 'hooks/UseUploader';
import { Save, Upload } from 'lucide-react';
import { IDropdownData } from 'models/DropdownData';
import { IList, IResponse, Status } from 'models/Global';
import { IPolicy, IPolicyPayload } from 'models/Policy';
import { Fragment, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { getFileExtension, isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
  selectedPolicy?: IPolicy;
}

const ActionPolicy = ({
  isOpen,
  onClose,
  onSubmit,
  selectedPolicy,
}: IProps) => {
  const apiClient = useAPI();
  const { uploadFile } = useUploader();
  const [formData, setFormData] = useState<IPolicyPayload>({
    policyName: '',
    description: '',
    policyType: '',
    effectiveDate: '',
    status: Status.INACTIVE,
    assignedMSP: '',
    country: '',
    industry: '',
    policyExpiryDate: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [dropdownData, setDropdownData] = useState<IDropdownData>({
    countries: [],
    industries: [],
    policyTypes: [],
  });
  const [file, setFile] = useState<{
    link: string;
    selectedFile: File | null;
    fileType: string;
  }>({
    link: '',
    selectedFile: null,
    fileType: '',
  });
  useEffect(() => {
    if (selectedPolicy) {
      fetchPolicy();
    }
  }, [selectedPolicy]);

  const fetchPolicy = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_POLICY_DETAILS.replace(
          ':id',
          selectedPolicy?.id ?? '',
        ),
      );
      if (isSuccessResponse(response.statusCode)) {
        setFormData({
          policyName: response.data.policyName,
          policyType: String(response.data.policyTypeId ?? ''),
          effectiveDate: response.data.effectiveDate,
          status: response.data.status,
          policyExpiryDate: response.data.policyEndDate,
          description: response.data.description,
          country: String(response.data.countryId ?? ''),
          industry: String(response.data.industryId ?? ''),
          assignedMSP: response.data.assignedMSP,
        });
        setFile({
          link: response.data.files[0]?.fileUrl ?? '',
          selectedFile: null,
          fileType: response.data.files[0]?.fileType ?? '',
        });
      }
    } catch (error) {
      console.error('Error fetching policy:', error);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchDropdownData();
    }
  }, [isOpen]);

  const fetchDropdownData = async () => {
    try {
      const [countryRes, industryRes, policyTypeRes] = await Promise.all<
        [
          IResponse<Array<{ id: string; name: string }>>,

          IResponse<Array<{ id: string; name: string }>>,

          IResponse<IList<{ id: string; name: string }>>,
        ]
      >([
        apiClient.get(API_END_POINTS.GET_ACTIVE_COUNTRY_LIST),
        apiClient.get(API_END_POINTS.GET_INDUSTRIES_LIST),
        apiClient.get(API_END_POINTS.GET_POLICY_TYPE_LIST),
      ]);

      setDropdownData({
        countries: countryRes.data.map(item => ({
          id: String(item.id),
          name: item.name,
        })),
        industries: industryRes.data.map(item => ({
          id: String(item.id),
          name: item.name,
        })),
        policyTypes: policyTypeRes.data.items.map(item => ({
          id: String(item.id),
          name: item.name,
        })),
      });
    } catch (error) {
      console.error('Error fetching dropdown data:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) {
      return;
    }
    setLoading(true);
    let link = '';
    if (file.selectedFile) {
      const { url, error } = await uploadFile(file.selectedFile, 'CONTENT');
      if (error) {
        toast.error(error);
        return;
      }
      link = url;
    }
    const createPayload = {
      policyName: formData.policyName,
      policyTypeId: formData.policyType,
      effectiveDate: formData.effectiveDate,
      status: formData.status,
      policyEndDate: formData.policyExpiryDate,
      description: formData.description,
      files: [
        {
          fileUrl: link,
          fileType: file.fileType,
        },
      ],
      countryId: formData.country,
      industryId: formData.industry,
      companyName: '',
    };

    const updatePayload = {
      ...createPayload,
      files: [
        {
          fileUrl: file.link,
          fileType: file.fileType,
        },
      ],
    };

    const payload = selectedPolicy ? updatePayload : createPayload;

    try {
      const response = await apiClient[selectedPolicy ? 'put' : 'post'](
        selectedPolicy
          ? API_END_POINTS.UPDATE_POLICY.replace(':id', selectedPolicy.id)
          : API_END_POINTS.CREATE_POLICY,
        {
          data: payload,
        },
      );
      if (isSuccessResponse(response.statusCode)) {
        onSubmit();
        toast.success(
          selectedPolicy
            ? 'Policy updated successfully'
            : 'Policy created successfully',
        );
        onClose();
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error creating policy:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (field: string, value: string) => {
    if (field === 'policyName') {
      if (value.length > 150) {
        setErrors(prev => ({
          ...prev,
          policyName: 'Policy name must be less than 150 characters',
        }));
        return;
      }
    }
    setFormData((prev: IPolicyPayload) => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFile({
      link: '',
      selectedFile: e.target.files?.[0] ?? null,
      fileType: getFileExtension(e.target.files?.[0] ?? null),
    });
  };

  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    if (!formData.policyName.trim()) {
      newErrors.policyName = 'Policy name is required';
    }
    if (!formData.policyType.trim()) {
      newErrors.policyType = 'Policy type is required';
    }
    if (!formData.effectiveDate) {
      newErrors.effectiveDate = 'Effective date is required';
    }
    if (!formData.policyExpiryDate) {
      newErrors.policyExpiryDate = 'Policy expiry date is required';
    }
    if (!formData.status.trim()) {
      newErrors.status = 'Status is required';
    }
    if (!formData.country.trim()) {
      newErrors.country = 'Country is required';
    }
    if (!formData.industry.trim()) {
      newErrors.industry = 'Industry is required';
    }
    if (!file.selectedFile && !file.link) {
      newErrors.file = 'File is required';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent
        className="max-h-[90vh] w-2/3 overflow-y-auto"
        allowNestedModals={true}
        nestedModalSelectors={['.tox-tinymce, .tox-tinymce-aux', '.tox']}
        onOpenAutoFocus={e => e.preventDefault()}
        onCloseAutoFocus={e => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>Request Policy Update</DialogTitle>
          <DialogDescription>
            Submit a request to update policy (requires Super Admin approval)
          </DialogDescription>
        </DialogHeader>
        <Card>
          <CardHeader>
            <CardTitle>Policy Details</CardTitle>
            <CardDescription>
              Enter the policy information and configuration
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="policy-name">
                    Policy Name *{' '}
                    <span className="text-xs text-muted-foreground">
                      (Max 150 characters)
                    </span>
                  </Label>
                  <Input
                    id="policy-name"
                    placeholder="Enter policy name"
                    value={formData.policyName}
                    onChange={e =>
                      handleInputChange('policyName', e.target.value)
                    }
                  />
                  <div className="flex justify-between">
                    <p className="text-sm text-red-500">{errors.policyName}</p>

                    <p className="text-sm text-muted-foreground">
                      {formData.policyName.length}/150 characters
                    </p>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="policy-type">Policy Type *</Label>
                  <Select
                    value={formData.policyType}
                    onValueChange={value =>
                      handleInputChange('policyType', value)
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select policy type" />
                    </SelectTrigger>
                    <SelectContent>
                      {dropdownData.policyTypes?.map(policyType => (
                        <SelectItem key={policyType.id} value={policyType.id}>
                          {policyType.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {errors.policyType && (
                    <p className="text-sm text-red-500">{errors.policyType}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="effective-date">Effective Date *</Label>
                  <Input
                    id="effective-date"
                    type="date"
                    value={formData.effectiveDate}
                    onChange={e =>
                      handleInputChange('effectiveDate', e.target.value)
                    }
                    onClick={e => {
                      const input = e.target as HTMLInputElement;
                      input.showPicker();
                    }}
                  />
                  {errors.effectiveDate && (
                    <p className="text-sm text-red-500">
                      {errors.effectiveDate}
                    </p>
                  )}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="policy-expiry-date">
                    Policy Expiry Date *
                  </Label>
                  <Input
                    id="policy-expiry-date"
                    type="date"
                    value={formData.policyExpiryDate}
                    onChange={e =>
                      handleInputChange('policyExpiryDate', e.target.value)
                    }
                    onClick={e => {
                      const input = e.target as HTMLInputElement;
                      input.showPicker();
                    }}
                  />
                  {errors.policyExpiryDate && (
                    <p className="text-sm text-red-500">
                      {errors.policyExpiryDate}
                    </p>
                  )}
                </div>

                {/* <div className="space-y-2">
                  <Label htmlFor="assigned-msp">Assigned MSP (Optional)</Label>
                  <Select
                    value={formData.assignedMSP}
                    onValueChange={value =>
                      handleInputChange('assignedMSP', value)
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select MSP" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="msp1">MSP Provider 1</SelectItem>
                      <SelectItem value="msp2">MSP Provider 2</SelectItem>
                      <SelectItem value="msp3">MSP Provider 3</SelectItem>
                    </SelectContent>
                  </Select>
                </div> */}

                <div className="space-y-2">
                  <Label htmlFor="country">Country *</Label>
                  <SearchSelect
                    value={formData.country}
                    onValueChange={value => handleInputChange('country', value)}
                    placeholder="Select country"
                    items={dropdownData.countries?.map(country => ({
                      value: country.id,
                      label: country.name,
                    })) ?? []}
                    hasError={!!errors.country}
                  />
                  {errors.country && (
                    <p className="text-sm text-red-500">{errors.country}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="industry">Industry *</Label>
                  <SearchSelect
                    value={formData.industry}
                    onValueChange={value =>
                      handleInputChange('industry', value)
                    }
                    placeholder="Select industry"
                    items={dropdownData.industries?.map(item => ({
                      value: item.id,
                      label: item.name,
                    })) ?? []}
                    hasError={!!errors.industry}
                  />
                  {errors.industry && (
                    <p className="text-sm text-red-500">{errors.industry}</p>
                  )}
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="status">Status *</Label>
                <Select
                  value={formData.status}
                  onValueChange={value => handleInputChange('status', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ACTIVE">Active</SelectItem>
                    <SelectItem value="INACTIVE">Inactive</SelectItem>
                  </SelectContent>
                </Select>
                {errors.status && (
                  <p className="text-sm text-red-500">{errors.status}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <TextEditor
                  isDark={true}
                  value={formData.description}
                  onChange={value => handleInputChange('description', value)}
                />
                {errors.description && (
                  <p className="text-sm text-red-500">{errors.description}</p>
                )}
              </div>

              <div>
                <Label htmlFor="file-upload">File Upload *</Label>

                <Fragment>
                  <label htmlFor="file-upload">
                    <div className="cursor-pointer rounded-lg border-2 border-dashed border-card-border p-8 text-center transition-colors hover:border-primary">
                      <Upload className="mx-auto mb-4 size-12 text-muted-foreground" />
                      <p className="text-sm text-muted-foreground">
                        Click to upload or drag and drop
                        <br />
                        PDF, DOC, DOCX files up to 10MB
                      </p>
                      {file.selectedFile || file.link ? (
                        <div className="text-sm text-primary">
                          {file.selectedFile?.name ||
                            FILE_PATH_PREFIX + file.link}
                        </div>
                      ) : (
                        <div className="text-sm text-muted-foreground">
                          No file selected
                        </div>
                      )}
                    </div>

                    <input
                      type="file"
                      accept="application/pdf, application/msword, application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                      className="hidden"
                      id="file-upload"
                      onChange={handleFileChange}
                    />
                  </label>
                  {errors.file && (
                    <p className="text-sm text-destructive">{errors.file}</p>
                  )}
                </Fragment>
                <div>
                  {file.link && (
                    <a
                      href={FILE_PATH_PREFIX + file.link}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-sm text-primary underline hover:text-primary/80"
                    >
                      View File
                    </a>
                  )}
                </div>
              </div>

              <div className="flex justify-end gap-4">
                <Button type="button" variant="outline" onClick={onClose}>
                  Cancel
                </Button>
                <Button type="submit" disabled={loading}>
                  <Save className="mr-2 size-4" />
                  {loading ? 'Submitting...' : 'Submit'}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      </DialogContent>
    </Dialog>
  );
};

export default ActionPolicy;
