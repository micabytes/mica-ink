package com.micabytes.ink;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;


@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass"})
public class Story implements VariableMap {
  @NonNls private static final String GET_ID = "getId";
  @NonNls private static final String IS_NULL = "isNull";
  @NonNls private static final String RANDOM = "random";
  @NonNls private static final String IS_KNOT = "isKnot";
  @NonNls private static final String CURRENT_BACKGROUND = "currentBackground";
  // All content in the story
  private final Map<String, Content> storyContent = new HashMap<>();
  // All defined functions with name and implementation.
  private final Map<String, Function> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  // All story interrupts currently active
  private final List<StoryInterrupt> interrupts = new ArrayList<>();
  // The story wrapper (caller - implements logging and callbacks)
  private final StoryWrapper wrapper;

  // Story state
  String fileName;
  @Nullable Container container;
  private int contentIdx;
  private List<String> text = new ArrayList<>();
  private final List<Container> choices = new ArrayList<>();
  private final List<Comment> comments = new ArrayList<>();
  private String image;
  private final Map<String, Object> variables = new HashMap<>();
  private boolean processing;
  private boolean running;

  public Story(StoryWrapper provider) {
    wrapper = provider;
  }

  @SuppressWarnings({"OverlyComplexMethod", "OverlyNestedMethod"})
  public void saveStream(JsonGenerator g) throws IOException {
    g.writeStartObject();
    if (fileName != null)
      g.writeStringField(StoryJson.FILE, fileName);
    g.writeFieldName(StoryJson.CONTENT);
    g.writeStartObject();
    for (Map.Entry<String, Content> entry : storyContent.entrySet()) {
      Content content = entry.getValue();
      if (content.count > 0) {
        g.writeFieldName(content.id);
        g.writeStartObject();
        g.writeNumberField(StoryJson.COUNT, content.count);
        if (content instanceof ParameterizedContainer) {
          ParameterizedContainer pContainer = (ParameterizedContainer) content;
          if (pContainer.getVariables() != null) {
            g.writeFieldName(StoryJson.VARIABLES);
            g.writeStartObject();
            for (Map.Entry<String, Object> vars : pContainer.getVariables().entrySet()) {
              if (vars.getValue() != null) {
                saveObject(g, vars.getKey(), vars.getValue());
              } else {
                wrapper.logError("Story variable data " + vars.getKey() + " has a null value");
              }
            }
            g.writeEndObject();
          }
        }
        g.writeEndObject();
      }
    }
    g.writeEndObject();
    if (container != null)
      g.writeStringField(StoryJson.CONTAINER, container.id);
    g.writeNumberField(StoryJson.COUNTER, contentIdx);
    g.writeFieldName(StoryJson.TEXT);
    g.writeStartArray();
    for (String s : text) {
      g.writeString(s);
    }
    g.writeEndArray();
    g.writeFieldName(StoryJson.CHOICES);
    g.writeStartArray();
    for (Container choice : choices) {
      g.writeString(choice.getId());
    }
    g.writeEndArray();
    if (image != null)
      g.writeStringField(StoryJson.IMAGE, image);
    g.writeFieldName(StoryJson.VARIABLES);
    g.writeStartObject();
    for (Map.Entry<String, Object> vars : variables.entrySet()) {
      if (vars.getValue() != null) {
        saveObject(g, vars.getKey(), vars.getValue());
      } else {
        wrapper.logError("SaveData: " + vars.getKey() + " contains a null value");
      }
    }
    g.writeEndObject();
    g.writeBooleanField(StoryJson.RUNNING, running);
    g.writeEndObject();
  }

  @SuppressWarnings({"rawtypes", "unchecked", "NullArgumentToVariableArgMethod"})
  private void saveObject(JsonGenerator g, String key, Object val) throws IOException {
    if (val instanceof Boolean) {
      g.writeBooleanField(key, (Boolean) val);
      return;
    }
    if (val instanceof BigDecimal) {
      g.writeNumberField(key, (BigDecimal) val);
      return;
    }
    if (val instanceof String) {
      g.writeStringField(key, (String) val);
      return;
    }
    Class valClass = val.getClass();
    try {
      Method m = valClass.getMethod(GET_ID, null);
      Object id = m.invoke(val, null);
      g.writeStringField(key, (String) id);
    } catch (IllegalAccessException | IllegalArgumentException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
      wrapper.logError("SaveObject: Could not save " + key + ": " + val + ". Not Boolean, Number, String and not an Object. " + e.getMessage());
    }
  }

