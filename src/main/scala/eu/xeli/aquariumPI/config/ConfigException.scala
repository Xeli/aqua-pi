package eu.xeli.aquariumPI.config

case class InvalidConfigException(val message: String = "",
                                  val cause: Throwable = None.orNull)
                              extends Exception(message, cause)
