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
    <div className="content-flex content-size-full content-flex-col content-items-center content-justify-center content-gap-y-10">
      <h2 className="content-text-white">
        Error! Something went wrong when loading remote app.
      </h2>

      <div className="content-flex content-gap-x-4">
        <button
          onClick={resetErrorBoundary}
          className="content-rounded-md content-border content-border-white content-bg-black/10 content-px-8 content-py-2 content-text-white content-decoration-solid content-transition-all content-duration-200 content-ease-in-out hover:content-bg-white hover:content-text-black"
        >
          Try again
        </button>

        <button
          onClick={handleRetry}
          className="content-rounded-md content-border content-border-white content-bg-black/10 content-px-8 content-py-2 content-text-white content-decoration-solid content-transition-all content-duration-200 content-ease-in-out hover:content-bg-white hover:content-text-black"
        >
          Reload the page
        </button>
      </div>
    </div>
  );
};

export default ErrorFallback;
