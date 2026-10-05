import { Upload, X } from 'lucide-react';
import { Fragment, useEffect, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'components/common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import { Switch } from 'components/common/Switch';
import {
  LEADERBOARD_FIELD_MAX_LENGTH,
  TLeaderboardFieldErrors,
  validateLeaderboardTextFields,
} from 'schemas/LeaderboardSchema';
import {
  THUMBNAIL_ACCEPT,
  validateThumbnailFile,
  validateVideoFile,
  VIDEO_ACCEPT,
} from 'features/leader-board/leaderboardValidation';
import { useAPI } from 'hooks/UseAPI';
import { useUploader } from 'hooks/UseUploader';
import { FileType as GlobalFileType, Status, VideoType } from 'models/Global';
import { ILeaderBoard } from 'models/LeaderBoard';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { isSuccessResponse } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  entry?: ILeaderBoard | null;
  onSubmit: () => void;
}

const EMPTY_ERRORS: TLeaderboardFieldErrors = {
  title: '',
  name: '',
  designation: '',
  video: '',
  thumbnail: '',
};

const ActionLeaderboardModal = ({
  isOpen,
  onClose,
  entry,
  onSubmit,
}: IProps) => {
  const isEditMode = Boolean(entry?.id);
  const apiClient = useAPI();
  const { uploadFile } = useUploader();

  const [formData, setFormData] = useState({
    id: '',
    title: '',
    name: '',
    designation: '',
    videoType: VideoType.UPLOAD_FILE,
    status: Status.INACTIVE,
  });
  const [thumbnail, setThumbnail] = useState<{
    link: string;
    selectedFile: File | null;
  }>({ link: '', selectedFile: null });
  const [video, setVideo] = useState<{
    link: string;
    selectedFile: File | null;
  }>({ link: '', selectedFile: null });
  const [videoPreview, setVideoPreview] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errors, setErrors] = useState<TLeaderboardFieldErrors>(EMPTY_ERRORS);
  const [isValidatingVideo, setIsValidatingVideo] = useState(false);

  const fieldIdPrefix = isEditMode ? 'edit' : 'add';

  useEffect(() => {
    if (!isOpen) return;

    if (entry) {
      setFormData({
        id: entry.id,
        title: entry.title,
        name: entry.name,
        designation: entry.designation,
        videoType: entry.videoType,
        status: entry.status,
      });
      setThumbnail({ link: entry.thumbnailUrl, selectedFile: null });
      setVideo({ link: entry.videoUrl, selectedFile: null });
    } else {
      resetForm();
    }
    setErrors(EMPTY_ERRORS);
  }, [isOpen, entry]);

  useEffect(() => {
    if (video.selectedFile) {
      const objectUrl = URL.createObjectURL(video.selectedFile);
      setVideoPreview(objectUrl);
      return () => URL.revokeObjectURL(objectUrl);
    }
    setVideoPreview('');
  }, [video.selectedFile]);

  const resetForm = () => {
    setFormData({
      id: '',
      title: '',
      name: '',
      designation: '',
      videoType: VideoType.UPLOAD_FILE,
      status: Status.INACTIVE,
    });
    setErrors(EMPTY_ERRORS);
    setThumbnail({ link: '', selectedFile: null });
    setVideo({ link: '', selectedFile: null });
    setVideoPreview('');
  };

  const handleClose = () => {
    resetForm();
    onClose();
  };

  const handleInputChange = (field: 'title' | 'name' | 'designation', value: string) => {
    const maxLength = LEADERBOARD_FIELD_MAX_LENGTH[field];
    if (value.length > maxLength) {
      const maxMessages = {
        title: 'Title cannot exceed 150 characters.',
        name: 'Leader name cannot exceed 50 characters.',
        designation: 'Designation cannot exceed 250 characters.',
      };
      setErrors(prev => ({ ...prev, [field]: maxMessages[field] }));
      return;
    }

    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleThumbnailFileChange = (
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const error = validateThumbnailFile(file);
    if (error) {
      setErrors(prev => ({ ...prev, thumbnail: error }));
      e.target.value = '';
      return;
    }

    setThumbnail({ link: '', selectedFile: file });
    setErrors(prev => ({ ...prev, thumbnail: '' }));
  };

  const handleVideoFileChange = async (
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsValidatingVideo(true);
    const error = await validateVideoFile(file);
    setIsValidatingVideo(false);

    if (error) {
      setErrors(prev => ({ ...prev, video: error }));
      e.target.value = '';
      return;
    }

    setVideo({ link: '', selectedFile: file });
    setErrors(prev => ({ ...prev, video: '' }));
  };

  const handleRemoveThumbnailPreview = () => {
    setThumbnail({ link: '', selectedFile: null });
    setErrors(prev => ({ ...prev, thumbnail: '' }));
  };

  const handleRemoveVideoPreview = () => {
    setVideo({ link: '', selectedFile: null });
    setVideoPreview('');
    setErrors(prev => ({ ...prev, video: '' }));
  };

  const validateForm = async (): Promise<boolean> => {
    const textErrors = validateLeaderboardTextFields({
      title: formData.title,
      name: formData.name,
      designation: formData.designation,
    });

    const newErrors: TLeaderboardFieldErrors = { ...EMPTY_ERRORS, ...textErrors };

    if (!thumbnail.selectedFile && !thumbnail.link) {
      newErrors.thumbnail = 'Thumbnail image is required.';
    } else if (thumbnail.selectedFile) {
      const thumbnailError = validateThumbnailFile(thumbnail.selectedFile);
      if (thumbnailError) newErrors.thumbnail = thumbnailError;
    }

    if (!video.selectedFile && !video.link) {
      newErrors.video = 'Video file is required.';
    } else if (video.selectedFile) {
      const videoError = await validateVideoFile(video.selectedFile);
      if (videoError) newErrors.video = videoError;
    }

    setErrors(newErrors);
    return !Object.values(newErrors).some(Boolean);
  };

  const handleSubmit = async () => {
    if (!(await validateForm())) return;

    setIsSubmitting(true);
    try {
      let thumbnailLink = thumbnail.link;

      if (thumbnail.selectedFile) {
        const { url, error } = await uploadFile(
          thumbnail.selectedFile,
          GlobalFileType.THUMBNAIL as GlobalFileType,
        );
        if (error) {
          toast.error(
            'Unable to upload the selected thumbnail. Please try again.',
          );
          return;
        }
        thumbnailLink = url;
      }

      let videoLink = video.link;
      if (video.selectedFile) {
        const { url, error } = await uploadFile(
          video.selectedFile,
          GlobalFileType.CONTENT as GlobalFileType,
        );
        if (error) {
          toast.error(
            'Unable to upload the selected video. Please try again.',
          );
          return;
        }
        videoLink = url;
      }

      const payload = {
        title: formData.title.trim(),
        name: formData.name.trim(),
        designation: formData.designation.trim(),
        videoType: formData.videoType,
        videoUrl: videoLink,
        thumbnailUrl: thumbnailLink,
        status: formData.status,
      };

      const response = isEditMode
        ? await apiClient.put(
            API_END_POINTS.UPDATE_LEADERBOARD.replace(':id', formData.id),
            { data: payload },
          )
        : await apiClient.post(API_END_POINTS.CREATE_LEADERBOARD, {
            data: payload,
          });

      if (isSuccessResponse(response.statusCode)) {
        toast.success(
          isEditMode
            ? 'Leader entry has been successfully updated.'
            : 'Leader entry has been successfully added.',
        );
        onSubmit();
        handleClose();
      } else {
        toast.error(
          isEditMode
            ? 'You can only update your own leaderboards'
            : response.message,
        );
      }
    } catch (error) {
      toast.error(
        isEditMode
          ? 'Failed to update leader entry.'
          : 'Failed to add leader entry.',
      );
      console.error(error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const hasThumbnail =
    Boolean(thumbnail.selectedFile) || Boolean(thumbnail.link);
  const hasVideo =
    Boolean(video.selectedFile) ||
    Boolean(video.link) ||
    Boolean(videoPreview);

  return (
    <Dialog
      open={isOpen}
      onOpenChange={open => {
        if (!open) handleClose();
      }}
    >
      <DialogContent className="max-h-[90vh] max-w-[60%] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>
            {isEditMode
              ? 'Edit Leader Message Entry'
              : 'Add New Leader Message Entry'}
          </DialogTitle>
          <DialogDescription>
            {isEditMode
              ? 'Update leader message entry details'
              : 'Add a new leader message to the leaderboard with video content'}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          <div>
            <Label htmlFor={`${fieldIdPrefix}-title`}>Title *</Label>
            <Input
              id={`${fieldIdPrefix}-title`}
              placeholder="e.g., Cybersecurity Champion"
              value={formData.title}
              onChange={e => handleInputChange('title', e.target.value)}
            />
            <div className="flex justify-between">
              <p className="text-sm text-red-500">{errors.title}</p>
              <p className="text-sm text-muted-foreground">
                {formData.title.length}/{LEADERBOARD_FIELD_MAX_LENGTH.title}{' '}
                characters
              </p>
            </div>
          </div>

          <div>
            <Label htmlFor={`${fieldIdPrefix}-leader-name`}>Leader Name *</Label>
            <Input
              id={`${fieldIdPrefix}-leader-name`}
              placeholder="Enter leader name"
              value={formData.name}
              onChange={e => handleInputChange('name', e.target.value)}
            />
            <div className="flex justify-between">
              <p className="text-sm text-red-500">{errors.name}</p>
              <p className="text-sm text-muted-foreground">
                {formData.name.length}/{LEADERBOARD_FIELD_MAX_LENGTH.name}{' '}
                characters
              </p>
            </div>
          </div>

          <div>
            <Label htmlFor={`${fieldIdPrefix}-designation`}>Designation *</Label>
            <Input
              id={`${fieldIdPrefix}-designation`}
              placeholder="Enter designation/position"
              value={formData.designation}
              onChange={e => handleInputChange('designation', e.target.value)}
            />
            <div className="flex justify-between">
              <p className="text-sm text-red-500">{errors.designation}</p>
              <p className="text-sm text-muted-foreground">
                {formData.designation.length}/
                {LEADERBOARD_FIELD_MAX_LENGTH.designation} characters
              </p>
            </div>
          </div>

          <div>
            <Label htmlFor={`${fieldIdPrefix}-thumbnail`}>Thumbnail *</Label>
            {!hasThumbnail ? (
              <Fragment>
                <label htmlFor={`${fieldIdPrefix}-thumbnail`}>
                  <div className="cursor-pointer rounded-lg border-2 border-dashed border-card-border p-8 text-center transition-colors hover:border-primary">
                    <Upload className="mx-auto mb-4 size-12 text-muted-foreground" />
                    <p className="text-sm text-muted-foreground">
                      Click to upload or drag and drop
                      <br />
                      JPG, JPEG, PNG files up to 2 MB
                      <br />
                      Recommended resolution: 1280 × 720
                    </p>
                  </div>
                  <input
                    type="file"
                    accept={THUMBNAIL_ACCEPT}
                    className="hidden"
                    id={`${fieldIdPrefix}-thumbnail`}
                    onChange={handleThumbnailFileChange}
                  />
                </label>
                {errors.thumbnail && (
                  <p className="text-sm text-destructive">{errors.thumbnail}</p>
                )}
              </Fragment>
            ) : (
              <div className="relative overflow-hidden rounded-lg border-2 border-card-border">
                <Button
                  size="sm"
                  variant="destructive"
                  className="absolute right-2 top-2"
                  onClick={handleRemoveThumbnailPreview}
                >
                  <X className="mr-1 size-4" />
                  Remove
                </Button>
                <img
                  src={
                    thumbnail.selectedFile
                      ? URL.createObjectURL(thumbnail.selectedFile)
                      : FILE_PATH_PREFIX + thumbnail.link
                  }
                  alt="Thumbnail"
                  className="h-64 w-full bg-black object-contain"
                />
                {thumbnail.selectedFile && (
                  <div className="bg-muted p-2">
                    <p className="truncate text-xs text-muted-foreground">
                      {thumbnail.selectedFile.name}
                    </p>
                  </div>
                )}
              </div>
            )}
          </div>

          <div>
            <Label htmlFor={`${fieldIdPrefix}-video-upload`}>
              Upload Video *
            </Label>
            {!hasVideo ? (
              <Fragment>
                <label htmlFor={`${fieldIdPrefix}-video-upload`}>
                  <div className="cursor-pointer rounded-lg border-2 border-dashed border-card-border p-8 text-center transition-colors hover:border-primary">
                    <Upload className="mx-auto mb-4 size-12 text-muted-foreground" />
                    <p className="text-sm text-muted-foreground">
                      Click to upload or drag and drop
                      <br />
                      MP4, MOV, or WEBM files up to 100 MB
                      <br />
                      Duration: 10 seconds to 10 minutes
                    </p>
                  </div>
                  <input
                    type="file"
                    accept={VIDEO_ACCEPT}
                    className="hidden"
                    id={`${fieldIdPrefix}-video-upload`}
                    onChange={handleVideoFileChange}
                    disabled={isValidatingVideo}
                  />
                </label>
                {errors.video && (
                  <p className="text-sm text-destructive">{errors.video}</p>
                )}
                {isValidatingVideo && (
                  <p className="text-sm text-muted-foreground">
                    Validating video...
                  </p>
                )}
              </Fragment>
            ) : (
              <div className="relative overflow-hidden rounded-lg border-2 border-card-border">
                <video
                  src={
                    video.selectedFile
                      ? videoPreview
                      : FILE_PATH_PREFIX + video.link
                  }
                  controls
                  className="h-64 w-full bg-black object-contain"
                />
                <Button
                  size="sm"
                  variant="destructive"
                  className="absolute right-2 top-2"
                  onClick={handleRemoveVideoPreview}
                >
                  <X className="mr-1 size-4" />
                  Remove
                </Button>
                <div className="bg-muted p-2">
                  <p className="truncate text-xs text-muted-foreground">
                    {video.selectedFile?.name || video.link}
                  </p>
                </div>
              </div>
            )}
          </div>

          <div className="flex items-center justify-between">
            <Label htmlFor={`${fieldIdPrefix}-status`}>Active Status</Label>
            <Switch
              id={`${fieldIdPrefix}-status`}
              checked={formData.status === Status.ACTIVE}
              onCheckedChange={checked =>
                setFormData(prev => ({
                  ...prev,
                  status: checked ? Status.ACTIVE : Status.INACTIVE,
                }))
              }
            />
          </div>

          <Button
            className="w-full"
            disabled={isSubmitting || isValidatingVideo}
            onClick={handleSubmit}
          >
            {isSubmitting
              ? isEditMode
                ? 'Saving...'
                : 'Adding...'
              : isEditMode
                ? 'Save Changes'
                : 'Add Leader Entry'}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default ActionLeaderboardModal;
