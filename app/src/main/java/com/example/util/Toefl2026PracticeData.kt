package com.example.util

import com.example.data.model.AcademicReadingQuestion
import com.example.data.model.CompleteWordsExercise
import com.example.data.model.VocabEntry
import kotlin.math.abs

object Toefl2026PracticeData {

    val CLOZE_EXERCISES: List<CompleteWordsExercise> = listOf(
        CompleteWordsExercise(
            id = "cloze_1",
            passageTitle = "Evolutionary Adaptation in Marine Life",
            textWithBlanks = "Marine biologists have long investigated how deep-sea organisms survive extreme hydrostatic pressure. Recent telemetry has shown that abyssal organisms possess specialized cellular membranes that remain fluid under freezing temperatures, allowing them to ad___ to conditions that would crush terrestrial organisms.",
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
        ),
        CompleteWordsExercise(
            id = "cloze_6",
            passageTitle = "Archaeological Stratigraphy",
            textWithBlanks = "When excavating multi-layered settlements in Mesopotamia, archaeologists must carefully pre_____ fragile organic textiles from rapid oxidation upon exposure to atmospheric air.",
            targetWord = "preserve",
            obscuredWord = "pre____e",
            hint = "Verb meaning to maintain something in its original or existing state",
            explanation = "'Preserve' means to protect historical artifacts from decay or damage."
        ),
        CompleteWordsExercise(
            id = "cloze_7",
            passageTitle = "Macroeconomic Monetary Policy",
            textWithBlanks = "Central banks periodically adjust benchmark interest rates to mit_____ inflationary pressures without triggering a prolonged contraction in industrial output.",
            targetWord = "mitigate",
            obscuredWord = "mit____e",
            hint = "Verb meaning to make less severe, serious, or painful",
            explanation = "'Mitigate' is a high-frequency academic verb meaning to alleviate or reduce negative impacts."
        ),
        CompleteWordsExercise(
            id = "cloze_8",
            passageTitle = "Tectonic Plate Subduction",
            textWithBlanks = "Geophysical sensors deployed along the Pacific Rim monitor subtle seismic tremors that pre____ major volcanic eruptions by several weeks.",
            targetWord = "precede",
            obscuredWord = "pre___e",
            hint = "Verb meaning to come before something in time or order",
            explanation = "'Precede' indicates chronological priority—occurring prior to the main eruption."
        ),
        CompleteWordsExercise(
            id = "cloze_9",
            passageTitle = "Sociological Urbanization Trends",
            textWithBlanks = "Demographic transitions across nineteenth-century industrial centers acc________ the migration of agrarian laborers toward densely populated manufacturing hubs.",
            targetWord = "accelerated",
            obscuredWord = "acc_______d",
            hint = "Verb (past tense) meaning sped up or caused to happen more quickly",
            explanation = "'Accelerated' captures how industrialization increased the rate of rural-to-urban migration."
        ),
        CompleteWordsExercise(
            id = "cloze_10",
            passageTitle = "Astrophysical Spectroscopy",
            textWithBlanks = "By examining absorption lines in stellar spectra, astronomers can accurately det______ the chemical composition and radial velocity of distant exoplanets.",
            targetWord = "determine",
            obscuredWord = "det_____e",
            hint = "Verb meaning to ascertain or establish exactly as a result of research or calculation",
            explanation = "'Determine' is used across STEM contexts for establishing precise empirical values."
        ),
        CompleteWordsExercise(
            id = "cloze_11",
            passageTitle = "Ecological Keystone Species",
            textWithBlanks = "The removal of apex predators from temperate forest ecosystems can trig___ a trophic cascade that severely depletes native understory vegetation.",
            targetWord = "trigger",
            obscuredWord = "tri___r",
            hint = "Verb meaning to cause an event or situation to happen or exist",
            explanation = "'Trigger' means to initiate a chain reaction or ecological cascade."
        ),
        CompleteWordsExercise(
            id = "cloze_12",
            passageTitle = "Epidemiological Transmission Models",
            textWithBlanks = "Public health researchers emphasize that rigorous sanitation protocols are ind_________ for preventing waterborne pathogens in densely populated coastal regions.",
            targetWord = "indispensable",
            obscuredWord = "ind_________e",
            hint = "Adjective meaning absolutely necessary or essential",
            explanation = "'Indispensable' means too important to be done without."
        ),
        CompleteWordsExercise(
            id = "cloze_13",
            passageTitle = "Linguistic Acquisition Hypotheses",
            textWithBlanks = "Developmental psycholinguists argue that infants possess an in____ capacity to discern phonemic boundaries across diverse human languages.",
            targetWord = "innate",
            obscuredWord = "in___e",
            hint = "Adjective meaning inborn, natural, or existing from birth",
            explanation = "'Innate' refers to biological capacities present from birth rather than learned."
        ),
        CompleteWordsExercise(
            id = "cloze_14",
            passageTitle = "Renaissance Patronage Networks",
            textWithBlanks = "Wealthy mercantile families in fifteenth-century Florence sou___ to enhance their civic prestige by commissioning monumental frescoes from master workshops.",
            targetWord = "sought",
            obscuredWord = "sou__t",
            hint = "Verb (past tense of seek) meaning attempted or desired to obtain",
            explanation = "'Sought to' is a classic academic construction meaning 'attempted to' or 'aimed to'."
        ),
        CompleteWordsExercise(
            id = "cloze_15",
            passageTitle = "Quantum Decoherence",
            textWithBlanks = "Maintaining superconducting qubits at near-absolute-zero temperatures is crucial because even min___ thermal fluctuations can disrupt quantum superposition.",
            targetWord = "minute",
            obscuredWord = "min__e",
            hint = "Adjective meaning extremely small or negligible in scale",
            explanation = "'Minute' (pronounced my-NOOT) means tiny or infinitesimal in scientific contexts."
        ),
        CompleteWordsExercise(
            id = "cloze_16",
            passageTitle = "Behavioral Economics & Heuristics",
            textWithBlanks = "Empirical studies demonstrate that consumers rarely make purely rational calculations; instead, they rel_ on cognitive shortcuts when evaluating financial risk.",
            targetWord = "rely",
            obscuredWord = "re_y",
            hint = "Verb meaning to depend on with full trust or confidence (followed by 'on')",
            explanation = "'Rely on' means to depend upon a method or heuristic."
        ),
        CompleteWordsExercise(
            id = "cloze_17",
            passageTitle = "Glacial Geomorphology",
            textWithBlanks = "As continental ice sheets rec____ during interglacial epochs, they deposit vast moraines of unsorted boulders and glacial till across the landscape.",
            targetWord = "recede",
            obscuredWord = "rec__e",
            hint = "Verb meaning to move back or withdraw from a previous position",
            explanation = "'Recede' describes glaciers retreating or moving backward as temperatures rise."
        ),
        CompleteWordsExercise(
            id = "cloze_18",
            passageTitle = "Neuroplasticity in Adulthood",
            textWithBlanks = "Contrary to earlier dogma that the adult brain is anatomically fixed, modern neuroimaging rev____ that synaptic pathways reorganize in response to intensive skill training.",
            targetWord = "reveals",
            obscuredWord = "rev___s",
            hint = "Verb meaning makes previously unknown or secret information known",
            explanation = "'Reveals' is standard academic reporting language for showing empirical findings."
        ),
        CompleteWordsExercise(
            id = "cloze_19",
            passageTitle = "Agrarian Soil Depletion",
            textWithBlanks = "Intensive monoculture farming without crop rotation eventually dep_____ essential nitrogen compounds from topsoil, necessitating synthetic fertilizers.",
            targetWord = "depletes",
            obscuredWord = "dep____s",
            hint = "Verb meaning uses up the supply or resources of",
            explanation = "'Depletes' means exhausts or diminishes natural soil nutrients."
        ),
        CompleteWordsExercise(
            id = "cloze_20",
            passageTitle = "Constitutional Jurisprudence",
            textWithBlanks = "Legal historians note that ambiguous statutory clauses often req____ judicial interpretation to resolve conflicts between municipal ordinances and federal mandates.",
            targetWord = "require",
            obscuredWord = "req___e",
            hint = "Verb meaning need for a particular purpose",
            explanation = "'Require' expresses formal necessity in legal and academic prose."
        ),
        CompleteWordsExercise(
            id = "cloze_21",
            passageTitle = "Avian Magnetoreception",
            textWithBlanks = "Migratory songbirds nav_____ across thousands of kilometers of featureless ocean by detecting subtle inclinations in Earth's geomagnetic field.",
            targetWord = "navigate",
            obscuredWord = "nav____e",
            hint = "Verb meaning plan and direct the route or course of travel",
            explanation = "'Navigate' describes how migratory species orient and guide their long-distance flights."
        ),
        CompleteWordsExercise(
            id = "cloze_22",
            passageTitle = "Industrial Polymer Synthesis",
            textWithBlanks = "Chemical engineers introduced biodegradable catalysts to sub_______ for petroleum-derived plastics in medical packaging applications.",
            targetWord = "substitute",
            obscuredWord = "sub______e",
            hint = "Verb meaning use or add in place of (collocates with 'for')",
            explanation = "'Substitute for' means to replace an older material with an alternative."
        ),
        CompleteWordsExercise(
            id = "cloze_23",
            passageTitle = "Ancient Epigraphy",
            textWithBlanks = "Comparative philologists spent decades attempting to dec_____ the syllabic scripts inscribed on bronze tablets unearthed in Mycenaean palaces.",
            targetWord = "decipher",
            obscuredWord = "dec____r",
            hint = "Verb meaning convert a text written in code or an ancient script into normal language",
            explanation = "'Decipher' means to decode or interpret ancient inscriptions."
        ),
        CompleteWordsExercise(
            id = "cloze_24",
            passageTitle = "Atmospheric Aerosol Forcing",
            textWithBlanks = "Sulfate aerosols injected into the stratosphere by explosive volcanic eruptions ref____ incoming solar radiation, temporarily cooling global surface temperatures.",
            targetWord = "reflect",
            obscuredWord = "ref___t",
            hint = "Verb meaning throw back heat, light, or sound without absorbing it",
            explanation = "'Reflect' describes scattering solar radiation back into space."
        ),
        CompleteWordsExercise(
            id = "cloze_25",
            passageTitle = "Organizational Sociology",
            textWithBlanks = "Decentralized research teams frequently col________ across continental time zones using asynchronous version-control repositories.",
            targetWord = "collaborate",
            obscuredWord = "col_______e",
            hint = "Verb meaning work jointly on an activity or project",
            explanation = "'Collaborate' means to cooperate academically or professionally on shared research."
        )
    )

