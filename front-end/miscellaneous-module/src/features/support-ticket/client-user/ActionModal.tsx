import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from 'common/Button';
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
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import { useAPI } from 'hooks/UseAPI';
import { useUploader } from 'hooks/UseUploader';
import { Trash, Upload } from 'lucide-react';
import { IDropdownOption } from 'models/DropdownData';
import { useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  DefaultUserSupportTicketFormValues,
  UserSupportTicketFormData,
  userSupportTicketSchema,
} from 'schemas/SupportTicketSchema';
import { isSuccessResponse } from 'utils/Helper';
import {
  SUPPORT_TICKET_FILE_ACCEPT,
  selectValidSupportTicketFiles,
  validateSupportTicketFile,
} from 'utils/SupportTicketFileValidation';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
}

const ClientUserActionModal = ({ isOpen, onClose, onSubmit }: IProps) => {
  const {
    control,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm<UserSupportTicketFormData>({
    resolver: zodResolver(userSupportTicketSchema),
    defaultValues: DefaultUserSupportTicketFormValues,
    mode: 'onChange',
  });
  const apiClient = useAPI();
  const { uploadFile } = useUploader();
  const [submitting, setSubmitting] = useState(false);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [userSubPackages, setUserSubPackages] = useState<
    Array<{
      subPackageId: string;
      subPackageName: string;
    }>
  >([]);
  const [supportTypeList, setSupportTypeList] = useState<
    Array<IDropdownOption>
  >([]);

  const getUserSubPackages = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_USER_SUB_PACKAGES,
      );
      if (isSuccessResponse(response.statusCode)) {
        setUserSubPackages(response.data);
      }
    } catch (error) {
      console.error('Error fetching user sub packages:', error);
    }
  };

  const getSupportTypeList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_SUPPORT_ACTIVE_TYPE_LIST,
      );
      const supportTypeList = response.data.map(
        (supportType: IDropdownOption) => ({
          id: supportType.id,
          name: supportType.name,
        }),
      );
      setSupportTypeList(supportTypeList);
    } catch (error) {
      console.error('Error fetching support type list:', error);
    }
  };

  useEffect(() => {
    if (isOpen) {
      getUserSubPackages();
      getSupportTypeList();
    }
  }, []);

  const handleFormSubmit = async (data: UserSupportTicketFormData) => {
    setSubmitting(true);

    try {
      const links: Array<string> = [];
      if (selectedFiles.length > 0) {
        for (const file of selectedFiles) {
          const fileError = await validateSupportTicketFile(file);
          if (fileError) {
            toast.error(`${file.name}: ${fileError}`);
            setSubmitting(false);
            return;
          }

          const { url, error } = await uploadFile(file, 'CONTENT');
          if (error) {
            toast.error(error);
            setSubmitting(false);
            return;
          }
          links.push(url);
        }
      }

      const payload = {
        title: data.title,
        priority: data.priority,
        supportType: data.supportType,
        courseId: data.course,
        description: data.description,
        attachments: links,
      };

      const response = await apiClient.post(
        API_END_POINTS.CREATE_SUPPORT_TICKET,
        {
          data: payload,
        },
      );
      if (isSuccessResponse(response.statusCode)) {
        toast.success('Support ticket created successfully');
        onSubmit();
        handleClose();
      } else {
        toast.error('Failed to create support ticket');
      }
    } catch (error) {
      console.error('Error submitting form:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    reset();
    setSelectedFiles([]);
    onClose();
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    e.target.value = '';
    if (!files?.length) return;

    const { accepted, errors } = await selectValidSupportTicketFiles(
      Array.from(files),
      selectedFiles,
    );

    errors.forEach(error => toast.error(error));
    if (accepted.length > 0) {
      setSelectedFiles(prev => [...prev, ...accepted]);
    }
  };

  const removeFile = (index: number) => {
    setSelectedFiles(prev => prev.filter((_, i) => i !== index));
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent
        className="h-[90vh] w-full overflow-y-auto sm:w-2/3"
        allowNestedModals={true}
        nestedModalSelectors={['.tox-tinymce, .tox-tinymce-aux', '.tox']}
        onOpenAutoFocus={e => e.preventDefault()}
        onCloseAutoFocus={e => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>Create Support Ticket</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-3 sm:space-y-4">
          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
            <div>
              <Label htmlFor="subject">Subject *</Label>
              <Controller
                name="title"
                control={control}
                render={({ field }) => (
                  <Input
                    id="subject"
                    {...field}
                    maxLength={151}
                    placeholder="Enter ticket subject"
                  />
                )}
              />
              {errors.title && (
                <p className="mt-1 text-sm text-red-500">
                  {errors.title.message}
                </p>
              )}
            </div>
            <div>
              <Label htmlFor="supportType">Support Type *</Label>
              <Controller
                name="supportType"
                control={control}
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select support type" />
                    </SelectTrigger>
                    <SelectContent>
                      {supportTypeList.length > 0 ? (
                        supportTypeList.map(type => (
                          <SelectItem key={type.id} value={type.id}>
                            {type.name}
                          </SelectItem>
                        ))
                      ) : (
                        <SelectItem disabled value="No support types found">
                          No support types found
                        </SelectItem>
                      )}
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.supportType && (
                <p className="mt-1 text-sm text-red-500">
                  {errors.supportType.message}
                </p>
              )}
            </div>

            <div>
              <Label htmlFor="course">Course *</Label>
              <Controller
                name="course"
                control={control}
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select course" />
                    </SelectTrigger>
                    <SelectContent>
                      {userSubPackages.length > 0 ? (
                        userSubPackages.map(subPackage => (
                          <SelectItem
                            key={subPackage.subPackageId}
                            value={subPackage.subPackageId}
                          >
                            {subPackage.subPackageName}
                          </SelectItem>
                        ))
                      ) : (
                        <SelectItem disabled value="No sub packages found">
                          No sub packages found
                        </SelectItem>
                      )}
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.course && (
                <p className="mt-1 text-sm text-red-500">
                  {errors.course.message}
                </p>
              )}
            </div>
            <div>
              <Label htmlFor="priority">Priority *</Label>
              <Controller
                name="priority"
                control={control}
                render={({ field }) => (
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select priority" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="HIGH">High</SelectItem>
                      <SelectItem value="MEDIUM">Medium</SelectItem>
                      <SelectItem value="LOW">Low</SelectItem>
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.priority && (
                <p className="mt-1 text-sm text-red-500">
                  {errors.priority.message}
                </p>
              )}
            </div>
          </div>

          <div>
            <Label htmlFor="description">Description *</Label>
            <Controller
              name="description"
              control={control}
              render={({ field }) => (
                <TextEditor
                  isDark={true}
                  value={field.value || ''}
                  onChange={field.onChange}
                />
              )}
            />
            {errors.description && (
              <p className="mt-1 text-sm text-red-500">
                {errors.description.message}
              </p>
            )}
          </div>

          <div>
            <Label htmlFor="attachments">Attachments</Label>
            <p className="mb-2 text-xs text-gray-500">
              Allowed: PDF, JPG, PNG, DOC, DOCX | Max 2 MB per file | Total 10
              MB
              {selectedFiles.length > 0 && (
                <span className="ml-2 font-medium">
                  (Current:{' '}
                  {(
                    selectedFiles.reduce((sum, file) => sum + file.size, 0) /
                    (1024 * 1024)
                  ).toFixed(2)}{' '}
                  MB)
                </span>
              )}
            </p>
            <div className="flex items-center gap-2">
              <Input
                id="attachments"
                type="file"
                accept={SUPPORT_TICKET_FILE_ACCEPT}
                multiple
                onChange={handleFileUpload}
                className="hidden"
              />
              <Button
                type="button"
                variant="outline"
                onClick={() => document.getElementById('attachments')?.click()}
              >
                <Upload className="mr-2 size-4" />
                Upload Files
              </Button>
            </div>
            {selectedFiles.length > 0 && (
              <div className="mt-3 space-y-2">
                {selectedFiles.map((file, index) => (
                  <div
                    key={`${file.name}-${index}`}
                    className="flex items-center justify-between rounded border border-card-border p-2"
                  >
                    <div className="flex min-w-0 flex-1 items-center gap-2">
                      <span className="truncate text-sm text-primary">
                        {file.name}
                      </span>
                      <span className="whitespace-nowrap text-xs text-card-foreground">
                        (
                        {file.size >= 1024 * 1024
                          ? `${(file.size / (1024 * 1024)).toFixed(2)} MB`
                          : `${(file.size / 1024).toFixed(2)} KB`}
                        )
                      </span>
                    </div>
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => removeFile(index)}
                      className="ml-2"
                    >
                      <Trash className="size-4 text-red-500" />
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={handleClose}>
              Cancel
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Ticket'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ClientUserActionModal;
