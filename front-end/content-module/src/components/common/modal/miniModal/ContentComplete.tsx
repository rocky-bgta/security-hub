import { ContentCorrectIcon, ContentWrongIcon } from 'assets/icons';
import { Button } from 'common/Button';
import UserMiniModal from 'common/modal/miniModal/UserMiniModal';

interface IOptions {
  option: string;
  isCorrect: boolean;
}

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  onClick: () => void;
  isCorrect: boolean;
  isInteractive?: boolean;
  correctAnswer?: IOptions[];
  loading?: boolean;
}
const contentMap = {
  correct: (isInteractive: boolean) => ({
    icon: <ContentCorrectIcon width={133} height={109} />,
    title: isInteractive
      ? "Great job! That's the correct answer!"
      : "Great job! You've completed this Content.",
    description: isInteractive
      ? "You have successfully answered this question. Keep up the good work and continue to next when you're ready."
      : "You have successfully completed this content. Keep up the good work and continue to the next content when you're ready.",
    buttonText: isInteractive ? 'Continue Lesson' : 'Continue to Next Lesson',
  }),
  wrong: (correctAnswer: IOptions[] | undefined) => ({
    icon: <ContentWrongIcon width={133} height={109} />,
    title: "Oops! That's not the correct answer.",
    description: correctAnswer
      ? `Don't worry — the correct answer is "${correctAnswer?.map(item => item.option).join(', ')}".`
      : '',
    buttonText: 'Continue Lesson',
  }),
};

const ContentCompleteModal = ({
  isOpen,
  onClose,
  onClick,
  isCorrect,
  isInteractive = false,
  correctAnswer,
  loading,
}: IProps) => {
  const modalContent = isCorrect
    ? contentMap.correct(isInteractive)
    : contentMap.wrong(correctAnswer);

  return (
    <UserMiniModal isOpen={isOpen} onClose={onClose}>
      <div className="content-flex content-flex-col content-items-center content-justify-center content-text-center">
        {modalContent.icon}
        <p className="content-mb-3 content-mt-4 content-font-bold content-text-white">
          {modalContent.title}
        </p>
        <p className="content-text-sm content-text-white content-text-opacity-75">
          {modalContent.description}
        </p>
        <Button
          className="content-mt-4 content-rounded-full"
          onClick={onClick}
          disabled={loading}
        >
          {loading ? 'Loading...' : modalContent.buttonText}
        </Button>
      </div>
    </UserMiniModal>
  );
};

export default ContentCompleteModal;