    val READING_QUESTIONS: List<AcademicReadingQuestion> = listOf(
        AcademicReadingQuestion(
            id = "read_1",
            passage = "Early astronomical models posited an immutable celestial sphere. However, seventeenth-century telescopic observations by Galileo and Kepler provided empirical evidence that directly contradicted geocentric assumptions, instigating a fundamental paradigm shift across natural philosophy.",
            question = "In the passage, the word 'empirical' is closest in meaning to:",
            targetWord = "empirical",
            options = listOf(
                "Theoretical and speculative",
                "Derived from observation and experiment",
                "Traditionally accepted by authority",
                "Purely mathematical and abstract"
            ),
            correctIndex = 1,
            explanation = "'Empirical' refers to knowledge verifiable by observation or experience rather than pure logic or theory."
        ),
        AcademicReadingQuestion(
            id = "read_2",
            passage = "The widespread dissemination of movable type printing throughout fifteenth-century Europe facilitated the rapid distribution of scientific treatises, significantly diminishing the scholastic monopoly previously maintained by monastic scriptoria.",
            question = "In the passage, the word 'facilitated' is closest in meaning to:",
            targetWord = "facilitated",
            options = listOf(
                "Made easier or promoted",
                "Delayed or obstructed",
                "Formally prohibited",
                "Financially sponsored"
            ),
            correctIndex = 0,
            explanation = "'Facilitated' means made an action or process easy or easier."
        ),
        AcademicReadingQuestion(
            id = "read_3",
            passage = "Certain arid flora exhibit morphological adaptations, such as expansive shallow root networks and waxy cuticle coatings, which are indispensable for retaining moisture during prolonged multi-year droughts.",
            question = "In the passage, the word 'indispensable' is closest in meaning to:",
            targetWord = "indispensable",
            options = listOf(
                "Optional and decorative",
                "Completely essential and necessary",
                "Easily replaceable",
                "Temporarily beneficial"
            ),
            correctIndex = 1,
            explanation = "'Indispensable' denotes something absolutely necessary and impossible to manage without."
        ),
        AcademicReadingQuestion(
            id = "read_4",
            passage = "Paleontological excavations suggest that the transition of tetrapods from aquatic to littoral habitats was not a sudden divergence, but rather a gradual sequence of physiological innovations occurring over several million years.",
            question = "In the passage, the word 'divergence' is closest in meaning to:",
            targetWord = "divergence",
            options = listOf(
                "Complete harmony",
                "Separation or branching off",
                "Total extinction",
                "Predictable repetition"
            ),
            correctIndex = 1,
            explanation = "'Divergence' in evolutionary biology signifies a branching apart or developing in different directions."
        ),
        AcademicReadingQuestion(
            id = "read_5",
            passage = "While early ethnographers often portrayed indigenous foraging societies as entirely isolated from neighboring agrarian states, contemporary archaeological evidence demonstrates extensive, reciprocal trade networks spanning entire continents.",
            question = "In the passage, the word 'reciprocal' is closest in meaning to:",
            targetWord = "reciprocal",
            options = listOf(
                "Mutual and two-way",
                "Hostile and competitive",
                "Accidental and rare",
                "Strictly ceremonial"
            ),
            correctIndex = 0,
            explanation = "'Reciprocal' describes an exchange or relationship in which both parties give and receive equally."
        ),
        AcademicReadingQuestion(
            id = "read_6",
            passage = "During the late Permian period, massive basaltic lava floods in Siberia released unprecedented quantities of greenhouse gases, triggering severe oceanic anoxia that annihilated nearly ninety percent of marine calcifying species.",
            question = "In the passage, the word 'annihilated' is closest in meaning to:",
            targetWord = "annihilated",
            options = listOf(
                "Gradually relocated",
                "Completely destroyed",
                "Slightly modified",
                "Temporarily sheltered"
            ),
            correctIndex = 1,
            explanation = "'Annihilated' means wiped out or destroyed utterly."
        ),
        AcademicReadingQuestion(
            id = "read_7",
            passage = "Unlike classical symphony orchestras that adhere strictly to a composer's written score, baroque chamber ensembles routinely expected keyboardists to improvise harmonic accompaniments from a figured bass notation.",
            question = "In the passage, the phrase 'adhere strictly to' is closest in meaning to:",
            targetWord = "adhere",
            options = listOf(
                "Follow closely and faithfully",
                "Publicly criticize",
                "Completely rewrite",
                "Casually ignore"
            ),
            correctIndex = 0,
            explanation = "'Adhere to' means to stick firmly to or follow a rule, text, or commitment closely."
        ),
        AcademicReadingQuestion(
            id = "read_8",
            passage = "Hydrothermal vent communities thrive in complete darkness at the ocean floor; instead of photosynthetic algae, chemoautotrophic bacteria constitute the foundational trophic level by oxidizing dissolved hydrogen sulfide.",
            question = "In the passage, the word 'constitute' is closest in meaning to:",
            targetWord = "constitute",
            options = listOf(
                "Consume rapidly",
                "Form or make up",
                "Abandon permanently",
                "Measure accurately"
            ),
            correctIndex = 1,
            explanation = "'Constitute' means to be a part of a whole or to form/establish something."
        ),
        AcademicReadingQuestion(
            id = "read_9",
            passage = "Because ancient papyrus scrolls rapidly deteriorate in humid temperate climates, the vast majority of surviving Graeco-Roman administrative manuscripts have been recovered from the arid sands of Upper Egypt.",
            question = "In the passage, the word 'arid' is closest in meaning to:",
            targetWord = "arid",
            options = listOf(
                "Extremely dry and rainless",
                "Densely forested",
                "Politically unstable",
                "Volcanically active"
            ),
            correctIndex = 0,
            explanation = "'Arid' describes a region or climate characterized by severe lack of available water."
        ),
        AcademicReadingQuestion(
            id = "read_10",
            passage = "In his seminal treatise on optics, Ibn al-Haytham refuted the ancient emission theory of vision, demonstrating through camera obscura experiments that visual perception occurs when light rays enter the eye from external objects.",
            question = "In the passage, the word 'refuted' is closest in meaning to:",
            targetWord = "refuted",
            options = listOf(
                "Disproved with evidence",
                "Translated into Latin",
                "Enthusiastically endorsed",
                "Summarized briefly"
            ),
            correctIndex = 0,
            explanation = "'Refuted' means proved a statement or theory to be wrong or false."
        ),
        AcademicReadingQuestion(
            id = "read_11",
            passage = "Although synthetic fertilizers dramatically boosted global cereal yields during the mid-twentieth century, excessive nitrogen runoff has inadvertently caused widespread eutrophication in coastal estuaries.",
            question = "In the passage, the word 'inadvertently' is closest in meaning to:",
            targetWord = "inadvertently",
            options = listOf(
                "Unintentionally or accidentally",
                "Deliberately and systematically",
                "Profitably and efficiently",
                "Secretly and illegally"
            ),
            correctIndex = 0,
            explanation = "'Inadvertently' means without intention; accidentally."
        ),
        AcademicReadingQuestion(
            id = "read_12",
            passage = "Architectural historians attribute the remarkable longevity of Roman concrete harbor piers to the inclusion of volcanic pozzolana ash, which reacts with seawater to form interlocking crystalline minerals.",
            question = "In the passage, the word 'longevity' is closest in meaning to:",
            targetWord = "longevity",
            options = listOf(
                "Aesthetic elegance",
                "Long existence or durability",
                "Immense physical weight",
                "Financial expense"
            ),
            correctIndex = 1,
            explanation = "'Longevity' refers to long life, durability, or long continuance."
        )
    )

