import axios from 'axios';
import { useState } from 'react';

import useAPI from 'hooks/UseAPI';
import { IResponse } from 'models/Context';
import {
  FileType,
  IUploadPayload,
  IUploadResponse,
  IUseUploader,
} from 'models/Upload';
import { API_END_POINTS } from 'routes/APIEndpoints';

const useUploader = (): IUseUploader => {
  const [loading, setLoading] = useState<boolean>(false);
  const [progress, setProgress] = useState<number>(0);
  const [fileUrl, setFileUrl] = useState<string>('');
  const [error, setError] = useState<string>('');

  const apiClient = useAPI();

  const uploadFile = async (file: File, type = FileType.CONTENT) => {
    setLoading(true);
    setProgress(0);
    setFileUrl('');
    setError('');

    try {
      const payload: IUploadPayload = {
        filename: file.name,
        fileType: type,
      };

      const response: IResponse<IUploadResponse> = await apiClient.post(
        API_END_POINTS.UPLOAD_URL,
        { data: payload },
      );

      const uploadData = response.data;
      if (!uploadData?.url) {
        throw new Error('Invalid upload URL response');
      }

      await axios.put(uploadData.url, file, {
        headers: { ...uploadData.requiredHeaders },
        onUploadProgress: progressEvent => {
          const percent = Math.round(
            (progressEvent.loaded * 100) / (progressEvent.total ?? file.size),
          );
          setProgress(percent);
        },
      });

      setFileUrl(uploadData.key);

      return { url: uploadData.key, error: '' };
    } catch (err: any) {
      console.error('Upload error:', err);
      setError(err.message || 'Upload failed');

      return { url: '', error: err.message || 'Upload failed' };
    } finally {
      setLoading(false);
    }
  };

  return {
    uploadFile,
    loading,
    progress,
    fileUrl,
    error,
  };
};

export default useUploader;
