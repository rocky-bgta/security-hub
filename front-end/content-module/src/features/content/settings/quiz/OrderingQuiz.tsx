import {
  ChangeEvent,
  DragEvent,
  forwardRef,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';
import { FaBars } from 'react-icons/fa';
import { v4 as uuidv4 } from 'uuid';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import InputCard from 'components/InputCard';
import { IDataValidationHandle, IQuizFields } from 'models/Content';
import { FILE_PATH_PREFIX, ValidImageFormats } from 'utils/Constants';

interface IProps {
  data: IQuizFields;
  updateData: (data: IQuizFields) => void;
}

const OrderingQuiz = forwardRef<IDataValidationHandle, IProps>(
  ({ data, updateData }, ref) => {
    const fileUploaderRef = useRef<FileUploaderHandle>(null);

    const [option, setOption] = useState<{
      text: string;
      file: File | null;
    }>({
      text: '',
      file: null,
    });
    const [errors, setErrors] = useState<{
      question: string;
      options: string;
    }>({
      question: '',
      options: '',
    });

    const question = data?.question || '';
    const options = data?.options || [];

    const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
      const { id, value } = e.target;

      if (id === 'option') {
        setOption({ text: value, file: null });
        return;
      }

      updateData({
        ...data,
        question: value,
      });

      setErrors({
        ...errors,
        question: value.trim().length === 0 ? 'Question is required' : '',
      });
    };

    const handleUpdateFile = (files: FileList) => {
      if (files.length === 0) return;
      setOption({ text: '', file: files[0] });
    };

    const handleRemoveOptionImage = () => {
      setOption({ ...option, file: null });
      fileUploaderRef.current?.clearFiles();
    };

    const handleAddOption = () => {
      if (!!option?.text.trim().length || !!option?.file) {
        updateData({
          ...data,
          options: [
            ...options,
            {
              id: uuidv4(),
              optionText: option?.text.trim(),
              optionImage: option?.file,
              index: options.length,
              optionImageLink: '',
            },
          ],
        });

        setOption({ text: '', file: null });
        fileUploaderRef.current?.clearFiles();
      }
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
          updatedOptions.length > 1 ? '' : 'At least two options are required',
      });
    };

    const handleDragStart = (e: DragEvent, index: number) => {
      e.dataTransfer.setData('draggedItemIndex', index.toString());
    };

    const handleDragOver = (e: DragEvent) => {
      e.preventDefault();
    };

    const handleDrop = (e: DragEvent, targetIndex: number) => {
      e.preventDefault();

      const draggedIndex = parseInt(
        e.dataTransfer.getData('draggedItemIndex'),
        10,
      );

      if (draggedIndex === targetIndex) return;

      const reorderedOptions = [...options];
      const [draggedItem] = reorderedOptions.splice(draggedIndex, 1);
      reorderedOptions.splice(targetIndex, 0, draggedItem);

      updateData({
        ...data,
        options: reorderedOptions,
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

      if (options.length < 1) {
        setErrors(prev => ({
          ...prev,
          options: 'At least two options are required',
        }));
        isValid = false;
      } else {
        setErrors(prev => ({ ...prev, options: '' }));
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
              onChange: handleChangeInput,
            },
          ]}
        />
        <div className="content-mt-5">
          <InputCard title="Option" inputFields={[]}>
            <div>
              <div className="content-flex content-gap-2">
                <Input
                  id="option"
                  value={option.text}
                  placeholder="Enter option"
                  className="content-w-2/3 content-rounded content-border content-px-3 content-py-2"
                  onChange={handleChangeInput}
                />
                <Button
                  onClick={handleAddOption}
                  size="sm"
                  className="content-w-1/3"
                >
                  Add Option
                </Button>
              </div>
              <div className="content-mt-5 content-flex content-gap-2">
                {option.file ? (
                  <>
                    <img
                      src={URL.createObjectURL(option.file)}
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
            </div>

            {options.length > 0 && (
              <div>
                <p className="content-mb-3 content-border-b content-p-2">
                  Options
                </p>
                <div className="content-space-y-2">
                  {options.map((item, index) => (
                    <div
                      key={item.id}
                      className="content-mb-1 content-flex content-cursor-move content-items-center content-justify-between content-rounded content-border content-border-soft-blue-gray content-p-2"
                      draggable
                      onDragStart={e => handleDragStart(e, index)}
                      onDragOver={handleDragOver}
                      onDrop={e => handleDrop(e, index)}
                    >
                      <div className="content-flex content-items-center content-gap-3">
                        <FaBars />
                        {(item.optionImageLink || item.optionImage) && (
                          <img
                            src={
                              item.optionImage
                                ? URL.createObjectURL(item.optionImage)
                                : FILE_PATH_PREFIX + item.optionImageLink
                            }
                            alt="Option"
                            className="content-size-10"
                          />
                        )}
                        <p className="content-flex content-items-center content-gap-2 content-text-base content-font-normal">
                          {item.optionText}
                        </p>
                      </div>
                      <div className="content-flex content-items-center content-gap-2">
                        <Button
                          size="sm"
                          className="content-cursor-pointer content-bg-transparent content-p-0 content-text-vibrant-red content-underline hover:!content-bg-transparent"
                          onClick={() => handleRemoveOption(item.id)}
                        >
                          Remove
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
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

export default OrderingQuiz;
