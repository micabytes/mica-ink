
package com.micabytes.ink;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;


// TODO: Save Comments


public class Story implements VariableMap {
  public static final String GET_ID = "getId";
  // All content in the story
  private final Map<String, Content> storyContent = new HashMap<>();
  // All defined functions with name and implementation.
  Map<String, Function> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  private final List<StoryInterrupt> interrupts = new ArrayList<>();
  StoryProvider wrapper;

  // Story state
  String fileName;
  Container container;
  private int contentIdx;
  private List<String> text = new ArrayList<>();
  private final List<Container> choices = new ArrayList<>();
  private final List<Comment> comments = new ArrayList<>();
  private String image;
  private final Map<String, Object> variables = new HashMap<>();
  private boolean processing;
  private boolean running;

  public Story(StoryProvider provider) {
    wrapper = provider;
  }

  public ObjectNode saveData() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode retNode = mapper.createObjectNode();
    if (fileName != null)
      retNode.put("file", fileName);
    for (Map.Entry<String, Content> entry : storyContent.entrySet()) {
      Content content = entry.getValue();
      if (content.count > 0) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", content.id);
        node.put("#n", content.count);
        if (content instanceof ParameterizedContainer) {
          ParameterizedContainer container = (ParameterizedContainer) content;
          if (container.variables != null) {
            for (Map.Entry<String, Object> var : container.variables.entrySet()) {
              ObjectNode varNode = mapper.createObjectNode();
              if (var.getValue() != null) {
                varNode.put("id", var.getKey());
                saveObject(var.getValue(), varNode);
                node.withArray("vars").add(varNode);
              } else {
                wrapper.logError("SaveData: " + var.getKey() + " contains a null value");
              }
            }
          }
        }
        retNode.withArray("content").add(node);
      }
    }
    retNode.put("currentContainer", container.id);
    retNode.put("currentCounter", contentIdx);
    for (Container choice : choices) {
      retNode.withArray("currentChoices").add(choice.getId());
    }
    if (image != null)
      retNode.put("currentBackground", image);
    for (Map.Entry<String, Object> var : variables.entrySet()) {
      if (var.getValue() != null) {
        ObjectNode varNode = mapper.createObjectNode();
        varNode.put("id", var.getKey());
        saveObject(var.getValue(), varNode);
        retNode.withArray("vars").add(varNode);
      } else {
        wrapper.logError("SaveData: " + var.getKey() + " contains a null value");
      }
    }
    retNode.put("running", running);
    return retNode;
  }

  public void saveStreamed(JsonGenerator g) throws IOException {
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
          ParameterizedContainer container = (ParameterizedContainer) content;
          if (container.variables != null) {
            g.writeFieldName(StoryJson.VARIABLES);
            g.writeStartObject();
            for (Map.Entry<String, Object> var : container.variables.entrySet()) {
              if (var.getValue() != null) {
                saveObject(g, var.getKey(), var.getValue());
              } else {
                wrapper.logError("SaveData: " + var.getKey() + " contains a null value");
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
    g.writeFieldName(StoryJson.VARIABLES_GLOBAL);
    g.writeStartObject();
    for (Map.Entry<String, Object> var : variables.entrySet()) {
      if (var.getValue() != null) {
        saveObject(g, var.getKey(), var.getValue());
      } else {
        wrapper.logError("SaveData: " + var.getKey() + " contains a null value");
      }
    }
    g.writeEndObject();
    g.writeBooleanField(StoryJson.RUNNING, running);
    g.writeEndObject();
  }

  private void saveObject(Object val, ObjectNode varNode) {
    if (val instanceof Boolean) {
      varNode.put("val", (Boolean) val);
      return;
    }
    if (val instanceof BigDecimal) {
      varNode.put("val", (BigDecimal) val);
      return;
    }
    if (val instanceof String) {
      varNode.put("val", (String) val);
      return;
    }
    Class valClass = val.getClass();
    try {
      Method m = valClass.getMethod(GET_ID, null);
      Object id = m.invoke(val, null);
      varNode.put("val", (String) id);
    } catch (Exception ignored) {
      // NOOP
      // TODO: Loss of data. Handle this different?
    }
  }

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
    } catch (Exception ignored) {
      wrapper.logError("SaveObject: Could not save " + key + " " + val.toString() + ". Not Boolean, Number, String or Object.");
    }
  }

  public void loadData(JsonNode sNode, StoryProvider provider) {
    for (JsonNode node : sNode.withArray("content")) {
      String id = node.get("id").asText();
      Content content = storyContent.get(id);
      if (content != null) {
        if (node.has("#n"))
          content.count = node.get("#n").asInt();
        if (node.has("vars")) {
          ParameterizedContainer container = (ParameterizedContainer) content;
          for (JsonNode v : node.withArray("vars")) {
            Object val = loadObject(v, provider);
            if (val != null)
              container.variables.put(v.get("id").asText(), val);
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
    if (sNode.has("currentBackground"))
      image = sNode.get("currentBackground").asText();
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
        StoryInterrupt intr = provider.getInterrupt(v.asText());
        if (intr != null)
          interrupts.add(intr);
        else
          errorLog.add("Could not find interrupt with ID " + v.asText());
      }
    }
    */
    running = sNode.get("running").asBoolean();
  }

  private Object loadObject(JsonNode v, StoryProvider provider) {
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
                  else
                    wrapper.logException(new InkLoadingException("Attempting to write COUNT " + Integer.toString(p.nextIntValue(0)) + " to content " + cid + "."));
                  break;
                case StoryJson.VARIABLES:
                  p.nextToken(); // START_OBJECT
                  ParameterizedContainer container = (ParameterizedContainer) content;
                  while (p.nextToken() != JsonToken.END_OBJECT) {
                    String varName = p.getCurrentName();
                    Object obj = loadObjectStream(p);
                    container.variables.put(varName, obj);
                  }
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
            choices.add((Container) storyContent.get(p.getText()));
          }
          break;
        case StoryJson.IMAGE:
          image = p.nextTextValue();
          break;
        case StoryJson.VARIABLES_GLOBAL:
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
    // TODO: Need to handle name collissions
    functions.putAll(story.functions);
    storyContent.putAll(story.storyContent);
    variables.putAll(story.variables);
  }

  public void addInterrupt(StoryInterrupt intr, StoryProvider provider) {
    interrupts.add(intr);
    if (intr.isChoice()) {
      try {
        Choice choice = new Choice(0, intr.getInterrupt(), null);
        choice.setId(intr.getId());
        storyContent.put(choice.getId(), choice);
      } catch (InkParseException e) {
        wrapper.logException(e);
      }
    }
    String fileId = intr.getInterruptFile();
    if (fileId != null) {
      try {
        Story st = InkParser.parse(wrapper.getStream(fileId), wrapper);
        addAll(st);
      } catch (InkParseException e) {
        wrapper.logException(e);
        return;
      }
    }
  }

  void initialize() {
    variables.put("TRUE", BigDecimal.ONE);
    variables.put("FALSE", BigDecimal.ZERO);
    if (container.type == ContentType.KNOT) {
      if (container.getContent(0).isStitch())
        container = (Container) container.getContent(0);
    }
    contentIdx = -1;
    running = true;
    processing = true;
    try {
      incrementContent(null);
    } catch (InkRunTimeException e) {
      e.printStackTrace();
    }
    functions.put("isNull", new Function() {
      @Override
      public String getId() {
        return "isNull";
      }

      @Override
      public int getNumParams() {
        return 1;
      }

      @Override
      public boolean numParamsVaries() {
        return false;
      }

      @Override
      public Object eval(List<Object> parameters, VariableMap variables) throws InkRunTimeException {
        Object param = parameters.get(0);
        return param != null;
      }
    });
    functions.put("not", new Function() {
      @Override
      public String getId() {
        return "not";
      }

      @Override
      public int getNumParams() {
        return 1;
      }

      @Override
      public boolean numParamsVaries() {
        return false;
      }

      @Override
      public Object eval(List<Object> parameters, VariableMap variables) throws InkRunTimeException {
        Object param = parameters.get(0);
        if (param instanceof Boolean)
          return !((Boolean) param);
        if (param instanceof BigDecimal)
          return ((BigDecimal) param).intValue() == 0 ? Boolean.TRUE : Boolean.FALSE;
        return Boolean.FALSE;
      }
    });
    functions.put("random", new Function() {
      @Override
      public String getId() {
        return "random";
      }

      @Override
      public int getNumParams() {
        return 1;
      }

      @Override
      public boolean numParamsVaries() {
        return false;
      }

      @Override
      public Object eval(List<Object> parameters, VariableMap variables) throws InkRunTimeException {
        Object param = parameters.get(0);
        if (param instanceof BigDecimal) {
          int val = ((BigDecimal) param).intValue();
          if (val <= 0) return BigDecimal.ZERO;
          return new BigDecimal(new Random().nextInt(val));
        }
        return BigDecimal.ZERO;
      }
    });
    functions.put("isKnot", new Function() {
      @Override
      public String getId() {
        return "isKnot";
      }

      @Override
      public int getNumParams() {
        return 1;
      }

      @Override
      public boolean numParamsVaries() {
        return false;
      }

      @Override
      public Object eval(List<Object> parameters, VariableMap variables) throws InkRunTimeException {
        Object param = parameters.get(0);
        if (param instanceof String && variables.getValue("this") != null) {
          String str = (String) param;
          return str.equals(variables.getValue("this"));
        }
        return Boolean.FALSE;
      }
    });

  }

  public List<String> nextAll() throws InkRunTimeException {
    text.clear();
    ArrayList<String> ret = new ArrayList<>();
    while (hasNext()) {
      String text = next();
      if (!text.isEmpty())
        ret.add(text);
    }
    text.addAll(ret);
    return ret;
  }

  public boolean hasNext() {
    if (!processing) return false;
    if (container == null)
      return false;
    return contentIdx < container.getContentSize();
  }

  public String next() throws InkRunTimeException {
    if (!hasNext())
      throw new InkRunTimeException("Did you forget to run canContinue()?");
    processing = true;
    String ret = "";
    Content content = getContent();
    boolean processing = true;
    while (processing) {
      processing = false;
      ret += resolveContent(content);
      incrementContent(content);
      if (container != null) {
        Content nextContent = getContent();
        if (nextContent != null) {
          if (nextContent.text.startsWith(Symbol.GLUE))
            processing = true;
          if (nextContent.isChoice() && !nextContent.isFallbackChoice())
            processing = true;
          if (nextContent.isStitch())
            processing = true;
          if (nextContent.type == ContentType.TEXT && nextContent.text.startsWith(Symbol.DIVERT)) {
            Container divertTo = getDivertTarget(nextContent);
            if (divertTo != null && divertTo.getContent(0).text.startsWith(Symbol.GLUE))
              processing = true;
          }
        }
        if (ret.endsWith(Symbol.GLUE) && nextContent != null)
          processing = true;
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
    for (StoryInterrupt intr : interrupts) {
      if (intr.isActive() && intr.isChoice()) {
        String cond = intr.getInterruptCondition();
        try {
          Object res = Variable.evaluate(cond, this);
          if (checkResult(res)) {
            Choice choice = (Choice) storyContent.get(intr.getId());
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

  private void incrementContent(Content content) throws InkRunTimeException {
    if (content != null && content.isDivert()) {
      Container container = getDivertTarget(content);
      if (container != null)
        container.initialize(this, content);
      else
        running = false;
      this.container = container;
      contentIdx = 0;
      choices.clear();
      return;
    }
    contentIdx++;
    if (contentIdx >= container.getContentSize()) {
      if (choices.isEmpty() && (container.isChoice() || container.isGather())) {
        Container c = container;
        Container p = c.parent;
        while (p != null) {
          int i = p.getContentIndex(c) + 1;
          while (i < p.getContentSize()) {
            Content n = p.getContent(i);
            if (n.isGather()) {
              Container container = (Container) n;
              container.initialize(this, content);
              this.container = container;
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
        Container c = container;
        container = container.parent;
        contentIdx = container.getContentIndex(c) + 1;
        return;
      }
    } else {
      Content next = getContent();
      if (next != null && next.isFallbackChoice() && choices.isEmpty()) {
        Container container = (Container) next;
        container.initialize(this, content);
        this.container = container;
        contentIdx = 0;
        choices.clear();
        return;
      }
      if (next != null && next.isConditional()) {
        Container container = (Container) next;
        container.initialize(this, content);
        this.container = container;
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
    if (container == null && running)
      throw new InkRunTimeException("Current text container is NULL.");
    if (contentIdx >= container.getContentSize())
      return null;
    return container.getContent(contentIdx);
  }

  private String resolveContent(Content content) throws InkRunTimeException {
    if (content.type == ContentType.TEXT) {
      String ret = content.isDivert() ? resolveDivert(content) : StoryText.getText(content.text, content.count, this);
      content.increment();
      return ret;
    }
    if (content.isChoice()) {
      addChoice((Choice) content);
    }
    if (content.isComment()) {
      comments.add((Comment) content);
    }
    if (content.isVariable())
      ((Variable) content).evaluate(this);
    return "";
  }

  private String resolveDivert(Content content) throws InkRunTimeException {
    String ret = StoryText.getText(content.text, content.count, this);
    ret = ret.substring(0, ret.indexOf(Symbol.DIVERT)).trim();
    ret += Symbol.GLUE;
    return ret;
  }

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

  private String resolveInterrupt(String divert) {
    String ret = divert;
    for (StoryInterrupt intr : interrupts) {
      if (intr.isActive() && intr.isDivert()) {
        String cond = intr.getInterruptCondition();
        try {
          Object res = Variable.evaluate(cond, this);
          if (checkResult(res)) {
            String interrupt = intr.getInterrupt();
            if (interrupt.contains(Symbol.DIVERT)) {
              String from = interrupt.substring(0, interrupt.indexOf(Symbol.DIVERT)).trim();
              if (from.equals(divert)) {
                String to = interrupt.substring(interrupt.indexOf(Symbol.DIVERT) + 2).trim();
                intr.done();
                putVariable("event", intr);
                return to;
              }
            }
          }
        } catch (InkRunTimeException e) {
          wrapper.logException(e);
          return ret;
        }
      }
    }
    return ret;
  }

  private boolean checkResult(Object res) {
    if (res == null)
      return false;
    if (res instanceof Boolean && ((Boolean) res).booleanValue())
      return true;
    if (res instanceof BigDecimal && ((BigDecimal) res).intValue() > 0)
      return true;
    return false;
  }

  private String getFullId(String id) {
    if (id.equals(Symbol.DIVERT_END))
      return id;
    if (id.contains(String.valueOf(InkParser.DOT)))
      return id;
    Container p = container.parent;
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
      if (container == null)
        throw new InkRunTimeException("Selected choice " + i + " is null in " + old != null ? old.getId() : "null" + " and " + contentIdx);
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

  private void completeExtras(Container container) {
    for (StoryInterrupt intr : interrupts) {
      if (intr.getId().equals(container.getId())) {
        intr.done();
      }
    }
  }

  private static String cleanUpText(@NonNls String str) {
    return str.replaceAll(Symbol.GLUE, " ") // clean up glue
        .replaceAll("\\s+", " ") // clean up white space
        .trim();
  }

  @Override
  public Object getValue(String key) {
    if (key.equals("this")) {
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
        if (((ParameterizedContainer) c).hasValue(key))
          return ((ParameterizedContainer) c).getValue(key);
      }
      c = c.parent;
    }
    if (key.startsWith(Symbol.DIVERT)) {
      String k = key.substring(2).trim();
      if (storyContent.containsKey(k))
        return storyContent.get(k);
      wrapper.logException(new InkRunTimeException("Could not identify container id: " + k));
      return BigDecimal.ZERO;
    }
    if (storyContent.containsKey(key)) {
      Container container = (Container) storyContent.get(key);
      return BigDecimal.valueOf(container.getCount());
    }
    String pathId = getValueId(key);
    if (storyContent.containsKey(pathId)) {
      Container container = (Container) storyContent.get(pathId);
      return BigDecimal.valueOf(container.getCount());
    }
    if (variables.containsKey(key)) {
      return variables.get(key);
    }
    wrapper.logException(new InkRunTimeException("Could not identify the variable " + key + " or " + pathId));
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
    if (content.isKnot() && (container == null || (container != null && container.getContentSize() == 0)))
      container = (Container) content;
  }

  public boolean isEnded() {
    return container == null;
  }


  @Override
  public boolean hasVariable(String variable) {
    if (Character.isDigit(variable.charAt(0)))
      return false;
    Container c = container;
    while (c != null) {
      if (c.isKnot() || c.isFunction() || c.isStitch()) {
        if (((ParameterizedContainer) c).hasValue(variable))
          return true;
      }
      c = c.parent;
    }
    if (storyContent.containsKey(variable))
      return true;
    if (storyContent.containsKey(getValueId(variable)))
      return true;
    return variables.containsKey(variable);
  }

  private String getPathId(String id) {
    if (container == null)
      return id;
    Container p = container;
    while (!p.isKnot() || !p.isFunction() || !p.isStitch())
      p = p.parent;
    return p != null ? p.id + InkParser.DOT + id : id;
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
  public boolean hasFunction(String fct) {
    return functions.containsKey(fct);
  }

  @Override
  public Function getFunction(String token) {
    return functions.get(token);
  }

  @Override
  public boolean checkObject(String fct) {
    if (fct.contains(".")) {
      return hasVariable(fct.substring(0, fct.indexOf(InkParser.DOT)));
    }
    return false;
  }

  @Override
  public String debugInfo() {
    String ret = new String();
    ret += "File: " + fileName;
    ret += container != null ? " Container :" + container.getId() : " Container: null";
    if (contentIdx < container.getContentSize()) {
      Content cnt = container.getContent(contentIdx);
      ret += cnt != null ? " Line# :" + Integer.toString(cnt.lineNumber) : " Line#: ?";
    }
    return ret;
  }

  public String getImage() {
    return image;
  }

  public void setContainer(String s) {
    Container c = (Container) storyContent.get(s);
    if (c != null)
      container = c;
  }

  @Override
  public void logException(Exception e) {

  }

  // Legacy function used to set text
  public void setText(ArrayList<String> storyText) {
    text = storyText;
  }

  public List<String> getText() {
    return text;
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
    return null;
  }
}