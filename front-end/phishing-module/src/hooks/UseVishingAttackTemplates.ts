import { useCallback, useState } from 'react';
import { toast } from 'react-toastify';
import { useAPI } from 'hooks/UseAPI';
import type { IGetListParams, IList, IResponse } from 'models/Global';
import type {
  IVishingAttackTemplate,
  IVishingAttackTemplateRequest,
} from 'models/Vishing';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  isSuccessResponse,
  normalizePaginatedList,
  objectToQueryString,
} from 'utils/Helper';

export interface IVishingAttackTemplateActionResult {
  ok: boolean;
  message: string;
  data?: IVishingAttackTemplate | null;
}

interface UseVishingAttackTemplatesReturn {
  loading: boolean;
  saving: boolean;
  fetchAttackTemplates: (
    params?: IGetListParams & { searchParam?: string },
  ) => Promise<IList<IVishingAttackTemplate>>;
  getAttackTemplateById: (id: string) => Promise<IVishingAttackTemplate | null>;
  createAttackTemplate: (
    data: IVishingAttackTemplateRequest,
  ) => Promise<IVishingAttackTemplateActionResult>;
  updateAttackTemplate: (
    id: string,
    data: IVishingAttackTemplateRequest,
  ) => Promise<IVishingAttackTemplateActionResult>;
  deleteAttackTemplate: (
    id: string,
  ) => Promise<IVishingAttackTemplateActionResult>;
}

export const useVishingAttackTemplates =
  (): UseVishingAttackTemplatesReturn => {
    const { get, post, put, del } = useAPI();
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    const fetchAttackTemplates = useCallback(
      async (
        params: IGetListParams & { searchParam?: string } = {},
      ): Promise<IList<IVishingAttackTemplate>> => {
        setLoading(true);
        try {
          const queryString = objectToQueryString(
            params as Record<string, unknown>,
          );
          const response = (await get(
            `${API_END_POINTS.VISHING_ATTACK_TEMPLATE_LIST}${queryString}`,
          )) as IResponse<IList<IVishingAttackTemplate>> | undefined;

          if (!response || !isSuccessResponse(response.statusCode)) {
            toast.error(
              response?.message || 'Failed to load attack templates.',
            );
            return { offset: 0, pageSize: 0, total: 0, items: [] };
          }

          return normalizePaginatedList<IVishingAttackTemplate>(response.data);
        } catch (error) {
          console.error('Error fetching vishing attack templates:', error);
          toast.error('Failed to load attack templates.');
          return { offset: 0, pageSize: 0, total: 0, items: [] };
        } finally {
          setLoading(false);
        }
      },
      [get],
    );

    const getAttackTemplateById = useCallback(
      async (id: string): Promise<IVishingAttackTemplate | null> => {
        setLoading(true);
        try {
          const response = (await get(
            API_END_POINTS.VISHING_ATTACK_TEMPLATE_DETAILS(id),
          )) as IResponse<IVishingAttackTemplate> | undefined;

          if (!response || !isSuccessResponse(response.statusCode)) {
            toast.error(
              response?.message || 'Failed to load attack template.',
            );
            return null;
          }

          return response.data ?? null;
        } catch (error) {
          console.error('Error fetching vishing attack template:', error);
          toast.error('Failed to load attack template.');
          return null;
        } finally {
          setLoading(false);
        }
      },
      [get],
    );

    const createAttackTemplate = useCallback(
      async (
        data: IVishingAttackTemplateRequest,
      ): Promise<IVishingAttackTemplateActionResult> => {
        setSaving(true);
        try {
          const response = (await post(
            API_END_POINTS.VISHING_ATTACK_TEMPLATE_CREATE,
            { data },
          )) as IResponse<IVishingAttackTemplate> | undefined;

          if (!response || !isSuccessResponse(response.statusCode)) {
            return {
              ok: false,
              message:
                response?.message || 'Failed to create attack template.',
            };
          }

          return {
            ok: true,
            message:
              response.message || 'Attack template created successfully.',
            data: response.data,
          };
        } catch (error) {
          console.error('Error creating vishing attack template:', error);
          return {
            ok: false,
            message: 'Failed to create attack template.',
          };
        } finally {
          setSaving(false);
        }
      },
      [post],
    );

    const updateAttackTemplate = useCallback(
      async (
        id: string,
        data: IVishingAttackTemplateRequest,
      ): Promise<IVishingAttackTemplateActionResult> => {
        setSaving(true);
        try {
          const response = (await put(
            API_END_POINTS.VISHING_ATTACK_TEMPLATE_DETAILS(id),
            { data },
          )) as IResponse<IVishingAttackTemplate> | undefined;

          if (!response || !isSuccessResponse(response.statusCode)) {
            return {
              ok: false,
              message:
                response?.message || 'Failed to update attack template.',
            };
          }

          return {
            ok: true,
            message:
              response.message || 'Attack template updated successfully.',
            data: response.data,
          };
        } catch (error) {
          console.error('Error updating vishing attack template:', error);
          return {
            ok: false,
            message: 'Failed to update attack template.',
          };
        } finally {
          setSaving(false);
        }
      },
      [put],
    );

    const deleteAttackTemplate = useCallback(
      async (id: string): Promise<IVishingAttackTemplateActionResult> => {
        setSaving(true);
        try {
          const response = (await del(
            API_END_POINTS.VISHING_ATTACK_TEMPLATE_DETAILS(id),
          )) as IResponse<null> | undefined;

          if (!response || !isSuccessResponse(response.statusCode)) {
            return {
              ok: false,
              message:
                response?.message || 'Failed to delete attack template.',
            };
          }

          return {
            ok: true,
            message:
              response.message || 'Attack template deleted successfully.',
          };
        } catch (error) {
          console.error('Error deleting vishing attack template:', error);
          return {
            ok: false,
            message: 'Failed to delete attack template.',
          };
        } finally {
          setSaving(false);
        }
      },
      [del],
    );

    return {
      loading,
      saving,
      fetchAttackTemplates,
      getAttackTemplateById,
      createAttackTemplate,
      updateAttackTemplate,
      deleteAttackTemplate,
    };
  };
