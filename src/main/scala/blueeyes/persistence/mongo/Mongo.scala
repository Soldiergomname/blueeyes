package blueeyes.persistence.mongo

import blueeyes.json.JsonAST.{JObject, JNothing}
import collection.immutable.List
import blueeyes.persistence.mongo.json.MongoJson._
import scala.collection.JavaConversions._
import com.mongodb.{MongoException, DBObject, DB, DBCollection}

trait Mongo{
  def database(databaseName: String): MongoDatabase
}

trait DatabaseCollectionSource{
  def getCollection(collectionName: String): DatabaseCollection
}

trait DatabaseCollection{
  def insert(dbObjects: List[com.mongodb.DBObject])
}

class MongoDatabase(collectionSource: DatabaseCollectionSource){
  def apply[T](query: MongoQuery[T]): T = {
    query(collectionSource.getCollection(query.collection.name))
  }
}

trait QueryBehaviour[T] extends Function[DatabaseCollection, T]

trait SelectQueryBehaviour extends QueryBehaviour[List[JObject]]{
  def apply(collection: DatabaseCollection): List[JObject] = {
    Nil
  }
  def selection : MongoSelection
  def filter    : Option[MongoFilter]
  def sort      : Option[MongoSort]
  def skip      : Option[Int]
  def limit     : Option[Int]
}
trait SelectOneQueryBehaviour extends QueryBehaviour[Option[JObject]]{
  def apply(collection: DatabaseCollection): Option[JObject] = {
    None
  }
  def selection : MongoSelection
  def filter    : Option[MongoFilter]
}
trait InsertQueryBehaviour extends QueryBehaviour[JNothing.type]{
  def apply(collection: DatabaseCollection): JNothing.type = {
    collection.insert(objects.map(jObject2MongoObject(_)))
    JNothing
  }
  def objects: List[JObject]
}
trait RemoveQueryBehaviour extends QueryBehaviour[JNothing.type]{
  def apply(collection: DatabaseCollection): JNothing.type = {
    JNothing
  }
  def filter: Option[MongoFilter]
}
trait UpdateQueryBehaviour extends QueryBehaviour[JNothing.type]{
  def apply(collection: DatabaseCollection): JNothing.type = {
    JNothing
  }
  def value: JObject
  def filter: Option[MongoFilter]
  def upsert: Boolean
  def multi: Boolean
}

object RealMongo{
  class RealMongo(database: DB) extends Mongo{
    def database(databaseName: String) = new MongoDatabase(new RealDatabaseCollectionSource(database))
  }

  class RealDatabaseCollectionSource(database: DB) extends DatabaseCollectionSource{
    def getCollection(collectionName: String) = new RealDatabaseCollection(database.getCollection(collectionName))
  }

  class RealDatabaseCollection(collection: DBCollection) extends DatabaseCollection{
    def insert(dbObjects: List[DBObject]) = {
      val result = collection.insert(dbObjects).getLastError
      if (!result.ok){
        result.throwOnError
      }
    }
  }
}
