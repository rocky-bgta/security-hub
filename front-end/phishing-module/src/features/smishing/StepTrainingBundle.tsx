import { useState } from 'react';
import {
  AlertCircle,
  BookOpen,
  Clock,
  FileText,
  RotateCcw,
  Search,
  Sparkles,
} from 'lucide-react';
import { Badge } from 'components/common/Badge';
import { Card, CardContent, CardHeader } from 'components/common/Card';
import { Checkbox } from 'components/common/Checkbox';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import {
  PACKAGES,
  TRAINING_FILTERS,
  TRAINING_MODULES,
} from 'features/smishing/data';
import { cn } from 'utils/Helper';

export default function StepTrainingBundle(props: {
  trProduct: string;
  setTrProduct: (v: string) => void;
  trPackage: string;
  setTrPackage: (v: string) => void;
  trDurationType: string;
  setTrDurationType: (v: string) => void;
  trDurationValue: string;
  setTrDurationValue: (v: string) => void;
  trCategory: string;
  setTrCategory: (v: string) => void;
  trDifficulty: string;
  setTrDifficulty: (v: string) => void;
  trSelected: string[];
  setTrSelected: (v: string[]) => void;
}) {
  const [search, setSearch] = useState('');
  const toggle = (id: string) =>
    props.setTrSelected(
      props.trSelected.includes(id)
        ? props.trSelected.filter(x => x !== id)
        : [...props.trSelected, id],
    );

  const filtered = TRAINING_MODULES.filter(m => {
    if (search && !m.title.toLowerCase().includes(search.toLowerCase())) {
      return false;
    }
    return true;
  });

  const resetFilters = () => {
    setSearch('');
    props.setTrCategory('All');
    props.setTrDifficulty('All');
  };

  const filterRows: { label: string; options: string[] }[][] = [
    [
      { label: 'Country', options: TRAINING_FILTERS.country },
      { label: 'Compliance', options: TRAINING_FILTERS.compliance },
    ],
    [
      { label: 'Categories', options: TRAINING_FILTERS.category },
      { label: 'Content Types', options: ['Video', 'Article', 'Interactive'] },
      {
        label: 'Duration Range',
        options: ['< 5 min', '5-10 min', '10-20 min', '> 20 min'],
      },
    ],
    [
      { label: 'Payload Type', options: ['Link', 'Attachment', 'QR', 'Voice'] },
      { label: 'Difficulty', options: TRAINING_FILTERS.difficulty },
      { label: 'Tone', options: TRAINING_FILTERS.tone },
    ],
    [
      {
        label: 'Attacker Persona',
        options: ['IT Support', 'Vendor', 'Executive', 'Recruiter'],
      },
      {
        label: 'Social Engineering Strategy',
        options: ['Authority', 'Urgency', 'Reciprocity', 'Fear'],
      },
      {
        label: 'Campaign Objective',
        options: ['Awareness', 'Compliance', 'Resilience'],
      },
    ],
    [
      {
        label: 'Trigger Event',
        options: ['Login', 'Payment', 'Delivery', 'Reset'],
      },
      {
        label: 'Attack Technique',
        options: ['Credential Harvest', 'Malware', 'OAuth Consent'],
      },
      {
        label: 'Emotional Trigger',
        options: ['Fear', 'Curiosity', 'Reward', 'Anger'],
      },
    ],
    [
      {
        label: 'Urgency Level',
        options: ['Low', 'Medium', 'High', 'Critical'],
      },
      {
        label: 'Brand',
        options: ['Microsoft', 'Google', 'DHL', 'PayPal', 'Generic'],
      },
      {
        label: 'Call to Action',
        options: ['Verify', 'Reset', 'Confirm', 'Reschedule'],
      },
    ],
  ];

  return (
    <Card className="relative space-y-8">
      <CardHeader className="space-y-1.5">
        <h2 className="flex items-center gap-2 text-2xl font-semibold tracking-tight text-white sm:text-3xl">
          <BookOpen className="size-6 text-[#00FFA3]" />
          Training & content bundle
        </h2>
        <p className="text-sm text-white/60">
          Tie training modules to a licensed product and package, then select
          modules assigned when users fail the simulation.
        </p>
      </CardHeader>

      <CardContent className="space-y-6">
        <section className="rounded-2xl border border-white/10 bg-white/[0.02] p-5 backdrop-blur">
          <h3 className="mb-4 text-sm font-semibold uppercase tracking-wide text-white/80">
            License, package & completion period
          </h3>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2">
              <Label className="text-xs text-white/70">Product</Label>
              <Input
                readOnly
                value={props.trProduct}
                className="h-10 rounded-xl border-white/10 bg-white/[0.04] text-white"
              />
            </div>
            <div className="space-y-2">
              <Label className="text-xs text-white/70">Package</Label>
              <Select
                value={props.trPackage}
                onValueChange={props.setTrPackage}
              >
                <SelectTrigger className="h-10 rounded-xl border-white/10 bg-white/[0.04] text-white focus:ring-2 focus:ring-[#00FFA3]/30">
                  <SelectValue placeholder="Select package" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Gold (expires May 24, 2027)">
                    Gold (expires May 24, 2027)
                  </SelectItem>
                  {PACKAGES.map(p => (
                    <SelectItem key={p} value={p}>
                      {p}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label className="text-xs text-white/70">
                Training Completion Period
              </Label>
              <Select
                value={props.trDurationType}
                onValueChange={props.setTrDurationType}
              >
                <SelectTrigger className="h-10 rounded-xl border-white/10 bg-white/[0.04] text-white focus:ring-2 focus:ring-[#00FFA3]/30">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {['Days', 'Weeks'].map(d => (
                    <SelectItem key={d} value={d}>
                      {d}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label className="text-xs text-white/70">Period Length</Label>
              <Input
                type="number"
                min={1}
                max={365}
                value={props.trDurationValue}
                onChange={e => props.setTrDurationValue(e.target.value)}
                placeholder="Enter duration"
                className="h-10 rounded-xl border-white/10 bg-white/[0.04] text-white placeholder:text-white/30 focus-visible:border-[#00FFA3]/60 focus-visible:ring-2 focus-visible:ring-[#00FFA3]/30"
              />
              <p className="text-[11px] text-white/40">
                Between 1 and 365 days
              </p>
            </div>
          </div>
        </section>

        <section className="rounded-2xl border border-white/10 bg-white/[0.02] p-5 backdrop-blur">
          <div className="mb-4 flex items-center justify-between">
            <h3 className="flex items-center gap-2 text-sm font-semibold uppercase tracking-wide text-white/80">
              <Search className="size-4 text-[#00FFA3]" />
              Filter training modules
            </h3>
            <button
              type="button"
              onClick={resetFilters}
              className="flex items-center gap-1 rounded-lg border border-white/10 bg-white/[0.03] px-3 py-1.5 text-xs text-white/70 transition hover:bg-white/[0.06] hover:text-white"
            >
              <RotateCcw className="size-3.5" /> Reset
            </button>
          </div>

          <div className="space-y-3">
            <div className="grid gap-3 md:grid-cols-3">
              <div className="space-y-1.5">
                <Label className="text-[11px] text-white/60">
                  Search module
                </Label>
                <Input
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  placeholder="Search by title..."
                  className="h-9 rounded-lg border-white/10 bg-white/[0.04] text-sm text-white placeholder:text-white/30 focus-visible:border-[#00FFA3]/60 focus-visible:ring-2 focus-visible:ring-[#00FFA3]/30"
                />
              </div>
              {filterRows[0].map(f => (
                <div key={f.label} className="space-y-1.5">
                  <Label className="text-[11px] text-white/60">{f.label}</Label>
                  <Select>
                    <SelectTrigger className="h-9 rounded-lg border-white/10 bg-white/[0.04] text-sm text-white">
                      <SelectValue placeholder="Any" />
                    </SelectTrigger>
                    <SelectContent>
                      {f.options.map(o => (
                        <SelectItem key={o} value={o}>
                          {o}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              ))}
            </div>
            {filterRows.slice(1).map((row, i) => (
              <div key={i} className="grid gap-3 md:grid-cols-3">
                {row.map(f => (
                  <div key={f.label} className="space-y-1.5">
                    <Label className="text-[11px] text-white/60">
                      {f.label}
                    </Label>
                    <Select>
                      <SelectTrigger className="h-9 rounded-lg border-white/10 bg-white/[0.04] text-sm text-white">
                        <SelectValue placeholder="Any" />
                      </SelectTrigger>
                      <SelectContent>
                        {f.options.map(o => (
                          <SelectItem key={o} value={o}>
                            {o}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                ))}
              </div>
            ))}
          </div>
        </section>

        <section className="rounded-2xl border border-[#00FFA3]/20 bg-[#00FFA3]/[0.03] p-5 backdrop-blur">
          <h3 className="flex items-center gap-2 text-sm font-semibold uppercase tracking-wide text-white/80">
            <Sparkles className="size-4 text-[#00FFA3]" />
            AI Recommended Training Modules
          </h3>
          <div className="mt-4 rounded-xl border border-dashed border-white/10 p-6 text-center">
            <p className="text-sm text-white/50">
              No AI recommendations available for this campaign yet.
            </p>
          </div>
        </section>

        <section className="space-y-4">
          <div className="flex items-end justify-between gap-3">
            <div>
              <h3 className="text-sm font-semibold uppercase tracking-wide text-white/80">
                Select training modules ({filtered.length} modules available)
              </h3>
              <p className="mt-1 text-xs text-white/50">
                At least two modules are required
              </p>
            </div>
            <Badge className="bg-[#00FFA3]/15 text-[#00FFA3] hover:bg-[#00FFA3]/20">
              {props.trSelected.length} selected
            </Badge>
          </div>

          <div className="grid gap-3 md:grid-cols-2">
            {filtered.map(m => {
              const sel = props.trSelected.includes(m.id);
              return (
                <button
                  key={m.id}
                  type="button"
                  onClick={() => toggle(m.id)}
                  className={cn(
                    'group relative overflow-hidden rounded-2xl border p-4 text-left transition-all duration-200',
                    'hover:-translate-y-0.5 hover:border-white/20 hover:bg-white/[0.04]',
                    sel
                      ? 'border-[#00FFA3]/60 bg-[#00FFA3]/[0.06] shadow-[0_0_0_1px_rgba(0,255,163,0.4),0_0_30px_-8px_rgba(0,255,163,0.45)]'
                      : 'border-white/10 bg-white/[0.02]',
                  )}
                >
                  <div className="flex items-start gap-3">
                    <Checkbox
                      checked={sel}
                      onCheckedChange={() => toggle(m.id)}
                      onClick={e => e.stopPropagation()}
                      className={cn(
                        'mt-0.5 border-white/30',
                        sel &&
                          'border-[#00FFA3] bg-[#00FFA3] text-black data-[state=checked]:bg-[#00FFA3] data-[state=checked]:text-black',
                      )}
                    />
                    <div className="flex-1 space-y-2">
                      <div className="flex items-start justify-between gap-2">
                        <div className="text-sm font-semibold text-white">
                          {m.title}
                        </div>
                      </div>
                      <p className="text-xs text-white/55">{m.desc}</p>
                      <div className="flex flex-wrap gap-1.5">
                        {m.tags.map(t => (
                          <span
                            key={t}
                            className="rounded-full border border-white/10 bg-white/[0.04] px-2 py-0.5 text-[10px] text-white/70"
                          >
                            {t}
                          </span>
                        ))}
                      </div>
                      <div className="flex items-center gap-3 pt-1 text-[11px] text-white/50">
                        <span className="inline-flex items-center gap-1">
                          <FileText className="size-3" /> {m.type}
                        </span>
                        <span className="inline-flex items-center gap-1">
                          <Clock className="size-3" /> {m.duration}
                        </span>
                      </div>
                    </div>
                  </div>
                </button>
              );
            })}
          </div>

          {props.trSelected.length < 2 && (
            <p className="flex items-center gap-1.5 text-xs text-amber-300/90">
              <AlertCircle className="size-3.5" />
              Select at least two modules to continue.
            </p>
          )}
        </section>
      </CardContent>
    </Card>
  );
}
