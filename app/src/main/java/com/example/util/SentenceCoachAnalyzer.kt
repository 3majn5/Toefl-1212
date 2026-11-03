package com.example.util

import com.example.data.model.VocabEntry
import com.example.data.model.WritingEvaluationResult

object SentenceCoachAnalyzer {

    private val INFORMAL = mapOf(
        "gonna" to "going to", "wanna" to "want to", "kids" to "children", "stuff" to "material / items",
        "things" to "factors / aspects", "a lot of" to "a considerable amount of / many",
        "lots of" to "numerous", "really" to "considerably", "pretty" to "fairly", "get" to "obtain / become",
        "big" to "substantial", "ok" to "acceptable", "okay" to "acceptable", "huge" to "substantial"
    )
    private val CONNECTORS = listOf(
        "because", "although", "however", "therefore", "whereas", "while", "which", "that", "since",
        "consequently", "moreover", "furthermore", "despite", "unless", "thus", "as a result"
    )
    private val CONTRACTION = Regex("\\b\\w+'(t|re|ve|ll|d|s|m)\\b", RegexOption.IGNORE_CASE)

    /** Inflected and derived forms that count as using the target word. */
    private fun acceptedForms(entry: VocabEntry): Set<String> {
        val base = entry.answerForm.lowercase()
        val forms = mutableSetOf(base)
        if (!base.contains(' ')) {
            val stem = base.removeSuffix("e")
            forms += listOf("${base}s", "${base}es", "${base}d", "${base}ed", "${stem}ed", "${stem}ing", "${base}ing", "${base}ly")
            if (base.endsWith("y")) forms += listOf("${base.dropLast(1)}ies", "${base.dropLast(1)}ied")
        }
        entry.wordFamily.forEach { forms += it.substringBefore(" (").trim().lowercase() }
        return forms.filter { it.length >= 2 }.toSet()
    }

    private fun wordSimilarity(a: String, b: String): Float {
        val wa = a.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
        val wb = b.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
        if (wa.isEmpty() || wb.isEmpty()) return 0f
        return wa.intersect(wb).size.toFloat() / wa.union(wb).size
    }

