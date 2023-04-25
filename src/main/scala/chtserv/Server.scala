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
import cats.effect.kernel.Ref
import cats.effect.std

object Server extends IOApp {
  val listener: ServerSocket = new ServerSocket(9080)
  def run(args: List[String]): IO[ExitCode] = {
    for {
      ref <- Ref[IO].of(0)
      server <- Monad[IO].tailRecM[Boolean, Boolean](true) { refs =>
        val clientT = createServer(ref).start
        clientT.flatMap { ss =>
          Monad[IO].pure(Left(true))
        }
      }
      _ <- IO.println("richard")
    } yield (ExitCode.Success)
  }

  private def createServer(list: Ref[IO, Int]) = {
    IO {
      val dd = listener.accept()
      val in =
        new BufferedReader(new InputStreamReader(dd.getInputStream()));
      println("user joined")
      val addUser =
        list.updateAndGet(x => x + 1).flatMap(myID => IO(println(myID)))
      addUser >> Monad[IO].tailRecM[BufferedReader, Boolean](in) { reader =>
        println("ri")
        for {
          z <- IO(reader.readLine())
          _ <- IO(println(z))
          res <-
            if (z == "exit") {
              reader.close()
              IO(Right(true))
            } else
              IO(Left(reader))
        } yield res
      }
    }.flatten
  }
}
