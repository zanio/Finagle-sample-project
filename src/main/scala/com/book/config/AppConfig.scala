package com.book.config

import com.typesafe.config.Config

trait AppConfig {

   private val config: Config = com.typesafe.config.ConfigFactory.load()

   val destination: String = config.getString("nytimes.host")
   val clientLabel: String = config.getString("nytimes.clientLabel")

    val redisHost = config.getString("redis.host")
    val redisPort = config.getInt("redis.port")
   val redisPassword: String = config.getString("redis.password")
   val redisConnectionString = s"$redisHost:$redisPort"

   val webClientConnectionString = s"$destination:443"

   val nyTimesToken: String = config.getString("nytimes.apiKey")
   val path: String = config.getString("nytimes.path")

   val REDIS_TTL: Int = config.getInt("redis.ttl")

}