    val DISCUSSION_WRITING_PROMPTS: List<String> = listOf(
        "Academic Discussion: Online vs In-Person University Instruction.\nYour professor asks: 'Do digital learning environments enhance or diminish academic collaboration among undergraduates?' Write a short academic post (2-3 sentences) utilizing the target word naturally.",
        "Academic Discussion: Environmental Resource Allocation.\nYour professor asks: 'How should municipalities prioritize investments in renewable energy infrastructure versus public healthcare?' Express your viewpoint integrating the target word.",
        "Academic Discussion: Artificial Intelligence in Higher Education.\nYour professor asks: 'Does automated AI assessment support or undermine independent critical thinking?' Formulate an academic argument using the target word.",
        "Academic Discussion: Urban Congestion & Public Transit.\nYour professor asks: 'Should metropolitan governments impose congestion pricing on private vehicles or subsidize free commuter rail?' Defend your policy stance using the target word.",
        "Academic Discussion: Space Exploration vs Deep-Sea Research.\nYour professor asks: 'Should national science foundations allocate greater funding to planetary probes or oceanic biodiversity mapping?' Write a scholarly response incorporating the target word.",
        "Academic Discussion: Biodiversity Conservation & Economic Growth.\nYour professor asks: 'Can developing nations expand industrial agriculture without compromising tropical forest ecosystems?' Present a balanced academic argument using the target word.",
        "Academic Discussion: Universal Basic Income & Automation.\nYour professor asks: 'As robotics transform manufacturing and service sectors, what fiscal mechanisms best protect displaced workers?' Use the target word in your analysis.",
        "Academic Discussion: Cultural Heritage Preservation.\nYour professor asks: 'When historic districts face severe housing shortages, should cities relax zoning laws or strictly preserve heritage architecture?' Respond using the target word.",
        "Academic Discussion: Scientific Peer Review & Open Access.\nYour professor asks: 'Should all publicly funded scientific research be immediately published in open-access repositories without subscription barriers?' Integrate the target word into your post.",
        "Academic Discussion: Standardized Testing in University Admissions.\nYour professor asks: 'Do standardized entrance examinations provide an objective measure of academic readiness, or do they reinforce socioeconomic disparities?' Use the target word.",
        "Academic Discussion: Genetic Engineering in Agriculture.\nYour professor asks: 'To what extent should drought-resistant genetically modified crops be deployed to combat global food insecurity?' Formulate your response with the target word.",
        "Academic Discussion:Corporate Social Responsibility vs Regulation.\nYour professor asks: 'Are voluntary corporate sustainability pledges sufficient to reduce carbon emissions, or are binding government mandates indispensable?' Include the target word."
    )

