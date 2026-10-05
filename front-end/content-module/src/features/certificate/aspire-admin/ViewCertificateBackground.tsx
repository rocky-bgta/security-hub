import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  data: string;
}
const ViewCertificateBackgroundModal = ({ isOpen, onClose, data }: IProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      className="content-max-h-[80vh] content-w-1/2 content-overflow-y-scroll"
    >
      <ModalHeader onClose={onClose} className='content-py-3'>
        <h2 className="content-text-2xl content-text-white">Preview Background Image</h2>
      </ModalHeader>
      <ModalBody className="!content-p-0">
        <div className="content-flex content-size-full content-items-center content-justify-center content-p-3">
          <img
            src={data}
            alt="Certificate Background"
            className="content-size-full"
          />
        </div>
      </ModalBody>
    </Modal>
  );
};
export default ViewCertificateBackgroundModal;
