import { CheckCircle2, XCircle } from 'lucide-react';

import { cn } from 'utils/Helper';

import { IClassifyFeedback } from '../types';

interface IProps {
  feedback: IClassifyFeedback | null;
}

const ClassifyFeedback = ({ feedback }: IProps) => {
  if (!feedback) return null;

  return (
    <div
      className={cn(
        'content-absolute content-left-1/2 content-top-1/2 content-z-50 content-flex content-w-[min(90%,20rem)] content--translate-x-1/2 content--translate-y-1/2 content-flex-col content-items-center content-gap-2 content-rounded-lg content-border content-bg-black content-p-4 content-text-center',
        feedback.isCorrect
          ? 'content-border-primary content-text-primary'
          : 'content-border-vibrant-red content-text-vibrant-red',
      )}
      role="status"
      aria-live="polite"
    >
      {feedback.isCorrect ? (
        <CheckCircle2 className="content-size-8" />
      ) : (
        <XCircle className="content-size-8" />
      )}
      <p className="content-text-base content-font-semibold">{feedback.message}</p>
    </div>
  );
};

export default ClassifyFeedback;
