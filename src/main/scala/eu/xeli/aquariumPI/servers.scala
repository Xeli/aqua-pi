package eu.xeli.aquariumPI

case class Servers(pigpio: Server, aquaStatus: Server)
case class Server(host: String, port: Int, auth: Option[Authentication])
case class Authentication(username: String, password: String)
