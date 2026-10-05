import clsx from 'clsx';
import { FC } from 'react';

import { IModalBodyProps } from 'models/Modal';

const ModalBody: FC<IModalBodyProps> = ({ className = '', children }) => {
  return (
    <div className={clsx('content-modal-body', className)}>{children}</div>
  );
};

export default ModalBody;
