package com.example.util

import com.example.data.model.VocabEntry
import com.example.data.model.WritingEvaluationResult

object SentenceCoachAnalyzer {

    fun evaluate(sentence: String, entry: VocabEntry): WritingEvaluationResult {
        val trimmed = sentence.trim()
        val lower = trimmed.lowercase()
        val targetLower = entry.headword.trim().lowercase()

        // Check if headword or root lemma is present
        val rootLemma = targetLower.removeSuffix("e").removeSuffix("ed").removeSuffix("ing").removeSuffix("s")
        val isPresent = lower.contains(targetLower) || (rootLemma.length >= 4 && lower.contains(rootLemma))

        val strengths = mutableListOf<String>()
        val suggestions = mutableListOf<String>()
        var prepCheck: String? = null
        var score = 3

        if (isPresent) {
            strengths.add("Target vocabulary '${entry.headword}' is integrated into the sentence.")
            score += 1
        } else {
            suggestions.add("Ensure the target word '${entry.headword}' is explicitly included.")
            score -= 1
        }

        // Punctuation and Capitalization Check
        if (trimmed.isNotEmpty() && trimmed.first().isUpperCase()) {
            strengths.add("Proper initial capitalization.")
        } else {
            suggestions.add("Begin the academic sentence with a capital letter.")
        }

        if (trimmed.endsWith(".") || trimmed.endsWith("!") || trimmed.endsWith("?")) {
            strengths.add("Appropriate terminal punctuation.")
        } else {
            suggestions.add("Conclude the sentence with standard terminal punctuation (period).")
        }

        // Length and Complexity
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.size >= 8) {
            strengths.add("Substantive academic sentence length (${words.size} words).")
            if (words.size >= 12) score += 1
        } else {
            suggestions.add("Expand the sentence context to demonstrate academic usage (aim for at least 8-10 words).")
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
            targetWord = entry.headword,
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
