package com.example.util

import com.example.data.model.ConfusablePair
import com.example.data.model.VocabEntry

object ConfusableWordsData {

    val PAIRS: List<ConfusablePair> = listOf(
        ConfusablePair(
            id = "cp_discrete_discreet",
            wordA = "discrete",
            wordB = "discreet",
            distinction = "'Discrete' means separate, distinct, or detached. 'Discreet' means cautious, prudent, or maintaining privacy.",
            exampleA = "The speech signals were broken down into discrete acoustic frequencies.",
            exampleB = "The diplomat made discreet inquiries before making an official announcement.",
            exerciseSentence = "In computer science, algorithms often operate on ________ units of information rather than continuous values.",
            correctWord = "discrete",
            explanation = "Because units are separate and distinct, 'discrete' is correct. 'Discreet' refers to being tactful or private."
        ),
        ConfusablePair(
            id = "cp_comprise_compose",
            wordA = "comprise",
            wordB = "compose",
            distinction = "The whole comprises the parts ('The university comprises five colleges'). Parts compose the whole ('Five colleges compose the university').",
            exampleA = "The solar system comprises eight recognized planets and numerous celestial bodies.",
            exampleB = "Nitrogen and oxygen compose nearly ninety-nine percent of Earth's atmosphere.",
            exerciseSentence = "Marine biologists established that the coral reef community ________ hundreds of interdependent micro-organisms.",
            correctWord = "comprises",
            explanation = "'Comprises' is correct because the singular whole (community) contains the smaller parts."
        ),
        ConfusablePair(
            id = "cp_elicit_illicit",
            wordA = "elicit",
            wordB = "illicit",
            distinction = "'Elicit' is a verb meaning to draw out or evoke. 'Illicit' is an adjective meaning illegal, forbidden, or unauthorized.",
            exampleA = "The psychological test was designed to elicit natural emotional reactions.",
            exampleB = "The authorities seized a large shipment of illicit wildlife products at the border.",
            exerciseSentence = "The lecturer posed thought-provoking questions to ________ lively debate among the graduate students.",
            correctWord = "elicit",
            explanation = "We need an infinitive verb meaning 'to draw forth response/debate', which is 'elicit'. 'Illicit' is an adjective meaning illegal."
        ),
        ConfusablePair(
            id = "cp_complement_compliment",
            wordA = "complement",
            wordB = "compliment",
            distinction = "'Complement' means to add to in a way that completes or enhances. 'Compliment' means an expression of praise or admiration.",
            exampleA = "Empirical field studies complement theoretical laboratory models.",
            exampleB = "The dean paid the guest speaker a sincere compliment regarding her groundbreaking monograph.",
            exerciseSentence = "Qualitative interviews were conducted to ________ the quantitative statistical data in the mixed-methods study.",
            correctWord = "complement",
            explanation = "The interviews complete or enhance the data, so 'complement' is the correct term."
        ),
        ConfusablePair(
            id = "cp_precede_proceed",
            wordA = "precede",
            wordB = "proceed",
            distinction = "'Precede' means to come or occur before in time or order. 'Proceed' means to advance, move forward, or continue.",
            exampleA = "A period of heavy volcanic activity preceded the global cooling event.",
            exampleB = "The committee decided to proceed with the proposed curriculum restructuring.",
            exerciseSentence = "Extensive archaeological surveys must ________ any major construction within historical heritage zones.",
            correctWord = "precede",
            explanation = "The surveys must take place before construction begins, so 'precede' is required."
        ),
        ConfusablePair(
            id = "cp_principal_principle",
            wordA = "principal",
            wordB = "principle",
            distinction = "'Principal' means the head of a school, or primary/main. 'Principle' is a noun meaning a fundamental law, doctrine, or moral rule.",
            exampleA = "Deforestation is the principal cause of habitat destruction in the tropical basin.",
            exampleB = "Newton's third law illustrates the principle of action and reaction.",
            exerciseSentence = "The ________ researcher was awarded a competitive federal grant for cancer immunotherapy.",
            correctWord = "principal",
            explanation = "'Principal' serves as an adjective meaning 'chief' or 'primary' researcher."
        ),
        ConfusablePair(
            id = "cp_imply_infer",
            wordA = "imply",
            wordB = "infer",
            distinction = "The speaker or text implies (suggests indirectly); the listener or reader infers (draws a conclusion).",
            exampleA = "The author's tone implies a skeptical attitude toward the newly proposed policy.",
            exampleB = "From the carbon-isotope ratios, geochemists can infer ancient climatic conditions.",
            exerciseSentence = "Readers of the historical document can readily ________ that civil unrest was escalating during that decade.",
            correctWord = "infer",
            explanation = "The reader is deducing or drawing the conclusion from the text, so 'infer' is correct."
        ),
        ConfusablePair(
            id = "cp_economic_economical",
            wordA = "economic",
            wordB = "economical",
            distinction = "'Economic' relates to trade, industry, and money. 'Economical' means cost-effective, thrifty, or avoiding waste.",
            exampleA = "The region experienced an unprecedented period of rapid economic expansion.",
            exampleB = "Electric vehicles provide an economical alternative to traditional combustion engines.",
            exerciseSentence = "The government implemented monetary reforms to stimulate sustainable ________ growth.",
            correctWord = "economic",
            explanation = "Relating to the general financial system of a country or market requires 'economic'."
        ),
        ConfusablePair(
            id = "cp_continuous_continual",
            wordA = "continuous",
            wordB = "continual",
            distinction = "'Continuous' means without any interruption in space or time. 'Continual' means occurring repeatedly with short intervals in between.",
            exampleA = "The patient was kept under continuous physiological monitoring throughout the surgical procedure.",
            exampleB = "The laboratory experienced continual equipment recalibrations throughout the trial.",
            exerciseSentence = "Deep oceanic currents maintain a ________ flow of thermal energy across planetary latitudes.",
            correctWord = "continuous",
            explanation = "The flow of current is uninterrupted, making 'continuous' the accurate scientific descriptor."
        ),
        ConfusablePair(
            id = "cp_adverse_averse",
            wordA = "adverse",
            wordB = "averse",
            distinction = "'Adverse' means unfavorable, hostile, or detrimental. 'Averse' describes a person feeling a strong disinclination or reluctance ('risk-averse').",
            exampleA = "Prolonged droughts created adverse ecological conditions across the savannah.",
            exampleB = "Institutional investors proved averse to backing experimental biotechnology startups.",
            exerciseSentence = "Clinical trials revealed that the experimental compound produced minimal ________ side effects in patients.",
            correctWord = "adverse",
            explanation = "Unfavorable side effects are described with 'adverse'."
        ),
        ConfusablePair(
            id = "cp_affect_effect",
            wordA = "affect",
            wordB = "effect",
            distinction = "'Affect' is primarily a verb meaning to influence or alter. 'Effect' is primarily a noun meaning a result, or a formal verb meaning to bring about ('effect change').",
            exampleA = "Ocean acidification directly affects the calcifying rates of reef-building corals.",
            exampleB = "The greenhouse effect regulates surface temperatures across terrestrial planets.",
            exerciseSentence = "Fluctuations in solar irradiance significantly ________ upper-atmospheric ozone concentrations.",
            correctWord = "affect",
            explanation = "A plural verb meaning 'influence' is required here, so 'affect' is correct."
        ),
        ConfusablePair(
            id = "cp_allude_elude",
            wordA = "allude",
            wordB = "elude",
            distinction = "'Allude' (with 'to') means to refer indirectly or hint at. 'Elude' means to escape from, evade, or fail to be understood by.",
            exampleA = "Renaissance poets frequently allude to classical Greco-Roman mythology.",
            exampleB = "A unified theory of quantum gravity continues to elude theoretical physicists.",
            exerciseSentence = "In her keynote address, the historian briefly chose to ________ to earlier archival discoveries.",
            correctWord = "allude",
            explanation = "Followed by 'to', meaning to reference indirectly, 'allude' is the right choice."
        ),
        ConfusablePair(
            id = "cp_eminent_imminent",
            wordA = "eminent",
            wordB = "imminent",
            distinction = "'Eminent' means famous, respected, and distinguished within a sphere. 'Imminent' means about to happen very soon.",
            exampleA = "An eminent panel of seismologists reviewed the fault-line telemetry.",
            exampleB = "Harmonic tremors beneath the crater signaled that a volcanic eruption was imminent.",
            exerciseSentence = "Rapid barometric drops warned meteorologists that a severe cyclonic storm was ________.",
            correctWord = "imminent",
            explanation = "Because the storm is about to occur immediately, 'imminent' is required."
        ),
        ConfusablePair(
            id = "cp_ingenious_ingenuous",
            wordA = "ingenious",
            wordB = "ingenuous",
            distinction = "'Ingenious' means clever, original, and inventive. 'Ingenuous' means innocent, unsuspecting, or candidly sincere.",
            exampleA = "Archimedes devised an ingenious hydraulic screw for raising irrigation water.",
            exampleB = "The chronicler's ingenuous account lacked the cynical bias of court historians.",
            exerciseSentence = "Engineers developed an ________ passive cooling system inspired by subterranean termite mounds.",
            correctWord = "ingenious",
            explanation = "Describing a clever and inventive engineering design requires 'ingenious'."
        ),
        ConfusablePair(
            id = "cp_prescribe_proscribe",
            wordA = "prescribe",
            wordB = "proscribe",
            distinction = "'Prescribe' means to recommend, authorize, or lay down a rule. 'Proscribe' means to forbid, ban, or outlaw by authority.",
            exampleA = "International treaties prescribe strict safety protocols for nuclear reactors.",
            exampleB = "The conservation statute seeks to proscribe commercial trawling in marine sanctuaries.",
            exerciseSentence = "In normative linguistics, traditional grammars attempt to ________ how speakers ought to construct sentences.",
            correctWord = "prescribe",
            explanation = "Setting down authoritative rules for usage is 'prescribe' (as in prescriptive grammar)."
        ),
        ConfusablePair(
            id = "cp_mitigate_militate",
            wordA = "mitigate",
            wordB = "militate",
            distinction = "'Mitigate' means to make less severe or alleviate. 'Militate' (used with 'against') means to be a powerful factor preventing something.",
            exampleA = "Coastal mangrove restoration helps mitigate storm-surge erosion.",
            exampleB = "Extreme aridity and nutrient-poor soils militate against intensive agriculture in the region.",
            exerciseSentence = "Urban green roofs are installed to ________ the urban heat-island phenomenon.",
            correctWord = "mitigate",
            explanation = "To lessen or alleviate the severity of heat is 'mitigate'."
        ),
        ConfusablePair(
            id = "cp_exacerbate_exaggerate",
            wordA = "exacerbate",
            wordB = "exaggerate",
            distinction = "'Exacerbate' means to make an existing problem or condition worse. 'Exaggerate' means to represent something as larger or worse than it actually is.",
            exampleA = "Prolonged timber harvesting will exacerbate soil erosion along steep watersheds.",
            exampleB = "Early explorers tended to exaggerate the perils of navigating polar latitudes.",
            exerciseSentence = "Deforestation along riverbanks can severely ________ seasonal downstream flooding.",
            correctWord = "exacerbate",
            explanation = "Making an actual physical condition (flooding) worse is 'exacerbate'."
        ),
        ConfusablePair(
            id = "cp_persecute_prosecute",
            wordA = "persecute",
            wordB = "prosecute",
            distinction = "'Persecute' means to subject someone to hostility and ill-treatment. 'Prosecute' means to institute legal proceedings against someone, or to carry on a trade/war.",
            exampleA = "Authoritarian regimes frequently persecute dissenting intellectuals.",
            exampleB = "Regulatory agencies are empowered to prosecute corporations that violate environmental laws.",
            exerciseSentence = "The district attorney gathered forensic evidence to ________ the financial fraud case.",
            correctWord = "prosecute",
            explanation = "Conducting legal proceedings in court is 'prosecute'."
        ),
        ConfusablePair(
            id = "cp_disinterested_uninterested",
            wordA = "disinterested",
            wordB = "uninterested",
            distinction = "'Disinterested' means impartial, unbiased, and having no personal stake. 'Uninterested' means bored or lacking interest.",
            exampleA = "Peer review relies on the judgment of disinterested scientific referees.",
            exampleB = "The audience grew uninterested during the overly technical recitation of tables.",
            exerciseSentence = "A judge must remain completely ________ when arbitrating commercial contract disputes.",
            correctWord = "disinterested",
            explanation = "Impartiality and lack of bias are expressed by 'disinterested'."
        ),
        ConfusablePair(
            id = "cp_historic_historical",
            wordA = "historic",
            wordB = "historical",
            distinction = "'Historic' means famous, momentous, or influential in history. 'Historical' means relating to the past or the study of history.",
            exampleA = "The signing of the peace accord was a historic milestone for regional stability.",
            exampleB = "Demographic historians analyze historical census records from the eighteenth century.",
            exerciseSentence = "The cartographer compared ________ maps of the coastline with modern satellite imagery.",
            correctWord = "historical",
            explanation = "Maps originating from the past are 'historical' documents."
        ),
        ConfusablePair(
            id = "cp_emigrate_immigrate",
            wordA = "emigrate",
            wordB = "immigrate",
            distinction = "'Emigrate' (from) means to leave one's own country to settle in another. 'Immigrate' (to/into) means to come to live permanently in a foreign country.",
            exampleA = "Millions of agrarian workers chose to emigrate from Europe during the nineteenth century.",
            exampleB = "Skilled engineers continued to immigrate to industrializing metropolitan centers.",
            exerciseSentence = "Crop failures compelled thousands of families to ________ from the drought-stricken province.",
            correctWord = "emigrate",
            explanation = "Paired with 'from' (leaving a place of origin), 'emigrate' is required."
        ),
        ConfusablePair(
            id = "cp_ensure_assure",
            wordA = "ensure",
            wordB = "assure",
            distinction = "'Ensure' means to make certain that something will occur. 'Assure' means to tell someone positively to dispel doubt.",
            exampleA = "Double-blind experimental protocols ensure that observer bias is minimized.",
            exampleB = "The project director was able to assure the committee that safety standards were met.",
            exerciseSentence = "Strict calibration procedures ________ the reproducibility of spectroscopic measurements.",
            correctWord = "ensure",
            explanation = "Making certain of an outcome (reproducibility) requires 'ensure'."
        ),
        ConfusablePair(
            id = "cp_adapt_adopt",
            wordA = "adapt",
            wordB = "adopt",
            distinction = "'Adapt' means to modify or adjust to new conditions. 'Adopt' means to take up, accept, or formally approve a method or policy.",
            exampleA = "Alpine plants adapt to high ultraviolet radiation by producing protective pigments.",
            exampleB = "The standardization board voted to adopt the metric measurement protocol.",
            exerciseSentence = "Desert mammals have evolved physiological mechanisms to ________ to extreme water scarcity.",
            correctWord = "adapt",
            explanation = "Adjusting biologically to environmental conditions (followed by 'to') is 'adapt'."
        ),
        ConfusablePair(
            id = "cp_Stationary_stationery",
            wordA = "stationary",
            wordB = "stationery",
            distinction = "'Stationary' (with 'a') is an adjective meaning not moving or fixed. 'Stationery' (with 'e') is a noun meaning writing paper and office materials.",
            exampleA = "Geostationary satellites remain over a stationary point above the equator.",
            exampleB = "Archival letters were preserved on watermarked eighteenth-century stationery.",
            exerciseSentence = "Tidal turbines generate electricity as water flows past ________ rotor blades anchored to the seabed.",
            correctWord = "stationary",
            explanation = "Fixed, non-moving blades are 'stationary'."
        )
    )

