package eu.xeli.aquariumPI

import org.scalamock.scalatest.MockFactory
import org.scalatest._

class ControllerSpec extends FlatSpec with Matchers with MockFactory {
  "A controller" should "give back the correct target value" in {

    val controlleeRequiredValue = 10.0
    val controlleeMock = mock[Controllee]
    (controlleeMock.getValue _).expects().returning(controlleeRequiredValue)
    (controlleeMock.getFrequency _).expects().returning(100)

    val outputMock = mock[Output]
    (outputMock.getValue _).expects().returning(0)

    val controller = new Controller(outputMock, None)
    controller.addControllee(controlleeMock)

    controller.getCurrentTarget() should be (controlleeRequiredValue)
  }
}
