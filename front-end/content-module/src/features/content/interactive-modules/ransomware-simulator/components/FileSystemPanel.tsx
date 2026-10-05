import { IFileRuntimeState } from '../types';
import { cn } from 'utils/Helper';

import SimulatorPanel from './SimulatorPanel';

interface IProps {
  files: Array<IFileRuntimeState>;
  fileStatusText: string;
  onPreview: (fileName: string) => void;
}

const statusLabel: Record<IFileRuntimeState['status'], string> = {
  safe: 'Safe',
  encrypted: 'Encrypted',
  decrypting: 'Decrypting...',
  decrypted: 'Decrypted',
};

const FileSystemPanel = ({ files, fileStatusText, onPreview }: IProps) => {
  return (
    <SimulatorPanel
      title="File System"
      headerRight={
        <span className="content-shrink-0 content-text-xs content-text-muted-foreground">
          {fileStatusText}
        </span>
      }
    >
      <ul className="content-space-y-1.5">
        {files.map(file => (
          <li key={file.name}>
            <button
              type="button"
              onClick={() => onPreview(file.name)}
              className={cn(
                'content-flex content-w-full content-items-center content-gap-2 content-rounded-md content-border content-px-2.5 content-py-1.5 content-text-left content-transition-colors',
                file.status === 'encrypted' &&
                  'content-border-red-500/40 content-bg-red-500/10',
                file.status === 'decrypted' &&
                  'content-border-emerald-500/40 content-bg-emerald-500/10',
                file.status === 'decrypting' &&
                  'content-border-amber-500/40 content-bg-amber-500/10',
                file.status === 'safe' &&
                  'content-border-white/10 content-bg-white/5 hover:content-bg-white/10',
              )}
            >
              <span className="content-text-base" aria-hidden>
                {file.name.endsWith('.docx') && '📄'}
                {file.name.endsWith('.jpg') && '🖼️'}
                {file.name.endsWith('.xlsx') && '📊'}
                {file.name.endsWith('.pptx') && '📽️'}
                {file.name.endsWith('.db') && '🗄️'}
                {file.name.endsWith('.mp4') && '🎬'}
                {file.name.endsWith('.zip') && '📦'}
                {file.name.endsWith('.ini') && '⚙️'}
              </span>
              <span className="content-min-w-0 content-flex-1 content-truncate content-text-sm content-font-medium content-text-white">
                {file.displayName}
              </span>
              <span
                className={cn(
                  'content-shrink-0 content-text-xs content-font-medium',
                  file.status === 'encrypted' && 'content-text-red-400',
                  file.status === 'decrypted' && 'content-text-emerald-400',
                  file.status === 'decrypting' && 'content-text-amber-400',
                  file.status === 'safe' && 'content-text-muted-foreground',
                )}
              >
                {statusLabel[file.status]}
              </span>
            </button>
          </li>
        ))}
      </ul>
    </SimulatorPanel>
  );
};

export default FileSystemPanel;
