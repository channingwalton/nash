package org.casualmiracles.nash

import java.io.{File, FilenameFilter}
import javax.script.{Invocable, ScriptEngine, ScriptEngineManager}

import scala.io.Source
import scala.util.control.NonFatal

class Nash(migrationsDir: File) {

  case class Migration(version: Int, script: String)

  private val runMigrationJS =
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

  private val engine: ScriptEngine = new ScriptEngineManager().getEngineByName("nashorn")
  private val runner: Invocable = engine.asInstanceOf[Invocable]

  engine.eval(runMigrationJS)

  private val migrationFiles = migrationsDir.listFiles(new FilenameFilter {
    override def accept(dir: File, name: String): Boolean = name.endsWith("js")
  }).toList

  private val migrations: Either[List[String], List[Migration]] =
    sequence(migrationFiles.map(toMigrations))

  private def toMigrations(f: File): Either[String, Migration] =
    for {
      version ← migrationNumber(f).right
      script ← loadContents(f).right
    } yield Migration(version, script)

  private def loadContents(f: File): Either[String, String] =
    try {
      Right(Source.fromFile(f).getLines().mkString("\n"))
    } catch {
      case NonFatal(_) ⇒ Left[String, String](s"Unable to load contents of file ${f.getName}")
    }

  private def migrationNumber(f: File): Either[String, Int] =
    try {
      Right(f.getName.substring(0, f.getName.indexOf("_")).toInt)
    } catch {
      case NonFatal(_) ⇒ Left[String, Int](s"Unable to extract a migration number from file ${f.getName}")
    }

  def apply(objects: List[String]): Either[List[String], List[String]] =
    migrations.right.flatMap { migrations ⇒
      sequence(objects.map(migrate(migrations, _)))
    }

  private def migrate(migrations: List[Migration], json: String): Either[String, String] =
    try {
      Right(migrations.foldLeft(json) { (data, migration) ⇒
        engine.eval(migration.script)
        runner.invokeFunction("runMigration", data, migration.version.asInstanceOf[Integer]).asInstanceOf[String]
      })
    } catch {
      case NonFatal(e) ⇒ Left(e.getMessage)
    }

  private def sequence[A, B](e: List[Either[A, B]]): Either[List[A], List[B]] = {
    e.partition(_.isLeft) match {
      case (Nil,  r) => Right(for(Right(i) <- r) yield i)
      case (l, _)    => Left(for(Left(s) <- l) yield s)
    }
  }
}