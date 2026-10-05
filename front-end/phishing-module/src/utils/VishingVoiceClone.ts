import type { IVishingVoice } from 'models/Vishing';

const displayFileName = (fileName?: string): string => {
  if (!fileName?.trim()) return 'Cloned Voice';
  return fileName.replace(/\.[^/.]+$/, '') || fileName;
};

export const mapVishingVoiceToProfile = (
  voice: IVishingVoice,
): {
  id: string;
  name: string;
  role: string;
  gender: 'Male' | 'Female' | 'Neutral';
  tone: string;
  language: string;
  cloned: boolean;
  sampleUrl?: string;
  provider?: string;
  status?: string;
} => ({
  id: voice.voiceCloneId,
  name: voice.voiceName?.trim() || displayFileName(voice.fileName),
  role: voice.provider || 'Custom',
  gender: 'Neutral',
  tone: voice.status || 'Professional',
  language: voice.language || 'English (US)',
  cloned: true,
  sampleUrl: voice.sampleUrl || undefined,
  provider: voice.provider,
  status: voice.status,
});
