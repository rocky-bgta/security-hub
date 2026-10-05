import { IIdName } from 'models/EmailTemplate';
import React from 'react';

interface CategoryBadgeProps {
  category: string | IIdName;
  className?: string;
}

/**
 * Badge component for displaying landing page categories
 * Based on Task-04 Landing Page Library
 */
export const CategoryBadge: React.FC<CategoryBadgeProps> = ({
  category,
  className = '',
}) => {
  const label = typeof category === 'object' ? category.name : category;

  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${className}`}
    >
      {label}
    </span>
  );
};

export default CategoryBadge;
