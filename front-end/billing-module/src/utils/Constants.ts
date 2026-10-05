export const DEFAULT_PAGINATION_OFFSET = 0;

export const DEFAULT_PAGINATION_LIMIT = 10;

export const DEFAULT_PAGINATION_PER_PAGE = [
  {
    id: 10,
    value: '10',
    label: '10',
  },
  {
    id: 15,
    value: '15',
    label: '15',
  },
  {
    id: 20,
    value: '20',
    label: '20',
  },
];

export const DOT = -1;

export const ALPHANUMERIC_PATTERN = /^[a-z0-9]+$/i;

export const InitGetListParams = {
  offset: DEFAULT_PAGINATION_OFFSET,
  pageSize: DEFAULT_PAGINATION_LIMIT,
};

export const BASE_URL = import.meta.env.VITE_API_BASE_URL;
export const PUBLIC_URL = import.meta.env.VITE_PUBLIC_URL;
export const FILE_PATH_PREFIX = 'https://content.aspireelearning.com/';

export const ValidImageFormats = [
  'image/png',
  'image/jpg',
  'image/jpeg',
  'image/gif',
  'image/svg',
  'image/webp',
];

export const ValidVideoFormats = [
  'video/mp4',
  'video/avi',
  'video/mov',
  'video/mkv',
  'video/flv',
  'video/webm',
];

export const ValidAudioFormats = ['audio/mp3', 'audio/mpeg'];

export const TypeOptions = [
  { id: 1, label: 'Title', value: 'title' },
  { id: 2, label: 'Subtitle', value: 'subtitle' },
  { id: 3, label: 'Paragraph', value: 'paragraph' },
];

export const USERID = 'b2c2965f-f071-4a39-a2c2-9d3e5b325500';
