package com.micabytes.ink

import java.math.BigDecimal
import java.util.*

class Story(internal val wrapper: StoryWrapper, fileName: String, internal var container: Container, internal val content: HashMap<String, Content>) : VariableMap {
  // Story Content
  private val fileNames: MutableList<String> = ArrayList()
  //private val content = HashMap<String, Content>()
  private val functions = TreeMap<String, Function>(String.CASE_INSENSITIVE_ORDER)
  private val interrupts = ArrayList<StoryInterrupt>()
  private val storyEnd = Container(0, "", null)
  // Story state
  //private var container: Container? = null
  private var containerIdx: Int = 0
  private var currentText = Symbol.GLUE
  private val text: MutableList<String> = ArrayList()
  private val choices = ArrayList<Container>()
  private val variables = HashMap<String, Any>()
  //private val comments = ArrayList<Comment>()
  //private var image: String? = null
  //private var processing: Boolean = false
  //private var running: Boolean = false

  init {
    fileNames.add(fileName)
    variables.put(Variable.TRUE_UC, BigDecimal.ONE)
    variables.put(Variable.FALSE_UC, BigDecimal.ZERO)
    functions.put(IS_NULL, NullFunction())
    functions.put(GET_NULL, GetNullFunction())
    functions.put("not", NotFunction())
    functions.put(RANDOM, RandomFunction())
    functions.put(IS_KNOT, IsKnotFunction())
    functions.put(FLOOR, FloorFunction())
  }

  fun add(story: Story) {
    // TODO: Need to handle name collisions
    content.putAll(story.content)
    functions.putAll(story.functions)
    variables.putAll(story.variables)
    story.fileNames
        .filterNot { fileNames.contains(it) }
        .forEach { fileNames.add(it) }
  }

  private operator fun hasNext(): Boolean {
    /*
    if (!processing) return false
    if (container == null)
      return false
    return containerIdx < container!!.size
    */
    return true
  }

  @Throws(InkRunTimeException::class)
  fun next(): List<String> {
    if (!hasNext())
      throw InkRunTimeException("Did you forget to run canContinue()?")
    currentText = Symbol.GLUE
    while (hasNext() && container.size > containerIdx) {
      val current = container.get(containerIdx)
      when (current) {
        is Choice -> {
          if (current.evaluateConditions(this))
            choices.add(current)
          containerIdx++
        }
      //is Comment -> comments.add(current)
      //is Variable -> content.evaluate(this)
        is Divert -> {
          container = current.resolveDivert(this)
          containerIdx = 0
        }
      // is ..
      // is Tunnel
        else -> {
          addText(current)
          containerIdx++
        }
      }
    }
    if (!currentText.isEmpty()) {
      text.add(cleanUpText(currentText))
    }
    return text
  }

  fun addText(current: Content) {
    val nextText = current.getText(this)
    if (currentText.endsWith(Symbol.GLUE) || nextText.startsWith(Symbol.GLUE))
      currentText += nextText
    else {
      text.add(cleanUpText(currentText))
      currentText = nextText
    }
  }

  @Throws(InkRunTimeException::class)
  fun choose(i: Int) {
    if (i < choices.size && i >= 0) {
      container = choices[i]
      container.count++
      //completeExtras(container)
      containerIdx = 0
      choices.clear()
    } else {
      val cId = if (container != null) container!!.id else "null"
      throw InkRunTimeException("Trying to select a choice " + i + " that does not exist in story: " + fileNames[0] + " container: " + cId + " cIndex: " + containerIdx)
    }
  }

  val choiceSize: Int
    get() = choices.size

