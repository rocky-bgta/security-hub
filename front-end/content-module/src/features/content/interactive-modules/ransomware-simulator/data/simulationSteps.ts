import { ISimulationStep } from '../types';

export const SIMULATION_STEPS: Array<ISimulationStep> = [
  {
    id: 1,
    name: 'Initial Infection',
    emoji: '📥',
    description:
      'Ransomware often enters through phishing emails, malicious downloads, or exploited vulnerabilities. Always verify sources before opening attachments or clicking links.',
  },
  {
    id: 2,
    name: 'Social Engineering',
    emoji: '🎭',
    description:
      'Attackers use psychological manipulation to trick users into actions that compromise security. Be skeptical of urgent requests or offers that seem too good to be true.',
  },
  {
    id: 3,
    name: 'Privilege Escalation',
    emoji: '⬆️',
    description:
      'Malware attempts to gain higher permissions to access more system resources. Use the principle of least privilege for user accounts.',
  },
  {
    id: 4,
    name: 'Defense Evasion',
    emoji: '🛡️',
    description:
      'Ransomware tries to avoid detection by security software. Keep your security solutions updated and use multiple layers of protection.',
  },
  {
    id: 5,
    name: 'Command & Control',
    emoji: '📡',
    description:
      'The malware establishes communication with attacker servers. Network monitoring can help detect suspicious connections.',
  },
  {
    id: 6,
    name: 'Discovery',
    emoji: '🔍',
    description:
      'The ransomware scans the network to find valuable targets and map the environment. Network segmentation can limit the spread.',
  },
  {
    id: 7,
    name: 'Encryption',
    emoji: '🔐',
    description:
      'Files are encrypted using strong algorithms. Regular backups are essential to recover from ransomware attacks without paying ransoms.',
  },
];

export const DISK_STATUS_BY_STEP: Array<string> = [
  'Initial infection in progress...',
  'Social engineering attack in progress...',
  'Escalating privileges...',
  'Evading security measures...',
  'Establishing command and control...',
  'Discovering files and resources...',
  'Encrypting files...',
];
