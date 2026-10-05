import { useState } from 'react';
import { safeExecCommand } from 'utils/Security';
import { cn } from 'utils/Helper';
import type { AIPromptSubmitPayload, HtmlEditorProps } from './types';
import { useIframeEditor } from './useIframeEditor';
import EditorTabBar from './EditorTabBar';
import EditorToolbar from './EditorToolbar';
import EditorContentArea from './EditorContentArea';
import EditorStatusBar from './EditorStatusBar';
import ImageEditDialog from './ImageEditDialog';
import FloatingElementToolbar from './FloatingElementToolbar';
import AIPromptModal from './AIPromptModal';

export type { HtmlEditorProps } from './types';

export const HtmlEditor = ({
  value,
  onChange,
  readOnly = false,
  error,
  onSave,
  iframeTitle = 'HTML Editor',
  onAIGenerate,
  features,
  renderCaptureFormModal,
}: HtmlEditorProps) => {
  const [formGeneratorOpen, setFormGeneratorOpen] = useState(false);
  const captureFormEnabled = features?.captureForm ?? false;

  const {
    activeTab,
    isFullscreen,
    codeValue,
    imageDialog,
    iframeRef,
    floatingToolbar,
    aiPromptOpen,
    aiLoading,

    setIsFullscreen,
    setAiLoading,
    handleTabChange,
    handleCodeChange,
    exec,
    saveSelection,
    restoreSelection,
    handleInsertLink,
    handleInsertImage,
    handleImageSave,
    updateImageDialog,
    closeImageDialog,

    deselectElement,
    openAIPrompt,
    closeAIPrompt,
    getSelectedElementContext,
    applyAIContent,
  } = useIframeEditor({ value, onChange, readOnly, onSave });

  const handleAISubmit = async ({
    prompt,
    providerType,
    model,
  }: AIPromptSubmitPayload) => {
    if (!onAIGenerate) return;
    const context = getSelectedElementContext();
    if (!context) return;

    setAiLoading(true);
    try {
      const result = await onAIGenerate({
        providerType,
        model,
        prompt,
        elementHtml: context.elementHtml,
        templateCode: codeValue,
      });
      if (result) {
        applyAIContent(result);
      }
      closeAIPrompt();
    } catch {
      setAiLoading(false);
    }
  };

  const handleInsertCaptureForm = (html: string) => {
    if (activeTab === 'code') {
      handleCodeChange(codeValue + '\n' + html);
    } else {
      const doc = iframeRef.current?.contentDocument;
      if (doc) {
        safeExecCommand(doc, 'insertHTML', html);
        setTimeout(() => {
          const newHtml = doc.doctype
            ? `<!DOCTYPE ${doc.doctype.name}>\n` + doc.documentElement.outerHTML
            : doc.documentElement.outerHTML;
          onChange(newHtml);
        }, 50);
      } else {
        onChange(value + '\n' + html);
      }
    }
  };

  const aiContext = aiPromptOpen ? getSelectedElementContext() : null;

  const containerClass = isFullscreen
    ? 'home-fixed home-inset-0 home-z-50 home-flex home-flex-col'
    : 'home-flex home-flex-col home-overflow-hidden home-rounded-lg home-border home-border-card-border';

  return (
    <div className={cn(containerClass)}>
      <EditorTabBar
        activeTab={activeTab}
        onTabChange={handleTabChange}
        isFullscreen={isFullscreen}
        onToggleFullscreen={() => setIsFullscreen(f => !f)}
      />

      {activeTab === 'visual' && !readOnly && (
        <EditorToolbar
          exec={exec}
          saveSelection={saveSelection}
          restoreSelection={restoreSelection}
          onInsertLink={handleInsertLink}
          onInsertImage={handleInsertImage}
          onInsertForm={
            captureFormEnabled ? () => setFormGeneratorOpen(true) : undefined
          }
        />
      )}

      <EditorContentArea
        activeTab={activeTab}
        iframeRef={iframeRef}
        codeValue={codeValue}
        onCodeChange={handleCodeChange}
        readOnly={readOnly}
        isFullscreen={isFullscreen}
        hasContent={!!value}
        iframeTitle={iframeTitle}
      />

      <EditorStatusBar
        activeTab={activeTab}
        charCount={value.length}
        lineCount={value.split('\n').length}
      />

      {error && (
        <div className="home-border-t home-border-red-300 home-bg-red-50 home-px-4 home-py-2">
          <p className="home-text-sm home-text-red-600">{error}</p>
        </div>
      )}

      {imageDialog && (
        <ImageEditDialog
          state={imageDialog}
          onUpdate={updateImageDialog}
          onSave={handleImageSave}
          onClose={closeImageDialog}
        />
      )}

      {floatingToolbar && activeTab === 'visual' && !readOnly && (
        <FloatingElementToolbar
          position={floatingToolbar}
          onAIGenerate={onAIGenerate ? openAIPrompt : undefined}
          onDeselect={deselectElement}
        />
      )}

      {onAIGenerate && (
        <AIPromptModal
          isOpen={aiPromptOpen}
          elementTag={aiContext?.elementTag ?? ''}
          elementHtml={aiContext?.elementHtml ?? ''}
          isLoading={aiLoading}
          onSubmit={handleAISubmit}
          onClose={closeAIPrompt}
        />
      )}

      {captureFormEnabled &&
        renderCaptureFormModal?.({
          isOpen: formGeneratorOpen,
          onInsert: handleInsertCaptureForm,
          onClose: () => setFormGeneratorOpen(false),
        })}
    </div>
  );
};

export default HtmlEditor;
