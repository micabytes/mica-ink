package com.micabytes.ink

import com.micabytes.ink.util.InkRunTimeException
import java.math.BigDecimal
import java.util.*

class Story(internal val wrapper: StoryWrapper,
            fileName: String,
            internal var container: Container,
            internal val content: HashMap<String, Content>) : VariableMap {
  // Story Content
  internal val fileNames: MutableList<String> = ArrayList()
  private val interrupts = ArrayList<StoryInterrupt>()
  private val storyEnd = Knot("== END ==", 0)
  private var endProcessing = false
  private var currentText = Symbol.GLUE
  val text: MutableList<String> = ArrayList()
  val choices = ArrayList<Container>()
  internal val variables = HashMap<String, Any>()
  private val functions = TreeMap<String, Function>(String.CASE_INSENSITIVE_ORDER)


  init {
    fileNames.add(fileName)
    content[storyEnd.id] = storyEnd
    for (cnt in content) {
      if (cnt.value is Knot && (cnt.value as Knot).isFunction)
        functions[cnt.value.id.toLowerCase(Locale.US)] = cnt.value as Knot
    }
    putVariable(Symbol.TRUE, BigDecimal.ONE)
    putVariable(Symbol.FALSE, BigDecimal.ZERO)
    putVariable(Symbol.PI, Expression.PI)
    putVariable(Symbol.e, Expression.e)
    functions[IS_NULL] = IsNullFunction()
    functions[NOT] = NotFunction()
    functions[RANDOM] = RandomFunction()
    functions[FLOOR] = FloorFunction()
    /*
    functions.put(GET_NULL, GetNullFunction())
    functions.put(IS_KNOT, IsKnotFunction())
    */
    /*
    addFunction(object : Function("SQRT", 1) {
      override fun eval(parameters: List<BigDecimal>): BigDecimal {
        val x = parameters[0]
        if (x.compareTo(BigDecimal.ZERO) == 0) {
          return BigDecimal(0)
        }
        if (x.signum() < 0) {
          throw ExpressionException(
              "Argument to SQRT() function must not be negative")
        }
        val n = x.movePointRight(mc!!.precision shl 1)
            .toBigInteger()

        val bits = n.bitLength() + 1 shr 1
        var ix = n.shiftRight(bits)
        var ixPrev: BigInteger

        do {
          ixPrev = ix
          ix = ix.add(n.divide(ix)).shiftRight(1)
          // Give other threads a chance to work;
          Thread.`yield`()
        } while (ix.compareTo(ixPrev) != 0)

        return BigDecimal(ix, mc!!.precision)
      }
    })
    */

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

  @Throws(InkRunTimeException::class)
  fun next(): List<String> {
    choices.clear()
    currentText = Symbol.GLUE
    endProcessing = false
    while (container.index >= container.size)
      increment()
    while (!endProcessing) {
      val current = container.get(container.index)
      when (current) {
        is Stitch -> {
          if (container.index == 0) {
            container.index = container.size
            container = current
            container.index = 0
            current.count++
          }
          else {
            container.index = container.size
            increment()
          }
        }
        is Choice -> {
          if (current.isFallBack()) {
            if (choices.isEmpty()) {
              // Note: This assumes that the fallback choice is the last in the choice list.
              container.index ++
              container = current
              container.index = 0
              current.count++
            } else
              increment()
          } else {
            if (current.evaluateConditions(this))
              choices.add(current)
            increment()
          }
        }
        is Gather -> {
          if (choices.size > 0)
            endProcessing = true
          else {
            container.index ++
            container = current
            container.index = 0
            current.count++
          }
        }
        is Conditional -> {
          container.index ++
          container = current.resolveConditional(this)
          current.count++
          container.index = 0
          if (container.index >= container.size)
            increment()
        }
        is Declaration -> {
          current.evaluate(this)
          increment()
        }
        is Divert -> {
          container.index ++
          container = current.resolveDivert(this)
          container.count++
          container.index = 0
        }
        is Tag -> {
          wrapper.resolveTag(current.text)
          increment()
        }
        // is Tunnel
        else -> {
          addText(current)
          current.count ++
          increment()
        }
      }
      if (container.id == storyEnd.id)
        endProcessing = true
    }
    if (!currentText.isEmpty()) {
      val txt = cleanUpText(currentText)
      if (!txt.isEmpty())
        text.add(txt)
    }
    return text
  }

  private fun increment() {
    container.index++
    while (container.index >= container.size && !endProcessing) {
      when (container) {
        is Choice -> {
          container = container.parent!!
          /*
          var p = container.parent!!
          var pIdx = p.indexOf(container) + 1
          var gatherFound = false
          while (!gatherFound) {
            while (pIdx < p.size && !gatherFound) {
              val nextContainer = p.get(pIdx)
              if (nextContainer is Gather) {
                container = p
                containerIdx = pIdx
                gatherFound = true
              } else
                pIdx++
            }
            if (!gatherFound) {
              pIdx = p.parent!!.indexOf(p) + 1
              p = p.parent!!
            }
          }
          */
        }
        is Gather -> {
          container = container.parent!!
        }
        is Conditional -> {
          container = container.parent!!
        }
        is ConditionalOption -> {
          container = container.parent!!
        }
        else -> {
          endProcessing = true
        }
      }
    }

  }

  private fun addText(current: Content) {
    val nextText = current.getText(this)
    if (currentText.endsWith(Symbol.GLUE) || nextText.startsWith(Symbol.GLUE))
      currentText += nextText
    else {
      text.add(cleanUpText(currentText))
      currentText = nextText
    }
  }

  @Throws(InkRunTimeException::class)
  fun choose(idx: Int) {
    val i = if (idx == -1) choices.size - 1 else idx
    if (i < choices.size && i >= 0) {
      container = choices[i]
      container.count++
      //completeExtras(container)
      container.index = 0
      choices.clear()
    } else {
      val cId = if (container != null) container.id else "null"
      throw InkRunTimeException("Trying to select a choice " + i + " that does not exist in story: " + fileNames[0] + " container: " + cId + " cIndex: " + container.index)
    }
  }

  val choiceSize: Int
    get() = choices.size

  @Throws(InkRunTimeException::class)
  fun choiceText(i: Int): String {
    if (i >= choices.size || i < 0)
      throw InkRunTimeException("Trying to retrieve a choice " + i + " that does not exist in story: " + fileNames[0] + " container: " + container.id + " cIndex: " + container.index)
    return (choices[i] as Choice).getText(this)
  }

  fun putVariable(key: String, value: Any) {
    var c  : Container? = container
    while (c != null) {
      if (c is Knot || c is Function || c is Stitch) {
        if ((c as ParameterizedContainer).hasValue(key)) {
          when (value) {
            is Boolean -> c.setValue(key, if (value) BigDecimal.ONE else BigDecimal.ZERO)
            is Int -> c.setValue(key, BigDecimal(value))
            else -> c.setValue(key, value)
          }
          return
        }
      }
      c = c.parent
    }
    /*if (isNumber(value))
      values.put(variable, BigDecimal(value))
    else {
      expression = expression!!.replace("(?i)\\b$variable\\b".toRegex(), "(" + value + ")")
      rpn = null
    }*/
    variables[key] = value
  }

  fun putVariables(map: Map<String, Any>) {
    variables.putAll(map)
  }
  /*
  fun with(variable: String, value: BigDecimal): Expression {
    return setVariable(variable, value)
  }

  fun and(variable: String, value: String): Expression {
    return setVariable(variable, value)
  }
  */

  val isEnded: Boolean
    get() = container.id == storyEnd.id

  // TODO: Should be allowed?
  fun setContainer(s: String) {
    container = content[s] as Container
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
for (text in interrupts) {
 if (text.isActive && text.isChoice) {
   val cond = text.condition
   try {
     val res = Declaration.evaluate(cond, this)
     if (checkResult(res)) {
       val choice = storyContent[text.id] as Choice
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
if (content is Tag) {
 comments.add(content)
}
if (content is Declaration)
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
        if (values.containsKey(d))
          divertTo = values[d] as Container
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
  }

  private fun getFullId(id: String): String {
    if (id == Symbol.DIVERT_END)
      return id
    if (id.contains(Symbol.DOT.toString()))
      return id
    val p = if (container != null) container!!.parent else null
    return if (p != null) p.id + Symbol.DOT + id else id
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
    for (text in interrupts) {
      if (text.id == extraContainer.id) {
        text.done()
      }
    }
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
    var c: Container? = container
    while (c != null) {
      if (c is ParameterizedContainer) {
        if (c.hasValue(token))
          return c.getValue(token)
      }
      c = c.parent
    }
    if (token.startsWith(Symbol.DIVERT)) {
      val k = token.substring(2).trim({ it <= ' ' })
      if (content.containsKey(k))
        return content[k]!!
      wrapper.logException(InkRunTimeException("Could not identify container id: $k"))
      return BigDecimal.ZERO
    }
    if (content.containsKey(token)) {
      val storyContainer = content[token] as Content
      return BigDecimal.valueOf(storyContainer.count.toLong())
    }
    val pathId = getValueId(token)
    if (content.containsKey(pathId)) {
      val storyContainer = content[pathId] as Content
      return BigDecimal.valueOf(storyContainer.count.toLong())
    }
    val knotId = getKnotId(token)
    if (content.containsKey(knotId)) {
      val storyContainer = content[knotId] as Content
      return BigDecimal.valueOf(storyContainer.count.toLong())
    }
    if (container is ParameterizedContainer && (container as ParameterizedContainer).hasValue(token))
      return (container as ParameterizedContainer).getValue(token)
    if (variables.containsKey(token)) {
      return variables[token]!!
    }
    wrapper.logException(InkRunTimeException("Could not identify the variable $token or $pathId"))
    return BigDecimal.ZERO
  }

  private fun getValueId(id: String): String {
    //if (id == Symbol.DIVERT_END)
    //  return id
    if (id.contains(Symbol.DOT.toString()))
      return id
    return container.id + Symbol.DOT + id
  }

  private fun getKnotId(id: String): String {
    //if (id == Symbol.DIVERT_END)
    //  return id
    if (id.contains(Symbol.DOT.toString()))
      return id
    var knot = container
    while (knot != null) {
      if (knot is Knot)
        return knot.id + Symbol.DOT + id
      knot = knot.parent!!
    }
    return id
  }

  override fun hasValue(token: String): Boolean {
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
    */
    if (functions.containsKey(token))
      return false // TODO: Need a better solution for this. Perhaps lookahead in shuntingYard
    if (content.containsKey(token))
      return true
    if (content.containsKey(getValueId(token)))
      return true
    if (content.containsKey(getKnotId(token)))
      return true
    if (container is ParameterizedContainer && (container as ParameterizedContainer).hasValue(token))
      return true
    return variables.containsKey(token)
  }

  override fun hasFunction(token: String): Boolean {
    return functions.containsKey(token.toLowerCase(Locale.US))
  }

  override fun getFunction(token: String): Function {
    if (hasFunction(token.toLowerCase(Locale.US)))
      return functions[token.toLowerCase(Locale.US)]!!
    throw RuntimeException()
    //return //NullFunction()
    // TODO: Empty Function
  }

  override fun hasGameObject(token: String): Boolean {
    if (Expression.isNumber(token))
      return false
    if (token.contains(".")) {
      return hasValue(token.substring(0, token.indexOf(Symbol.DOT)))
    }
    return false
  }

  override fun debugInfo(): String {
    var ret = ""
    ret += "StoryDebugInfo File: $fileNames"
    ret += if (true) " Container :" + container.id else " Container: null"
    if (container.index < container.size) {
      val cnt = container.get(container.index)
      ret += if (true) " Line# :" + Integer.toString(cnt.lineNumber) else " Line#: ?"
    }
    return ret
  }

  private class IsNullFunction : Function {
    override val numParams: Int = 1
    override val isFixedNumParams: Boolean = true
    override fun eval(params: List<Any>, vMap: VariableMap): Any {
      val param = params[0]
      if (param == BigDecimal.ZERO) return BigDecimal.ONE
      return BigDecimal.ZERO
    }
  }

  class NotFunction : Function {
    override val numParams: Int = 1
    override val isFixedNumParams: Boolean = true
    override fun eval(params: List<Any>, vMap: VariableMap): Any {
      val param = params[0]
      when (param) {
        is Boolean -> return !param
        is BigDecimal -> return if (param == BigDecimal.ZERO) BigDecimal.ONE else BigDecimal.ZERO
      }
      return BigDecimal.ZERO
    }
  }

  private class RandomFunction : Function {
    override val numParams: Int = 1
    override val isFixedNumParams: Boolean = true
    override fun eval(params: List<Any>, vMap: VariableMap): Any {
      val param = params[0]
      if (param is BigDecimal) {
        val v = param.toInt()
        return if (v > 0) BigDecimal(Random().nextInt(v)) else BigDecimal.ZERO
      }
      return BigDecimal.ZERO
    }
  }

  private class FloorFunction : Function {
    override val numParams: Int = 1
    override val isFixedNumParams: Boolean = true
    override fun eval(params: List<Any>, vMap: VariableMap): Any {
      val param = params[0]
      if (param is BigDecimal) {
        return BigDecimal.valueOf(param.toInt().toLong())
      }
      return BigDecimal.ZERO
    }
  }

  /*
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

  */

  fun resolveInterrupt(divert: String): String {
    for (interrupt in interrupts) {
      if (interrupt.isActive && interrupt.isDivert) {
        try {
          val res = Declaration.evaluate(interrupt.condition, this)
          if (checkResult(res)) {
            val text = interrupt.text.substring(2)
            val from = text.substring(0, text.indexOf(Symbol.DIVERT)).trim({ it <= ' ' })
            if (from == divert) {
              if (!contains(interrupt.file))
                add(InkParser.parse(wrapper, interrupt.file))
              val to = text.substring(text.indexOf(Symbol.DIVERT) + 2).trim({ it <= ' ' })
              interrupt.isActive = false
              putVariable(Symbol.EVENT, interrupt)
              return to
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

  fun addInterrupt(i: StoryInterrupt) = interrupts.add(i)

  fun clearInterrupts() = interrupts.clear()

  fun contains(s: String): Boolean = fileNames.any { it.equals(s, ignoreCase = true) }

  companion object {
    private const val IS_NULL = "isnull"
    private const val GET_NULL = "getnull"
    private const val NOT = "not"
    private const val RANDOM = "random"
    private const val IS_KNOT = "isknot"
    private const val FLOOR = "floor"

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
    */
  }

  fun getDivert(d: String): Container {
    if (content.containsKey(d))
      return content[d] as Container
    if (content.containsKey(getValueId(d)))
      return content[getValueId(d)] as Container
    if (content.containsKey(getKnotId(d)))
      return content[getKnotId(d)] as Container
    if (variables.containsKey(d)) {
      val t = variables[d]
      if (t is Container)
        return t
      else
        throw InkRunTimeException("Attempt to divert to a variable $d which is not a Container")
    }
    throw InkRunTimeException("Attempt to divert to non-defined node $d")
  }


  private fun checkResult(res: Any): Boolean {
    if (res is Boolean)return res
    if (res is BigDecimal) return res > BigDecimal.ZERO
    return false
  }

  fun clear() = text.clear()

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
