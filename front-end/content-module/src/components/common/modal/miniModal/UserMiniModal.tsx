import { ReactNode } from 'react';

import { CloseIcon } from 'assets/icons';
import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import { PUBLIC_URL } from 'utils/Constants';

const bgLine = PUBLIC_URL + '/images/bg-line.svg';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  children?: ReactNode;
}

const UserMiniModal = ({ isOpen, onClose, children }: IProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      variant="user"
      disableOutsideClick={true}
      className="content-h-auto content-w-1/4 content-bg-gradient-to-b content-from-[#324650] content-to-[#12151E]"
    >
      <ModalBody className="!content-px-0">
        <div
          style={{ backgroundImage: `url(${bgLine})` }}
          className="content-p-5"
        >
          <div className="content-flex content-justify-end">
            <CloseIcon
              className="content-cursor-pointer content-text-white"
              onClick={onClose}
            />
          </div>
          {children}
        </div>
      </ModalBody>
    </Modal>
  );
};

export default UserMiniModal;
