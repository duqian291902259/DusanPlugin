package site.duqian.plugin.entity

import com.google.gson.annotations.SerializedName

data class RequestParams(val project: String)

/**
 * {"id":"cmpl-6tRpvYZJNHPnguLYddT3Occy7k91y","object":"text_completion","created":1678673063,"model":"text-davinci-003",
 * "choices":[{"text":" Kotlin is a statically typed, cross-platform, general-purpose programming language developed by JetBrains. It is designed to interoperate fully with Java and is an officially supported language for Android development. It is also the language used by the Spring Boot framework.",
 * "index":0,"logprobs":null,"finish_reason":"stop"}],"usage":{"prompt_tokens":10,"completion_tokens":53,"total_tokens":63}}
 */
data class ChatGptResult(
    @SerializedName("choices")
    val choices: List<Choice?>? = null,
    @SerializedName("created")
    val created: Int? = null, // 1678360192
    @SerializedName("id")
    val id: String? = null, // cmpl-6s8Rc4GdOpDBFCbvzAEYg2YW7QOio
    @SerializedName("model")
    val model: String? = null, // text-davinci-003
    @SerializedName("object")
    val objectX: String? = null, // text_completion
    @SerializedName("usage")
    val usage: Usage? = null
)

data class Choice(
    @SerializedName("finish_reason")
    val finishReason: String? = null, // stop
    @SerializedName("index")
    val index: Int? = null, // 0
    @SerializedName("logprobs")
    val logprobs: Any? = null, // null
    @SerializedName("text")
    val text: String? = null //  Kotlin is a statically typed, cross-platform, general-purpose programming language developed by JetBrains. It is designed to interoperate fully with Java and is an officially supported language for Android development. It is also the language used by the Spring Framework.
)

data class Usage(
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null, // 52
    @SerializedName("prompt_tokens")
    val promptTokens: Int? = null, // 10
    @SerializedName("total_tokens")
    val totalTokens: Int? = null // 62
)
