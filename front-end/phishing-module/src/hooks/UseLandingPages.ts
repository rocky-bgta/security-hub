import { IList, IResponse } from 'models/Global';
import {
  IAILandingPageParams,
  IImportedSite,
  ILandingPage,
  ILandingPageCreateForm,
  ILandingPageFixHtmlRequest,
  ILandingPageListParams,
  ILandingPagePreview,
  ILandingPageUpdateForm,
  IValidationResult,
  ITemplateLandingPage,
} from 'models/LandingPage';
import { useCallback, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { isSuccessResponse, objectToQueryString } from 'utils/Helper';
import { useAPI } from './UseAPI';

/**
 * Custom hook for landing page management
 * Based on Task-04, Task-05 Landing Page Creation
 */

function getApiErrorMessage(response: {
  message?: string;
  data?: unknown;
}): string {
  const nested =
    response.data &&
    typeof response.data === 'object' &&
    response.data !== null &&
    'message' in response.data &&
    typeof (response.data as { message?: string }).message === 'string'
      ? (response.data as { message: string }).message.trim()
      : '';
  return (
    (response.message && response.message.trim()) ||
    nested ||
    'Failed to create landing page.'
  );
}
export const useLandingPages = () => {
  const { get, put, post, del } = useAPI();

  const [landingPages, setLandingPages] = useState<IList<ILandingPage>>({
    items: [],
    total: 0,
    offset: 0,
    pageSize: 0,
  });
  const [loading, setLoading] = useState<boolean>(false);
  const [updating, setUpdating] = useState<boolean>(false);
  const [creating, setCreating] = useState<boolean>(false);
  const [importing, setImporting] = useState<boolean>(false);
  const [generating, setGenerating] = useState<boolean>(false);
  const [fixingHtml, setFixingHtml] = useState<boolean>(false);
  const [totalCount, setTotalCount] = useState<number>(0);

  /**
   * Fetch landing pages with pagination and filters
   */
  const fetchLandingPages = useCallback(
    async (params: ILandingPageListParams): Promise<void> => {
      try {
        setLoading(true);
        const queryString = objectToQueryString(
          params as Record<string, unknown>,
        );
        const response = await get(
          `${API_END_POINTS.LANDING_PAGE_LIST}${queryString}`,
        );

        if (response?.data) {
          setLandingPages({
            items: response.data.items || [],
            total: response.data.total || 0,
            offset: response.data.offset || 0,
            pageSize: response.data.pageSize || 0,
          });
          setTotalCount(response.data.total || 0);
        }
      } catch (error) {
        console.error('Error fetching landing pages:', error);
        toast.error('Failed to load landing pages');
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Fetch landing pages linked to an email template (campaign wizard recommendations)
   */
  const fetchLandingPagesByTemplate = useCallback(
    async (templateId: string): Promise<ITemplateLandingPage[]> => {
      try {
        const response = (await get(
          API_END_POINTS.LANDING_PAGES_BY_TEMPLATE(templateId),
        )) as IResponse<ITemplateLandingPage[]> | undefined;

        if (isSuccessResponse(response?.statusCode ?? 0) && response?.data) {
          return response.data;
        }
        return [];
      } catch (error) {
        console.error('Error fetching template landing pages:', error);
        toast.error('Failed to load recommended landing pages');
        return [];
      }
    },
    [get],
  );

  /**
   * Get landing page by ID
   */
  const getLandingPageById = useCallback(
    async (pageId: string): Promise<ILandingPage | null> => {
      try {
        setLoading(true);
        const response = (await get(
          API_END_POINTS.LANDING_PAGE_DETAILS(pageId),
        )) as IResponse<ILandingPage> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching landing page:', error);
        toast.error('Failed to load landing page');
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Get landing page preview with full HTML content
   */
  const getLandingPagePreview = useCallback(
    async (pageId: string): Promise<ILandingPagePreview | null> => {
      try {
        setLoading(true);
        const response = (await get(
          API_END_POINTS.LANDING_PAGE_PREVIEW(pageId),
        )) as IResponse<ILandingPagePreview> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error fetching preview:', error);
        toast.error('Failed to load preview');
        return null;
      } finally {
        setLoading(false);
      }
    },
    [get],
  );

  /**
   * Update a landing page
   */
  const updateLandingPage = useCallback(
    async (
      pageId: string,
      data: ILandingPageUpdateForm,
    ): Promise<ILandingPage | null> => {
      try {
        setUpdating(true);
        const response = (await put(
          API_END_POINTS.LANDING_PAGE_UPDATE(pageId),
          { data },
        )) as IResponse<ILandingPage> | undefined;

        if (response?.data) {
          toast.success('Landing page updated successfully');
          // Update local state
          setLandingPages({
            items: landingPages.items.map(p =>
              p.pageId === pageId ? response.data! : p,
            ),
            total: landingPages.total,
            offset: landingPages.offset,
            pageSize: landingPages.pageSize,
          });
          return response.data;
        }
        return null;
      } catch (error: any) {
        console.error('Error updating landing page:', error);
        toast.error(
          error?.response?.data?.message || 'Failed to update landing page',
        );
        return null;
      } finally {
        setUpdating(false);
      }
    },
    [put],
  );

  /**
   * Delete a landing page
   */
  const deleteLandingPage = useCallback(
    async (pageId: string): Promise<boolean> => {
      try {
        setUpdating(true);
        await del(API_END_POINTS.LANDING_PAGE_DELETE(pageId));
        toast.success('Landing page deleted successfully');
        // Remove from local state
        setLandingPages({
          items: landingPages.items.filter(p => p.pageId !== pageId),
          total: landingPages.total,
          offset: landingPages.offset,
          pageSize: landingPages.pageSize,
        });
        return true;
      } catch (error: any) {
        console.error('Error deleting landing page:', error);
        const message =
          error?.response?.data?.message || 'Failed to delete landing page';
        toast.error(message);
        return false;
      } finally {
        setUpdating(false);
      }
    },
    [del],
  );

  /**
   * Duplicate a landing page
   */
  const duplicateLandingPage = useCallback(
    async (pageId: string): Promise<ILandingPage | null> => {
      try {
        setUpdating(true);
        const response = (await post(
          API_END_POINTS.LANDING_PAGE_DUPLICATE(pageId),
          {},
        )) as IResponse<ILandingPage> | undefined;

        if (response?.data) {
          toast.success('Landing page duplicated successfully');
          // Add to local state
          setLandingPages({
            items: [response.data!, ...landingPages.items],
            total: landingPages.total,
            offset: landingPages.offset,
            pageSize: landingPages.pageSize,
          });
          return response.data;
        }
        return null;
      } catch (error: any) {
        console.error('Error duplicating landing page:', error);
        toast.error(
          error?.response?.data?.message || 'Failed to duplicate landing page',
        );
        return null;
      } finally {
        setUpdating(false);
      }
    },
    [post],
  );

  /**
   * Fetch available categories
   */
  const fetchCategories = useCallback(async (): Promise<string[]> => {
    try {
      const response = (await get(API_END_POINTS.LANDING_PAGE_CATEGORIES)) as
        | IResponse<string[]>
        | undefined;
      return response?.data || [];
    } catch (error) {
      console.error('Error fetching categories:', error);
      return [];
    }
  }, [get]);

  // --- Task-05: Creation Methods ---

  /**
   * Create a new landing page
   */
  const createLandingPage = useCallback(
    async (data: ILandingPageCreateForm): Promise<ILandingPage | null> => {
      try {
        setCreating(true);
        const response = await post(API_END_POINTS.LANDING_PAGE_CREATE, {
          data,
        });

        if (!isSuccessResponse(response.statusCode)) {
          if (response.statusCode === 409) {
            toast.error('A landing page with this name already exists.');
          } else {
            toast.error(getApiErrorMessage(response));
          }
          return null;
        }
        toast.success('Landing page created successfully');
        return response?.data as ILandingPage;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.status === 409) {
          toast.error('A landing page with this name already exists.');
        } else {
          toast.error(
            err.response?.data?.message ??
              (error instanceof Error
                ? error.message
                : 'Failed to create landing page'),
          );
        }
        return null;
      } finally {
        setCreating(false);
      }
    },
    [post],
  );

  /**
   * Import website from URL
   */
  const importWebsite = useCallback(
    async (
      websiteUrl: string,
      includeAssets: boolean = true,
    ): Promise<IImportedSite | null> => {
      try {
        setImporting(true);
        const response = (await post(API_END_POINTS.LANDING_PAGE_IMPORT, {
          data: { websiteUrl, includeAssets },
        })) as IResponse<IImportedSite> | undefined;

        if (response?.data) {
          toast.success('Website imported successfully');
          return response.data;
        }
        return null;
      } catch (error: any) {
        console.error('Error importing website:', error);
        toast.error(
          error?.response?.data?.message || 'Failed to import website',
        );
        return null;
      } finally {
        setImporting(false);
      }
    },
    [post],
  );

  /**
   * Generate landing page with AI
   */
  const generateAILandingPage = useCallback(
    async (params: IAILandingPageParams): Promise<ILandingPage | null> => {
      try {
        setGenerating(true);
        const response = await post(API_END_POINTS.LANDING_PAGE_AI_GENERATE, {
          data: params,
        });
        if (isSuccessResponse(response.statusCode)) {
          toast.success('Landing page generated successfully');
          return response.data;
        } else {
          toast.error(response.message);
          return null;
        }
      } catch (error: any) {
        console.error('Error generating landing page:', error);
        toast.error(error?.response?.data?.message || 'AI generation failed');
        return null;
      } finally {
        setGenerating(false);
      }
    },
    [post],
  );

  /**
   * AI fix for selected HTML fragment (visual editor)
   */
  const fixLandingPageHtml = useCallback(
    async (request: ILandingPageFixHtmlRequest): Promise<string | null> => {
      try {
        setFixingHtml(true);
        const response = (await post(API_END_POINTS.LANDING_PAGE_FIX_HTML, {
          data: request,
        })) as IResponse<unknown> | undefined;

        const raw = response?.data;
        if (raw == null) {
          return null;
        }
        if (typeof raw === 'string') {
          return raw;
        }
        if (typeof raw === 'object') {
          const o = raw as Record<string, unknown>;
          if (typeof o.htmlContent === 'string') return o.htmlContent;
          if (typeof o.fixedHtml === 'string') return o.fixedHtml;
          if (typeof o.innerHtml === 'string') return o.innerHtml;
          if (typeof o.content === 'string') return o.content;
        }
        return null;
      } catch (error: any) {
        console.error('Error fixing HTML with AI:', error);
        toast.error(
          error?.response?.data?.message || 'Failed to apply AI HTML fix',
        );
        return null;
      } finally {
        setFixingHtml(false);
      }
    },
    [post],
  );

  /**
   * Validate HTML content
   */
  const validateHtml = useCallback(
    async (
      htmlContent: string,
      validateForms: boolean = true,
      securityCheck: boolean = true,
    ): Promise<IValidationResult | null> => {
      try {
        const response = (await post(
          API_END_POINTS.LANDING_PAGE_VALIDATE_HTML,
          { data: { htmlContent, validateForms, securityCheck } },
        )) as IResponse<IValidationResult> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error validating HTML:', error);
        return null;
      }
    },
    [post],
  );

  /**
   * Upload thumbnail for landing page
   */
  const uploadThumbnail = useCallback(
    async (pageId: string, thumbnailUrl: string): Promise<string | null> => {
      try {
        const response = (await post(
          API_END_POINTS.LANDING_PAGE_UPLOAD_THUMBNAIL(pageId),
          { data: { thumbnailUrl } },
        )) as IResponse<string> | undefined;
        return response?.data || null;
      } catch (error) {
        console.error('Error uploading thumbnail:', error);
        return null;
      }
    },
    [post],
  );

  return {
    landingPages,
    loading,
    updating,
    creating,
    importing,
    generating,
    fixingHtml,
    totalCount,
    fetchLandingPages,
    fetchLandingPagesByTemplate,
    getLandingPageById,
    getLandingPagePreview,
    updateLandingPage,
    deleteLandingPage,
    duplicateLandingPage,
    fetchCategories,
    // Task-05 methods
    createLandingPage,
    importWebsite,
    generateAILandingPage,
    fixLandingPageHtml,
    validateHtml,
    uploadThumbnail,
  };
};

export default useLandingPages;
