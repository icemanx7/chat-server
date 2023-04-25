package chtserv.models.server

import java.io.BufferedReader

object ConnectedUser {

  final case class ConnectedUserModel(
      userUUID: String,
      userInputStream: BufferedReader,
      userNickName: String
  )

}