  @SuppressWarnings({"OverlyComplexMethod", "HardCodedStringLiteral"})
  public void loadData(JsonNode sNode, StoryWrapper provider) {
    for (JsonNode node : sNode.withArray(StoryJson.CONTENT)) {
      String id = node.get("id").asText();
      Content content = storyContent.get(id);
      if (content != null) {
        if (node.has("#n"))
          content.count = node.get("#n").asInt();
        if (node.has("vars")) {
          ParameterizedContainer pContainer = (ParameterizedContainer) content;
          for (JsonNode v : node.withArray("vars")) {
            Object val = loadObject(v, provider);
            if (val != null)
              pContainer.getVariables().put(v.get("id").asText(), val);
          }
        }
      } else {
        wrapper.logException(new InkParseException("Could not identify content ID " + id + " while loading data for " + fileName));
      }
    }
    container = (Container) storyContent.get(sNode.get("currentContainer").asText());
    contentIdx = sNode.get("currentCounter").asInt();
    for (JsonNode cNode : sNode.withArray("currentChoices")) {
      Content cont = storyContent.get(cNode.asText());
      if (cont != null)
        choices.add((Container) cont);
      else {
        wrapper.logError("Could not identify ");
      }
    }
    if (sNode.has(CURRENT_BACKGROUND))
      image = sNode.get(CURRENT_BACKGROUND).asText();
    if (sNode.has("vars")) {
      for (JsonNode v : sNode.withArray("vars")) {
        Object val = loadObject(v, provider);
        if (val != null)
          variables.put(v.get("id").asText(), val);
      }
    }
    /*
    if (sNode.has("ints")) {
      for (JsonNode v : sNode.withArray("ints")) {
        StoryInterrupt interrupt = provider.getInterrupt(v.asText());
        if (interrupt != null)
          interrupts.add(interrupt);
        else
          errorLog.add("Could not find interrupt with ID " + v.asText());
      }
    }
    */
    running = sNode.get(StoryJson.RUNNING).asBoolean();
  }

  private static Object loadObject(@NonNls JsonNode v, StoryWrapper provider) {
    JsonNode node = v.get("val");
    if (node != null) {
      if (node.isBoolean())
        return node.asBoolean();
      if (node.isInt())
        return BigDecimal.valueOf(node.asInt());
      if (node.isDouble())
        return BigDecimal.valueOf(node.asDouble());
      Object obj = provider.getStoryObject(node.asText());
      if (obj == null)
        return node.asText();
      return obj;
    }
    return null;
  }

