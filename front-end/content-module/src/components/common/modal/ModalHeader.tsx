import clsx from 'clsx';
import { FC } from 'react';

import { CloseIcon } from 'assets/icons';
import { IModalHeaderProps } from 'models/Modal';

const ModalHeader: FC<IModalHeaderProps> = ({
  onClose = () => {},
  closeButtonPosition = 'right',
  className = '',
  iconClassName = '',
  children = null,
}) => {
  return (
    <header
      className={clsx(
        'content-modal-header',
        closeButtonPosition === 'left' ? 'content-left' : '',
      )}
    >
      <span className={clsx('content-modal-header-content', className)}>
        {children}
      </span>

      <span
        className={clsx('content-modal-close', iconClassName)}
        onClick={onClose}
      >
        <CloseIcon className="content-size-5 content-text-white" />
      </span>
    </header>
  );
};

export default ModalHeader;
