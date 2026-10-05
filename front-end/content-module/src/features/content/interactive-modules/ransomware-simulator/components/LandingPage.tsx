import { AlertTriangle, ShieldAlert } from 'lucide-react';

import { Button } from 'common/Button';

import SimulatorPanel from './SimulatorPanel';

interface IProps {
  onStart: () => void;
}

const INFECTION_METHODS = [
  {
    title: 'Phishing Emails',
    detail: 'Malicious attachments or links in deceptive messages',
  },
  {
    title: 'Malicious Downloads',
    detail: 'Compromised software or cracked applications',
  },
  {
    title: 'Exploit Kits',
    detail: 'Attacks targeting unpatched software vulnerabilities',
  },
  {
    title: 'Remote Desktop Protocol (RDP)',
    detail: 'Brute-force attacks on exposed remote access',
  },
  {
    title: 'Supply Chain Attacks',
    detail: 'Compromised software updates from trusted vendors',
  },
];

const LandingPage = ({ onStart }: IProps) => {
  return (
    <SimulatorPanel className="content-mx-auto content-max-w-3xl content-p-4 content-text-center">
      <div className="content-mb-3 content-flex content-items-start content-gap-2 content-rounded-md content-border content-border-red-500/40 content-bg-red-500/10 content-p-2.5 content-text-left">
        <AlertTriangle className="content-mt-0.5 content-size-4 content-shrink-0 content-text-red-400" />
        <p className="content-text-sm content-font-medium content-text-red-300">
          Educational simulation only. No real files on your device will be
          modified or encrypted.
        </p>
      </div>

      <div className="content-mb-3 content-flex content-justify-center">
        <div className="content-flex content-size-10 content-items-center content-justify-center content-rounded-full content-bg-primary/15">
          <ShieldAlert className="content-size-5 content-text-primary" />
        </div>
      </div>

      <p className="content-mb-4 content-text-sm content-leading-snug content-text-muted-foreground">
        Experience how ransomware infects a system, encrypts files, and demands
        payment — in a safe, guided environment designed for security awareness
        training.
      </p>

      <Button
        type="button"
        size="lg"
        onClick={onStart}
        data-walkthrough-id="landing-cta"
        className="content-mb-4 content-w-full sm:content-w-auto"
      >
        Simulate Ransomware Attack
      </Button>

      <div className="content-mb-3 content-text-left">
        <h3 className="content-mb-2 content-text-sm content-font-semibold content-text-white">
          Common infection methods
        </h3>
        <ul className="content-grid content-grid-cols-1 content-gap-2 sm:content-grid-cols-2">
          {INFECTION_METHODS.map(method => (
            <li
              key={method.title}
              className="content-rounded-md content-border content-border-white/10 content-bg-white/5 content-p-2.5"
            >
              <p className="content-text-sm content-font-medium content-text-white">
                {method.title}
              </p>
              <p className="content-mt-0.5 content-text-xs content-leading-snug content-text-muted-foreground">
                {method.detail}
              </p>
            </li>
          ))}
        </ul>
      </div>

      <p className="content-text-xs content-text-muted-foreground">
        Built for cybersecurity education. Always keep offline backups and never
        pay ransoms in real incidents.
      </p>
    </SimulatorPanel>
  );
};

export default LandingPage;
