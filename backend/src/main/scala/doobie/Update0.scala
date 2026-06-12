// Update0 提供 doobie 兼容的更新执行封装。

package doobie

import cats.effect.IO
import doobie.util.fragment.Fragment

final case class Update0(sql: String, generatedKeys: Option[Any]):
  def run: ConnectionIO[Int] =
    Fragment.const(sql).update.run
