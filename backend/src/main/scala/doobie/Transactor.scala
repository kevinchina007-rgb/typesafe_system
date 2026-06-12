// Transactor 提供 doobie 兼容的事务执行封装。

package doobie

import cats.effect.IO
import cats.effect.kernel.Resource
import com.typesafe.travel.persistence.{DatabaseConfig, DatabaseConnection}

import java.sql.Connection

sealed trait Transactor[F[_]]:
  private[doobie] def useConnection[A](use: Connection => IO[A]): IO[A]

object Transactor:
  private final case class ConfigTransactor[F[_]](databaseConfig: DatabaseConfig) extends Transactor[F]:
    override private[doobie] def useConnection[A](use: Connection => IO[A]): IO[A] =
      ConnectionExecutor.run(databaseConfig)(use)

  private final case class ExistingConnectionTransactor[F[_]](connection: Connection) extends Transactor[F]:
    override private[doobie] def useConnection[A](use: Connection => IO[A]): IO[A] =
      use(connection)

  def fromDatabaseConfig[F[_]](databaseConfig: DatabaseConfig): Transactor[F] =
    ConfigTransactor(databaseConfig)

  def fromConnection[F[_]](connection: Connection, maybeLogHandler: Option[Any]): Transactor[F] =
    ExistingConnectionTransactor(connection)

  def resource[F[_]](databaseConfig: DatabaseConfig): Resource[IO, Transactor[F]] =
    Resource.pure(fromDatabaseConfig[F](databaseConfig))

private object ConnectionExecutor:
  def run[A](databaseConfig: DatabaseConfig)(use: Connection => IO[A]): IO[A] =
    DatabaseConnection.open(databaseConfig).bracket { connection =>
      connection.setAutoCommit(false)
      use(connection).attempt.flatMap {
        case Right(value) =>
          IO.blocking(connection.commit()).as(value)
        case Left(error) =>
          IO.blocking(connection.rollback()) *> IO.raiseError(error)
      }
    } { connection =>
      IO.blocking(connection.close()).handleErrorWith(_ => IO.unit)
    }
