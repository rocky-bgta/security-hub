import { MouseEvent, RefObject, useRef } from 'react';

import { IModalProps } from 'models/Modal';
import { cn } from 'utils/Helper';

import 'styles/modal.css';

const Modal = ({
  className = 'h-3/4',
  disableOutsideClick = false,
  isOpen = false,
  onClose,
  children,
  animation = 'zoom',
}: IModalProps) => {
  const refModal = useRef<HTMLDivElement | null>(
    null,
  ) as RefObject<HTMLDivElement>;

  const handleModalClick = (event: MouseEvent<HTMLDivElement>) => {
    if (!isOpen || disableOutsideClick) return;
    if (refModal?.current.contains(event.target as Node)) {
      return;
    }
    onClose();
  };

  return (
    <div
      className={cn('modal', isOpen ? 'modal-visible' : 'modal-hidden')}
      onClick={handleModalClick}
    >
      <div
        className={cn(`modal-content modal-animate-${animation}`, className)}
        ref={refModal}
      >
        {children}
      </div>
    </div>
  );
};

export default Modal;
