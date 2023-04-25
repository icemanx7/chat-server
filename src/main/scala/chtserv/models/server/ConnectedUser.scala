package chtserv.models.server

import java.io.BufferedReader
import java.util.UUID
import java.io.BufferedOutputStream
import java.io.BufferedWriter

object ConnectedUser {

  final case class ConnectedUserModel(
      userUUID: UUID,
      userInputStream: BufferedReader,
      userOutPutStream: BufferedWriter,
      userNickName: String
  )

}