  @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod", "NestedSwitchStatement"})
  public void loadStream(JsonParser p) throws IOException {
    while (p.nextToken() != JsonToken.END_OBJECT) {
      switch (p.getCurrentName()) {
        case StoryJson.CONTENT:
          p.nextToken(); // START_OBJECT
          while (p.nextToken() != JsonToken.END_OBJECT) {
            String cid = p.getCurrentName();
            Content content = storyContent.get(cid);
            p.nextToken(); // START_OBJECT
            while (p.nextToken() != JsonToken.END_OBJECT) {
              switch (p.getCurrentName()) {
                case StoryJson.COUNT:
                  if (content != null)
                    content.count = p.nextIntValue(0);
                  // The way this is used in P&T2, this is not actually an error.
                  // wrapper.logException(new InkLoadingException("Attempting to write COUNT " + Integer.toString(p.nextIntValue(0)) + " to content " + cid + "."));
                  break;
                case StoryJson.VARIABLES:
                  p.nextToken(); // START_OBJECT
                  ParameterizedContainer pContainer = (ParameterizedContainer) content;
                  while (p.nextToken() != JsonToken.END_OBJECT) {
                    String varName = p.getCurrentName();
                    Object obj = loadObjectStream(p);
                    if (pContainer != null)
                      pContainer.getVariables().put(varName, obj);
                  }
                  break;
                default:
                  break;
              }
            }
          }
          break;
        case StoryJson.CONTAINER:
          container = (Container) storyContent.get(p.nextTextValue());
          break;
        case StoryJson.COUNTER:
          contentIdx = p.nextIntValue(0);
          break;
        case StoryJson.TEXT:
          p.nextToken(); // START_ARRAY
          while (p.nextToken() != JsonToken.END_ARRAY) {
            text.add(p.getText());
          }
          break;
        case StoryJson.CHOICES:
          p.nextToken(); // START_ARRAY
          while (p.nextToken() != JsonToken.END_ARRAY) {
            Content cnt = storyContent.get(p.getText());
            if (cnt instanceof Choice)
              choices.add((Container) cnt);
            else
              wrapper.logException(new InkLoadingException(cnt.getId() + " is not a choice"));
          }
          break;
        case StoryJson.IMAGE:
          image = p.nextTextValue();
          break;
        case StoryJson.VARIABLES:
          p.nextToken(); // START_OBJECT
          while (p.nextToken() != JsonToken.END_OBJECT) {
            String varName = p.getCurrentName();
            Object obj = loadObjectStream(p);
            variables.put(varName, obj);
          }
          break;
        case StoryJson.RUNNING:
          running = p.nextBooleanValue();
          break;
        default:
          break;
      }
    }
  }

  private Object loadObjectStream(JsonParser p) throws IOException {
    JsonToken token = p.nextToken();
    if (token.isBoolean())
      return p.getBooleanValue();
    if (token.isNumeric())
      return new BigDecimal(p.getText());
    String str = p.getText();
    Object obj = wrapper.getStoryObject(str);
    if (obj == null)
      return str;
    return obj;
  }

  void addAll(Story story) {
    // TODO: Need to handle name collisions
    functions.putAll(story.functions);
    storyContent.putAll(story.storyContent);
    variables.putAll(story.variables);
  }

  public void addInterrupt(StoryInterrupt interrupt) {
    interrupts.add(interrupt);
    if (interrupt.isChoice()) {
      Choice choice = new Choice(0, interrupt.getInterrupt(), null);
      choice.setId(interrupt.getId());
      storyContent.put(choice.getId(), choice);
    }
    String fileId = interrupt.getInterruptFile();
    if (fileId != null) {
      try {
        Story st = InkParser.parse(wrapper.getStream(fileId), wrapper);
        addAll(st);
      } catch (InkParseException e) {
        wrapper.logException(e);
      }
    }
  }

  void initialize() {
    variables.put(Variable.TRUE_UC, BigDecimal.ONE);
    variables.put(Variable.FALSE_UC, BigDecimal.ZERO);
    if (container != null && container.type == ContentType.KNOT) {
      if (container.getContent(0).isStitch())
        container = (Container) container.getContent(0);
    }
    contentIdx = -1;
    running = true;
    processing = true;
    try {
      incrementContent(null);
    } catch (InkRunTimeException e) {
      logException(e);
    }
    functions.put(IS_NULL, new NullFunction());
    functions.put("not", new NotFunction());
    functions.put(RANDOM, new RandomFunction());
    functions.put(IS_KNOT, new IsKnotFunction());
  }

  public List<String> nextAll() throws InkRunTimeException {
    text.clear();
    ArrayList<String> ret = new ArrayList<>();
    while (hasNext()) {
      String txt = next();
      if (!txt.isEmpty())
        ret.add(txt);
    }
    text.addAll(ret);
    return ret;
  }

  private boolean hasNext() {
    if (!processing) return false;
    if (container == null)
      return false;
    return contentIdx < container.getContentSize();
  }

