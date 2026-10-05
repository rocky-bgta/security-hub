import {
  ITranscriptionResult,
  SUPPORTED_LANGUAGES,
} from 'models/EmailTemplate';
import { useCallback, useRef, useState } from 'react';
import { toast } from 'react-toastify';
import { IResponse } from 'models/Global';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { useAPI } from './UseAPI';

interface UseVoiceRecordingReturn {
  isRecording: boolean;
  transcript: string;
  detectedLanguage: string;
  confidence: number;
  error: string | null;
  isProcessing: boolean;
  startRecording: (languageHint?: string) => Promise<void>;
  stopRecording: () => Promise<void>;
  clearTranscript: () => void;
  updateTranscript: (value: string) => void;
  supportedLanguages: Record<string, string>;
}

type TSttStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

interface ISttInitResponse {
  audioId: string;
  status: TSttStatus;
}

interface ISttStatusResponse extends ISttInitResponse {
  transcription?: string;
}

const STT_POLL_INTERVAL_MS = 5000;
const STT_MAX_RETRIES_ON_FAILED = 1;

const normalizeApiResponse = <T>(
  response: unknown,
): { data?: T; message?: string; ok: boolean } => {
  const res = response as
    | IResponse<T>
    | {
        status?: number;
        message?: string;
        data?: T;
      }
    | T;

  const hasWrappedShape =
    typeof res === 'object' &&
    res !== null &&
    'statusCode' in res &&
    'data' in res;

  if (hasWrappedShape) {
    const wrapped = res as IResponse<T>;
    return {
      data: wrapped.data,
      message: wrapped.message,
      ok: isSuccessResponse(wrapped.statusCode),
    };
  }

  const hasAxiosShape =
    typeof res === 'object' &&
    res !== null &&
    'status' in res &&
    'data' in res &&
    !('audioId' in (res as object));

  if (hasAxiosShape) {
    const axiosLike = res as { status?: number; message?: string; data?: T };
    return {
      data: axiosLike.data,
      message: axiosLike.message,
      ok:
        typeof axiosLike.status === 'number'
          ? isSuccessResponse(axiosLike.status)
          : true,
    };
  }

  return {
    data: res as T,
    ok: true,
  };
};

/**
 * Custom hook for voice-to-text recording and transcription
 * Based on BRD Use Case 2.1.3.2: Multi-language voice input support
 */
