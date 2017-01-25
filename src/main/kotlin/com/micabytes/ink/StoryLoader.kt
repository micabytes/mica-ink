package com.micabytes.ink

import java.io.IOException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

object StoryLoader {

  /*
  init {
    fileNames.add(fileName)
  }


  @Throws(InkParseException::class)
  fun include(fileName: String) {
    if (!fileNames.contains(fileName)) {
      val st = InkParser.parse(wrapper.getStream(fileName), wrapper, fileName)
      addAll(st)
    }
  }

  fun addInterrupt(interrupt: StoryInterrupt) {
    interrupts.add(interrupt)
    val fileId = interrupt.interruptFile
    for (f in fileNames) {
      if (f == fileId) return  // No need to add the data again.
    }
    if (interrupt.isChoice) {
      val choice = Choice(interrupt.id, 0, interrupt.interrupt, null)
      storyContent.put(choice.id, choice)
    }
    if (fileId != null) {
      try {
        val st = InkParser.parse(wrapper.getStream(fileId), wrapper, fileId)
        addAll(st)
      } catch (e: InkParseException) {
        wrapper.logException(e)
      }

    }
  }

  //@SuppressWarnings("OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod", "NestedSwitchStatement")
  @Throws(IOException::class)
  fun loadStream(p: JsonParser) {
    while (p.nextToken() != JsonToken.END_OBJECT) {
      when (p.currentName) {
        StoryJson.CONTENT -> {
          p.nextToken() // START_OBJECT
          while (p.nextToken() != JsonToken.END_OBJECT) {
            val cid = p.currentName
            val content = storyContent[cid]
            p.nextToken() // START_OBJECT
            while (p.nextToken() != JsonToken.END_OBJECT) {
              when (p.currentName) {
                StoryJson.COUNT -> if (content != null)
                  content.count = p.nextIntValue(0)
                StoryJson.VARIABLES -> {
                  p.nextToken() // START_OBJECT
                  val pContainer = content as ParameterizedContainer?
                  while (p.nextToken() != JsonToken.END_OBJECT) {
                    val varName = p.currentName
                    val obj = loadObjectStream(p)
                    if (pContainer != null && pContainer.getVariables() != null)
                      pContainer.getVariables()!!.put(varName, obj)
                  }
                }
                else -> {
                }
              }// The way this is used in P&T2, this is not actually an error.
              // wrapper.logException(new InkLoadingException("Attempting to write COUNT " + Integer.toString(p.nextIntValue(0)) + " to children " + cid + "."));
            }
          }
        }
        StoryJson.CONTAINER -> container = storyContent[p.nextTextValue()] as Container
        StoryJson.COUNTER -> contentIdx = p.nextIntValue(0)
        StoryJson.TEXT -> {
          p.nextToken() // START_ARRAY
          while (p.nextToken() != JsonToken.END_ARRAY) {
            text.add(p.text)
          }
        }
        StoryJson.CHOICES -> {
          p.nextToken() // START_ARRAY
          while (p.nextToken() != JsonToken.END_ARRAY) {
            val cnt = storyContent[p.text]
            if (cnt is Choice)
              choices.add(cnt as Container)
            else wrapper?.logException(InkLoadingException(p.text + " is not a choice"))
          }
        }
        StoryJson.IMAGE -> image = p.nextTextValue()
        StoryJson.VARIABLES -> {
          p.nextToken() // START_OBJECT
          while (p.nextToken() != JsonToken.END_OBJECT) {
            val varName = p.currentName
            val obj = loadObjectStream(p)
            values.put(varName, obj)
          }
        }
        StoryJson.RUNNING -> running = p.nextBooleanValue()!!
        else -> {
        }
      }
    }
  }

  @Throws(IOException::class)
  private fun loadObjectStream(p: JsonParser): Any? {
    val token = p.nextToken()
    if (token == JsonToken.VALUE_NULL)
      return null
    if (token.isBoolean)
      return p.booleanValue
    if (token.isNumeric)
      return BigDecimal(p.text)
    val str = p.text
    val obj = wrapper.getStoryObject(str) ?: return str
    return obj
  }
  */

}