import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import InfoViewCard from 'components/InfoViewCard';
import { Status } from 'models/Global';
import { HumanizeDate } from 'utils/Helper';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  data?: any;
}

const FeatureViewModal = ({ isOpen, onClose, data }: IProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      disableOutsideClick={true}
      className="content-h-auto content-w-3/5"
      variant="user"
    >
      <ModalHeader onClose={onClose}>
        <p className="content-py-5 content-text-lg content-font-medium content-text-white">
          View Feature
        </p>
      </ModalHeader>
      <ModalBody className="content-my-6">
        <InfoViewCard
          title={data?.featureName}
          description={data?.featureDescription}
          createdAt={HumanizeDate(data?.createdAt)}
          updatedAt={HumanizeDate(data?.updatedAt)}
          status={data?.featureStatus === Status.ENABLED}
          availability={String(data?.availability)?.toLowerCase()}
        />
      </ModalBody>
    </Modal>
  );
};

export default FeatureViewModal;
