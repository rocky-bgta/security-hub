interface IFallbackProps {
  error: unknown;
  resetErrorBoundary: (...args: unknown[]) => void;
}

const ErrorFallback = ({ error, resetErrorBoundary }: IFallbackProps) => {
  console.error('Remote app loading error in host:', error);

  const handleRetry = () => {
    console.log('Retried for remote import again.');
    window.location.reload();
  };

  return (
    <div className="home-flex home-size-full home-flex-col home-items-center home-justify-center home-gap-y-10">
      <h2 className="home-text-white">
        Error! Something went wrong when loading remote app.
      </h2>

      <div className="home-flex home-gap-x-4">
        <button
          onClick={resetErrorBoundary}
          className="home-rounded-md home-border home-border-white home-bg-black/10 home-px-8 home-py-2 home-text-white home-decoration-solid home-transition-all home-duration-200 home-ease-in-out hover:home-bg-white hover:home-text-black"
        >
          Try again
        </button>
        <button
          onClick={handleRetry}
          className="home-rounded-md home-border home-border-white home-bg-black/10 home-px-8 home-py-2 home-text-white home-decoration-solid home-transition-all home-duration-200 home-ease-in-out hover:home-bg-white hover:home-text-black"
        >
          Reload the page
        </button>
      </div>
    </div>
  );
};

export default ErrorFallback;
