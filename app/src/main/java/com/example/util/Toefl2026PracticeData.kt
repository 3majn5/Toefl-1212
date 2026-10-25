package com.example.util

import com.example.data.model.AcademicReadingQuestion
import com.example.data.model.CompleteWordsExercise

object Toefl2026PracticeData {

    val CLOZE_EXERCISES: List<CompleteWordsExercise> = listOf(
        CompleteWordsExercise(
            id = "cloze_1",
            passageTitle = "Evolutionary Adaptation in Marine Life",
            textWithBlanks = "Marine biologists have long investigated how deep-sea organisms survive extreme hydrostatic pressure. Recent telemetry has shown that abyssal organisms possess specialized cellular membranes that remain fluid under freezing temperatures, allowing them to ad____ to conditions that would crush terrestrial organisms.",
            targetWord = "adapt",
            obscuredWord = "ad__t",
            hint = "Verb meaning to adjust or become suited to new conditions",
            explanation = "'Adapt' is required: to modify structure or behavior to survive in an environmental niche."
        ),
        CompleteWordsExercise(
            id = "cloze_2",
            passageTitle = "Paleoclimatology and Tree Rings",
            textWithBlanks = "Dendrochronologists analyze variations in annular tree growth to reconstruct temperature anomalies over millennial spans. Their findings are highly con_______ with glacial ice-core data retrieved from Greenland, confirming severe medieval climate fluctuations.",
            targetWord = "consistent",
            obscuredWord = "con______t",
            hint = "Adjective meaning agreeing with, matching, or compatible with other findings",
            explanation = "'Consistent' fits grammatically and semantically: 'consistent with' means agreeing or in accordance with."
        ),
        CompleteWordsExercise(
            id = "cloze_3",
            passageTitle = "Cognitive Psychology & Memory Recall",
            textWithBlanks = "In controlled neurological experiments, researchers observed that spaced repetition algorithms sig__________ enhance long-term memory consolidation compared to massed cramming sessions.",
            targetWord = "significantly",
            obscuredWord = "sig__________y",
            hint = "Adverb meaning in an important, noticeable, or statistically meaningful way",
            explanation = "'Significantly' is the standard academic adverb used to describe measurable, non-accidental empirical effects."
        ),
        CompleteWordsExercise(
            id = "cloze_4",
            passageTitle = "Economic Shifts in Urban Planning",
            textWithBlanks = "Municipal authorities frequently enc______ substantial logistical challenges when expanding public rapid transit infrastructure into historically preserved architectural districts.",
            targetWord = "encounter",
            obscuredWord = "enc_____r",
            hint = "Verb meaning to meet with or experience difficulties unexpectedly",
            explanation = "'Encounter' describes coming face to face with obstacles or complications in project execution."
        ),
        CompleteWordsExercise(
            id = "cloze_5",
            passageTitle = "Renewable Energy Transitions",
            textWithBlanks = "Engineering faculties are developing hybrid photovoltaic cells to max_____ energy conversion efficiency during overcast meteorological conditions.",
            targetWord = "maximize",
            obscuredWord = "max____e",
            hint = "Verb meaning to increase to the greatest possible amount or degree",
            explanation = "'Maximize' expresses increasing efficiency to the highest attainable limit."
        )
    )

    val READING_QUESTIONS: List<AcademicReadingQuestion> = listOf(
        AcademicReadingQuestion(
            id = "read_1",
            passage = "Early astronomical models posited an immutable celestial sphere. However, seventeenth-century telescopic observations by Galileo and Kepler provided empirical evidence that directly contradicted geocentric assumptions, instigating a fundamental paradigm shift across natural philosophy.",
            question = "In the passage, the word 'empirical' is closest in meaning to:",
            targetWord = "empirical",
            options = listOf("Theoretical and speculative", "Derived from observation and experiment", "Traditionally accepted by authority", "Purely mathematical and abstract"),
            correctIndex = 1,
            explanation = "'Empirical' refers to knowledge verifiable by observation or experience rather than pure logic or theory."
        ),
        AcademicReadingQuestion(
            id = "read_2",
            passage = "The widespread dissemination of movable type printing throughout fifteenth-century Europe facilitated the rapid distribution of scientific treatises, significantly diminishing the scholastic monopoly previously maintained by monastic scriptoria.",
            question = "In the passage, the word 'facilitated' is closest in meaning to:",
            targetWord = "facilitated",
            options = listOf("Made easier or promoted", "Delayed or obstructed", "Formally prohibited", "Financially sponsored"),
            correctIndex = 0,
            explanation = "'Facilitated' means made an action or process easy or easier."
        ),
        AcademicReadingQuestion(
            id = "read_3",
            passage = "Certain arid flora exhibit morphological adaptations, such as expansive shallow root networks and waxy cuticle coatings, which are indispensable for retaining moisture during prolonged multi-year droughts.",
            question = "In the passage, the word 'indispensable' is closest in meaning to:",
            targetWord = "indispensable",
            options = listOf("Optional and decorative", "Completely essential and necessary", "Easily replaceable", "Temporarily beneficial"),
            correctIndex = 1,
            explanation = "'Indispensable' denotes something absolutely necessary and impossible to manage without."
        ),
        AcademicReadingQuestion(
            id = "read_4",
            passage = "Paleontological excavations suggest that the transition of tetrapods from aquatic to littoral habitats was not a sudden divergence, but rather a gradual sequence of physiological innovations occurring over several million years.",
            question = "In the passage, the word 'divergence' is closest in meaning to:",
            targetWord = "divergence",
            options = listOf("Complete harmony", "Separation or branching off", "Total extinction", "Predictable repetition"),
            correctIndex = 1,
            explanation = "'Divergence' in evolutionary biology signifies a branching apart or developing in different directions."
        )
    )

    val DISCUSSION_WRITING_PROMPTS = listOf(
        "Academic Discussion: Online vs In-Person University Instruction.\nYour professor asks: 'Do digital learning environments enhance or diminish academic collaboration among undergraduates?' Write a short academic post (2-3 sentences) utilizing the target word naturally.",
        "Academic Discussion: Environmental Resource Allocation.\nYour professor asks: 'How should municipalities prioritize investments in renewable energy infrastructure versus public healthcare?' Express your viewpoint integrating the target word.",
        "Academic Discussion: Artificial Intelligence in Higher Education.\nYour professor asks: 'Does automated AI assessment support or undermine independent critical thinking?' Formulate an academic argument using the target word."
    )
}
