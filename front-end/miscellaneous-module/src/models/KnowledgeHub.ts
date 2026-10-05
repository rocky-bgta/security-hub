export enum KnowledgeHubResourceType {
  DOCUMENT = 'DOCUMENT',
  VIDEO = 'VIDEO',
  GUIDE = 'GUIDE',
  PDF = 'PDF',
  EXTERNAL_LINK = 'EXTERNAL_LINK',
}

export const KnowledgeResourceTypes = Object.values(KnowledgeHubResourceType);

export interface IKnowledgeHubCommon {
  name: string;
  slug: string;
  categoryId: string;
  content: string;
  sequence: number;
  imageUrl: string;
  videoUrl: string;
  publishedDate: string;
  expireDate: string;
  status: 'DRAFT' | 'ACTIVE' | 'INACTIVE';
  resourceType: KnowledgeHubResourceType;
  allowDownload: boolean;
}

export interface IKnowledgeHubPayload extends IKnowledgeHubCommon {
  tags: string;
  file?: File;
}

export interface IKnowledgeHub extends IKnowledgeHubCommon {
  id: string;
  likeCount: number;
  dislikeCount: number;
  tags: Array<{
    id: string;
    name: string;
    createdBy: string;
    createdAt: string;
  }>;
  category: {
    id: string;
    name: string;
    description: string;
    status: 'DRAFT' | 'ACTIVE' | 'INACTIVE';
  };
  comments: [
    {
      id: string;
      newsId: string;
      parentCommentId: string;
      userId: string;
      userName: string;
      content: string;
      createdAt: string;
      updatedAt: string;
    },
  ];
}
