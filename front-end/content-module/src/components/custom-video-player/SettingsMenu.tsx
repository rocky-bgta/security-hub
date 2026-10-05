import { MouseEvent } from 'react';

import { SettingsIcon } from 'assets/icons';
import { VideoQuality } from 'components/custom-video-player/hooks/useVideoQuality';
import { cn } from 'utils/Helper';

interface VideoLanguageOption {
  code: string;
  label: string;
}

interface SettingsMenuProps {
  showSettings: boolean;
  availableQualities: Array<VideoQuality>;
  currentQuality: number;
  availableLanguages?: Array<VideoLanguageOption>;
  currentLanguageCode?: string;
  onToggleSettings: (e: MouseEvent) => void;
  onQualityChange: (index: number) => void;
  onLanguageChange?: (code: string) => void;
}

const SettingsMenu = ({
  showSettings,
  availableQualities,
  currentQuality,
  availableLanguages = [],
  currentLanguageCode,
  onToggleSettings,
  onQualityChange,
  onLanguageChange,
}: SettingsMenuProps) => {
  const hasQualityOptions = availableQualities.length > 0;
  const hasLanguageOptions = availableLanguages.length > 1;

  return (
    <div className="content-relative content-flex">
      <button
        onClick={onToggleSettings}
        className="content-text-white hover:content-text-gray-300"
      >
        <SettingsIcon />
      </button>

      {showSettings && (
        <div className="content-absolute content-bottom-full content-right-0 content-mb-2 content-w-48 content-rounded content-bg-black content-bg-opacity-70 content-p-2">
          {hasQualityOptions && (
            <>
              <div className="content-mb-2 content-text-sm content-font-medium content-text-white">
                Quality
              </div>
              {availableQualities.map((quality, index) => {
                const isCurrentlySelected = index === currentQuality;

                return (
                  <button
                    key={index}
                    onClick={() => onQualityChange(index)}
                    className={cn(
                      'content-w-full content-rounded content-px-2 content-py-1 content-text-left content-text-sm',
                      isCurrentlySelected
                        ? 'content-bg-blue-500 content-text-white'
                        : 'content-text-white hover:content-bg-gray-700',
                    )}
                  >
                    {quality.resolution}
                  </button>
                );
              })}
            </>
          )}

          {hasLanguageOptions && (
            <>
              {hasQualityOptions && (
                <div className="content-my-2 content-border-t content-border-white/20" />
              )}
              <div className="content-mb-2 content-text-sm content-font-medium content-text-white">
                Language
              </div>
              {availableLanguages.map(language => {
                const isSelected = language.code === currentLanguageCode;

                return (
                  <button
                    key={language.code}
                    onClick={() => onLanguageChange?.(language.code)}
                    className={cn(
                      'content-w-full content-rounded content-px-2 content-py-1 content-text-left content-text-sm',
                      isSelected
                        ? 'content-bg-blue-500 content-text-white'
                        : 'content-text-white hover:content-bg-gray-700',
                    )}
                  >
                    {language.label}
                  </button>
                );
              })}
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default SettingsMenu;
