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
      userName <- Console[IO].readLine
      _ = println("Signing in")
      _ = println(userName)
      _ <- IO(cOutput.write(userName))
      _ <- IO(cOutput.newLine())
      _ <- IO(cOutput.flush())
      ff <- Monad[IO].tailRecM[Boolean, Boolean](true) { resf =>
        for {
          text <- IO.blocking(Console[IO].readLine).flatten
          _ <- IO(cOutput.write(text))
          _ <- IO(cOutput.newLine())
          _ <- IO(cOutput.flush())
          res <- IO(Left(true))
        } yield res
      }
    } yield ExitCode.Success
  }
}
