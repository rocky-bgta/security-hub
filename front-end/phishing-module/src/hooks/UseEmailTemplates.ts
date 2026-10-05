import {
  EmployeeDataField,
  IAIGenerateParams,
  IAIGenerationOptions,
  IEmailTemplateFixHtmlRequest,
  IEmailTemplate,
  IEmailTemplateListResponse,
  IEmailTemplatePreview,
  IFilterOptions,
  IIdName,
  ITemplateListParams,
  TemplateType,
} from 'models/EmailTemplate';
import { useCallback, useState } from 'react';
import { toast } from 'react-toastify';
import { API_END_POINTS } from 'routes/APIEndpoints';
import {
  TAIGenerateForm,
  TEmailTemplateCreateApiPayload,
  TEmailTemplateUpdateApiPayload,
} from 'schemas/EmailTemplateSchema';
import {
  isSuccessResponse,
  objectToQueryString,
  TEMPLATE_LIST_PAGE_SIZE,
} from 'utils/Helper';
import { useAPI } from './UseAPI';

interface UseEmailTemplatesReturn {
  templates: IEmailTemplateListResponse;
  loading: boolean;
  creating: boolean;
  filterOptions: IFilterOptions | null;
  queryParams: ITemplateListParams;
  setQueryParams: React.Dispatch<React.SetStateAction<ITemplateListParams>>;
  fetchTemplates: (params?: ITemplateListParams) => Promise<void>;
  getTemplateDetails: (id: string) => Promise<IEmailTemplate | null>;
  getTemplatePreview: (id: string) => Promise<IEmailTemplatePreview | null>;
  updateTemplate: (
    id: string,
    data: TEmailTemplateUpdateApiPayload,
  ) => Promise<IEmailTemplate | null>;
  deleteTemplate: (id: string) => Promise<boolean>;
  duplicateTemplate: (id: string) => Promise<IEmailTemplate | null>;
  fetchFilterOptions: () => Promise<void>;
  // Task-03 additions
  createTemplate: (
    data: TEmailTemplateCreateApiPayload,
  ) => Promise<IEmailTemplate | null>;
  generateAITemplate: (data: TAIGenerateForm) => Promise<IEmailTemplate | null>;
  sanitizeHtml: (htmlContent: string) => Promise<string | null>;
  uploadAttachment: (templateId: string, file: File) => Promise<File | null>;
  deleteAttachment: (
    templateId: string,
    attachmentId: string,
  ) => Promise<boolean>;
  fetchAIOptions: () => Promise<IAIGenerationOptions | null>;
  fixEmailTemplateHtml: (
    request: IEmailTemplateFixHtmlRequest,
  ) => Promise<string | null>;
}

export interface UseEmailTemplatesOptions {
  templateType?: TemplateType;
}

const DEFAULT_PARAMS: ITemplateListParams = {
  offset: 0,
  pageSize: TEMPLATE_LIST_PAGE_SIZE,
  searchParam: '',
  sortBy: 'createdAt',
  sortOrder: 'desc',
};

/** Backend may echo the same message at top level and under `data`. */
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
    'Failed to generate template.'
  );
}

const toOptionalIdName = (
  value?: { id?: string; name?: string },
): IIdName | undefined => {
  if (!value?.id?.trim()) return undefined;
  return { id: value.id, name: value.name ?? '' };
};

function toAIGenerateRequestBody(form: TAIGenerateForm): IAIGenerateParams {
  const constraints = toOptionalIdName(form.generationOptions.constraints);

  return {
    templateType: TemplateType.EMAIL,
    templateName: form.templateName,
    description: form.description ?? '',
    payloadType: form.payloadType,
    emailSubject: form.emailSubject,
    targetIndustry: toOptionalIdName(form.targetIndustry),
    department: toOptionalIdName(form.department),
    attackerPersona: form.attackerPersona,
    attackTechnique: form.attackTechnique,
    triggerEvent: form.triggerEvent,
    expectedUserAction: form.expectedUserAction,
    socialEngineeringStrategy: form.socialEngineeringStrategy,
    campaignObjective: form.campaignObjective,
    voiceInputContent: form.voiceInputContent ?? '',
    additionalContext: form.additionalContext ?? '',
    inputLanguage: form.inputLanguage,
    generationMode: form.generationMode,
    difficultyLevel: form.difficultyLevel,
    thumbnailUrl: form.thumbnailUrl ?? '',
    tags: form.tags ?? [],
    employeeDataRequired: (form.employeeDataRequired ?? [
      'EMAIL_ADDRESS',
    ]) as EmployeeDataField[],
    providerType: form.providerType,
    model: form.model,
    generationOptions: {
      tone: form.generationOptions.tone,
      ...(form.generationOptions.language?.trim()
        ? { language: form.generationOptions.language.trim() }
        : {}),
      ...(constraints ? { constraints } : {}),
    },
    landingPageIds: form.landingPageIds ?? [],
  };
}

