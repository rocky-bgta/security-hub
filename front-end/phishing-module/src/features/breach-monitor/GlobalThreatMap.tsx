import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';

const GlobalThreatMap = () => {
  // Simplified SVG world map with threat hotspots
  const hotspots = [
    { cx: 170, cy: 95, r: 12, intensity: 0.9, label: 'North America' },
    { cx: 310, cy: 100, r: 15, intensity: 1, label: 'Europe' },
    { cx: 365, cy: 120, r: 10, intensity: 0.7, label: 'Middle East' },
    { cx: 420, cy: 110, r: 13, intensity: 0.85, label: 'South Asia' },
    { cx: 480, cy: 115, r: 14, intensity: 0.95, label: 'East Asia' },
    { cx: 220, cy: 160, r: 8, intensity: 0.5, label: 'South America' },
    { cx: 330, cy: 170, r: 7, intensity: 0.4, label: 'Africa' },
    { cx: 500, cy: 180, r: 6, intensity: 0.3, label: 'Oceania' },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Global Threat Map</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="relative overflow-hidden rounded-lg">
          <svg viewBox="0 0 600 280" className="h-auto w-full">
            {/* Simplified continent outlines */}
            <defs>
              <radialGradient id="hotspot-grad">
                <stop
                  offset="0%"
                  stopColor="hsl(0 72% 51%)"
                  stopOpacity="0.8"
                />
                <stop
                  offset="100%"
                  stopColor="hsl(0 72% 51%)"
                  stopOpacity="0"
                />
              </radialGradient>
            </defs>

            {/* Background grid */}
            {Array.from({ length: 12 }).map((_, i) => (
              <line
                key={`vl-${i}`}
                x1={i * 50}
                y1={0}
                x2={i * 50}
                y2={280}
                stroke="hsl(220 20% 18%)"
                strokeWidth={0.5}
              />
            ))}
            {Array.from({ length: 6 }).map((_, i) => (
              <line
                key={`hl-${i}`}
                x1={0}
                y1={i * 50}
                x2={600}
                y2={i * 50}
                stroke="hsl(220 20% 18%)"
                strokeWidth={0.5}
              />
            ))}

            {/* Simplified continents as rough shapes */}
            {/* North America */}
            <path
              d="M100,50 L200,40 L220,80 L200,130 L160,140 L120,120 L100,80 Z"
              fill="hsl(220 20% 25%)"
              opacity={0.5}
            />
            {/* South America */}
            <path
              d="M180,150 L230,140 L240,180 L230,220 L200,230 L180,200 Z"
              fill="hsl(220 20% 25%)"
              opacity={0.5}
            />
            {/* Europe */}
            <path
              d="M280,40 L340,35 L350,80 L330,100 L290,95 L275,70 Z"
              fill="hsl(220 20% 25%)"
              opacity={0.5}
            />
            {/* Africa */}
            <path
              d="M290,110 L350,105 L360,150 L340,200 L310,210 L290,170 Z"
              fill="hsl(220 20% 25%)"
              opacity={0.5}
            />
            {/* Asia */}
            <path
              d="M350,30 L500,25 L520,80 L500,130 L430,140 L380,120 L360,80 Z"
              fill="hsl(220 20% 25%)"
              opacity={0.5}
            />
            {/* Oceania */}
            <path
              d="M470,160 L530,155 L540,190 L510,200 L480,195 Z"
              fill="hsl(220 20% 25%)"
              opacity={0.5}
            />

            {/* Hotspots */}
            {hotspots.map((h, i) => (
              <g key={i}>
                <circle
                  cx={h.cx}
                  cy={h.cy}
                  r={h.r * 2.5}
                  fill="hsl(0 72% 51%)"
                  opacity={h.intensity * 0.15}
                />
                <circle
                  cx={h.cx}
                  cy={h.cy}
                  r={h.r * 1.5}
                  fill="hsl(0 72% 51%)"
                  opacity={h.intensity * 0.3}
                />
                <circle
                  cx={h.cx}
                  cy={h.cy}
                  r={h.r * 0.7}
                  fill="hsl(25 95% 53%)"
                  opacity={h.intensity * 0.8}
                />
                <circle
                  cx={h.cx}
                  cy={h.cy}
                  r={h.r * 0.3}
                  fill="hsl(45 93% 80%)"
                  opacity={h.intensity}
                />
              </g>
            ))}
          </svg>
        </div>
        <div className="mt-3 flex items-center justify-center gap-4 text-xs text-muted-foreground">
          <span className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-full bg-[#dc2626]" /> High
            Activity
          </span>
          <span className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-full bg-[#f59e0b]" /> Medium
            Activity
          </span>
          <span className="flex items-center gap-1.5">
            <span className="size-2.5 rounded-full bg-[#22c55e]" /> Low Activity
          </span>
        </div>
      </CardContent>
    </Card>
  );
};

export default GlobalThreatMap;
