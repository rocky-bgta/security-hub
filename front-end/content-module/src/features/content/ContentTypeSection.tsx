import clsx from 'clsx';
import { Fragment, useState } from 'react';
import { LuPlus } from 'react-icons/lu';

import {
  AnimationBasedContentIcon,
  CloseIcon,
  LinkBasedContentIcon,
  PdfBasedContentIcon,
  QuestionBasedContentIcon,
  SideShowBasedContentIcon,
  SliderLevelBasedContentIcon,
  TabbedBasedContentIcon,
  TextBasedContentIcon,
  VideoBasedContentIcon,
} from 'assets/icons';
import { Button } from 'common/Button';
import { Input } from 'common/Input';
import Modal from 'common/modal/Modal';
import ModalBody from 'common/modal/ModalBody';
import ModalHeader from 'common/modal/ModalHeader';
import { ContentTypes, IContentOption } from 'models/Content';

const ContentOptions: Array<IContentOption> = [
  {
    id: 1,
    title: 'Text/Web-based',
    type: ContentTypes.TEXT,
    icon: <TextBasedContentIcon />,
    disable: false,
  },
  {
    id: 2,
    title: 'Link',
    type: ContentTypes.LINK,
    icon: <LinkBasedContentIcon />,
    disable: false,
  },
  {
    id: 3,
    title: 'PDF',
    type: ContentTypes.PDF,
    icon: <PdfBasedContentIcon />,
    disable: false,
  },
  {
    id: 4,
    title: 'Question',
    type: ContentTypes.QUESTION,
    icon: <QuestionBasedContentIcon fill="#ffffff" />,
    disable: false,
  },
  {
    id: 5,
    title: 'Slider show',
    type: ContentTypes.SLIDER_SHOW,
    icon: <SideShowBasedContentIcon />,
    disable: false,
  },
  {
    id: 6,
    title: 'Tabbed',
    type: ContentTypes.TABBED,
    icon: <TabbedBasedContentIcon fill="#ffffff" />,
    disable: false,
  },
  {
    id: 7,
    title: 'Video',
    type: ContentTypes.VIDEO,
    icon: <VideoBasedContentIcon />,
    disable: false,
  },
  {
    id: 8,
    title: 'Animation',
    type: ContentTypes.ANIMATION,
    icon: <AnimationBasedContentIcon />,
    disable: false,
  },
  {
    id: 9,
    title: 'Mark down',
    type: ContentTypes.MARKDOWN,
    icon: <AnimationBasedContentIcon />,
    disable: false,
  },
  {
    id: 10,
    title: 'Story Blocks',
    type: ContentTypes.STORY_BLOCK,
    icon: <VideoBasedContentIcon />,
    disable: false,
  },
  {
    id: 11,
    title: 'Slider Level',
    type: ContentTypes.SLIDER_LEVEL,
    icon: <SliderLevelBasedContentIcon fill="#ffffff" />,
    disable: false,
  },
  {
    id: 12,
    title: 'Quiz',
    type: ContentTypes.QUIZ,
    icon: <QuestionBasedContentIcon fill="#ffffff" />,
    disable: false,
  },
  {
    id: 13,
    title: 'Interactive Video',
    type: ContentTypes.INTERACTIVE_VIDEO,
    icon: <VideoBasedContentIcon />,
    disable: false,
  },
  {
    id: 14,
    title: 'Interactive Content',
    type: ContentTypes.INTERACTIVE_CONTENT,
    icon: <AnimationBasedContentIcon />,
    disable: false,
  },
];

interface IProps {
  onClickOption: ({ name, type }: { name: string; type: ContentTypes }) => void;
}

const ContentTypeSection = ({ onClickOption }: IProps) => {
  const [openOptions, setOpenOptions] = useState<boolean>(false);
  const [openModal, setOpenModal] = useState<boolean>(false);
  const [contentName, setContentName] = useState<{
    text: string;
    error: string;
  }>({
    text: '',
    error: '',
  });
  const [selectedOption, setSelectedOption] = useState<IContentOption | null>(
    null,
  );

  const toggleOptions = () => setOpenOptions(!openOptions);

  const handleClickClose = () => {
    setOpenModal(false);
    setContentName({ text: '', error: '' });
    setSelectedOption(null);
  };

  const handleClickOpen = (option: IContentOption) => {
    setSelectedOption(option);
    setOpenModal(true);
  };

  const handleClickSubmit = () => {
    if (contentName.text && selectedOption) {
      onClickOption({ name: contentName.text, type: selectedOption.type });
      handleClickClose();
    } else {
      setContentName({ ...contentName, error: 'Please enter a name' });
    }
  };

  return (
    <Fragment>
      <div className="content-mt-9 content-flex content-h-px content-items-center content-justify-center content-border-b content-border-card-border">
        <Button
          className={clsx(
            'content-flex content-size-12 content-items-center content-justify-center content-rounded-full content-text-white',
            openOptions
              ? 'content-bg-red-500 hover:!content-bg-red-600'
              : 'content-bg-green-500 hover:!content-bg-green-600',
          )}
          onClick={toggleOptions}
        >
          <span>
            {openOptions ? (
              <CloseIcon />
            ) : (
              <LuPlus className="content-size-6" />
            )}
          </span>
        </Button>
      </div>

      {openOptions && (
        <div className="content-mt-10 content-flex content-flex-wrap content-items-center content-justify-center content-gap-x-14 content-gap-y-8">
          {ContentOptions.map(option => (
            <Button
              key={option.id}
              disabled={option.disable}
              className="content-flex content-size-fit content-flex-col content-items-center content-justify-center content-gap-2 !content-bg-transparent content-p-0 content-text-white hover:content-text-primary"
              onClick={() => handleClickOpen(option)}
            >
              {option.icon}
              <span>{option.title}</span>
            </Button>
          ))}
        </div>
      )}

      {openModal && (
        <Modal
          isOpen={openModal}
          onClose={handleClickClose}
          disableOutsideClick={true}
          className="content-h-auto content-w-1/5"
        >
          <ModalHeader onClose={handleClickClose}>
            <p className="content-py-5 content-text-lg content-font-medium">
              Add Content Name
            </p>
          </ModalHeader>
          <ModalBody className="content-my-6 content-flex content-flex-col content-gap-4">
            <div>
              <Input
                id="content-name"
                className="content-w-full"
                placeholder="Give the content a name"
                value={contentName.text}
                onChange={e =>
                  setContentName({
                    text: e.target.value,
                    error:
                      e.target.value.trim().length === 0
                        ? 'Please enter a name'
                        : '',
                  })
                }
              />
              {contentName.error && (
                <p className="content-mt-1 content-text-sm content-text-red-500">
                  {contentName.error}
                </p>
              )}
            </div>
            <Button
              className="content-ml-auto content-w-max"
              size="sm"
              onClick={handleClickSubmit}
            >
              Submit
            </Button>
          </ModalBody>
        </Modal>
      )}
    </Fragment>
  );
};

export default ContentTypeSection;
