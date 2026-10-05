import { Loader2, Sparkles, X } from 'lucide-react';
import { useCallback, useEffect, useRef, useState } from 'react';
import { Button } from 'common/Button';
import { Label } from 'common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import useAPI from 'hooks/UseAPI';
import { IResponse } from 'models/Context';
import {
  AI_GENERATE_PROVIDER_TYPES,
  type AIGenerateProviderType,
  type AIPromptSubmitPayload,
  type IAIModelItem,
} from 'models/HtmlEditor';
import { API_END_POINTS } from 'routes/APIEndpoints';
import { toast } from 'react-toastify';
import { isSuccessResponse } from 'utils/Helper';

interface AIPromptModalProps {
  isOpen: boolean;
  elementTag: string;
  elementHtml: string;
  isLoading: boolean;
  onSubmit: (payload: AIPromptSubmitPayload) => void | Promise<void>;
  onClose: () => void;
}

const AIPromptModal = ({
  isOpen,
  elementTag,
  elementHtml,
  isLoading,
  onSubmit,
  onClose,
}: AIPromptModalProps) => {
  const apiClient = useAPI();
  const [prompt, setPrompt] = useState('');
  const [providerType, setProviderType] =
    useState<AIGenerateProviderType>('OPENAI');
  const [model, setModel] = useState('');
  const [aiModels, setAiModels] = useState<IAIModelItem[]>([]);
  const [modelsLoading, setModelsLoading] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const fetchAiModels = useCallback(
    async (provider: AIGenerateProviderType) => {
      setModelsLoading(true);
      try {
        const queryString = `?providerType=${encodeURIComponent(provider)}`;
        const response = (await apiClient.get(
          `${API_END_POINTS.AI_MODELS}${queryString}`,
        )) as IResponse<IAIModelItem[]>;
        if (isSuccessResponse(response.statusCode)) {
          const list = (response.data ?? []).filter(m => m.active);
          setAiModels(list);
          setModel(prev => {
            const stillValid = list.some(m => m.name === prev);
            if (stillValid && prev) return prev;
            return list.find(m => m.default)?.name ?? list[0]?.name ?? '';
          });
        } else {
          setAiModels([]);
          setModel('');
          toast.error(response.message || 'Failed to load AI models');
        }
      } catch (e) {
        console.error('Error fetching AI models:', e);
        setAiModels([]);
        setModel('');
        toast.error('Failed to load AI models');
      } finally {
        setModelsLoading(false);
      }
    },
    [apiClient],
  );

  useEffect(() => {
    if (!isOpen) return;
    setPrompt('');
    setTimeout(() => textareaRef.current?.focus(), 100);
  }, [isOpen]);

  useEffect(() => {
    if (!isOpen) return;
    void fetchAiModels(providerType);
  }, [isOpen, providerType, fetchAiModels]);

  const handleSubmit = () => {
    const trimmed = prompt.trim();
    if (!trimmed || isLoading || !model || modelsLoading) return;
    void onSubmit({
      prompt: trimmed,
      providerType,
      model,
    });
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      e.preventDefault();
      handleSubmit();
    }
  };

  if (!isOpen) return null;

  const previewHtml =
    elementHtml.length > 300 ? elementHtml.slice(0, 300) + '...' : elementHtml;

  const canSubmit =
    !!prompt.trim() && !!model && !modelsLoading && aiModels.length > 0;

  return (
    <div
      className="home-fixed home-inset-0 home-z-[100] home-flex home-items-center home-justify-center home-bg-black/60"
      onClick={onClose}
    >
      <div
        className="home-mx-4 home-max-h-[90vh] home-w-2/5 home-overflow-y-auto home-rounded-xl home-border home-border-card-border home-bg-card-background home-shadow-2xl"
        onClick={e => e.stopPropagation()}
      >
        <div className="home-flex home-items-center home-justify-between home-border-b home-border-card-border home-px-5 home-py-4">
          <div className="home-flex home-items-center home-gap-2">
            <div className="home-flex home-size-8 home-items-center home-justify-center home-rounded-lg home-bg-gradient-to-br home-from-indigo-500 home-to-purple-500">
              <Sparkles className="home-size-4 home-text-white" />
            </div>
            <div>
              <h3 className="home-text-sm home-font-semibold home-text-primary">
                AI Content Generator
              </h3>
              <p className="home-text-xs home-text-muted-foreground">
                Editing{' '}
                <code className="home-rounded home-bg-indigo-50 home-px-1 home-py-0.5 home-font-mono home-text-[10px] home-font-semibold home-text-indigo-600 dark:home-bg-indigo-950 dark:home-text-indigo-400">
                  &lt;{elementTag}&gt;
                </code>{' '}
                element
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            disabled={isLoading}
            className="home-text-muted-foreground home-transition-colors hover:home-text-primary disabled:home-opacity-50"
          >
            <X className="home-size-4" />
          </button>
        </div>

        <div className="home-space-y-4 home-p-5">
          <div className="home-grid home-gap-3 sm:home-grid-cols-2">
            <div className="home-space-y-1.5">
              <Label
                htmlFor="ai-prompt-provider"
                className="home-text-xs home-font-medium home-text-primary"
              >
                AI provider
              </Label>
              <Select
                value={providerType}
                onValueChange={v =>
                  setProviderType(v as AIGenerateProviderType)
                }
                disabled={isLoading}
              >
                <SelectTrigger
                  id="ai-prompt-provider"
                  className="home-h-9 home-text-xs"
                >
                  <SelectValue placeholder="Provider" />
                </SelectTrigger>
                <SelectContent>
                  {AI_GENERATE_PROVIDER_TYPES.map(p => (
                    <SelectItem key={p} value={p}>
                      {p}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="home-space-y-1.5">
              <Label
                htmlFor="ai-prompt-model"
                className="home-text-xs home-font-medium home-text-primary"
              >
                Model
              </Label>
              <Select
                value={model || undefined}
                onValueChange={setModel}
                disabled={isLoading || modelsLoading || aiModels.length === 0}
              >
                <SelectTrigger id="ai-prompt-model" className="home-h-9 home-text-xs">
                  <SelectValue
                    placeholder={
                      modelsLoading
                        ? 'Loading models...'
                        : aiModels.length === 0
                          ? 'No active models'
                          : 'Select model'
                    }
                  />
                </SelectTrigger>
                <SelectContent>
                  {aiModels.map(m => (
                    <SelectItem key={m.id} value={m.name}>
                      {m.name}
                      {m.default ? ' (default)' : ''}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div>
            <label className="home-mb-1.5 home-block home-text-xs home-font-medium home-text-muted-foreground">
              Current Element Content
            </label>
            <div className="home-max-h-28 home-overflow-auto home-rounded-md home-border home-border-card-border home-bg-gray-50 home-p-3 home-font-mono home-text-xs home-text-gray-600 dark:home-bg-gray-900 dark:home-text-gray-400">
              {previewHtml || (
                <span className="home-italic home-text-gray-400">(empty)</span>
              )}
            </div>
          </div>

          <div>
            <label
              htmlFor="ai-prompt-input"
              className="home-mb-1.5 home-block home-text-xs home-font-medium home-text-primary"
            >
              What would you like the AI to do?
            </label>
            <textarea
              ref={textareaRef}
              id="ai-prompt-input"
              value={prompt}
              onChange={e => setPrompt(e.target.value)}
              onKeyDown={handleKeyDown}
              disabled={isLoading}
              rows={4}
              className="home-w-full home-resize-none home-rounded-lg home-border home-border-card-border home-bg-transparent home-px-3 home-py-2.5 home-text-sm home-text-primary placeholder:home-text-muted-foreground focus:home-border-indigo-500 focus:home-outline-none focus:home-ring-1 focus:home-ring-indigo-500 disabled:home-opacity-50"
              placeholder="e.g., Make this heading more compelling and professional..."
            />
            <p className="home-mt-1 home-text-[10px] home-text-muted-foreground">
              Ctrl+Enter to submit
            </p>
          </div>
        </div>

        <div className="home-flex home-items-center home-justify-end home-gap-2 home-border-t home-border-card-border home-px-5 home-py-3">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={onClose}
            disabled={isLoading}
          >
            Cancel
          </Button>
          <Button
            type="button"
            size="sm"
            onClick={handleSubmit}
            disabled={!canSubmit || isLoading}
            className="home-gap-1.5 home-bg-gradient-to-r home-from-indigo-500 home-to-purple-500 home-text-white hover:home-from-indigo-600 hover:home-to-purple-600"
          >
            {isLoading ? (
              <>
                <Loader2 className="home-size-3.5 home-animate-spin" />
                Generating...
              </>
            ) : (
              <>
                <Sparkles className="home-size-3.5" />
                Generate
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AIPromptModal;
