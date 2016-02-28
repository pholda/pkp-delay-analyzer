package pl.pholda.pda.model

/**
  * Created by piotr on 25.02.16.
  */
case class TrainInRoute(
  trainId: TrainId,
  delays: List[DelayAtStation]
)