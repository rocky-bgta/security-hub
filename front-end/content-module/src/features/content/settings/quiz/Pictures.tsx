import {
  ChangeEvent,
  forwardRef,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';
import { v4 as uuidv4 } from 'uuid';

import { Button } from 'common/Button';
import { Checkbox } from 'common/Checkbox';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import InputCard from 'components/InputCard';
import { IDataValidationHandle, IQuizFields } from 'models/Content';
import { FILE_PATH_PREFIX, ValidImageFormats } from 'utils/Constants';

interface IProps {
  data: IQuizFields;
  updateData: (data: IQuizFields) => void;
}

const PicturesQuiz = forwardRef<IDataValidationHandle, IProps>(
  ({ data, updateData }, ref) => {
    const fileUploaderRef = useRef<FileUploaderHandle>(null);

    const [option, setOption] = useState<File | null>(null);
    const [errors, setErrors] = useState<{
      question: string;
      options: string;
    }>({
      question: '',
      options: '',
    });

    const question = data?.question ?? '';
    const options = data?.options ?? [];

    const handleChangeQuestion = (e: ChangeEvent<HTMLInputElement>) => {
      const { value } = e.target;

      updateData({
        ...data,
        question: value,
      });
      if (value.trim().length === 0) {
        setErrors({ ...errors, question: 'Question is required' });
      } else {
        setErrors({ ...errors, question: '' });
      }
    };

    const handleUpdateFile = (files: FileList) => {
      if (files.length === 0) return;
      setOption(files[0]);
    };

    const handleRemoveOptionImage = () => {
      setOption(null);
      fileUploaderRef.current?.clearFiles();
    };

    const handleAddOption = () => {
      if (option) {
        updateData({
          ...data,
          options: [
            ...options,
            {
              id: uuidv4(),
              optionText: '',
              optionImage: option,
              optionImageLink: '',
              isCorrect: false,
            },
          ],
        });

        setOption(null);
        fileUploaderRef.current?.clearFiles();
      }
    };

    const handleToggleCorrect = (id: string) => {
      const updatedOptions = options.map(option => {
        if (option.id === id) {
          return {
            ...option,
            isCorrect: !option.isCorrect,
          };
        }
        return option;
      });

      updateData({
        ...data,
        options: updatedOptions,
      });
    };

    const handleRemoveOption = (id: string) => {
      const updatedOptions = options.filter(opt => opt.id !== id);
      updateData({
        ...data,
        options: updatedOptions,
      });

      setErrors({
        ...errors,
        options:
          updatedOptions.length === 0 ? 'At least one option is required' : '',
      });
    };

    const validateFields = () => {
      let isValid = true;

      if (question.trim().length === 0) {
        setErrors(prev => ({ ...prev, question: 'Question is required' }));
        isValid = false;
      } else {
        setErrors(prev => ({ ...prev, question: '' }));
      }

      if (options.length === 0) {
        setErrors(prev => ({
          ...prev,
          options: 'At least one option is required',
        }));
        isValid = false;
      } else {
        const hasCorrectOption = options.some(opt => opt.isCorrect);
        if (!hasCorrectOption) {
          setErrors(prev => ({
            ...prev,
            options: 'At least one option must be correct',
          }));
          isValid = false;
        } else {
          setErrors(prev => ({ ...prev, options: '' }));
        }
      }

      return isValid;
    };

    useImperativeHandle(ref, () => ({
      validateData: () => {
        return { success: validateFields() };
      },
    }));

    return (
      <div className="content-mt-5">
        <InputCard
          title="Question Title"
          inputFields={[
            {
              className:
                'content-my-2 content-w-full content-rounded content-border content-px-3 content-py-2',
              type: 'text',
              id: 'question',
              value: question,
              error: errors.question,
              placeholder: 'Enter question title',
              onChange: handleChangeQuestion,
            },
          ]}
        />
        <div className="content-mt-5">
          <InputCard title="Option" inputFields={[]}>
            <div className="content-flex content-gap-2">
              <div className="content-w-3/4">
                {option ? (
                  <>
                    <img
                      src={URL.createObjectURL(option)}
                      alt="Option"
                      className="content-h-auto content-w-32"
                    />
                    <Button
                      size="sm"
                      className="content-cursor-pointer !content-bg-transparent content-p-0 content-text-vibrant-red content-underline"
                      onClick={handleRemoveOptionImage}
                    >
                      Remove
                    </Button>
                  </>
                ) : (
                  <FileUploader
                    ref={fileUploaderRef}
                    id="optionImage"
                    accept={ValidImageFormats.join(',')}
                    placeholder="Upload image"
                    onUpload={handleUpdateFile}
                  />
                )}
              </div>
              <Button
                onClick={handleAddOption}
                size="sm"
                className="content-h-max content-w-1/4"
              >
                Add Option
              </Button>
            </div>
            {options.length > 0 && (
              <>
                <p className="content-border-b content-p-2">Options</p>
                {options.map(item => (
                  <div
                    key={item.id}
                    className="content-mb-1 content-flex content-items-center content-justify-between content-rounded content-bg-slate-100 content-p-2"
                  >
                    <div className="content-flex content-items-center content-gap-3">
                      <label className="content-flex content-items-center content-gap-2 content-text-base content-font-normal">
                        <Checkbox
                          checked={item.isCorrect}
                          onCheckedChange={() => handleToggleCorrect(item.id!)}
                        />
                        {item.optionText}
                      </label>
                      {(item.optionImageLink || item.optionImage) && (
                        <img
                          src={
                            item.optionImage
                              ? URL.createObjectURL(item.optionImage)
                              : FILE_PATH_PREFIX + item.optionImageLink
                          }
                          alt="Option"
                          className="content-h-auto content-w-1/2"
                        />
                      )}
                      <p className="content-flex content-items-center content-gap-2 content-text-base content-font-normal">
                        {item.optionText}
                      </p>
                    </div>
                    <div className="content-flex content-items-center content-gap-2">
                      <Button
                        size="sm"
                        className="content-cursor-pointer !content-bg-transparent content-p-0 content-text-vibrant-red content-underline"
                        onClick={() => handleRemoveOption(item.id)}
                      >
                        Remove
                      </Button>
                    </div>
                  </div>
                ))}
              </>
            )}
            {errors.options && (
              <p className="content-mt-1 content-text-sm content-text-red-500">
                {errors.options}
              </p>
            )}
          </InputCard>
        </div>
      </div>
    );
  },
);

PicturesQuiz.displayName = 'PicturesQuiz';

export default PicturesQuiz;
