import { ReactNode, useLayoutEffect, useState } from 'react';
import { createPortal } from 'react-dom';

interface IProps {
  children: ReactNode;
}

const OverlayPortal = ({ children }: IProps) => {
  const [host, setHost] = useState<Element | null>(null);

  useLayoutEffect(() => {
    setHost(document.querySelector('[data-ransomware-simulator-overlay]'));
  }, []);

  if (!host) return null;
  return createPortal(<>{children}</>, host);
};

export default OverlayPortal;
