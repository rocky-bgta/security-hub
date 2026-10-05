import { BsBook, BsFileEarmarkPdf, BsMarkdown } from 'react-icons/bs';

import {
  ListIcon,
  LockIcon,
  QuestionBasedContentIcon,
  SliderLevelBasedContentIcon,
  TabbedBasedContentIcon,
} from 'assets/icons/index';
import { ContentTypes } from 'models/Content';
import { IoText } from 'react-icons/io5';

interface IProps {
  type: ContentTypes;
}

const ContentIcon = ({ type }: IProps) => {
  switch (type) {
    case ContentTypes.PDF:
      return <BsFileEarmarkPdf fill="#FFFFFF" />;
    case ContentTypes.VIDEO:
      return <LockIcon fill="#FFFFFF" />;
    case ContentTypes.LINK:
      return <ListIcon fill="#FFFFFF" />;
    case ContentTypes.TEXT:
      return <IoText fill="#FFFFFF" />;
    case ContentTypes.SLIDER_SHOW:
      return <SliderLevelBasedContentIcon fill="#FFFFFF" />;
    case ContentTypes.MARKDOWN:
      return <BsMarkdown className="content-text-white" />;
    case ContentTypes.STORY_BLOCK:
      return <BsBook fill="#FFFFFF" />;
    case ContentTypes.SLIDER_LEVEL:
      return <SliderLevelBasedContentIcon fill="#FFFFFF" />;
    case ContentTypes.QUESTION:
      return <QuestionBasedContentIcon fill="#FFFFFF" />;
    case ContentTypes.TABBED:
      return <TabbedBasedContentIcon width={16} height={16} fill="#FFFFFF" />;
    case ContentTypes.ANIMATION:
    case ContentTypes.VIDEO:
      return <LockIcon fill="#FFFFFF" />;
    case ContentTypes.INTERACTIVE_VIDEO:
      return <LockIcon fill="#FFFFFF" />;
    case ContentTypes.INTERACTIVE_CONTENT:
      return <LockIcon fill="#FFFFFF" />;
    case ContentTypes.QUIZ:
      return <QuestionBasedContentIcon fill="#FFFFFF" />;
    default:
      return null;
  }
};

export default ContentIcon;
