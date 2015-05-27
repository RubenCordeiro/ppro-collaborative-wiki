package pt.up.fe.ppro

import com.typesafe.config.ConfigFactory

object WeakiConfiguration {
  private val config = ConfigFactory.load()
  private val weakiConfig = config.getConfig("weaki")

  object Chat {
    private val chatConfig = weakiConfig.getConfig("chat")

    lazy val interface = if (chatConfig.hasPath("interface")) chatConfig.getString("interface") else "localhost"
    lazy val port = if (chatConfig.hasPath("port")) chatConfig.getInt("port") else 8080
  }

  object Collab {
    private val collabConfig = weakiConfig.getConfig("collab")

    lazy val port = if (collabConfig.hasPath("port")) collabConfig.getInt("port") else 8000
	lazy val interface = if (collabConfig.hasPath("interface")) collabConfig.getString("interface") else "localhost"
  }

  object Config {
    private val configConfig = config.getConfig("config")

    lazy val nodeExecutable = configConfig.getString("node.executable")
  }
}
