package com.example.groww.data.model.network

import com.google.gson.annotations.SerializedName

data class NewsSentimentResponse(
    @SerializedName("items")
    val items: String,
    @SerializedName("sentiment_score_definition")
    val sentimentScoreDefinition: String,
    @SerializedName("relevance_score_definition")
    val relevanceScoreDefinition: String,
    @SerializedName("feed")
    val feed: List<Article>
)

data class Article(
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("time_published")
    val timePublished: String,
    @SerializedName("authors")
    val authors: List<String>,
    @SerializedName("summary")
    val summary: String,
    @SerializedName("banner_image")
    val bannerImage: String?,
    @SerializedName("source")
    val source: String,
    @SerializedName("category_within_source")
    val categoryWithinSource: String,
    @SerializedName("source_domain")
    val sourceDomain: String,
    @SerializedName("topics")
    val topics: List<Topic>,
    @SerializedName("overall_sentiment_score")
    val overallSentimentScore: Double,
    @SerializedName("overall_sentiment_label")
    val overallSentimentLabel: String,
    @SerializedName("ticker_sentiment")
    val tickerSentiment: List<TickerSentiment>
)

data class Topic(
    @SerializedName("topic")
    val topic: String,
    @SerializedName("relevance_score")
    val relevanceScore: String
)

data class TickerSentiment(
    @SerializedName("ticker")
    val ticker: String,
    @SerializedName("relevance_score")
    val relevanceScore: String,
    @SerializedName("ticker_sentiment_score")
    val tickerSentimentScore: String,
    @SerializedName("ticker_sentiment_label")
    val tickerSentimentLabel: String
)