/**
 * Custom hook for email template management operations
 */
const useEmailTemplates = (
  options?: UseEmailTemplatesOptions,
): UseEmailTemplatesReturn => {
  const apiClient = useAPI();
  const [loading, setLoading] = useState(false);
  const [creating, setCreating] = useState(false);
  const [templates, setTemplates] = useState<IEmailTemplateListResponse>({
    offset: 0,
    pageSize: 10,
    total: 0,
    items: [],
  });
  const [filterOptions, setFilterOptions] = useState<IFilterOptions | null>(
    null,
  );
  const [queryParams, setQueryParams] = useState<ITemplateListParams>({
    ...DEFAULT_PARAMS,
    ...(options?.templateType ? { templateType: options.templateType } : {}),
  });

  /**
   * Fetch templates list with the given params, or current query params.
   */
  const fetchTemplates = useCallback(
    async (params?: ITemplateListParams) => {
      const effectiveParams = params ?? queryParams;
      setLoading(true);
      try {
        const queryString = objectToQueryString(
          effectiveParams as Record<string, unknown>,
        );
        const response = await apiClient.get(
          API_END_POINTS.EMAIL_TEMPLATE_LIST + queryString,
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(`Failed to fetch templates: ${response.statusCode}`);
        }

        const data = response.data;

        setTemplates({
          offset: data.offset || 0,
          pageSize: data.pageSize || TEMPLATE_LIST_PAGE_SIZE,
          total: data.total || 0,
          items: data.items || [],
        });
      } catch (error) {
        console.error('Error fetching templates:', error);
        toast.error('Failed to load templates. Please try again.');
      } finally {
        setLoading(false);
      }
    },
    [apiClient, queryParams],
  );

  /**
   * Get template details (metadata and config)
   */
  const getTemplateDetails = useCallback(
    async (id: string): Promise<IEmailTemplate | null> => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.EMAIL_TEMPLATE_DETAILS(id),
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(
            `Failed to fetch template details: ${response.statusCode}`,
          );
        }

        return response.data as IEmailTemplate;
      } catch (error) {
        console.error('Error fetching template details:', error);
        toast.error('Failed to load template.');
        return null;
      }
    },
    [apiClient],
  );

  /**
   * Get full template preview with HTML content
   */
  const getTemplatePreview = useCallback(
    async (id: string): Promise<IEmailTemplatePreview | null> => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.EMAIL_TEMPLATE_PREVIEW(id),
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(
            `Failed to fetch template preview: ${response.statusCode}`,
          );
        }

        return response.data as IEmailTemplatePreview;
      } catch (error) {
        console.error('Error fetching template preview:', error);
        toast.error('Failed to load template preview.');
        return null;
      }
    },
    [apiClient],
  );

  /**
   * Update an existing template
   */
  const updateTemplate = useCallback(
    async (
      id: string,
      data: TEmailTemplateUpdateApiPayload,
    ): Promise<IEmailTemplate | null> => {
      try {
        const response = await apiClient.put(
          API_END_POINTS.EMAIL_TEMPLATE_UPDATE(id),
          { data },
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(`Failed to update template: ${response.statusCode}`);
        }

        toast.success('Template updated successfully!');
        return response.data as IEmailTemplate;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.status === 403) {
          toast.error("You don't have permission to edit this template.");
        } else if (err.response?.status === 404) {
          toast.error('Template not found.');
        } else {
          toast.error(
            err.response?.data?.message || 'Failed to update template.',
          );
        }
        return null;
      }
    },
    [apiClient],
  );

  /**
   * Delete a template
   */
  const deleteTemplate = useCallback(
    async (id: string): Promise<boolean> => {
      try {
        const response = await apiClient.del(
          API_END_POINTS.EMAIL_TEMPLATE_DELETE(id),
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(`Failed to delete template: ${response.statusCode}`);
        }

        toast.success('Template deleted successfully!');
        return true;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.status === 403) {
          toast.error('You cannot delete templates created by Super Admin.');
        } else if (err.response?.status === 409) {
          toast.error(
            'Cannot delete template. It is used by active campaigns.',
          );
        } else if (err.response?.status === 404) {
          toast.error('Template not found.');
        } else {
          toast.error(
            err.response?.data?.message || 'Failed to delete template.',
          );
        }
        return false;
      }
    },
    [apiClient],
  );

  /**
   * Duplicate a template
   */
  const duplicateTemplate = useCallback(
    async (id: string): Promise<IEmailTemplate | null> => {
      try {
        const response = await apiClient.post(
          API_END_POINTS.EMAIL_TEMPLATE_DUPLICATE(id),
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(
            `Failed to duplicate template: ${response.statusCode}`,
          );
        }

        toast.success('Template duplicated successfully!');
        return response.data as IEmailTemplate;
      } catch (error: unknown) {
        const err = error as { response?: { data?: { message?: string } } };
        toast.error(
          err.response?.data?.message || 'Failed to duplicate template.',
        );
        return null;
      }
    },
    [apiClient],
  );

  /**
   * Fetch filter options for dropdowns
   */
  const fetchFilterOptions = useCallback(async () => {
    try {
      const response = await apiClient.get(
        API_END_POINTS.EMAIL_TEMPLATE_FILTERS,
      );

      if (!isSuccessResponse(response.statusCode)) {
        throw new Error(
          `Failed to fetch filter options: ${response.statusCode}`,
        );
      }

      setFilterOptions(response.data as IFilterOptions);
    } catch (error) {
      console.error('Error fetching filter options:', error);
    }
  }, [apiClient]);

  // --- Task-03: Template Creation Methods ---

  /**
   * Create a new email template
   */
  const createTemplate = useCallback(
    async (data: TEmailTemplateCreateApiPayload): Promise<IEmailTemplate | null> => {
      setCreating(true);
      try {
        const response = await apiClient.post(
          API_END_POINTS.EMAIL_TEMPLATE_CREATE,
          { data },
        );

        if (!isSuccessResponse(response.statusCode)) {
          if (response.statusCode === 409) {
            toast.error('A template with this name already exists.');
          } else {
            toast.error(getApiErrorMessage(response));
          }
          return null;
        }

        toast.success('Template created successfully!');
        return response.data as IEmailTemplate;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.status === 409) {
          toast.error('A template with this name already exists.');
        } else {
          toast.error(
            err.response?.data?.message ??
              (error instanceof Error
                ? error.message
                : 'Failed to create template.'),
          );
        }
        return null;
      } finally {
        setCreating(false);
      }
    },
    [apiClient],
  );

  /**
   * Generate a template using AI
   */
  const generateAITemplate = useCallback(
    async (data: TAIGenerateForm): Promise<IEmailTemplate | null> => {
      setCreating(true);
      try {
        const body = toAIGenerateRequestBody(data);
        const response = await apiClient.post(
          API_END_POINTS.EMAIL_TEMPLATE_AI_GENERATE,
          { data: body },
        );

        if (!isSuccessResponse(response.statusCode)) {
          if (response.statusCode === 409) {
            toast.error('A template with this name already exists.');
          } else {
            toast.error(getApiErrorMessage(response));
          }
          return null;
        }

        toast.success('Template generated successfully!');
        return response.data as IEmailTemplate;
      } catch (error: unknown) {
        console.error('Error generating AI template:', error);
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.status === 409) {
          toast.error('A template with this name already exists.');
        } else {
          toast.error(
            err.response?.data?.message ??
              (error instanceof Error
                ? error.message
                : 'Failed to generate template.'),
          );
        }
        return null;
      } finally {
        setCreating(false);
      }
    },
    [apiClient],
  );

  /**
   * Sanitize HTML content
   */
  const sanitizeHtml = useCallback(
    async (htmlContent: string): Promise<string | null> => {
      try {
        const response = await apiClient.post(
          API_END_POINTS.EMAIL_TEMPLATE_SANITIZE_HTML,
          {
            data: { htmlContent },
          },
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(`Failed to sanitize HTML: ${response.statusCode}`);
        }

        return response.data?.sanitizedHtml || htmlContent;
      } catch (error) {
        console.error('Error sanitizing HTML:', error);
        return htmlContent;
      }
    },
    [apiClient],
  );

  /**
   * Upload attachment to a template
   */
  const uploadAttachment = useCallback(
    async (templateId: string, file: File): Promise<File | null> => {
      try {
        const formData = new FormData();
        formData.append('file', file);
        const response = await apiClient.post(
          API_END_POINTS.EMAIL_TEMPLATE_UPLOAD_ATTACHMENT(templateId),
          {
            data: formData,
            headers: { 'Content-Type': 'multipart/form-data' },
          },
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(
            `Failed to upload attachment: ${response.statusCode}`,
          );
        }

        toast.success('Attachment uploaded successfully!');
        return file;
      } catch (error: unknown) {
        const err = error as {
          response?: { status: number; data?: { message?: string } };
        };
        if (err.response?.data?.message?.includes('5MB')) {
          toast.error('File size exceeds 5MB limit.');
        } else if (
          err.response?.data?.message?.includes('JPG') ||
          err.response?.data?.message?.includes('PNG')
        ) {
          toast.error('Only JPG and PNG files are allowed.');
        } else if (err.response?.data?.message?.includes('5 attachments')) {
          toast.error('Maximum 5 attachments allowed.');
        } else {
          toast.error(
            err.response?.data?.message || 'Failed to upload attachment.',
          );
        }
        return null;
      }
    },
    [apiClient],
  );

  /**
   * Delete attachment from a template
   */
  const deleteAttachment = useCallback(
    async (templateId: string, attachmentId: string): Promise<boolean> => {
      try {
        const response = await apiClient.del(
          API_END_POINTS.EMAIL_TEMPLATE_DELETE_ATTACHMENT(
            templateId,
            attachmentId,
          ),
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(
            `Failed to delete attachment: ${response.statusCode}`,
          );
        }

        toast.success('Attachment deleted successfully!');
        return true;
      } catch (error: unknown) {
        const err = error as { response?: { data?: { message?: string } } };
        toast.error(
          err.response?.data?.message || 'Failed to delete attachment.',
        );
        return false;
      }
    },
    [apiClient],
  );

  /**
   * Fetch AI generation options
   */
  const fetchAIOptions =
    useCallback(async (): Promise<IAIGenerationOptions | null> => {
      try {
        const response = await apiClient.get(
          API_END_POINTS.EMAIL_TEMPLATE_AI_OPTIONS,
        );

        if (!isSuccessResponse(response.statusCode)) {
          throw new Error(`Failed to fetch AI options: ${response.statusCode}`);
        }

        return response.data as IAIGenerationOptions;
      } catch (error) {
        console.error('Error fetching AI options:', error);
        return null;
      }
    }, [apiClient]);

  /**
   * AI fix for selected HTML fragment (visual editor)
   */
  const fixEmailTemplateHtml = useCallback(
    async (request: IEmailTemplateFixHtmlRequest): Promise<string | null> => {
      try {
        const response = await apiClient.post(
          API_END_POINTS.EMAIL_TEMPLATE_FIX_HTML,
          { data: request },
        );

        const raw = response?.data;
        if (raw == null) {
          return null;
        }
        if (typeof raw === 'string') {
          return raw;
        }
        if (typeof raw === 'object') {
          const o = raw as Record<string, unknown>;
          if (typeof o.emailBody === 'string') return o.emailBody;
          if (typeof o.htmlContent === 'string') return o.htmlContent;
          if (typeof o.fixedHtml === 'string') return o.fixedHtml;
          if (typeof o.innerHtml === 'string') return o.innerHtml;
          if (typeof o.content === 'string') return o.content;
        }
        return null;
      } catch (error: unknown) {
        const err = error as { response?: { data?: { message?: string } } };
        console.error('Error fixing email template HTML with AI:', error);
        toast.error(
          err.response?.data?.message || 'Failed to apply AI email HTML fix',
        );
        return null;
      }
    },
    [apiClient],
  );

  return {
    templates,
    loading,
    creating,
    filterOptions,
    queryParams,
    setQueryParams,
    fetchTemplates,
    getTemplateDetails,
    getTemplatePreview,
    updateTemplate,
    deleteTemplate,
    duplicateTemplate,
    fetchFilterOptions,
    // Task-03 additions
    createTemplate,
    generateAITemplate,
    sanitizeHtml,
    uploadAttachment,
    deleteAttachment,
    fetchAIOptions,
    fixEmailTemplateHtml,
  };
};

export { useEmailTemplates };
export default useEmailTemplates;
