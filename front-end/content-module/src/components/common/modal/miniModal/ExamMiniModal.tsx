import clsx from 'clsx';
import { Button } from 'common/Button';
import { PUBLIC_URL } from 'utils/Constants';
import Modal from '../Modal';
import ModalBody from '../ModalBody';

const bgLine = PUBLIC_URL + '/images/bg-line.svg';

interface IProps {
  isOpen: boolean;
  onClose: () => void;
  isCorrect: boolean;
  handleNextQuestion: () => void;
  isLastStep: boolean;
  loading: boolean;
}

const contentMap = {
  correct: {
    title: 'Correct Answer!',
    description:
      'Well done — that’s the right choice. Keep up the great work and move on to the next question.',
    buttonText: 'Next Question',
  },
  wrong: {
    title: 'Oops! Wrong Answer.',
    description:
      'That’s not the correct answer. Review the concept and try to do better on the next one.',
    buttonText: 'Try Next Question',
  },
};

const ExamMiniModal = ({
  isOpen,
  onClose,
  isCorrect,
  handleNextQuestion,
  isLastStep,
  loading,
}: IProps) => {
  const modalContent = isCorrect ? contentMap.correct : contentMap.wrong;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      variant="user"
      disableOutsideClick={true}
      className="content-h-auto content-w-1/4 content-bg-gradient-to-b content-from-[#324650] content-to-[#12151E]"
    >
      <ModalBody className="!content-px-0">
        <div
          style={{ backgroundImage: `url(${bgLine})` }}
          className="content-p-5 content-text-center"
        >
          <p
            className={clsx(
              isCorrect ? 'content-text-primary' : 'content-text-vibrant-red',
              'content-font-bold',
            )}
          >
            {modalContent.title}
          </p>
          <p className="content-mb-6 content-mt-4 content-text-sm content-text-white content-text-opacity-75">
            {modalContent.description}
          </p>
          <Button
            className="content-mx-auto content-rounded-full"
            onClick={() => {
              if (!isLastStep) onClose();
              handleNextQuestion();
            }}
          >
            {isLastStep
              ? loading
                ? 'Submitting Answer...'
                : 'Submit Answer'
              : modalContent.buttonText}
          </Button>
        </div>
      </ModalBody>
    </Modal>
  );
};

export default ExamMiniModal;
