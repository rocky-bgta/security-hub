import clsx from 'clsx';
import { sanitizeHtml } from 'home-module/security';
import { Fragment, useEffect, useRef, useState } from 'react';
import { AiOutlineBook, AiOutlineSound } from 'react-icons/ai';
import { LuAudioLines } from 'react-icons/lu';

import { Button } from 'common/Button';
import BlockTypedInputWithFeatureImage from 'components/BlockTypedInputWithFeatureImage';
import { IContent, ITab, ITabbedContent } from 'models/Content';
import { timeToSeconds } from 'utils/Helper';

interface IProps {
  content: IContent<ITabbedContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const Tabbed = ({ content, isInteractive, handleNextContent }: IProps) => {
  const audioRef = useRef<HTMLAudioElement>(null);
  const [activeTab, setActiveTab] = useState<{
    additionalProperties: ITab;
  } | null>(null);
  const [currentTime, setCurrentTime] = useState<number>(0);
  const [audioEnded, setAudioEnded] = useState<boolean>(false);

  useEffect(() => {
    const audioElement = audioRef.current;
    const audioUrl = content?.specific?.additionalProperties?.audioUrl;

    if (!audioUrl) {
      setCurrentTime(Infinity);
      setAudioEnded(true);
      return;
    }

    if (audioElement) {
      const handleTimeUpdate = () => {
        setCurrentTime(Math.floor(audioElement.currentTime));
      };
      const handleEnded = () => {
        setAudioEnded(true);
      };
      audioElement.addEventListener('timeupdate', handleTimeUpdate);
      audioElement.addEventListener('ended', handleEnded);
      return () => {
        audioElement.removeEventListener('timeupdate', handleTimeUpdate);
        audioElement.removeEventListener('ended', handleEnded);
      };
    }
  }, []);

  const handleTabClick = (tab: { additionalProperties: ITab }) =>
    setActiveTab(tab);

  return (
    <Fragment>
      {isInteractive && audioEnded && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}
      <div className="content-flex content-size-full">
        <div
          className={clsx(
            'content-max-h-[600px] content-overflow-y-auto content-p-5',
            activeTab ? 'content-w-1/2' : 'content-w-full',
          )}
        >
          <div onClick={() => setActiveTab(null)}>
            <BlockTypedInputWithFeatureImage content={content} />
          </div>

          <div
            className={clsx(
              'content-mt-5 content-flex content-w-fit content-justify-start content-gap-2',
              content?.specific?.additionalProperties?.tabbedPosition ===
                'COLUMN'
                ? 'content-flex-col'
                : 'content-flex-row',
            )}
          >
            {content?.specific?.tabSections?.map((tab, idx) => {
              const displayTime = Number(
                timeToSeconds(
                  tab?.additionalProperties?.displayTime.toString(),
                ),
              );
              if (currentTime >= displayTime) {
                return (
                  <Button
                    key={idx}
                    onClick={() => handleTabClick(tab)}
                    disabled={!audioEnded}
                    style={{
                      backgroundColor:
                        content?.specific?.additionalProperties?.buttonColor ||
                        '',
                      color:
                        content?.specific?.additionalProperties
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
                    <AiOutlineBook className="content-text-lg" />
                    {tab?.additionalProperties?.navigationButtonText}
                  </Button>
                );
              }
              return null;
            })}
          </div>
          <div
            className={clsx(
              'content-absolute content-right-5 content-top-5 content-items-center content-gap-2',
              audioEnded ? 'content-hidden' : 'content-flex',
            )}
          >
            <AiOutlineSound className="content-text-2xl content-text-white" />
            <LuAudioLines className="content-animate-bounce content-text-2xl content-text-white" />
          </div>
          <audio ref={audioRef} autoPlay>
            <source
              src={content?.specific?.additionalProperties?.audioUrl}
              type="audio/mpeg"
            />
          </audio>
        </div>

        {activeTab && (
          <div
            className={clsx(
              'content-h-[600px] content-overflow-y-auto content-bg-gray-50',
              { 'content-w-1/2': activeTab },
            )}
          >
            <div
              className="content-p-5"
              dangerouslySetInnerHTML={{
                __html: sanitizeHtml(
                  activeTab?.additionalProperties?.paragraph || '',
                ),
              }}
            />
          </div>
        )}
      </div>
    </Fragment>
  );
};

export default Tabbed;
