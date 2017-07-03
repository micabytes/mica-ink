package com.micabytes.ink

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.micabytes.ink.util.InkLoadingException
import java.io.IOException
import java.math.BigDecimal

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

  fun addInterrupt(text: StoryInterrupt) {
    interrupts.add(text)
    val fileId = text.file
    for (f in fileNames) {
      if (f == fileId) return  // No need to add the data again.
    }
    if (text.isChoice) {
      val choice = Choice(text.id, 0, text.text, null)
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
  */

  @Throws(IOException::class)
  fun loadStream(p: JsonParser, story: Story) {
    while (p.nextToken() != JsonToken.END_OBJECT) {
      when (p.currentName) {
        StoryJson.CONTENT -> {
          p.nextToken() // START_OBJECT
          while (p.nextToken() != JsonToken.END_OBJECT) {
            val cid = p.currentName
            val content = story.content[cid]
            p.nextToken() // START_OBJECT
            while (p.nextToken() != JsonToken.END_OBJECT) {
              when (p.currentName) {
                StoryJson.COUNT -> content?.count = p.nextIntValue(0)
                StoryJson.INDEX -> if (content != null && content is Container)
                  content.index = p.nextIntValue(0)
                StoryJson.VARIABLES -> {
                  p.nextToken() // START_OBJECT
                  val pContainer = content as ParameterizedContainer?
                  while (p.nextToken() != JsonToken.END_OBJECT) {
                    val varName = p.currentName
                    val obj = loadObjectStream(p, story)
                    if (obj != null)
                      pContainer?.values!!.put(varName, obj)
                  }
                }
                else -> {
                }
              }// The way this is used in P&T2, this is not actually an error.
              // wrapper.logException(new InkLoadingException("Attempting to write COUNT " + Integer.toString(p.nextIntValue(0)) + " to children " + cid + "."));
            }
          }
        }
        StoryJson.CONTAINER -> story.container = story.content[p.nextTextValue()] as Container
        StoryJson.TEXT -> {
          p.nextToken() // START_ARRAY
          while (p.nextToken() != JsonToken.END_ARRAY) {
            story.text.add(p.text)
          }
        }
        StoryJson.CHOICES -> {
          p.nextToken() // START_ARRAY
          while (p.nextToken() != JsonToken.END_ARRAY) {
            val cnt = story.content[p.text]
            if (cnt is Choice)
              story.choices.add(cnt as Container)
            else story.wrapper.logException(InkLoadingException(p.text + " is not a choice"))
          }
        }
        //StoryJson.IMAGE -> image = p.nextTextValue()
        StoryJson.VARIABLES -> {
          p.nextToken() // START_OBJECT
          while (p.nextToken() != JsonToken.END_OBJECT) {
            val varName = p.currentName
            val obj = loadObjectStream(p, story)
            if (obj != null) {
              story.variables.put(varName, obj)
            }
          }
        }
        //StoryJson.RUNNING -> running = p.nextBooleanValue()!!
        else -> {
        }
      }
    }
  }

  @Throws(IOException::class)
  private fun loadObjectStream(p: JsonParser, story: Story): Any? {
    val token = p.nextToken()
    if (token == JsonToken.VALUE_NULL)
      return null
    if (token.isBoolean)
      return p.booleanValue
    if (token.isNumeric)
      return BigDecimal(p.text)
    val str = p.text
    val obj = story.wrapper.getStoryObject(str)
    return obj
  }

}