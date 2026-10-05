export interface ITier {
  id: string;
  tierName: string;
  commissionPercentage: number;
  salesThreshold: number;
  active: boolean;
  eligibilityCriteria: string;
  tierBenefits: string;
  createdAt: string;
  tierDescription: string;
}

export interface ITierSummary {
  totalTiers: number;
  activeTiers: number;
  inactiveTiers: number;
}
