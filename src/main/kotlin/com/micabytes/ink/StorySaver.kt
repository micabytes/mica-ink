package com.micabytes.ink

import com.fasterxml.jackson.core.JsonGenerator
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal

object StorySaver {

  @Throws(IOException::class)
  fun saveStream(g: JsonGenerator, story: Story) {
    g.writeStartObject()
    g.writeFieldName(StoryJson.FILES)
    g.writeStartArray()
    for (s in story.fileNames) {
      g.writeString(s)
    }
    g.writeEndArray()
    g.writeFieldName(StoryJson.CONTENT)
    g.writeStartObject()
    for ((id, c) in story.content) {
      if (c.count > 0) {
        g.writeFieldName(c.id)
        g.writeStartObject()
        g.writeNumberField(StoryJson.COUNT, c.count)
        if (c is Container && c.index > 0) {
          g.writeNumberField(StoryJson.INDEX, c.index)
        }
        if (c is ParameterizedContainer) {
          if (c.values.isNotEmpty()) {
            g.writeFieldName(StoryJson.VARIABLES)
            g.writeStartObject()
            for ((key, value) in c.values) {
              saveObject(g, story, key, value)
            }
            g.writeEndObject()
          }
        }
        g.writeEndObject()
      }
    }
    g.writeEndObject()
    g.writeStringField(StoryJson.CONTAINER, story.container.id)
    g.writeFieldName(StoryJson.TEXT)
    g.writeStartArray()
    for (s in story.text) {
      g.writeString(s)
    }
    g.writeEndArray()
    g.writeFieldName(StoryJson.CHOICES)
    g.writeStartArray()
    for (choice in story.choices) {
      g.writeString(choice.id)
    }
    g.writeEndArray()
    //if (image != null)
    //  g.writeStringField(StoryJson.IMAGE, image)
    g.writeFieldName(StoryJson.VARIABLES)
    g.writeStartObject()
    for ((key, value) in story.variables) {
      saveObject(g, story, key, value)
    }
    g.writeEndObject()
    g.writeEndObject()
  }

  @Throws(IOException::class)
  private fun saveObject(g: JsonGenerator, story: Story, key: String, value: Any) {
    when (value) {
      is Boolean -> {
        g.writeBooleanField(key, (value as Boolean?)!!)
        return
      }
      is BigDecimal -> {
        g.writeNumberField(key, value as BigDecimal?)
        return
      }
      is String -> {
        g.writeStringField(key, value as String?)
        return
      }
      else -> {
        val valClass = value.javaClass
        try {
          val m = valClass.getMethod("getId", null)
          val id = m.invoke(value, null)
          g.writeStringField(key, id as String)
        } catch (e: IllegalAccessException) {
          story.wrapper.logError("SaveObject: Could not save " + key + ": " + value + ". Not Boolean, Number, String and not an Object. " + e.message)
        } catch (e: IllegalArgumentException) {
          story.wrapper.logError("SaveObject: Could not save " + key + ": " + value + ". Not Boolean, Number, String and not an Object. " + e.message)
        } catch (e: SecurityException) {
          story.wrapper.logError("SaveObject: Could not save " + key + ": " + value + ". Not Boolean, Number, String and not an Object. " + e.message)
        } catch (e: InvocationTargetException) {
          story.wrapper.logError("SaveObject: Could not save " + key + ": " + value + ". Not Boolean, Number, String and not an Object. " + e.message)
        } catch (e: NoSuchMethodException) {
          story.wrapper.logError("SaveObject: Could not save " + key + ": " + value + ". Not Boolean, Number, String and not an Object. " + e.message)
        }
      }
    }
  }

}