import { useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Bot,
  ChevronLeft,
  ChevronRight,
  Pencil,
  Plus,
  Sparkles,
  Trash2,
  Wand2,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from 'components/common/Card';
import { Button } from 'components/common/Button';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import { Textarea } from 'components/common/Textarea';
import { Badge } from 'components/common/Badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import { Switch } from 'components/common/Switch';
import ConfirmDialog from 'components/ConfirmDialog';
import { toast } from 'react-toastify';
import {
  toLlmEscalationLimit,
  toVishingScenarioCreateRequest,
  type UiEscalationLevel,
} from 'schemas/VishingSchema';
import { useVishingWizard } from '../context/VishingWizardContext';
import { useVishingScenarios } from 'hooks/UseVishingScenarios';
import { useVishingAttackTemplates } from 'hooks/UseVishingAttackTemplates';
import { useVishingCampaigns } from 'hooks/UseVishingCampaigns';
import type { IVishingAttackTemplate, IVishingScenario } from 'models/Vishing';
import { cn } from 'utils/Helper';
import {
  ScriptVariablePicker,
  useScriptVariableInsert,
} from '../ScriptVariablePicker';

type Scenario = {
  id: string;
  name: string;
  template: string;
  templateId: string;
  script: string;
  llm: boolean;
  escalation: UiEscalationLevel;
  status: 'draft' | 'published';
};

const mapScenarioFromApi = (scenario: IVishingScenario): Scenario => ({
  id: scenario.id,
  name: scenario.scenarioName,
  template: scenario.attackTemplateName ?? 'Custom',
  templateId: scenario.attackTemplateId ?? '',
  script: scenario.scriptBody ?? '',
  llm: scenario.enableLlmResponses ?? true,
  escalation:
    (scenario.escalationLimit &&
      ESCALATION_LEVEL_MAP[scenario.escalationLimit]) ||
    'medium',
  status: scenario.status === 'PUBLISHED' ? 'published' : 'draft',
});

const ESCALATION_LEVEL_MAP = {
  LOW: 'low',
  MEDIUM: 'medium',
  HIGH: 'high',
} as const;

export function ScenarioScriptTab() {
  const { id: routeCampaignId } = useParams<{ id: string }>();
  const {
    campaignId: wizardCampaignId,
    setSelectedScenarioId,
    setScenarioAttach,
    selectedScenarioId,
    scenarioAttach,
  } = useVishingWizard();
  const campaignId = wizardCampaignId || routeCampaignId || null;
  const {
    fetchScenarios,
    createScenario,
    updateScenario,
    deleteScenario,
    publishScenario,
    saving,
  } = useVishingScenarios();
  const { fetchAttackTemplates, loading: templatesLoading } =
    useVishingAttackTemplates();
  const { getSuccessKeywords, updateSuccessKeywords } = useVishingCampaigns();
  const [scenarios, setScenarios] = useState<Scenario[]>([]);
  const [templates, setTemplates] = useState<IVishingAttackTemplate[]>([]);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [listLoading, setListLoading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteCandidate, setDeleteCandidate] = useState<Scenario | null>(null);
  const [editorOpen, setEditorOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [templateId, setTemplateId] = useState('');
  const [name, setName] = useState('');
  const [script, setScript] = useState('');
  const [llm, setLlm] = useState(
    () => scenarioAttach.enableLlmResponses ?? true,
  );
  const [escalation, setEscalation] = useState<UiEscalationLevel>(
    () =>
      (scenarioAttach.escalationLimit &&
        ESCALATION_LEVEL_MAP[scenarioAttach.escalationLimit]) ||
      'medium',
  );
  const [keywords, setKeywords] = useState<string[]>([]);
  const [newKw, setNewKw] = useState('');
  const [keywordsLoadedFor, setKeywordsLoadedFor] = useState<string | null>(
    null,
  );
  const pendingScenarioRef = useRef<IVishingScenario | null>(null);
  const { textareaProps, insertVariable, resetCaret } = useScriptVariableInsert(
    script,
    setScript,
  );
  const PAGE_SIZE = 12;

  const applyTemplate = (template?: IVishingAttackTemplate | null) => {
    resetCaret();
    if (!template) {
      setTemplateId('');
      setName('');
      setScript('');
      return;
    }
    setTemplateId(template.id);
    setName(template.name);
    setScript(template.script);
  };

  const loadScenarios = async (pageNumber = page) => {
    setListLoading(true);
    try {
      const result = await fetchScenarios({
        pageSize: PAGE_SIZE,
        offset: (pageNumber - 1) * PAGE_SIZE,
        sortBy: 'createdAt',
        sortOrder: 'desc',
      });

      // A scenario saved moments ago may be absent from this response, so keep
      // showing it on the first page until the list catches up.
      const pending = pageNumber === 1 ? pendingScenarioRef.current : null;
      const isPendingMissing =
        pending !== null && !result.items.some(item => item.id === pending.id);
      if (pending && !isPendingMissing) {
        pendingScenarioRef.current = null;
      }

      const items =
        pending && isPendingMissing
          ? [pending, ...result.items].slice(0, PAGE_SIZE)
          : result.items;

      setScenarios(items.map(mapScenarioFromApi));
      setTotal(isPendingMissing ? result.total + 1 : result.total);
    } finally {
      setListLoading(false);
    }
  };

  const loadAttackTemplates = async () => {
    const result = await fetchAttackTemplates({
      pageSize: 100,
      offset: 0,
      sortBy: 'name',
      sortOrder: 'asc',
    });
    const nextTemplates = Array.isArray(result.items) ? result.items : [];
    setTemplates(nextTemplates);
    return nextTemplates;
  };

  useEffect(() => {
    void loadScenarios(page);
    // eslint-disable-next-line react-hooks/exhaustive-deps -- reload when page changes
  }, [fetchScenarios, page]);

  useEffect(() => {
    void loadAttackTemplates();
    // eslint-disable-next-line react-hooks/exhaustive-deps -- load templates once on mount
  }, [fetchAttackTemplates]);

  useEffect(() => {
    if (!campaignId) {
      setKeywordsLoadedFor(null);
      return;
    }
    if (keywordsLoadedFor === campaignId) return;

    let cancelled = false;
    const loadKeywords = async () => {
      const kw = await getSuccessKeywords(campaignId);
      if (cancelled) return;
      // Keep any keywords added before campaign id was available.
      setKeywords(prev => {
        const merged = Array.from(new Set([...kw, ...prev]));
        if (merged.length > kw.length) {
          void updateSuccessKeywords(campaignId, merged);
        }
        return merged;
      });
      setKeywordsLoadedFor(campaignId);
    };

    void loadKeywords();
    return () => {
      cancelled = true;
    };
  }, [
    campaignId,
    getSuccessKeywords,
    updateSuccessKeywords,
    keywordsLoadedFor,
  ]);

  const persistKeywords = async (nextKeywords: string[]): Promise<boolean> => {
    if (!campaignId) {
      setKeywords(nextKeywords);
      return true;
    }
    setKeywords(nextKeywords);
    const saved = await updateSuccessKeywords(campaignId, nextKeywords);
    if (!saved) {
      toast.error('Failed to save success keywords.');
      return false;
    }
    setKeywords(saved);
    return true;
  };

  const addKeyword = async () => {
    const v = newKw.trim().toLowerCase();
    if (!v) return;
    if (keywords.includes(v)) {
      toast.error('Keyword already exists.');
      return;
    }
    const ok = await persistKeywords([...keywords, v]);
    if (!ok) return;
    setNewKw('');
    toast.success('Keyword added.');
  };

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const currentPage = Math.min(page, totalPages);
  const pageStart = total === 0 ? 0 : (currentPage - 1) * PAGE_SIZE;
  const pageEnd = Math.min(pageStart + scenarios.length, total);

  useEffect(() => {
    setScenarioAttach({
      scenarioId: selectedScenarioId ?? '',
      enableLlmResponses: llm,
      escalationLimit: toLlmEscalationLimit(escalation),
    });
  }, [llm, escalation, selectedScenarioId, setScenarioAttach]);

  const resetEditor = (items: IVishingAttackTemplate[] = templates) => {
    applyTemplate(items[0] ?? null);
  };

  const openAddScenario = async () => {
    // Always refresh so newly created templates from the library appear.
    const items = await loadAttackTemplates();
    if (items.length === 0) {
      toast.error(
        'No attack templates available. Create one in Attack Templates first.',
      );
      return;
    }
    setEditingId(null);
    resetEditor(items);
    setEditorOpen(true);
  };

  const openEditScenario = async (scenario: Scenario) => {
    const items = await loadAttackTemplates();
    const matched =
      items.find(t => t.id === scenario.templateId) ??
      items.find(t => t.name === scenario.template);

    resetCaret();
    setEditingId(scenario.id);
    setTemplateId(matched?.id ?? '');
    setName(scenario.name);
    setScript(scenario.script);
    setLlm(scenario.llm);
    setEscalation(scenario.escalation);
    setEditorOpen(true);
  };

  const closeEditor = () => {
    setEditorOpen(false);
    setEditingId(null);
  };

  const save = async (status: Scenario['status']) => {
    if (!name.trim()) {
      toast.error('Scenario name cannot be empty.');
      return;
    }
    if (!script.trim()) {
      toast.error('Script cannot be empty.');
      return;
    }

    const template = templates.find(x => x.id === templateId);
    if (!template) {
      toast.error('Please select an attack template.');
      return;
    }

    const payload = toVishingScenarioCreateRequest({
      name,
      script,
      templateId,
      templateName: template.name,
      llm,
      escalation,
    });

    const saved = editingId
      ? await updateScenario(editingId, payload)
      : await createScenario(payload);

    if (!saved?.id) {
      toast.error(
        editingId ? 'Failed to update scenario.' : 'Failed to save scenario.',
      );
      return;
    }

    const markPending = (
      scenario: IVishingScenario,
      scenarioStatus: IVishingScenario['status'],
    ) => {
      pendingScenarioRef.current = {
        ...scenario,
        scenarioName: scenario.scenarioName || name.trim(),
        scriptBody: scenario.scriptBody ?? script.trim(),
        attackTemplateName: scenario.attackTemplateName ?? template.name,
        enableLlmResponses: scenario.enableLlmResponses ?? llm,
        status: scenarioStatus,
      };
    };

    // The list read can lag behind the write, so patch the edited card locally
    // instead of refetching a possibly stale page.
    const patchEditedScenario = (
      scenarioId: string,
      scenarioStatus: Scenario['status'],
    ) => {
      setScenarios(prev =>
        prev.map(item =>
          item.id === scenarioId
            ? {
                ...item,
                name: name.trim(),
                template: template.name,
                templateId,
                script: script.trim(),
                llm,
                escalation,
                status: scenarioStatus,
              }
            : item,
        ),
      );
    };

    if (status === 'published') {
      const published = await publishScenario(saved.id);
      if (!published?.id) {
        toast.error(
          editingId
            ? 'Scenario updated but failed to publish.'
            : 'Scenario saved as draft but failed to publish.',
        );
        return;
      }

      setSelectedScenarioId(published.id);
      setScenarioAttach({
        scenarioId: published.id,
        enableLlmResponses: llm,
        escalationLimit: toLlmEscalationLimit(escalation),
      });

      if (editingId) {
        patchEditedScenario(editingId, 'published');
      } else {
        markPending(published, 'PUBLISHED');
        setPage(1);
        await loadScenarios(1);
      }
      closeEditor();
      toast.success('Scenario published.');
      return;
    }

    if (editingId) {
      patchEditedScenario(
        editingId,
        scenarios.find(item => item.id === editingId)?.status ?? 'draft',
      );
      closeEditor();
      toast.success('Scenario updated.');
      return;
    }

    markPending(saved, 'DRAFT');
    setPage(1);
    await loadScenarios(1);
    closeEditor();
    toast.success('Draft saved.');
  };

  const removeScenario = async (id: string) => {
    setDeleting(true);
    try {
      const removed = await deleteScenario(id);
      if (!removed) {
        toast.error('Failed to delete scenario.');
        return;
      }

      if (selectedScenarioId === id) {
        setSelectedScenarioId(null);
      }
      if (pendingScenarioRef.current?.id === id) {
        pendingScenarioRef.current = null;
      }

      const nextTotal = Math.max(0, total - 1);
      const nextTotalPages = Math.max(1, Math.ceil(nextTotal / PAGE_SIZE));
      const nextPage = Math.min(page, nextTotalPages);
      setPage(nextPage);
      await loadScenarios(nextPage);
      setDeleteCandidate(null);
      toast.success('Scenario deleted.');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between gap-3">
            <CardTitle className="flex items-center gap-2">
              <Sparkles className="size-4 text-primary" /> Saved Scenarios
            </CardTitle>
            <Button
              onClick={() => void openAddScenario()}
              disabled={templatesLoading}
            >
              <Plus className="mr-1 size-4" /> Add Scenario
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          <div className="mb-3 flex items-center justify-between text-[11px] text-muted-foreground">
            <span>
              {listLoading
                ? 'Loading scenarios...'
                : `Showing ${total === 0 ? 0 : pageStart + 1}–${pageEnd} of ${total}`}
            </span>
            <span className="hidden sm:inline">
              Page {currentPage} / {totalPages}
            </span>
          </div>

          {scenarios.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              {listLoading
                ? 'Loading scenarios...'
                : 'No saved scenarios yet. Click Add Scenario to create one.'}
            </p>
          ) : (
            <div className="grid gap-3 sm:grid-cols-3">
              {scenarios.map(s => (
                <div
                  key={s.id}
                  className={cn(
                    'flex cursor-pointer flex-col justify-between rounded-lg border border-card-border/60 p-3',
                    selectedScenarioId === s.id &&
                      'border-primary/40 bg-primary/10',
                  )}
                  onClick={() =>
                    setSelectedScenarioId(
                      selectedScenarioId === s.id ? null : s.id,
                    )
                  }
                  role="button"
                  tabIndex={0}
                  onKeyDown={e => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      setSelectedScenarioId(s.id);
                    }
                  }}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                      <div className="truncate text-sm font-medium">
                        {s.name}
                      </div>
                      <div className="mt-0.5 truncate text-xs text-muted-foreground">
                        {s.template}
                      </div>
                    </div>
                    <div className="flex shrink-0 items-center gap-1">
                      <Button
                        size="sm"
                        variant="ghost"
                        aria-label={`Edit ${s.name}`}
                        title="Edit scenario"
                        onClick={e => {
                          e.stopPropagation();
                          void openEditScenario(s);
                        }}
                      >
                        <Pencil className="size-3.5" />
                      </Button>
                      <Button
                        size="sm"
                        variant="ghost"
                        aria-label={`Delete ${s.name}`}
                        title="Delete scenario"
                        onClick={e => {
                          e.stopPropagation();
                          setDeleteCandidate(s);
                        }}
                      >
                        <Trash2 className="size-3.5 text-destructive" />
                      </Button>
                    </div>
                  </div>
                  <div className="mt-3">
                    <Badge variant="outline" className="capitalize">
                      {s.status}
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <div className="mt-5 flex items-center justify-center gap-1">
              <Button
                size="sm"
                variant="ghost"
                disabled={currentPage === 1 || listLoading}
                onClick={() => setPage(p => Math.max(1, p - 1))}
                className="h-8 rounded-lg border border-card-border px-3 text-xs disabled:opacity-40"
              >
                <ChevronLeft className="mr-1 size-3" /> Previous
              </Button>
              {Array.from({ length: totalPages }, (_, i) => i + 1).map(n => (
                <button
                  key={n}
                  type="button"
                  onClick={() => setPage(n)}
                  disabled={listLoading}
                  className={cn(
                    'h-8 min-w-8 rounded-lg border px-2.5 text-xs transition',
                    n === currentPage
                      ? 'border-primary/60 bg-primary/15 text-primary'
                      : 'border-card-border text-muted-foreground hover:border-primary/30 hover:text-foreground',
                  )}
                >
                  {n}
                </button>
              ))}
              <Button
                size="sm"
                variant="ghost"
                disabled={currentPage === totalPages || listLoading}
                onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                className="h-8 rounded-lg border border-card-border px-3 text-xs disabled:opacity-40"
              >
                Next <ChevronRight className="ml-1 size-3" />
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Sparkles className="size-4 text-primary" /> Success Keywords
          </CardTitle>
          <CardDescription>
            Words or phrases that, if spoken by the callee during the call, mark
            them as compromised. Leave empty if you only detect compromise via
            keypad (DTMF) input.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex flex-wrap gap-1.5">
            {keywords.length === 0 ? (
              <p className="text-xs text-muted-foreground">
                No success keywords configured yet.
              </p>
            ) : (
              keywords.map(k => (
                <Badge
                  key={k}
                  variant="outline"
                  className="border-primary/40 text-primary"
                >
                  {k}
                  <button
                    type="button"
                    className="ml-1 text-xs opacity-60 hover:opacity-100"
                    onClick={() =>
                      void persistKeywords(keywords.filter(x => x !== k))
                    }
                  >
                    ×
                  </button>
                </Badge>
              ))
            )}
          </div>
          <div className="flex gap-2">
            <Input
              placeholder="Add success keywords. eg: my card number is"
              value={newKw}
              onChange={e => setNewKw(e.target.value)}
              onKeyDown={e => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  void addKeyword();
                }
              }}
            />
            <Button type="button" onClick={() => void addKeyword()}>
              Add
            </Button>
          </div>
        </CardContent>
      </Card>

      <Dialog
        open={editorOpen}
        onOpenChange={open => {
          if (!open) closeEditor();
          else setEditorOpen(true);
        }}
      >
        <DialogContent className="max-w-3xl p-0">
          <div className="border-b px-6 py-5">
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2 text-lg">
                <Wand2 className="size-5 text-primary" />
                {editingId ? 'Edit Scenario' : 'Add Scenario'}
              </DialogTitle>
              <DialogDescription>
                Customize a template with dynamic placeholders, then save or
                publish.
              </DialogDescription>
            </DialogHeader>
          </div>

          <div className="max-h-[70vh] space-y-5 overflow-y-auto px-6 py-5">
            <div className="flex items-center justify-between gap-5">
              <div className="w-1/2 space-y-1.5">
                <Label>Attack template</Label>
                <Select
                  value={templateId}
                  onValueChange={value =>
                    applyTemplate(templates.find(t => t.id === value) ?? null)
                  }
                  disabled={templatesLoading || templates.length === 0}
                >
                  <SelectTrigger>
                    <SelectValue
                      placeholder={
                        templatesLoading
                          ? 'Loading templates...'
                          : templates.length === 0
                            ? 'No attack templates found'
                            : 'Select an attack template'
                      }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {templates.map(t => (
                      <SelectItem key={t.id} value={t.id}>
                        {t.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="w-1/2 space-y-1.5">
                <Label>Scenario name</Label>
                <Input value={name} onChange={e => setName(e.target.value)} />
              </div>
            </div>

            <div className="flex items-stretch justify-between gap-5">
              <div className="flex w-7/12 flex-col space-y-1.5">
                <Label>Script</Label>
                <Textarea
                  value={script}
                  onChange={e => setScript(e.target.value)}
                  rows={10}
                  className="min-h-[220px] flex-1 resize-none font-mono text-sm"
                  {...textareaProps}
                />
              </div>

              <div className="flex w-5/12 flex-col">
                <ScriptVariablePicker
                  script={script}
                  onInsert={insertVariable}
                  className="flex flex-1 flex-col"
                  gridClassName="min-h-0 flex-1 content-start overflow-y-auto"
                />
              </div>
            </div>

            <div className="space-y-3 rounded-lg border border-card-border/60 p-4">
              <div className="flex items-center gap-2 text-sm font-medium">
                <Bot className="size-4 text-primary" /> LLM Off-Script Handling
              </div>
              <div className="flex items-center justify-between">
                <Label htmlFor="llm-toggle">Enable LLM responses</Label>
                <Switch
                  id="llm-toggle"
                  checked={llm}
                  onCheckedChange={setLlm}
                />
              </div>
              <div className="space-y-1.5">
                <Label>Escalation limit</Label>
                <Select
                  value={escalation}
                  onValueChange={value =>
                    setEscalation(value as UiEscalationLevel)
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="low">Low - strict to script</SelectItem>
                    <SelectItem value="medium">Medium - balanced</SelectItem>
                    <SelectItem value="high">High - adaptive</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-2 border-t bg-muted/30 px-6 py-4">
            <Button variant="ghost" onClick={closeEditor} disabled={saving}>
              Cancel
            </Button>
            <Button
              variant="outline"
              onClick={() => void save('draft')}
              disabled={saving}
            >
              {editingId ? 'Save changes' : 'Save draft'}
            </Button>
            <Button onClick={() => void save('published')} disabled={saving}>
              {editingId ? 'Save & publish' : 'Publish scenario'}
            </Button>
          </div>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        isOpen={Boolean(deleteCandidate)}
        onClose={() => {
          if (!deleting) setDeleteCandidate(null);
        }}
        onConfirm={() => {
          if (deleteCandidate) void removeScenario(deleteCandidate.id);
        }}
        message={`Are you sure you want to delete "${deleteCandidate?.name || ''}"? This action cannot be undone.`}
        loading={deleting}
        loadingText="Deleting..."
        buttonText="Delete"
      />
    </div>
  );
}
