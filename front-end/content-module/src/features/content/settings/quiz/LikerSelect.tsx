import clsx from 'clsx';
import {
  ChangeEvent,
  forwardRef,
  useEffect,
  useImperativeHandle,
  useState,
} from 'react';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import InputCard from 'components/InputCard';
import {
  IDataValidationHandle,
  IQuizFields,
  LikerSelectOptionIconMapper,
} from 'models/Content';

interface IProps {
  data: IQuizFields;
  updateData: (data: IQuizFields) => void;
}

const LikerSelect = forwardRef<IDataValidationHandle, IProps>(
  ({ data, updateData }, ref) => {
    const [error, setError] = useState<string>('');

    const question = data?.question ?? '';
    const options = data?.options?.length
      ? data?.options
      : [
          {
            index: 1,
            id: '1',
            optionText: 'Strongly Disagree',
            optionImage: null,
            optionImageLink: 'strongly-disagree',
          },
          {
            index: 2,
            id: '2',
            optionText: 'Somewhat Disagree',
            optionImage: null,
            optionImageLink: 'somewhat-disagree',
          },
          {
            index: 3,
            id: '3',
            optionText: 'Neither Disagree or Agree',
            optionImage: null,
            optionImageLink: 'neither-disagree-agree',
          },
          {
            index: 4,
            id: '4',
            optionText: 'Somewhat Agree',
            optionImage: null,
            optionImageLink: 'somewhat-agree',
          },
          {
            index: 5,
            id: '5',
            optionText: 'Strongly Agree',
            optionImage: null,
            optionImageLink: 'strongly-agree',
          },
        ];

    useEffect(() => {
      updateData({
        ...data,
        options: options,
      });
    }, [options]);

    const handleChangeQuestion = (e: ChangeEvent<HTMLInputElement>) => {
      const { value } = e.target;
      updateData({
        ...data,
        question: value,
      });
      setError(value.trim().length > 0 ? '' : 'Question is required');
    };

    const handleChangeOptionText = (value: string, index: number) => {
      const updatedOptions = [...options];
      updatedOptions[index] = {
        ...updatedOptions[index],
        optionText: value,
      };

      updateData({
        ...data,
        options: updatedOptions,
      });
    };

    const handleClearOptionText = (index: number) => {
      const updatedOptions = [...options];
      updatedOptions[index] = {
        ...updatedOptions[index],
        optionText: '',
      };

      updateData({
        ...data,
        options: updatedOptions,
      });
    };

    const validateFields = () => {
      let isValid = true;

      if (question.trim().length === 0) {
        setError('Question is required');
        isValid = false;
      }

      options.forEach(item => {
        if (item.optionText.trim().length === 0) {
          isValid = false;
        }
      });

      return isValid;
    };

    useImperativeHandle(ref, () => ({
      validateData: () => {
        return { success: validateFields() };
      },
    }));

    return (
      <div className="content-mt-5 content-flex content-flex-col content-gap-5">
        <InputCard
          title="Question"
          inputFields={[
            {
              className:
                'content-my-2 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'question',
              value: question,
              error: error,
              placeholder: 'Type your question here',
              onChange: handleChangeQuestion,
            },
          ]}
        />

        <InputCard title="Option" inputFields={[]}>
          {options.map((item, index) => {
            const IconComponent =
              LikerSelectOptionIconMapper[
                item.optionImageLink as keyof typeof LikerSelectOptionIconMapper
              ];

            return (
              <div
                key={index}
                className="content-flex content-grow content-flex-col content-items-start content-gap-1"
              >
                <div className="content-flex content-w-full content-items-center content-gap-3">
                  <IconComponent />
                  <Input
                    id={`option-${index}`}
                    type="text"
                    className={clsx({
                      'content-has-error': !item.optionText.trim().length,
                    })}
                    value={item.optionText}
                    onChange={e =>
                      handleChangeOptionText(e.target.value, index)
                    }
                    placeholder="Enter option text"
                  />
                  <Button
                    size="sm"
                    className="content-ml-3 content-cursor-pointer !content-bg-transparent content-p-0 content-text-gray-400 hover:content-underline"
                    onClick={() => handleClearOptionText(index)}
                  >
                    Clear
                  </Button>
                </div>
                {!item.optionText.trim().length && (
                  <p className="content-ml-9 content-text-sm content-text-red-500">
                    Please enter text for this option
                  </p>
                )}
              </div>
            );
          })}
        </InputCard>
      </div>
    );
  },
);

export default LikerSelect;
