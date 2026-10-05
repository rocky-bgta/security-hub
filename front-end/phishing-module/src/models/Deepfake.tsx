export const STEPS = [
  { id: 1, title: 'Onboarding', desc: 'Guided setup' },
  { id: 2, title: 'Face setup', desc: 'Capture, review, confirm' },
  { id: 3, title: 'Voice cloning', desc: 'Record or upload' },
  { id: 4, title: 'Script', desc: 'Customise variables' },
  { id: 5, title: 'Generate', desc: 'Provider & render' },
] as const;

export interface BackgroundOption {
  name: string;
  value: string;
  url: string;
  id?: string;
  fileKey?: string;
}

export type DeepfakeImageType = 'BACKGROUND' | 'FACE_CAPTURE';

export interface IDeepfakeImage {
  id: string;
  fileName: string;
  fileKey: string;
  url: string;
  imageType: DeepfakeImageType;
  status: 'ACTIVE' | 'INACTIVE' | string;
  createdAt: string;
  active: boolean;
  global: boolean;
}

export interface IDeepfakeImageListParams {
  offset?: number;
  pageSize?: number;
  fileName?: string;
  uploadDate?: string;
  isActive?: boolean;
  isGlobal?: boolean;
  imageType?: DeepfakeImageType;
}

export interface VoiceSample {
  duration: number;
  engine: string;
  /** Provider credential id used for voice cloning. */
  providerId?: string;
  audioFile?: File | null;
  voiceId?: string;
  voiceName?: string;
  sampleUrl?: string;
}

/** Query params for GET /deepfake/voices. */
export interface IDeepfakeVoiceListParams {
  provider?: string;
  providerId?: string;
  offset?: number;
  pageSize?: number;
  language?: string;
  status?: string;
  search?: string;
}

/** Cloned voice resource returned by deepfake voice endpoints. */
export interface IDeepfakeVoice {
  voiceCloneId: string;
  voiceName?: string;
  provider: string;
  fileName: string;
  sampleUrl: string;
  language: string;
  status: string;
  usedFallbackVoice: boolean;
  createdAt: string;
}

export enum UploadToContentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  DONE = 'DONE',
  DELETED = 'DELETED',
}

/** Query params for GET /deepfake/videos (documented filters only). */
export interface IDeepfakeVideoListParams {
  offset?: number;
  pageSize?: number;
  uploadDate?: string;
  title?: string;
  description?: string;
}

/** Video resource returned by deepfake video endpoints. */
export interface IDeepfakeVideo {
  videoId?: string;
  id?: string;
  title?: string;
  description?: string;
  language?: string;
  provider?: string;
  providerId?: string;
  videoProvider?: string;
  model?: string;
  backgroundUrl?: string;
  backgroundPreviewUrl?: string;
  backgroundType?: 'PRESET' | 'CUSTOM' | string;
  backgroundPreset?: string;
  backgroundKey?: string;
  faceImageUrl?: string;
  facePreviewUrl?: string;
  faceKey?: string;
  faceConfirmed?: boolean;
  audioUrl?: string;
  audioPreviewUrl?: string;
  voiceCloneId?: string;
  voiceProvider?: string;
  voiceName?: string;
  script?: string;
  thumbnailUrl?: string;
  videoUrl?: string;
  durationSec?: number;
  uploadDate?: string;
  createdAt?: string;
  status?: string;
  renderProgress?: number;
  /** Last completed backend wizard step (1–6). */
  currentStep?: number;
  uploadToContent?: UploadToContentStatus | string;
}

/**
 * Backend wizard steps: 1 onboarding, 2 face upload, 3 face confirm,
 * 4 voice, 5 script, 6 generate.
 * UI wizard steps: 1–5 (face upload+confirm are combined as UI step 2).
 */
export const resolveUiStepFromCompletedApiStep = (
  currentStep?: number,
): number => {
  const completed = currentStep ?? 0;
  if (completed <= 0) return 1;
  if (completed === 1) return 2;
  if (completed === 2) return 2;
  if (completed === 3) return 3;
  if (completed === 4) return 4;
  return 5;
};

export interface IDeepfakeVideoPage {
  id: string;
  title: string;
  uploadDate: string;
  uploadToContent?: UploadToContentStatus | string;
}

export interface IDeepfakeVideoStatus {
  videoId: string;
  status: 'DRAFT' | 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  videoUrl: string;
  failureReason: string;
}

export interface IDeepfakeMicroContentRequest {
  videoIds: string[];
}

export interface IDeepfakeBackgroundUploadResponse {
  backgroundKey: string;
  backgroundPreviewUrl: string;
}

export interface IDeepfakeStep1StartPayload {
  title: string;
  description?: string;
  language: string;
  backgroundType: 'CUSTOM';
  backgroundKey: string;
}

export type IDeepfakeStep1UpdatePayload = IDeepfakeStep1StartPayload;

export type IDeepfakeStep2Input =
  | { file: File; faceImageId?: never }
  | { file?: never; faceImageId: string };

export interface IDeepfakeStep3Payload {
  confirmed: boolean;
}

export interface IDeepfakeStep5Payload {
  script: string;
  variables?: {
    [x: string]: string;
  };
}

export interface IDeepfakeStep6Payload {
  providerId?: string;
}

/** Prefer a direct public/key URL over a signed preview when both exist. */
export const resolveBackgroundDisplayUrl = (video: IDeepfakeVideo): string => {
  const key = video.backgroundKey || '';
  if (/^https?:\/\//i.test(key)) return key;

  const preview = video.backgroundPreviewUrl || video.backgroundUrl || '';
  if (/^https?:\/\//i.test(preview)) return preview;

  return key || preview;
};

export const resolveFaceDisplayUrl = (video: IDeepfakeVideo): string =>
  video.facePreviewUrl || video.faceImageUrl || '';

export const TEMPLATE_SCRIPT = `Hi [recipient_name], this is [executive_name] from [company_name].
We're running an urgent verification on your account today. Please follow the secure link I'll send you and confirm your credentials within the next 10 minutes.
Thanks for your prompt cooperation.`;

export const STEP_HEADERS: Record<
  string,
  { eyebrow: string; title: string; desc: string }
> = {
  1: {
    eyebrow: 'Step 1 · Onboarding',
    title: "Let's set up your training video",
    desc: "Choose to upload a photo or capture your face in real time. Then you'll record a voice sample and customise the script.",
  },
  '2_1': {
    eyebrow: 'Step 2 · Face setup',
    title: 'Upload a photo or capture live (5s timer)',
    desc: 'Upload a static photo or activate the webcam for a 5-second selfie. The capture becomes the avatar base.',
  },
  '2_2': {
    eyebrow: 'Step 2 · Face setup',
    title: 'Review & confirm the captured face',
    desc: "Confirm to lock in the finalised face model ready for animation, or retake if the framing isn't right.",
  },
  3: {
    eyebrow: 'Step 3 · Voice',
    title: 'Clone your voice',
    desc: 'Record the consent script aloud, or upload a pre-recorded sample. We train a one-shot voice clone you can re-use across any future script.',
  },
  4: {
    eyebrow: 'Step 4 · Script',
    title: 'Customise the message',
    desc: 'Edit the script and fill placeholders like [recipient_name] or [company_name]. Variables get baked into the rendered video.',
  },
  5: {
    eyebrow: 'Step 5 · Generate',
    title: 'Real-time deepfake generation',
    desc: 'Pick an AI engine, then fuse the face model, cloned voice and script. Lip-sync is mapped in seconds.',
  },
} as const;
