// ConnectionIO 提供 doobie 兼容的连接级 IO 定义。

package doobie

import cats.MonadThrow
import cats.effect.IO

import java.sql.Connection

final case class ConnectionIO[+A](unsafeRun: Connection => IO[A]):
  def map[B](f: A => B): ConnectionIO[B] =
    ConnectionIO(connection => unsafeRun(connection).map(f))

  def flatMap[B](f: A => ConnectionIO[B]): ConnectionIO[B] =
    ConnectionIO(connection => unsafeRun(connection).flatMap(value => f(value).unsafeRun(connection)))

  def void: ConnectionIO[Unit] =
    map(_ => ())

object ConnectionIO:
  def pure[A](value: A): ConnectionIO[A] =
    ConnectionIO(_ => IO.pure(value))

  def raiseError[A](error: Throwable): ConnectionIO[A] =
    ConnectionIO(_ => IO.raiseError(error))

  given MonadThrow[ConnectionIO] with
    override def pure[A](x: A): ConnectionIO[A] = ConnectionIO.pure(x)

    override def flatMap[A, B](fa: ConnectionIO[A])(f: A => ConnectionIO[B]): ConnectionIO[B] =
      fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => ConnectionIO[Either[A, B]]): ConnectionIO[B] =
      ConnectionIO { connection =>
        def loop(nextA: A): IO[B] =
          f(nextA).unsafeRun(connection).flatMap {
            case Right(value) => IO.pure(value)
            case Left(retry)  => loop(retry)
          }

        loop(a)
      }

    override def raiseError[A](e: Throwable): ConnectionIO[A] =
      ConnectionIO.raiseError(e)

    override def handleErrorWith[A](fa: ConnectionIO[A])(f: Throwable => ConnectionIO[A]): ConnectionIO[A] =
      ConnectionIO(connection => fa.unsafeRun(connection).handleErrorWith(error => f(error).unsafeRun(connection)))

trait Read[A]:
  def fromResultSet(resultSet: java.sql.ResultSet): A

object Read:
  given Read[Int] with
    def fromResultSet(resultSet: java.sql.ResultSet): Int =
      resultSet.getInt(1)

  given Read[Long] with
    def fromResultSet(resultSet: java.sql.ResultSet): Long =
      resultSet.getLong(1)

  given Read[String] with
    def fromResultSet(resultSet: java.sql.ResultSet): String =
      resultSet.getString(1)

  given Read[BigDecimal] with
    def fromResultSet(resultSet: java.sql.ResultSet): BigDecimal =
      BigDecimal(resultSet.getBigDecimal(1))

  given Read[Boolean] with
    def fromResultSet(resultSet: java.sql.ResultSet): Boolean =
      resultSet.getBoolean(1)

  given Read[java.sql.Timestamp] with
    def fromResultSet(resultSet: java.sql.ResultSet): java.sql.Timestamp =
      resultSet.getTimestamp(1)

  given Read[java.time.LocalDate] with
    def fromResultSet(resultSet: java.sql.ResultSet): java.time.LocalDate =
      resultSet.getDate(1).toLocalDate

  given Read[java.time.Instant] with
    def fromResultSet(resultSet: java.sql.ResultSet): java.time.Instant =
      resultSet.getTimestamp(1).toInstant
