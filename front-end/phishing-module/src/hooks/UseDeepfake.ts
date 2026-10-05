import {
  IDeepfakeBackgroundUploadResponse,
  IDeepfakeStep1StartPayload,
  IDeepfakeStep1UpdatePayload,
  IDeepfakeStep3Payload,
  IDeepfakeStep5Payload,
  IDeepfakeStep6Payload,
  IDeepfakeMicroContentRequest,
  IDeepfakeImage,
  IDeepfakeImageListParams,
  IDeepfakeStep2Input,
  IDeepfakeVideo,
  IDeepfakeVideoListParams,
  IDeepfakeVideoPage,
  IDeepfakeVideoStatus,
  IDeepfakeVoice,
  IDeepfakeVoiceListParams,
  UploadToContentStatus,
} from 'models/Deepfake';
import { IResponse, IList } from 'models/Global';
import { useCallback, useState } from 'react';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { useAPI } from './UseAPI';

const STATUS_POLL_INTERVAL_MS = 2000;
const STATUS_MAX_ATTEMPTS = 90;

const statusToProgress = (
  status: IDeepfakeVideoStatus['status'],
  attempt: number,
): number => {
  switch (status) {
    case 'COMPLETED':
      return 100;
    case 'PROCESSING':
      return Math.min(90, 40 + attempt * 2);
    case 'PENDING':
      return 25;
    case 'DRAFT':
      return 10;
    case 'FAILED':
    default:
      return 0;
  }
};

const resolveVideoId = (video: IDeepfakeVideo | null | undefined): string =>
  video?.videoId ?? video?.id ?? '';

const resolveVoiceId = (voice: IDeepfakeVoice | null | undefined): string =>
  voice?.voiceCloneId ?? '';

interface UseDeepfakeReturn {
  loading: boolean;
  saving: boolean;
  generating: boolean;
  videos: IList<IDeepfakeVideo>;
  voices: IList<IDeepfakeVoice>;
  voicesLoading: boolean;
  fetchVideos: (
    params?: IDeepfakeVideoListParams,
    options?: { silent?: boolean },
  ) => Promise<void>;
  fetchVoices: (params?: IDeepfakeVoiceListParams) => Promise<IDeepfakeVoice[]>;
  fetchImages: (
    params?: IDeepfakeImageListParams,
  ) => Promise<IList<IDeepfakeImage>>;
  deleteVoice: (voiceCloneId: string) => Promise<boolean>;
  getVideoById: (videoId: string) => Promise<IDeepfakeVideo | null>;
  startWizard: (
    data: IDeepfakeStep1StartPayload,
  ) => Promise<IResponse<IDeepfakeVideo | null> | null>;
  uploadBackground: (
    file: File,
  ) => Promise<IResponse<IDeepfakeBackgroundUploadResponse | null> | null>;
  updateStep1: (
    videoId: string,
    data: IDeepfakeStep1UpdatePayload,
  ) => Promise<IResponse<IDeepfakeVideo | null> | null>;
  updateStep2: (
    videoId: string,
    input: IDeepfakeStep2Input,
  ) => Promise<IResponse<IDeepfakeVideo | null> | null>;
  updateStep3: (
    videoId: string,
    data: IDeepfakeStep3Payload,
  ) => Promise<IResponse<IDeepfakeVideo | null> | null>;
  updateStep4: (
    videoId: string,
    options: {
      file?: File | null;
      voiceCloneId?: string;
      voiceName?: string;
      provider?: string;
      providerId?: string;
      language?: string;
    },
  ) => Promise<IResponse<IDeepfakeVideo | null> | null>;
  updateStep5: (
    videoId: string,
    data: IDeepfakeStep5Payload,
  ) => Promise<IResponse<IDeepfakeVideo | null> | null>;
  updateStep6: (
    videoId: string,
    data: IDeepfakeStep6Payload,
  ) => Promise<IResponse<IDeepfakeVideo | null> | null>;
  getVideoStatus: (videoId: string) => Promise<IDeepfakeVideoStatus | null>;
  pollRenderStatus: (
    videoId: string,
    onProgress?: (progress: number) => void,
  ) => Promise<IDeepfakeVideoStatus | null>;
  deleteVideo: (videoId: string) => Promise<boolean>;
  addAsMicroContent: (
    videoIds: string[],
  ) => Promise<{ ok: boolean; message: string }>;
  removeMicroContent: (
    videoId: string,
  ) => Promise<{ ok: boolean; message: string }>;
  patchVideoUploadToContent: (
    videoId: string,
    uploadToContent?: UploadToContentStatus,
  ) => void;
}

