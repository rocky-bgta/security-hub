import axios from 'axios';
import { useState } from 'react';
import { toast } from 'react-toastify';

interface DownloadResult {
  success: boolean;
  error: string;
}

export const useDownloader = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const [progress, setProgress] = useState<number>(0);
  const [error, setError] = useState<string>('');

  const downloadFile = async (
    url: string,
    filename?: string,
  ): Promise<DownloadResult> => {
    setLoading(true);
    setProgress(0);
    setError('');

    try {
      if (!url) {
        toast.warning('No file to download');
        return { success: false, error: 'No file to download' };
      }
      const response = await axios.get(url, {
        responseType: 'blob',
        onDownloadProgress: progressEvent => {
          const percent = Math.round(
            (progressEvent.loaded * 100) / (progressEvent.total || 0),
          );
          setProgress(percent);
        },
      });

      const blob = new Blob([response.data]);
      const blobUrl = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = blobUrl;
      a.download = `${filename || 'Download'}.${url.split('.').pop()}`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);

      setTimeout(() => URL.revokeObjectURL(blobUrl), 100);

      return { success: true, error: '' };
    } catch (err: any) {
      console.error('Download error:', err);
      setError(err.message || 'Download failed');
      toast.error('Download failed');
      return { success: false, error: err.message || 'Download failed' };
    } finally {
      setLoading(false);
    }
  };

  return {
    downloadFile,
    loading,
    progress,
    error,
  };
};
