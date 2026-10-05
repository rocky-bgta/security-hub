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

export const USERID = 'c69e1a06-108d-4df5-bce6-ec85f1405490';
// export const USERID = '9c97e416-bb5b-42a0-b388-38ac9acf8a62';
// export const USERID = '1991974a-d4b9-4c65-9b3e-fd773bc2c454';
// export const USERID = '1d3442f7-80ef-4713-80f3-82b5f7c96e30';
// 6065876b-3740-448d-8082-f6c8e1225b11 sajol id incomplete
