import { CourseCompletedIcon } from 'assets/icons';
import { Button } from 'common/Button';
import UserMiniModal from './UserMiniModal';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  retake: () => void;
  takeExam: () => void | Promise<void>;
  isNavigatingToExam?: boolean;
}

const CourseCompletedModal = ({
  isOpen,
  onClose,
  takeExam,
  retake,
  isNavigatingToExam = false,
}: IProps) => {
  return (
    <UserMiniModal isOpen={isOpen} onClose={onClose}>
      <div className="content-flex content-flex-col content-items-center content-justify-center content-text-center">
        <CourseCompletedIcon width={384} height={179} />
        <p className="content-mt-6 content-text-center 2xl:content-text-2xl content-text-xl content-font-bold content-text-white">
          Course Completed <br /> You have successfully completed this course.
          Select an option below to continue.
        </p>
        <Button
          className="content-mt-9 content-rounded-full content-px-14"
          onClick={takeExam}
          disabled={isNavigatingToExam}
        >
          {isNavigatingToExam ? 'Loading assessment...' : 'Go to Assessment'}
        </Button>
        <Button
          onClick={retake}
          className="content-mt-4 hover:content-underline"
        >
          Retake Course
        </Button>
      </div>
    </UserMiniModal>
  );
};

export default CourseCompletedModal;
