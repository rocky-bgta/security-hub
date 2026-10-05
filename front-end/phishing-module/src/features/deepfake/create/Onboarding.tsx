import { useEffect, useState } from 'react';
import {
  Check,
  Image as ImageIcon,
  Languages,
  Loader2,
  Upload,
} from 'lucide-react';

import { Button } from 'components/common/Button';
import { Input } from 'components/common/Input';
import { Label } from 'components/common/Label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from 'components/common/Select';
import BackgroundUploadDialog from 'features/deepfake/create/BackgroundUploadDialog';
import { cn } from 'utils/Helper';
import { type BackgroundOption, type IDeepfakeImage } from 'models/Deepfake';
import { useDeepfake } from 'hooks/UseDeepfake';
import useDropDown from 'hooks/UseDropDown';

const DEFAULT_LANGUAGE_NAME = 'English';

interface ILanguageOption {
  id: string;
  displayName: string;
}

interface IOnboardingProps {
  name: string;
  setName: (s: string) => void;
  language: string;
  setLanguage: (s: string) => void;
  background: BackgroundOption;
  setBackground: (b: BackgroundOption) => void;
  onCustomBackgroundFile?: (file: File | null) => void;
}

const Onboarding = ({
  name,
  setName,
  language,
  setLanguage,
  background,
  setBackground,
  onCustomBackgroundFile,
}: IOnboardingProps) => {
  const [uploadOpen, setUploadOpen] = useState(false);
  const [savedBackgrounds, setSavedBackgrounds] = useState<IDeepfakeImage[]>(
    [],
  );
  const [backgroundsLoading, setBackgroundsLoading] = useState(true);
  const [languages, setLanguages] = useState<ILanguageOption[]>([]);
  const [languagesLoading, setLanguagesLoading] = useState(true);
  const { fetchImages } = useDeepfake();
  const { fetchLanguages } = useDropDown();

  useEffect(() => {
    let active = true;
    void fetchLanguages()
      .then(response => {
        if (!active) return;
        setLanguages(response.data ?? []);
      })
      .catch(error => {
        console.error('Error fetching active languages:', error);
        if (active) setLanguages([]);
      })
      .finally(() => {
        if (active) setLanguagesLoading(false);
      });
    return () => {
      active = false;
    };
  }, [fetchLanguages]);

  // Preselect a language once the list arrives, without touching a saved value.
  useEffect(() => {
    if (language || languages.length === 0) return;
    const preferred =
      languages.find(item => item.displayName === DEFAULT_LANGUAGE_NAME) ??
      languages[0];
    setLanguage(preferred.displayName);
  }, [language, languages, setLanguage]);

  useEffect(() => {
    let active = true;
    void fetchImages({
      imageType: 'BACKGROUND',
      isActive: true,
      offset: 0,
      pageSize: 100,
    })
      .then(result => {
        if (active) setSavedBackgrounds(result.items);
      })
      .finally(() => {
        if (active) setBackgroundsLoading(false);
      });
    return () => {
      active = false;
    };
  }, [fetchImages]);

  const selectBackground = (nextBackground: BackgroundOption) => {
    onCustomBackgroundFile?.(null);
    setBackground(nextBackground);
  };

  return (
    <div className="space-y-6">
      <div className="grid items-center gap-4 md:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor="name" className="flex">
            Video Title
          </Label>
          <Input
            id="name"
            placeholder="Q4 CEO impersonation drill"
            value={name}
            onChange={e => setName(e.target.value)}
            maxLength={80}
          />
        </div>
        <div className="space-y-2">
          <Label className="flex items-center gap-1.5">
            <Languages className="size-3.5" /> Voice language
          </Label>
          <Select
            value={language || undefined}
            onValueChange={setLanguage}
            disabled={languagesLoading || languages.length === 0}
          >
            <SelectTrigger>
              <SelectValue
                placeholder={
                  languagesLoading
                    ? 'Loading languages…'
                    : languages.length === 0
                      ? 'No languages available'
                      : 'Select a language'
                }
              />
            </SelectTrigger>
            <SelectContent>
              {languages.map(item => (
                <SelectItem key={item.id} value={item.displayName}>
                  {item.displayName}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <Label className="flex items-center gap-1.5">
            <ImageIcon className="size-3.5" /> Background
          </Label>
          <Button
            type="button"
            variant="outline"
            size="sm"
            className="h-auto gap-1.5 px-3 py-2 text-xs"
            onClick={() => setUploadOpen(true)}
          >
            <Upload className="size-3.5" />
            Upload image
          </Button>
        </div>
        <div className="space-y-2">
          {backgroundsLoading ? (
            <div className="flex items-center gap-2 rounded-lg p-4 text-xs text-muted-foreground">
              <Loader2 className="size-4 animate-spin" />
              Loading backgrounds…
            </div>
          ) : savedBackgrounds.length === 0 ? (
            <div className="rounded-lg border border-dashed border-card-border p-4 text-center text-xs text-muted-foreground">
              No saved backgrounds yet.
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
              {savedBackgrounds.map(item => {
                const active = background.id === item.id;
                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() =>
                      selectBackground({
                        id: item.id,
                        fileKey: item.fileKey,
                        name: item.fileName || 'Saved background',
                        url: item.url,
                        value: `center / cover no-repeat url(${item.url})`,
                      })
                    }
                    className={cn(
                      'group relative aspect-video overflow-hidden rounded-md border-2 transition-all',
                      active
                        ? 'border-primary'
                        : 'border-card-border hover:border-muted-foreground',
                    )}
                  >
                    <img
                      src={item.url}
                      alt={item.fileName || 'Saved background'}
                      className="size-full object-cover"
                    />
                    <span className="absolute inset-x-0 bottom-0 truncate bg-background/70 px-1.5 py-0.5 text-[10px] capitalize backdrop-blur">
                      {item.fileName
                        .split('.')[0]
                        .replace(/-/g, ' ')
                        .toLowerCase()}
                    </span>
                    {active && (
                      <span className="absolute right-1 top-1 grid size-5 place-items-center rounded-full bg-primary text-primary-foreground">
                        <Check className="size-3" />
                      </span>
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </div>
      </div>

      <BackgroundUploadDialog
        open={uploadOpen}
        onOpenChange={setUploadOpen}
        onUploaded={(nextBackground, file) => {
          onCustomBackgroundFile?.(file);
          setBackground(nextBackground);
        }}
      />
    </div>
  );
};

export default Onboarding;
