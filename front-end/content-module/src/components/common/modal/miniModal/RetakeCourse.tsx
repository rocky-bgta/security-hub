import { CourseRetakeIcon } from 'assets/icons';
import { Button } from 'common/Button';
import UserMiniModal from './UserMiniModal';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  retake: () => void;
  loading?: boolean;
}

const RetakeCourseModal = ({ isOpen, onClose, retake, loading }: IProps) => {
  const handleClose = () => onClose();

  return (
    <UserMiniModal isOpen={isOpen} onClose={onClose}>
      <div className="content-flex content-flex-col content-items-center content-justify-center content-text-center">
        <CourseRetakeIcon width={112} height={98} />
        <p className="content-mt-3 content-text-2xl content-font-bold content-text-white">
          Confirm Retake
        </p>
        <p className="content-text-white">
          Are you sure you want to retake the course? Your progress will be
          reset, and you will need to complete all topics again.
        </p>
        <Button
          onClick={handleClose}
          variant="destructive"
          className="content-mt-8 content-w-3/4 content-rounded-full"
        >
          Cancel
        </Button>
        <Button
          onClick={retake}
          className="content-mt-4 content-w-3/4 content-rounded-full"
          disabled={false}
        >
          {loading ? 'Retaking...' : 'Yes, Retake Course'}
        </Button>
      </div>
    </UserMiniModal>
  );
};

export default RetakeCourseModal;
