// 本文件定义 UploadBlogImagePlanner，负责博客域对应接口入口。

import type { ContentImagePlannerResponse } from '@/microservices/content/objects/ContentImagePlannerResponse'
import { createQueryString, createSingleFileFormData, executeMultipartApiRequest } from '@/shared-kernel/api/ApiTransport'

export const uploadBlogImage = (userId: string, imageFile: File): Promise<ContentImagePlannerResponse> =>
  executeMultipartApiRequest(
    '/blog/images' + createQueryString({ userId }),
    'POST',
    createSingleFileFormData('image', imageFile),
  )
