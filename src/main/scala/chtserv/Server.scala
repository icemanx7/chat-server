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

object Server extends IOApp {
  val listener: ServerSocket = new ServerSocket(9080)
  def run(args: List[String]): IO[ExitCode] = {
    for {
      server <- createServer().foreverM.start.whileM_(IO(true))
      _ <- IO.println("richard")
    } yield (ExitCode.Success)
  }

  private def createServer() = {
    IO {
      val dd = listener.accept()
      val in = // 3rd statement
        new BufferedReader(new InputStreamReader(dd.getInputStream()));
      println("user joined")

      // @tailrec
      // def runWhile(arg: BufferedReader): Unit = {
      //   println("ri")
      //   val z = arg.readLine()
      //   if (z == "exist") {
      //     println("User is done")
      //     arg.close()
      //   } else {
      //     println("restart")
      //     println(z)
      //     runWhile(arg)
      //   }

      // }
      // runWhile(in)
      // while (true) {
      //   println("ri")
      //   val z = in.readLine()
      //   println(z)
      //   println("zc")
      // }

      Monad[IO].tailRecM[BufferedReader, Boolean](in) { reader =>
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
