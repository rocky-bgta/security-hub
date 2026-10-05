import { ReactNode } from 'react';

import { Button } from 'common/Button';
import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';

interface IProps {
  isOpen: boolean;
  loading?: boolean;
  loadingText?: string;
  buttonText?: string;
  onClose: () => void;
  onSubmit: () => void;
  children?: ReactNode;
}

const ContentSettingsModal = ({
  isOpen,
  loading = false,
  loadingText = 'Updating...',
  buttonText = 'Update',
  onClose,
  onSubmit,
  children,
}: IProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      disableOutsideClick={true}
      variant="user"
      className="content-m-0 content-ml-auto content-h-screen content-w-[520px] content-overflow-y-auto content-overflow-x-hidden"
      animation="right"
    >
      <ModalHeader
        onClose={onClose}
        className="content-px-6 content-py-4 content-text-xl content-font-semibold content-text-white"
      >
        Block Settings
      </ModalHeader>
      <ModalBody className="content-py-10">
        {children}
        <Button
          className="content-mt-5 content-w-full"
          onClick={onSubmit}
          disabled={loading}
        >
          {loading ? loadingText : buttonText}
        </Button>
      </ModalBody>
    </Modal>
  );
};

export default ContentSettingsModal;
