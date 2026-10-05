import RemoteUseUploaderHook from 'home-module/useUploader';

export const useUploader = (): {
  uploadFile: (
    file: File,
    type?: string,
  ) => Promise<{
    url: string;
    error: string;
  }>;
  loading: boolean;
  progress: number;
  fileUrl: string;
  error: string;
} => RemoteUseUploaderHook();
