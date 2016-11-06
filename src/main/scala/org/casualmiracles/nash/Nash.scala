package org.casualmiracles.nash

import java.io.{File, FilenameFilter}
import javax.script.{Invocable, ScriptEngineManager}

import scala.io.Source
import scala.collection.JavaConversions._

class Nash(migrationsDir: File) {

  private val helpers =
    s"""
       |var runMigration = function(text) {
       |  var obj = JSON.parse(text);
       |  var migrated = migrate(obj)
       |  return JSON.stringify(migrated);
       |}
     """.stripMargin

  private val engine = new ScriptEngineManager().getEngineByName("nashorn")
  private val runner = engine.asInstanceOf[Invocable]

  engine.eval(helpers)

  private val migrations = migrationsDir.listFiles(new FilenameFilter {
    override def accept(dir: File, name: String) = name.endsWith("js")
  }).map(f ⇒ Source.fromFile(f).getLines().mkString("\n")).toList

  def apply(objects: List[String]): List[String] =
    objects map migrate(migrations)

  private def migrate(migrations: List[String])(json: String): String =
    migrations.foldLeft(json) { (data, script) ⇒
      engine.eval(script)
      runner.invokeFunction("runMigration", data).asInstanceOf[String]
    }
}

object Nash {
  def main(args: Array[String]): Unit = {
    val nash = new Nash(new File("migrations"))
    val objects = """{"version": 1, "name":"channing"}""" :: """{"version": 1, "name":"lance"}""" :: Nil

    println("Final result:" + nash(objects))
  }
}