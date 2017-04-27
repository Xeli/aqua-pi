package eu.xeli.aquariumPI

import collection.JavaConverters._
import java.nio.file._
import java.util.concurrent._

class FileWatcher(dir: Path, filename: String, function: (() => Unit)) extends Runnable {
  val watchService = FileSystems.getDefault().newWatchService()
  dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY)

  def run() {
    while(true) {
      val watchKey = watchService.take()
      watchKey.pollEvents().asScala.foreach(process _)
      watchKey.reset()
    }
  }

  def process(event: WatchEvent[_]) {
    event.context() match {
      case path:Path => {
        if(path.endsWith(filename)) {
          function()
        }
      }
      case _ => return
    }

  }

  val executor = new ScheduledThreadPoolExecutor(1)
  executor.execute(this)
}
