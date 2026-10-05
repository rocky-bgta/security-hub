import { Textarea } from 'components/common/Textarea';

interface IScriptReadingProps {
  script: string;
  setScript: (value: string) => void;
}

const ScriptReading = ({ script, setScript }: IScriptReadingProps) => {
  return (
    <div className="space-y-6">
      <Textarea
        rows={8}
        value={script}
        onChange={event => setScript(event.target.value)}
        className="font-mono text-sm leading-relaxed"
      />

      <div className="flex items-center justify-end">
        <div className="text-xs text-muted-foreground">
          {script.length} chars · ~
          {Math.ceil(script.split(/\s+/).filter(Boolean).length / 2.5)}s spoken
        </div>
      </div>
    </div>
  );
};

export default ScriptReading;
