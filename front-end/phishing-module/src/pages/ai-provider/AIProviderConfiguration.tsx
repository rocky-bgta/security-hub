import { Button } from 'common/Button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'common/Card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'common/Tabs';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import { TableSkeleton } from 'components/LoadingSkeleton';
import AIModelCreateModal from 'features/ai-provider/AIModelCreateModal';
import AIProviderCreateModal from 'features/ai-provider/AIProviderCreateModal';
import useAIProviderConfig from 'hooks/UseAIProviderConfig';
import {
  AIProviderType,
  IAIModelItem,
  IAIProviderConfigItem,
} from 'models/AiProvider';
import { useCallback, useEffect, useState } from 'react';
import { toast } from 'react-toastify';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'common/Select';
import { PlusIcon, Trash2Icon } from 'lucide-react';
import { useAuth } from 'hooks/UseAuth';
import { ROLE } from 'utils/Role';

/**
 * Configure the AI API provider used for phishing module features (e.g. template generation).
 */
const AIProviderConfiguration = () => {
  const { role } = useAuth();

  const {
    isSubmitting,
    isLoadingList,
    isSubmittingModel,
    isLoadingModelList,
    saveProviderConfig,
    getProviderConfigList,
    getModelList,
    saveModel,
    deleteModel,
  } = useAIProviderConfig();
  const [configs, setConfigs] = useState<IAIProviderConfigItem[]>([]);
  const [models, setModels] = useState<IAIModelItem[]>([]);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isModelCreateModalOpen, setIsModelCreateModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState('ai-configurations');
  const [modelProviderFilter, setModelProviderFilter] = useState<
    'ALL' | AIProviderType
  >('ALL');

  const loadConfigs = useCallback(async () => {
    const result = await getProviderConfigList();
    if (!result.ok) {
      toast.error(result.message);
      return;
    }

    setConfigs(result.data);
  }, [getProviderConfigList]);

  const loadModels = useCallback(async () => {
    const result = await getModelList(
      modelProviderFilter === 'ALL' ? undefined : modelProviderFilter,
    );
    if (!result.ok) {
      toast.error(result.message);
      return;
    }
    setModels(result.data);
  }, [getModelList, modelProviderFilter]);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadConfigs();
      if (role === ROLE.ASPIRE_ADMIN) {
        loadModels();
      }
    }, 0);

    return () => clearTimeout(timer);
  }, [loadConfigs, loadModels, role]);

  const formatDate = (value: string) => {
    if (!value) {
      return '-';
    }

    const parsedDate = new Date(value);
    if (Number.isNaN(parsedDate.getTime())) {
      return '-';
    }

    return parsedDate.toLocaleString();
  };

  const handleDeleteModel = async (id: string) => {
    const confirmDelete = window.confirm(
      'Are you sure you want to delete this AI model?',
    );
    if (!confirmDelete) {
      return;
    }
    const result = await deleteModel(id);
    if (!result.ok) {
      toast.error(result.message);
      return;
    }
    toast.success(result.message);
    loadModels();
  };

  return (
    <div className="mx-auto flex w-full flex-col gap-4">
      <div className="flex items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">
            AI provider configuration
          </h1>
          <p className="mt-1 text-muted-foreground">
            Connect an AI API to power template and content generation features.
          </p>
        </div>
        {activeTab === 'ai-configurations' ? (
          <Button onClick={() => setIsCreateModalOpen(true)}>
            <PlusIcon className="size-4" /> Add config
          </Button>
        ) : (
          <Button onClick={() => setIsModelCreateModalOpen(true)}>
            <PlusIcon className="size-4" /> Add model
          </Button>
        )}
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        {role === ROLE.ASPIRE_ADMIN && (
          <TabsList className="w-fit justify-start">
            <TabsTrigger value="ai-configurations">
              AI configurations
            </TabsTrigger>
            <TabsTrigger value="model-configurations">
              Model configurations
            </TabsTrigger>
          </TabsList>
        )}

        <TabsContent value="ai-configurations">
          <Card className="p-0">
            <CardHeader>
              <div className="flex items-center justify-between gap-4">
                <div>
                  <CardTitle>AI configurations</CardTitle>
                  <CardDescription>
                    Manage configured AI provider credentials.
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              {isLoadingList ? (
                <TableSkeleton count={5} />
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Provider</TableHead>
                      <TableHead>Secret Name</TableHead>
                      <TableHead>Description</TableHead>
                      <TableHead>Active</TableHead>
                      <TableHead>Updated At</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {configs.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={5} className="text-center">
                          No AI provider configuration found.
                        </TableCell>
                      </TableRow>
                    ) : (
                      configs.map(config => (
                        <TableRow key={config.id}>
                          <TableCell>{config.providerType}</TableCell>
                          <TableCell>{config.secretName}</TableCell>
                          <TableCell>{config.description || '-'}</TableCell>
                          <TableCell>{config.active ? 'Yes' : 'No'}</TableCell>
                          <TableCell>{formatDate(config.updatedAt)}</TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="model-configurations">
          <Card className="p-0">
            <CardHeader>
              <div className="flex items-center justify-between gap-4">
                <div>
                  <CardTitle>Model configurations</CardTitle>
                  <CardDescription>
                    Manage available AI models for each provider.
                  </CardDescription>
                </div>
                <div className="flex items-center gap-2">
                  <label
                    htmlFor="provider-filter"
                    className="text-sm text-muted-foreground"
                  >
                    Provider
                  </label>
                  <Select
                    value={modelProviderFilter}
                    onValueChange={v =>
                      setModelProviderFilter(v as 'ALL' | AIProviderType)
                    }
                  >
                    <SelectTrigger id="provider-filter" className="w-40">
                      <SelectValue placeholder="Select provider" />
                    </SelectTrigger>
                    <SelectContent className="w-40">
                      <SelectItem value="ALL">All</SelectItem>
                      <SelectItem value={AIProviderType.OPENAI}>
                        OPENAI
                      </SelectItem>
                      <SelectItem value={AIProviderType.GEMINI}>
                        GEMINI
                      </SelectItem>
                      <SelectItem value={AIProviderType.CLAUDE}>
                        CLAUDE
                      </SelectItem>
                      <SelectItem value={AIProviderType.ZAI}>ZAI</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              {isLoadingModelList ? (
                <TableSkeleton count={5} />
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Name</TableHead>
                      <TableHead>Provider</TableHead>
                      <TableHead>Default</TableHead>
                      <TableHead>Active</TableHead>
                      <TableHead>Created At</TableHead>
                      <TableHead>Action</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {models.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={6} className="text-center">
                          No model configuration found.
                        </TableCell>
                      </TableRow>
                    ) : (
                      models.map(model => (
                        <TableRow key={model.id}>
                          <TableCell>{model.name}</TableCell>
                          <TableCell>{model.providerType}</TableCell>
                          <TableCell>{model.default ? 'Yes' : 'No'}</TableCell>
                          <TableCell>{model.active ? 'Yes' : 'No'}</TableCell>
                          <TableCell>{formatDate(model.createdAt)}</TableCell>
                          <TableCell>
                            <Button
                              variant="outline"
                              size="sm"
                              disabled={isSubmittingModel}
                              onClick={() => handleDeleteModel(model.id)}
                            >
                              <Trash2Icon className="size-4" />
                            </Button>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      <AIProviderCreateModal
        isOpen={isCreateModalOpen}
        isSubmitting={isSubmitting}
        onClose={() => setIsCreateModalOpen(false)}
        onCreate={saveProviderConfig}
        onCreated={loadConfigs}
      />
      <AIModelCreateModal
        isOpen={isModelCreateModalOpen}
        isSubmitting={isSubmittingModel}
        onClose={() => setIsModelCreateModalOpen(false)}
        onCreate={saveModel}
        onCreated={loadModels}
      />
    </div>
  );
};

export default AIProviderConfiguration;
