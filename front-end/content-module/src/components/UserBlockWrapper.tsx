import { ReactNode } from 'react';

import { IContent, TContent } from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface IProps {
  content: IContent<TContent>;
  children: ReactNode;
}

const UserBlockWrapper = ({ content, children }: IProps) => {
  return (
    <div className="content-relative content-size-full content-rounded">
      {content?.specific?.backgroundFormatting?.backgroundImage && (
        <div
          style={{
            backgroundImage: `url(${FILE_PATH_PREFIX + content.specific.backgroundFormatting.backgroundImage})`,
            opacity:
              content?.specific?.backgroundFormatting?.backgroundOpacity / 100,
          }}
          className="content-pointer-events-none content-absolute content-inset-0 content-z-0 content-bg-cover content-bg-center"
        />
      )}

      <div
        className="content-z-1 content-pointer-events-none content-absolute content-inset-0"
        style={{
          backgroundColor:
            content?.specific?.backgroundFormatting?.backgroundColor,
        }}
      />

      <div className="content-z-2 content-relative content-h-full content-overflow-auto">
        {children}
      </div>
    </div>
  );
};

export default UserBlockWrapper;
