package com.micabytes.ink

import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import com.fasterxml.jackson.core.JsonGenerator

object StorySaver {

  @SuppressWarnings("OverlyComplexMethod", "OverlyNestedMethod")
  @Throws(IOException::class)
  fun saveStream(g: JsonGenerator) {
    /*
    g.writeStartObject()
    g.writeFieldName(StoryJson.FILES)
    g.writeStartArray()
    for (s in fileNames) {
      g.writeString(s)
    }
    g.writeEndArray()
    g.writeFieldName(StoryJson.CONTENT)
    g.writeStartObject()
    for ((key1, content1) in storyContent) {
      if (content.count > 0) {
        g.writeFieldName(content.id)
        g.writeStartObject()
        g.writeNumberField(StoryJson.COUNT, content.count)
        if (content is ParameterizedContainer) {
          if (content.getVariables() != null) {
            g.writeFieldName(StoryJson.VARIABLES)
            g.writeStartObject()
            for ((key, value) in content.getVariables()!!) {
              saveObject(g, key, value)
              if (value == null) {
                wrapper.logDebug("Wrote a null value for " + key)
              }
            }
            g.writeEndObject()
          }
        }
        g.writeEndObject()
      }
    }
    g.writeEndObject()
    if (container != null)
      g.writeStringField(StoryJson.CONTAINER, container!!.id)
    g.writeNumberField(StoryJson.COUNTER, contentIdx)
    g.writeFieldName(StoryJson.TEXT)
    g.writeStartArray()
    for (s in text) {
      g.writeString(s)
    }
    g.writeEndArray()
    g.writeFieldName(StoryJson.CHOICES)
    g.writeStartArray()
    for (choice in choices) {
      g.writeString(choice.id)
    }
    g.writeEndArray()
    if (image != null)
      g.writeStringField(StoryJson.IMAGE, image)
    g.writeFieldName(StoryJson.VARIABLES)
    g.writeStartObject()
    for ((key, value) in variables) {
      if (value != null) {
        saveObject(g, key, value)
      } else {
        saveObject(g, key, null)
      }
    }
    g.writeEndObject()
    g.writeBooleanField(StoryJson.RUNNING, running)
    g.writeEndObject()
    */
  }

  @SuppressWarnings("rawtypes", "unchecked", "NullArgumentToVariableArgMethod")
  @Throws(IOException::class)
  private fun saveObject(g: JsonGenerator, key: String, `val`: Any?) {
    /*
    if (`val` == null) {
      g.writeNullField(key)
      return
    }
    if (`val` is Boolean) {
      g.writeBooleanField(key, (`val` as Boolean?)!!)
      return
    }
    if (`val` is BigDecimal) {
      g.writeNumberField(key, `val` as BigDecimal?)
      return
    }
    if (`val` is String) {
      g.writeStringField(key, `val` as String?)
      return
    }
    val valClass = `val`.javaClass
    try {
      val m = valClass.getMethod(Story.GET_ID, null)
      val id = m.invoke(`val`, null)
      g.writeStringField(key, id as String)
    } catch (e: IllegalAccessException) {
      wrapper.logError("SaveObject: Could not save " + key + ": " + `val` + ". Not Boolean, Number, String and not an Object. " + e.message)
    } catch (e: IllegalArgumentException) {
      wrapper.logError("SaveObject: Could not save " + key + ": " + `val` + ". Not Boolean, Number, String and not an Object. " + e.message)
    } catch (e: SecurityException) {
      wrapper.logError("SaveObject: Could not save " + key + ": " + `val` + ". Not Boolean, Number, String and not an Object. " + e.message)
    } catch (e: InvocationTargetException) {
      wrapper.logError("SaveObject: Could not save " + key + ": " + `val` + ". Not Boolean, Number, String and not an Object. " + e.message)
    } catch (e: NoSuchMethodException) {
      wrapper.logError("SaveObject: Could not save " + key + ": " + `val` + ". Not Boolean, Number, String and not an Object. " + e.message)
    }
    */
  }

}