import { CourseCompletedIcon } from 'assets/icons';
import { Button } from 'common/Button';
import UserMiniModal from './UserMiniModal';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  isNavigatingToCourse: () => void;
}

const PhishingTrainingCompleteModal = ({
  isOpen,
  onClose,
  isNavigatingToCourse,
}: IProps) => {
  return (
    <UserMiniModal isOpen={isOpen} onClose={onClose}>
      <div className="content-flex content-flex-col content-items-center content-justify-center content-text-center">
        <CourseCompletedIcon width={384} height={179} />
        <p className="content-mt-6 content-text-center content-text-2xl content-font-bold content-text-white">
          Congratulations! <br /> Phishing training completed successfully.
        </p>
        <Button
          className="content-mt-9 content-rounded-full content-px-14"
          onClick={isNavigatingToCourse}
        >
          Go to Course
        </Button>
      </div>
    </UserMiniModal>
  );
};

export default PhishingTrainingCompleteModal;
