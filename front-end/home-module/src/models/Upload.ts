export enum FileType {
  CONTENT = 'CONTENT',
  LOGO = 'LOGO',
}

export interface IUseUploader {
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
}

export interface IUploadPayload {
  filename: string;
  fileType: FileType;
}

export interface IUploadResponse {
  provider: string;
  fileId: string;
  bucketOrContainer: string;
  key: string;
  method: string;
  url: string;
  requiredHeaders: {
    [key: string]: string;
  };
  expiresAt: string;
}
