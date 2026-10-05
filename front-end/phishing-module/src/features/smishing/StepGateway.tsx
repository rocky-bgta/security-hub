import { useMemo } from 'react';
import { AlertCircle, Check, Globe, ServerCog } from 'lucide-react';
import { Card, CardContent, CardHeader } from 'components/common/Card';
import { GATEWAYS, USER_COUNTRY } from 'features/smishing/data';
import { cn } from 'utils/Helper';

export default function StepGateway(props: {
  gateway: string;
  setGateway: (v: string) => void;
  senderId: string;
  setSenderId: (v: string) => void;
  senderIdValid: boolean;
  gatewayStatus: 'idle' | 'checking' | 'ok' | 'error';
  checkGateway: () => void;
}) {
  const filtered = useMemo(
    () =>
      GATEWAYS.filter(
        g => g.country === USER_COUNTRY || g.country === 'GLOBAL',
      ),
    [],
  );
  const selected = filtered.find(g => g.id === props.gateway);
  const showReuseWarning = !!selected?.previouslyUsed;

  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between gap-4">
        <div className="space-y-1.5">
          <div className="flex items-center gap-2">
            <ServerCog className="size-5 text-[#00FFA3]" />
            <h2 className="text-xl font-semibold tracking-tight text-white sm:text-2xl">
              SMS Gateway Configuration
            </h2>
          </div>
          <p className="text-sm text-white/60">
            Select an outbound SMS gateway provider for message delivery.
          </p>
        </div>
        <div className="hidden shrink-0 items-center gap-1.5 rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs text-white/70 sm:flex">
          <Globe className="size-3.5 text-[#00FFA3]" />
          Region: {USER_COUNTRY}
        </div>
      </CardHeader>

      <CardContent className="space-y-6">
        <div className="grid gap-3">
          {filtered.map(g => {
            const isSelected = props.gateway === g.id;
            return (
              <button
                key={g.id}
                type="button"
                onClick={() => props.setGateway(g.id)}
                className={cn(
                  'group relative flex w-full items-center justify-between gap-4 rounded-2xl border bg-white/[0.03] p-4 text-left backdrop-blur-md transition-all duration-200',
                  'hover:-translate-y-0.5 hover:bg-white/[0.06] hover:shadow-[0_8px_30px_-10px_rgba(0,255,163,0.35)]',
                  isSelected
                    ? 'border-[#00FFA3]/60 bg-[#00FFA3]/[0.06] shadow-[0_0_0_1px_rgba(0,255,163,0.4),0_0_40px_-10px_rgba(0,255,163,0.6)]'
                    : 'border-white/10',
                )}
              >
                <div className="flex min-w-0 items-center gap-3">
                  <div
                    className={cn(
                      'flex size-10 shrink-0 items-center justify-center rounded-xl border transition-colors',
                      isSelected
                        ? 'border-[#00FFA3]/40 bg-[#00FFA3]/10 text-[#00FFA3]'
                        : 'border-white/10 bg-white/5 text-white/60',
                    )}
                  >
                    <ServerCog className="size-5" />
                  </div>
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-sm font-medium text-white">
                        {g.name}
                      </span>
                      <span className="text-xs text-white/40">.</span>
                      <span className="text-xs text-white/50">{g.region}</span>
                      {g.tag && (
                        <span className="rounded-full border border-[#00FFA3]/30 bg-[#00FFA3]/10 px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide text-[#00FFA3]">
                          {g.tag}
                        </span>
                      )}
                    </div>
                    <div className="mt-1 truncate font-mono text-xs text-white/50">
                      {g.endpoint}
                    </div>
                  </div>
                </div>

                <div className="flex shrink-0 items-center gap-3">
                  <span
                    className={cn(
                      'inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[11px] font-medium',
                      g.connected
                        ? 'border border-[#00FFA3]/30 bg-[#00FFA3]/10 text-[#00FFA3]'
                        : 'border border-white/10 bg-white/5 text-white/50',
                    )}
                  >
                    <span
                      className={cn(
                        'h-1.5 w-1.5 rounded-full',
                        g.connected ? 'bg-[#00FFA3]' : 'bg-white/40',
                      )}
                    />
                    {g.connected ? 'Connected' : 'Not Connected'}
                  </span>
                  <div
                    className={cn(
                      'flex size-6 items-center justify-center rounded-full border transition-all',
                      isSelected
                        ? 'border-[#00FFA3] bg-[#00FFA3] text-black'
                        : 'border-white/15 bg-transparent text-transparent',
                    )}
                  >
                    <Check className="size-3.5" strokeWidth={3} />
                  </div>
                </div>
              </button>
            );
          })}
        </div>

        {showReuseWarning && (
          <div className="flex items-start gap-3 rounded-xl border border-amber-400/30 bg-amber-400/[0.06] p-4 text-sm text-amber-100 shadow-[0_0_30px_-10px_rgba(251,191,36,0.4)]">
            <AlertCircle className="mt-0.5 size-4 shrink-0 text-amber-300" />
            <div className="space-y-0.5">
              <div className="font-medium text-amber-200">
                Gateway recently used
              </div>
              <p className="text-xs leading-relaxed text-amber-100/80">
                You have previously used{' '}
                <span className="font-medium text-amber-100">
                  {selected?.name}
                </span>
                . For better delivery performance and to avoid potential routing
                or blocking issues, consider selecting a different SMS provider.
              </p>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
