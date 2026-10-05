import clsx from 'clsx';
import { FC, MouseEvent, RefObject, useRef } from 'react';

import useDisableScroll from 'hooks/UseDisableScroll';
import { IModalProps } from 'models/Modal';
import 'styles/modal.css';

const Modal: FC<IModalProps> = ({
  className = 'content-h-3/4',
  disableOutsideClick = false,
  isOpen = false,
  onClose,
  children,
  animation = 'zoom',
  variant = 'user',
}) => {
  const refModal = useRef<HTMLDivElement | null>(
    null,
  ) as RefObject<HTMLDivElement>;

  useDisableScroll(isOpen);

  const handleModalClick = (event: MouseEvent<HTMLDivElement>) => {
    if (!isOpen || disableOutsideClick) return;
    if (refModal?.current.contains(event.target as Node)) {
      return;
    }
    onClose();
  };

  return (
    <div
      className={clsx(
        variant == 'default' ? 'content-modal-bg' : 'content-user-modal-bg',
        'content-modal',
        isOpen ? 'content-modal-visible' : 'content-modal-hidden',
      )}
      onClick={handleModalClick}
    >
      <div
        className={clsx(
          `content-modal-content content-modal-animate-${animation}`,
          className,
          variant == 'default'
            ? ''
            : 'content-bg-[linear-gradient(180deg,_#324650_0%,_#12151E_100%)]',
        )}
        ref={refModal}
      >
        {children}
      </div>
    </div>
  );
};

export default Modal;
