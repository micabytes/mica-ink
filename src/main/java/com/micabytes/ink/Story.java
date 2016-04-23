
package com.micabytes.ink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.jetbrains.annotations.NonNls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class Story {
  @NonNls private static final String GLUE = "<>";
  @NonNls static final String DIVERT = "->";
  @NonNls private static final String DIVERT_END = "END";
  public static final char LT = '<';
  public static final char GT = '>';
  // All defined functions with name and implementation.
  Map<String, Function> functions = new TreeMap<String, Function>(String.CASE_INSENSITIVE_ORDER);
  // Named containers
  String fileName;
  private final HashMap<String, Content> storyContent = new HashMap<>();
  Container currentContainer;
  private int currentCounter;
  private final ArrayList<Container> currentChoices = new ArrayList<>();
  private String currentBackground;
  private final HashMap<String, Object> variables = new HashMap<>();
  private boolean running;
  private boolean processing;
  private ArrayList<String> errorLog = new ArrayList<>();

  public ObjectNode saveData() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode retNode = mapper.createObjectNode();
    if (fileName != null)
      retNode.put("file", fileName);
    for (Map.Entry<String, Content> entry : storyContent.entrySet())
    {
      Content content = entry.getValue();
      if (content.count > 0) {
        ObjectNode node = mapper.createObjectNode();
        node.put("id", content.id);
        node.put("#n", content.count);
        if (content instanceof ParameterizedContainer) {
          ParameterizedContainer container = (ParameterizedContainer) content;
          if (container.variables != null) {
            for (Map.Entry<String, Object> var : container.variables.entrySet())
            {
              ObjectNode varNode = mapper.createObjectNode();
              if (var.getValue() != null) {
                varNode.put("id", var.getKey());
                saveObject(var.getValue(), varNode);
                node.withArray("vars").add(varNode);
              }
              else {
                errorLog.add("SaveData: " + var.getKey() + " contains a null value");
              }
            }
          }
        }
        retNode.withArray("content").add(node);
      }
    }
    retNode.put("currentContainer", currentContainer.id);
    retNode.put("currentCounter", currentCounter);
    for (Container choice : currentChoices) {
      retNode.withArray("currentChoices").add(choice.getId());
    }
    if (currentBackground != null)
      retNode.put("currentBackground", currentBackground);
    for (Map.Entry<String, Object> var : variables.entrySet())
    {
      if (var.getValue() != null) {
        ObjectNode varNode = mapper.createObjectNode();
        varNode.put("id", var.getKey());
        saveObject(var.getValue(), varNode);
        retNode.withArray("vars").add(varNode);
      }
      else {
        errorLog.add("SaveData: " + var.getKey() + " contains a null value");
      }
    }
    retNode.put("running", running);
    return retNode;
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
      Method m = valClass.getMethod("getSaveId", null);
      Object id = m.invoke(val, null);
      varNode.put("val", (String) id);
    } catch (Exception ignored) {
      // NOOP
      // TODO: Loss of data. Handle this different?
    }
  }

  public void loadData(JsonNode sNode, StoryProvider provider) {
    for (JsonNode node : sNode.withArray("content")) {
      String id = node.get("id").asText();
      Content content = storyContent.get(id);
      content.count = node.get("#n").asInt();
      if (node.has("vars")) {
        ParameterizedContainer container = (ParameterizedContainer) content;
        for (JsonNode v : node.withArray("vars")) {
          container.variables.put(v.get("id").asText(), loadObject(v, provider));
        }
      }
    }
    currentContainer = (Container) storyContent.get(sNode.get("currentContainer").asText());
    currentCounter = sNode.get("currentCounter").asInt();
    for (JsonNode cNode : sNode.withArray("currentChoices")) {
      currentChoices.add((Container) storyContent.get(cNode.asText()));
    }
    if (sNode.has("currentBackground"))
      currentBackground = sNode.get("currentBackground").asText();
    if (sNode.has("vars")) {
      for (JsonNode v : sNode.withArray("vars")) {
        variables.put(v.get("id").asText(), loadObject(v, provider));
      }
    }
    running = sNode.get("running").asBoolean();
  }

  private Object loadObject(JsonNode v, StoryProvider provider) {
    JsonNode node = v.get("val");
    if (node.isBoolean())
      return node.asBoolean();
    if (node.isInt())
      return BigDecimal.valueOf(node.asInt());
    if (node.isDouble())
      return BigDecimal.valueOf(node.asDouble());
    return provider.getStoryObject(node.asText());
  }

  void addAll(Story story) {
    // TODO: Need to handle name collissions
    functions.putAll(story.functions);
    storyContent.putAll(story.storyContent);
    variables.putAll(story.variables);
  }

  void initialize() {
    variables.put("TRUE", BigDecimal.ONE);
    variables.put("FALSE", BigDecimal.ZERO);
    if (currentContainer.type == ContentType.KNOT) {
      if (currentContainer.getContent(0).isStitch())
        currentContainer = (Container) currentContainer.getContent(0);
    }
    currentCounter = -1;
    running = true;
    processing = true;
    try {
      incrementContent(null);
    } catch (InkRunTimeException e) {
      e.printStackTrace();
    }
    /*Container def = storyContent.get(InkParser.DEFAULT_KNOT_NAME);
    if (def != currentContainer) {
      storyContent.remove(def);
    }
    */
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
      public Object eval(List<Object> parameters, Story story) throws InkRunTimeException {
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
      public Object eval(List<Object> parameters, Story story) throws InkRunTimeException {
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
      public Object eval(List<Object> parameters, Story story) throws InkRunTimeException {
        Object param = parameters.get(0);
        if (param instanceof BigDecimal) {
          int val = ((BigDecimal) param).intValue();
          if (val <= 0) return BigDecimal.ZERO;
          return new BigDecimal(new Random().nextInt(val));
        }
        return BigDecimal.ZERO;
      }
    });

  }

  public boolean hasNext() {
    if (!processing) return false;
    if (currentContainer == null)
      return false;
    return currentCounter < currentContainer.getContentSize();
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
      if (currentContainer != null) {
        Content nextContent = getContent();
        if (nextContent != null) {
          if (nextContent.text.startsWith(GLUE))
            processing = true;
          if (nextContent.isChoice() && !nextContent.isFallbackChoice())
            processing = true;
          if (nextContent.isStitch())
            processing = true;
          if (nextContent.type == ContentType.TEXT && nextContent.text.startsWith(DIVERT)) {
            Container divertTo = getDivertTarget(nextContent);
            if (divertTo != null && divertTo.getContent(0).text.startsWith(GLUE))
              processing = true;
          }
        }
        if (ret.endsWith(GLUE) && nextContent != null)
          processing = true;
        content = nextContent;
      }
    }
    if (currentContainer != null && currentContainer.getBackground() != null) {
      currentBackground = currentContainer.getBackground();
    }
    return cleanUpText(ret);
  }

  private void incrementContent(Content content) throws InkRunTimeException {
    if (content != null && content.isDivert()) {
      Container container = getDivertTarget(content);
      if (container != null)
        container.initialize(this, content);
      else
        running = false;
      currentContainer = container;
      currentCounter = 0;
      currentChoices.clear();
      return;
    }
    currentCounter++;
    if (currentCounter >= currentContainer.getContentSize()) {
      if (currentChoices.isEmpty() && (currentContainer.isChoice() || currentContainer.isGather())) {
        Container c = currentContainer;
        Container p = c.parent;
        while (p != null) {
          int i = p.getContentIndex(c) + 1;
          while (i < p.getContentSize()) {
            Content n = p.getContent(i);
            if (n.isGather()) {
              Container container = (Container) n;
              container.initialize(this, content);
              currentContainer = container;
              currentCounter = 0;
              currentChoices.clear();
              return;
            }
            if (n.isChoice() && currentContainer.isGather()) {
              currentContainer = p;
              currentCounter = i;
              currentChoices.clear();
              return;
            }
            i++;
          }
          c = p;
          p = c.parent;
        }
        currentContainer = null;
        currentCounter = 0;
        currentChoices.clear();
        return;
      }
      if (currentContainer.isConditional()) {
        Container c = currentContainer;
        currentContainer = currentContainer.parent;
        currentCounter = currentContainer.getContentIndex(c) + 1;
        return;
      }
    }
    else {
      Content next = getContent();
      if (next != null && next.isFallbackChoice() && currentChoices.isEmpty()) {
        Container container = (Container) next;
        container.initialize(this, content);
        currentContainer = container;
        currentCounter = 0;
        currentChoices.clear();
        return;
      }
      if (next != null && next.isConditional()) {
        Container container = (Container) next;
        container.initialize(this, content);
        currentContainer = container;
        currentCounter = 0;
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
    if (currentContainer == null && running)
      throw new InkRunTimeException("Current text container is NULL.");
    if (currentCounter >= currentContainer.getContentSize())
      return null;
    return currentContainer.getContent(currentCounter);
  }

  private String resolveContent(Content content) throws InkRunTimeException {
    if (content.type == ContentType.TEXT) {
      String ret = content.isDivert() ? resolveDivert(content) : content.getText(this);
      content.increment();
      return ret;
    }
    if (content.isChoice()) {
      addChoice((Choice) content);
    }
    if (content.isVariable())
      ((Variable) content).evaluate(this);
    return "";
  }

  private String resolveDivert(Content content) throws InkRunTimeException {
    String ret = content.getText(this);
    ret = ret.substring(0, ret.indexOf(DIVERT)).trim();
    ret += GLUE;
    return ret;
  }

  private Container getDivertTarget(Content content) throws InkRunTimeException {
    String d = content.text.substring(content.text.indexOf(DIVERT) + 2).trim();
    if (d.contains(Content.BRACE_LEFT))
      d = d.substring(0, d.indexOf(Content.BRACE_LEFT));
    if (d.equals(DIVERT_END))
      return null;
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
    if (divertTo.type == ContentType.KNOT && (divertTo.getContent(0).isStitch()))
      divertTo = (Container) divertTo.getContent(0);
    if ((divertTo.type == ContentType.KNOT || divertTo.type == ContentType.STITCH )&& (divertTo.getContent(0).isConditional()))
      divertTo = (Container) divertTo.getContent(0);
    // TODO: Should increment for each run through?
    return divertTo;
  }

  private String getFullId(String id) {
    if (id.equals(DIVERT_END))
      return id;
    if (id.contains(String.valueOf(InkParser.DOT)))
      return id;
    Container p = currentContainer.parent;
    return p != null ? p.id + InkParser.DOT + id : id;
  }

  private void addChoice(Choice choice) throws InkRunTimeException {
    // Check conditions
    if (!choice.evaluateConditions(this))
      return;
    // Resolve
    if (choice.getChoiceText(this).isEmpty()) {
      if (currentChoices.isEmpty()) {
        currentContainer = choice;
        currentCounter = 0;
      }
      // else nothing - this is a fallback choice and we ignore it
    } else {
      currentChoices.add(choice);
    }
  }

  public List<String> nextAll() throws InkRunTimeException {
    ArrayList<String> ret = new ArrayList<>();
    while (hasNext()) {
      String text = next();
      if (!text.isEmpty())
        ret.add(text);
    }
    return ret;
  }

  public void choose(int i) throws InkRunTimeException {
    if (i < currentChoices.size()) {
      currentContainer = currentChoices.get(i);
      currentContainer.increment();
      currentCounter = 0;
      currentChoices.clear();
      processing = true;
    } else
      throw new InkRunTimeException("Trying to select a choice that does not exist");
  }

  public int getChoiceSize() {
    return currentChoices.size();
  }

  public Choice getChoice(int i) {
    return (Choice) currentChoices.get(i);
  }

  private static String cleanUpText(@NonNls String str) {
    return str.replaceAll(GLUE, " ") // clean up glue
              .replaceAll("\\s+", " ") // clean up white space
              .trim();
  }

  public Object getValue(String key) throws InkRunTimeException {
    Container c = currentContainer;
    while (c != null) {
      if (c.isKnot() || c.isFunction() || c.isStitch()) {
        if (((ParameterizedContainer) c).hasValue(key))
          return ((ParameterizedContainer) c).getValue(key);
      }
      c = c.parent;
    }
    if (key.startsWith(DIVERT)) {
      String k = key.substring(2).trim();
      if (storyContent.containsKey(k))
        return storyContent.get(k);
      throw new InkRunTimeException("Could not identify container id: " + k);
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
    throw new InkRunTimeException("Could not identify the variable " + key + " or " + pathId);
  }

  private String getValueId(String id) {
    if (id.equals(DIVERT_END))
      return id;
    if (id.contains(String.valueOf(InkParser.DOT)))
      return id;
    return currentContainer != null ? currentContainer.id + InkParser.DOT + id : id;
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
    }
    else {
      // Should not be possible.
      throw new InkParseException("No ID for content. This should not be possible.");
    }
    // Set starting knot
    if ((content.isKnot())&&(currentContainer == null || (currentContainer != null && currentContainer.getContentSize() == 0)))
      currentContainer = (Container) content;
  }

  public boolean isEnded() {
    return currentContainer == null;
  }


  public boolean hasVariable(String variable) {
    if (Character.isDigit(variable.charAt(0)))
      return false;
    Container c = currentContainer;
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
    if (currentContainer == null)
      return id;
    Container p = currentContainer;
    while (!p.isKnot() || !p.isFunction() || !p.isStitch())
      p = p.parent;
    return p != null ? p.id + InkParser.DOT + id : id;
  }


  public void putVariable(String key, Object value) {
    Container c = currentContainer;
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

  boolean hasFunction(String fct) {
    return functions.containsKey(fct);
  }

  boolean checkObject(String fct) {
    if (fct.contains(".")) {
      return hasVariable(fct.substring(0, fct.indexOf(InkParser.DOT)));
    }
    return false;
  }

  public String getCurrentBackground() {
    return currentBackground;
  }

}