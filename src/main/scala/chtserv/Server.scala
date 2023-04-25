package chtserv

import cats.effect.kernel.MonadCancel
import cats.effect.{ExitCode, IO, IOApp}

import java.io.{BufferedReader, InputStream, InputStreamReader, ObjectInputStream}
import java.net.ServerSocket

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
        new BufferedReader(
          new InputStreamReader(dd.getInputStream()));
      println("user joined")
      while (true) {
        println("ri")
        val z = in.readLine()
        println(z)
        println("zc")
      }
    }
  }
  private def block(listener: ServerSocket) = {
    listener.accept()
    println("user connected")
  }

}
