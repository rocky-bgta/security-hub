import { RefObject } from 'react';

import { Button } from 'common/Button';
import { IContent, TContent } from 'models/Content';

interface IProps {
  contents: Array<IContent<TContent>>;
  sectionRefs: RefObject<Array<HTMLDivElement>>;
}

const ContentsIndex = ({ contents, sectionRefs }: IProps) => {
  const handleClick = (index: number) => {
    sectionRefs.current[index]?.scrollIntoView({
      behavior: 'smooth',
    });
  };

  return (
    <div className="content-sticky content-left-0 content-top-20 content-h-[92vh] content-w-1/5 content-overflow-y-auto content-border-r content-border-card-border content-scrollbar-hide">
      {contents?.map((content, index) => {
        const words = content?.common?.contentName.split(' ');
        const contentName =
          words.slice(0, 12).join(' ') + (words.length > 12 ? '...' : '');
        const contentType = content?.common?.contentType
          .toLowerCase()
          .split('_')
          .join(' ');

        return (
          <Button
            key={content.id}
            className="content-h-auto content-w-full content-flex-col !content-items-start !content-bg-transparent !content-p-0 content-text-start content-capitalize content-text-secondary"
            onClick={() => handleClick(index)}
          >
            <h3 className="content-w-full content-cursor-pointer content-bg-white content-bg-opacity-25 content-px-4 content-py-2 content-text-sm content-text-white">
              {contentType} Based
            </h3>
            <p className="content-pb-2 content-pl-10 content-pr-1 content-text-cloudy-white">
              {contentName}
            </p>
          </Button>
        );
      })}
    </div>
  );
};

export default ContentsIndex;