  @SuppressWarnings({"NonConstantStringShouldBeStringBuffer", "OverlyComplexMethod", "StringConcatenationInLoop"})
  private String next() throws InkRunTimeException {
    if (!hasNext())
      throw new InkRunTimeException("Did you forget to run canContinue()?");
    processing = true;
    String ret = "";
    Content content = getContent();
    boolean inProgress = true;
    while (inProgress) {
      inProgress = false;
      ret += resolveContent(content);
      incrementContent(content);
      if (container != null) {
        Content nextContent = getContent();
        if (nextContent != null) {
          if (nextContent.text.startsWith(Symbol.GLUE))
            inProgress = true;
          if (nextContent.isChoice() && !nextContent.isFallbackChoice())
            inProgress = true;
          if (nextContent.isStitch())
            inProgress = true;
          if (nextContent.type == ContentType.TEXT && nextContent.text.startsWith(Symbol.DIVERT)) {
            Container divertTo = getDivertTarget(nextContent);
            if (divertTo != null && divertTo.getContent(0).text.startsWith(Symbol.GLUE))
              inProgress = true;
          }
        }
        if (ret.endsWith(Symbol.GLUE) && nextContent != null)
          inProgress = true;
        content = nextContent;
      }
    }
    if (container != null && container.getBackground() != null) {
      image = container.getBackground();
    }
    if (!hasNext()) {
      resolveExtras();
    }
    return cleanUpText(ret);
  }

  private void resolveExtras() {
    for (StoryInterrupt interrupt : interrupts) {
      if (interrupt.isActive() && interrupt.isChoice()) {
        String cond = interrupt.getInterruptCondition();
        try {
          Object res = Variable.evaluate(cond, this);
          if (checkResult(res)) {
            Choice choice = (Choice) storyContent.get(interrupt.getId());
            if (choice.evaluateConditions(this)) {
              choices.add(0, choice);
            }
          }
        } catch (InkRunTimeException e) {
          wrapper.logException(e);
        }
      }
    }
  }

  @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
  private void incrementContent(Content content) throws InkRunTimeException {
    if (content != null && content.isDivert()) {
      Container divertTarget = getDivertTarget(content);
      if (divertTarget != null)
        divertTarget.initialize(this, content);
      else
        running = false;
      container = divertTarget;
      contentIdx = 0;
      choices.clear();
      return;
    }
    contentIdx++;
    if (container != null && contentIdx >= container.getContentSize()) {
      if (choices.isEmpty() && (container.isChoice() || container.isGather())) {
        Container c = container;
        Container p = c.parent;
        while (p != null) {
          int i = p.getContentIndex(c) + 1;
          while (i < p.getContentSize()) {
            Content n = p.getContent(i);
            if (n.isGather()) {
              Container newContainer = (Container) n;
              newContainer.initialize(this, content);
              container = newContainer;
              contentIdx = 0;
              choices.clear();
              return;
            }
            if (n.isChoice() && container.isGather()) {
              container = p;
              contentIdx = i;
              choices.clear();
              return;
            }
            i++;
          }
          c = p;
          p = c.parent;
        }
        container = null;
        contentIdx = 0;
        choices.clear();
        return;
      }
      if (container.isConditional()) {
        Container oldContainer = container;
        container = oldContainer.parent;
        contentIdx = container != null ? container.getContentIndex(oldContainer) + 1 : 0;
      }
    } else {
      Content next = getContent();
      if (next != null && next.isFallbackChoice() && choices.isEmpty()) {
        Container nextContainer = (Container) next;
        nextContainer.initialize(this, content);
        container = nextContainer;
        contentIdx = 0;
        choices.clear();
        return;
      }
      if (next != null && next.isConditional()) {
        Container nextContainer = (Container) next;
        nextContainer.initialize(this, content);
        container = nextContainer;
        contentIdx = 0;
        return;
      }
      if (next != null && next.isGather()) {
        processing = false;
      }
    }
  }

