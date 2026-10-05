import { CloseIcon } from 'assets/icons';
import { IModalHeaderProps } from 'models/Modal';
import { cn } from 'utils/Helper';

const ModalHeader = ({
  onClose = () => {},
  closeButtonPosition = 'right',
  className = '',
  iconClassName = '',
  children,
}: IModalHeaderProps) => {
  return (
    <header
      className={cn(
        'modal-header',
        closeButtonPosition === 'left' ? 'left' : '',
      )}
    >
      <span className={cn('modal-header-content', className)}>{children}</span>

      <span className={cn('modal-close', iconClassName)} onClick={onClose}>
        <CloseIcon className="size-5" />
      </span>
    </header>
  );
};

export default ModalHeader;
