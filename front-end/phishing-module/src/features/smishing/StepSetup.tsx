import { Card, CardContent, CardHeader } from 'components/common/Card';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import { RadioGroup, RadioGroupItem } from 'components/common/Radio';
import { Checkbox } from 'components/common/Checkbox';
import { cn } from 'utils/Helper';

export default function StepSetup(props: {
  campaignName: string;
  setCampaignName: (v: string) => void;
  campaignType: 'smishing' | 'smishing_training';
  setCampaignType: (v: 'smishing' | 'smishing_training') => void;
  stages: ('click' | 'compromised')[];
  toggleStage: (s: 'click' | 'compromised') => void;
  learningMode: 'micro';
  setLearningMode: (v: 'micro') => void;
}) {
  const {
    campaignType,
    setCampaignType,
    stages,
    toggleStage,
    learningMode,
    setLearningMode,
  } = props;

  const max = 50;
  const name = props.campaignName.slice(0, max);
  const count = name.length;

  const types = [
    {
      id: 'smishing' as const,
      title: 'Smishing Simulation',
      desc: 'Send AI Smishing Simulation Without Training.',
      badge: null as string | null,
    },
    {
      id: 'smishing_training' as const,
      title: 'AI Smishing with Training',
      desc: 'Send AI Smishing Simulation and assign training to users who fail.',
      badge: 'Recommended',
    },
  ];

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <h2 className="text-2xl font-semibold tracking-tight text-white sm:text-3xl">
            Campaign Setup
          </h2>
          <p className="text-sm text-white/60">
            Enter a name and select the campaign type.
          </p>
        </CardHeader>

        <CardContent className="space-y-6">
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label className="text-sm font-medium text-white/80">
                Campaign Name *
              </Label>
              <span
                className={cn(
                  'text-xs tabular-nums text-white/40',
                  count >= max && 'text-[#00FFA3]',
                )}
              >
                {count}/{max}
              </span>
            </div>
            <Input
              value={name}
              onChange={e =>
                props.setCampaignName(e.target.value.slice(0, max))
              }
              placeholder="Enter campaign name"
              maxLength={max}
            />
          </div>

          <div className="space-y-3">
            <Label className="text-sm font-medium text-white/80">
              Campaign Type
            </Label>
            <RadioGroup
              value={campaignType}
              onValueChange={v =>
                setCampaignType(v as 'smishing' | 'smishing_training')
              }
              className="grid gap-3"
            >
              {types.map(t => {
                const selected = campaignType === t.id;
                return (
                  <label
                    key={t.id}
                    htmlFor={`ctype-${t.id}`}
                    className={cn(
                      'group relative flex cursor-pointer items-start gap-4 rounded-2xl border p-5 transition-all duration-200',
                      'hover:-translate-y-0.5 hover:border-white/20 hover:bg-white/[0.04]',
                      selected
                        ? 'border-[#00FFA3]/60 bg-[#00FFA3]/[0.06] shadow-[0_0_0_1px_rgba(0,255,163,0.4),0_0_30px_-8px_rgba(0,255,163,0.45)]'
                        : 'border-white/10 bg-white/[0.02]',
                    )}
                  >
                    <RadioGroupItem
                      id={`ctype-${t.id}`}
                      value={t.id}
                      className={cn(
                        'mt-1 size-4 border-white/30',
                        selected &&
                          'border-[#00FFA3] text-[#00FFA3] [&_svg]:fill-[#00FFA3]',
                      )}
                    />
                    <div className="flex-1 space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="text-base font-semibold text-white">
                          {t.title}
                        </span>
                        {t.badge && (
                          <span className="rounded-full border border-[#00FFA3]/40 bg-[#00FFA3]/10 px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide text-[#00FFA3]">
                            {t.badge}
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-white/55">{t.desc}</p>
                    </div>
                  </label>
                );
              })}
            </RadioGroup>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <h2 className="text-xl font-semibold tracking-tight text-white sm:text-2xl">
            Phishing Response Stage & Learning Mode
          </h2>
          <p className="text-sm text-white/60">
            Select at least one response stage and learning mode.
          </p>
        </CardHeader>

        <CardContent className="space-y-6">
          <div className="grid gap-4 sm:grid-cols-2">
            {[
              {
                id: 'click' as const,
                title: 'Click',
                desc: 'If a user clicks a campaign link, micro training will be automatically assigned.',
                badge: null as string | null,
              },
              {
                id: 'compromised' as const,
                title: 'Compromised',
                desc: 'If a user submits credentials during a campaign, micro training will be automatically assigned.',
                badge: 'Recommended',
              },
            ].map(s => {
              const checked = stages.includes(s.id);
              return (
                <label
                  key={s.id}
                  className={cn(
                    'group relative flex cursor-pointer items-start gap-3 rounded-xl border p-4 transition-all duration-200',
                    'hover:-translate-y-0.5 hover:border-white/20 hover:bg-white/[0.04]',
                    checked
                      ? 'border-[#00FFA3]/60 bg-[#00FFA3]/[0.06] shadow-[0_0_0_1px_rgba(0,255,163,0.4),0_0_30px_-8px_rgba(0,255,163,0.45)]'
                      : 'border-white/10 bg-white/[0.02]',
                  )}
                >
                  <Checkbox
                    checked={checked}
                    onCheckedChange={() => toggleStage(s.id)}
                    className={cn(
                      'mt-0.5 border-white/30',
                      checked &&
                        'border-[#00FFA3] bg-[#00FFA3] text-black data-[state=checked]:bg-[#00FFA3] data-[state=checked]:text-black',
                    )}
                  />
                  <div className="flex-1 space-y-1">
                    <div className="flex items-center justify-between gap-2">
                      <span className="text-sm font-semibold text-white">
                        {s.title}
                      </span>
                      {s.badge && (
                        <span className="rounded-full border border-[#00FFA3]/40 bg-[#00FFA3]/10 px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide text-[#00FFA3]">
                          {s.badge}
                        </span>
                      )}
                    </div>
                    <p className="text-xs leading-relaxed text-white/55">
                      {s.desc}
                    </p>
                  </div>
                </label>
              );
            })}
          </div>

          {stages.length === 0 && (
            <p className="text-xs text-red-400/80">
              Please select at least one response stage.
            </p>
          )}

          <div className="h-px w-full bg-white/10" />

          <div>
            <RadioGroup
              value={learningMode}
              onValueChange={v => setLearningMode(v as 'micro')}
              className="grid gap-3"
            >
              <label
                htmlFor="lm-micro"
                className={cn(
                  'group relative flex cursor-pointer items-start gap-3 rounded-xl border p-4 transition-all duration-200',
                  'hover:-translate-y-0.5 hover:border-white/20 hover:bg-white/[0.04]',
                  learningMode === 'micro'
                    ? 'border-[#00FFA3]/60 bg-[#00FFA3]/[0.06] shadow-[0_0_0_1px_rgba(0,255,163,0.4),0_0_30px_-8px_rgba(0,255,163,0.45)]'
                    : 'border-white/10 bg-white/[0.02]',
                )}
              >
                <RadioGroupItem
                  id="lm-micro"
                  value="micro"
                  className={cn(
                    'mt-0.5 size-4 border-white/30',
                    learningMode === 'micro' &&
                      'border-[#00FFA3] text-[#00FFA3] [&_svg]:fill-[#00FFA3]',
                  )}
                />
                <div className="flex-1 space-y-1">
                  <span className="text-sm font-semibold text-white">
                    Micro Content
                  </span>
                  <p className="text-xs leading-relaxed text-white/55">
                    Short training module, duration not exceeding 3 minutes.
                  </p>
                </div>
              </label>
            </RadioGroup>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
