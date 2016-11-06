import java.io.File

import org.casualmiracles.nash.Nash

object NashExample {
  def main(args: Array[String]): Unit = {
    val nash = new Nash(new File("src/test/migrations"))
    val objects = """{"name":"channing"}""" :: """{"name":"lance"}""" :: Nil

    println(nash(objects))
  }
}
