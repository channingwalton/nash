package org.casualmiracles.nash

import java.io.{File, FilenameFilter}
import javax.script.{Invocable, ScriptEngineManager}

import scala.io.Source
import scala.collection.JavaConversions._

object Hack {
  val engine = new ScriptEngineManager().getEngineByName("nashorn")
  val runner = engine.asInstanceOf[Invocable]

  def runMigrations: List[String] = {
    val objects = """{"version": 1, "name":"channing"}""" :: """{"version": 1, "name":"lance"}""" :: Nil

    val migrations = new File("migrations").listFiles(new FilenameFilter {
      override def accept(dir: File, name: String) = name.endsWith("js")
    }).map(f ⇒ Source.fromFile(f).getLines().mkString("\n")).toList

    objects map migrate(migrations)
  }

  def migrate(migrations: List[String])(json: String): String =
    migrations.foldLeft(json) { (data, script) ⇒
      engine.eval(script)
      val r = runner.invokeFunction("migrate", data).asInstanceOf[String]
      println(s"Migrated $data to $r")
      r
    }


  def main(args: Array[String]): Unit = {
    println("Final result:" + runMigrations)
  }
}