const useVoiceRecording = (): UseVoiceRecordingReturn => {
  const apiClient = useAPI();
  const [isRecording, setIsRecording] = useState(false);
  const [isProcessing, setIsProcessing] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [detectedLanguage, setDetectedLanguage] = useState('');
  const [confidence, setConfidence] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const languageHintRef = useRef<string | undefined>(undefined);

  const sleep = (ms: number) =>
    new Promise(resolve => {
      window.setTimeout(resolve, ms);
    });

  /**
   * Start voice recording
   */
  /**
   * Process the recorded audio and send to transcription API
   */
  const processRecording = useCallback(async () => {
    if (audioChunksRef.current.length === 0) {
      setError('No audio recorded');
      return;
    }

    setIsProcessing(true);

    try {
      // Create audio blob
      const audioBlob = new Blob(audioChunksRef.current, {
        type: 'audio/webm',
      });

      // Create FormData
      const formData = new FormData();
      formData.append('file', audioBlob, 'recording.webm');

      const normalizedLanguageHint =
        languageHintRef.current && languageHintRef.current !== 'auto-detect'
          ? languageHintRef.current
          : undefined;

      let transcription = '';
      let attempt = 0;

      while (attempt <= STT_MAX_RETRIES_ON_FAILED) {
        let transcribeUrl = API_END_POINTS.STT_TRANSCRIBE;
        if (normalizedLanguageHint) {
          transcribeUrl += `?languageHint=${encodeURIComponent(normalizedLanguageHint)}`;
        }

        const initResponseRaw = await apiClient.post(transcribeUrl, {
          data: formData,
          headers: { 'Content-Type': 'multipart/form-data' },
        });
        const initResponse =
          normalizeApiResponse<ISttInitResponse>(initResponseRaw);

        if (!initResponse.ok || !initResponse.data?.audioId) {
          throw new Error(
            initResponse.message || 'Failed to start audio transcription.',
          );
        }

        const { audioId } = initResponse.data;

        while (true) {
          const statusResponseRaw = await apiClient.get(
            API_END_POINTS.STT_TRANSCRIBE_STATUS(audioId),
          );
          const statusResponse =
            normalizeApiResponse<ISttStatusResponse>(statusResponseRaw);

          if (!statusResponse.ok || !statusResponse.data) {
            throw new Error(
              statusResponse.message || 'Failed while polling transcription.',
            );
          }

          const { status, transcription: completedTranscription } =
            statusResponse.data;

          if (status === 'COMPLETED') {
            transcription = completedTranscription ?? '';
            break;
          }

          if (status === 'FAILED') {
            if (attempt < STT_MAX_RETRIES_ON_FAILED) {
              attempt += 1;
              break;
            }
            throw new Error('Transcription failed. Please try again.');
          }

          await sleep(STT_POLL_INTERVAL_MS);
        }

        if (transcription) {
          break;
        }
      }

      if (!transcription) {
        throw new Error('Transcription failed. Please try again.');
      }

      const normalizedResult = {
        transcribedText: transcription,
        detectedLanguage: normalizedLanguageHint ?? '',
        detectedLanguageName: '',
        confidenceScore: 0,
        isAutoDetected: !normalizedLanguageHint,
      } as ITranscriptionResult;

      setTranscript(normalizedResult.transcribedText);
      setDetectedLanguage(normalizedResult.detectedLanguage);
      setConfidence(normalizedResult.confidenceScore);
    } catch (err) {
      console.error('Transcription error:', err);
      const message =
        err instanceof Error && err.message
          ? err.message
          : 'Transcription failed. Please try again.';
      setError(message);
      toast.error(message);
    } finally {
      setIsProcessing(false);
      audioChunksRef.current = [];
    }
  }, [apiClient]);

  const startRecording = useCallback(
    async (languageHint?: string) => {
      setError(null);
      languageHintRef.current = languageHint;

      try {
        // Request microphone permission
        const stream = await navigator.mediaDevices.getUserMedia({
          audio: true,
        });

        // Create MediaRecorder
        const mediaRecorder = new MediaRecorder(stream, {
          mimeType: 'audio/webm;codecs=opus',
        });

        audioChunksRef.current = [];

        mediaRecorder.ondataavailable = event => {
          if (event.data.size > 0) {
            audioChunksRef.current.push(event.data);
          }
        };

        mediaRecorder.onstop = async () => {
          // Stop all tracks
          stream.getTracks().forEach(track => track.stop());

          // Process the recorded audio
          await processRecording();
        };

        mediaRecorder.onerror = () => {
          setError('Recording failed. Please try again.');
          setIsRecording(false);
        };

        mediaRecorderRef.current = mediaRecorder;
        mediaRecorder.start(1000); // Collect data every 1 second
        setIsRecording(true);
      } catch (err) {
        if ((err as Error).name === 'NotAllowedError') {
          setError('Microphone access required for voice input');
          toast.error('Microphone access required for voice input');
        } else {
          setError('Failed to start recording. Please check microphone.');
          toast.error('Failed to start recording');
        }
      }
    },
    [processRecording],
  );

  /**
   * Stop voice recording
   */
  const stopRecording = useCallback(async () => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
    }
  }, [isRecording]);

  /**
   * Clear transcript and reset state
   */
  const clearTranscript = useCallback(() => {
    setTranscript('');
    setDetectedLanguage('');
    setConfidence(0);
    setError(null);
  }, []);

  const updateTranscript = useCallback((value: string) => {
    setTranscript(value);
  }, []);

  return {
    isRecording,
    transcript,
    detectedLanguage,
    confidence,
    error,
    isProcessing,
    startRecording,
    stopRecording,
    clearTranscript,
    updateTranscript,
    supportedLanguages: SUPPORTED_LANGUAGES,
  };
};

export default useVoiceRecording;
