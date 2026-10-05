import { ChangeEvent, useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';
import { Loader2 } from 'lucide-react';
import { Button } from 'components/common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import FileUploader, {
  FileUploaderHandle,
} from 'components/common/FileUploader';
import { useAPI } from 'hooks/UseAPI';
import { useStore } from 'hooks/UseStore';
import { useUploader } from 'hooks/UseUploader';
import { FileType, IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { FcUpload } from 'react-icons/fc';

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

const ClientAdminBrandingManagement = () => {
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
  const [logoPreview, setLogoPreview] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const fileUploaderRef = useRef<FileUploaderHandle>(null);

  const { userInfo, setBrandingInfo } = useStore();
  const apiClient = useAPI();
  const { uploadFile } = useUploader();

  useEffect(() => {
    fetchBrandingData();
  }, [userInfo]);

  const fetchBrandingData = async () => {
    if (!userInfo?.userId) return;

    try {
      const response: IResponse<BrandingResponse> = await apiClient.get(
        API_END_POINTS.GET_BRANDING,
      );

      const data = {
        companyName: response.data?.companyName || '',
        logoFilePath: response.data?.logoFilePath || '',
        logoFile: null,
      };

      setBrandingData(data);
      setOriginalData(data);
      setLogoPreview(
        response.data?.logoFilePath
          ? FILE_PATH_PREFIX + response.data.logoFilePath
          : '',
      );
    } catch (error) {
      console.error('Error fetching branding data:', error);
      const emptyData = {
        companyName: '',
        logoFilePath: '',
        logoFile: null,
      };
      setBrandingData(emptyData);
      setOriginalData(emptyData);
    }
  };

  const handleCompanyNameChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    if (value.length <= 50) {
      setBrandingData(prev => ({ ...prev, companyName: value }));
    }
  };

  const handleFileUpload = (files: FileList, id: string) => {
    if (!files || files.length === 0) return;

    const file = files[0];
    setBrandingData(prev => ({ ...prev, logoFile: file }));

    const reader = new FileReader();
    reader.onloadend = () => {
      setLogoPreview(reader.result as string);
    };
    reader.readAsDataURL(file);
  };

  const handleDiscardCompanyName = () => {
    setBrandingData(prev => ({
      ...prev,
      companyName: originalData.companyName,
    }));
  };

  const handleDiscardLogo = () => {
    setBrandingData(prev => ({ ...prev, logoFile: null }));
    setLogoPreview(
      originalData.logoFilePath
        ? FILE_PATH_PREFIX + originalData.logoFilePath
        : '',
    );
    fileUploaderRef.current?.clearFiles();
  };

  const handleSaveCompanyName = async () => {
    if (!brandingData.companyName.trim()) {
      toast.error('Company name is required');
      return;
    }
    setIsSubmitting(true);
    try {
      const payload = {
        companyName: brandingData.companyName,
        logoFilePath: originalData.logoFilePath || '',
      };

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
          logoFile: brandingData.logoFile,
        };
        setOriginalData(prev => ({
          ...prev,
          companyName: response.data.companyName,
        }));
        setBrandingData(updatedData);
        toast.success('Company name saved successfully');
      } else {
        toast.error('Failed to save company name');
      }
    } catch (error: any) {
      console.error('Error saving company name:', error);
      const errorMessage =
        error?.response?.data?.message ||
        error?.message ||
        'Failed to save company name';
      toast.error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSaveLogo = async () => {
    setIsSubmitting(true);
    try {
      let logoFilePath = originalData.logoFilePath;
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
          setIsSubmitting(false);
          return;
        }
        logoFilePath = url;
      }
      const payload = {
        companyName: originalData.companyName,
        logoFilePath: logoFilePath || '',
      };

      const response: IResponse<BrandingResponse> = await apiClient.put(
        API_END_POINTS.UPDATE_BRANDING,
        {
          data: payload,
        },
      );

      if (isSuccessResponse(response.statusCode)) {
        const updatedData = {
          companyName: brandingData.companyName,
          logoFilePath: response.data.logoFilePath,
          logoFile: null,
        };
        setOriginalData(prev => ({
          ...prev,
          logoFilePath: response.data.logoFilePath,
        }));
        setBrandingData(updatedData);
        setLogoPreview(
          response.data.logoFilePath
            ? FILE_PATH_PREFIX + response.data.logoFilePath
            : '',
        );
        setBrandingInfo((prev: any) => ({
          ...prev,
          logoFilePath: response.data.logoFilePath,
        }));
        toast.success('Logo saved successfully');
      } else {
        toast.error('Failed to save logo');
      }
    } catch (error: any) {
      console.error('Error saving logo:', error);
      const errorMessage =
        error?.response?.data?.message ||
        error?.message ||
        'Failed to save logo';
      toast.error(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const hasCompanyNameChanges =
    brandingData.companyName !== originalData.companyName;
  const hasLogoChanges = brandingData.logoFile !== null;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Branding Management
        </h1>
        <p className="text-muted-foreground">
          Customize your company name and logo
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Company Name</CardTitle>
          <CardDescription>
            The name of your company. This will be used as the name of your
            company in the Aspire Tech portal and in all communications that
            Aspire Tech will send.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="companyName">Company Name</Label>
            <Input
              id="companyName"
              placeholder="Type your company name"
              value={brandingData.companyName}
              onChange={handleCompanyNameChange}
              maxLength={50}
              readOnly
            />
            <p className="text-sm text-muted-foreground">
              {brandingData.companyName.length}/50 characters
            </p>
          </div>
          <div className="flex justify-end gap-3">
            <Button
              variant="outline"
              onClick={handleDiscardCompanyName}
              disabled={!hasCompanyNameChanges}
              size="sm"
            >
              Discard
            </Button>
            <Button
              onClick={handleSaveCompanyName}
              disabled={!hasCompanyNameChanges || isSubmitting}
              size="sm"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 size-4 animate-spin" />
                  Saving...
                </>
              ) : (
                'Save'
              )}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Logo Customization</CardTitle>
          <CardDescription>
            Set an image to be this company&apos;s logo. The logo will be used on all
            communications for this company besides phishing emails and will
            appear on the Learning Management System. Logo uploader supports PNG
            and JPG only, at a recommended size of 300x400. Updates to the logo
            can take up to an hour to appear.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          {logoPreview && (
            <div className="grid grid-cols-2 gap-4">
              <div className="flex items-center justify-center rounded-lg border border-card-border bg-white p-8">
                <div className="flex flex-col items-center gap-2">
                  <img
                    src={logoPreview}
                    alt="Logo preview on light background"
                    className="max-h-32 max-w-full object-contain"
                  />
                </div>
              </div>
              <div className="flex items-center justify-center rounded-lg border border-card-border bg-gray-800 p-8">
                <div className="flex flex-col items-center gap-2">
                  <img
                    src={logoPreview}
                    alt="Logo preview on dark background"
                    className="max-h-32 max-w-full object-contain"
                  />
                </div>
              </div>
            </div>
          )}

          <div className="flex items-center justify-center">
            <FileUploader
              ref={fileUploaderRef}
              id="logo-upload"
              accept="image/png,image/jpeg,image/jpg"
              maxSize={5}
              placeholder={
                logoPreview ? (
                  <span className="flex items-center gap-2">
                    <FcUpload className="size-4" />
                    REPLACE FILE
                  </span>
                ) : (
                  <span className="flex items-center gap-2">
                    <FcUpload className="size-4" />
                    UPLOAD FILE
                  </span>
                )
              }
              onUpload={handleFileUpload}
              containerClassName="inline-flex items-center rounded-md px-4 py-4 text-sm font-medium text-primary cursor-pointer"
            />
          </div>

          <div className="flex justify-end gap-3">
            <Button
              variant="outline"
              onClick={handleDiscardLogo}
              disabled={!hasLogoChanges}
              size="sm"
            >
              Discard
            </Button>
            <Button
              onClick={handleSaveLogo}
              disabled={!hasLogoChanges || isSubmitting}
              size="sm"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="mr-2 size-4 animate-spin" />
                  Saving...
                </>
              ) : (
                'Save'
              )}
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ClientAdminBrandingManagement;
