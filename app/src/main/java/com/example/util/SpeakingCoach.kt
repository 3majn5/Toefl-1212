package com.example.util

import com.example.data.model.VocabEntry
import kotlin.math.roundToInt

/** A TOEFL-style independent speaking task. */
data class SpeakingPrompt(val id: String, val question: String, val prepSeconds: Int = 15, val speakSeconds: Int = 45)

/** Feedback for one spoken answer, computed from the speech-recognition transcript. */
data class SpeakingFeedback(
    val wordCount: Int,
    val wordsPerMinute: Int,
    val targetWordsUsed: List<String>,
    val targetWordsMissed: List<String>,
    val fillerCount: Int,
    val fillersFound: List<String>,
    val linkingWordsUsed: List<String>,
    val sentenceCount: Int,
    /** 0–4, mirroring the TOEFL speaking rubric scale. */
    val score: Int,
    val strengths: List<String>,
    val tips: List<String>
)

/**
 * Offline speaking coach. The phone's speech recognizer produces the transcript; this scores
 * delivery (pace, fillers), development (length, linking words) and vocabulary (target words).
 */
object SpeakingCoach {

    val PROMPTS = listOf(
        SpeakingPrompt("s1", "Some students prefer to study alone, while others prefer to study in groups. Which do you prefer and why?"),
        SpeakingPrompt("s2", "Describe a skill you would like to learn in the future. Explain why it is important to you."),
        SpeakingPrompt("s3", "Do you agree or disagree: universities should require every student to take a course in public speaking?"),
        SpeakingPrompt("s4", "Talk about a book, film, or article that changed the way you think. What did you learn from it?"),
        SpeakingPrompt("s5", "Some people think technology makes us less social. Others think it connects us more. What is your view?"),
        SpeakingPrompt("s6", "Would you rather live in a big city or a small town? Use specific reasons and examples."),
        SpeakingPrompt("s7", "Describe a challenge you overcame. What did the experience teach you?"),
        SpeakingPrompt("s8", "Should governments spend more money on public transportation or on building new roads? Explain your opinion."),
        SpeakingPrompt("s9", "Is it better to have a few close friends or many acquaintances? Support your answer with examples."),
        SpeakingPrompt("s10", "Some people believe students should be allowed to use phones in class. Do you agree? Why or why not?"),
        SpeakingPrompt("s11", "Describe a teacher or mentor who influenced you, and explain how."),
        SpeakingPrompt("s12", "Do you think it is more important to choose a job you enjoy or a job that pays well? Explain.")
    )

    private val FILLERS = listOf("um", "uh", "uhm", "erm", "er", "hmm", "you know", "i mean", "kind of", "sort of", "basically")
    private val LINKING = listOf(
        "first", "firstly", "second", "secondly", "finally", "for example", "for instance", "because", "therefore",
        "however", "in addition", "moreover", "furthermore", "as a result", "on the other hand", "in conclusion",
        "although", "while", "since", "consequently", "to sum up", "overall", "such as", "this means"
    )

    /** Picks three target words to weave into the answer, favoring words the learner is studying. */
    fun pickTargetWords(entries: List<VocabEntry>, seed: Int): List<VocabEntry> {
        val singleWords = entries.filter { !it.answerForm.contains(' ') && it.answerForm.length >= 4 }
        val learning = singleWords.filter { it.studyStatus != com.example.data.model.StudyStatus.NEW }
        val pool = if (learning.size >= 6) learning else singleWords
        return pool.shuffled(kotlin.random.Random(seed)).take(3)
    }

