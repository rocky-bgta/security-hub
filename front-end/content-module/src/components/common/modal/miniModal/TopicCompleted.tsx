import { CourseCompletedIcon } from 'assets/icons';
import { Button } from 'common/Button';
import UserMiniModal from './UserMiniModal';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  onClick?: () => void;
}

const TopicCompletedModal = ({ isOpen, onClose, onClick }: IProps) => {
  return (
    <UserMiniModal isOpen={isOpen} onClose={onClose}>
      <div className="content-flex content-flex-col content-items-center content-justify-center content-text-center">
        <CourseCompletedIcon width={384} height={179} />
        <p className="content-mt-6 content-text-2xl content-font-bold content-text-white">
          Topic completed successfully. Ready for the next topics?
        </p>
        <Button
          className="content-mt-9 content-rounded-full content-px-14"
          onClick={onClick}
        >
          Go to other topics
        </Button>
      </div>
    </UserMiniModal>
  );
};

export default TopicCompletedModal;
