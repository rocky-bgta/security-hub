import { toast } from 'react-toastify';
import { ChangeEvent, useEffect, useRef, useState } from 'react';

import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import FileUploader from 'common/FileUploader';
import { Input } from 'common/Input';
import useAPI from 'hooks/UseAPI';
import useStore from 'hooks/UseStore';
import useUploader from 'hooks/UseUploader';
import { IResponse } from 'models/Context';
import { FileType } from 'models/Upload';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { isSuccessResponse } from 'utils/Helper';
import { Loader } from 'lucide-react';

interface IProps {
  stepId: number;
  updateStepsStatus: (
    stepId: number,
    status: 'complete' | 'incomplete',
  ) => void;
}

interface IBrandingData {
  companyName: string;
  logoFilePath: string;
  logoFile?: File | null;
}

interface BrandingResponse {
  id: string;
  companyName: string;
  logoFilePath: string;
  clientAdminId: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  active: boolean;
}

interface FileUploaderHandle {
  clearFiles: () => void;
}

const Personalization = ({ stepId, updateStepsStatus }: IProps) => {
  const [brandingData, setBrandingData] = useState<IBrandingData>({
    companyName: '',
    logoFilePath: '',
    logoFile: null,
  });
  const [originalData, setOriginalData] = useState<IBrandingData>({
    companyName: '',
    logoFilePath: '',
    logoFile: null,
  });
  const [error, setError] = useState<{
    companyName: string;
    logoFile: string;
  }>({
    companyName: '',
    logoFile: '',
  });
  const [logoPreview, setLogoPreview] = useState<string>('');
  const [disableCompanyNameInput, setDisableCompanyNameInput] =
    useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const fileUploaderRef = useRef<FileUploaderHandle>(null);
  const fileSizeErrorRef = useRef<NodeJS.Timeout | null>(null);

  const apiClient = useAPI();
  const { userInfo } = useStore();
  const { uploadFile } = useUploader();

  useEffect(() => {
    const fetchBrandingData = async () => {
      if (!userInfo?.userId) return;

      try {
        setIsLoading(true);
        const response: IResponse<BrandingResponse> = await apiClient.get(
          API_END_POINTS.GET_BRANDING,
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(response.message);
        }

        const data = {
          companyName: response.data?.companyName || '',
          logoFilePath: response.data?.logoFilePath || '',
          logoFile: null,
        };

        if (data.companyName) setDisableCompanyNameInput(true);

        setBrandingData(data);
        setOriginalData(data);
        setLogoPreview(
          response.data?.logoFilePath
            ? FILE_PATH_PREFIX + response.data.logoFilePath
            : '',
        );
      } catch (error) {
        console.error('Error fetching branding data:', error);
        // If branding doesn't exist yet, initialize with empty data
        const emptyData = {
          companyName: '',
          logoFilePath: '',
          logoFile: null,
        };
        setBrandingData(emptyData);
        setOriginalData(emptyData);
      } finally {
        setIsLoading(false);
      }
    };

    fetchBrandingData();
  }, [apiClient, userInfo]);

  const handleCompanyNameChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;

    if (
      value.length &&
      !/^[A-Za-z][A-Za-z0-9]*(?:-[A-Za-z0-9]+)?(?: [A-Za-z][A-Za-z0-9]*(?:-[A-Za-z0-9]+)?)*$/.test(
        value,
      )
    ) {
      setError(prev => ({
        ...prev,
        companyName: 'Invalid company name',
      }));
    } else {
      setError(prev => ({ ...prev, companyName: '' }));
    }

    if (value.length <= 50) {
      setBrandingData(prev => ({ ...prev, companyName: value }));
    }
  };

  const handleBrandingFileUpload = (files: FileList) => {
    if (!files || files.length === 0) return;

    const file = files[0];
    if (file.size > 500 * 1024) {
      setError(prev => ({
        ...prev,
        logoFile: 'Logo file size should not exceed 500KB.',
      }));

      if (fileSizeErrorRef.current) clearTimeout(fileSizeErrorRef.current);

      fileSizeErrorRef.current = setTimeout(() => {
        setError(prev => ({ ...prev, logoFile: '' }));
      }, 5000);

      fileUploaderRef.current?.clearFiles();
      setBrandingData(prev => ({ ...prev, logoFile: null }));
      setLogoPreview('');
      return;
    }
    setError(prev => ({ ...prev, logoFile: '' }));
    setBrandingData(prev => ({ ...prev, logoFile: file }));

    // Create preview
    const reader = new FileReader();
    reader.onloadend = () => {
      setLogoPreview(reader.result as string);
    };
    reader.readAsDataURL(file);
  };

  const handleDiscardChanges = () => {
    setBrandingData({ ...originalData });
    setLogoPreview(
      originalData.logoFilePath
        ? FILE_PATH_PREFIX + originalData.logoFilePath
        : '',
    );
    setError({
      companyName: '',
      logoFile: '',
    });
    fileUploaderRef.current?.clearFiles();
  };

  const handleSaveChanges = async () => {
    if (!brandingData.companyName.trim()) {
      toast.error('Company name is required');
      return;
    }

    for (const key in error) {
      if (error[key as keyof typeof error].length) {
        toast.error(error[key as keyof typeof error]);
        return;
      }
    }

    try {
      setSubmitting(true);
      let logoFilePath = brandingData.logoFilePath;

      // Upload logo if a new file is selected
      if (brandingData.logoFile) {
        const { url, error } = await uploadFile(
          brandingData.logoFile,
          FileType.LOGO as FileType,
        );
        if (error || !url) {
          toast.error(
            error ||
              'Failed to upload logo. Please check your file and try again.',
          );
          return;
        }
        logoFilePath = url; // url is the file path like "/Thakral/ann.png"
      }
      // delete brandingData.logoFile;
      const payload = {
        companyName: brandingData.companyName,
        logoFilePath: logoFilePath || '',
      };

      // Update branding data
      const response: IResponse<BrandingResponse> = await apiClient.put(
        API_END_POINTS.UPDATE_BRANDING,
        {
          data: payload,
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        const updatedData = {
          companyName: response.data.companyName,
          logoFilePath: response.data.logoFilePath,
          logoFile: null,
        };
        setOriginalData(updatedData);
        setBrandingData(updatedData);
        setLogoPreview(
          response.data.logoFilePath
            ? FILE_PATH_PREFIX + response.data.logoFilePath
            : '',
        );

        updateStepsStatus(stepId, 'complete');
        fileUploaderRef.current?.clearFiles();
        toast.success('Branding information saved successfully');
      } else {
        toast.error('Failed to save branding information');
      }
    } catch (error: unknown) {
      console.error('Error saving branding information:', error);
      toast.error(
        error instanceof Error
          ? error.message
          : 'Failed to save branding information',
      );
    } finally {
      setSubmitting(false);
    }
  };

  const hasChanges =
    brandingData.companyName !== originalData.companyName ||
    brandingData.logoFile !== null;

  return (
    <div className="home-h-full home-space-y-6">
      {isLoading ? (
        <div className="home-flex home-h-full home-items-center home-justify-center">
          <Loader className="home-size-10 home-animate-spin home-text-primary" />
        </div>
      ) : (
        <Card>
          <CardHeader>
            <CardTitle>Organization Name</CardTitle>
            <CardDescription>
              The organization name displayed across the portal, reports,
              certificates, and user communications.
            </CardDescription>
          </CardHeader>
          <CardContent className="home-space-y-4">
            <div className="home-space-y-2">
              <Input
                id="companyName"
                placeholder="Type your organization name"
                value={brandingData.companyName}
                onChange={handleCompanyNameChange}
                maxLength={50}
                disabled={disableCompanyNameInput}
              />
              {error.companyName && (
                <p className="home-text-xs home-text-red-500">
                  {error.companyName}
                </p>
              )}
              {!disableCompanyNameInput && (
                <p className="home-text-sm home-text-muted-foreground">
                  {brandingData.companyName.length}/50 characters
                </p>
              )}
            </div>
          </CardContent>

          <CardHeader>
            <CardTitle>Organization Logo</CardTitle>
            <CardDescription>
              Official organization logo displayed throughout the platform to
              ensure a consistent branded experience for all users.
            </CardDescription>
          </CardHeader>
          <CardContent className="home-space-y-6">
            {logoPreview && (
              <div className="home-grid home-grid-cols-1 home-gap-4">
                <div className="home-flex home-items-center home-justify-center home-rounded-lg home-border home-border-card-border home-bg-white home-p-8">
                  <div className="home-flex home-flex-col home-items-center home-gap-2">
                    <img
                      src={logoPreview}
                      alt="Logo preview on light background"
                      className="home-max-h-32 home-max-w-full home-object-contain"
                    />
                  </div>
                </div>
              </div>
            )}

            <div className="home-flex home-flex-col home-justify-center">
              <FileUploader
                ref={fileUploaderRef}
                id="logo-upload"
                accept="image/png,image/jpeg,.png,.jpg,.jpeg"
                maxSize={5}
                placeholder={logoPreview ? 'Replace File' : 'Choose File'}
                onUpload={handleBrandingFileUpload}
                containerClassName="home-w-full"
                description="Recommended size: 300 × 400 px • Maximum file size: 500 KB • Supported formats: PNG, JPG."
              />
              {error.logoFile && (
                <p className="home-mt-2 home-text-xs home-text-red-500">
                  {error.logoFile}
                </p>
              )}
            </div>

            <div className="home-flex home-justify-end home-gap-3">
              <Button
                variant="outline"
                onClick={handleDiscardChanges}
                disabled={!hasChanges || submitting}
                size="sm"
              >
                Discard
              </Button>
              <Button
                onClick={handleSaveChanges}
                disabled={!hasChanges || submitting}
                size="sm"
              >
                {submitting ? 'Saving...' : 'Save'}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default Personalization;
