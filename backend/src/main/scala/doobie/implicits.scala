// implicits 提供 doobie 兼容的隐式扩展。

package doobie

import cats.syntax.all.*
import doobie.util.fragment.Fragment

import java.sql.{Connection, PreparedStatement, ResultSet, Timestamp}
import java.time.{Instant, LocalDate}
import java.util.UUID

object implicits:
  extension (sc: StringContext)
    def sql(args: Any*): Fragment =
      Fragment.fromInterpolation(sc, args)

  extension (fragment: Fragment)
    def update: UpdateFragment =
      UpdateFragment(fragment)

    def query[A](using read: Read[A]): QueryFragment[A] =
      QueryFragment(fragment)

  extension [A](connectionIO: ConnectionIO[A])
    def transact(transactor: Transactor[?]): cats.effect.IO[A] =
      transactor.useConnection(connection => connectionIO.unsafeRun(connection))

  final case class UpdateFragment(fragment: Fragment):
    def run: ConnectionIO[Int] =
      ConnectionIO { connection =>
        cats.effect.IO.blocking {
          val statement = connection.prepareStatement(fragment.sql)
          try
            bindParameters(statement, fragment.params)
            statement.executeUpdate()
          finally statement.close()
        }
      }

  final case class QueryFragment[A](fragment: Fragment)(using read: Read[A]):
    def unique: ConnectionIO[A] =
      ConnectionIO { connection =>
        cats.effect.IO.blocking {
          val statement = connection.prepareStatement(fragment.sql)
          try
            bindParameters(statement, fragment.params)
            val resultSet = statement.executeQuery()
            try
              if resultSet.next() then read.fromResultSet(resultSet)
              else throw new IllegalArgumentException(s"Query returned no rows: ${fragment.sql}")
            finally resultSet.close()
          finally statement.close()
        }
      }

    def to[F[_]]: ConnectionIO[List[A]] =
      ConnectionIO { connection =>
        cats.effect.IO.blocking {
          val statement = connection.prepareStatement(fragment.sql)
          try
            bindParameters(statement, fragment.params)
            val resultSet = statement.executeQuery()
            try
              val rows = List.newBuilder[A]
              while resultSet.next() do rows += read.fromResultSet(resultSet)
              rows.result()
            finally resultSet.close()
          finally statement.close()
        }
      }

  private def bindParameters(statement: PreparedStatement, params: Vector[Any]): Unit =
    params.zipWithIndex.foreach { case (param, index) =>
      bindSingleParameter(statement, index + 1, param)
    }

  private def bindSingleParameter(statement: PreparedStatement, position: Int, param: Any): Unit =
    param match
      case null => statement.setObject(position, null)
      case value: String => statement.setString(position, value)
      case value: Int => statement.setInt(position, value)
      case value: Long => statement.setLong(position, value)
      case value: BigDecimal => statement.setBigDecimal(position, value.bigDecimal)
      case value: java.math.BigDecimal => statement.setBigDecimal(position, value)
      case value: Boolean => statement.setBoolean(position, value)
      case value: Timestamp => statement.setTimestamp(position, value)
      case value: LocalDate => statement.setDate(position, java.sql.Date.valueOf(value))
      case value: Instant => statement.setTimestamp(position, Timestamp.from(value))
      case value: UUID => statement.setString(position, value.toString)
      case value: Fragment => statement.setString(position, value.sql)
      case value: Option[?] =>
        value match
          case Some(inner) => bindSingleParameter(statement, position, inner)
          case None        => statement.setObject(position, null)
      case other => statement.setObject(position, other)
