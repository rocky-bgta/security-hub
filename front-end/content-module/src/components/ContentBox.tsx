import clsx from 'clsx';
import { Button } from 'common/Button';
import Border from 'components/UserBorder';
import { IContentBoxProps } from 'models/ContentBox';
import { FILE_PATH_PREFIX } from 'utils/Constants';
import { truncateText } from 'utils/Helper';

const ContentBox = ({
  title,
  status,
  description,
  image,
  onClick,
  isOpenView,
  isOpenEdit,
  viewOnly = false,
}: IContentBoxProps) => {
  return (
    <Border className="content-h-full content-p-2">
      <div className="content-flex content-h-full content-flex-col content-justify-between content-gap-4 content-overflow-hidden">
        <div
          onClick={viewOnly ? undefined : onClick}
          className={clsx(
            'content-relative content-shrink-0',
            !viewOnly && 'content-cursor-pointer',
          )}
        >
          {image ? (
            <img
              src={FILE_PATH_PREFIX + image}
              alt={title}
              className="content-h-56 content-w-full content-object-cover"
            />
          ) : (
            <div className="content-h-56 content-w-full content-bg-gray-200" />
          )}
          <div className="content-mt-2 content-flex content-items-start content-justify-between">
            <p className="content-text-white">{title}</p>
            {status && (
              <p
                className={clsx(
                  'content-w-fit content-rounded-md content-px-3 content-py-1 content-text-sm content-font-medium',
                  status === 'Enabled'
                    ? 'content-bg-success/10 content-text-success'
                    : 'content-bg-vibrant-red/10 content-text-vibrant-red',
                )}
              >
                {status}
              </p>
            )}
          </div>
        </div>
        <div className="content-grow">
          <p className="content-text-sm content-text-cloudy-white">
            {truncateText(description, 20)}
          </p>
        </div>
        <div
          className={clsx(
            'content-mb-1 content-flex',
            viewOnly ? 'content-w-full' : 'content-justify-between',
          )}
        >
          {!viewOnly && (
            <Button
              onClick={onClick}
              className="content-px-2 content-py-1 content-text-sm"
              variant="outline"
            >
              Chapter
            </Button>
          )}
          <Button
            onClick={isOpenView}
            size="sm"
            className={clsx(
              'content-px-2 content-py-1 content-text-sm',
              viewOnly && 'content-w-full',
            )}
            variant="outline"
          >
            View
          </Button>
          {!viewOnly && (
            <Button
              onClick={isOpenEdit}
              className="content-px-2 content-py-1 content-text-sm"
              variant="outline"
            >
              Edit
            </Button>
          )}
        </div>
      </div>
    </Border>
  );
};

export default ContentBox;
