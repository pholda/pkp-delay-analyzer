package pl.pholda.pda.model

import org.joda.time.{Minutes, DateTime}

/**
  * Created by piotr on 25.02.16.
  */
case class TrainAtStation(
  trainId: TrainId,
  trainNumber: TrainNumber,
  operator: String,
  from: String,
  to: String,
  arriveDate: Option[DateTime],
  arriveDelay: Minutes,
  departureDate: Option[DateTime],
  departureDelay: Minutes
)