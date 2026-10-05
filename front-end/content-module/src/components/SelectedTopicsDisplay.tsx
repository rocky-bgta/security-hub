import { ITopic } from 'models/Topic';

const SelectedTopicsDisplay = ({
  selectedTopics,
  topics,
}: {
  selectedTopics: string[];
  topics: ITopic[];
}) => {
  if (selectedTopics.length === 0) return null;

  return (
    <div className="content-mt-4 content-rounded-lg content-border content-border-card-border content-p-3">
      <p className="content-mb-2 content-text-sm content-font-medium content-text-primary">
        Selected Topics ({selectedTopics.length}):
      </p>
      <div className="content-flex content-flex-wrap content-gap-2">
        {selectedTopics.map(topicId => {
          const topic = topics.find(t => t.id === topicId);
          return (
            <span
              key={topicId}
              className="content-rounded-full content-bg-primary/20 content-px-2 content-py-1 content-text-xs content-text-primary"
            >
              {topic?.topicName || topicId}
            </span>
          );
        })}
      </div>
    </div>
  );
};

export default SelectedTopicsDisplay;
