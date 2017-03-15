package eu.xeli.aquariumPI

class Relay(pin: Int, hasInverseHardware: Boolean = false) {

  private def setRelay(state: Boolean) {
    //TODO
  }

  def set(state: Boolean) {
    if (hasInverseHardware) {
      setRelay(!state)
    } else {
      setRelay(state)
    }
  }

  def enable() {
    set(true)
  }

  def disable() {
    set(false)
  }
}
