import { safeWindowOpen } from 'home-module/security';
import { Button } from 'common/Button';
import {
  Download,
  ExternalLink,
  RotateCcw,
  ZoomIn,
  ZoomOut,
} from 'lucide-react';
import { useCallback, useEffect, useRef, useState } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import { cn } from 'utils/Helper';

import 'react-pdf/dist/Page/AnnotationLayer.css';
import 'react-pdf/dist/Page/TextLayer.css';

pdfjs.GlobalWorkerOptions.workerSrc = `https://unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;

const MIN_ZOOM = 0.75;
const MAX_ZOOM = 2;
const ZOOM_STEP = 0.25;

interface IPdfViewerProps {
  src: string;
  title?: string;
  className?: string;
  showToolbar?: boolean;
}

const PdfViewer = ({
  src,
  title: _title = 'PDF document',
  className,
  showToolbar = true,
}: IPdfViewerProps) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [zoom, setZoom] = useState(1);
  const [numPages, setNumPages] = useState(0);
  const [containerWidth, setContainerWidth] = useState(0);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    const updateWidth = () => {
      setContainerWidth(container.clientWidth);
    };

    updateWidth();

    const observer = new ResizeObserver(updateWidth);
    observer.observe(container);

    return () => observer.disconnect();
  }, []);

  const handleZoomIn = () => {
    setZoom(prev => Math.min(prev + ZOOM_STEP, MAX_ZOOM));
  };

  const handleZoomOut = () => {
    setZoom(prev => Math.max(prev - ZOOM_STEP, MIN_ZOOM));
  };

  const handleZoomReset = () => {
    setZoom(1);
  };

  const handleOpenInNewTab = useCallback(() => {
    safeWindowOpen(src, '_blank');
  }, [src]);

  const handleDownload = useCallback(() => {
    safeWindowOpen(src, '_blank');
  }, [src]);

  const handleLoadSuccess = ({ numPages: pages }: { numPages: number }) => {
    setNumPages(pages);
    setHasError(false);
  };

  const handleLoadError = () => {
    setHasError(true);
  };

  const loadingSpinner = (
    <div className="flex min-h-[50vh] items-center justify-center">
      <div className="size-8 animate-spin rounded-full border-2 border-slate-600 border-t-sky-400" />
    </div>
  );

  const pageWidth = containerWidth > 0 ? containerWidth * zoom : undefined;

  return (
    <div className={cn('flex flex-col gap-3', className)}>
      {showToolbar && (
        <div className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-slate-700/50 bg-slate-800/40 px-3 py-2">
          <div className="flex flex-wrap items-center gap-1">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleZoomOut}
              disabled={zoom <= MIN_ZOOM}
              className="h-8 border-slate-600/60 bg-slate-800/60 px-2 text-slate-200 hover:bg-slate-700/60 hover:text-white"
              title="Zoom out"
            >
              <ZoomOut className="size-4" />
            </Button>
            <span className="min-w-14 text-center text-xs font-medium text-slate-300">
              {Math.round(zoom * 100)}%
            </span>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleZoomIn}
              disabled={zoom >= MAX_ZOOM}
              className="h-8 border-slate-600/60 bg-slate-800/60 px-2 text-slate-200 hover:bg-slate-700/60 hover:text-white"
              title="Zoom in"
            >
              <ZoomIn className="size-4" />
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleZoomReset}
              disabled={zoom === 1}
              className="h-8 border-slate-600/60 bg-slate-800/60 px-2 text-slate-200 hover:bg-slate-700/60 hover:text-white"
              title="Reset zoom"
            >
              <RotateCcw className="size-4" />
            </Button>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleOpenInNewTab}
              className="h-8 gap-1.5 border-slate-600/60 bg-slate-800/60 text-xs text-slate-200 hover:bg-slate-700/60 hover:text-white"
            >
              <ExternalLink className="size-3.5" />
              Open
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleDownload}
              className="h-8 gap-1.5 border-slate-600/60 bg-slate-800/60 text-xs text-slate-200 hover:bg-slate-700/60 hover:text-white"
            >
              <Download className="size-3.5" />
              Download
            </Button>
          </div>
        </div>
      )}

      <div
        ref={containerRef}
        className="relative max-h-[70vh] min-h-[50vh] w-full overflow-y-auto rounded-xl border border-slate-700/50 bg-slate-950/40"
      >
        {hasError ? (
          <div className="flex min-h-[50vh] flex-col items-center justify-center gap-3 p-6 text-center">
            <p className="text-sm text-slate-400">
              Unable to display this PDF inline. You can open it in a new tab
              instead.
            </p>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleOpenInNewTab}
              className="gap-2 border-slate-600/60 bg-slate-800/60 text-slate-200 hover:bg-slate-700/60 hover:text-white"
            >
              <ExternalLink className="size-4" />
              Open in new tab
            </Button>
          </div>
        ) : (
          <Document
            key={src}
            file={src}
            onLoadSuccess={handleLoadSuccess}
            onLoadError={handleLoadError}
            loading={loadingSpinner}
            className="flex flex-col items-center gap-4 p-4"
          >
            {Array.from({ length: numPages }, (_, index) => (
              <Page
                key={`page-${index + 1}`}
                pageNumber={index + 1}
                width={pageWidth}
                renderTextLayer
                renderAnnotationLayer
                className="shadow-md"
              />
            ))}
          </Document>
        )}
      </div>
    </div>
  );
};

export default PdfViewer;