    /**
     * Generates an authentic TOEFL 2026 "Complete the Words" (Cloze) exercise
     * for ANY of the 1,212 vocabulary entries (#1–#1212).
     */
    fun buildClozeForEntry(entry: VocabEntry): CompleteWordsExercise {
        val enriched = AcademicVocabularyEnricher.enrich(entry)
        val cleanWord = entry.headword.trim()
        val obscured = obscureWordForToefl2026(cleanWord)

        val baseSentence = when {
            entry.exampleSentence.isNotBlank() && containsWholeWord(entry.exampleSentence, cleanWord) ->
                replaceWordWithObscured(entry.exampleSentence, cleanWord, obscured)
            entry.dictionaryExamples.any { containsWholeWord(it, cleanWord) } -> {
                val ex = entry.dictionaryExamples.first { containsWholeWord(it, cleanWord) }
                replaceWordWithObscured(ex, cleanWord, obscured)
            }
            else -> synthesizeClozePassage(enriched, obscured)
        }

        val posLabel = entry.grammaticalLabels.joinToString(", ").ifBlank { "Academic term" }
        val collocationTip = enriched.collocations.take(2).joinToString(", ")
        val grammarTip = enriched.grammarPatterns.firstOrNull() ?: ""

        return CompleteWordsExercise(
            id = "cloze_entry_${entry.entryNumber}",
            passageTitle = "Entry #${entry.entryNumber} (Page ${entry.sourcePage}) • ${posLabel.uppercase()}",
            textWithBlanks = baseSentence,
            targetWord = cleanWord,
            obscuredWord = obscured,
            hint = "[$posLabel] ${entry.primaryMeaning}" +
                    if (collocationTip.isNotBlank()) " • Collocations: $collocationTip" else "",
            explanation = buildString {
                append("'${cleanWord}' (#${entry.entryNumber}, PDF p.${entry.sourcePage}) means \"${entry.primaryMeaning}\". ")
                if (grammarTip.isNotBlank()) append("Grammar pattern: $grammarTip. ")
                if (enriched.commonMistakes.isNotBlank()) append("Note: ${enriched.commonMistakes}")
            }
        )
    }

