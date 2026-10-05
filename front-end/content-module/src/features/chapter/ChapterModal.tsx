import { zodResolver } from '@hookform/resolvers/zod';
import clsx from 'clsx';
import { useEffect, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';

import { Button } from 'common/Button';
import { Input } from 'common/Input';
import { Label } from 'common/Label';
import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { useAPI } from 'hooks/UseAPI';
import { IChapter } from 'models/Chapter';
import { IResponse, Status } from 'models/Global';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  ChapterFormFields,
  ChapterFormSchema,
  DefaultChapterFormValues,
  TChapterFormFields,
  TChapterFormFieldsKeys,
} from 'schemas/ChapterSchema';
import { isSuccessResponse } from 'utils/Helper';

interface IFormModalProps {
  isOpen: boolean;
  onSubmit: () => void;
  onClose: () => void;
  chapterLength: number;
  topicId: string;
  selectedChapter: IChapter | null;
}

const ChapterModal = ({
  isOpen,
  onSubmit,
  onClose,
  chapterLength,
  topicId,
  selectedChapter,
}: IFormModalProps) => {
  const {
    control,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm<TChapterFormFields>({
    resolver: zodResolver(ChapterFormSchema),
    defaultValues: DefaultChapterFormValues,
    mode: 'onChange',
  });
  const [submitting, setSubmitting] = useState<boolean>(false);
  const [chapterDetails, setChapterDetails] = useState<IChapter | null>(null);

  const apiClient = useAPI();

  useEffect(() => {
    if (!isOpen || !selectedChapter) return;

    fetchChapterDetails();
  }, [isOpen, selectedChapter]);

  const fetchChapterDetails = async () => {
    try {
      const response: IResponse<IChapter> = await apiClient.get(
        API_END_POINTS.CHAPTER_DETAILS + selectedChapter?.id,
      );
      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(response.message);
      }
      reset(response.data);
      setChapterDetails(response.data);
    } catch (error) {
      console.error('Error fetching chapter data:', error);
      toast.error((error as Error).message);
    }
  };

  const StatusOptions = [
    { id: '1', label: 'Enable', value: Status.ENABLED },
    { id: '2', label: 'Disable', value: Status.DISABLED },
  ];

  const handleFormSubmit = async (fields: TChapterFormFields) => {
    setSubmitting(true);
    const chapterIds = chapterDetails?.contentIds.map(content => content.id);
    const payload = {
      ...fields,
      topicId,
      position: selectedChapter ? chapterDetails?.position : chapterLength + 1,
      contentIds: selectedChapter ? chapterIds : [],
    };

    let response: IResponse<IChapter>;

    try {
      if (selectedChapter) {
        response = await apiClient.put(
          API_END_POINTS.CHAPTER_UPDATE + selectedChapter.id,
          { data: payload },
        );
      } else {
        response = await apiClient.post(API_END_POINTS.CHAPTER_CREATE, {
          data: payload,
        });
      }

      if ([200, 201].includes(response.statusCode)) {
        handleClose();
        onSubmit();
        toast.success(response.message);
      } else {
        toast.error(response.message);
      }
    } catch (error) {
      console.error('Error creating/updating chapter:', error);
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    reset(DefaultChapterFormValues);
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      disableOutsideClick={true}
      className="content-h-auto content-w-1/3"
    >
      <ModalHeader onClose={handleClose}>
        <p className="content-py-5 content-text-lg content-font-medium">
          {selectedChapter ? 'Edit Chapter' : 'Create Chapter'}
        </p>
      </ModalHeader>
      <ModalBody>
        <form onSubmit={handleSubmit(handleFormSubmit)}>
          <div className="content-py-5">
            {ChapterFormFields.map((field, idx) => (
              <div
                key={idx}
                className="content-mb-6 content-flex content-flex-col"
              >
                <Controller
                  name={field.key as TChapterFormFieldsKeys}
                  control={control}
                  render={({ field: { onChange, value } }) => {
                    if (field.type === 'select') {
                      return (
                        <>
                          <Label htmlFor={field.key}>{field.name}</Label>
                          <Select
                            value={value}
                            onValueChange={value => onChange(value)}
                          >
                            <SelectTrigger disabled={false}>
                              <SelectValue placeholder="Choose an option" />
                            </SelectTrigger>
                            <SelectContent>
                              {StatusOptions.map(option => (
                                <SelectItem
                                  key={option.id}
                                  value={option.value}
                                >
                                  {option.label}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </>
                      );
                    }

                    if (field.type === 'textarea') {
                      return (
                        <>
                          <Label htmlFor={field.key}>{field.name}</Label>
                          <textarea
                            key={field.key}
                            id={field.key}
                            name={field.name}
                            value={value as string}
                            className={clsx(
                              'content-h-20 content-w-full content-rounded-md content-border content-border-soft-blue-gray content-bg-transparent content-p-2 content-text-cloudy-white',
                              errors[field.key as TChapterFormFieldsKeys] &&
                              'content-has-error',
                            )}
                            onChange={e => onChange(e.target.value)}
                          />
                        </>
                      );
                    }

                    return (
                      <>
                        <Label htmlFor={field.key}>{field.name}</Label>
                        <Input
                          key={field.key}
                          id={field.key}
                          name={field.name}
                          value={value as string}
                          className={
                            errors[field.key as TChapterFormFieldsKeys] &&
                            'content-has-error'
                          }
                          onChange={e => onChange(e.target.value)}
                        />
                      </>
                    );
                  }}
                />

                {errors[field.key as TChapterFormFieldsKeys] && (
                  <p className="content-text-sm content-text-vibrant-red">
                    {errors[field.key as TChapterFormFieldsKeys]?.message}
                  </p>
                )}
              </div>
            ))}

            <div className="content-mt-5 content-flex content-justify-end content-space-x-2.5">
              <Button
                onClick={handleClose}
                variant="destructive"
                className="content-w-32"
              >
                Cancel
              </Button>
              <Button type="submit" className="content-w-32">
                {submitting
                  ? 'Saving...'
                  : selectedChapter
                    ? 'Update'
                    : 'Create'}
              </Button>
            </div>
          </div>
        </form>
      </ModalBody>
    </Modal>
  );
};

export default ChapterModal;
