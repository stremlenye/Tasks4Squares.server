package models

import org.joda.time.DateTime

/**
  * Created by stremlenye on 04/11/15.
  */
case class Token (token: String, owner: String, issuedAt: DateTime)
