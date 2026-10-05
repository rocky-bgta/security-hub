import { Camera, Check, Loader2, Plus, RotateCcw, Upload } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'components/common/Button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from 'components/common/Dialog';
import FileUploader, {
  type FileUploaderHandle,
} from 'components/common/FileUploader';
import { useDeepfake } from 'hooks/UseDeepfake';
import type { IDeepfakeImage } from 'models/Deepfake';
import { cn } from 'utils/Helper';

interface IFaceCaptureProps {
  face: string | null;
  setFace: (value: string | null) => void;
  confirmed: boolean;
  selectedFaceImageId?: string;
  onRetake: () => void;
  onCaptured: () => void;
  onSelectExisting: (image: IDeepfakeImage) => void;
}

const FaceCapture = ({
  face,
  setFace,
  confirmed,
  selectedFaceImageId,
  onRetake,
  onCaptured,
  onSelectExisting,
}: IFaceCaptureProps) => {
  const uploaderRef = useRef<FileUploaderHandle>(null);
  const videoRef = useRef<HTMLVideoElement>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const [capturing, setCapturing] = useState(false);
  const [count, setCount] = useState(5);
  const [camError, setCamError] = useState<string | null>(null);
  const [addFaceOpen, setAddFaceOpen] = useState(false);
  const [savedFaces, setSavedFaces] = useState<IDeepfakeImage[]>([]);
  const [facesLoading, setFacesLoading] = useState(true);
  const { fetchImages } = useDeepfake();

  const stopStream = () => {
    streamRef.current?.getTracks().forEach(track => track.stop());
    streamRef.current = null;
  };

  useEffect(() => () => stopStream(), []);

  useEffect(() => {
    let active = true;
    setFacesLoading(true);
    void fetchImages({
      imageType: 'FACE_CAPTURE',
      isActive: true,
      offset: 0,
      pageSize: 100,
    })
      .then(result => {
        if (active) setSavedFaces(result.items);
      })
      .finally(() => {
        if (active) setFacesLoading(false);
      });
    return () => {
      active = false;
    };
  }, [fetchImages]);

  const startCamera = async () => {
    setCamError(null);
    setCount(5);
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: 640, height: 480 },
        audio: false,
      });
      streamRef.current = stream;
      setCapturing(true);
      requestAnimationFrame(() => {
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
          videoRef.current.play().catch(() => {});
        }
      });
    } catch (error) {
      setCamError(
        error instanceof Error
          ? error.message
          : 'Unable to access webcam. Check browser permissions.',
      );
      toast.error('Could not access webcam');
    }
  };

  useEffect(() => {
    if (!capturing || count === 0) return;

    const timeout = setTimeout(() => {
      if (count === 1) {
        const video = videoRef.current;
        if (video && video.videoWidth) {
          const canvas = document.createElement('canvas');
          canvas.width = video.videoWidth;
          canvas.height = video.videoHeight;
          const context = canvas.getContext('2d');
          if (context) {
            context.translate(canvas.width, 0);
            context.scale(-1, 1);
            context.drawImage(video, 0, 0, canvas.width, canvas.height);
            setFace(canvas.toDataURL('image/jpeg', 0.9));
          }
        }
        stopStream();
        setCapturing(false);
        setCount(5);
        onCaptured();
        setAddFaceOpen(false);
        return;
      }

      setCount(value => value - 1);
    }, 1000);

    return () => clearTimeout(timeout);
  }, [capturing, count, onCaptured, setFace]);

  const cancelCapture = () => {
    stopStream();
    setCapturing(false);
    setCount(5);
  };

  const onUpload = (files: FileList) => {
    const file = files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = event => {
      setFace(event.target?.result as string);
      onCaptured();
      setAddFaceOpen(false);
      uploaderRef.current?.clearFiles();
    };
    reader.readAsDataURL(file);
  };

  const closeAddFaceModal = (open: boolean) => {
    if (!open) cancelCapture();
    setAddFaceOpen(open);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between gap-3">
        <div>
          <h3 className="text-sm font-medium">Saved face captures</h3>
          <p className="text-xs text-muted-foreground">
            Select an existing face or add a new one.
          </p>
        </div>
        <Button type="button" onClick={() => setAddFaceOpen(true)}>
          <Plus className="mr-1.5 size-4" /> Add New Face
        </Button>
      </div>

      {facesLoading ? (
        <div className="flex items-center justify-center gap-2 rounded-xl border border-card-border p-10 text-sm text-muted-foreground">
          <Loader2 className="size-4 animate-spin" />
          Loading face captures…
        </div>
      ) : savedFaces.length === 0 ? (
        <div className="rounded-xl border border-dashed border-card-border p-10 text-center text-sm text-muted-foreground">
          No saved face captures yet. Click Add New Face to create one.
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
          {savedFaces.map(item => {
            const active = selectedFaceImageId === item.id;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => onSelectExisting(item)}
                className={cn(
                  'group relative overflow-hidden rounded-xl border-2 text-left transition',
                  active
                    ? 'border-primary ring-1 ring-primary'
                    : 'border-card-border hover:border-primary/50',
                )}
              >
                <img
                  src={item.url}
                  alt={item.fileName || 'Saved face'}
                  className="aspect-square w-full object-contain"
                />
                <span className="block truncate px-2 py-1.5 text-xs">
                  {item.fileName}
                </span>
                {active && (
                  <span className="absolute right-2 top-2 grid size-6 place-items-center rounded-full bg-primary text-primary-foreground">
                    <Check className="size-3.5" />
                  </span>
                )}
              </button>
            );
          })}
        </div>
      )}

      {face && (
        <div className="flex items-center gap-4 rounded-xl border border-primary/30 bg-primary/5 p-3">
          <img
            src={face}
            alt="Selected face"
            className="size-20 rounded-lg object-cover"
          />
          <div className="min-w-0 flex-1">
            <p className="text-sm font-medium">Face selected</p>
            <p className="text-xs text-muted-foreground">
              {confirmed
                ? 'Confirmed and ready to use.'
                : 'Review the preview and continue to confirm.'}
            </p>
          </div>
          <Button variant="outline" size="sm" onClick={onRetake}>
            <RotateCcw className="mr-1.5 size-4" /> Clear
          </Button>
        </div>
      )}

      <Dialog open={addFaceOpen} onOpenChange={closeAddFaceModal}>
        <DialogContent className="max-w-3xl">
          <DialogHeader>
            <DialogTitle>Add New Face</DialogTitle>
            <DialogDescription>
              Upload a front-facing photo or capture one with your webcam.
            </DialogDescription>
          </DialogHeader>

          {capturing ? (
            <div className="space-y-3">
              <div className="relative mx-auto aspect-video w-full max-w-2xl overflow-hidden rounded-xl border-2 border-muted/60 bg-black">
                <video
                  ref={videoRef}
                  autoPlay
                  playsInline
                  muted
                  className="size-full object-cover"
                  style={{ transform: 'scaleX(-1)' }}
                />
                <div className="absolute left-3 top-3 flex items-center gap-1.5 rounded-full bg-destructive/90 px-2 py-1 text-[10px] font-semibold uppercase tracking-wider text-destructive-foreground">
                  <span className="size-1.5 animate-pulse rounded-full bg-white" />
                  Live
                </div>
                <div className="absolute inset-0 grid place-items-center">
                  <div className="grid size-24 place-items-center rounded-full bg-background/70 backdrop-blur">
                    <span className="text-5xl font-bold">{count}</span>
                  </div>
                </div>
                <div className="absolute inset-x-0 bottom-3 text-center text-xs text-white/80">
                  Hold still — capturing in {count}s…
                </div>
              </div>
              <div className="flex justify-center">
                <Button variant="outline" size="sm" onClick={cancelCapture}>
                  Cancel
                </Button>
              </div>
            </div>
          ) : (
            <div className="grid gap-4 md:grid-cols-2">
              <FileUploader
                ref={uploaderRef}
                id="deepfake-face-upload"
                accept="image/png,image/jpeg,image/jpg"
                maxSize={1}
                multiple={false}
                containerClassName="group flex flex-col items-center justify-center gap-3 rounded-xl border-2 border-dashed border-card-border p-8 transition-all hover:border-primary/60 hover:bg-primary/5"
                onUpload={onUpload}
              >
                <div className="grid size-12 place-items-center rounded-full bg-primary/10 text-primary group-hover:bg-primary/20">
                  <Upload className="size-5" />
                </div>
                <div className="text-center">
                  <div className="font-medium">Upload photo</div>
                  <div className="text-xs text-muted-foreground">
                    PNG or JPG, max 1MB, front facing
                  </div>
                </div>
              </FileUploader>

              <button
                type="button"
                onClick={startCamera}
                className="group relative flex flex-col items-center justify-center gap-3 overflow-hidden rounded-xl border-2 border-dashed border-card-border p-8 transition-all hover:border-[#9d68ff]/60 hover:bg-[#9d68ff]/5"
              >
                <div className="grid size-12 place-items-center rounded-full bg-[#9d68ff]/10 text-[#9d68ff] group-hover:bg-[#9d68ff]/20">
                  <Camera className="size-5" />
                </div>
                <div className="text-center">
                  <div className="font-medium">Capture live</div>
                  <div className="text-xs text-muted-foreground">
                    5-second selfie via webcam
                  </div>
                </div>
              </button>
            </div>
          )}

          {camError && (
            <p className="text-center text-xs text-destructive">{camError}</p>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default FaceCapture;
