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
import { validateThumbnailFile } from 'features/leader-board/leaderboardValidation';
import { useAPI } from 'hooks/UseAPI';
import { useUploader } from 'hooks/UseUploader';
import { Upload, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  hasLatestNewsFormErrors,
  LATEST_NEWS_FIELD_MAX_LENGTH,
  TLatestNewsFormErrors,
  validateLatestNewsForm,
} from 'schemas/LatestNewsSchema';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  id?: string;
  onSubmit: () => void;
}

const ActionLatestNews = ({ isOpen, onClose, id, onSubmit }: IProps) => {
  const apiClient = useAPI();
  const { uploadFile } = useUploader();
  const [formData, setFormData] = useState({
    title: '',
    slug: '',
    content: '',
    category: '',
    publishDate: '',
    expiryDate: '',
    status: 'DRAFT',
  });
  const [categories, setCategories] = useState<any[]>([]);
  const [thumbnailImage, setThumbnailImage] = useState<{
    link: string;
    selectedFile: File | null;
  }>({
    link: '',
    selectedFile: null,
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<TLatestNewsFormErrors>({});

  useEffect(() => {
    if (id) {
      fetchNews();
    }
  }, [id]);

  const fetchNews = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_LATEST_NEWS_DETAILS.replace(':id', id ?? ''),
      );
      if (isSuccessResponse(response.statusCode)) {
        setFormData({
          title: response.data.name,
          slug: response.data.slug,
          content: response.data.content,
          category: response.data.category.id ?? '',
          publishDate: response.data.publishedDate,
          expiryDate: response.data.expireDate,
          status: response.data.status,
        });
        setThumbnailImage({
          link: response.data.imageUrl,
          selectedFile: null as File | null,
        });
      }
    } catch {
      toast.error('Failed to load news post details.');
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchCategories();
    }
  }, [isOpen]);

  const fetchCategories = async () => {
    const response = await apiClient.get(
      API_END_POINTS.GET_LATEST_NEWS_CATEGORY_LIST,
    );
    if (isSuccessResponse(response.statusCode)) {
      setCategories(response.data);
    }
  };

  const clearFieldError = (field: keyof TLatestNewsFormErrors) => {
    setErrors(prev => ({ ...prev, [field]: undefined }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const validationErrors = validateLatestNewsForm(
      { ...formData, status: formData.status as 'DRAFT' | 'ACTIVE' | 'INACTIVE' },
      {
        thumbnailFile: thumbnailImage.selectedFile,
        hasExistingThumbnail: Boolean(thumbnailImage.link),
      },
    );

    if (hasLatestNewsFormErrors(validationErrors)) {
      setErrors(validationErrors);
      return;
    }

    let thumbnailLink = thumbnailImage.link;
    if (thumbnailImage.selectedFile) {
      const { url, error } = await uploadFile(
        thumbnailImage.selectedFile,
        'THUMBNAIL',
      );
      if (error) {
        setErrors(prev => ({ ...prev, thumbnailImage: error }));
        return;
      }
      thumbnailLink = url;
    }

    const payload = {
      name: formData.title,
      slug: formData.slug,
      categoryId: formData.category,
      content: formData.content,
      sequence: 0,
      imageUrl: thumbnailLink,
      videoUrl: '',
      publishedDate: formData.publishDate,
      expireDate: formData.expiryDate,
      status: formData.status,
    };

    try {
      setLoading(true);
      const response = await apiClient[id ? 'put' : 'post'](
        id
          ? API_END_POINTS.UPDATE_LATEST_NEWS.replace(':id', id)
          : API_END_POINTS.CREATE_LATEST_NEWS,
        {
          data: payload,
        },
      );
      if (isSuccessResponse(response.statusCode)) {
        toast.success(
          id
            ? 'Latest news has been successfully updated.'
            : 'Latest news has been successfully created.',
        );
        onClose();
        onSubmit();
      } else {
        toast.error(response.message || 'Failed to save news post.');
      }
    } catch {
      toast.error(
        id
          ? 'An error occurred while updating latest news.'
          : 'An error occurred while creating latest news.',
      );
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: string, value: string) => {
    if (field === 'title') {
      if (value.length > LATEST_NEWS_FIELD_MAX_LENGTH.title) {
        setErrors(prev => ({
          ...prev,
          title: 'Title cannot exceed 100 characters.',
        }));
        return;
      }
    }

    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));

    if (field in errors) {
      clearFieldError(field as keyof TLatestNewsFormErrors);
    }
  };

  const handleSlugBlur = () => {
    const sanitized = formData.slug
      .toLowerCase()
      .trim()
      .replace(/[^a-z0-9-]+/g, '-')
      .replace(/(^-|-$)/g, '');

    if (sanitized !== formData.slug) {
      setFormData(prev => ({ ...prev, slug: sanitized }));
    }
  };

  const applyThumbnailFile = (file: File): boolean => {
    const thumbnailError = validateThumbnailFile(file);
    if (thumbnailError) {
      setErrors(prev => ({ ...prev, thumbnailImage: thumbnailError }));
      return false;
    }

    setErrors(prev => ({ ...prev, thumbnailImage: undefined }));
    setThumbnailImage({ link: '', selectedFile: file });
    return true;
  };

  const handleThumbnailFileChange = (
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const file = e.target.files?.[0];
    if (file) {
      if (!applyThumbnailFile(file)) {
        e.target.value = '';
      }
    }
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    const file = e.dataTransfer.files?.[0];
    if (file) {
      if (applyThumbnailFile(file)) {
        const fileInput = document.getElementById(
          'thumbnailImage',
        ) as HTMLInputElement;
        if (fileInput) {
          fileInput.value = '';
        }
      }
    }
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleRemoveThumbnail = () => {
    setThumbnailImage({ link: '', selectedFile: null });
    clearFieldError('thumbnailImage');
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent
        className="max-h-[90vh] max-w-[65%] overflow-auto"
        allowNestedModals={true}
        nestedModalSelectors={['.tox-tinymce, .tox-tinymce-aux', '.tox']}
        onOpenAutoFocus={e => e.preventDefault()}
        onCloseAutoFocus={e => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>
            {id ? 'Edit News Post' : 'Create News Post'}
          </DialogTitle>
          <DialogDescription>
            {id
              ? 'Edit the details of your news post'
              : 'Create a new news post for your platform'}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Post Details</CardTitle>
                <CardDescription>
                  Enter the details for your news post
                </CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="space-y-2">
                  <Label htmlFor="title">Title *</Label>
                  <Input
                    id="title"
                    value={formData.title}
                    onChange={e => handleChange('title', e.target.value)}
                    placeholder="e.g., New Course Available"
                    className={errors.title ? 'has-error' : ''}
                  />
                  <div className="flex items-center justify-between">
                    <p className="text-xs text-muted-foreground">
                      5–100 characters
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {formData.title.length}/{LATEST_NEWS_FIELD_MAX_LENGTH.title}{' '}
                      characters
                    </p>
                  </div>
                  {errors.title && (
                    <p className="text-xs text-destructive">{errors.title}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="slug">URL *</Label>
                  <Input
                    id="slug"
                    value={formData.slug}
                    onChange={e => handleChange('slug', e.target.value)}
                    onBlur={handleSlugBlur}
                    placeholder="auto-generated-from-title"
                    className={errors.slug ? 'has-error' : ''}
                  />
                  <p className="text-sm text-muted-foreground">
                    Auto-generated from title or enter custom URL slug
                  </p>
                  {errors.slug && (
                    <p className="text-xs text-destructive">{errors.slug}</p>
                  )}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="content">Content *</Label>
                  <TextEditor
                    isDark={true}
                    value={formData.content}
                    onChange={value => handleChange('content', value)}
                  />
                  {errors.content && (
                    <p className="text-xs text-destructive">{errors.content}</p>
                  )}
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="category">Category *</Label>
                    <Select
                      value={formData.category}
                      onValueChange={value => handleChange('category', value)}
                    >
                      <SelectTrigger className={errors.category ? 'has-error' : ''}>
                        <SelectValue placeholder="Select category" />
                      </SelectTrigger>
                      <SelectContent>
                        {categories.map((category: any) => (
                          <SelectItem key={category.id} value={category.id}>
                            {category.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    {errors.category && (
                      <p className="text-xs text-destructive">
                        {errors.category}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="status">Status *</Label>
                    <Select
                      value={formData.status}
                      onValueChange={value => handleChange('status', value)}
                    >
                      <SelectTrigger className={errors.status ? 'has-error' : ''}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="DRAFT">Draft</SelectItem>
                        <SelectItem value="ACTIVE">Active</SelectItem>
                        <SelectItem value="INACTIVE">Inactive</SelectItem>
                      </SelectContent>
                    </Select>
                    {errors.status && (
                      <p className="text-xs text-destructive">
                        {errors.status}
                      </p>
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="publishDate">Publish Date *</Label>
                    <input
                      className={`date-input w-full rounded-md border bg-transparent p-2 text-white ${
                        errors.publishDate
                          ? 'border-destructive'
                          : 'border-card-border'
                      }`}
                      id="publishDate"
                      type="datetime-local"
                      value={formData.publishDate}
                      onChange={e =>
                        handleChange('publishDate', e.target.value)
                      }
                      onClick={e => {
                        const input = e.target as HTMLInputElement;
                        input.showPicker();
                      }}
                    />
                    {errors.publishDate && (
                      <p className="text-xs text-destructive">
                        {errors.publishDate}
                      </p>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="expiryDate">Expiry Date *</Label>
                    <input
                      className={`date-input w-full rounded-md border bg-transparent p-2 text-white ${
                        errors.expiryDate
                          ? 'border-destructive'
                          : 'border-card-border'
                      }`}
                      id="expiryDate"
                      type="datetime-local"
                      value={formData.expiryDate}
                      onChange={e => handleChange('expiryDate', e.target.value)}
                      onClick={e => {
                        const input = e.target as HTMLInputElement;
                        input.showPicker();
                      }}
                    />
                    {errors.expiryDate && (
                      <p className="text-xs text-destructive">
                        {errors.expiryDate}
                      </p>
                    )}
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="thumbnailImage">Thumbnail Image *</Label>
                  <Input
                    id="thumbnailImage"
                    type="file"
                    accept="image/jpeg,image/jpg,image/png"
                    onChange={handleThumbnailFileChange}
                    className="hidden"
                  />
                  {!(thumbnailImage.selectedFile || thumbnailImage.link) ? (
                    <>
                      <div
                        onDrop={handleDrop}
                        onDragOver={handleDragOver}
                        className={`relative cursor-pointer rounded-lg border-2 border-dashed p-12 text-center transition-colors hover:border-primary/50 ${
                          errors.thumbnailImage
                            ? 'border-destructive'
                            : 'border-card-border'
                        }`}
                        onClick={() =>
                          document.getElementById('thumbnailImage')?.click()
                        }
                      >
                        <div className="flex flex-col items-center justify-center space-y-4">
                          <Upload className="size-12 text-muted-foreground" />
                          <div className="space-y-2">
                            <p className="text-sm text-muted-foreground">
                              Drag and drop your file here, or click to browse
                            </p>
                          </div>
                        </div>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        Maximum file size: 2MB (JPG, JPEG, PNG)
                      </p>
                    </>
                  ) : (
                    <>
                      <div className="relative overflow-hidden rounded-lg border-2 border-card-border">
                        <Button
                          size="sm"
                          variant="destructive"
                          type="button"
                          className="absolute right-2 top-2 z-10"
                          onClick={handleRemoveThumbnail}
                        >
                          <X className="mr-1 size-4" />
                          Remove
                        </Button>

                        <img
                          className="h-64 w-full bg-black object-contain"
                          src={
                            thumbnailImage.selectedFile
                              ? URL.createObjectURL(thumbnailImage.selectedFile)
                              : FILE_PATH_PREFIX + thumbnailImage.link
                          }
                          alt="Thumbnail Image"
                        />
                      </div>
                      <div
                        onDrop={handleDrop}
                        onDragOver={handleDragOver}
                        className={`relative cursor-pointer rounded-lg border-2 border-dashed p-8 text-center transition-colors hover:border-primary/50 ${
                          errors.thumbnailImage
                            ? 'border-destructive'
                            : 'border-card-border'
                        }`}
                        onClick={() =>
                          document.getElementById('thumbnailImage')?.click()
                        }
                      >
                        <div className="flex flex-col items-center justify-center space-y-3">
                          <Upload className="size-8 text-muted-foreground" />
                          <p className="text-sm text-muted-foreground">
                            Drag and drop your file here, or click to browse
                          </p>
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={e => {
                              e.stopPropagation();
                              document
                                .getElementById('thumbnailImage')
                                ?.click();
                            }}
                          >
                            Replace File
                          </Button>
                        </div>
                      </div>
                    </>
                  )}
                  {errors.thumbnailImage && (
                    <p className="text-xs text-destructive">
                      {errors.thumbnailImage}
                    </p>
                  )}
                </div>
              </CardContent>
            </Card>

            <div className="flex gap-4">
              <Button
                type="button"
                variant="outline"
                className="flex-1"
                onClick={onClose}
              >
                Cancel
              </Button>
              {id ? (
                <Button type="submit" className="flex-1" disabled={loading}>
                  {loading ? 'Updating...' : 'Update News Post'}
                </Button>
              ) : (
                <Button type="submit" className="flex-1" disabled={loading}>
                  {loading ? 'Creating...' : 'Create News Post'}
                </Button>
              )}
            </div>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ActionLatestNews;
