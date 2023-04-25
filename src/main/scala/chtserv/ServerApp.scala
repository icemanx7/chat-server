package chtserv

import cats.effect.kernel.MonadCancel
import cats.effect.{ExitCode, IO, IOApp}

import java.io.{
  BufferedReader,
  InputStream,
  InputStreamReader,
  ObjectInputStream
}
import java.net.ServerSocket
import cats.Monad
import cats.instances.tailRec
import scala.annotation.tailrec
import cats.effect.std
import cats.effect.IO
import cats.effect.std.Console
import chtserv.models.server.ConnectedUser._
import cats.effect.{Async, Concurrent, Ref, Sync}
import cats.{FlatMap, MonadError}
import cats.effect.{Async, Concurrent, Ref, Sync}
import cats.effect.std.UUIDGen
import cats.implicits._
import java.util.UUID
import chtserv.models.server.ConnectedUser
import java.io.BufferedWriter
import java.io.OutputStreamWriter

object ServerApp extends IOApp {
  val listener: ServerSocket = new ServerSocket(9080)

  def generateUUID: IO[UUID] = IO(UUID.randomUUID())
  def run(args: List[String]): IO[ExitCode] = {
    for {
      ref <- ConnectedClients[IO]()
      server <- Monad[IO].tailRecM[Boolean, Boolean](true) { refs =>
        val clientT = handleClient(ref).start
        clientT.flatMap { ss =>
          Monad[IO].pure(Left(true))
        }
      }
    } yield (ExitCode.Success)
  }

  private def handleClient(list: ConnectedClients[IO]): IO[Boolean] = {
    for {
      socketListener <- IO(listener.accept())
      socketReader <- IO(
        new BufferedReader(
          new InputStreamReader(socketListener.getInputStream)
        )
      )
      socketWriter <- IO(
        new BufferedWriter(
          new OutputStreamWriter(socketListener.getOutputStream)
        )
      )
      nickName <- IO(socketReader.readLine())
      uuid <- generateUUID
      user = ConnectedUserModel(uuid, socketReader, socketWriter, nickName)
      userId <- list.addUser(user)
      _ <- Console[IO].println(s"User has joined $user")
      readUserInput <- Monad[IO].tailRecM[BufferedReader, Boolean](
        socketReader
      ) { reader =>
        for {
          _ <- Console[IO].println("reading users message")
          userText <- IO(reader.readLine())
          _ <- Console[IO].println(s"The user text \n $userText")
          listUsers <- list.allMinusMe(nickName)
          res <-
            if (userText == "exit") {
              reader.close()
              IO(Right(true))
            } else {
              println(listUsers)
              IO(listUsers.foreach { ff =>
                ff.userOutPutStream.write(userText)
                ff.userOutPutStream.flush
              }) >>
                IO(Left(reader))
            }
        } yield res
      }
    } yield true
  }

  class ConnectedClients[F[_]: Concurrent](
      clients: Ref[F, List[ConnectedUserModel]]
  ) {
    def addUser(user: ConnectedUserModel): F[Unit] =
      clients.getAndUpdate(clients => user :: clients).void

    def getAll: F[List[ConnectedUserModel]] = clients.get
    def allMinusMe(nickName: String): F[List[ConnectedUserModel]] =
      clients.get.map(_.filter(_.userNickName != nickName))
  }
  object ConnectedClients {
    def apply[F[_]: Concurrent](): F[ConnectedClients[F]] =
      Ref[F]
        .of(List.empty[ConnectedUserModel])
        .map(cls => new ConnectedClients(cls))
  }
}