    /**
     * Generates a TOEFL "Academic Reading Vocabulary-in-Context" question
     * for ANY of the 1,212 vocabulary entries (#1–#1212), using deterministic
     * POS-matched distractors from the 1,212 dataset.
     */
    fun buildReadingQuestionForEntry(
        entry: VocabEntry,
        allEntries: List<VocabEntry>
    ): AcademicReadingQuestion {
        val enriched = AcademicVocabularyEnricher.enrich(entry)
        val cleanWord = entry.headword.trim()
        val passage = buildAcademicPassageForReading(enriched)

        val primaryPos = entry.grammaticalLabels.firstOrNull()?.lowercase()
        val samePosPool = allEntries.filter {
            it.entryNumber != entry.entryNumber &&
                    it.primaryMeaning.isNotBlank() &&
                    !it.primaryMeaning.equals(entry.primaryMeaning, ignoreCase = true) &&
                    (primaryPos == null || it.grammaticalLabels.any { lbl -> lbl.equals(primaryPos, ignoreCase = true) })
        }.ifEmpty {
            allEntries.filter { it.entryNumber != entry.entryNumber && it.primaryMeaning.isNotBlank() }
        }

        val distractorMeanings = if (samePosPool.size >= 3) {
            val step = (samePosPool.size / 4).coerceAtLeast(1)
            val seed = abs(entry.entryNumber * 31 + entry.sourcePage * 7)
            listOf(
                samePosPool[(seed) % samePosPool.size].primaryMeaning,
                samePosPool[(seed + step) % samePosPool.size].primaryMeaning,
                samePosPool[(seed + step * 2) % samePosPool.size].primaryMeaning
            ).distinct()
        } else {
            emptyList()
        }.toMutableList()

        val fallbackDistractors = listOf(
            "temporarily suspended without empirical verification",
            "confined exclusively to informal vernacular discourse",
            "entirely independent of environmental or structural constraints",
            "artificially ornamental and devoid of functional utility"
        )
        var fbIdx = 0
        while (distractorMeanings.size < 3) {
            val candidate = fallbackDistractors[fbIdx % fallbackDistractors.size]
            if (candidate !in distractorMeanings && !candidate.equals(entry.primaryMeaning, ignoreCase = true)) {
                distractorMeanings.add(candidate)
            }
            fbIdx++
        }

        val targetMeaning = entry.primaryMeaning.ifBlank {
            entry.definitions.firstOrNull() ?: "essential academic concept"
        }

        // Deterministic option order based on entryNumber
        val correctSlot = entry.entryNumber % 4
        val options = MutableList(4) { "" }
        options[correctSlot] = targetMeaning
        var dIdx = 0
        for (i in 0 until 4) {
            if (i != correctSlot) {
                options[i] = distractorMeanings[dIdx++]
            }
        }

        val familyInfo = if (enriched.wordFamily.isNotEmpty()) {
            " Word family: ${enriched.wordFamily.take(4).joinToString(", ")}."
        } else ""

        return AcademicReadingQuestion(
            id = "read_entry_${entry.entryNumber}",
            passage = passage,
            question = "In the passage, the word '${cleanWord}' (Entry #${entry.entryNumber}, Page ${entry.sourcePage}) is closest in meaning to:",
            targetWord = cleanWord,
            options = options,
            correctIndex = correctSlot,
            explanation = "'${cleanWord}' means \"$targetMeaning\".${familyInfo} ${enriched.commonMistakes}"
        )
    }

