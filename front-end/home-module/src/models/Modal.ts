export interface IModalHeaderProps {
  children: React.ReactNode;
  /**
   * Additional className for the modal header
   */
  className?: string;
  /**
   * Additional className for the header icon
   */
  iconClassName?: string;
  /**
   * Defines the close button's position
   */
  closeButtonPosition?: 'right' | 'left';
  /**
   * Defines the modal should be hidden
   */
  onClose?: () => void;
}

export interface IModalBodyProps {
  children?: React.ReactNode;
  /**
   * Additional className for modal body
   */
  className?: string;
}

export interface IModalProps extends React.HTMLAttributes<HTMLDivElement> {
  children?: React.ReactNode;
  /**
   * Function executed when the closed button is clicked
   */
  onClose: () => void;
  /**
   * Defines if the modal is open
   */
  isOpen: boolean;
  /**
   * Defines the modal will not be hidden if turns true
   */
  disableOutsideClick?: boolean;
  /**
   * Defines the modal animation
   */
  animation?: 'fade' | 'zoom' | 'top' | 'bottom';
  /**
   * Defines the modal className
   */
  className?: string;
}
