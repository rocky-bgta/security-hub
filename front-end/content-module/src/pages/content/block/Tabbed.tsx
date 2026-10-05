import { sanitizeHtml } from 'home-module/security';
import { useEffect, useRef, useState } from 'react';
import { FaCheck } from 'react-icons/fa';

import { TabButtonIcon } from 'assets/icons';
import { Button } from 'common/Button';
import CustomAudioPlayer from 'components/CustomAudioPlayer';
import UserBlockTypedInputWithFeatureImage from 'components/UserBlockTypedInputWithFeatureImage';
import {
  AllContentTypes,
  IContent,
  ITab,
  ITabbedContent,
} from 'models/Content';
import { cn, timeToSeconds } from 'utils/Helper';

interface IProps {
  content: IContent<ITabbedContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
  showCompleteButton?: (type: AllContentTypes) => void;
  courseComplete?: () => void;
}

const Tabbed = ({
  content,
  isInteractive,
  handleNextContent,
  showCompleteButton,
  courseComplete,
}: IProps) => {
  const audioRef = useRef<HTMLAudioElement | null>(null);

  const [activeTab, setActiveTab] = useState<{
    additionalProperties: ITab;
  } | null>(null);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [audioEnded, setAudioEnded] = useState<boolean>(false);
  const [clickedTabs, setClickedTabs] = useState<Set<string>>(new Set());
  const totalTabs = content?.specific?.tabSections?.length || 0;
  const [showNextButton, setShowNextButton] = useState<boolean>(false);
  useEffect(() => {
    const audioUrl = content?.specific?.additionalProperties?.audioUrl;
    if (!audioUrl) {
      setCurrentTime(Infinity);
      setAudioEnded(true);
      return;
    }

    const audioElement = audioRef.current;
    if (audioElement) {
      const handleTimeUpdate = () =>
        setCurrentTime(Math.floor(audioElement.currentTime));

      const handleEnded = () => setAudioEnded(true);

      audioElement.addEventListener('timeupdate', handleTimeUpdate);
      audioElement.addEventListener('ended', handleEnded);

      return () => {
        audioElement.removeEventListener('timeupdate', handleTimeUpdate);
        audioElement.removeEventListener('ended', handleEnded);
      };
    }
  }, []);

  useEffect(() => {
    if (clickedTabs.size === totalTabs && totalTabs > 0) {
      showCompleteButton?.(content.common.contentType);
      courseComplete?.();
      setShowNextButton(true);
    }
  }, [clickedTabs, totalTabs, courseComplete, showCompleteButton]);

  const handleTabClick = (tab: { additionalProperties: ITab }) => {
    setActiveTab(tab);

    setClickedTabs(prev => {
      const newSet = new Set(prev);
      newSet.add(tab.additionalProperties.id);
      return newSet;
    });
  };

  return (
    <div className="content-flex content-size-full content-flex-col lg:content-flex-row">
      {isInteractive && showNextButton && (
        <div className="content-absolute content-bottom-1 content-right-4 content-z-10">
          <Button
            onClick={handleNextContent}
            size="sm"
            className="content-px-8"
          >
            Next
          </Button>
        </div>
      )}
      <div
        className={cn(
          `content-relative content-min-h-[320px] content-overflow-y-auto content-bg-[#D9D9D940] sm:content-min-h-[480px] lg:content-min-h-[600px]`,
          activeTab ? 'content-w-full lg:content-w-2/5' : 'content-w-full',
        )}
        onClick={e => {
          if (activeTab && e.target === e.currentTarget) {
            setActiveTab(null);
          }
        }}
      >
        <UserBlockTypedInputWithFeatureImage content={content} />

        <div
          className={cn(
            'content-mt-4 content-flex content-w-full content-flex-wrap content-justify-start content-gap-2 content-border-t content-border-graphite content-pb-10 content-pl-4 content-pt-4',
            content?.specific?.additionalProperties?.tabbedPosition === 'COLUMN'
              ? 'content-flex-col'
              : 'content-flex-row',
          )}
        >
          {content?.specific?.tabSections?.map((tab, idx) => {
            const displayTime = Number(
              timeToSeconds(tab?.additionalProperties?.displayTime.toString()),
            );
            if (currentTime >= displayTime) {
              return (
                <Button
                  key={idx}
                  onClick={e => {
                    e.stopPropagation();
                    handleTabClick(tab);
                  }}
                  disabled={!audioEnded}
                  style={{
                    backgroundColor:
                      content?.specific?.additionalProperties?.buttonColor ||
                      '',
                    color:
                      tab?.additionalProperties?.id ===
                      activeTab?.additionalProperties?.id
                        ? '#37BE99'
                        : content?.specific?.additionalProperties
                            ?.buttonTextColor || '',
                    cursor: !audioEnded ? 'not-allowed' : 'pointer',
                  }}
                  className="content-flex content-w-fit content-items-center content-gap-2 content-px-7 content-py-2 hover:content-transition-colors"
                  onMouseOver={e => {
                    e.currentTarget.style.backgroundColor =
                      content?.specific?.additionalProperties
                        ?.buttonHoverColor || '';
                  }}
                  onMouseOut={e => {
                    e.currentTarget.style.backgroundColor =
                      content?.specific?.additionalProperties?.buttonColor ||
                      '';
                  }}
                >
                  <TabButtonIcon />
                  {tab?.additionalProperties?.navigationButtonText}
                  {clickedTabs.has(tab.additionalProperties.id) && (
                    <FaCheck className="content-ml-2 content-text-primary" />
                  )}
                </Button>
              );
            }
            return null;
          })}
        </div>
        {!activeTab && content?.specific?.additionalProperties?.audioUrl && (
          <div className="content-absolute content-inset-x-3 content-bottom-4 content-z-10 content-flex content-items-center content-justify-center sm:content-inset-x-6">
            <CustomAudioPlayer
              audioRef={audioRef}
              audioUrl={content?.specific?.additionalProperties?.audioUrl || ''}
            />
          </div>
        )}
      </div>

      {activeTab && (
        <div className="content-max-h-[50vh] content-w-full content-overflow-y-auto lg:content-h-[600px] lg:content-max-h-none lg:content-w-3/5">
          <div
            className="content-p-5"
            dangerouslySetInnerHTML={{
              __html: sanitizeHtml(
                activeTab?.additionalProperties?.paragraph || '',
              ),
            }}
          />
          {activeTab?.additionalProperties?.audioUrl && (
            <div className="content-mt-4 content-px-5">
              <CustomAudioPlayer
                autoPlay={true}
                audioRef={audioRef}
                audioUrl={activeTab?.additionalProperties?.audioUrl || ''}
              />
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default Tabbed;
