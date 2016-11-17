import java.io.File

import org.casualmiracles.nash.Nash

object NashExample {
  def main(args: Array[String]): Unit = {
    val nash = new Nash(new File("src/test/migrations"))
    val objects = """{"name":"channing"}""" :: """{"name":"lance"}""" :: Nil

    println(nash(objects))
    // Right(List({"name":"channing","age":0,"nashVersion":2,"dob":""}, {"name":"lance","age":0,"nashVersion":2,"dob":""}))
  }
}
