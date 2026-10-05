interface IProps {
  courseName: string;
  chapterName: string;
  contentName: string;
}
const ContentBreadcrumbs = ({
  courseName,
  chapterName,
  contentName,
}: IProps) => {
  return (
    <div>
      <p className="content-text-white content-text-opacity-50">
        {courseName} / {chapterName} /{' '}
        <span className="content-text-white content-text-opacity-100">
          {contentName}
        </span>
      </p>
    </div>
  );
};

export default ContentBreadcrumbs;