  @Throws(InkRunTimeException::class)
  fun choiceText(i: Int): String {
    if (i >= choices.size || i < 0)
      throw InkRunTimeException("Trying to retrieve a choice " + i + " that does not exist in story: " + fileNames[0] + " container: " + container.id + " cIndex: " + containerIdx)
    return (choices[i] as Choice).getText(this)
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


  //fun getChoice(i: Int): Choice {
  //  return choices[i] as Choice
  //}
    /*
    incrementContent()
    if (content.type == ContentType.TEXT) {
      val ret = if (content is Divert) resolveDivert(content) else
      content.increment()
      return ret
    }
    //ret += resolveContent(current)
    //incrementContent(content)
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
    */
       /*
    if (container != null && container!!.background != null) {
      image = container!!.background
    }
    if (!hasNext()) {
      resolveExtras()
    }
    //return cleanUpText(ret)
  //var cntent: Content = content
  //var endOfLine = false

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
      containerIdx = 0
      choices.clear()
      return
    }
    if (content != null && content is Conditional) {
      val nextContainer = content as Container?
      nextContainer!!.initialize(this, content)
      container = nextContainer
      containerIdx = 0
      return
    }
    containerIdx++
    if (container != null && containerIdx >= container!!.size) {
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
              containerIdx = 0
              choices.clear()
              return
            }
            if (n is Choice && container !is Gather) {
              container = p
              containerIdx = i
              choices.clear()
              return
            }
            i++
          }
          c = p
          p = c.parent
        }
        container = null
        containerIdx = 0
        choices.clear()
        return
      }
      if (container is Conditional) {
        val oldContainer = container
        container = oldContainer!!.parent
        containerIdx = if (container != null) container!!.indexOf(oldContainer) + 1 else 0
      }
    } else {
      val next = content
      if (next != null && next is FallbackChoice && choices.isEmpty()) {
        val nextContainer = next as Container?
        nextContainer!!.initialize(this, content)
        container = nextContainer
        containerIdx = 0
        choices.clear()
        return
      }
      if (next != null && next is Conditional) {
        val nextContainer = next as Container?
        nextContainer!!.initialize(this, content)
        container = nextContainer
        containerIdx = 0
        return
      }
      if (next != null && next is Gather) {
        processing = false
      }
    }
  }

  private val nxtcontent: Content?
    @Throws(InkRunTimeException::class)
    get() {
      if (!running)
        return null
      if (container == null)
        throw InkRunTimeException("Current text container is NULL.")
      if (containerIdx >= container!!.size)
        return null
      return container!!.get(containerIdx)
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
  */
  /*

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
        containerIdx = 0
      }
      // else nothing - this is a fallback choice and we ignore it
    } else {
      choices.add(choice)
    }
  }


  private fun completeExtras(extraContainer: Container) {
    for (interrupt in interrupts) {
      if (interrupt.id == extraContainer.id) {
        interrupt.done()
      }
    }
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


  fun getContainer(key: String): Container {
    return storyContent[key] as Container
  }

  fun setContainer(s: String) {
    val c = storyContent[s] as Container
    if (c != null)
      container = c
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
      +". ContentIdx: " + containerIdx
    }

  */

  override fun logException(e: Exception) {
    wrapper.logException(e)
  }

  override fun getValue(token: String): Any {
    if (Symbol.THIS == token)
        return container.id
    //var c : Container? = container
    /*
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
    */
    return BigDecimal.ZERO
  }

  override fun hasVariable(token: String): Boolean {
    /*
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
      */
    return variables.containsKey(token)
  }

  override fun hasFunction(token: String): Boolean {
    return functions.containsKey(token)
  }

  override fun getFunction(token: String): Function {
    if (hasFunction(token))
      return functions.get(token)!!
    return NullFunction()
    // TODO: Empty Function
  }

  override fun checkObject(token: String): Boolean {
    /*
    if (token.contains(".")) {
      return hasVariable(token.substring(0, token.indexOf(InkParser.DOT.toInt())))
    }*/
    return false
  }

  override fun debugInfo(): String {
    var ret = ""
    ret += "StoryDebugInfo File: " + fileNames
    ret += if (true) " Container :" + container.id else " Container: null"
    if (containerIdx < container.size) {
      val cnt = container.get(containerIdx)
      ret += if (true) " Line# :" + Integer.toString(cnt.lineNumber) else " Line#: ?"
    }
    return ret
  }

  private class NullFunction : Function {

    override val numParams: Int
      get() = 1

    override val isFixedNumParams: Boolean
      get() = true

    @Throws(InkRunTimeException::class)
    override fun eval(params: List<Any>, vmap: VariableMap): Any {
      //val param = params[0]
      //return param == null
      return false
    }
  }

  private class GetNullFunction : Function {

    override val numParams: Int
      get() = 0

    override val isFixedNumParams: Boolean
      get() = true

    @Throws(InkRunTimeException::class)
    override fun eval(params: List<Any>, vmap: VariableMap): Any {
      return false
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
      //val param = params[0]
      //if (param is String && vmap.getValue(Symbol.THIS) != null) {
      //  return param == vmap.getValue(Symbol.THIS)
      //}
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
    private val IS_NULL = "isNull"
    private val GET_NULL = "getNull"
    private val RANDOM = "random"
    private val IS_KNOT = "isKnot"
    private val FLOOR = "floor"

    private fun cleanUpText(str: String): String {
      return str.replace(Symbol.GLUE.toRegex(), " ") // clean up glue
          .replace("\\s+".toRegex(), " ") // clean up white space
          .trim({ it <= ' ' })
    }

    //private val CURRENT_BACKGROUND = "currentBackground"

    /*
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


    private fun isContainerEmpty(c: Container): Boolean {
      return c.size == 0
    }
    */
  }

}


/*
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
*/
