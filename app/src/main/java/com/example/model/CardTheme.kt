package com.example.model

enum class CardTheme(
    val id: String,
    val displayName: String,
    val icon: String,
    val description: String,
    val sampleEmojis: List<String>,
    val emojis: List<String>
) {
    ANIMALS(
        id = "animals",
        displayName = "Animals",
        icon = "🐾",
        description = "Cute pets & wild safari",
        sampleEmojis = listOf("🐶", "🐱", "🐼", "🦊"),
        emojis = listOf(
            "🐶", "🐱", "🐼", "🦁", "🦊", "🐯",
            "🐰", "🐸", "🐵", "🦄", "🐙", "🐬",
            "🦋", "🐨", "🦉", "🐘", "🐢", "🐧",
            "🦩", "🦔", "🐝", "🦭", "🦓", "🦒"
        )
    ),
    FOOD(
        id = "food",
        displayName = "Food",
        icon = "🍕",
        description = "Yummy snacks & sweets",
        sampleEmojis = listOf("🍕", "🍔", "🍦", "🍩"),
        emojis = listOf(
            "🍕", "🍔", "🍟", "🍦", "🍩", "🍓",
            "🍉", "🥑", "🌮", "🍣", "🍫", "🍎",
            "🍿", "🥞", "🎂", "🍇", "🥐", "🍜",
            "🍪", "🥨", "🍍", "🧁", "🍒", "🍗"
        )
    ),
    SPACE(
        id = "space",
        displayName = "Space",
        icon = "🚀",
        description = "Cosmic planets & stars",
        sampleEmojis = listOf("🚀", "🛸", "🪐", "🌟"),
        emojis = listOf(
            "🚀", "🛸", "🪐", "🌟", "🌙", "☄️",
            "👾", "🛰️", "🌍", "👨‍🚀", "🔭", "🌌",
            "☀️", "🌠", "⚡", "🌕", "🔮", "📡",
            "💫", "👽", "✨", "🌑", "🧭", "🛡️"
        )
    ),
    EMOJI_MIX(
        id = "emoji_mix",
        displayName = "Emoji Mix",
        icon = "✨",
        description = "Vibrant fun collection",
        sampleEmojis = listOf("🍎", "🎮", "🌈", "💎"),
        emojis = listOf(
            "🍎", "🍕", "🚀", "⚽", "🐱", "🦊",
            "🌟", "🎮", "🎵", "🍔", "🌈", "🐼",
            "💎", "🍦", "🥑", "🦄", "🛸", "🎯",
            "🍩", "🎈", "🔥", "🎨", "🌮", "⚡"
        )
    );

    companion object {
        fun fromId(id: String): CardTheme {
            return entries.find { it.id == id } ?: EMOJI_MIX
        }
    }
}