    /** [spokenSeconds] ≤ 0 means the answer was typed, so pace isn't judged. */
    fun analyze(transcript: String, targets: List<VocabEntry>, spokenSeconds: Int): SpeakingFeedback {
        val spoken = spokenSeconds > 0
        val text = " " + transcript.lowercase().replace(Regex("[^a-z' ]"), " ").replace(Regex("\\s+"), " ").trim() + " "
        val words = text.trim().split(' ').filter { it.isNotBlank() }
        val wordCount = words.size
        val minutes = (spokenSeconds.coerceAtLeast(1)) / 60.0
        val wpm = if (wordCount == 0 || !spoken) 0 else (wordCount / minutes).roundToInt()

        val used = mutableListOf<String>()
        val missed = mutableListOf<String>()
        for (target in targets) {
            val forms = buildSet {
                val base = target.answerForm.lowercase()
                add(base)
                addAll(inflections(base))
                target.wordFamily.forEach { add(it.substringBefore(" (").lowercase()) }
            }.filter { it.isNotBlank() }
            if (forms.any { text.contains(" $it ") }) used += target.answerForm else missed += target.answerForm
        }

        val fillersFound = FILLERS.filter { f -> Regex("(?<= )${Regex.escape(f)}(?= )").containsMatchIn(text) }
        val fillerCount = FILLERS.sumOf { f -> Regex("(?<= )${Regex.escape(f)}(?= )").findAll(text).count() }
        val linking = LINKING.filter { text.contains(" $it ") }
        // Speech recognizers rarely add punctuation, so estimate sentences from linking words and length
        val sentenceCount = maxOf(
            transcript.count { it == '.' || it == '?' || it == '!' },
            (wordCount / 14.0).roundToInt()
        )

        val strengths = mutableListOf<String>()
        val tips = mutableListOf<String>()
        var points = 0

        // Development: TOEFL answers of ~45 s usually run 90–130 words
        when {
            wordCount >= 80 -> { points += 2; strengths += "Well-developed answer ($wordCount words)" }
            wordCount >= 45 -> { points += 1; tips += "Add one more reason or example — aim for 80+ words in 45 seconds" }
            else -> tips += "Keep talking for the full time: state your opinion, give two reasons, and an example"
        }
        // Delivery: comfortable pace is about 110–160 wpm
        when {
            wordCount == 0 -> {}
            !spoken -> if (wordCount >= 45) points += 1 // typed: pace can't be judged, don't penalize
            wpm in 100..170 -> { points += 1; strengths += "Natural pace ($wpm words per minute)" }
            wpm < 100 -> tips += "Your pace was slow ($wpm wpm). Practise saying key phrases without long pauses"
            else -> tips += "You spoke very fast ($wpm wpm). Slow down slightly so every word is clear"
        }
        val fillerRate = if (wordCount == 0) 0.0 else fillerCount.toDouble() / wordCount
        if (wordCount > 0 && fillerRate <= 0.03) {
            strengths += "Few filler words"
        } else if (fillerCount > 0) {
            points -= 1
            tips += "Cut fillers like ${fillersFound.take(3).joinToString { "\"$it\"" }} — pause silently instead"
        }
        // Organization
        if (linking.size >= 3) {
            points += 1
            strengths += "Clear organization (${linking.take(3).joinToString { "\"$it\"" }})"
        } else {
            tips += "Signal your structure with phrases like \"first\", \"for example\", \"as a result\""
        }
        // Vocabulary
        if (used.isNotEmpty()) strengths += "Used target word${if (used.size > 1) "s" else ""}: ${used.joinToString()}"
        if (used.size >= 2) points += 1
        if (missed.isNotEmpty()) tips += "Try to include: ${missed.joinToString()}"

        return SpeakingFeedback(
            wordCount = wordCount,
            wordsPerMinute = wpm,
            targetWordsUsed = used,
            targetWordsMissed = missed,
            fillerCount = fillerCount,
            fillersFound = fillersFound,
            linkingWordsUsed = linking,
            sentenceCount = sentenceCount,
            score = points.coerceIn(0, 4),
            strengths = strengths,
            tips = tips
        )
    }

    private fun inflections(base: String): List<String> = buildList {
        add(base + "s"); add(base + "es"); add(base + "ed"); add(base + "d"); add(base + "ing"); add(base + "ly")
        if (base.endsWith("e")) { add(base.dropLast(1) + "ing"); add(base.dropLast(1) + "ion") }
        if (base.endsWith("y")) { add(base.dropLast(1) + "ies"); add(base.dropLast(1) + "ied") }
    }

    /**
     * Pronunciation check: the recognizer returns its best guesses for what it heard.
     * Returns 2 if the target was its top guess, 1 if it was among the alternatives, 0 otherwise.
     */
    fun pronunciationScore(target: String, guesses: List<String>): Int {
        val t = normalize(target)
        val normalized = guesses.map { normalize(it) }
        return when {
            normalized.firstOrNull()?.let { it == t || it.split(' ').contains(t) } == true -> 2
            normalized.any { it == t || it.split(' ').contains(t) } -> 1
            else -> 0
        }
    }

    private fun normalize(s: String) = s.lowercase().replace(Regex("[^a-z' ]"), " ").replace(Regex("\\s+"), " ").trim()
}
