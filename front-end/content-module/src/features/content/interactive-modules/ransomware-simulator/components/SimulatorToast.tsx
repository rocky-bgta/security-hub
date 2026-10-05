interface IProps {
  message: string;
  visible: boolean;
}

const SimulatorToast = ({ message, visible }: IProps) => {
  if (!visible) return null;

  return (
    <div
      className="content-pointer-events-auto content-max-w-sm content-rounded-lg content-border content-border-steel-gray content-bg-muted content-px-4 content-py-3 content-text-sm content-text-white content-shadow-lg"
      role="status"
      aria-live="polite"
    >
      {message}
    </div>
  );
};

export default SimulatorToast;
