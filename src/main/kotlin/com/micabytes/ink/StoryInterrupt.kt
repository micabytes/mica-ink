package com.micabytes.ink


interface StoryInterrupt {

    val id: String

    val interrupt: String

    val interruptCondition: String

    val interruptFile: String

    val isChoice: Boolean

    val isDivert: Boolean

    val isActive: Boolean

    fun done()

}
