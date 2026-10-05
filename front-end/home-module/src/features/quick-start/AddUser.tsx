import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
import { Card, CardContent, CardHeader } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import CustomPhoneInput from 'components/CustomPhoneInput';
import SearchSelect from 'components/SearchSelect';
import useAPI from 'hooks/UseAPI';
import useStore from 'hooks/UseStore';
import {
  Download,
  Loader2,
  Upload,
  UserPlus,
  FileSpreadsheet,
} from 'lucide-react';
import { Status } from 'models/Global';
import { ChangeEvent, useEffect, useMemo, useRef, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { BULK_IMPORT_TEMPLATE_URL } from 'utils/Constants';
import {
  blockEmailSpaceKey,
  cn,
  isSuccessResponse,
  sanitizeEmailInput,
} from 'utils/Helper';
import { object, string, z } from 'zod';

interface IProps {
  stepId: number;
  updateStepsStatus: (
    stepId: number,
    status: 'complete' | 'incomplete',
  ) => void;
}

const FIRST_NAME_MAX_LENGTH = 50;
const LAST_NAME_MAX_LENGTH = 50;
const EMAIL_MAX_LENGTH = 254;

const firstNameRegex = /^[A-Za-z\s'.-]+$/;
const lastNameRegex = /^[A-Za-z\s'-]+$/;

const InvalidDomains = [
  'gmail.com',
  'googlemail.com',
  'yahoo.com',
  'yahoo.co.uk',
  'yahoo.co.in',
  'outlook.com',
  'hotmail.com',
  'live.com',
  'msn.com',
  'icloud.com',
  'me.com',
  'mac.com',
  'aol.com',
  'zoho.com',
  'zoho.eu',
  'protonmail.com',
  'tutanota.com',
  'tutamail.com',
  'tutanota.de',
  'gmx.com',
  'gmx.net',
  'mail.com',
  'fastmail.com',
  'yandex.com',
  'yandex.ru',
  'rediffmail.com',
  'qq.com',
  'foxmail.com',
  '163.com',
  '126.com',
  'yeah.net',
  'sina.com',
  'sina.cn',
  'naver.com',
  'kakao.com',
  'daum.net',
  'orange.fr',
  'wanadoo.fr',
  'laposte.net',
  'free.fr',
  't-online.de',
  'swisscows.com',
  'posteo.de',
  'runbox.com',
  'disroot.org',
  'riseup.net',
  'kolab.org',
  'openmailbox.org',
  'seznam.cz',
  'libero.it',
  'virgilio.it',
  'tiscali.it',
  'aruba.it',
  'biglobe.ne.jp',
  'excite.com',
  'earthlink.net',
  'lycos.com',
  'inbox.lv',
  'uk2.net',
  'sapo.pt',
  'telia.com',
  'telia.se',
  'telstra.com',
  'optus.com.au',
  'btinternet.com',
  'sky.com',
  'virginmedia.com',
  'cox.net',
  'comcast.net',
  'xfinity.com',
  'verizon.net',
  'tempmail.com',
  '10minutemail.com',
  'mailinator.com',
  'guerrillamail.com',
  'throwawaymail.com',
  'getnada.com',
  'fakemail.net',
  'minutemail.net',
  'emailondeck.com',
  'burnermail.io',
  'temp-mail.org',
  'tempinbox.com',
  'mohmal.com',
  'anonaddy.com',
  'simplelogin.io',
  'spamgourmet.com',
  'maildrop.cc',
  'sharklasers.com',
  'trashmail.com',
  'mailcatch.com',
  'dispostable.com',
  'mytemp.email',
  'tempail.com',
  'jetable.org',
  'haribumail.com',
  'mailtemp.info',
  'tempemailaddress.com',
  'guerrillamailblock.com',
  'dropmail.me',
  'inboxkitten.com',
  'tempbox.net',
  'mailnull.com',
  'spambox.us',
  'fakeinbox.com',
  'disposableinbox.com',
  'quickemailverification.com',
  'mailnesia.com',
];

const userSchema = object({
  firstName: string()
    .trim()
    .min(1, 'First name is required.')
    .min(3, 'First name must be at least 3 characters long.')
    .max(FIRST_NAME_MAX_LENGTH, 'First name cannot exceed 50 characters.')
    .regex(
      firstNameRegex,
      'First name can contain letters, spaces, hyphens, and apostrophes only.',
    ),
  lastName: string()
    .trim()
    .min(1, 'Last name is required.')
    .min(3, 'Last name must be at least 3 characters long.')
    .max(LAST_NAME_MAX_LENGTH, 'Last name cannot exceed 50 characters.')
    .regex(
      lastNameRegex,
      'Last name can contain letters, spaces, hyphens, and apostrophes only.',
    ),
  email: string()
    .trim()
    .min(1, 'Email address is required.')
    .max(EMAIL_MAX_LENGTH, 'Email address cannot exceed 254 characters.')
    .pipe(z.email('Please enter a valid email address.'))
    .transform(value => value.toLowerCase())
    .refine(
      email => {
        const domain = email.split('@')[1];
        return !InvalidDomains.includes(domain);
      },
      {
        message: 'Please use a corporate or business email address.',
      },
    ),
  phoneNumber: string()
    .trim()
    .min(1, 'Phone number is required.')
    .refine(
      phone => /^\+?[0-9]+$/.test(phone),
      'Please enter a valid phone number.',
    )
    .refine(phone => {
      const digitsLength = phone.replace(/\D/g, '').length;
      return digitsLength >= 7 && digitsLength <= 15;
    }, 'Please enter a valid phone number.'),
  department: string().min(1, 'Please select a department.'),
  status: z.enum(Status),
});

type UserFormData = z.infer<typeof userSchema>;

const options = [
  {
    id: 0,
    title: 'Add a Single User',
    description: 'Manually enter user details',
    icon: UserPlus,
  },
  {
    id: 1,
    title: 'Import Users via CSV/Excel',
    description: 'Bulk import from a file',
    icon: FileSpreadsheet,
  },
];

const AddUser = ({ stepId, updateStepsStatus }: IProps) => {
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);
  const [selectedOption, setSelectedOption] = useState<number>(0);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [departmentList, setDepartmentList] = useState<
    Array<{ id: string; name: string }>
  >([]);
  const fileUploaderRef = useRef<HTMLInputElement>(null);

  const apiClient = useAPI();
  const { userInfo } = useStore();

  useEffect(() => {
    fetchDepartmentList();
  }, []);

  const fetchDepartmentList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_DEPARTMENT_LIST +
          'active=true&isSystemDefined=true&pageSize=1000',
      );
      if (isSuccessResponse(response.statusCode)) {
        setDepartmentList(
          response.data.items.map(
            (department: { id: string; name: string }) => ({
              id: department.name,
              name: department.name,
            }),
          ),
        );
      }
    } catch (error) {
      console.error('Error fetching department list:', error);
    }
  };

  const {
    control,
    handleSubmit,
    clearErrors,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<UserFormData>({
    resolver: zodResolver(userSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: '',
      department: '',
      status: Status.ACTIVE,
    },
    mode: 'onChange',
  });

  const departmentOptions = useMemo(
    () =>
      departmentList.map(dept => ({
        value: dept.name,
        label: dept.name,
      })),
    [departmentList],
  );

  const onFormSubmit = async (data: UserFormData) => {
    if (!userInfo) return;

    const phoneNumber = data.phoneNumber.startsWith('+')
      ? data.phoneNumber
      : '+' + data.phoneNumber;

    const createPayload = {
      firstName: data.firstName.trim(),
      lastName: data.lastName.trim(),
      email: data.email.trim(),
      phoneNumber: phoneNumber.trim(),
      department: data.department,
      status: data.status,
      clientAdminId: userInfo.userId,
    };

    try {
      const response = await apiClient.post(API_END_POINTS.ADD_END_USER, {
        data: createPayload,
      });

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message || 'Something went wrong');
      }

      toast.success(response.message || 'User added successfully');
      updateStepsStatus(stepId, 'complete');
    } catch (error) {
      toast.error((error as Error).message || 'Something went wrong');
      console.error(error);
    }
  };

  const handleFileUpload = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setUploadedFile(file);
    }
  };

  const handleImportUser = async () => {
    if (!userInfo) return;

    if (!uploadedFile) {
      return toast.error('Import file required');
    }

    try {
      setSubmitting(true);

      const formData = new FormData();
      formData.append('file', uploadedFile);

      const response = await apiClient.post(
        API_END_POINTS.IMPORT_END_USER + userInfo.userId,
        {
          data: formData,
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        },
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.data.message || 'Import failed');
      }

      toast.success(response.message || 'Users imported successfully');
      updateStepsStatus(stepId, 'complete');
      if (fileUploaderRef.current) {
        fileUploaderRef.current.value = '';
      }
    } catch (error: unknown) {
      if (error instanceof Error) {
        toast.error(error.message);
      } else {
        toast.error('Import failed');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleDownloadTemplate = () => {
    setLoading(true);

    const link = document.createElement('a');
    link.href = BULK_IMPORT_TEMPLATE_URL;
    link.download = 'Bulk_user_import_template.csv';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    setTimeout(() => {
      setLoading(false);
    }, 1000);
  };

  return (
    <Card>
      <CardHeader>
        <div className="home-flex home-gap-x-4">
          {options.map(option => (
            <div
              key={option.id}
              className={cn(
                'home-group home-relative home-flex home-flex-1 home-cursor-pointer home-items-center home-gap-x-4 home-rounded-xl home-border home-p-5 home-transition-all home-duration-200',
                option.id === selectedOption
                  ? 'home-border-primary home-bg-primary/20'
                  : 'home-border-card-border hover:home-border-primary/50 hover:home-bg-muted/50',
              )}
              onClick={() => setSelectedOption(option.id)}
            >
              <div
                className={cn(
                  'home-flex home-size-12 home-shrink-0 home-items-center home-justify-center home-rounded-full home-transition-colors home-duration-200',
                  option.id === selectedOption
                    ? 'home-bg-primary home-text-primary-foreground'
                    : 'home-bg-muted home-text-muted-foreground group-hover:home-bg-primary/10 group-hover:home-text-primary',
                )}
              >
                <option.icon className="home-size-6" />
              </div>

              <div className="home-flex home-flex-1 home-flex-col home-gap-y-1">
                <p
                  className={cn(
                    'home-text-sm home-font-semibold home-transition-colors',
                    option.id === selectedOption
                      ? 'home-text-foreground'
                      : 'home-text-muted-foreground group-hover:home-text-foreground',
                  )}
                >
                  {option.title}
                </p>
                <span className="home-text-xs home-text-muted-foreground">
                  {option.description}
                </span>
              </div>

              {option.id === selectedOption && (
                <div className="home-size-5 home-rounded-full home-bg-primary"></div>
              )}
            </div>
          ))}
        </div>
      </CardHeader>
      <CardContent className="home-space-y-4">
        {selectedOption === 0 ? (
          <div className="home-flex home-flex-col home-gap-2">
            <div className="home-space-y-2">
              <Label htmlFor="firstName">First Name *</Label>
              <Controller
                name="firstName"
                control={control}
                render={({ field: { onChange, value } }) => (
                  <Input
                    id="firstName"
                    value={value}
                    placeholder="Enter First Name"
                    maxLength={FIRST_NAME_MAX_LENGTH}
                    onChange={e => {
                      onChange(e.target.value);
                      if (errors.firstName?.type === 'maxLength') {
                        clearErrors('firstName');
                      }
                    }}
                    onKeyDown={e => {
                      if (
                        value.length >= FIRST_NAME_MAX_LENGTH &&
                        e.key.length === 1 &&
                        !e.ctrlKey &&
                        !e.metaKey &&
                        !e.altKey
                      ) {
                        setError('firstName', {
                          type: 'maxLength',
                          message: 'First name cannot exceed 50 characters.',
                        });
                      }
                    }}
                  />
                )}
              />
              {errors.firstName && (
                <p className="home-text-xs home-text-red-500">
                  {errors.firstName.message}
                </p>
              )}
            </div>

            <div className="home-space-y-2">
              <Label htmlFor="lastName">Last Name *</Label>
              <Controller
                name="lastName"
                control={control}
                render={({ field: { onChange, value } }) => (
                  <Input
                    id="lastName"
                    value={value}
                    placeholder="Enter Last Name"
                    maxLength={LAST_NAME_MAX_LENGTH}
                    onChange={e => {
                      onChange(e.target.value);
                      if (errors.lastName?.type === 'maxLength') {
                        clearErrors('lastName');
                      }
                    }}
                    onKeyDown={e => {
                      if (
                        value.length >= LAST_NAME_MAX_LENGTH &&
                        e.key.length === 1 &&
                        !e.ctrlKey &&
                        !e.metaKey &&
                        !e.altKey
                      ) {
                        setError('lastName', {
                          type: 'maxLength',
                          message: 'Last name cannot exceed 50 characters.',
                        });
                      }
                    }}
                  />
                )}
              />
              {errors.lastName && (
                <p className="home-text-xs home-text-red-500">
                  {errors.lastName.message}
                </p>
              )}
            </div>

            <div className="home-space-y-2">
              <Label htmlFor="email">Email *</Label>
              <Controller
                name="email"
                control={control}
                render={({ field: { onChange, value, ...field } }) => (
                  <Input
                    {...field}
                    value={value}
                    id="email"
                    type="email"
                    placeholder="Enter Email Address"
                    maxLength={EMAIL_MAX_LENGTH}
                    onChange={e => {
                      onChange(sanitizeEmailInput(e.target.value));
                      if (errors.email?.type === 'maxLength') {
                        clearErrors('email');
                      }
                    }}
                    onBlur={e => onChange(e.target.value.trim())}
                    onKeyDown={e => {
                      blockEmailSpaceKey(e);

                      if (
                        value.length >= EMAIL_MAX_LENGTH &&
                        e.key.length === 1 &&
                        !e.ctrlKey &&
                        !e.metaKey &&
                        !e.altKey
                      ) {
                        setError('email', {
                          type: 'maxLength',
                          message: 'Email cannot exceed 254 characters.',
                        });
                      }
                    }}
                  />
                )}
              />
              {errors.email && (
                <p className="home-text-xs home-text-red-500">
                  {errors.email.message}
                </p>
              )}
            </div>

            <div className="home-space-y-2">
              <Label htmlFor="phone">Phone Number *</Label>
              <Controller
                name="phoneNumber"
                control={control}
                render={({ field }) => (
                  <CustomPhoneInput
                    inputProps={{
                      id: 'phoneNumber',
                      maxLength: 17,
                      placeholder: 'Enter Phone Number',
                    }}
                    value={field.value}
                    handleChange={field.onChange}
                  />
                )}
              />
              {errors.phoneNumber && (
                <p className="home-text-xs home-text-red-500">
                  {errors.phoneNumber.message}
                </p>
              )}
            </div>

            <div className="home-space-y-2">
              <Label htmlFor="department">Department *</Label>
              <Controller
                name="department"
                control={control}
                render={({ field }) => (
                  <SearchSelect
                    value={field.value || undefined}
                    onValueChange={field.onChange}
                    placeholder="Select department"
                    items={departmentOptions}
                    hasError={!!errors.department}
                  />
                )}
              />
              {errors.department && (
                <p className="home-text-xs home-text-red-500">
                  {errors.department.message}
                </p>
              )}
            </div>

            <Button
              type="button"
              onClick={handleSubmit(onFormSubmit)}
              disabled={isSubmitting}
              className="home-col-span-2 home-mt-6 home-w-full"
            >
              {isSubmitting ? 'Submitting...' : 'Add User'}
            </Button>
          </div>
        ) : (
          <div className="home-space-y-4">
            <div className="home-flex home-items-center home-justify-between">
              <Label htmlFor="file-upload">Upload CSV/Excel File *</Label>
              <Button
                variant="outline"
                onClick={handleDownloadTemplate}
                disabled={loading}
              >
                {loading ? (
                  <Loader2 className="home-mr-2 home-size-4 home-animate-spin" />
                ) : (
                  <Download className="home-mr-2 home-size-4" />
                )}
                Download Template
              </Button>
            </div>
            <div className="home-relative home-rounded-lg home-border-2 home-border-dashed home-border-card-border home-p-6 home-text-center">
              <div className="home-space-y-4">
                <Upload className="home-mx-auto home-size-12 home-text-muted-foreground" />
                <div>
                  <p className="home-mb-2 home-text-sm home-text-muted-foreground">
                    Drag and drop your file here, or click to browse
                  </p>
                  <Input
                    id="file-upload"
                    ref={fileUploaderRef}
                    type="file"
                    accept=".csv"
                    onChange={handleFileUpload}
                    className="home-absolute home-inset-0 home-opacity-0"
                  />
                  <Button
                    variant="outline"
                    onClick={() => fileUploaderRef.current?.click()}
                  >
                    Choose File
                  </Button>
                </div>
                {uploadedFile && (
                  <div className="home-text-sm home-text-foreground">
                    Selected: {uploadedFile.name}
                  </div>
                )}
              </div>
            </div>

            <Button
              className="home-w-full"
              onClick={handleImportUser}
              disabled={submitting}
            >
              Upload
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default AddUser;
