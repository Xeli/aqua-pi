package eu.xeli.aquariumPI

import collection.JavaConverters._
import java.nio.file._
import java.util.concurrent._

class FileWatcher(dir: Path, filename: String, function: (() => Unit)) extends Runnable {
  val watchService = FileSystems.getDefault().newWatchService()
  dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)

  def run() {
    while(true) {
      println("watching for changes")
      val watchKey = watchService.take()
      watchKey.pollEvents().asScala.foreach(process _)
      watchKey.reset()
    }
  }

  def process(event: WatchEvent[_]) {
    var path: Path = null
    event.context() match {
      case context:Path => path = context
      case _ => return
    }

    if(path.endsWith(filename)) {
      function()
    }
  }


  val executor = new ScheduledThreadPoolExecutor(1)
  executor.execute(this)


}
