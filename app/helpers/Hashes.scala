package helpers

/**
 * Created by stremlenye on 23/01/15.
 */
object Hashes {
  def sha(s: String) = {
    new String(java.security.MessageDigest.getInstance("SHA-256").digest(s.toCharArray.map(_.toByte)).map(_.toChar))
  }
}
