import { ISimulatedFile } from '../types';

export const SIMULATED_FILES: Array<ISimulatedFile> = [
  {
    name: 'document.docx',
    icon: '📄',
    type: 'document',
    content: `Project Report
Introduction:
This document outlines the quarterly performance metrics for our department.
Key Metrics:
- Revenue Growth: 15% year-over-year
- Customer Satisfaction: 92%
- Employee Retention: 85%
Conclusion:
We have exceeded our targets for this quarter and are on track for continued growth.`,
  },
  {
    name: 'photo.jpg',
    icon: '🖼️',
    type: 'image',
    content: `JPEG Image
Dimensions: 1920x1080
Color Space: RGB
File Size: 2.4 MB
EXIF Data:
- Camera: Canon EOS 5D Mark IV
- Exposure: 1/125 sec at f/8
- ISO: 100
- Date: 2023-06-15
Image Description:
Family vacation photo taken at the beach during summer.`,
  },
  {
    name: 'spreadsheet.xlsx',
    icon: '📊',
    type: 'spreadsheet',
    content: `Quarterly Financial Report
Month   | Revenue | Expenses | Profit
--------|---------|----------|--------
January | $45,000 | $32,000  | $13,000
February| $52,000 | $35,000  | $17,000
March   | $48,000 | $33,000  | $15,000
Total Q1: $145,000 revenue, $100,000 expenses, $45,000 profit`,
  },
  {
    name: 'presentation.pptx',
    icon: '📽️',
    type: 'presentation',
    content: `Product Launch Presentation
Slide 1: Title
"NextGen Product Launch"
Slide 2: Market Analysis
- Target market size: $2.3B
- Growth rate: 12% annually
- Key competitors: 3 major players
Slide 3: Product Features
- AI-powered analytics
- Cloud-based infrastructure
- Mobile compatibility
Slide 4: Timeline
- Development: Q2 2023
- Beta testing: Q3 2023
- Full launch: Q4 2023`,
  },
  {
    name: 'database.db',
    icon: '🗄️',
    type: 'database',
    content: `SQLite Database Schema
Table: users
- id INTEGER PRIMARY KEY
- name TEXT NOT NULL
- email TEXT UNIQUE
- created_at TIMESTAMP
Table: products
- id INTEGER PRIMARY KEY
- name TEXT NOT NULL
- price DECIMAL(10,2)
- category_id INTEGER
Table: orders
- id INTEGER PRIMARY KEY
- user_id INTEGER
- product_id INTEGER
- quantity INTEGER
- order_date TIMESTAMP
Foreign Key Constraints:
- orders.user_id references users(id)
- orders.product_id references products(id)`,
  },
  {
    name: 'video.mp4',
    icon: '🎬',
    type: 'video',
    content: `Video File Information
Format: MP4
Codec: H.264
Resolution: 1920x1080 (Full HD)
Frame Rate: 30 fps
Duration: 5:42
Bitrate: 4500 kbps
Audio: AAC, 44.1 kHz, Stereo
Video Description:
Tutorial video demonstrating how to use our new software interface. Includes step-by-step instructions for all major features.`,
  },
  {
    name: 'archive.zip',
    icon: '📦',
    type: 'archive',
    content: `ZIP Archive Contents
Archive created: 2023-07-10 14:32:15
Compression method: Deflate
Original size: 45.2 MB
Compressed size: 32.7 MB
Compression ratio: 72%
Files in archive:
1. project_files/ (directory)
2. project_files/src/ (directory)
3. project_files/src/main.js (12.4 KB)
4. project_files/src/styles.css (8.7 KB)
5. project_files/images/ (directory)
6. project_files/images/logo.png (245 KB)
7. README.md (3.2 KB)`,
  },
  {
    name: 'config.ini',
    icon: '⚙️',
    type: 'config',
    content: `[Application Settings]
version=2.1.4
debug_mode=false
log_level=INFO
[Database]
host=localhost
port=5432
username=app_user
password=********
database_name=production_db
connection_timeout=30
[Security]
enable_encryption=true
session_timeout=3600
max_login_attempts=5
password_policy=strong
[API]
api_key=ABCD1234EFGH5678IJKL9012MNOP3456
rate_limit=1000/hour
enable_cors=true`,
  },
];