    /**
     * Returns an Academic Discussion prompt tailored to a specific entry (#1–#1212).
     */
    fun buildDiscussionPromptForEntry(entry: VocabEntry): String {
        val enriched = AcademicVocabularyEnricher.enrich(entry)
        val basePrompt = DISCUSSION_WRITING_PROMPTS[entry.entryNumber % DISCUSSION_WRITING_PROMPTS.size]
        val collocations = enriched.collocations.take(3).joinToString(" • ")
        val pattern = enriched.grammarPatterns.firstOrNull() ?: ""
        return buildString {
            append(basePrompt)
            append("\n\nTarget Word: #${entry.entryNumber} '${entry.headword}' (PDF Page ${entry.sourcePage})")
            append("\nMeaning: ${entry.primaryMeaning}")
            if (collocations.isNotBlank()) append("\nSuggested Collocations: $collocations")
            if (pattern.isNotBlank()) append("\nSyntax Pattern: $pattern")
        }
    }

    fun obscureWordForToefl2026(word: String): String {
        val trimmed = word.trim()
        if (trimmed.length <= 2) return trimmed.first() + "_"
        if (trimmed.length <= 4) {
            return trimmed.first() + "_".repeat(trimmed.length - 2) + trimmed.last()
        }
        val prefixLen = if (trimmed.length >= 8) 3 else 2
        val suffixLen = 1
        val hiddenCount = (trimmed.length - prefixLen - suffixLen).coerceAtLeast(1)
        return trimmed.substring(0, prefixLen) + "_".repeat(hiddenCount) + trimmed.takeLast(suffixLen)
    }

