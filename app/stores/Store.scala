package stores

import entities.Entity
import Entity.EntityLike
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocumentWriter, BSONDocumentReader, BSONDocument}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

/**
  * Created by stremlenye on 04/11/15.
  */
trait Store[A] {
  def collection: BSONCollection
}

abstract class CommonStore[A] (db: DefaultDB) extends Store[A] {

  val name: String

  override def collection: BSONCollection = db.collection[BSONCollection](name)
}

trait BSONIdentifier[K] {
  def identifier(id: K): BSONDocument
}

trait Fetchable[A, K] extends Store[A] with BSONIdentifier[K] {
  def fetch()(implicit el: EntityLike[A]): Future[Option[Seq[A]]] = {
    implicit val reader: BSONDocumentReader[Option[A]] = el.reader
    collection.find(BSONDocument()).cursor[Option[A]]().collect[Seq]() map {
      seq => if(seq.exists(item => item.isEmpty)) None else Some(seq.map(_.get))
    }
  }

  def fetch(id: K)(implicit el: EntityLike[A]): Future[Option[A]] = {
    implicit val reader: BSONDocumentReader[Option[A]] = el.reader
    collection.find(identifier(id)).one[Option[A]].map(_.getOrElse(None))
  }
}

trait Creatable[A] extends Store[A] {
  def create(entity: A)(implicit el: EntityLike[A]): Future[Int] = {
    implicit val writer: BSONDocumentWriter[A] = el.writer
    collection.insert(entity).map(_.n)
  }
}

trait Updatable[A, K] extends Store[A] with BSONIdentifier[K] {
  def update(id: K, entity: A)(implicit el: EntityLike[A]): Future[Int] = {
    implicit val writer: BSONDocumentWriter[A] = el.writer
    collection.update(identifier(id), entity).map(_.n)
  }
}

trait Removable[A, K] extends Store[A] with BSONIdentifier[K] {
  def remove(id: K)(implicit el: EntityLike[A]): Future[Int] = {
    implicit val writer: BSONDocumentWriter[A] = el.writer
    collection.remove(identifier(id)).map(_.n)
  }
}
