// Fragment 提供 doobie 兼容的 SQL 片段封装。

package doobie.util.fragment

import doobie.{ConnectionIO, Read}
import doobie.implicits.*

final case class Fragment(sql: String, params: Vector[Any] = Vector.empty):
  def ++(other: Fragment): Fragment =
    Fragment(sql + other.sql, params ++ other.params)

  def update: doobie.implicits.UpdateFragment =
    doobie.implicits.UpdateFragment(this)

  def query[A](using read: Read[A]): doobie.implicits.QueryFragment[A] =
    doobie.implicits.QueryFragment(this)

object Fragment:
  def const(sql: String): Fragment =
    Fragment(sql)

  def fromInterpolation(stringContext: StringContext, args: Seq[Any]): Fragment =
    val parts = stringContext.parts.iterator
    val argIter = args.iterator
    val builder = new StringBuilder(parts.next())
    val params = Vector.newBuilder[Any]

    while argIter.hasNext do
      argIter.next() match
        case fragment: Fragment =>
          builder.append(fragment.sql)
          params ++= fragment.params
        case other =>
          builder.append("?")
          params += other
      builder.append(parts.next())

    Fragment(builder.result(), params.result())