    private fun containsWholeWord(sentence: String, word: String): Boolean {
        val regex = Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE)
        return regex.containsMatchIn(sentence)
    }

    private fun replaceWordWithObscured(sentence: String, word: String, obscured: String): String {
        val regex = Regex("\\b${Regex.escape(word)}\\b", RegexOption.IGNORE_CASE)
        return regex.replaceFirst(sentence, obscured)
    }

    private fun synthesizeClozePassage(entry: VocabEntry, obscured: String): String {
        val pos = entry.grammaticalLabels.firstOrNull()?.lowercase() ?: ""
        return when {
            pos.contains("v") ->
                "In peer-reviewed interdisciplinary studies, researchers frequently $obscured key variables to evaluate whether theoretical models align with empirical observations (${entry.primaryMeaning})."
            pos.contains("adj") ->
                "Contemporary scholars emphasize that maintaining a $obscured methodological framework (${entry.primaryMeaning}) is essential when interpreting longitudinal datasets."
            pos.contains("adv") ->
                "Controlled laboratory trials demonstrated that the catalyst reacted $obscured (${entry.primaryMeaning}) under elevated atmospheric pressure."
            else ->
                "University seminars on comparative analysis examine how $obscured (${entry.primaryMeaning}) influences long-term institutional and ecological outcomes."
        }
    }

    private fun buildAcademicPassageForReading(entry: VocabEntry): String {
        val coreSentence = when {
            entry.exampleSentence.isNotBlank() -> entry.exampleSentence
            entry.dictionaryExamples.isNotEmpty() -> entry.dictionaryExamples.first()
            else -> "In longitudinal university studies, researchers observed how ${entry.headword} (${entry.primaryMeaning}) functions within complex analytical systems."
        }
        val contextLead = "Across contemporary university seminars and peer-reviewed monographs, scholars examine how key conceptual terms operate in empirical contexts. "
        val contextTail = if (entry.collocations.isNotEmpty()) {
            " Specifically, specialists frequently analyze patterns such as '${entry.collocations.first()}' when evaluating competing hypotheses."
        } else {
            " Consequently, understanding the precise nuance of this term is vital for interpreting advanced academic literature."
        }
        return contextLead + coreSentence + contextTail
    }
}
