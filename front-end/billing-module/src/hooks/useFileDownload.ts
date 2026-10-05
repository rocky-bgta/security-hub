import { useCallback } from 'react';

export const useFileDownload = () => {
  const downloadFile = useCallback(
    async (fileUrl: string, fileName: string) => {
      if (!fileUrl) {
        console.error('File URL is required');
        return;
      }

      try {
        const response = await fetch(fileUrl, {
          method: 'GET',
        });

        if (!response.ok) {
          throw new Error('Failed to download file');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);

        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.download = fileName;
        document.body.appendChild(anchor);
        anchor.click();

        anchor.remove();
        window.URL.revokeObjectURL(url);
      } catch (error) {
        console.error('Download error:', error);
      }
    },
    [],
  );

  return { downloadFile };
};
