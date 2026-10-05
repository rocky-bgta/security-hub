import { useEffect, useMemo, useState } from 'react';
import {
  Activity,
  AlertCircle,
  CheckCircle2,
  Download,
  FileText,
  Loader2,
  PhoneCall,
  Repeat,
  Search,
  Send,
  ServerCog,
  ShieldCheck,
  Sparkles,
  TrendingUp,
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
import { Badge } from 'components/common/Badge';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from 'components/common/Tabs';
import { ScrollArea } from 'components/common/ScrollArea';
import { Separator } from 'components/common/Separator';
import { Progress } from 'components/common/Progress';
import { toast } from 'react-toastify';
import { cn, downloadBlob } from 'utils/Helper';
import { useVishingCampaigns } from 'hooks/UseVishingCampaigns';
import type { ICampaignVoiceScenario } from 'models/Campaign';
import type {
  IVishingCallLog,
  IVishingRecipient,
  IVishingReport,
  VishingCallLogStatus,
  VishingRecipientStatus,
} from 'models/Vishing';

type CallLog = {
  id: string;
  target: string;
  number: string;
  duration: string;
  status: VishingCallLogStatus | null;
  retries: number;
  keywords: string[];
  failurePoint?: string;
  transcript: string;
};

interface IVishingCampaignAnalyticsProps {
  campaignId: string;
  scenario?: ICampaignVoiceScenario | null;
  refreshKey?: number;
}

const VishingCampaignAnalytics = ({
  campaignId,
  scenario = null,
  refreshKey = 0,
}: IVishingCampaignAnalyticsProps) => {
  return (
    <Tabs defaultValue="capture" className="space-y-6">
      <TabsList className="w-max">
        <TabsTrigger value="capture">
          <Activity className="mr-1 size-4" /> Data Capture
        </TabsTrigger>
        <TabsTrigger value="remediation">
          <Send className="mr-1 size-4" /> Remediation
        </TabsTrigger>
        <TabsTrigger value="reporting">
          <FileText className="mr-1 size-4" /> Reporting
        </TabsTrigger>
      </TabsList>
      <TabsContent
        value="capture"
        forceMount
        className="space-y-6 data-[state=inactive]:hidden"
      >
        <InteractionTab
          campaignId={campaignId}
          scenario={scenario}
          refreshKey={refreshKey}
        />
      </TabsContent>
      <TabsContent
        value="remediation"
        forceMount
        className="space-y-6 data-[state=inactive]:hidden"
      >
        <RemediationTab campaignId={campaignId} refreshKey={refreshKey} />
      </TabsContent>
      <TabsContent
        value="reporting"
        forceMount
        className="space-y-6 data-[state=inactive]:hidden"
      >
        <WorkflowTab campaignId={campaignId} refreshKey={refreshKey} />
        <NfrTab campaignId={campaignId} refreshKey={refreshKey} />
      </TabsContent>
    </Tabs>
  );
};

export default VishingCampaignAnalytics;

function formatDuration(seconds?: number): string {
  if (seconds == null || Number.isNaN(seconds)) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

function mapApiCallLog(log: IVishingCallLog, index: number): CallLog {
  const target =
    log.recipientName || log.phoneNumber || log.id || `Call ${index + 1}`;

  return {
    id: log.id || log.recipientId || `log-${index}`,
    target,
    number: log.phoneNumber ?? '',
    duration: formatDuration(log.durationSeconds),
    status: log.status ?? null,
    retries: log.retries ?? 0,
    keywords: log.detectedKeywords ?? [],
    transcript: log.transcript ?? '',
  };
}

function InteractionTab({
  campaignId,
  scenario,
  refreshKey = 0,
}: {
  campaignId: string | null;
  scenario?: ICampaignVoiceScenario | null;
  refreshKey?: number;
}) {
  const { getDataCapture, getSuccessKeywords } = useVishingCampaigns();
  const [logs, setLogs] = useState<CallLog[]>([]);
  const [selected, setSelected] = useState<CallLog | null>(null);
  const [captureTotals, setCaptureTotals] = useState({
    totalCalls: 0,
    compromisedCount: 0,
  });
  const [loadedFor, setLoadedFor] = useState<string | null>(null);
  const [loadFailedFor, setLoadFailedFor] = useState<string | null>(null);
  const [filter, setFilter] = useState('');
  const [successKeywords, setSuccessKeywords] = useState<string[]>([]);
  const scenarioName = scenario?.scenarioName?.trim();
  const scenarioScript = scenario?.scriptBody?.trim();
  const hasScenario = Boolean(scenarioName || scenarioScript);
  const loaded = loadedFor === campaignId;
  const loadFailed = loadFailedFor === campaignId;

  useEffect(() => {
    if (!campaignId) return;

    let cancelled = false;
    const keepExisting = loadedFor === campaignId;

    const load = async () => {
      try {
        const capture = await getDataCapture(campaignId, {
          pageSize: 50,
          offset: 0,
        });
        if (cancelled) return;

        if (!capture) {
          if (!keepExisting) setLoadFailedFor(campaignId);
          return;
        }

        setCaptureTotals({
          totalCalls: capture.totalCalls,
          compromisedCount: capture.compromisedCount,
        });
        const mapped = (capture.callLogs ?? []).map(mapApiCallLog);
        setLogs(mapped);
        setSelected(
          prev => mapped.find(l => l.id === prev?.id) ?? mapped[0] ?? null,
        );
        setLoadedFor(campaignId);
        setLoadFailedFor(null);
      } catch {
        if (!cancelled && !keepExisting) setLoadFailedFor(campaignId);
      }
    };

    void load();
    return () => {
      cancelled = true;
    };
    // loadedFor is read only to keep existing data visible during silent refresh.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [campaignId, refreshKey, getDataCapture]);

  useEffect(() => {
    if (!campaignId) return;

    let cancelled = false;

    const loadSuccessKeywords = async () => {
      const keywords = await getSuccessKeywords(campaignId);
      if (cancelled) return;
      setSuccessKeywords(keywords);
    };

    void loadSuccessKeywords();
    return () => {
      cancelled = true;
    };
  }, [campaignId, refreshKey, getSuccessKeywords]);

  const filtered = logs.filter(
    l =>
      l.target.toLowerCase().includes(filter.toLowerCase()) ||
      l.transcript.toLowerCase().includes(filter.toLowerCase()),
  );

  const stats = useMemo(() => {
    const answered = logs.filter(
      l =>
        l.status === 'ANSWERED' ||
        l.status === 'VOICE_ENGAGED' ||
        l.status === 'COMPROMISED',
    ).length;
    const totalRetries = logs.reduce((s, l) => s + l.retries, 0);
    return {
      answered,
      success: captureTotals.compromisedCount,
      totalRetries,
      total: captureTotals.totalCalls || logs.length,
    };
  }, [logs, captureTotals]);

  if (!campaignId) {
    return (
      <EmptyCampaignState message="Select a campaign to view data capture metrics." />
    );
  }

  if (loadFailed) {
    return <EmptyCampaignState message="No campaign found." />;
  }

  if (!loaded) {
    return <LoadingState message="Loading call logs…" />;
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-4">
        <StatCard label="Total calls" value={stats.total} icon={PhoneCall} />
        <StatCard
          label="Answered"
          value={stats.answered}
          icon={CheckCircle2}
          accent="text-emerald-500"
        />
        <StatCard
          label="Compromised"
          value={stats.success}
          icon={Sparkles}
          accent="text-primary"
        />
        <StatCard
          label="Retries triggered"
          value={stats.totalRetries}
          icon={Repeat}
          accent="text-amber-500"
        />
      </div>

      <div className="grid gap-6">
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between gap-3">
              <div>
                <CardTitle>Call Logs</CardTitle>
                <CardDescription>
                  Recordings, transcripts, and metrics.
                </CardDescription>
              </div>
              <div className="relative w-64">
                <Search className="pointer-events-none absolute left-2 top-2.5 size-4 text-muted-foreground" />
                <Input
                  placeholder="Filter by target or transcript"
                  value={filter}
                  onChange={e => setFilter(e.target.value)}
                  className="pl-8"
                />
              </div>
            </div>
          </CardHeader>
          <CardContent>
            <div className="grid gap-3 lg:grid-cols-5">
              <ScrollArea className="h-[360px] lg:col-span-2">
                <div className="space-y-2 pr-3">
                  {filtered.length === 0 ? (
                    <div className="rounded-md border border-dashed border-card-border/60 p-6 text-center text-sm text-muted-foreground">
                      No call logs recorded for this campaign yet.
                    </div>
                  ) : (
                    filtered.map(l => (
                      <button
                        key={l.id}
                        onClick={() => setSelected(l)}
                        className={cn(
                          'w-full rounded-md border p-3 text-left transition',
                          selected?.id === l.id
                            ? 'border-primary bg-primary/5'
                            : 'border-card-border hover:border-primary/40',
                        )}
                      >
                        <div className="flex items-center justify-between">
                          <span className="text-sm font-medium">
                            {l.target}
                          </span>
                          <StatusBadge status={l.status} />
                        </div>
                        <div className="mt-1 font-mono text-xs text-muted-foreground">
                          {l.number || '—'}
                        </div>
                        <div className="mt-1 flex items-center gap-2 text-xs text-muted-foreground">
                          <span>{l.duration}</span>
                          <span>·</span>
                          <span>{l.retries} retries</span>
                        </div>
                      </button>
                    ))
                  )}
                </div>
              </ScrollArea>

              <div className="rounded-md border border-card-border/60 p-3 lg:col-span-3">
                {selected ? (
                  <>
                    <div className="mb-2">
                      <div className="font-medium">{selected.target}</div>
                      <div className="font-mono text-xs text-muted-foreground">
                        {selected.number || '—'}
                      </div>
                    </div>
                    <div className="mb-3 flex flex-wrap gap-1.5 text-xs">
                      <Badge variant="outline">
                        Duration: {selected.duration}
                      </Badge>
                      <Badge variant="outline">
                        Retries: {selected.retries}
                      </Badge>
                      {selected.failurePoint && (
                        <Badge
                          variant="outline"
                          className="border-destructive/50 text-destructive"
                        >
                          <AlertCircle className="mr-1 size-3" />{' '}
                          {selected.failurePoint}
                        </Badge>
                      )}
                      {selected.keywords.map(k => (
                        <Badge
                          key={k}
                          variant="outline"
                          className="border-emerald-500/40 text-emerald-500"
                        >
                          <CheckCircle2 className="mr-1 size-3" /> {k}
                        </Badge>
                      ))}
                    </div>
                    <div className="space-y-3">
                      <div className="rounded-md bg-muted/30 p-3">
                        <div className="mb-1 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                          Scenario
                        </div>
                        {hasScenario ? (
                          <>
                            <div className="text-sm font-medium">
                              {scenarioName || 'Scenario'}
                            </div>
                            {scenarioScript && (
                              <pre className="mt-2 whitespace-pre-wrap font-mono text-xs leading-relaxed text-muted-foreground">
                                {scenarioScript}
                              </pre>
                            )}
                          </>
                        ) : (
                          <p className="text-xs text-muted-foreground">
                            No scenario available.
                          </p>
                        )}
                      </div>

                      <div className="rounded-md bg-muted/30 p-3">
                        <div className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                          Success Keywords
                        </div>
                        {successKeywords.length === 0 ? (
                          <p className="text-xs text-muted-foreground">
                            No success keywords available.
                          </p>
                        ) : (
                          <div className="flex flex-wrap gap-1.5">
                            {successKeywords.map(keyword => (
                              <Badge
                                key={keyword}
                                variant="outline"
                                className="text-xs"
                              >
                                {keyword}
                              </Badge>
                            ))}
                          </div>
                        )}
                      </div>

                      <div className="rounded-md bg-muted/30 p-3">
                        <div className="mb-1 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                          Transcript
                        </div>
                        <pre className="whitespace-pre-wrap font-mono text-xs leading-relaxed">
                          {selected.transcript || 'No transcript available.'}
                        </pre>
                      </div>
                    </div>
                  </>
                ) : (
                  <div className="flex h-full min-h-[200px] items-center justify-center text-sm text-muted-foreground">
                    Select a call log to view details.
                  </div>
                )}
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function StatCard({
  label,
  value,
  icon: Icon,
  accent = 'text-primary',
}: {
  label: string;
  value: number | string;
  icon: typeof PhoneCall;
  accent?: string;
}) {
  return (
    <Card>
      <CardContent className="flex items-center gap-3 p-4">
        <div
          className={cn('grid size-10 place-items-center rounded-md', accent)}
        >
          <Icon className="size-5" />
        </div>
        <div>
          <div className="text-2xl font-semibold leading-none">{value}</div>
          <div className="mt-1 text-xs text-muted-foreground">{label}</div>
        </div>
      </CardContent>
    </Card>
  );
}

function StatusBadge({ status }: { status: CallLog['status'] }) {
  const successfulStatuses: VishingCallLogStatus[] = [
    'ANSWERED',
    'VOICE_ENGAGED',
    'COMPROMISED',
    'REPORTED',
    'DELIVERED',
    'OPENED',
    'CLICKED',
    'DATA_SUBMITTED',
  ];
  const failedStatuses: VishingCallLogStatus[] = ['CALL_FAILED', 'BOUNCED'];
  const className = status
    ? successfulStatuses.includes(status)
      ? 'border-emerald-500/40 text-emerald-500'
      : failedStatuses.includes(status)
        ? 'border-destructive/50 text-destructive'
        : 'border-amber-500/40 text-amber-500'
    : 'border-muted-foreground/40 text-muted-foreground';

  return (
    <Badge variant="outline" className={className}>
      {status?.replace(/_/g, ' ') ?? 'UNKNOWN'}
    </Badge>
  );
}

type RemediationTarget = {
  id: string;
  name: string;
  email: string;
  phone: string;
  status: VishingRecipientStatus | null;
};

function mapRecipientToTarget(recipient: IVishingRecipient): RemediationTarget {
  const name =
    [recipient.firstName, recipient.lastName].filter(Boolean).join(' ') ||
    recipient.email ||
    recipient.recipientId;

  return {
    id: recipient.recipientId,
    name,
    email: recipient.email ?? '',
    phone: recipient.phoneNumber ?? '',
    status: recipient.status ?? null,
  };
}

function RemediationTab({
  campaignId,
  refreshKey = 0,
}: {
  campaignId: string | null;
  refreshKey?: number;
}) {
  const { getRemediation, fetchRecipients, sendTeachableMoment, exportReport } =
    useVishingCampaigns();
  const [targets, setTargets] = useState<RemediationTarget[]>([]);
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [filter, setFilter] = useState('');
  const [sending, setSending] = useState(false);
  const [exporting, setExporting] = useState<'csv' | 'json' | null>(null);
  const [loadedFor, setLoadedFor] = useState<string | null>(null);
  const [loadFailedFor, setLoadFailedFor] = useState<string | null>(null);
  const [metrics, setMetrics] = useState({
    answerRate: 0,
    failureRate: 0,
    riskScore: 0,
    compromisedRate: 0,
  });
  const loaded = loadedFor === campaignId;
  const loadFailed = loadFailedFor === campaignId;

  useEffect(() => {
    if (!campaignId) return;

    let cancelled = false;
    const keepExisting = loadedFor === campaignId;

    const load = async () => {
      try {
        const [remediation, recipients] = await Promise.all([
          getRemediation(campaignId),
          fetchRecipients(campaignId, { pageSize: 100, offset: 0 }),
        ]);
        if (cancelled) return;

        if (!remediation) {
          if (!keepExisting) setLoadFailedFor(campaignId);
          return;
        }

        const mappedTargets = recipients.items.map(mapRecipientToTarget);
        setTargets(mappedTargets);
        setSelected(prev => {
          if (keepExisting && prev.size > 0) {
            const ids = new Set(mappedTargets.map(t => t.id));
            return new Set([...prev].filter(id => ids.has(id)));
          }
          return new Set(
            mappedTargets
              .filter(t => t.status === 'COMPROMISED')
              .map(t => t.id),
          );
        });

        setMetrics({
          answerRate: Math.round(remediation.answerRate),
          failureRate: Math.round(remediation.failureRate),
          riskScore: Math.round(remediation.averageRiskScore),
          compromisedRate: Math.round(remediation.compromiseRate),
        });

        setLoadedFor(campaignId);
        setLoadFailedFor(null);
      } catch {
        if (!cancelled && !keepExisting) setLoadFailedFor(campaignId);
      }
    };

    void load();
    return () => {
      cancelled = true;
    };
    // loadedFor is read only to keep existing data visible during silent refresh.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [campaignId, refreshKey, getRemediation, fetchRecipients]);

  const toggle = (id: string) => {
    setSelected(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const send = async () => {
    if (selected.size === 0) return toast.error('Select at least one target.');
    if (!campaignId) return toast.error('Campaign not found.');

    setSending(true);
    try {
      const results = await Promise.all(
        Array.from(selected).map(recipientId =>
          sendTeachableMoment(campaignId, { recipientId }),
        ),
      );
      if (results.every(Boolean)) {
        toast.success(
          `Teachable-moment notifications sent to ${selected.size} target(s).`,
        );
      } else {
        toast.error('Some notifications failed to send.');
      }
    } finally {
      setSending(false);
    }
  };

  const exportData = async (format: 'csv' | 'json') => {
    if (!campaignId) {
      toast.error('Campaign not found.');
      return;
    }

    setExporting(format);
    try {
      const blob = await exportReport(campaignId, format, true);
      if (!blob) {
        toast.error('Failed to export report.');
        return;
      }

      downloadBlob(blob, `vishing-campaign-report-${campaignId}.${format}`);
      toast.success(`Downloaded ${format.toUpperCase()} report.`);
    } finally {
      setExporting(null);
    }
  };

  const filtered = targets.filter(
    t =>
      t.name.toLowerCase().includes(filter.toLowerCase()) ||
      t.email.includes(filter),
  );

  if (!campaignId) {
    return (
      <EmptyCampaignState message="Select a campaign to view remediation metrics." />
    );
  }

  if (loadFailed) {
    return <EmptyCampaignState message="No campaign found." />;
  }

  if (!loaded) {
    return <LoadingState message="Loading remediation data…" />;
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-4">
        <StatCard
          label="Answer rate"
          value={`${metrics.answerRate}%`}
          icon={PhoneCall}
          accent="text-primary"
        />
        <StatCard
          label="Failure rate"
          value={`${metrics.failureRate}%`}
          icon={AlertCircle}
          accent="text-destructive"
        />
        <StatCard
          label="Risk score"
          value={metrics.riskScore}
          icon={TrendingUp}
          accent="text-amber-500"
        />
        <StatCard
          label="Compromised Rate"
          value={`${metrics.compromisedRate}%`}
          icon={AlertCircle}
          accent="text-destructive"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between gap-3">
              <div>
                <CardTitle className="flex items-center gap-2">
                  <Send className="size-4 text-primary" /> Teachable-Moment
                  Notifications
                </CardTitle>
                <CardDescription>
                  Notify failed targets immediately after the simulation.
                </CardDescription>
              </div>
              <div className="relative w-56">
                <Search className="pointer-events-none absolute left-2 top-2.5 size-4 text-muted-foreground" />
                <Input
                  placeholder="Filter targets"
                  value={filter}
                  onChange={e => setFilter(e.target.value)}
                  className="pl-8"
                />
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex flex-wrap items-center justify-end gap-4 rounded-md border border-card-border/60 bg-muted/20 p-3">
              <Button onClick={send} disabled={sending}>
                <Send className="mr-1 size-4" />
                {sending ? 'Sending…' : `Send to ${selected.size} target(s)`}
              </Button>
            </div>
            <div className="rounded-md border border-card-border/60">
              <div className="grid grid-cols-12 border-b border-card-border/60 bg-muted/30 px-3 py-2 text-xs font-medium text-muted-foreground">
                <div className="col-span-1"></div>
                <div className="col-span-3">Name</div>
                <div className="col-span-3">Email</div>
                <div className="col-span-3">Phone</div>
                <div className="col-span-2">Status</div>
              </div>
              {filtered.length === 0 ? (
                <div className="px-3 py-8 text-center text-sm text-muted-foreground">
                  No recipients found for this campaign.
                </div>
              ) : (
                filtered.map(t => (
                  <label
                    key={t.id}
                    className="grid grid-cols-12 items-center border-b border-card-border/40 px-3 py-2 text-sm last:border-0 hover:bg-muted/20"
                  >
                    <div className="col-span-1">
                      <input
                        type="checkbox"
                        checked={selected.has(t.id)}
                        onChange={() => toggle(t.id)}
                        className="size-4 rounded border-card-border"
                      />
                    </div>
                    <div className="col-span-3 font-medium">{t.name}</div>
                    <div className="col-span-3 truncate text-muted-foreground">
                      {t.email || '—'}
                    </div>
                    <div className="col-span-3 font-mono text-xs text-muted-foreground">
                      {t.phone || '—'}
                    </div>
                    <div className="col-span-2">
                      <StatusBadge status={t.status} />
                    </div>
                  </label>
                ))
              )}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Download className="size-4 text-primary" /> Export
            </CardTitle>
            <CardDescription>
              Download campaign reports for record-keeping.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button
              variant="outline"
              onClick={() => void exportData('csv')}
              disabled={exporting !== null}
              className="w-full justify-start"
            >
              <FileText className="mr-2 size-4" />
              {exporting === 'csv'
                ? 'Downloading CSV…'
                : 'Export campaign as CSV'}
            </Button>
            <Button
              variant="outline"
              onClick={() => void exportData('json')}
              disabled={exporting !== null}
              className="w-full justify-start"
            >
              <FileText className="mr-2 size-4" />
              {exporting === 'json'
                ? 'Downloading JSON…'
                : 'Export campaign as JSON'}
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function WorkflowTab({
  campaignId,
  refreshKey = 0,
}: {
  campaignId: string | null;
  refreshKey?: number;
}) {
  const { getReport } = useVishingCampaigns();
  const [report, setReport] = useState<IVishingReport | null>(null);
  const [loadedFor, setLoadedFor] = useState<string | null>(null);
  const [loadFailedFor, setLoadFailedFor] = useState<string | null>(null);
  const loaded = loadedFor === campaignId;
  const loadFailed = loadFailedFor === campaignId;

  useEffect(() => {
    if (!campaignId) return;

    let cancelled = false;
    const keepExisting = loadedFor === campaignId;

    const load = async () => {
      try {
        const data = await getReport(campaignId);
        if (cancelled) return;
        if (!data) {
          if (!keepExisting) setLoadFailedFor(campaignId);
          return;
        }
        setReport(data);
        setLoadedFor(campaignId);
        setLoadFailedFor(null);
      } catch {
        if (!cancelled && !keepExisting) setLoadFailedFor(campaignId);
      }
    };

    void load();
    return () => {
      cancelled = true;
    };
    // loadedFor is read only to keep existing data visible during silent refresh.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [campaignId, refreshKey, getReport]);

  if (!campaignId) {
    return (
      <EmptyCampaignState message="Select a campaign to view the report summary." />
    );
  }

  if (loadFailed) {
    return <EmptyCampaignState message="No campaign found." />;
  }

  if (!loaded) {
    return <LoadingState message="Loading campaign report…" />;
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="Total calls"
          value={report?.callsTotal ?? '—'}
          icon={PhoneCall}
          accent="text-primary"
        />
        <StatCard
          label="Answered"
          value={report?.callsAnswered ?? '—'}
          icon={CheckCircle2}
          accent="text-emerald-500"
        />
        <StatCard
          label="Engaged"
          value={report?.callsEngaged ?? '—'}
          icon={Activity}
          accent="text-primary"
        />
        <StatCard
          label="Compromised"
          value={report?.callsCompromised ?? '—'}
          icon={AlertCircle}
          accent="text-destructive"
        />
        <StatCard
          label="No answer"
          value={report?.callsNoAnswer ?? '—'}
          icon={PhoneCall}
          accent="text-amber-500"
        />
        <StatCard
          label="Failed"
          value={report?.callsFailed ?? '—'}
          icon={AlertCircle}
          accent="text-destructive"
        />
        <StatCard
          label="Reported"
          value={report?.callsReported ?? '—'}
          icon={ShieldCheck}
          accent="text-emerald-500"
        />
        <StatCard
          label="Retries"
          value={report?.retriesTriggered ?? '—'}
          icon={Repeat}
          accent="text-amber-500"
        />
      </div>
    </div>
  );
}

function NfrTab({
  campaignId,
  refreshKey = 0,
}: {
  campaignId: string | null;
  refreshKey?: number;
}) {
  const { getLiveMetrics, getReport } = useVishingCampaigns();
  const [liveMetrics, setLiveMetrics] = useState({
    activeCalls: 0,
    answeredCalls: 0,
    engagedCalls: 0,
    completedCalls: 0,
  });
  const [report, setReport] = useState<IVishingReport | null>(null);
  const [loadedFor, setLoadedFor] = useState<string | null>(null);
  const [loadFailedFor, setLoadFailedFor] = useState<string | null>(null);
  const loaded = loadedFor === campaignId;
  const loadFailed = loadFailedFor === campaignId;

  useEffect(() => {
    if (!campaignId) return;

    let cancelled = false;
    const keepExisting = loadedFor === campaignId;

    const load = async () => {
      try {
        const [metrics, reportData] = await Promise.all([
          getLiveMetrics(campaignId),
          getReport(campaignId),
        ]);
        if (cancelled) return;
        if (!metrics && !reportData) {
          if (!keepExisting) setLoadFailedFor(campaignId);
          return;
        }
        if (metrics) {
          setLiveMetrics(metrics);
        }
        setReport(reportData);
        setLoadedFor(campaignId);
        setLoadFailedFor(null);
      } catch {
        if (!cancelled && !keepExisting) setLoadFailedFor(campaignId);
      }
    };

    void load();

    const timer = window.setInterval(() => {
      void getLiveMetrics(campaignId).then(metrics => {
        if (!cancelled && metrics) setLiveMetrics(metrics);
      });
    }, 15000);

    return () => {
      cancelled = true;
      window.clearInterval(timer);
    };
    // loadedFor is read only to keep existing data visible during silent refresh.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [campaignId, refreshKey, getLiveMetrics, getReport]);

  if (!campaignId) {
    return (
      <EmptyCampaignState message="Select a campaign to view live metrics." />
    );
  }

  if (loadFailed) {
    return <EmptyCampaignState message="No campaign found." />;
  }

  if (!loaded) {
    return <LoadingState message="Loading live metrics…" />;
  }

  const failedCalls = report?.callsFailed ?? 0;
  const retries = report?.retriesTriggered ?? 0;

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-4">
        <StatCard
          label="Active calls"
          value={liveMetrics.activeCalls}
          icon={PhoneCall}
          accent="text-primary"
        />
        <StatCard
          label="Answered"
          value={liveMetrics.answeredCalls}
          icon={CheckCircle2}
          accent="text-emerald-500"
        />
        <StatCard
          label="Engaged"
          value={liveMetrics.engagedCalls}
          icon={Activity}
          accent="text-primary"
        />
        <StatCard
          label="Completed"
          value={liveMetrics.completedCalls}
          icon={ShieldCheck}
          accent="text-emerald-500"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ServerCog className="size-4 text-primary" /> Live simulation status
          </CardTitle>
          <CardDescription>
            Call throughput and campaign outcome counts from the API.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <ThresholdRow
            label="Active calls"
            value={liveMetrics.activeCalls}
            max={Math.max(liveMetrics.activeCalls, 1)}
            unit=""
          />
          <ThresholdRow
            label="Answered calls"
            value={liveMetrics.answeredCalls}
            max={Math.max(
              liveMetrics.completedCalls,
              liveMetrics.answeredCalls,
              1,
            )}
            unit=""
          />
          <ThresholdRow
            label="Completed calls"
            value={liveMetrics.completedCalls}
            max={Math.max(liveMetrics.completedCalls, 1)}
            unit=""
          />
          <Separator />
          <ul className="space-y-1 text-sm">
            <li className="flex items-center justify-between rounded-md border border-card-border/60 px-2 py-1.5">
              <span>Active calls in progress</span>
              <Badge
                variant="outline"
                className="border-primary/40 text-primary"
              >
                {liveMetrics.activeCalls}
              </Badge>
            </li>
            <li className="flex items-center justify-between rounded-md border border-card-border/60 px-2 py-1.5">
              <span>Failed calls</span>
              <Badge
                variant="outline"
                className="border-destructive/50 text-destructive"
              >
                {failedCalls}
              </Badge>
            </li>
            <li className="flex items-center justify-between rounded-md border border-card-border/60 px-2 py-1.5">
              <span>Retries triggered</span>
              <Badge
                variant="outline"
                className="border-amber-500/40 text-amber-500"
              >
                {retries}
              </Badge>
            </li>
          </ul>
        </CardContent>
      </Card>
    </div>
  );
}

function EmptyCampaignState({ message }: { message: string }) {
  return (
    <div className="rounded-lg border border-dashed border-card-border/60 bg-muted/10 p-10 text-center">
      <PhoneCall className="mx-auto mb-3 size-8 text-muted-foreground" />
      <p className="text-sm text-muted-foreground">{message}</p>
    </div>
  );
}

function LoadingState({ message }: { message: string }) {
  return (
    <div className="flex items-center justify-center gap-2 rounded-lg border border-card-border/60 bg-muted/10 p-10 text-sm text-muted-foreground">
      <Loader2 className="size-4 animate-spin" />
      {message}
    </div>
  );
}

function ThresholdRow({
  label,
  value,
  max,
  unit,
  inverse,
}: {
  label: string;
  value: number;
  max: number;
  unit: string;
  inverse?: boolean;
}) {
  const ok = inverse ? value >= max : value <= max;
  const pct = Math.min(100, (value / max) * 100);
  return (
    <div className="space-y-1">
      <div className="flex items-center justify-between text-sm">
        <span>{label}</span>
        <span className={ok ? 'text-emerald-500' : 'text-destructive'}>
          {value}
          {unit} / {max}
          {unit}
        </span>
      </div>
      <Progress value={pct} />
    </div>
  );
}
