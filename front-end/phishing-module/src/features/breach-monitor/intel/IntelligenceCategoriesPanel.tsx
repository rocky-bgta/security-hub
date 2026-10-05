import { Card, CardContent, CardHeader, CardTitle } from 'common/Card';
import { Key, Bug, Database, Skull } from 'lucide-react';

const categories = [
  {
    icon: Key,
    label: 'Credential Leaks',
    description: 'Exposed usernames, passwords, and API keys',
  },
  {
    icon: Bug,
    label: 'Ransomware Activity',
    description: 'Ransomware discussions and attack planning',
  },
  {
    icon: Database,
    label: 'Data Leaks',
    description: 'Leaked databases and sensitive documents',
  },
  {
    icon: Skull,
    label: 'Malware Discussions',
    description: 'Malware distribution and exploit kits',
  },
];

const IntelligenceCategoriesPanel = () => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-xl">Intelligence Categories</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {categories.map(c => (
            <div
              key={c.label}
              className="flex items-center gap-3 rounded-lg p-2.5 transition-colors hover:bg-muted/70"
            >
              <div className="rounded-md bg-primary/10 p-1.5">
                <c.icon className="size-4 text-primary" />
              </div>
              <div>
                <p className="text-sm font-medium">{c.label}</p>
                <p className="text-xs text-muted-foreground">{c.description}</p>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default IntelligenceCategoriesPanel;
