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
import { useStore } from 'hooks/UseStore';
import { useUploader } from 'hooks/UseUploader';
import { Trash, Upload } from 'lucide-react';
import { IDropdownOption } from 'models/DropdownData';
import { IMspPackage, IMspProduct } from 'models/MspProduct';
import { ISupportTicketList } from 'models/SupportTicket';
import { useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  ClientAdminSupportTicketFormData,
  clientAdminSupportTicketSchema,
  DefaultClientAdminSupportTicketFormValues,
} from 'schemas/SupportTicketSchema';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: () => void;
  ticket: ISupportTicketList;
}

const MSPAdminForwardModal = ({
  isOpen,
  onClose,
  onSubmit,
  ticket,
}: IProps) => {
  const {
    control,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm<ClientAdminSupportTicketFormData>({
    resolver: zodResolver(clientAdminSupportTicketSchema),
    defaultValues: DefaultClientAdminSupportTicketFormValues,
    mode: 'onChange',
  });
  const { userInfo } = useStore();
  const apiClient = useAPI();
  const { uploadFile } = useUploader();
  const [submitting, setSubmitting] = useState(false);
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [productList, setProductList] = useState<Array<IMspProduct>>([]);
  const [packageList, setPackageList] = useState<Array<IMspPackage>>([]);
  const [selectedProductId, setSelectedProductId] = useState<string>('');
  const [supportTypeList, setSupportTypeList] = useState<
    Array<IDropdownOption>
  >([]);

  const getProductList = async () => {
    setProductList([]);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_MSP_PRODUCT_LIST.replace(
          ':id',
          userInfo?.userId || '',
        ),
      );
      if (isSuccessResponse(response.statusCode)) {
        setProductList(response.data);
      }
    } catch (error) {
      console.error('Error fetching MSP products:', error);
    }
  };

  const getPackageList = async () => {
    setPackageList([]);
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_MSP_PACKAGE_LIST.replace(':id', selectedProductId),
      );
      if (isSuccessResponse(response.statusCode)) {
        setPackageList(response.data);
      }
    } catch (error) {
      console.error('Error fetching MSP package list:', error);
    }
  };

  const getSupportTypeList = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_SUPPORT_ACTIVE_TYPE_LIST,
      );
      // if (isSuccessResponse(response.statusCode)) {
      //   setSupportTypeList(response.data);
      // }
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
    if (isOpen && selectedProductId) {
      getPackageList();
    }
  }, [isOpen, selectedProductId]);

  useEffect(() => {
    if (isOpen) {
      getProductList();
      getSupportTypeList();
    }
  }, [isOpen]);

  const handleFormSubmit = async (data: ClientAdminSupportTicketFormData) => {
    setSubmitting(true);

    try {
      const links: Array<string> = [];
      if (selectedFiles.length > 0) {
        for (const file of selectedFiles) {
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
        parentTicketId: ticket.id,
        priority: data.priority,
        supportType: data.supportType,
        productId: data.product,
        packageId: data.package,
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
        toast.error(
          response.data.data.error || 'Failed to create support ticket',
        );
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
    setSelectedProductId('');
    onClose();
  };

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files) {
      const fileArray = Array.from(files);
      const maxFileSize = 2 * 1024 * 1024; // 2 MB in bytes
      const maxTotalSize = 10 * 1024 * 1024; // 10 MB in bytes

      const invalidFiles = fileArray.filter(file => file.size > maxFileSize);
      const validFiles = fileArray.filter(file => file.size <= maxFileSize);

      if (invalidFiles.length > 0) {
        const fileNames = invalidFiles.map(f => f.name).join(', ');
        toast.error(`The following files exceed 2 MB: ${fileNames}`);
      }

      if (validFiles.length > 0) {
        const currentTotalSize = selectedFiles.reduce(
          (sum, file) => sum + file.size,
          0,
        );
        const newFilesSize = validFiles.reduce(
          (sum, file) => sum + file.size,
          0,
        );
        const totalSize = currentTotalSize + newFilesSize;

        if (totalSize > maxTotalSize) {
          toast.error(
            `Total file size cannot exceed 10 MB. Current total: ${(currentTotalSize / (1024 * 1024)).toFixed(2)} MB`,
          );
        } else {
          setSelectedFiles(prev => [...prev, ...validFiles]);
        }
      }
    }

    // Reset input value to allow uploading the same file again
    e.target.value = '';
  };

  const removeFile = (index: number) => {
    setSelectedFiles(prev => prev.filter((_, i) => i !== index));
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent
        className="h-[90vh] w-2/3 overflow-y-auto"
        allowNestedModals={true}
        nestedModalSelectors={['.tox-tinymce, .tox-tinymce-aux', '.tox']}
        onOpenAutoFocus={e => e.preventDefault()}
        onCloseAutoFocus={e => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>Forward {ticket.ticketId} to MSP</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
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
              <Label htmlFor="product">Product *</Label>
              <Controller
                name="product"
                control={control}
                render={({ field }) => (
                  <Select
                    value={field.value}
                    onValueChange={value => {
                      field.onChange(value);
                      setSelectedProductId(value);
                    }}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select product" />
                    </SelectTrigger>
                    <SelectContent>
                      {productList.length > 0 ? (
                        productList.map(product => (
                          <SelectItem key={product.id} value={product.id}>
                            {product.productName}
                          </SelectItem>
                        ))
                      ) : (
                        <SelectItem disabled value="No products found">
                          No products found
                        </SelectItem>
                      )}
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.product && (
                <p className="mt-1 text-sm text-red-500">
                  {errors.product.message}
                </p>
              )}
            </div>
            <div>
              <Label htmlFor="package">Package *</Label>
              <Controller
                name="package"
                control={control}
                render={({ field }) => (
                  <Select
                    value={field.value}
                    onValueChange={field.onChange}
                    disabled={
                      selectedProductId === '' || packageList.length === 0
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select package" />
                    </SelectTrigger>
                    <SelectContent>
                      {packageList.length > 0 ? (
                        packageList.map(pkg => (
                          <SelectItem key={pkg.id} value={pkg.id}>
                            {pkg.name}
                          </SelectItem>
                        ))
                      ) : (
                        <SelectItem disabled value="No packages found">
                          No packages found
                        </SelectItem>
                      )}
                    </SelectContent>
                  </Select>
                )}
              />
              {errors.package && (
                <p className="mt-1 text-sm text-red-500">
                  {errors.package.message}
                </p>
              )}
            </div>
            <div className="col-span-2">
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
              Maximum file size: 2 MB per file | Total limit: 10 MB
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
                    className="flex items-center justify-between rounded border bg-gray-50 p-2"
                  >
                    <div className="flex min-w-0 flex-1 items-center gap-2">
                      <span className="truncate text-sm text-gray-600">
                        {file.name}
                      </span>
                      <span className="whitespace-nowrap text-xs text-gray-400">
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

export default MSPAdminForwardModal;