export const useDeepfake = (): UseDeepfakeReturn => {
  const { get, post, put, del } = useAPI();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [voicesLoading, setVoicesLoading] = useState(false);
  const [videos, setVideos] = useState<IList<IDeepfakeVideoPage>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 0,
  });
  const [voices, setVoices] = useState<IList<IDeepfakeVoice>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 0,
  });

  const fetchVideos = useCallback(
    async (
      params: IDeepfakeVideoListParams = {},
      options?: { silent?: boolean },
    ): Promise<void> => {
      if (!options?.silent) {
        setLoading(true);
      }
      try {
        const queryString = deepfakeQueryString({
          offset: params.offset,
          pageSize: params.pageSize,
          uploadDate: params.uploadDate,
          title: params.title,
          description: params.description,
        });
        const response = (await get(
          `${API_END_POINTS.DEEPFAKE_VIDEO_LIST}${queryString}`,
        )) as IResponse<IList<IDeepfakeVideoPage>> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          setVideos({
            items: [],
            total: 0,
            offset: params.offset ?? 0,
            pageSize: params.pageSize ?? 0,
          });
          return;
        }

        const next = response.data ?? {
          items: [],
          total: 0,
          offset: 0,
          pageSize: 0,
        };

        setVideos(previous => {
          const previousById = new Map(
            previous.items.map(item => [
              (item as IDeepfakeVideo).videoId ?? item.id ?? '',
              item,
            ]),
          );

          return {
            ...next,
            items: next.items.map(item => {
              const itemId = (item as IDeepfakeVideo).videoId ?? item.id ?? '';
              const existing = previousById.get(itemId);
              if (item.uploadToContent || !existing?.uploadToContent) {
                return item;
              }
              return { ...item, uploadToContent: existing.uploadToContent };
            }),
          };
        });
      } catch (error) {
        console.error('Error fetching deepfake videos:', error);
        setVideos({
          items: [],
          total: 0,
          offset: params.offset ?? 0,
          pageSize: params.pageSize ?? 0,
        });
      } finally {
        if (!options?.silent) {
          setLoading(false);
        }
      }
    },
    [get],
  );

  const fetchVoices = useCallback(
    async (
      params: IDeepfakeVoiceListParams = {},
    ): Promise<IDeepfakeVoice[]> => {
      setVoicesLoading(true);
      try {
        const queryString = deepfakeQueryString({
          provider: params.provider,
          providerId: params.providerId,
          offset: params.offset ?? 0,
          pageSize: params.pageSize ?? 10,
          language: params.language,
          status: params.status,
          search: params.search,
        });
        const response = (await get(
          `${API_END_POINTS.DEEPFAKE_VOICE_LIST}${queryString}`,
        )) as IResponse<IList<IDeepfakeVoice>> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          setVoices({
            items: [],
            total: 0,
            offset: params.offset ?? 0,
            pageSize: params.pageSize ?? 10,
          });
          return [];
        }

        const data = response.data ?? {
          items: [],
          total: 0,
          offset: 0,
          pageSize: 0,
        };
        setVoices(data);
        return data.items ?? [];
      } catch (error) {
        console.error('Error fetching deepfake voices:', error);
        setVoices({
          items: [],
          total: 0,
          offset: params.offset ?? 0,
          pageSize: params.pageSize ?? 10,
        });
        return [];
      } finally {
        setVoicesLoading(false);
      }
    },
    [get],
  );

  const getVideoById = useCallback(
    async (videoId: string): Promise<IDeepfakeVideo | null> => {
      setLoading(true);
      try {
        const response = (await get(
          API_END_POINTS.DEEPFAKE_VIDEO_DETAILS(videoId),
        )) as IResponse<IDeepfakeVideo> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return null;
        }

        return response.data ?? null;
      } catch (error) {
        console.error('Error fetching deepfake video:', error);
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const fetchImages = useCallback(
    async (
      params: IDeepfakeImageListParams = {},
    ): Promise<IList<IDeepfakeImage>> => {
      const fallback = {
        items: [],
        total: 0,
        offset: params.offset ?? 0,
        pageSize: params.pageSize ?? 10,
      };
      try {
        const queryString = deepfakeQueryString({
          offset: params.offset ?? 0,
          pageSize: params.pageSize ?? 10,
          fileName: params.fileName,
          uploadDate: params.uploadDate,
          isActive: params.isActive,
          isGlobal: params.isGlobal,
          imageType: params.imageType,
        });
        const response = (await get(
          `${API_END_POINTS.DEEPFAKE_IMAGE_LIST}${queryString}`,
        )) as IResponse<IList<IDeepfakeImage>> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return fallback;
        }

        return response.data ?? fallback;
      } catch (error) {
        console.error('Error fetching deepfake images:', error);
        return fallback;
      }
    },
    [get],
  );

  const deleteVoice = useCallback(
    async (voiceCloneId: string): Promise<boolean> => {
      try {
        const response = (await del(
          API_END_POINTS.DEEPFAKE_VOICE_DELETE(voiceCloneId),
        )) as IResponse<null> | undefined;
        return !!response && isSuccessResponse(response.statusCode);
      } catch (error) {
        console.error('Error deleting cloned voice:', error);
        return false;
      }
    },
    [del],
  );

  const startWizard = useCallback(
    async (
      data: IDeepfakeStep1StartPayload,
    ): Promise<IResponse<IDeepfakeVideo | null> | null> => {
      setSaving(true);
      try {
        const response = (await post(
          API_END_POINTS.DEEPFAKE_VIDEO_START_STEP_1,
          {
            data,
          },
        )) as IResponse<IDeepfakeVideo | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error starting deepfake wizard:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const uploadBackground = useCallback(
    async (
      file: File,
    ): Promise<IResponse<IDeepfakeBackgroundUploadResponse | null> | null> => {
      setSaving(true);
      try {
        const formData = new FormData();
        formData.append('file', file);

        const response = (await post(
          API_END_POINTS.DEEPFAKE_UPLOAD_BACKGROUND,
          {
            data: formData,
            headers: { 'Content-Type': 'multipart/form-data' },
          },
        )) as IResponse<IDeepfakeBackgroundUploadResponse | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error uploading deepfake background:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [post],
  );

  const updateStep = useCallback(
    async <T>(
      videoId: string,
      step: number,
      data?: T,
      query?: Record<string, string | undefined>,
    ): Promise<IResponse<IDeepfakeVideo | null> | null> => {
      setSaving(true);
      try {
        const queryString = query ? deepfakeQueryString(query) : '';
        const url = `${API_END_POINTS.DEEPFAKE_VIDEO_UPDATE_STEP(videoId, step)}${
          queryString ? `?${queryString}` : ''
        }`;
        const response = (await put(url, data ? { data } : {})) as
          | IResponse<IDeepfakeVideo | null>
          | undefined;
        return response ?? null;
      } catch (error) {
        console.error(`Error updating deepfake step ${step}:`, error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  const updateStep1 = useCallback(
    (videoId: string, data: IDeepfakeStep1UpdatePayload) =>
      updateStep(videoId, 1, data),
    [updateStep],
  );

  const updateStep2 = useCallback(
    async (
      videoId: string,
      input: IDeepfakeStep2Input,
    ): Promise<IResponse<IDeepfakeVideo | null> | null> => {
      setSaving(true);
      try {
        if ('faceImageId' in input) {
          const queryString = deepfakeQueryString({
            faceImageId: input.faceImageId,
          });
          const response = (await put(
            `${API_END_POINTS.DEEPFAKE_VIDEO_UPDATE_STEP(videoId, 2)}?${queryString}`,
            {},
          )) as IResponse<IDeepfakeVideo | null> | undefined;
          return response ?? null;
        }

        const formData = new FormData();
        formData.append('file', input.file);

        const response = (await put(
          API_END_POINTS.DEEPFAKE_VIDEO_UPDATE_STEP(videoId, 2),
          {
            data: formData,
            headers: { 'Content-Type': 'multipart/form-data' },
          },
        )) as IResponse<IDeepfakeVideo | null> | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error updating deepfake step 2:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  const updateStep3 = useCallback(
    (videoId: string, data: IDeepfakeStep3Payload) =>
      updateStep(videoId, 3, data),
    [updateStep],
  );

  const updateStep4 = useCallback(
    async (
      videoId: string,
      options: {
        file?: File | null;
        voiceCloneId?: string;
        voiceName?: string;
        provider?: string;
        providerId?: string;
        language?: string;
      },
    ): Promise<IResponse<IDeepfakeVideo | null> | null> => {
      setSaving(true);
      try {
        // Existing clone: PUT .../step/4?voiceCloneId=...
        // New sample: PUT .../step/4?provider=...&providerId=...&voiceName=...
        // &language=... with the multipart file.
        const queryString = options.file
          ? deepfakeQueryString({
              provider: options.provider,
              providerId: options.providerId,
              language: options.language,
              voiceName: options.voiceName,
            })
          : deepfakeQueryString({ voiceCloneId: options.voiceCloneId });
        const url = `${API_END_POINTS.DEEPFAKE_VIDEO_UPDATE_STEP(videoId, 4)}${
          queryString ? `?${queryString}` : ''
        }`;

        if (options.file) {
          const formData = new FormData();
          formData.append('file', options.file);

          const response = (await put(url, {
            data: formData,
            headers: { 'Content-Type': 'multipart/form-data' },
          })) as IResponse<IDeepfakeVideo | null> | undefined;
          return response ?? null;
        }

        const response = (await put(url, {})) as
          | IResponse<IDeepfakeVideo | null>
          | undefined;
        return response ?? null;
      } catch (error) {
        console.error('Error updating deepfake step 4:', error);
        return null;
      } finally {
        setSaving(false);
      }
    },
    [put],
  );

  const updateStep5 = useCallback(
    (videoId: string, data: IDeepfakeStep5Payload) =>
      updateStep(videoId, 5, data),
    [updateStep],
  );

  const updateStep6 = useCallback(
    (videoId: string, data: IDeepfakeStep6Payload) =>
      updateStep(videoId, 6, data),
    [updateStep],
  );

  const getVideoStatus = useCallback(
    async (videoId: string): Promise<IDeepfakeVideoStatus | null> => {
      try {
        const response = (await get(
          API_END_POINTS.DEEPFAKE_VIDEO_STATUS(videoId),
        )) as IResponse<IDeepfakeVideoStatus> | undefined;
        return response?.data ?? null;
      } catch (error) {
        console.error('Error fetching deepfake video status:', error);
        return null;
      }
    },
    [get],
  );

  const pollRenderStatus = useCallback(
    async (
      videoId: string,
      onProgress?: (progress: number) => void,
    ): Promise<IDeepfakeVideoStatus | null> => {
      setGenerating(true);
      try {
        for (let attempt = 0; attempt < STATUS_MAX_ATTEMPTS; attempt++) {
          const status = await getVideoStatus(videoId);
          if (!status) {
            await new Promise(resolve =>
              setTimeout(resolve, STATUS_POLL_INTERVAL_MS),
            );
            continue;
          }

          const progress = statusToProgress(status.status, attempt);
          onProgress?.(progress);

          if (status.status === 'COMPLETED') {
            return status;
          }

          if (status.status === 'FAILED') {
            return status;
          }

          await new Promise(resolve =>
            setTimeout(resolve, STATUS_POLL_INTERVAL_MS),
          );
        }

        return null;
      } finally {
        setGenerating(false);
      }
    },
    [getVideoStatus],
  );

  const deleteVideo = useCallback(
    async (videoId: string): Promise<boolean> => {
      setSaving(true);
      try {
        const response = (await del(
          API_END_POINTS.DEEPFAKE_VIDEO_DELETE(videoId),
        )) as IResponse<null> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return false;
        }

        return true;
      } catch (error) {
        console.error('Error deleting deepfake video:', error);
        return false;
      } finally {
        setSaving(false);
      }
    },
    [del],
  );

  const patchVideoUploadToContent = useCallback(
    (videoId: string, uploadToContent?: UploadToContentStatus) => {
      setVideos(previous => ({
        ...previous,
        items: previous.items.map(item => {
          const itemId = (item as IDeepfakeVideo).videoId ?? item.id ?? '';
          if (itemId !== videoId) return item;
          return { ...item, uploadToContent };
        }),
      }));
    },
    [],
  );

  const addAsMicroContent = useCallback(
    async (videoIds: string[]): Promise<{ ok: boolean; message: string }> => {
      setSaving(true);
      try {
        const payload: IDeepfakeMicroContentRequest = { videoIds };
        const response = (await post(API_END_POINTS.DEEPFAKE_MICRO_CONTENT, {
          data: payload,
        })) as IResponse<unknown> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message: response?.message || 'Failed to add as content',
          };
        }

        videoIds.forEach(videoId =>
          patchVideoUploadToContent(videoId, UploadToContentStatus.PROCESSING),
        );

        return {
          ok: true,
          message: response.message || 'Added as content successfully',
        };
      } catch (error) {
        console.error('Error adding deepfake as micro content:', error);
        return {
          ok: false,
          message: 'Failed to add as content',
        };
      } finally {
        setSaving(false);
      }
    },
    [patchVideoUploadToContent, post],
  );

  const removeMicroContent = useCallback(
    async (videoId: string): Promise<{ ok: boolean; message: string }> => {
      setSaving(true);
      try {
        const response = (await del(
          API_END_POINTS.DEEPFAKE_MICRO_CONTENT_DELETE(videoId),
        )) as IResponse<null> | undefined;

        if (!response || !isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message: response?.message || 'Failed to remove content',
          };
        }

        patchVideoUploadToContent(videoId, UploadToContentStatus.DELETED);

        return {
          ok: true,
          message: response.message || 'Removed from content successfully',
        };
      } catch (error) {
        console.error('Error removing deepfake micro content:', error);
        return {
          ok: false,
          message: 'Failed to remove content',
        };
      } finally {
        setSaving(false);
      }
    },
    [del, patchVideoUploadToContent],
  );

  const deepfakeQueryString = (
    params: Record<string, string | number | boolean | undefined>,
  ): string => {
    return Object.entries(params)
      .filter(
        ([, value]) => value !== undefined && value !== null && value !== '',
      )
      .map(
        ([key, value]) =>
          `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`,
      )
      .join('&');
  };

  return {
    loading,
    saving,
    generating,
    videos,
    voices,
    voicesLoading,
    fetchVideos,
    fetchVoices,
    fetchImages,
    deleteVoice,
    getVideoById,
    startWizard,
    uploadBackground,
    updateStep1,
    updateStep2,
    updateStep3,
    updateStep4,
    updateStep5,
    updateStep6,
    getVideoStatus,
    pollRenderStatus,
    deleteVideo,
    addAsMicroContent,
    removeMicroContent,
    patchVideoUploadToContent,
  };
};

export { resolveVideoId, resolveVoiceId };
