package pl.pholda.pda.model

import org.joda.time.{DateTime, Minutes}

/**
  * Created by piotr on 25.02.16.
  */
case class DelayAtStation(
  stationId: StationId,
  stationName: String,
  trainNumber: TrainNumber,
  arriveDate: Option[DateTime],
  arriveDelay: Minutes = Minutes.ZERO,
  departureDate: Option[DateTime],
  departureDelay: Minutes = Minutes.ZERO,
  isEstimated: Boolean = false
)