    fun evaluate(sentence: String, entry: VocabEntry): WritingEvaluationResult {
        val trimmed = sentence.trim()
        val lower = trimmed.lowercase()
        val targetLower = entry.answerForm.lowercase()
        val tokens = lower.split(Regex("[^a-z'-]+")).filter { it.isNotBlank() }

        val forms = acceptedForms(entry)
        val formUsed = forms.firstOrNull { form ->
            Regex("(?<![a-z])${Regex.escape(form)}(?![a-z])").containsMatchIn(lower)
        }
        val isPresent = formUsed != null

        val strengths = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        var prepCheck: String? = null
        var score = 2

        // 1. Target word
        if (isPresent) {
            score += 1
            strengths.add(
                if (formUsed == targetLower) "Uses the target word '${entry.answerForm}'."
                else "Uses '$formUsed', a valid form of '${entry.answerForm}'."
            )
        } else {
            suggestions.add("Ensure the target word '${entry.answerForm}' is included (or a form such as ${forms.filter { it != targetLower }.take(2).joinToString(" / ").ifBlank { "its plural or past tense" }}).")
            score -= 1
        }

        // 2. Mechanics
        val capitalized = trimmed.isNotEmpty() && trimmed.first().isUpperCase()
        val punctuated = trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?")
        if (capitalized && punctuated) {
            strengths.add("Correct capitalization and end punctuation.")
            score += 1
        } else {
            if (!capitalized) suggestions.add("Start the sentence with a capital letter.")
            if (!punctuated) suggestions.add("End the sentence with a period.")
        }

        // 3. Length and structure
        when {
            tokens.size < 6 -> suggestions.add("This looks like a fragment. Add a subject, a verb, and some context (aim for 10+ words).")
            tokens.size in 10..35 -> { strengths.add("Good academic sentence length (${tokens.size} words)."); score += 1 }
            tokens.size > 40 -> suggestions.add("At ${tokens.size} words the sentence may be a run-on. Consider splitting it in two.")
            else -> suggestions.add("Add a little more context to show the word's meaning clearly.")
        }

        // 4. Complexity: linking words and natural collocations
        val connector = CONNECTORS.firstOrNull { Regex("\\b${Regex.escape(it)}\\b").containsMatchIn(lower) }
        val collocationUsed = entry.collocations.firstOrNull { coll ->
            val words = coll.lowercase().split(" ").filter { it.length > 2 && it !in forms }
            words.isNotEmpty() && words.all { Regex("\\b${Regex.escape(it)}\\b").containsMatchIn(lower) }
        }
        if (collocationUsed != null) {
            strengths.add("Natural collocation: '$collocationUsed'.")
            score += 1
        } else if (connector != null) {
            strengths.add("Links ideas with '$connector', which adds academic complexity.")
            score += 1
        } else if (entry.collocations.isNotEmpty()) {
            suggestions.add("Try a common collocation, e.g. '${entry.collocations.first()}'.")
        }

        // 5. Register
        val informal = INFORMAL.keys.filter { Regex("\\b${Regex.escape(it)}\\b").containsMatchIn(lower) }
        if (informal.isNotEmpty()) {
            suggestions.add("Informal wording: " + informal.take(3).joinToString { "'$it' → '${INFORMAL[it]}'" } + ".")
            score -= 1
        }
        if (CONTRACTION.containsMatchIn(trimmed) && !lower.contains("'s ")) {
            suggestions.add("Avoid contractions in academic writing (write 'do not' instead of 'don't').")
        }

        // 6. Part of speech: a pure verb right after an article is usually a misuse ("the analyze")
        val isPureVerb = entry.grammaticalLabels.any { it.startsWith("v") } &&
            entry.wordFamily.none { it.substringBefore(" (").equals(entry.answerForm, ignoreCase = true) }
        if (isPureVerb && Regex("\\b(the|a|an)\\s+${Regex.escape(targetLower)}\\b").containsMatchIn(lower)) {
            suggestions.add("'${entry.answerForm}' is a verb here; after 'the/a' use its noun form" +
                (entry.wordFamily.firstOrNull { it.contains("(n.)") }?.let { " ('${it.substringBefore(" (")}')" } ?: "") + ".")
            score -= 1
        }

        // 7. Repetition and copying
        val repeated = tokens.groupingBy { it }.eachCount().filter { it.value >= 3 && it.key.length > 3 }.keys
        if (repeated.isNotEmpty()) suggestions.add("Vary your wording; '${repeated.first()}' is repeated several times.")
        val examples = (listOf(entry.exampleSentence) + entry.dictionaryExamples).filter { it.isNotBlank() }
        if (examples.any { wordSimilarity(it, trimmed) >= 0.75f }) {
            suggestions.add("This is very close to the model sentence. Write your own example to really learn the word.")
            score -= 1
        }

        // Collocation & Preposition Verification
        when (targetLower) {
            "consist of" -> {
                if (lower.contains("is consisted of") || lower.contains("was consisted of")) {
                    suggestions.add("Grammar Trap: 'consist of' cannot be used in passive voice. Write 'The committee consists of...' instead of 'is consisted of'.")
                    prepCheck = "Active voice only: [Subject] consists of [Parts]"
                    score -= 1
                } else if (lower.contains("consist of") || lower.contains("consists of") || lower.contains("consisted of")) {
                    strengths.add("Correct active prepositional syntax with 'of'.")
                    prepCheck = "Verified: Valid prepositional pattern with 'of'."
                }
            }
            "adhere" -> {
                if (lower.contains("adhere to") || lower.contains("adheres to") || lower.contains("adhered to")) {
                    strengths.add("Correct dependent preposition 'to' with 'adhere'.")
                    prepCheck = "Verified: 'adhere to' + noun"
                } else {
                    suggestions.add("'Adhere' typically requires the preposition 'to' (e.g. 'adhere to the protocol').")
                    prepCheck = "Requires preposition 'to'"
                }
            }
            "attribute" -> {
                if (lower.contains("attribute") && lower.contains("to")) {
                    strengths.add("Correct 'attribute [X] to [Y]' causal construction.")
                    prepCheck = "Verified: attribute [effect] to [cause]"
                } else {
                    suggestions.add("When using 'attribute' as a verb, pair it with 'to' (e.g., 'attribute success to discipline').")
                    prepCheck = "Pattern: attribute [X] to [Y]"
                }
            }
            "comprise" -> {
                if (lower.contains("comprise of") || lower.contains("comprises of")) {
                    suggestions.add("Stylistic note: Standard academic style prefers 'comprises' without 'of' (The book comprises ten chapters).")
                    prepCheck = "Preference: [Whole] comprises [Parts] (without 'of')"
                } else if (lower.contains("comprise")) {
                    strengths.add("Clean academic use of 'comprise'.")
                }
            }
            "rely" -> {
                if (lower.contains("rely on") || lower.contains("rely upon") || lower.contains("relies on") || lower.contains("relied on")) {
                    strengths.add("Accurate dependent preposition 'on/upon' with 'rely'.")
                    prepCheck = "Verified: rely on / upon"
                } else {
                    suggestions.add("'Rely' requires the preposition 'on' or 'upon'.")
                }
            }
        }

        // Authentic model sentence
        val model = entry.exampleSentence.ifBlank {
            entry.dictionaryExamples.firstOrNull()
                ?: "Researchers demonstrated that ${entry.headword} remains essential for academic inquiry."
        }

        return WritingEvaluationResult(
            targetWord = entry.answerForm,
            submittedSentence = trimmed,
            isTargetWordPresent = isPresent,
            strengths = strengths,
            suggestions = suggestions,
            requiredPrepositionCheck = prepCheck,
            modelSentence = model,
            estimatedAcademicScore = score.coerceIn(1, 5)
        )
    }
}
