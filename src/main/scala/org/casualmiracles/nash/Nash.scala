package org.casualmiracles.nash

import java.io.{File, FilenameFilter}
import javax.script.{Invocable, ScriptEngineManager}

import scala.io.Source
import scala.collection.JavaConversions._

class Nash(migrationsDir: File) {

  case class Migration(version: Int, script: String)

  private val helpers =
    s"""
       |var runMigration = function(text, version) {
       |  var obj = JSON.parse(text);
       |  var result;
       |  if (obj.nashVersion >= version) {
       |    result = obj;
       |  } else {
       |    result = migrate(obj)
       |    result.nashVersion = version
       |  }
       |
       |  return JSON.stringify(result);
       |}
     """.stripMargin

  private val engine = new ScriptEngineManager().getEngineByName("nashorn")
  private val runner = engine.asInstanceOf[Invocable]

  engine.eval(helpers)

  private val migrations = migrationsDir.listFiles(new FilenameFilter {
    override def accept(dir: File, name: String) = name.endsWith("js")
  }).toList.map(toMigrations).sortBy(_.version)

  private def toMigrations(f: File): Migration = {
    val version = f.getName.substring(0, f.getName.indexOf("_")).toInt
    val script = Source.fromFile(f).getLines().mkString("\n")
    Migration(version, script)
  }

  def apply(objects: List[String]): List[String] =
    objects map migrate(migrations)

  private def migrate(migrations: List[Migration])(json: String): String =
    migrations.foldLeft(json) { (data, migration) ⇒
      engine.eval(migration.script)
      runner.invokeFunction("runMigration", data, migration.version.asInstanceOf[Integer]).asInstanceOf[String]
    }
}