  private Content getContent() throws InkRunTimeException {
    if (!running)
      return null;
    if (container == null)
      throw new InkRunTimeException("Current text container is NULL.");
    if (contentIdx >= container.getContentSize())
      return null;
    return container.getContent(contentIdx);
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  private String resolveContent(Content content) throws InkRunTimeException {
    if (content.type == ContentType.TEXT) {
      String ret = content.isDivert() ? resolveDivert(content) : StoryText.getText(content.text, content.count, this);
      content.increment();
      return ret;
    }
    if (content.isChoice()) {
      addChoice((Choice) content);
    }
    if (content instanceof Comment) {
      comments.add((Comment) content);
    }
    if (content instanceof Variable)
      ((Variable) content).evaluate(this);
    return "";
  }

  private String resolveDivert(Content content) {
    String ret = StoryText.getText(content.text, content.count, this);
    ret = ret.substring(0, ret.indexOf(Symbol.DIVERT)).trim();
    ret += Symbol.GLUE;
    return ret;
  }

  @SuppressWarnings("OverlyComplexMethod")
  private Container getDivertTarget(Content content) throws InkRunTimeException {
    String d = content.text.substring(content.text.indexOf(Symbol.DIVERT) + 2).trim();
    if (d.contains(StoryText.BRACE_LEFT))
      d = d.substring(0, d.indexOf(StoryText.BRACE_LEFT));
    if (d.equals(Symbol.DIVERT_END))
      return null;
    d = resolveInterrupt(d);
    Container divertTo = (Container) storyContent.get(d);
    if (divertTo == null) {
      String fd = getFullId(d);
      divertTo = (Container) storyContent.get(fd);
      if (divertTo == null) {
        if (variables.containsKey(d))
          divertTo = (Container) variables.get(d);
        if (divertTo == null)
          throw new InkRunTimeException("Attempt to divert to non-defined " + d + " or " + fd + " in line " + content.lineNumber);
      }
    }
    // TODO: This needs to be rewritten for proper functioning of parameters (parameters on a knot with a base stitch)
    if (divertTo.type == ContentType.KNOT && divertTo.getContent(0).isStitch())
      divertTo = (Container) divertTo.getContent(0);
    if ((divertTo.type == ContentType.KNOT || divertTo.type == ContentType.STITCH) && divertTo.getContent(0).isConditional())
      divertTo = (Container) divertTo.getContent(0);
    // TODO: Should increment for each run through?
    return divertTo;
  }

  @SuppressWarnings("OverlyNestedMethod")
  private String resolveInterrupt(String divert) {
    for (StoryInterrupt interrupt : interrupts) {
      if (interrupt.isActive() && interrupt.isDivert()) {
        String cond = interrupt.getInterruptCondition();
        try {
          Object res = Variable.evaluate(cond, this);
          if (checkResult(res)) {
            String interruptText = interrupt.getInterrupt();
            if (interruptText.contains(Symbol.DIVERT)) {
              @NonNls String from = interruptText.substring(0, interruptText.indexOf(Symbol.DIVERT)).trim();
              if (from.equals(divert)) {
                String to = interruptText.substring(interruptText.indexOf(Symbol.DIVERT) + 2).trim();
                interrupt.done();
                putVariable(Symbol.EVENT, interrupt);
                return to;
              }
            }
          }
        } catch (InkRunTimeException e) {
          wrapper.logException(e);
          return divert;
        }
      }
    }
    return divert;
  }

  private static boolean checkResult(Object res) {
    if (res == null)
      return false;
    if (res instanceof Boolean && (Boolean) res)
      return true;
    return res instanceof BigDecimal && ((BigDecimal) res).intValue() > 0;
  }

  private String getFullId(String id) {
    if (id.equals(Symbol.DIVERT_END))
      return id;
    if (id.contains(String.valueOf(InkParser.DOT)))
      return id;
    Container p = container != null ? container.parent : null;
    return p != null ? p.id + InkParser.DOT + id : id;
  }

  private void addChoice(@NotNull Choice choice) throws InkRunTimeException {
    // Check conditions
    if (!choice.evaluateConditions(this))
      return;
    // Resolve
    if (choice.getChoiceText(this).isEmpty()) {
      if (choices.isEmpty()) {
        container = choice;
        contentIdx = 0;
      }
      // else nothing - this is a fallback choice and we ignore it
    } else {
      choices.add(choice);
    }
  }

  public void choose(int i) throws InkRunTimeException {
    if (i < choices.size() && i >= 0) {
      Container old = container;
      container = choices.get(i);
      if (container == null) {
        String oldId = (old != null) ? old.getId() : "null";
        throw new InkRunTimeException("Selected choice " + i + " is null in " + oldId + " and " + contentIdx);
      }
      container.increment();
      completeExtras(container);
      contentIdx = 0;
      choices.clear();
      processing = true;
    } else {
      String cId = container != null ? container.getId() : "null";
      throw new InkRunTimeException("Trying to select a choice " + i + " that does not exist in story: " + fileName + " container: " + cId + " cIndex: " + contentIdx);
    }
  }

  public int getChoiceSize() {
    return choices.size();
  }

  public Choice getChoice(int i) {
    return (Choice) choices.get(i);
  }

  private void completeExtras(Container extraContainer) {
    for (StoryInterrupt interrupt : interrupts) {
      if (interrupt.getId().equals(extraContainer.getId())) {
        interrupt.done();
      }
    }
  }

  private static String cleanUpText(@NonNls String str) {
    return str.replaceAll(Symbol.GLUE, " ") // clean up glue
        .replaceAll("\\s+", " ") // clean up white space
        .trim();
  }

  @SuppressWarnings("OverlyComplexMethod")
  @Override
  public Object getValue(String token) {
    if (Symbol.THIS.equals(token)) {
      if (container != null)
        return container.getId();
      else {
        wrapper.logError("Attempting to invoke this with null container in " + fileName);
        return "";
      }
    }
    Container c = container;
    while (c != null) {
      if (c.isKnot() || c.isFunction() || c.isStitch()) {
        if (((ParameterizedContainer) c).hasValue(token))
          return ((ParameterizedContainer) c).getValue(token);
      }
      c = c.parent;
    }
    if (token.startsWith(Symbol.DIVERT)) {
      String k = token.substring(2).trim();
      if (storyContent.containsKey(k))
        return storyContent.get(k);
      wrapper.logException(new InkRunTimeException("Could not identify container id: " + k));
      return BigDecimal.ZERO;
    }
    if (storyContent.containsKey(token)) {
      Container storyContainer = (Container) storyContent.get(token);
      return BigDecimal.valueOf(storyContainer.getCount());
    }
    String pathId = getValueId(token);
    if (storyContent.containsKey(pathId)) {
      Container storyContainer = (Container) storyContent.get(pathId);
      return BigDecimal.valueOf(storyContainer.getCount());
    }
    if (variables.containsKey(token)) {
      return variables.get(token);
    }
    wrapper.logException(new InkRunTimeException("Could not identify the variable " + token + " or " + pathId));
    return BigDecimal.ZERO;
  }

  private String getValueId(String id) {
    if (id.equals(Symbol.DIVERT_END))
      return id;
    if (id.contains(String.valueOf(InkParser.DOT)))
      return id;
    return container != null ? container.id + InkParser.DOT + id : id;
  }

  void add(Content content) throws InkParseException {
    if (content.getId() != null) {
      if (storyContent.containsKey(content.getId())) {
        throw new InkParseException("Invalid container ID. Two containers may not have the same ID");
      }
      if (content.isFunction())
        functions.put(content.getId(), (Function) content);
      else
        storyContent.put(content.getId(), content);
    } else {
      // Should not be possible.
      throw new InkParseException("No ID for content. This should not be possible.");
    }
    // Set starting knot
    if (content.isKnot() && (container == null || isContainerEmpty(container)))
      container = (Container) content;
  }

  private static boolean isContainerEmpty(@NotNull Container c) {
    return c.getContentSize() == 0;
  }

  public boolean isEnded() {
    return container == null;
  }

  @Override
  public boolean hasVariable(String token) {
    if (Character.isDigit(token.charAt(0)))
      return false;
    Container c = container;
    while (c != null) {
      if (c.isKnot() || c.isFunction() || c.isStitch()) {
        if (((ParameterizedContainer) c).hasValue(token))
          return true;
      }
      c = c.parent;
    }
    if (storyContent.containsKey(token))
      return true;
    if (storyContent.containsKey(getValueId(token)))
      return true;
    return variables.containsKey(token);
  }

  public void putVariable(@NonNls String key, Object value) {
    Container c = container;
    while (c != null) {
      if (c.isKnot() || c.isFunction() || c.isStitch()) {
        if (((ParameterizedContainer) c).hasValue(key)) {
          ((ParameterizedContainer) c).setValue(key, value);
          return;
        }
      }
      c = c.parent;
    }
    variables.put(key, value);
  }

  public Container getContainer(String key) {
    return (Container) storyContent.get(key);
  }

  @Override
  public boolean hasFunction(String token) {
    return functions.containsKey(token);
  }

  @Override
  public Function getFunction(String token) {
    return functions.get(token);
  }

  @Override
  public boolean checkObject(String token) {
    if (token.contains(".")) {
      return hasVariable(token.substring(0, token.indexOf(InkParser.DOT)));
    }
    return false;
  }

  @Override
  public String debugInfo() {
    @NonNls String ret = "";
    ret += "StoryDebugInfo File: " + fileName;
    ret += container != null ? " Container :" + container.getId() : " Container: null";
    if (container != null && contentIdx < container.getContentSize()) {
      Content cnt = container.getContent(contentIdx);
      ret += cnt != null ? " Line# :" + Integer.toString(cnt.lineNumber) : " Line#: ?";
    }
    return ret;
  }

  public String getImage() {
    return image;
  }

  public void setContainer(@NotNull String s) {
    Container c = (Container) storyContent.get(s);
    if (c != null)
      container = c;
  }

  @Override
  public void logException(Exception e) {
    if (wrapper != null)
      wrapper.logException(e);
  }

  // Legacy function used to set text
  @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
  public void setText(List<String> storyText) {
    text = storyText;
  }

  public List<String> getText() {
    return Collections.unmodifiableList(text);
  }

  public String getComment() throws InkRunTimeException {
    Iterator<Comment> cIt = comments.iterator();
    while (cIt.hasNext()) {
      Comment c = cIt.next();
      if (c.evaluateConditions(this)) {
        String ret = c.getCommentText(this);
        if (c.type == ContentType.COMMENT_ONCE)
          cIt.remove();
        return ret;
      }
    }
    return "";
  }

  @NonNls
  public String getStoryStatus() {
    String fileId = fileName != null ? fileName : "null";
    String contId = container != null ? container.getId() : "null";
    return "Story errors with FileName: " + fileId
        + ". Container: " + contId
        + ". ContentIdx: " + contentIdx;
  }

  private static class NullFunction implements Function {

    @Override
    public int getNumParams() {
      return 1;
    }

    @Override
    public boolean isFixedNumParams() {
      return true;
    }

    @Override
    public Object eval(List<Object> params, VariableMap vmap) throws InkRunTimeException {
      Object param = params.get(0);
      return param == null;
    }
  }

  private static class NotFunction implements Function {

    @Override
    public int getNumParams() {
      return 1;
    }

    @Override
    public boolean isFixedNumParams() {
      return true;
    }

    @Override
    public Object eval(List<Object> params, VariableMap vmap) throws InkRunTimeException {
      Object param = params.get(0);
      if (param instanceof Boolean)
        return !((Boolean) param);
      if (param instanceof BigDecimal)
        return ((BigDecimal) param).intValue() == 0 ? Boolean.TRUE : Boolean.FALSE;
      return Boolean.FALSE;
    }
  }

  private static class RandomFunction implements Function {

    @Override
    public int getNumParams() {
      return 1;
    }

    @Override
    public boolean isFixedNumParams() {
      return true;
    }

    @Override
    public Object eval(List<Object> params, VariableMap vmap) throws InkRunTimeException {
      Object param = params.get(0);
      if (param instanceof BigDecimal) {
        int val = ((BigDecimal) param).intValue();
        if (val <= 0) return BigDecimal.ZERO;
        return new BigDecimal(new Random().nextInt(val));
      }
      return BigDecimal.ZERO;
    }
  }

  private static class IsKnotFunction implements Function {

    @Override
    public int getNumParams() {
      return 1;
    }

    @Override
    public boolean isFixedNumParams() {
      return true;
    }

    @Override
    public Object eval(List<Object> params, VariableMap vmap) throws InkRunTimeException {
      Object param = params.get(0);
      if (param instanceof String && vmap.getValue(Symbol.THIS) != null) {
        @NonNls String str = (String) param;
        return str.equals(vmap.getValue(Symbol.THIS));
      }
      return Boolean.FALSE;
    }
  }
}