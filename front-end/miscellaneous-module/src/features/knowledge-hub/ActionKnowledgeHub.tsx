import { safeWindowOpen } from 'home-module/security';
import { Upload } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
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
import { Switch } from 'common/Switch';
import TextEditor from 'common/TextEditor';
import { useAPI } from 'hooks/UseAPI';
import { useUploader } from 'hooks/UseUploader';
import {
  IKnowledgeHubPayload,
  KnowledgeHubResourceType,
  KnowledgeResourceTypes,
} from 'models/KnowledgeHub';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  id?: string;
  onSubmit: () => void;
}

const InitialFormData: IKnowledgeHubPayload = {
  name: '',
  slug: '',
  resourceType: KnowledgeHubResourceType.DOCUMENT,
  content: '',
  categoryId: '',
  sequence: 1,
  imageUrl: '',
  videoUrl: '',
  publishedDate: '',
  expireDate: '',
  tags: '',
  allowDownload: true,
  status: 'ACTIVE',
};

const ActionKnowledgeHub = ({ isOpen, onClose, id, onSubmit }: IProps) => {
  const apiClient = useAPI();
  const { uploadFile } = useUploader();
  const [formData, setFormData] =
    useState<IKnowledgeHubPayload>(InitialFormData);
  const [categories, setCategories] = useState<
    Array<{ id: string; name: string }>
  >([]);
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    if (isOpen) {
      fetchCategories();

      if (id) {
        fetchKnowledgeHub();
      } else {
        // Add mode - reset to initial values
        setFormData(InitialFormData);
        setFile(null);
      }
    }
  }, [isOpen, id]);

  const fetchKnowledgeHub = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_KNOWLEDGE_HUB_DETAILS.replace(':id', id ?? ''),
      );
      if (isSuccessResponse(response.statusCode)) {
        setFormData({
          name: response.data.name,
          slug: response.data.slug,
          resourceType: response.data.resourceType,
          content: response.data.content,
          categoryId: response.data.categoryId,
          sequence: response.data.sequence,
          imageUrl: response.data.imageUrl,
          videoUrl: response.data.videoUrl,
          publishedDate: response.data.publishedDate,
          expireDate: response.data.expireDate,
          tags: response.data.tags
            .map((tag: { name: string }) => tag.name)
            .join(', '),
          allowDownload: response.data.allowDownload,
          status: response.data.status,
        });
      }
    } catch (error) {
      console.error('Error fetching knowledge hub:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.GET_LATEST_NEWS_CATEGORY_LIST,
      );
      if (isSuccessResponse(response.statusCode)) {
        setCategories(response.data);
      }
    } catch (error) {
      console.error('Error fetching categories:', error);
    }
  };

  const validateForm = () => {
    if (!formData.name) {
      toast.error('Name is required');
      return false;
    }
    if (!formData.resourceType) {
      toast.error('Resource type is required');
      return false;
    }
    if (!formData.categoryId) {
      toast.error('Category is required');
      return false;
    }
    if (!formData.status) {
      toast.error('Status is required');
      return false;
    }
    if (!formData.publishedDate) {
      toast.error('Published date is required');
      return false;
    }
    if (!formData.expireDate) {
      toast.error('Expire date is required');
      return false;
    }
    if (!formData.sequence) {
      toast.error('Sequence is required');
      return false;
    }
    if (!file && !formData.videoUrl) {
      toast.error('File or video URL is required');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      let fileUrl = '';

      // Upload file if a new one is selected
      if (file) {
        const { url, error } = await uploadFile(file, 'CONTENT');
        if (error) {
          toast.error(error);
          setLoading(false);
          return;
        }
        fileUrl = url;
      }

      const payload: IKnowledgeHubPayload = {
        ...formData,
        tags: formData.tags
          .split(' ')
          .filter(tag => tag.trim() !== '')
          .map(tag => tag.trim())
          .join(','),

        slug: formData.name.toLowerCase().replace(/ /g, '-'),
        ...(fileUrl && {
          imageUrl: fileUrl,
          videoUrl: fileUrl,
        }),
      };

      const endpoint = id
        ? API_END_POINTS.UPDATE_KNOWLEDGE_HUB.replace(':id', id)
        : API_END_POINTS.CREATE_KNOWLEDGE_HUB;

      const response = id
        ? await apiClient.put(endpoint, { data: payload })
        : await apiClient.post(endpoint, { data: payload });

      if (isSuccessResponse(response.statusCode)) {
        toast.success(
          id
            ? 'Knowledge hub updated successfully'
            : 'Knowledge hub created successfully',
        );
        onSubmit();
        onClose();
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error saving knowledge hub:', error);
      toast.error('An error occurred while saving');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile && selectedFile.size > 50 * 1024 * 1024) {
      toast.error('File size must be less than 50MB');
      e.target.value = '';
      return;
    }
    if (selectedFile) {
      setFile(selectedFile);
    }
  };

  const handlePreviewVideo = () => {
    safeWindowOpen(FILE_PATH_PREFIX + formData.videoUrl, '_blank');
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent
        className="max-h-[90vh] w-2/3 overflow-auto"
        allowNestedModals={true}
        nestedModalSelectors={['.tox-tinymce, .tox-tinymce-aux', '.tox']}
        onOpenAutoFocus={e => e.preventDefault()}
        onCloseAutoFocus={e => e.preventDefault()}
      >
        <DialogHeader>
          <DialogTitle>{id ? 'Edit Resource' : 'Add New Resource'}</DialogTitle>
          <DialogDescription>
            {id
              ? 'Update resource information'
              : 'Add a new learning resource to the Knowledge Hub'}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 p-2">
          <div className="space-y-2">
            <Label htmlFor="name">Title *</Label>
            <Input
              id="name"
              value={formData.name}
              onChange={e => handleChange('name', e.target.value)}
              placeholder="e.g., Getting Started with Cybersecurity"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="type">Resource Type *</Label>
              <Select
                value={formData.resourceType}
                onValueChange={value => handleChange('resourceType', value)}
              >
                <SelectTrigger className="capitalize">
                  <SelectValue placeholder="Select resource type" />
                </SelectTrigger>
                <SelectContent className="capitalize">
                  {KnowledgeResourceTypes.map(type => (
                    <SelectItem key={type} value={type}>
                      {type.split('_').join(' ').toLowerCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="category">Category *</Label>
              <Select
                value={formData.categoryId}
                onValueChange={value => handleChange('categoryId', value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select category" />
                </SelectTrigger>
                <SelectContent>
                  {categories.map(category => (
                    <SelectItem key={category.id} value={category.id}>
                      {category.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="content">Description (Optional)</Label>
            <TextEditor
              isDark={true}
              value={formData.content}
              onChange={value => handleChange('content', value)}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="file-upload">Upload File *</Label>
            <label htmlFor="file-upload" className="cursor-pointer">
              <div className="rounded-lg border-2 border-dashed border-card-border p-6">
                <div className="text-center">
                  <Upload className="mx-auto size-12 text-gray-400" />
                  <div className="mt-4">
                    <span className="mt-2 block text-sm font-medium text-gray-900">
                      Click to upload a file or drag and drop
                    </span>
                    <input
                      id="file-upload"
                      name="file-upload"
                      type="file"
                      className="sr-only"
                      onChange={handleFileChange}
                    />
                    <p className="mt-1 text-xs text-gray-500">
                      PDF, DOC, DOCX, PPT, PPTX, MP4, or image files up to 50MB
                    </p>
                    {file || formData.videoUrl ? (
                      <p className="mt-2 text-sm text-primary">
                        Selected:{' '}
                        {file?.name || FILE_PATH_PREFIX + formData.videoUrl}
                      </p>
                    ) : null}
                  </div>
                </div>
              </div>
            </label>
            {formData.videoUrl && (
              <Button
                variant="outline"
                type="button"
                onClick={handlePreviewVideo}
                className="w-full"
              >
                Preview File
              </Button>
            )}
          </div>

          <div className="grid grid-cols-2 items-center gap-4">
            <div className="space-y-2">
              <Label htmlFor="tags">Tags (Optional)</Label>
              <Input
                id="tags"
                value={formData.tags}
                onChange={e => handleChange('tags', e.target.value)}
                placeholder="e.g., Security, Tutorial, Beginner (comma-separated)"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="status">Status *</Label>
              <Select
                value={formData.status}
                onValueChange={value => handleChange('status', value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="DRAFT">Draft</SelectItem>
                  <SelectItem value="ACTIVE">Active</SelectItem>
                  <SelectItem value="INACTIVE">Inactive</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="publishedDate">Published Date *</Label>
              <Input
                id="publishedDate"
                type="datetime-local"
                value={formData.publishedDate}
                onChange={e => handleChange('publishedDate', e.target.value)}
                onClick={e => {
                  const input = e.target as HTMLInputElement;
                  input.showPicker();
                }}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="expireDate">Expire Date *</Label>
              <Input
                id="expireDate"
                type="datetime-local"
                value={formData.expireDate}
                onChange={e => handleChange('expireDate', e.target.value)}
                onClick={e => {
                  const input = e.target as HTMLInputElement;
                  input.showPicker();
                }}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="sequence">Sequence *</Label>
              <Input
                id="sequence"
                type="number"
                min={1}
                value={formData.sequence}
                onChange={e => handleChange('sequence', Number(e.target.value))}
              />
            </div>

            <div className="flex items-center space-x-2">
              <Switch
                id="allowDownload"
                checked={formData.allowDownload}
                onCheckedChange={checked =>
                  handleChange('allowDownload', checked)
                }
              />
              <Label htmlFor="allowDownload">Allow Download</Label>
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading
                ? id
                  ? 'Updating...'
                  : 'Creating...'
                : id
                  ? 'Save Changes'
                  : 'Add Resource'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default ActionKnowledgeHub;
