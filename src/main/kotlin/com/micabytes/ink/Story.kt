package com.micabytes.ink

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode

import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.math.BigDecimal
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.Random
import java.util.TreeMap

class Story(internal val wrapper: StoryWrapper, fileName: String) : VariableMap {
  // All the children in the story
  private val storyContent = HashMap<String, Content>()
  // All defined functions with name and implementation.
  private val functions = TreeMap<String, Function>(String.CASE_INSENSITIVE_ORDER)
  // All story interrupts currently active
  private val interrupts = ArrayList<StoryInterrupt>()

  // Story state
  private val fileNames: MutableList<String> = ArrayList()
  private val storyEnd = Container(0, "", null)
  private var container: Container? = null
  private var contentIdx: Int = 0
  private val text: MutableList<String> = ArrayList()
  private val choices = ArrayList<Container>()
  private val comments = ArrayList<Comment>()
  private var image: String? = null
  private val variables = HashMap<String, Any>()
  private var processing: Boolean = false
  private var running: Boolean = false


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

  @SuppressWarnings("OverlyComplexMethod", "OverlyNestedMethod")
  @Throws(IOException::class)
  fun saveStream(g: JsonGenerator) {
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
  }

  @SuppressWarnings("rawtypes", "unchecked", "NullArgumentToVariableArgMethod")
  @Throws(IOException::class)
  private fun saveObject(g: JsonGenerator, key: String, `val`: Any?) {
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
      val m = valClass.getMethod(GET_ID, null)
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
            variables.put(varName, obj)
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

  fun addAll(story: Story) {
    // TODO: Need to handle name collisions
    storyContent.putAll(story.storyContent)
    functions.putAll(story.functions)
    variables.putAll(story.variables)
    for (s in story.fileNames) {
      if (!fileNames.contains(s))
        fileNames.add(s)
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

  internal fun initialize() {
    variables.put(Variable.TRUE_UC, BigDecimal.ONE)
    variables.put(Variable.FALSE_UC, BigDecimal.ZERO)
    if (container != null && container!!.type == ContentType.KNOT) {
      if (container!!.get(0) is Stitch)
        container = container!!.get(0) as Container
    }
    contentIdx = -1
    running = true
    processing = true
    try {
      incrementContent(null)
    } catch (e: InkRunTimeException) {
      logException(e)
    }
    functions.put(IS_NULL, NullFunction())
    functions.put(GET_NULL, GetNullFunction())
    functions.put("not", NotFunction())
    functions.put(RANDOM, RandomFunction())
    functions.put(IS_KNOT, IsKnotFunction())
    functions.put(FLOOR, FloorFunction())
  }

  @Throws(InkRunTimeException::class)
  fun nextAll(): List<String> {
    text.clear()
    val ret = ArrayList<String>()
    while (hasNext()) {
      val txt = next()
      if (!txt.isEmpty())
        ret.add(txt)
    }
    text.addAll(ret)
    return ret
  }

  private operator fun hasNext(): Boolean {
    if (!processing) return false
    if (container == null)
      return false
    return contentIdx < container!!.size
  }

  @SuppressWarnings("NonConstantStringShouldBeStringBuffer", "OverlyComplexMethod", "StringConcatenationInLoop")
  @Throws(InkRunTimeException::class)
  private operator fun next(): String {
    if (!hasNext())
      throw InkRunTimeException("Did you forget to run canContinue()?")
    processing = true
    var ret = Symbol.GLUE
    var content: Content = content
    var endOfLine = false
    while (!endOfLine) {
      endOfLine = true
      ret += resolveContent(content)
      incrementContent(content)
      if (container != storyEnd) {
        val nextContent = content
        if (nextContent != null) {
          if (nextContent.text.startsWith(Symbol.GLUE))
            endOfLine = false
          if (nextContent is Choice && !nextContent.isFallbackChoice)
            endOfLine = false
          if (nextContent is Stitch)
            endOfLine = false
          if (nextContent.type == ContentType.TEXT && nextContent.text.startsWith(Symbol.DIVERT)) {
            val divertTo = getDivertTarget(nextContent)
            if (divertTo != null && divertTo.get(0).text.startsWith(Symbol.GLUE))
              endOfLine = false
          }
        }
        if (ret.endsWith(Symbol.GLUE) && nextContent != null)
          endOfLine = false
        content = nextContent
      }
    }
    if (container != null && container!!.background != null) {
      image = container!!.background
    }
    if (!hasNext()) {
      resolveExtras()
    }
    return cleanUpText(ret)
  }

  private fun resolveExtras() {
    for (interrupt in interrupts) {
      if (interrupt.isActive && interrupt.isChoice) {
        val cond = interrupt.interruptCondition
        try {
          val res = Variable.evaluate(cond, this)
          if (checkResult(res)) {
            val choice = storyContent[interrupt.id] as Choice
            if (choice.evaluateConditions(this)) {
              choices.add(0, choice)
            }
          }
        } catch (e: InkRunTimeException) {
          wrapper.logException(e)
        }

      }
    }
  }

  @SuppressWarnings("OverlyComplexMethod", "OverlyLongMethod")
  @Throws(InkRunTimeException::class)
  private fun incrementContent(content: Content?) {
    if (content != null && content.isDivert) {
      val divertTarget = getDivertTarget(content)
      if (divertTarget != null)
        divertTarget.initialize(this, content)
      else
        running = false
      container = divertTarget
      contentIdx = 0
      choices.clear()
      return
    }
    if (content != null && content is Conditional) {
      val nextContainer = content as Container?
      nextContainer!!.initialize(this, content)
      container = nextContainer
      contentIdx = 0
      return
    }
    contentIdx++
    if (container != null && contentIdx >= container!!.size) {
      if (choices.isEmpty() && (container is Choice || container is Gather)) {
        var c: Container = container
        var p = c.parent
        while (p != null) {
          var i = p.indexOf(c) + 1
          while (i < p.size) {
            val n = p.get(i)
            if (n is Gather) {
              val newContainer = n as Container
              newContainer.initialize(this, content)
              container = newContainer
              contentIdx = 0
              choices.clear()
              return
            }
            if (n is Choice && container !is Gather) {
              container = p
              contentIdx = i
              choices.clear()
              return
            }
            i++
          }
          c = p
          p = c.parent
        }
        container = null
        contentIdx = 0
        choices.clear()
        return
      }
      if (container is Conditional) {
        val oldContainer = container
        container = oldContainer!!.parent
        contentIdx = if (container != null) container!!.indexOf(oldContainer) + 1 else 0
      }
    } else {
      val next = content
      if (next != null && next is FallbackChoice && choices.isEmpty()) {
        val nextContainer = next as Container?
        nextContainer!!.initialize(this, content)
        container = nextContainer
        contentIdx = 0
        choices.clear()
        return
      }
      if (next != null && next is Conditional) {
        val nextContainer = next as Container?
        nextContainer!!.initialize(this, content)
        container = nextContainer
        contentIdx = 0
        return
      }
      if (next != null && next is Gather) {
        processing = false
      }
    }
  }

  private val content: Content?
    @Throws(InkRunTimeException::class)
    get() {
      if (!running)
        return null
      if (container == null)
        throw InkRunTimeException("Current text container is NULL.")
      if (contentIdx >= container!!.size)
        return null
      return container!!.get(contentIdx)
    }

  @SuppressWarnings("ChainOfInstanceofChecks")
  @Throws(InkRunTimeException::class)
  private fun resolveContent(content: Content): String {
    if (content.type == ContentType.TEXT) {
      val ret = if (content is Divert) resolveDivert(content) else StoryText.getText(content.text, content.count, this)
      content.increment()
      return ret
    }
    if (content is Choice) {
      addChoice(content as Choice)
    }
    if (content is Comment) {
      comments.add(content)
    }
    if (content is Variable)
      content.evaluate(this)
    return ""
  }

  private fun resolveDivert(content: Content): String {
    var ret = StoryText.getText(content.text, content.count, this)
    ret = ret.substring(0, ret.indexOf(Symbol.DIVERT)).trim({ it <= ' ' })
    ret += Symbol.GLUE
    return ret
  }

  @SuppressWarnings("OverlyComplexMethod")
  @Throws(InkRunTimeException::class)
  private fun getDivertTarget(content: Content): Container? {
    var d = content.text.substring(content.text.indexOf(Symbol.DIVERT) + 2).trim({ it <= ' ' })
    if (d.contains(StoryText.BRACE_LEFT))
      d = d.substring(0, d.indexOf(StoryText.BRACE_LEFT))
    if (d == Symbol.DIVERT_END)
      return null
    d = resolveInterrupt(d)
    var divertTo: Container? = storyContent[d] as Container
    if (divertTo == null) {
      val fd = getFullId(d)
      divertTo = storyContent[fd] as Container
      if (divertTo == null) {
        if (variables.containsKey(d))
          divertTo = variables[d] as Container
        if (divertTo == null)
          throw InkRunTimeException("Attempt to divert to non-defined " + d + " or " + fd + " in line " + content.lineNumber)
      }
    }
    // TODO: This needs to be rewritten for proper functioning of parameters (parameters on a knot with a base stitch)
    if (divertTo.type == ContentType.KNOT && divertTo.get(0) is Stitch)
      divertTo = divertTo.get(0) as Container
    if ((divertTo.type == ContentType.KNOT || divertTo.type == ContentType.STITCH) && divertTo.get(0) is Conditional)
      divertTo = divertTo.get(0) as Container
    // TODO: Should increment for each run through?
    return divertTo
  }

  @SuppressWarnings("OverlyNestedMethod")
  private fun resolveInterrupt(divert: String): String {
    for (interrupt in interrupts) {
      if (interrupt.isActive && interrupt.isDivert) {
        val cond = interrupt.interruptCondition
        try {
          val res = Variable.evaluate(cond, this)
          if (checkResult(res)) {
            val interruptText = interrupt.interrupt
            if (interruptText.contains(Symbol.DIVERT)) {
              val from = interruptText.substring(0, interruptText.indexOf(Symbol.DIVERT)).trim({ it <= ' ' })
              if (from == divert) {
                val to = interruptText.substring(interruptText.indexOf(Symbol.DIVERT) + 2).trim({ it <= ' ' })
                interrupt.done()
                putVariable(Symbol.EVENT, interrupt)
                return to
              }
            }
          }
        } catch (e: InkRunTimeException) {
          wrapper.logException(e)
          return divert
        }

      }
    }
    return divert
  }

  private fun getFullId(id: String): String {
    if (id == Symbol.DIVERT_END)
      return id
    if (id.contains(InkParser.DOT.toString()))
      return id
    val p = if (container != null) container!!.parent else null
    return if (p != null) p.id + InkParser.DOT + id else id
  }

  @Throws(InkRunTimeException::class)
  private fun addChoice(choice: Choice) {
    // Check conditions
    if (!choice.evaluateConditions(this))
      return
    // Resolve
    if (choice.getChoiceText(this).isEmpty()) {
      if (choices.isEmpty()) {
        container = choice
        contentIdx = 0
      }
      // else nothing - this is a fallback choice and we ignore it
    } else {
      choices.add(choice)
    }
  }

  @Throws(InkRunTimeException::class)
  fun choose(i: Int) {
    if (i < choices.size && i >= 0) {
      val old = container
      container = choices[i]
      if (container == null) {
        val oldId = if (old != null) old.id else "null"
        throw InkRunTimeException("Selected choice $i is null in $oldId and $contentIdx")
      }
      container!!.count++
      completeExtras(container)
      contentIdx = 0
      choices.clear()
      processing = true
    } else {
      val cId = if (container != null) container!!.id else "null"
      throw InkRunTimeException("Trying to select a choice " + i + " that does not exist in story: " + fileNames[0] + " container: " + cId + " cIndex: " + contentIdx)
    }
  }

  val choiceSize: Int
    get() = choices.size

  fun getChoice(i: Int): Choice {
    return choices[i] as Choice
  }

  private fun completeExtras(extraContainer: Container) {
    for (interrupt in interrupts) {
      if (interrupt.id == extraContainer.id) {
        interrupt.done()
      }
    }
  }

  @SuppressWarnings("OverlyComplexMethod")
  override fun getValue(token: String): Any {
    if (Symbol.THIS == token) {
      if (container != null)
        return container!!.id
      else {
        wrapper.logError("Attempting to invoke this with null container in " + fileNames[0])
        return ""
      }
    }
    var c = container
    while (c != null) {
      if (c is Knot || c is Function || c is Stitch) {
        if ((c as ParameterizedContainer).hasValue(token))
          return c.getValue(token)
      }
      c = c.parent
    }
    if (token.startsWith(Symbol.DIVERT)) {
      val k = token.substring(2).trim({ it <= ' ' })
      if (storyContent.containsKey(k))
        return storyContent[k]
      wrapper.logException(InkRunTimeException("Could not identify container id: " + k))
      return BigDecimal.ZERO
    }
    if (storyContent.containsKey(token)) {
      val storyContainer = storyContent[token] as Container
      return BigDecimal.valueOf(storyContainer.count.toLong())
    }
    val pathId = getValueId(token)
    if (storyContent.containsKey(pathId)) {
      val storyContainer = storyContent[pathId] as Container
      return BigDecimal.valueOf(storyContainer.count.toLong())
    }
    if (variables.containsKey(token)) {
      return variables[token]
    }
    wrapper.logException(InkRunTimeException("Could not identify the variable $token or $pathId"))
    return BigDecimal.ZERO
  }

  private fun getValueId(id: String): String {
    if (id == Symbol.DIVERT_END)
      return id
    if (id.contains(InkParser.DOT.toString()))
      return id
    return if (container != null) container!!.id + InkParser.DOT + id else id
  }

  @Throws(InkParseException::class)
  internal fun add(content: Content) {
    if (content.id != null) {
      if (storyContent.containsKey(content.id)) {
        throw InkParseException("Invalid container ID. Two containers may not have the same ID")
      }
      if (content is Function)
        functions.put(content.id, content as Function)
      else
        storyContent.put(content.id, content)
    } else {
      // Should not be possible.
      throw InkParseException("No ID for children. This should not be possible.")
    }
    // Set starting knot
    if (content is Knot && (container == null || isContainerEmpty(container!!)))
      container = content as Container
  }

  val isEnded: Boolean
    get() = container == null

  override fun hasVariable(token: String): Boolean {
    if (Character.isDigit(token[0]))
      return false
    var c = container
    while (c != null) {
      if (c is Knot || c is Function || c is Stitch) {
        if ((c as ParameterizedContainer).hasValue(token))
          return true
      }
      c = c.parent
    }
    if (storyContent.containsKey(token))
      return true
    if (storyContent.containsKey(getValueId(token)))
      return true
    return variables.containsKey(token)
  }

  fun putVariable(key: String, value: Any) {
    var c = container
    while (c != null) {
      if (c is Knot || c is Function || c is Stitch) {
        if ((c as ParameterizedContainer).hasValue(key)) {
          c.setValue(key, value)
          return
        }
      }
      c = c.parent
    }
    variables.put(key, value)
  }

  fun getContainer(key: String): Container {
    return storyContent[key] as Container
  }

  override fun hasFunction(token: String): Boolean {
    return functions.containsKey(token)
  }

  override fun getFunction(token: String): Function {
    return functions.get(token)
  }

  override fun checkObject(token: String): Boolean {
    if (token.contains(".")) {
      return hasVariable(token.substring(0, token.indexOf(InkParser.DOT.toInt())))
    }
    return false
  }

  override fun debugInfo(): String {
    var ret = ""
    ret += "StoryDebugInfo File: " + fileNames
    ret += if (container != null) " Container :" + container!!.id else " Container: null"
    if (container != null && contentIdx < container!!.size) {
      val cnt = container!!.get(contentIdx)
      ret += if (cnt != null) " Line# :" + Integer.toString(cnt.lineNumber) else " Line#: ?"
    }
    return ret
  }

  fun setContainer(s: String) {
    val c = storyContent[s] as Container
    if (c != null)
      container = c
  }

  override fun logException(e: Exception) {
    wrapper?.logException(e)
  }

  // Legacy function used to set text
  @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
  fun setText(storyText: MutableList<String>) {
    text = storyText
  }

  fun getText(): List<String> {
    return Collections.unmodifiableList(text)
  }

  val comment: String
    @Throws(InkRunTimeException::class)
    get() {
      val cIt = comments.iterator()
      while (cIt.hasNext()) {
        val c = cIt.next()
        if (c.evaluateConditions(this)) {
          val ret = c.getCommentText(this)
          if (c.type == ContentType.COMMENT_ONCE)
            cIt.remove()
          return ret
        }
      }
      return ""
    }

  val storyStatus: String
    get() {
      val fileId = fileNames.toString()
      val contId = if (container != null) container!!.id else "null"
      return "Story errors with FileName: " + fileId
      +". Container: " + contId
      +". ContentIdx: " + contentIdx
    }

  private class NullFunction : Function {

    override val numParams: Int
      get() = 1

    override val isFixedNumParams: Boolean
      get() = true

    @Throws(InkRunTimeException::class)
    override fun eval(params: List<Any>, vmap: VariableMap): Any {
      val param = params[0]
      return param == null
    }
  }

  private class GetNullFunction : Function {

    override val numParams: Int
      get() = 0

    override val isFixedNumParams: Boolean
      get() = true

    @Throws(InkRunTimeException::class)
    override fun eval(params: List<Any>, vmap: VariableMap): Any? {
      return null
    }
  }


  class NotFunction : Function {

    override val numParams: Int
      get() = 1

    override val isFixedNumParams: Boolean
      get() = true

    @Throws(InkRunTimeException::class)
    override fun eval(params: List<Any>, vmap: VariableMap): Any {
      val param = params[0]
      if (param is Boolean)
        return !param
      if (param is BigDecimal)
        return if (param.toInt() == 0) java.lang.Boolean.TRUE else java.lang.Boolean.FALSE
      return java.lang.Boolean.FALSE
    }
  }

  private class RandomFunction : Function {

    override val numParams: Int
      get() = 1

    override val isFixedNumParams: Boolean
      get() = true

    @Throws(InkRunTimeException::class)
    override fun eval(params: List<Any>, vmap: VariableMap): Any {
      val param = params[0]
      if (param is BigDecimal) {
        val `val` = param.toInt()
        if (`val` <= 0) return BigDecimal.ZERO
        return BigDecimal(Random().nextInt(`val`))
      }
      return BigDecimal.ZERO
    }
  }

  private class IsKnotFunction : Function {

    override val numParams: Int
      get() = 1

    override val isFixedNumParams: Boolean
      get() = true

    @Throws(InkRunTimeException::class)
    override fun eval(params: List<Any>, vmap: VariableMap): Any {
      val param = params[0]
      if (param is String && vmap.getValue(Symbol.THIS) != null) {
        return param == vmap.getValue(Symbol.THIS)
      }
      return java.lang.Boolean.FALSE
    }
  }

  private class FloorFunction : Function {

    override val numParams: Int
      get() = 1

    override val isFixedNumParams: Boolean
      get() = true

    @Throws(InkRunTimeException::class)
    override fun eval(params: List<Any>, vmap: VariableMap): Any {
      val param = params[0]
      if (param is BigDecimal) {
        return BigDecimal.valueOf(param.toInt().toLong())
      }
      return BigDecimal.ZERO
    }
  }

  companion object {
    private val GET_ID = "getId"
    private val IS_NULL = "isNull"
    private val GET_NULL = "getNull"
    private val RANDOM = "random"
    private val IS_KNOT = "isKnot"
    private val FLOOR = "floor"
    private val CURRENT_BACKGROUND = "currentBackground"

    private fun loadObject(v: JsonNode, provider: StoryWrapper): Any? {
      val node = v.get("val")
      if (node != null) {
        if (node.isBoolean)
          return node.asBoolean()
        if (node.isInt)
          return BigDecimal.valueOf(node.asInt().toLong())
        if (node.isDouble)
          return BigDecimal.valueOf(node.asDouble())
        val obj = provider.getStoryObject(node.asText()) ?: return node.asText()
        return obj
      }
      return null
    }

    private fun checkResult(res: Any?): Boolean {
      if (res == null)
        return false
      if (res is Boolean && (res as Boolean?)!!)
        return true
      return res is BigDecimal && res.toInt() > 0
    }

    private fun cleanUpText(str: String): String {
      return str.replace(Symbol.GLUE.toRegex(), " ") // clean up glue
          .replace("\\s+".toRegex(), " ") // clean up white space
          .trim({ it <= ' ' })
    }

    private fun isContainerEmpty(c: Container): Boolean {
      return c.size == 0
    }
  }

}