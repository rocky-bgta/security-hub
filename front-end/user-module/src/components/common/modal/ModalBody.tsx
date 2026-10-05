import { IModalBodyProps } from 'models/Modal';
import { cn } from 'utils/Helper';

const ModalBody = ({ className = '', children }: IModalBodyProps) => {
  return <div className={cn('modal-body', className)}>{children}</div>;
};

export default ModalBody;
