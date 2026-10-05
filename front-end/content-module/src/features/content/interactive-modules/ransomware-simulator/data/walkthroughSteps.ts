import { IWalkthroughStep } from '../types';

export const WALKTHROUGH_STEPS: Array<IWalkthroughStep> = [
  {
    id: 'landing',
    targetId: 'landing-cta',
    title: 'Begin the simulation',
    blurb:
      'Click Simulate Ransomware Attack. This is a safe demo — nothing on your device is changed.',
    whatToDo:
      'Start the educational ransomware simulator. Nothing on your real device will be changed.',
    whereToClick: 'Click the “Simulate Ransomware Attack” button.',
    whyItMatters:
      'You need to enter the workspace before you can watch how an attack unfolds.',
    expectedOutcome: 'The simulation dashboard opens with settings and controls.',
  },
  {
    id: 'family',
    targetId: 'family-select',
    title: 'Choose a ransomware family',
    blurb:
      'Pick a family (WannaCry is a good start), then click Next. This only changes the fake file extension.',
    whatToDo:
      'Pick a ransomware family (WannaCry is a good starter). This only affects the fake file extension used in the demo.',
    whereToClick: 'Use the Ransomware Family dropdown, then click Next when ready.',
    whyItMatters:
      'Different families use different extensions and tactics — useful for recognizing attacks in the wild.',
    expectedOutcome: 'Your selected family is ready for the attack sequence.',
    allowSkipAction: true,
  },
  {
    id: 'algorithm',
    targetId: 'algorithm-select',
    title: 'Choose an encryption algorithm',
    blurb:
      'Pick AES-256 (or another algorithm), then click Next. This is cosmetic in the demo — it only changes the educational text.',
    whatToDo:
      'Select the encryption algorithm used in the educational copy during encryption.',
    whereToClick:
      'Use the Encryption Algorithm dropdown, then click Next when ready.',
    whyItMatters:
      'Real ransomware uses strong encryption. In this demo the choice only updates the educational text.',
    expectedOutcome: 'The selected algorithm appears in later educational messages.',
    allowSkipAction: true,
  },
  {
    id: 'speed',
    targetId: 'speed-select',
    title: 'Set simulation speed',
    blurb:
      'Choose Normal speed, then click Next. Speed only paces the attack stages.',
    whatToDo:
      'Choose how quickly the attack stages advance. Normal is recommended for first-time users.',
    whereToClick: 'Use the Simulation Speed dropdown, then click Next.',
    whyItMatters:
      'Speed only paces the attack-flow steps so you can follow the educational text.',
    expectedOutcome: 'The attack will run at your chosen pace.',
    allowSkipAction: true,
  },
  {
    id: 'start',
    targetId: 'start-btn',
    title: 'Start the attack sequence',
    blurb: 'Click Start to begin the seven-stage attack.',
    whatToDo: 'Begin the seven-stage ransomware lifecycle simulation.',
    whereToClick: 'Click the green Start button in Simulation Control.',
    whyItMatters:
      'This starts the infection → escalation → encryption journey you need to understand.',
    expectedOutcome: 'Attack Flow steps light up one by one with educational tips.',
  },
  {
    id: 'observe-flow',
    targetId: 'attack-flow',
    title: 'Watch the attack stages',
    blurb:
      'Watch each highlighted stage. After the last one, encryption starts automatically.',
    whatToDo:
      'Follow each highlighted stage and read the educational information as it updates.',
    whereToClick:
      'No click needed — observe the Attack Flow. You can pause or reset if you want.',
    whyItMatters:
      'Real ransomware follows similar stages. Knowing them helps you spot warning signs earlier.',
    expectedOutcome: 'After the last stage, file encryption begins automatically.',
    autoAdvanceOnPhase: 'encrypting',
    allowSkipAction: true,
  },
  {
    id: 'observe-encryption',
    targetId: 'encryption-dashboard',
    title: 'See files get encrypted',
    blurb: 'Watch the disk bar and files lock. This is why backups matter.',
    whatToDo:
      'Watch the disk progress bar and file list as fake files are renamed and locked.',
    whereToClick: 'No click needed — observe encryption progress.',
    whyItMatters:
      'Once files are encrypted, recovery without backups or a key is extremely difficult — which is why backups matter.',
    expectedOutcome: 'Encryption reaches 100% and a ransom note appears.',
    autoAdvanceOnPhase: 'completed',
    allowSkipAction: true,
  },
  {
    id: 'open-decrypt',
    targetId: 'decrypt-cta',
    title: 'Open the recovery demo',
    blurb:
      'Click Decrypt Files Now. In real life, restore from backups — don’t pay.',
    whatToDo:
      'Open the decryption flow from the ransom note. In real life you should not pay — restore from backups instead.',
    whereToClick:
      'Click “Decrypt Files Now” on the ransom note (or Show Ransom Note first if it was minimized).',
    whyItMatters:
      'This demo shows how attackers pressure victims — and why paying is discouraged.',
    expectedOutcome: 'The decryption panel opens and shows a demo decryption key.',
  },
  {
    id: 'decrypt',
    targetId: 'decrypt-form',
    title: 'Recover the files (demo)',
    blurb: 'Copy the shown key, paste it, then click Decrypt Files.',
    whatToDo:
      'Copy the shown key, paste it into the input, then click Decrypt Files.',
    whereToClick:
      'Use Copy, paste into the decryption key field, then click Decrypt Files.',
    whyItMatters:
      'Only the correct key unlocks these files. In reality you rarely get a reliable key — backups are the real recovery path.',
    expectedOutcome: 'Files restore to their original names and the guide completes.',
    autoAdvanceOnPhase: 'recovered',
  },
];
