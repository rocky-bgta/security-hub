import useDebounce from 'hooks/UseDebounce';
import {
  EMPTY_EMAIL_TEMPLATE_FILTERS,
  IEmailTemplateListFilters,
} from 'models/EmailTemplate';
import { useCallback, useMemo, useState } from 'react';
import { DEBOUNCE_DELAY } from 'utils/Constants';

const emptyFilters = (): IEmailTemplateListFilters => ({
  ...EMPTY_EMAIL_TEMPLATE_FILTERS,
  tags: [],
});

/**
 * Applied filter + search state for the email/SMS template library.
 * Draft selections live in the filter panel until Apply.
 */
export const useTemplateLibraryFilters = () => {
  const [applied, setApplied] =
    useState<IEmailTemplateListFilters>(emptyFilters);
  const [searchValue, setSearchValue] = useState('');
  const debouncedSearch = useDebounce(searchValue, DEBOUNCE_DELAY);

  const applyFilters = useCallback((draft: IEmailTemplateListFilters) => {
    setApplied({
      ...draft,
      tags: [...draft.tags],
    });
  }, []);

  const resetFilters = useCallback(() => {
    setSearchValue('');
    setApplied(emptyFilters());
  }, []);

  const activeFilterCount = useMemo(
    () =>
      [
        applied.difficulty,
        applied.payloadType,
        applied.location,
        applied.language,
        applied.status,
        ...(applied.tags.length > 0 ? [true] : []),
      ].filter(Boolean).length,
    [applied],
  );

  return {
    applied,
    searchValue,
    setSearchValue,
    debouncedSearch,
    applyFilters,
    resetFilters,
    activeFilterCount,
  };
};

export default useTemplateLibraryFilters;
