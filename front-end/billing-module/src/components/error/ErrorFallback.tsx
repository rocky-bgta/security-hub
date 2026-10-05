interface IFallbackProps {
  error: any;
  resetErrorBoundary: (...args: any[]) => void;
}

const ErrorFallback = ({ error, resetErrorBoundary }: IFallbackProps) => {
  console.error('Remote app loading error:', error);

  const handleRetry = () => {
    console.log('Retried for remote import again.');
    resetErrorBoundary();
    window.location.reload();
  };

  return (
    <div className="flex size-full flex-col items-center justify-center gap-y-10">
      <h2 className="text-white">
        Error! Something went wrong when loading remote app.
      </h2>

      <button
        onClick={handleRetry}
        className="rounded-md border border-white bg-black/10 px-8 py-2 text-white decoration-solid transition-all duration-200 ease-in-out hover:bg-white hover:text-black"
      >
        Refresh Here
      </button>
    </div>
  );
};

export default ErrorFallback;
