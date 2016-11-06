package org.casualmiracles.nash

import java.io.{File, FilenameFilter}
import javax.script.{Invocable, ScriptEngineManager}

import scala.io.Source
import scala.collection.JavaConversions._

object Hack {

  val helpers =
    s"""
       |var runMigration = function(text) {
       |  var obj = JSON.parse(text);
       |  var migrated = migrate(obj)
       |  return JSON.stringify(migrated);
       |}
     """.stripMargin

  val engine = new ScriptEngineManager().getEngineByName("nashorn")
  val runner = engine.asInstanceOf[Invocable]

  engine.eval(helpers)

  def runMigrations(migrationsDir: File): List[String] = {
    val objects = """{"version": 1, "name":"channing"}""" :: """{"version": 1, "name":"lance"}""" :: Nil

    val migrations = migrationsDir.listFiles(new FilenameFilter {
      override def accept(dir: File, name: String) = name.endsWith("js")
    }).map(f ⇒ Source.fromFile(f).getLines().mkString("\n")).toList

    objects map migrate(migrations)
  }

  def migrate(migrations: List[String])(json: String): String =
    migrations.foldLeft(json) { (data, script) ⇒
      engine.eval(script)
      runner.invokeFunction("runMigration", data).asInstanceOf[String]
    }

  def main(args: Array[String]): Unit = {
    println("Final result:" + runMigrations(new File("migrations")))
  }
}