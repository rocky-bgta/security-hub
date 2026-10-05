import { useEffect, useState } from 'react';

import useDebounce from 'hooks/UseDebounce';

function isPdfUrl(url: string): boolean {
  return /\.pdf(\?.*)?$/i.test(url);
}

export const useValidatePdfUrl = (url: string, delay = 500) => {
  const debouncedUrl = useDebounce(url, delay);

  const [isPdf, setIsPdf] = useState<boolean>(false);
  const [isChecking, setIsChecking] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!debouncedUrl || !isPdfUrl(debouncedUrl)) {
      setIsPdf(false);
      setIsChecking(false);
      setError('URL does not look like a PDF');
      return;
    }

    async function validateUrl(url: string) {
      setIsChecking(true);
      try {
        const res = await fetch(url, { method: 'HEAD' });
        const type = res.headers.get('Content-Type');

        if (res.ok && type?.includes('application/pdf')) {
          setIsPdf(true);
          setError(null);
        } else {
          setIsPdf(false);
          setError('Not a valid PDF file');
        }
      } catch (err) {
        setIsPdf(false);
        setError('Failed to fetch PDF');
      } finally {
        setIsChecking(false);
      }
    }

    validateUrl(debouncedUrl);
  }, [debouncedUrl]);

  return { isPdf, isChecking, error };
};
