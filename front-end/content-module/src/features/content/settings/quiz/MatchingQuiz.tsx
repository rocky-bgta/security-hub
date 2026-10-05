/* eslint-disable react-hooks/refs */
import {
  ChangeEvent,
  createRef,
  forwardRef,
  Fragment,
  RefObject,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from 'react';
import { v4 as uuidv4 } from 'uuid';

import { DeleteIcon } from 'assets/icons';
import { Button } from 'common/Button';
import { Card } from 'common/Card';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import FileUploader, { FileUploaderHandle } from 'components/FileUploader';
import InputCard from 'components/InputCard';
import {
  IDataValidationHandle,
  IQuizFields,
  MatchingQuizTypes,
} from 'models/Content';
import { FILE_PATH_PREFIX, ValidImageFormats } from 'utils/Constants';
import { MatchingQuizDefault } from 'utils/ContentDefaultValues';
import { cn } from 'utils/Helper';

enum SIDES {
  LEFT = 'Left',
  RIGHT = 'Right',
}

interface IProps {
  data: IQuizFields;
  updateData: (data: IQuizFields) => void;
}

const MatchingQuiz = forwardRef<IDataValidationHandle, IProps>(
  ({ data, updateData }, ref) => {
    const imageUploaderRefs = useRef<
      Array<{
        left: RefObject<FileUploaderHandle | null>;
        right: RefObject<FileUploaderHandle | null>;
      }>
    >([]);

    const [fieldErrors, setFieldErrors] = useState<
      Array<{
        left: string;
        right: string;
      }>
    >([]);
    const [errors, setErrors] = useState<{
      question: string;
      labelLeft: string;
      labelRight: string;
      pairs: string;
    }>({
      question: '',
      labelLeft: '',
      labelRight: '',
      pairs: '',
    });

    const question = data?.question || '';
    const labelLeft = data?.labelLeft || '';
    const labelRight = data?.labelRight || '';
    const pairs = data?.pairs ?? [];

    useEffect(() => {
      imageUploaderRefs.current =
        data.pairs?.map(() => ({
          left: createRef<FileUploaderHandle | null>(),
          right: createRef<FileUploaderHandle | null>(),
        })) ?? [];
    }, [data?.pairs?.length]);

    useEffect(() => {
      if (data) {
        setFieldErrors(
          data.pairs?.map(() => ({
            left: '',
            right: '',
          })) ?? [],
        );
      }
    }, [data]);

    const handleAddPair = () => {
      const updatedPairs = [
        ...pairs,
        {
          ...MatchingQuizDefault,
          id: uuidv4(),
        },
      ];

      setFieldErrors([
        ...fieldErrors,
        {
          left: '',
          right: '',
        },
      ]);
      setErrors(prev => ({ ...prev, pairs: '' }));
      updateData({ ...data, pairs: updatedPairs });
    };

    const handleDeletePair = (index: number) => {
      const updatedPairs = [...pairs.filter((_, i) => i !== index)];

      setErrors(prev => ({
        ...prev,
        pairs: updatedPairs.length === 0 ? 'At least one pair is required' : '',
      }));
      updateData({ ...data, pairs: updatedPairs });
    };

    const handleChangeInputType = (
      index: number,
      side: SIDES,
      type: MatchingQuizTypes,
    ) => {
      const updatedPairs = [...pairs];
      if (side === SIDES.LEFT) {
        updatedPairs[index].inputTypeLeft = type;
        updatedPairs[index].valueLeft = '';
        updatedPairs[index].linkLeft = '';
        updatedPairs[index].fileLeft = null;
        updatedPairs[index].objectURLLeft = '';
      } else {
        updatedPairs[index].inputTypeRight = type;
        updatedPairs[index].valueRight = '';
        updatedPairs[index].linkRight = '';
        updatedPairs[index].fileRight = null;
        updatedPairs[index].objectURLRight = '';
      }

      const updatedErrors = [...fieldErrors];
      if (side === SIDES.LEFT) {
        updatedErrors[index].left = '';
      } else {
        updatedErrors[index].right = '';
      }

      setFieldErrors(updatedErrors);
      updateData({ ...data, pairs: updatedPairs });
    };

    const handleChangeInput = (e: ChangeEvent<HTMLInputElement>) => {
      const { value } = e.target,
        [id, side] = e.target.id.split('_');

      if (['question', 'labelLeft', 'labelRight'].includes(id)) {
        updateData({ ...data, [id]: value });
        setErrors(prev => ({
          ...prev,
          [id]:
            value.trim().length === 0
              ? `${
                  id === 'labelLeft'
                    ? 'Left Column Label'
                    : id === 'labelRight'
                      ? 'Right Column Label'
                      : 'Question'
                } is required`
              : '',
        }));
        return;
      }

      const updatedErrors = [...fieldErrors];
      const updatedPairs = [
        ...pairs.map((item, idx) => {
          if (item.id === id) {
            if (side === SIDES.LEFT) {
              if (value.trim() === '') {
                updatedErrors[idx].left = 'Answer is required';
              } else {
                updatedErrors[idx].left = '';
              }

              return {
                ...item,
                valueLeft: value,
              };
            }

            if (value.trim() === '') {
              updatedErrors[idx].right = 'Answer is required';
            } else {
              updatedErrors[idx].right = '';
            }
            return {
              ...item,
              valueRight: value,
            };
          }
          return item;
        }),
      ];

      setFieldErrors(updatedErrors);
      updateData({ ...data, pairs: updatedPairs });
    };

    const handleFileUpload = (files: FileList, index: number, side: SIDES) => {
      if (files.length === 0) return;

      const updatedPairs = [
        ...pairs.map((item, idx) => {
          if (idx === index) {
            if (side === SIDES.LEFT) {
              return {
                ...item,
                fileLeft: files[0],
                objectURLLeft: URL.createObjectURL(files[0]),
                linkLeft: '',
              };
            }
            return {
              ...item,
              fileRight: files[0],
              objectURLRight: URL.createObjectURL(files[0]),
              linkRight: '',
            };
          }
          return item;
        }),
      ];

      updateData({ ...data, pairs: updatedPairs });
    };

    const handleRemoveFile = (index: number, side: SIDES) => {
      const updatedPairs = [
        ...pairs.map((item, idx) => {
          if (idx === index) {
            if (side === SIDES.LEFT) {
              return {
                ...item,
                fileLeft: null,
                objectURLLeft: '',
                linkLeft: '',
              };
            }
            return {
              ...item,
              fileRight: null,
              objectURLRight: '',
              linkRight: '',
            };
          }
          return item;
        }),
      ];

      updateData({ ...data, pairs: updatedPairs });
    };

    const validateFields = () => {
      let isValid = true;

      if (question.trim().length === 0) {
        setErrors(prev => ({ ...prev, question: 'Question is required' }));
        isValid = false;
      } else {
        setErrors(prev => ({ ...prev, question: '' }));
      }

      if (labelLeft.trim().length === 0) {
        setErrors(prev => ({
          ...prev,
          labelLeft: 'Left Column Label is required',
        }));
        isValid = false;
      } else {
        setErrors(prev => ({ ...prev, labelLeft: '' }));
      }

      if (labelRight.trim().length === 0) {
        setErrors(prev => ({
          ...prev,
          labelRight: 'Right Column Label is required',
        }));
        isValid = false;
      } else {
        setErrors(prev => ({ ...prev, labelRight: '' }));
      }

      if (pairs.length === 0) {
        setErrors(prev => ({
          ...prev,
          pairs: 'At least one pair is required',
        }));
        isValid = false;
      } else {
        const updatedErrorr = [...fieldErrors];
        pairs.forEach((pair, idx) => {
          if (pair.inputTypeLeft === MatchingQuizTypes.TEXT) {
            if (pair.valueLeft.trim() === '') {
              isValid = false;
              updatedErrorr[idx].left = 'Answer is required';
            } else {
              updatedErrorr[idx].left = '';
            }
          }

          if (pair.inputTypeLeft === MatchingQuizTypes.IMAGE) {
            if (pair.fileLeft === null) {
              isValid = false;
              updatedErrorr[idx].left = 'Image is required';
            } else {
              updatedErrorr[idx].left = '';
            }
          }

          if (pair.inputTypeRight === MatchingQuizTypes.TEXT) {
            if (pair.valueRight.trim() === '') {
              isValid = false;
              updatedErrorr[idx].right = 'Answer is required';
            } else {
              updatedErrorr[idx].right = '';
            }
          }

          if (pair.inputTypeRight === MatchingQuizTypes.IMAGE) {
            if (pair.fileRight === null) {
              isValid = false;
              updatedErrorr[idx].right = 'Image is required';
            } else {
              updatedErrorr[idx].right = '';
            }
          }
        });

        setFieldErrors(updatedErrorr);
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
              placeholder: 'Enter Question',
              onChange: handleChangeInput,
            },
          ]}
        >
          <div>
            <Label htmlFor="labelLeft">Left Column Label</Label>
            <Input
              className={cn('content-mt-2', {
                'content-has-error': !!errors.labelLeft,
              })}
              type="text"
              id="labelLeft"
              value={labelLeft}
              placeholder="Enter Left Column Label"
              onChange={handleChangeInput}
            />
            {errors.labelLeft && (
              <p className="content-mt-1 content-text-sm content-text-red-500">
                {errors.labelLeft}
              </p>
            )}
          </div>

          <div>
            <Label htmlFor="labelRight">Right Column Label</Label>
            <Input
              className={cn('content-mt-2', {
                'content-has-error': !!errors.labelRight,
              })}
              type="text"
              id="labelRight"
              value={labelRight}
              placeholder="Enter Right Column Label"
              onChange={handleChangeInput}
            />
            {errors.labelRight && (
              <p className="content-mt-1 content-text-sm content-text-red-500">
                {errors.labelRight}
              </p>
            )}
          </div>
        </InputCard>
        {data?.pairs?.map((pair, index) => (
          <Card
            className="content-relative content-mt-5"
            title={`Matching Question ${index + 1}`}
            key={index}
          >
            <Button
              className="content-absolute content-right-3 content-top-3 !content-bg-transparent content-p-0"
              onClick={() => handleDeletePair(index)}
            >
              <DeleteIcon />
            </Button>
            <div className="content-flex content-flex-col content-gap-y-3 content-p-3">
              <div className="content-flex content-gap-x-2">
                <Button
                  className="content-w-24"
                  size="sm"
                  variant={
                    pair.inputTypeLeft === MatchingQuizTypes.TEXT
                      ? 'secondary'
                      : 'outline'
                  }
                  onClick={_ =>
                    handleChangeInputType(
                      index,
                      SIDES.LEFT,
                      MatchingQuizTypes.TEXT,
                    )
                  }
                >
                  Text
                </Button>
                <Button
                  className="content-w-24"
                  size="sm"
                  variant={
                    pair.inputTypeLeft === MatchingQuizTypes.IMAGE
                      ? 'secondary'
                      : 'outline'
                  }
                  onClick={_ =>
                    handleChangeInputType(
                      index,
                      SIDES.LEFT,
                      MatchingQuizTypes.IMAGE,
                    )
                  }
                >
                  Image
                </Button>
              </div>
              <div className="content-flex content-flex-col content-gap-y-2">
                <Label htmlFor={`${pair.id}-${SIDES.LEFT}`}>
                  Answer {index + 1} (Left)
                </Label>
                {pair.inputTypeLeft === 'text' ? (
                  <Input
                    id={`${pair.id}_${SIDES.LEFT}`}
                    value={pair.valueLeft}
                    placeholder="Enter Answer"
                    onChange={handleChangeInput}
                  />
                ) : (
                  <div className="content-mt-2 content-flex content-items-center content-gap-4">
                    {pair.objectURLLeft || pair.linkLeft ? (
                      <Fragment>
                        <img
                          src={
                            pair.objectURLLeft
                              ? pair.objectURLLeft
                              : FILE_PATH_PREFIX + pair.linkLeft
                          }
                          alt="Left option"
                          className="content-h-32 content-object-cover"
                        />
                        <Button
                          type="button"
                          onClick={() => handleRemoveFile(index, SIDES.LEFT)}
                          className="content-rounded-none content-border-none !content-bg-transparent content-p-0 content-text-vibrant-red content-underline"
                        >
                          Remove
                        </Button>
                      </Fragment>
                    ) : (
                      <FileUploader
                        ref={imageUploaderRefs.current[index]?.left}
                        containerClassName="content-text-secondary"
                        accept={ValidImageFormats.join(',')}
                        placeholder="Upload image"
                        onUpload={files =>
                          handleFileUpload(files, index, SIDES.LEFT)
                        }
                      />
                    )}
                  </div>
                )}
                {fieldErrors[index]?.left && (
                  <span className="content-text-sm content-text-vibrant-red">
                    {fieldErrors[index].left}
                  </span>
                )}
              </div>

              <div className="content-mt-4 content-flex content-gap-x-2">
                <Button
                  className="content-w-24"
                  size="sm"
                  variant={
                    pair.inputTypeRight === MatchingQuizTypes.TEXT
                      ? 'secondary'
                      : 'outline'
                  }
                  onClick={_ =>
                    handleChangeInputType(
                      index,
                      SIDES.RIGHT,
                      MatchingQuizTypes.TEXT,
                    )
                  }
                >
                  Text
                </Button>
                <Button
                  className="content-w-24"
                  size="sm"
                  variant={
                    pair.inputTypeRight === MatchingQuizTypes.IMAGE
                      ? 'secondary'
                      : 'outline'
                  }
                  onClick={_ =>
                    handleChangeInputType(
                      index,
                      SIDES.RIGHT,
                      MatchingQuizTypes.IMAGE,
                    )
                  }
                >
                  Image
                </Button>
              </div>
              <div className="content-flex content-flex-col content-gap-y-2">
                <Label htmlFor={`${pair.id}_${SIDES.RIGHT}`}>
                  Answer {index + 1} (Right)
                </Label>
                {pair.inputTypeRight === 'text' ? (
                  <Input
                    type="text"
                    id={`${pair.id}_${SIDES.RIGHT}`}
                    value={pair.valueRight}
                    placeholder="Enter Answer"
                    onChange={handleChangeInput}
                  />
                ) : (
                  <div className="content-mt-2 content-flex content-items-center content-gap-4">
                    {pair.objectURLRight || pair.linkRight ? (
                      <Fragment>
                        <img
                          src={
                            pair.objectURLRight
                              ? pair.objectURLRight
                              : FILE_PATH_PREFIX + pair.linkRight
                          }
                          alt="Right option"
                          className="content-h-32 content-w-auto content-object-contain"
                        />
                        <Button
                          type="button"
                          onClick={() => handleRemoveFile(index, SIDES.RIGHT)}
                          className="content-rounded-none content-border-none !content-bg-transparent content-p-0 content-text-vibrant-red content-underline"
                        >
                          Remove
                        </Button>
                      </Fragment>
                    ) : (
                      <FileUploader
                        ref={imageUploaderRefs.current[index]?.right}
                        containerClassName="content-text-secondary"
                        accept={ValidImageFormats.join(',')}
                        placeholder="Upload image"
                        onUpload={files =>
                          handleFileUpload(files, index, SIDES.RIGHT)
                        }
                      />
                    )}
                  </div>
                )}
                {fieldErrors[index]?.right && (
                  <span className="content-text-sm content-text-vibrant-red">
                    {fieldErrors[index].right}
                  </span>
                )}
              </div>
            </div>
          </Card>
        ))}
        {!!errors.pairs.length && (
          <span className="content-text-sm content-text-vibrant-red">
            {errors.pairs}
          </span>
        )}
        <div className="content-mt-2 content-flex content-justify-center">
          <Button size="sm" onClick={handleAddPair}>
            Add New Pair
          </Button>
        </div>
      </div>
    );
  },
);

MatchingQuiz.displayName = 'MatchingQuiz';

export default MatchingQuiz;
