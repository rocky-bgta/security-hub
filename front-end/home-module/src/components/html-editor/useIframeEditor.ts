import { useCallback, useEffect, useRef, useState } from 'react';
import { loadHtmlIntoDocument, safeExecCommand } from 'utils/Security';
import {
  type EditorTab,
  type FloatingToolbarPosition,
  type ImageDialogState,
  EDITOR_STYLE_ID,
  SELECTABLE_TAGS,
} from './types';

interface UseIframeEditorOptions {
  value: string;
  onChange: (value: string) => void;
  readOnly: boolean;
  onSave?: () => void;
}

const LP_HOVER_ATTR = 'data-lp-hover';
const LP_SELECTED_ATTR = 'data-lp-selected';

function findSelectableElement(
  el: HTMLElement,
  root: HTMLElement,
): HTMLElement | null {
  let current: HTMLElement | null = el;
  while (
    current &&
    current !== root &&
    current !== root.ownerDocument.documentElement
  ) {
    if (SELECTABLE_TAGS.has(current.tagName)) return current;
    current = current.parentElement;
  }
  return null;
}

export function useIframeEditor({
  value,
  onChange,
  readOnly,
  onSave,
}: UseIframeEditorOptions) {
  const [activeTab, setActiveTab] = useState<EditorTab>('visual');
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [codeValue, setCodeValue] = useState(value);
  const [imageDialog, setImageDialog] = useState<ImageDialogState | null>(null);

  const [prevValue, setPrevValue] = useState(value);
  const [iframeReloadKey, setIframeReloadKey] = useState(0);

  const [floatingToolbar, setFloatingToolbar] =
    useState<FloatingToolbarPosition | null>(null);
  const [aiPromptOpen, setAiPromptOpen] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);

  const iframeRef = useRef<HTMLIFrameElement>(null);
  const imageElementRef = useRef<HTMLImageElement | null>(null);
  const internalHtml = useRef(value);
  const isLoading = useRef(false);
  const savedRange = useRef<Range | null>(null);

  const selectedElementRef = useRef<HTMLElement | null>(null);
  const hoveredElementRef = useRef<HTMLElement | null>(null);

  const onChangeRef = useRef(onChange);
  const onSaveRef = useRef(onSave);
  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);
  useEffect(() => {
    onSaveRef.current = onSave;
  }, [onSave]);

  const saveSelection = useCallback(() => {
    const sel = iframeRef.current?.contentDocument?.getSelection();
    if (sel && sel.rangeCount > 0) {
      savedRange.current = sel.getRangeAt(0).cloneRange();
    }
  }, []);

  const restoreSelection = useCallback(() => {
    const doc = iframeRef.current?.contentDocument;
    if (!doc || !savedRange.current) return;
    const sel = doc.getSelection();
    if (sel) {
      sel.removeAllRanges();
      sel.addRange(savedRange.current);
    }
    iframeRef.current?.contentWindow?.focus();
  }, []);

  const updateToolbarPosition = useCallback(() => {
    const el = selectedElementRef.current;
    const iframe = iframeRef.current;
    if (!el || !iframe) {
      setFloatingToolbar(null);
      return;
    }

    try {
      const iframeRect = iframe.getBoundingClientRect();
      const elementRect = el.getBoundingClientRect();

      setFloatingToolbar({
        top: iframeRect.top + elementRect.top,
        left: iframeRect.left + elementRect.left,
        width: elementRect.width,
        elementTag: el.tagName.toLowerCase(),
      });
    } catch {
      setFloatingToolbar(null);
    }
  }, []);

  const selectElement = useCallback(
    (el: HTMLElement) => {
      if (selectedElementRef.current) {
        selectedElementRef.current.removeAttribute(LP_SELECTED_ATTR);
      }
      selectedElementRef.current = el;
      el.setAttribute(LP_SELECTED_ATTR, 'true');
      updateToolbarPosition();
    },
    [updateToolbarPosition],
  );

  const deselectElement = useCallback(() => {
    if (selectedElementRef.current) {
      selectedElementRef.current.removeAttribute(LP_SELECTED_ATTR);
    }
    selectedElementRef.current = null;
    setFloatingToolbar(null);
  }, []);

  const getIframeHtml = useCallback((): string => {
    const doc = iframeRef.current?.contentDocument;
    if (!doc?.documentElement) return internalHtml.current;

    const editorStyle = doc.getElementById(EDITOR_STYLE_ID);
    const styleParent = editorStyle?.parentNode;
    editorStyle?.remove();

    const dt = doc.doctype;
    const doctype = dt ? `<!DOCTYPE ${dt.name}>\n` : '';
    let html = doctype + doc.documentElement.outerHTML;

    if (editorStyle && styleParent) styleParent.appendChild(editorStyle);

    html = html
      .replace(/ data-lp-hover="true"/g, '')
      .replace(/ data-lp-selected="true"/g, '');

    return html;
  }, []);

  const loadIframe = useCallback(
    (html: string, editable: boolean) => {
      const iframe = iframeRef.current;
      if (!iframe) return;
      const doc = iframe.contentDocument;
      if (!doc) return;

      deselectElement();
      hoveredElementRef.current = null;
      isLoading.current = true;

      loadHtmlIntoDocument(doc, html);

      if (editable) {
        doc.designMode = 'on';

        const style = doc.createElement('style');
        style.id = EDITOR_STYLE_ID;
        style.textContent = `
          img:hover {
            outline: 3px solid #3b82f6 !important;
            outline-offset: 2px !important;
            cursor: pointer !important;
          }
          [${LP_HOVER_ATTR}="true"] {
            outline: 1px dashed rgba(99, 102, 241, 0.55) !important;
            outline-offset: 1px !important;
            cursor: pointer !important;
          }
          [${LP_SELECTED_ATTR}="true"] {
            outline: 2px solid #6366f1 !important;
            outline-offset: 2px !important;
          }
          [${LP_SELECTED_ATTR}="true"][${LP_HOVER_ATTR}="true"] {
            outline: 2px solid #6366f1 !important;
          }
        `;
        doc.head?.appendChild(style);

        const body = doc.body;

        doc.addEventListener('mouseover', (e: MouseEvent) => {
          const target = e.target as HTMLElement;
          const selectable = findSelectableElement(target, body);
          if (selectable && selectable !== hoveredElementRef.current) {
            hoveredElementRef.current?.removeAttribute(LP_HOVER_ATTR);
            hoveredElementRef.current = selectable;
            selectable.setAttribute(LP_HOVER_ATTR, 'true');
          }
        });

        doc.addEventListener('mouseout', (e: MouseEvent) => {
          const related = e.relatedTarget as HTMLElement | null;
          if (!related || !doc.body.contains(related)) {
            hoveredElementRef.current?.removeAttribute(LP_HOVER_ATTR);
            hoveredElementRef.current = null;
          }
        });

        doc.addEventListener('click', (e: MouseEvent) => {
          const target = e.target as HTMLElement;

          if (target.tagName === 'IMG') {
            e.preventDefault();
            const img = target as HTMLImageElement;
            imageElementRef.current = img;
            setImageDialog({ src: img.src, alt: img.alt || '' });
          }

          const selectable = findSelectableElement(target, body);
          if (selectable) {
            selectElement(selectable);
          } else {
            deselectElement();
          }
        });

        doc.addEventListener('input', () => {
          if (isLoading.current) return;
          const newHtml = getIframeHtml();
          internalHtml.current = newHtml;
          setPrevValue(newHtml);
          onChangeRef.current(newHtml);
          updateToolbarPosition();
        });

        doc.addEventListener('keydown', (e: KeyboardEvent) => {
          if ((e.ctrlKey || e.metaKey) && e.key === 's') {
            e.preventDefault();
            const newHtml = getIframeHtml();
            internalHtml.current = newHtml;
            setPrevValue(newHtml);
            onChangeRef.current(newHtml);
            onSaveRef.current?.();
          }
          if (e.key === 'Escape') {
            deselectElement();
          }
        });

        doc.addEventListener('scroll', updateToolbarPosition);
      } else {
        doc.designMode = 'off';
      }

      requestAnimationFrame(() => {
        isLoading.current = false;
      });
    },
    [getIframeHtml, selectElement, deselectElement, updateToolbarPosition],
  );

  const exec = useCallback(
    (command: string, val?: string) => {
      const doc = iframeRef.current?.contentDocument;
      if (!doc) return;
      safeExecCommand(doc, command, val);
      setTimeout(() => {
        const newHtml = getIframeHtml();
        internalHtml.current = newHtml;
        setPrevValue(newHtml);
        onChangeRef.current(newHtml);
      }, 50);
    },
    [getIframeHtml],
  );

  if (value !== prevValue) {
    setPrevValue(value);
    setCodeValue(value);
    setIframeReloadKey(k => k + 1);
  }

  useEffect(() => {
    internalHtml.current = value;
  }, [value]);

  useEffect(() => {
    if (activeTab !== 'visual' && activeTab !== 'preview') return;
    const timer = setTimeout(() => {
      loadIframe(internalHtml.current, activeTab === 'visual' && !readOnly);
    }, 50);
    return () => clearTimeout(timer);
  }, [activeTab, iframeReloadKey, loadIframe, readOnly]);

  useEffect(() => {
    if (!floatingToolbar) return;
    window.addEventListener('scroll', updateToolbarPosition, true);
    window.addEventListener('resize', updateToolbarPosition);
    return () => {
      window.removeEventListener('scroll', updateToolbarPosition, true);
      window.removeEventListener('resize', updateToolbarPosition);
    };
  }, [floatingToolbar, updateToolbarPosition]);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 's') {
        e.preventDefault();
        onSaveRef.current?.();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const handleTabChange = (tab: EditorTab) => {
    deselectElement();
    if (activeTab === 'visual') {
      const html = getIframeHtml();
      internalHtml.current = html;
      setCodeValue(html);
      setPrevValue(html);
      onChange(html);
    } else if (activeTab === 'code') {
      internalHtml.current = codeValue;
      setPrevValue(codeValue);
      onChange(codeValue);
    }
    setActiveTab(tab);
  };

  const handleCodeChange = (code: string) => {
    setCodeValue(code);
    internalHtml.current = code;
    setPrevValue(code);
    onChange(code);
  };

  const handleImageSave = () => {
    const el = imageElementRef.current;
    if (!imageDialog || !el) return;
    el.src = imageDialog.src;
    el.alt = imageDialog.alt;
    const html = getIframeHtml();
    internalHtml.current = html;
    setPrevValue(html);
    onChange(html);
    setImageDialog(null);
    imageElementRef.current = null;
  };

  const handleInsertLink = () => {
    const doc = iframeRef.current?.contentDocument;
    if (!doc) return;
    const sel = doc.getSelection();
    const anchor = sel?.anchorNode?.parentElement?.closest('a');
    const url = prompt('Enter URL:', anchor?.href || 'https://');
    if (url) exec('createLink', url);
  };

  const handleInsertImage = () => {
    const url = prompt('Enter image URL:', 'https://');
    if (url) exec('insertImage', url);
  };

  const updateImageDialog = (updates: Partial<ImageDialogState>) => {
    setImageDialog(prev => (prev ? { ...prev, ...updates } : null));
  };

  const closeImageDialog = () => setImageDialog(null);

  const openAIPrompt = useCallback(() => {
    if (!selectedElementRef.current) return;
    setAiPromptOpen(true);
  }, []);

  const closeAIPrompt = useCallback(() => {
    setAiPromptOpen(false);
    setAiLoading(false);
  }, []);

  const getSelectedElementContext = useCallback(() => {
    const el = selectedElementRef.current;
    if (!el) return null;
    return {
      elementHtml: el.innerHTML,
      elementTag: el.tagName.toLowerCase(),
    };
  }, []);

  const applyAIContent = useCallback(
    (fullHtml: string) => {
      deselectElement();
      internalHtml.current = fullHtml;
      setCodeValue(fullHtml);
      setPrevValue(fullHtml);
      onChangeRef.current(fullHtml);
      setIframeReloadKey(k => k + 1);
    },
    [deselectElement],
  );

  return {
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
  };
}
