import { Badge } from 'common/Badge';
import { Checkbox } from 'common/Checkbox';
import { ICategoryDetail } from 'models/Course';
import { truncateText } from 'utils/Helper';

const TopicCard = ({
  key,
  topic,
  isSelected,
  onToggle,
}: {
  key: string;
  topic: any;
  isSelected: boolean;
  onToggle: () => void;
}) => {
  return (
    <div
      key={key}
      className="content-cursor-pointer content-rounded-lg content-bg-primary/10 content-p-4 content-transition-colors"
      onClick={onToggle}
    >
      <div className="content-flex content-items-start content-space-x-3">
        <Checkbox
          checked={isSelected}
          onCheckedChange={onToggle}
          onClick={e => e.stopPropagation()}
          className="content-mt-1"
        />
        <div className="content-min-w-0 content-flex-1">
          <h4 className="content-text-primary">{topic.topicName}</h4>
          <p className="content-mt-1 content-text-sm content-text-cloudy-white">
            {truncateText(topic.description, 10)}
          </p>
          <div className="content-mt-2 content-flex content-flex-wrap content-gap-1">
            <Badge variant="secondary" className="!content-text-gray-300">
              {topic.categoryDetails
                .map((category: ICategoryDetail) => category.categoryName)
                .join(', ')}
            </Badge>
            <Badge variant="secondary" className="!content-text-gray-300">
              {topic.contentTypeDetails.typeName}
            </Badge>
            <Badge variant="secondary" className="!content-text-gray-300">
              {topic.durationMinutes} minutes
            </Badge>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TopicCard;
