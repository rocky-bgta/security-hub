import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'common/Table';
import {
  IDashboardRiskImpact,
  RISK_COLORS,
  RiskLevel,
  getRiskLevelLabel,
} from 'models/Dashboard';
import { Link } from 'react-router-dom';
import { cn } from 'utils/Helper';
import {
  getSimulationPaths,
  type SimulationChannel,
} from 'utils/SimulationChannel';

interface CampaignRiskImpactProps {
  data?: IDashboardRiskImpact[];
  channel?: SimulationChannel;
}

const CampaignRiskImpact = ({
  data,
  channel = 'phishing',
}: CampaignRiskImpactProps) => {
  const displayData = data ?? [];

  const getRiskColor = (level: RiskLevel) => {
    return RISK_COLORS[level] || '#667eea';
  };

  const getAiRatingColor = (rating: string) => {
    switch (rating?.toLowerCase()) {
      case 'too effective':
        return 'text-emerald-400';
      case 'effective':
        return 'text-cyan-400';
      case 'balanced':
        return 'text-orange-400';
      case 'ineffective':
        return 'text-red-400';
      default:
        return 'text-gray-400';
    }
  };

  const getAiRatingBg = (rating: string) => {
    switch (rating?.toLowerCase()) {
      case 'too effective':
        return 'bg-emerald-500/10';
      case 'effective':
        return 'bg-cyan-500/10';
      case 'balanced':
        return 'bg-orange-500/10';
      case 'ineffective':
        return 'bg-red-500/10';
      default:
        return 'bg-gray-500/10';
    }
  };

  return (
    <Card className="h-full">
      <CardHeader>
        <CardTitle className="text-xl">Campaign Risk Impact</CardTitle>
      </CardHeader>
      <CardContent>
        <Table className="border-0">
          <TableHeader className="bg-transparent">
            <TableRow>
              <TableHead>Campaign Name</TableHead>
              <TableHead>Target Group</TableHead>
              <TableHead>Risk Impact</TableHead>
              <TableHead>AI Rating</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {displayData.length > 0 ? (
              displayData.map(row => (
                <TableRow key={row.campaignId}>
                  <TableCell className="capitalize">
                    {row.campaignName || 'N/A'}
                  </TableCell>
                  <TableCell className="capitalize">
                    {row.targetGroup?.toLowerCase().replace(/_/g, ' ') || 'N/A'}
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <span
                        className="size-2.5 rounded-full"
                        style={{
                          backgroundColor: getRiskColor(row.riskImpact),
                        }}
                      />
                      <span>{getRiskLevelLabel(row.riskImpact) || 'N/A'}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <span
                      className={cn(
                        'flex w-28 items-center rounded px-3 py-1 text-xs font-medium',
                        getAiRatingBg(row.aiRating),
                        getAiRatingColor(row.aiRating),
                      )}
                    >
                      {row.aiRating || 'N/A'}
                    </span>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell colSpan={4} className="py-8 text-center">
                  No data available
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>

        {displayData.length > 0 && (
          <div className="mt-4 text-right">
            <Link
              to={getSimulationPaths(channel).campaignRiskImpacts}
              state={{ fromDashboard: true, channel }}
              className="text-sm font-medium text-primary"
            >
              More Campaign Details &gt;
            </Link>
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export default CampaignRiskImpact;
