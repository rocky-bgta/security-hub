import { Globe, Mail, UserX, AlertTriangle } from 'lucide-react';

import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';

const tactics = [
  {
    icon: Globe,
    label: 'Phishing Websites',
    description: 'Fake login pages mimicking legitimate services',
  },
  {
    icon: Mail,
    label: 'Spoofed Emails',
    description: 'Emails impersonating trusted senders',
  },
  {
    icon: AlertTriangle,
    label: 'Typosquatting Domains',
    description: 'Domains with subtle misspellings',
  },
  {
    icon: UserX,
    label: 'Fake Social Profiles',
    description: 'Fraudulent social media accounts',
  },
];

const ImpersonationTacticsPanel = () => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Impersonation Tactics</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {tactics.map(t => (
            <div
              key={t.label}
              className="flex items-center gap-3 rounded-lg p-2.5 transition-colors hover:bg-muted/70"
            >
              <div className="rounded-md bg-primary/10 p-1.5">
                <t.icon className="size-4 text-primary" />
              </div>
              <div>
                <p className="text-sm font-medium">{t.label}</p>
                <p className="text-xs text-muted-foreground">{t.description}</p>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default ImpersonationTacticsPanel;
