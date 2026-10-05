import { useState } from 'react';

import { CopyIcon } from 'assets/icons';
import { Button } from 'common/Button';
import { CopyText } from 'utils/Helper';

interface IProps {
  copyText: string;
}

const UserCopyTextButton = ({ copyText }: IProps) => {
  const [copied, setCopied] = useState<boolean>(false);
  return (
    <div className="content-relative content-inline-block">
      <Button
        variant="outline"
        className="content-w-full content-grow content-p-2"
        onClick={() => CopyText(copyText, setCopied)}
      >
        <CopyIcon fill="#37BE99" />
        Copy Link
      </Button>
      {copied && (
        <div className="content-animate-fade-in content-absolute content-bottom-full content-left-1/2 content-mb-3 content--translate-x-1/2 content-transform content-whitespace-nowrap content-rounded-lg content-bg-white content-px-4 content-py-2 content-text-base content-text-black content-shadow-xl">
          Copied
          <div className="content-absolute content-left-1/2 content-top-full content--translate-x-1/2 content-transform content-border-x-8 content-border-t-8 content-border-transparent content-border-t-white"></div>
        </div>
      )}
    </div>
  );
};

export default UserCopyTextButton;
