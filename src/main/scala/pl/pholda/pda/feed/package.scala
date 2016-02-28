package pl.pholda.pda

import pl.pholda.pda.model.{StationId, TrainId}

/**
  * Created by piotr on 28.02.16.
  */
package object feed {

  protected val trainIdRegex = "p=train&id=(\\d+)$".r

  protected val stationIdRegex = "p=station&id=(\\d+)'?$".r

  def extractTrainId(url: String): TrainId = {
    trainIdRegex.findFirstMatchIn(url).get.group(1).toInt
  }

  def extractStationId(url: String): StationId = {
    stationIdRegex.findFirstMatchIn(url).get.group(1).toInt
  }

  val delayRegex = "(\\d+)".r
}
