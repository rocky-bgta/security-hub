import { useCallback, useState } from 'react';
import { useAPI } from 'hooks/UseAPI';
import type { IList } from 'models/Global';
import type {
  IVishingVoice,
  IVishingVoiceListParams,
} from 'models/Vishing';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  isSuccessResponse,
  normalizePaginatedList,
  objectToQueryString,
} from 'utils/Helper';
import { mapVishingVoiceToProfile } from 'utils/VishingVoiceClone';
import type { IResponse } from 'models/Global';

export interface IVoiceCloneProfile {
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
}

interface UseVoiceCloneReturn {
  loading: boolean;
  listVoiceClones: (
    params?: IVishingVoiceListParams,
  ) => Promise<IList<IVoiceCloneProfile>>;
  deleteVoiceClone: (voiceCloneId: string) => Promise<boolean>;
}

export const useVoiceClone = (): UseVoiceCloneReturn => {
  const { get, del } = useAPI();
  const [loading, setLoading] = useState(false);

  const listVoiceClones = useCallback(
    async (
      params: IVishingVoiceListParams = {},
    ): Promise<IList<IVoiceCloneProfile>> => {
      setLoading(true);
      try {
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = await get(
          `${API_END_POINTS.VISHING_VOICES_LIST}${queryString}`,
        );
        const list = normalizePaginatedList<IVishingVoice>(response);
        return {
          ...list,
          items: list.items.map(mapVishingVoiceToProfile),
        };
      } catch (error) {
        console.error('Error listing voice clones:', error);
        return { offset: 0, pageSize: 0, total: 0, items: [] };
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  const deleteVoiceClone = useCallback(
    async (voiceCloneId: string): Promise<boolean> => {
      try {
        const response = (await del(
          API_END_POINTS.DEEPFAKE_VOICE_DELETE(voiceCloneId),
        )) as IResponse<null> | undefined;
        return !!response && isSuccessResponse(response.statusCode);
      } catch (error) {
        console.error('Error deleting vishing voice clone:', error);
        return false;
      }
    },
    [del],
  );

  return {
    loading,
    listVoiceClones,
    deleteVoiceClone,
  };
};
