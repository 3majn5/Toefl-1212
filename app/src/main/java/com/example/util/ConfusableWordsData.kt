package com.example.util

import com.example.data.model.ConfusablePair

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
        )
    )
}
