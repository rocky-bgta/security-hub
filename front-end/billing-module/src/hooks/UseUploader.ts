import RemoteUseUploaderHook from 'home-module/useUploader';

export enum FileType {
  CONTENT = 'CONTENT',
  LOGO = 'LOGO',
}

export const useUploader = (): {
  uploadFile: (
    file: File,
    type?: FileType,
  ) => Promise<{
    url: string;
    error: string;
  }>;
  loading: boolean;
  progress: number;
  fileUrl: string;
  error: string;
} => RemoteUseUploaderHook();
