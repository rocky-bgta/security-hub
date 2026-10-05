import { useCallback, useState } from 'react';

import {
  IAIModelItem,
  IAIModelRequest,
  IAIProviderConfigItem,
  IAIProviderConfigRequest,
  AIProviderType,
} from 'models/AiProvider';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse } from 'utils/Helper';
import { useAPI } from './UseAPI';

export interface ISaveAIProviderConfigResult {
  ok: boolean;
  message: string;
}

export interface IListAIProviderConfigResult {
  ok: boolean;
  message: string;
  data: IAIProviderConfigItem[];
}

export interface IListAIModelResult {
  ok: boolean;
  message: string;
  data: IAIModelItem[];
}

/**
 * Persists AI provider credentials (API hook only; UI handles toasts).
 */
const useAIProviderConfig = () => {
  const apiClient = useAPI();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isLoadingList, setIsLoadingList] = useState(false);
  const [isSubmittingModel, setIsSubmittingModel] = useState(false);
  const [isLoadingModelList, setIsLoadingModelList] = useState(false);

  const saveProviderConfig = useCallback(
    async (
      request: IAIProviderConfigRequest,
    ): Promise<ISaveAIProviderConfigResult> => {
      setIsSubmitting(true);
      try {
        const response = await apiClient.post(
          API_END_POINTS.AI_PROVIDER_CONFIG,
          { data: request },
        );

        if (!isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message: response.message || 'Failed to save AI provider settings',
          };
        }

        return {
          ok: true,
          message:
            response.message || 'AI provider settings saved successfully',
        };
      } catch (error) {
        console.error('Error saving AI provider config:', error);
        return {
          ok: false,
          message: 'Failed to save AI provider settings',
        };
      } finally {
        setIsSubmitting(false);
      }
    },
    [apiClient],
  );

  const getProviderConfigList =
    useCallback(async (): Promise<IListAIProviderConfigResult> => {
      setIsLoadingList(true);
      try {
        const response = await apiClient.get(API_END_POINTS.AI_PROVIDER_CONFIG);

        if (!isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message:
              response.message || 'Failed to fetch AI provider configurations',
            data: [],
          };
        }

        return {
          ok: true,
          message:
            response.message ||
            'AI provider configurations loaded successfully',
          data: response.data || [],
        };
      } catch (error) {
        console.error('Error loading AI provider config list:', error);
        return {
          ok: false,
          message: 'Failed to fetch AI provider configurations',
          data: [],
        };
      } finally {
        setIsLoadingList(false);
      }
    }, [apiClient]);

  const getModelList = useCallback(
    async (providerType?: AIProviderType): Promise<IListAIModelResult> => {
      setIsLoadingModelList(true);
      try {
        const queryString = providerType
          ? `?providerType=${encodeURIComponent(providerType)}`
          : '';
        const response = await apiClient.get(
          `${API_END_POINTS.AI_MODELS}${queryString}`,
        );

        if (!isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message: response.message || 'Failed to fetch AI models',
            data: [],
          };
        }

        return {
          ok: true,
          message: response.message || 'AI models loaded successfully',
          data: response.data || [],
        };
      } catch (error) {
        console.error('Error loading AI model list:', error);
        return {
          ok: false,
          message: 'Failed to fetch AI models',
          data: [],
        };
      } finally {
        setIsLoadingModelList(false);
      }
    },
    [apiClient],
  );

  const saveModel = useCallback(
    async (request: IAIModelRequest): Promise<ISaveAIProviderConfigResult> => {
      setIsSubmittingModel(true);
      try {
        const response = await apiClient.post(API_END_POINTS.AI_MODELS, {
          data: request,
        });

        if (!isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message: response.message || 'Failed to create AI model',
          };
        }

        return {
          ok: true,
          message: response.message || 'AI model created successfully',
        };
      } catch (error) {
        console.error('Error saving AI model:', error);
        return {
          ok: false,
          message: 'Failed to create AI model',
        };
      } finally {
        setIsSubmittingModel(false);
      }
    },
    [apiClient],
  );

  const deleteModel = useCallback(
    async (id: string): Promise<ISaveAIProviderConfigResult> => {
      setIsSubmittingModel(true);
      try {
        const response = await apiClient.del(
          API_END_POINTS.AI_MODEL_DETAILS(id),
        );

        if (response?.statusCode && !isSuccessResponse(response.statusCode)) {
          return {
            ok: false,
            message: response.message || 'Failed to delete AI model',
          };
        }

        return {
          ok: true,
          message: response?.message || 'AI model deleted successfully',
        };
      } catch (error) {
        console.error('Error deleting AI model:', error);
        return {
          ok: false,
          message: 'Failed to delete AI model',
        };
      } finally {
        setIsSubmittingModel(false);
      }
    },
    [apiClient],
  );

  return {
    isSubmitting,
    isLoadingList,
    isSubmittingModel,
    isLoadingModelList,
    saveProviderConfig,
    getProviderConfigList,
    getModelList,
    saveModel,
    deleteModel,
  };
};

export default useAIProviderConfig;
