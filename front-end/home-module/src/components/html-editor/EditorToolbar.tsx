import {
  AlignCenter,
  AlignJustify,
  AlignLeft,
  AlignRight,
  Bold,
  Eraser,
  FormInput,
  Image as ImageIcon,
  Italic,
  Link,
  List,
  ListOrdered,
  Minus,
  Redo2,
  Strikethrough,
  Underline,
  Undo2,
} from 'lucide-react';
import { FONT_SIZES, HEADING_OPTIONS } from './types';

const Divider = () => (
  <div className="home-mx-1 home-h-6 home-w-px home-bg-gray-600/50" />
);

const TBtn = ({
  onClick,
  title,
  children,
}: {
  onClick: () => void;
  title: string;
  children: React.ReactNode;
}) => (
  <button
    type="button"
    onClick={onClick}
    title={title}
    className="home-flex home-size-8 home-items-center home-justify-center home-rounded home-text-gray-300 home-transition-colors hover:home-bg-white/10 hover:home-text-white"
  >
    {children}
  </button>
);

interface EditorToolbarProps {
  exec: (command: string, val?: string) => void;
  saveSelection: () => void;
  restoreSelection: () => void;
  onInsertLink: () => void;
  onInsertImage: () => void;
  onInsertForm?: () => void;
}

const EditorToolbar = ({
  exec,
  saveSelection,
  restoreSelection,
  onInsertLink,
  onInsertImage,
  onInsertForm,
}: EditorToolbarProps) => (
  <div
    className="home-flex home-flex-wrap home-items-center home-gap-0.5 home-border-b home-border-gray-700/50 home-bg-[#252547] home-px-2 home-py-1.5"
    onMouseDown={e => {
      const tag = (e.target as HTMLElement).tagName;
      if (tag !== 'SELECT' && tag !== 'OPTION' && tag !== 'INPUT') {
        e.preventDefault();
      }
    }}
  >
    <TBtn onClick={() => exec('undo')} title="Undo (Ctrl+Z)">
      <Undo2 className="home-size-4" />
    </TBtn>
    <TBtn onClick={() => exec('redo')} title="Redo (Ctrl+Y)">
      <Redo2 className="home-size-4" />
    </TBtn>

    <Divider />

    <select
      onMouseDown={saveSelection}
      onChange={e => {
        restoreSelection();
        exec('formatBlock', e.target.value);
      }}
      defaultValue="p"
      className="home-h-8 home-rounded home-border home-border-gray-600 home-bg-[#1e1e3a] home-px-2 home-text-xs home-text-gray-300 focus:home-border-blue-500 focus:home-outline-none"
      title="Block format"
    >
      {HEADING_OPTIONS.map(opt => (
        <option key={opt.value} value={opt.value}>
          {opt.label}
        </option>
      ))}
    </select>

    <select
      onMouseDown={saveSelection}
      onChange={e => {
        restoreSelection();
        exec('fontSize', e.target.value);
      }}
      defaultValue="3"
      className="home-ml-1 home-h-8 home-rounded home-border home-border-gray-600 home-bg-[#1e1e3a] home-px-2 home-text-xs home-text-gray-300 focus:home-border-blue-500 focus:home-outline-none"
      title="Font size"
    >
      {FONT_SIZES.map(opt => (
        <option key={opt.value} value={opt.value}>
          {opt.label}
        </option>
      ))}
    </select>

    <Divider />

    <TBtn onClick={() => exec('bold')} title="Bold (Ctrl+B)">
      <Bold className="home-size-4" />
    </TBtn>
    <TBtn onClick={() => exec('italic')} title="Italic (Ctrl+I)">
      <Italic className="home-size-4" />
    </TBtn>
    <TBtn onClick={() => exec('underline')} title="Underline (Ctrl+U)">
      <Underline className="home-size-4" />
    </TBtn>
    <TBtn onClick={() => exec('strikeThrough')} title="Strikethrough">
      <Strikethrough className="home-size-4" />
    </TBtn>

    <Divider />

    <div className="home-relative" title="Text color">
      <input
        type="color"
        onMouseDown={saveSelection}
        onChange={e => {
          restoreSelection();
          exec('foreColor', e.target.value);
        }}
        className="home-absolute home-inset-0 home-size-8 home-cursor-pointer home-opacity-0"
      />
      <div className="home-flex home-size-8 home-items-center home-justify-center home-rounded home-text-gray-300 hover:home-bg-white/10">
        <span
          className="home-text-sm home-font-bold home-leading-none"
          style={{ borderBottom: '3px solid #ef4444' }}
        >
          A
        </span>
      </div>
    </div>

    <div className="home-relative" title="Highlight color">
      <input
        type="color"
        defaultValue="#ffff00"
        onMouseDown={saveSelection}
        onChange={e => {
          restoreSelection();
          exec('hiliteColor', e.target.value);
        }}
        className="home-absolute home-inset-0 home-size-8 home-cursor-pointer home-opacity-0"
      />
      <div className="home-flex home-size-8 home-items-center home-justify-center home-rounded home-text-gray-300 hover:home-bg-white/10">
        <span className="home-rounded home-bg-yellow-400 home-px-1 home-text-xs home-font-bold home-text-black">
          A
        </span>
      </div>
    </div>

    <Divider />

    <TBtn onClick={() => exec('justifyLeft')} title="Align left">
      <AlignLeft className="home-size-4" />
    </TBtn>
    <TBtn onClick={() => exec('justifyCenter')} title="Align center">
      <AlignCenter className="home-size-4" />
    </TBtn>
    <TBtn onClick={() => exec('justifyRight')} title="Align right">
      <AlignRight className="home-size-4" />
    </TBtn>
    <TBtn onClick={() => exec('justifyFull')} title="Justify">
      <AlignJustify className="home-size-4" />
    </TBtn>

    <Divider />

    <TBtn onClick={() => exec('insertUnorderedList')} title="Bullet list">
      <List className="home-size-4" />
    </TBtn>
    <TBtn onClick={() => exec('insertOrderedList')} title="Numbered list">
      <ListOrdered className="home-size-4" />
    </TBtn>

    <Divider />

    <TBtn onClick={() => exec('insertHorizontalRule')} title="Horizontal line">
      <Minus className="home-size-4" />
    </TBtn>
    <TBtn onClick={onInsertLink} title="Insert / edit link">
      <Link className="home-size-4" />
    </TBtn>
    <TBtn onClick={onInsertImage} title="Insert image">
      <ImageIcon className="home-size-4" />
    </TBtn>
    {onInsertForm && (
      <TBtn onClick={onInsertForm} title="Insert capture form">
        <FormInput className="home-size-4" />
      </TBtn>
    )}

    <Divider />

    <TBtn onClick={() => exec('removeFormat')} title="Clear formatting">
      <Eraser className="home-size-4" />
    </TBtn>
  </div>
);

export default EditorToolbar;