    /**
     * Dynamically constructs contrast & discrimination pairs across all 1,212 vocabulary entries!
     * Groups entries by POS, orthographic similarity, or duplicate/anomaly flags so that
     * every single word (#1–#1212) can be studied and quizzed in Confusable Words Lab.
     */
    fun build1212ContrastPairs(
        entries: List<VocabEntry>,
        pageFilter: Int? = null,
        anomaliesOnly: Boolean = false
    ): List<ConfusablePair> {
        if (entries.isEmpty()) return PAIRS

        val filtered = when {
            anomaliesOnly -> entries.filter { it.hasAnomaly || it.hasDuplicateHeadword }
            pageFilter != null -> entries.filter { it.sourcePage == pageFilter }
            else -> entries
        }.sortedBy { it.entryNumber }

        if (filtered.isEmpty()) return PAIRS

        val byNumber = entries.associateBy { it.entryNumber }

        return filtered.map { entry ->
            // Pick the best contrast partner for this entry:
            // 1. If it has a duplicate partner in the PDF, contrast the two entries (#A vs #B)
            // 2. Otherwise find an entry with the same initial letter & POS or adjacent entry on the same page
            val partner: VocabEntry = entry.duplicateWithNumbers
                .firstNotNullOfOrNull { byNumber[it] }
                ?: entries.firstOrNull {
                    it.entryNumber != entry.entryNumber &&
                            it.headword.take(3).equals(entry.headword.take(3), ignoreCase = true) &&
                            !it.headword.equals(entry.headword, ignoreCase = true)
                }
                ?: entries.firstOrNull {
                    it.entryNumber != entry.entryNumber &&
                            it.sourcePage == entry.sourcePage &&
                            it.grammaticalLabels.intersect(entry.grammaticalLabels.toSet()).isNotEmpty()
                }
                ?: entries.firstOrNull { it.entryNumber != entry.entryNumber }
                ?: entry

            val wordALabel = if (entry.headword.equals(partner.headword, ignoreCase = true)) {
                "${entry.headword} (#${entry.entryNumber})"
            } else {
                entry.headword
            }
            val wordBLabel = if (entry.headword.equals(partner.headword, ignoreCase = true)) {
                "${partner.headword} (#${partner.entryNumber})"
            } else {
                partner.headword
            }

            val posA = entry.grammaticalLabels.joinToString("/").ifBlank { "term" }
            val posB = partner.grammaticalLabels.joinToString("/").ifBlank { "term" }

            val distinctionText = buildString {
                append("Entry #${entry.entryNumber} (p.${entry.sourcePage}) '$wordALabel' [$posA] means \"${entry.primaryMeaning}\". ")
                append("In contrast, Entry #${partner.entryNumber} (p.${partner.sourcePage}) '$wordBLabel' [$posB] means \"${partner.primaryMeaning}\".")
                if (entry.hasAnomaly && entry.anomalyNotes.isNotBlank()) {
                    append(" • Source Audit Note: ${entry.anomalyNotes}")
                }
            }

            val exA = entry.exampleSentence.ifBlank {
                "In academic discourse, '${entry.headword}' denotes ${entry.primaryMeaning}."
            }
            val exB = partner.exampleSentence.ifBlank {
                "In scholarly literature, '${partner.headword}' refers to ${partner.primaryMeaning}."
            }

            val blankedSentence = if (entry.exampleSentence.isNotBlank() &&
                Regex("\\b${Regex.escape(entry.headword)}\\b", RegexOption.IGNORE_CASE).containsMatchIn(entry.exampleSentence)
            ) {
                Regex("\\b${Regex.escape(entry.headword)}\\b", RegexOption.IGNORE_CASE)
                    .replaceFirst(entry.exampleSentence, "________") +
                        " (Clue: ${entry.primaryMeaning})"
            } else {
                "In university research, scholars use the [$posA] term ________ to express '${entry.primaryMeaning}'."
            }

            ConfusablePair(
                id = "dyn_1212_${entry.entryNumber}_vs_${partner.entryNumber}",
                wordA = wordALabel,
                wordB = wordBLabel,
                distinction = distinctionText,
                exampleA = exA,
                exampleB = exB,
                exerciseSentence = blankedSentence,
                correctWord = wordALabel,
                explanation = "'$wordALabel' (#${entry.entryNumber}, Page ${entry.sourcePage}) means \"${entry.primaryMeaning}\", whereas '$wordBLabel' (#${partner.entryNumber}) means \"${partner.primaryMeaning}\"."
            )
        }
    }
}
