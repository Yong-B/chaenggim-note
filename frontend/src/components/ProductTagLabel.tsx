import type { ProductTag } from '../api/types'
import { productLabel } from '../lib/labels'

/** P1 제품 태그. 상태 배지와 구분되도록 테두리만 있는 조용한 태그로 표시한다. 태그가 없으면 아무것도 그리지 않는다. */
export function ProductTagLabel({ tag }: { tag: ProductTag | null }) {
  if (!tag) return null
  return <span className="tag">{productLabel[tag]}</span>
}
