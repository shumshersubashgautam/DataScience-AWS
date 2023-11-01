package com.advancedspark.streaming.rating.store

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Time
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

import com.advancedspark.streaming.rating.core.RatingGeo

import redis.clients.jedis.Jedis

object Redis {
  def main(args: Array[String]) {

    val conf = new SparkConf()
    val session = SparkSession.builder().config(conf).getOrCreate()

    def createStreamingContext(): StreamingContext = {
      @transient val newSsc = new StreamingContext(session.sparkContext, Seconds(2))
      println(s"Creating new StreamingContext $newSsc")

      newSsc
    }
    val ssc = StreamingContext.getActiveOrCreate(createStreamingContext)

    // Kafka Config
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "demo.pipeline.io:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "example",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Set("item_ratings")

    // Create Kafka Direct Stream Receiver
    val ratingsStream = KafkaUtils.createDirectStream[String, String](
      ssc, PreferConsistent, Subscribe[String, String](topics, kafkaParams)
    )

    ratingsStream.foreachRDD {
      (message: RDD[ConsumerRecord[String, String]], batchTime: Time) => {
        message.cache()

        // Split each _2 element of the RDD (String,String) tuple into a RDD[Seq[String]]
        val tokens = message.map(_.value().split(","))

        // convert Tokens into RDD[Ratings]
        val ratings = tokens.map(token =>
          RatingGeo(token(0).trim.toInt, token(1).trim.toInt, token(2).trim.toInt, batchTime.milliseconds, token(3).trim.toString)
        )

       // increment the exact count for touserid in Redis
        ratings.foreachPartition(ratingsPartitionIter => {
          // TODO:  Fix this.
          //        1) This obviously only works when everything is running on 1 node.
          //        2) This should be using a Jedis Singleton/Pooled connection
          //        3) Explore the spark-redis package (RedisLabs:spark-redis:0.1.0+)
          val jedis = new Jedis("redis-master", 6379)
          val t = jedis.multi()
          ratingsPartitionIter.foreach(rating => {
            val keyExactRatingCount = s"""exact-rating-count:${rating.itemId}"""
	    t.incr(keyExactRatingCount)
            val keyDistinctUserRatingCount = s"""exact-distinct-user-rating-count:${rating.itemId}"""
            t.sadd(keyDistinctUserRatingCount, rating.userId.toString)
	  })
          t.exec()
          jedis.close()
        })

	message.unpersist()
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
