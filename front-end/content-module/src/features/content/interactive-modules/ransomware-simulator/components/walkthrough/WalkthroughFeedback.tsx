interface IProps {
  message: string | null;
}

const WalkthroughFeedback = ({ message }: IProps) => {
  if (!message) return null;

  return (
    <p className="content-mt-1 content-text-xs content-leading-snug content-text-white/80">
      {message}
    </p>
  );
};

export default WalkthroughFeedback;
