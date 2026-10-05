import { useState } from 'react';
import { Badge } from 'common/Badge';
import { Button } from 'common/Button';
import { Textarea } from 'common/Textarea';
import { AlertTriangle, Loader2, Mic, MicOff, Trash2 } from 'lucide-react';
import { cn } from 'utils/Helper';
import SearchSelect from 'components/SearchSelect';

interface VoiceInputControlProps {
  isRecording: boolean;
  isProcessing: boolean;
  transcript: string;
  detectedLanguage: string;
  confidence: number;
  error: string | null;
  selectedLanguage: string;
  supportedLanguages: Record<string, string>;
  /** When true, input language dropdown is disabled (e.g. loading from API) */
  languagesLoading?: boolean;
  onLanguageChange: (lang: string) => void;
  onStartRecording: () => void;
  onStopRecording: () => void;
  onClear: () => void;
  onTranscriptChange: (value: string) => void;
  className?: string;
}

/**
 * Voice input control with recording, transcription display, and language selection
 * Based on Task-03: Multi-language voice input support
 */
const VoiceInputControl = ({
  isRecording,
  isProcessing,
  transcript,
  detectedLanguage,
  confidence,
  error,
  selectedLanguage,
  supportedLanguages,
  languagesLoading = false,
  onLanguageChange,
  onStartRecording,
  onStopRecording,
  onClear,
  onTranscriptChange,
  className,
}: VoiceInputControlProps) => {
  const isLowConfidence = confidence > 0 && confidence < 0.5;
  const confidencePercent = Math.round(confidence * 100);
  const [isEditingTranscript, setIsEditingTranscript] = useState(false);
  const [draftTranscript, setDraftTranscript] = useState('');

  return (
    <div className={cn('space-y-4', className)}>
      {/* Language selector and record button */}
      <div className="flex items-end gap-4">
        <div className="flex-1">
          <label className="mb-1 block text-xs text-muted-foreground">
            Input Language
          </label>
          <SearchSelect
            items={
              Object.entries(supportedLanguages).map(([code, name]) => ({
                value: code,
                label: name,
              })) ?? []
            }
            placeholder="Select input language"
            value={selectedLanguage}
            onValueChange={value => onLanguageChange(value)}
            hasError={!!error}
            disabled={isRecording || languagesLoading}
          />
        </div>

        <div className="flex items-center gap-2">
          {isRecording ? (
            <Button
              type="button"
              variant="destructive"
              onClick={onStopRecording}
              className="gap-2"
            >
              <MicOff className="size-4" />
              Stop Recording
            </Button>
          ) : (
            <Button
              type="button"
              variant="default"
              onClick={onStartRecording}
              disabled={isProcessing}
              className="gap-2"
            >
              {isProcessing ? (
                <>
                  <Loader2 className="size-4 animate-spin" />
                  Processing...
                </>
              ) : (
                <>
                  <Mic className="size-4" />
                  Start Recording
                </>
              )}
            </Button>
          )}
        </div>
      </div>

      {/* Recording indicator */}
      {isRecording && (
        <div className="flex items-center gap-2 rounded-md bg-vibrant-red/10 px-4 py-3">
          <span className="relative flex size-3">
            <span className="absolute inline-flex size-full animate-ping rounded-full bg-vibrant-red opacity-75"></span>
            <span className="relative inline-flex size-3 rounded-full bg-vibrant-red"></span>
          </span>
          <span className="text-sm text-vibrant-red">
            Recording in progress...
          </span>
        </div>
      )}

      {isProcessing && !isRecording && (
        <div className="flex items-center gap-2 rounded-md bg-primary/10 px-4 py-3">
          <Loader2 className="size-4 animate-spin text-primary" />
          <span className="text-sm text-primary">Processing audio...</span>
        </div>
      )}

      {/* Transcription result */}
      {transcript && (
        <div className="rounded-lg border border-primary p-4">
          <div className="mb-2 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-sm font-medium text-primary">
                Transcribed Text
              </span>
              {detectedLanguage && (
                <Badge variant="outline" className="text-xs">
                  {supportedLanguages[detectedLanguage] || detectedLanguage}
                </Badge>
              )}
            </div>
            <div className="flex items-center gap-2">
              {!isEditingTranscript ? (
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setDraftTranscript(transcript);
                    setIsEditingTranscript(true);
                  }}
                >
                  Edit
                </Button>
              ) : (
                <>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      setDraftTranscript(transcript);
                      setIsEditingTranscript(false);
                    }}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="button"
                    size="sm"
                    onClick={() => {
                      onTranscriptChange(draftTranscript);
                      setIsEditingTranscript(false);
                    }}
                  >
                    Save
                  </Button>
                </>
              )}
              {confidence > 0 && (
                <Badge
                  variant={isLowConfidence ? 'warning' : 'default'}
                  className="text-xs"
                >
                  {confidencePercent}% confidence
                </Badge>
              )}
              <Button
                type="button"
                variant="destructive"
                size="sm"
                onClick={() => {
                  setIsEditingTranscript(false);
                  onClear();
                }}
              >
                <Trash2 className="size-4" />
              </Button>
            </div>
          </div>

          {isEditingTranscript ? (
            <Textarea
              value={draftTranscript}
              onChange={e => setDraftTranscript(e.target.value)}
              className="min-h-24"
            />
          ) : (
            <p className="whitespace-pre-wrap text-sm text-muted-foreground">
              {transcript}
            </p>
          )}

          <div className="mt-2 flex items-center justify-between text-xs text-muted-foreground">
            <span>{transcript.length} characters</span>
          </div>

          {isLowConfidence && (
            <div className="mt-3 flex items-start gap-2 rounded bg-yellow-600/10 p-2">
              <AlertTriangle className="mt-0.5 size-4 shrink-0 text-yellow-600" />
              <p className="text-xs text-yellow-600">
                Low confidence transcription. Please try again or select
                language manually.
              </p>
            </div>
          )}
        </div>
      )}

      {/* Error message */}
      {error && (
        <div className="flex items-center gap-2 rounded-md bg-vibrant-red/10 px-4 py-3">
          <AlertTriangle className="size-4 text-vibrant-red" />
          <span className="text-sm text-vibrant-red">{error}</span>
        </div>
      )}
    </div>
  );
};

export default VoiceInputControl;
