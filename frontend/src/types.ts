export interface Author {
  username: string
  displayName: string
}

export interface Post {
  id: number
  author: Author
  content: string
  createdAt: string
}

export interface Feed {
  posts: Post[]
  nextCursor: string | null
}
