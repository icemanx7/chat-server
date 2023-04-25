package chtserv

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}
import java.net.Socket
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import cats.Monad
import cats.effect.std._

object ClientApp extends IOApp {

  private def messageQueueListener(
      reader: BufferedReader,
      queue: Queue[IO, String]
  ): IO[Boolean] = {
    for {
      ff <- Monad[IO].tailRecM[Boolean, Boolean](true) { resf =>
        println("Reading queue")
        for {
          text <- IO.delay(reader.readLine())
          _ <- queue.tryOffer(text)
          res <- IO(Left(true))
        } yield res
      }
    } yield ff
  }
  override def run(args: List[String]): IO[ExitCode] = {
    val socket = new Socket("127.0.0.1", 9080)
    val cOutput = new BufferedWriter(
      new OutputStreamWriter(
        socket.getOutputStream
      )
    )
    val cInput = new BufferedReader(
      new InputStreamReader(
        socket.getInputStream
      )
    )
    for {
      q <- Queue.bounded[IO, String](10)
      fz <- messageQueueListener(cInput, q).start
      userName <- Console[IO].readLine
      _ = println("Signing in")
      _ = println(userName)
      _ <- IO(cOutput.write(userName))
      _ <- IO(cOutput.newLine())
      _ <- IO(cOutput.flush())
      ff <- Monad[IO].tailRecM[Boolean, Boolean](true) { resf =>
        for {
          text <- Console[IO].readLine
          _ <- IO(cOutput.write(text))
          _ <- IO(cOutput.newLine())
          _ <- IO(cOutput.flush())
          _ <- q.tryTake.flatMap(str => IO(println(str)))
          res <- IO(Left(true))
        } yield res
      }
    } yield ExitCode.Success
  }
}
