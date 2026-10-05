import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import { IFeatureDetails } from 'models/Feature';
import FeatureForm from './FeatureForm';

interface IFormModalProps {
  isOpen: boolean;
  onSubmit: () => void;
  onClose: () => void;
  featureId?: string;
  formData?: IFeatureDetails;
}

const FeatureModal = ({
  isOpen,
  onSubmit,
  onClose,
  formData,
  featureId,
}: IFormModalProps) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      disableOutsideClick={true}
      className="content-h-auto content-w-3/4"
      variant="user"
    >
      <ModalHeader onClose={onClose}>
        <p className="content-py-5 content-text-lg content-font-medium content-text-white">
          {featureId ? 'Edit Feature' : 'Create Feature'}
        </p>
      </ModalHeader>
      <ModalBody>
        <FeatureForm
          onClose={onClose}
          formData={formData}
          featureId={featureId}
          onSubmit={onSubmit}
        />
      </ModalBody>
    </Modal>
  );
};

export default FeatureModal;
