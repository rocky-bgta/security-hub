import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import { IChapter } from 'models/Chapter';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  data?: IChapter | null;
}

const ChapterViewModal = ({ isOpen, onClose, data }: IProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      disableOutsideClick={true}
      className="content-h-auto content-w-1/3"
    >
      <ModalHeader onClose={onClose}>
        <p className="content-py-5 content-text-lg content-font-medium">
          View Chapter
        </p>
      </ModalHeader>
      <ModalBody className="content-my-6 content-text-white">
        <div className="content-mb-5 content-text-2xl content-font-medium">
          {data?.chapterName}
        </div>
        <div className="content-text-cloudy-white">
          {data?.chapterDescription}
        </div>
      </ModalBody>
    </Modal>
  );
};

export default ChapterViewModal;
