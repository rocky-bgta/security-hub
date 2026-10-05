import { useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';

import { Button } from 'common/Button';
import { IContent, IQuizContent } from 'models/Content';
import { FILE_PATH_PREFIX } from 'utils/Constants';

interface AnnotationBody {
  purpose: string;
  value: string;
  id: string;
  annotation: string;
  created: string;
  creator: {
    isGuest: boolean;
    id: string;
  };
}

interface Point {
  0: number; // x
  1: number; // y
}

interface Geometry {
  bounds: {
    minX: number;
    minY: number;
    maxX: number;
    maxY: number;
  };
  x?: number;
  y?: number;
  w?: number;
  h?: number;
  points?: Point[]; // For polygon
}

interface AnnotationTarget {
  annotation: string;
  selector: {
    type: string; // 'RECTANGLE' or 'POLYGON'
    geometry: Geometry;
  };
  creator: {
    isGuest: boolean;
    id: string;
  };
  created: string;
}

interface Annotation {
  id: string;
  bodies: AnnotationBody[];
  target: AnnotationTarget;
}

interface BoundingBox {
  id: string;
  type: 'rectangle' | 'polygon';
  x: number;
  y: number;
  width: number;
  height: number;
  points?: Point[]; // For polygon
  tags: string[];
  isCorrect: boolean;
}

interface ScaledBoundingBox extends BoundingBox {
  scaledX: number;
  scaledY: number;
  scaledWidth: number;
  scaledHeight: number;
  scaledPoints?: { x: number; y: number }[]; // For polygon
}

interface IProps {
  content: IContent<IQuizContent>;
  isInteractive?: boolean;
  handleNextContent?: () => void;
}

const HotSpotBlock = ({
  content,
  isInteractive,
  handleNextContent,
}: IProps) => {
  const [visibleBoxes, setVisibleBoxes] = useState<Set<string>>(new Set());
  const [imageLink, setImageLink] = useState<string | null>(null);
  const [imageTitle, setImageTitle] = useState<string>('');
  const [boundingBoxes, setBoundingBoxes] = useState<BoundingBox[]>([]);
  const [userSelections, setUserSelections] = useState<Set<string>>(new Set());
  const [imageSize, setImageSize] = useState<{ width: number; height: number }>(
    { width: 0, height: 0 },
  );
  const [isChecked, setIsChecked] = useState<boolean>(false);
  const [correctFeedback, setCorrectFeedback] = useState<string>('');
  const [incorrectFeedback, setIncorrectFeedback] = useState<string>('');

  const imageRef = useRef<HTMLImageElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  // Function to check if a point is inside a polygon
  const isPointInPolygon = (
    point: { x: number; y: number },
    polygon: Point[],
  ): boolean => {
    // Ray casting algorithm
    let inside = false;
    for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
      const xi = polygon[i][0];
      const yi = polygon[i][1];
      const xj = polygon[j][0];
      const yj = polygon[j][1];

      const intersect =
        yi > point.y !== yj > point.y &&
        point.x < ((xj - xi) * (point.y - yi)) / (yj - yi) + xi;

      if (intersect) inside = !inside;
    }
    return inside;
  };

  // Initialize data from content
  useEffect(() => {
    setImageLink(content?.specific?.imageLink || null);
    setImageTitle((content?.specific?.imageTitle as string) || 'Spot items');
    setCorrectFeedback(
      (content?.specific?.correctFeedback as string) ||
        'Great job! You identified all items correctly.',
    );
    setIncorrectFeedback(
      (content?.specific?.incorrectFeedback as string) ||
        'Some answers are incorrect. Try again!',
    );

    // Get correct tags - this could be box IDs or tag values
    const correctBoxIds = content?.specific?.correctTags || [];

    // Process annotations to extract tags and build bounding boxes
    if (content?.specific?.annotations) {
      const annotations = content.specific
        .annotations as unknown as Annotation[];

      // Process annotations to extract bounding boxes and tags
      const boxes: BoundingBox[] = annotations.map(annotation => {
        // Get tags from bodies
        const tagBodies = annotation.bodies.filter(
          body => body.purpose === 'tagging',
        );
        const tags = tagBodies.map(body => body.value);

        // Check if this box is marked as correct (if its ID is in correctTags)
        const isCorrectBox = correctBoxIds.includes(annotation.id);

        // Extract geometry based on type
        const { type, geometry } = annotation.target.selector;
        const { bounds } = geometry;

        if (type === 'POLYGON' && geometry.points) {
          return {
            id: annotation.id,
            type: 'polygon',
            x: bounds.minX,
            y: bounds.minY,
            width: bounds.maxX - bounds.minX,
            height: bounds.maxY - bounds.minY,
            points: geometry.points,
            tags,
            isCorrect: isCorrectBox,
          };
        } else {
          // Default to rectangle
          const x = geometry.x !== undefined ? geometry.x : bounds.minX;
          const y = geometry.y !== undefined ? geometry.y : bounds.minY;
          const width =
            geometry.w !== undefined ? geometry.w : bounds.maxX - bounds.minX;
          const height =
            geometry.h !== undefined ? geometry.h : bounds.maxY - bounds.minY;

          return {
            id: annotation.id,
            type: 'rectangle',
            x,
            y,
            width,
            height,
            tags,
            isCorrect: isCorrectBox,
          };
        }
      });

      setBoundingBoxes(boxes);
    }

    // Reset state on new content
    setUserSelections(new Set());
    setIsChecked(false);
    setVisibleBoxes(new Set());
  }, [content]);

  // Handle image load to get dimensions
  const handleImageLoad = () => {
    if (imageRef.current) {
      setImageSize({
        width: imageRef.current.naturalWidth,
        height: imageRef.current.naturalHeight,
      });
    }
  };

  // Calculate scaled dimensions for each bounding box
  const calculateScaledBox = (box: BoundingBox): ScaledBoundingBox => {
    if (!imageRef.current || imageSize.width === 0) {
      return {
        ...box,
        scaledX: box.x,
        scaledY: box.y,
        scaledWidth: box.width,
        scaledHeight: box.height,
        scaledPoints: box.points?.map(point => ({ x: point[0], y: point[1] })),
      };
    }

    const scaleFactor = imageRef.current.width / imageSize.width;

    const result: ScaledBoundingBox = {
      ...box,
      scaledX: box.x * scaleFactor,
      scaledY: box.y * scaleFactor,
      scaledWidth: box.width * scaleFactor,
      scaledHeight: box.height * scaleFactor,
    };

    // Scale polygon points if present
    if (box.type === 'polygon' && box.points) {
      result.scaledPoints = box.points.map(point => ({
        x: point[0] * scaleFactor,
        y: point[1] * scaleFactor,
      }));
    }

    return result;
  };

  // Handle clicking on the image
  const handleImageClick = (event: React.MouseEvent<HTMLImageElement>) => {
    if (isChecked) return; // Don't allow interactions after checking answers

    // Get click coordinates relative to the image
    const rect = imageRef.current?.getBoundingClientRect();
    if (!rect) return;

    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    // Convert to percentages relative to original image size
    const percentX = x / rect.width;
    const percentY = y / rect.height;

    // Convert to original image coordinates
    const originalX = percentX * imageSize.width;
    const originalY = percentY * imageSize.height;

    // Check if click is within any bounding box
    for (const box of boundingBoxes) {
      let isInside = false;

      if (box.type === 'polygon' && box.points) {
        // Check if the point is inside the polygon
        isInside = isPointInPolygon({ x: originalX, y: originalY }, box.points);
      } else {
        // Check if the point is inside the rectangle
        isInside =
          originalX >= box.x &&
          originalX <= box.x + box.width &&
          originalY >= box.y &&
          originalY <= box.y + box.height;
      }

      if (isInside) {
        // User clicked inside this box - mark it as selected
        handleBoxClick(box.id);
        break;
      }
    }
  };

  // Handle clicking on a bounding box
  const handleBoxClick = (boxId: string) => {
    // Don't allow changes after checking answers
    if (isChecked) return;

    // Toggle selection - if already selected, unselect it
    if (userSelections.has(boxId)) {
      // Remove from selections
      setUserSelections(prev => {
        const newSelections = new Set(prev);
        newSelections.delete(boxId);
        return newSelections;
      });

      // Remove from visible boxes
      setVisibleBoxes(prev => {
        const newVisibleBoxes = new Set(prev);
        newVisibleBoxes.delete(boxId);
        return newVisibleBoxes;
      });
    } else {
      // Make box visible when clicked
      setVisibleBoxes(prev => {
        const newVisibleBoxes = new Set(prev);
        newVisibleBoxes.add(boxId);
        return newVisibleBoxes;
      });

      // Record user's selection
      setUserSelections(prev => {
        const newSelections = new Set(prev);
        newSelections.add(boxId);
        return newSelections;
      });
    }
  };

  // Handle container clicks (for delegation)
  const handleContainerClick = (event: React.MouseEvent) => {
    // No action needed - just stop propagation
    event.stopPropagation();
  };

  // Handle check button click
  const handleCheck = () => {
    // Get correct boxes and user selections
    const correctBoxIds = boundingBoxes
      .filter(box => box.isCorrect)
      .map(box => box.id);

    const userSelectedIds = Array.from(userSelections);

    // Check if the selections match exactly
    // 1. Same number of selections
    // 2. Every correct box is selected
    // 3. Every selected box is correct
    const correctSelectionCount =
      correctBoxIds.length === userSelectedIds.length;
    const allCorrectBoxesSelected = correctBoxIds.every(id =>
      userSelections.has(id),
    );
    const allSelectionsAreCorrect = userSelectedIds.every(id =>
      correctBoxIds.includes(id),
    );

    // Answer is correct only if all conditions are met
    const isCorrect =
      correctSelectionCount &&
      allCorrectBoxesSelected &&
      allSelectionsAreCorrect;

    // Show toast message based on result
    if (isCorrect) {
      toast.success(correctFeedback || 'Great job! You got it right.');
    } else {
      toast.error(incorrectFeedback || 'Not quite right. Try again.');
    }

    // Mark as checked, but don't show correct answers
    setIsChecked(true);
  };

  // Reset the check
  const handleReset = () => {
    setUserSelections(new Set());
    setIsChecked(false);
    setVisibleBoxes(new Set());
  };

  // Render a polygon from points
  const renderPolygon = (scaledPoints: { x: number; y: number }[]) => {
    if (!scaledPoints || scaledPoints.length < 3) return '';
    return scaledPoints.map(point => `${point.x},${point.y}`).join(' ');
  };

  return (
    <>
      {isInteractive && (
        <div className="content-absolute content-bottom-4 content-right-4 content-z-10">
          <Button onClick={handleNextContent} size="sm">
            Next
          </Button>
        </div>
      )}
      <div className="content-max-h-[600px] content-overflow-y-auto content-p-5">
        {/* Title */}
        <h2 className="content-mb-4 content-text-center content-text-xl content-font-semibold content-text-white">
          {imageTitle}
        </h2>

        {/* Image with bounding boxes */}
        <div
          ref={containerRef}
          className="content-relative content-rounded content-bg-white content-p-4 content-shadow-md"
          onClick={handleContainerClick}
        >
          {imageLink ? (
            <div className="content-relative content-inline-block">
              <img
                ref={imageRef}
                src={FILE_PATH_PREFIX + imageLink}
                alt={imageTitle}
                className="content-h-auto content-max-w-full content-cursor-pointer"
                onLoad={handleImageLoad}
                onClick={handleImageClick}
              />

              {/* SVG overlay for annotations */}
              <svg className="content-pointer-events-none content-absolute content-left-0 content-top-0 content-size-full">
                {boundingBoxes.map(box => {
                  const scaledBox = calculateScaledBox(box);
                  const isVisible = visibleBoxes.has(box.id);

                  // Skip rendering if not visible
                  if (!isVisible) {
                    return null;
                  }

                  // Style for both rectangles and polygons
                  const style = {
                    fill: 'rgba(59, 130, 246, 0.1)',
                    stroke: '#3b82f6',
                    strokeWidth: 2,
                  };

                  // Render rectangle or polygon
                  if (box.type === 'polygon' && scaledBox.scaledPoints) {
                    return (
                      <polygon
                        key={box.id}
                        points={renderPolygon(scaledBox.scaledPoints)}
                        style={style}
                        onClick={e => {
                          e.stopPropagation();
                          handleBoxClick(box.id);
                        }}
                        className="content-cursor-pointer"
                      />
                    );
                  } else {
                    return (
                      <rect
                        key={box.id}
                        x={scaledBox.scaledX}
                        y={scaledBox.scaledY}
                        width={scaledBox.scaledWidth}
                        height={scaledBox.scaledHeight}
                        style={style}
                        onClick={e => {
                          e.stopPropagation();
                          handleBoxClick(box.id);
                        }}
                        className="content-cursor-pointer"
                      />
                    );
                  }
                })}
              </svg>
            </div>
          ) : (
            <div className="content-flex content-h-64 content-items-center content-justify-center content-bg-gray-200 content-text-gray-500">
              No image available
            </div>
          )}
        </div>

        {/* Button Bar */}
        <div className="content-mt-4 content-flex content-justify-end content-space-x-3">
          {!isChecked && (
            <Button
              onClick={handleCheck}
              disabled={userSelections.size === 0}
              className="content-flex content-items-center"
            >
              <svg
                className="content-mr-1 content-size-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
              Check Answers
            </Button>
          )}

          {isChecked && (
            <Button onClick={handleReset} className="content-w-auto">
              Try Again
            </Button>
          )}
        </div>
      </div>
    </>
  );
};

export default HotSpotBlock;
