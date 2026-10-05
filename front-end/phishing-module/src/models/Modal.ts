/**
 * Common modal props interfaces
 */

export interface IModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export interface IFormModalProps extends IModalProps {
  onSubmit: (data: unknown) => void;
  formData?: Record<string, unknown>;
}

export interface IConfirmModalProps extends IModalProps {
  onConfirm: () => void;
  title?: string;
  message?: string;
  confirmText?: string;
  cancelText?: string;
  isLoading?: boolean;
}
