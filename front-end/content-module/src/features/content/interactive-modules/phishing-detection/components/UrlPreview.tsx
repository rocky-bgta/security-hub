import { cn } from 'utils/Helper';

interface IProps {
  url: string | null;
}

const UrlPreview = ({ url }: IProps) => {
  if (!url) return null;

  return (
    <div
      className={cn(
        'content-absolute content-bottom-4 content-left-1/2 content-z-50 content-max-w-[90%] content--translate-x-1/2 content-truncate content-rounded-lg content-bg-black content-px-4 content-py-2 content-text-sm content-text-white',
      )}
      role="status"
    >
      {url}
    </div>
  );
};

export default UrlPreview;
