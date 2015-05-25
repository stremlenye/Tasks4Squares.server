package models

/**
 * Created by stremlenye on 21/01/15.
 */

case class Task (id: String,
                  text: String,
                  priority: Int,
                  owner: String)

case class User(id: String,
                name: String,
                login: String)
