
package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Story {
  @NonNls private static final String GLUE = "<>";
  @NonNls static final String DIVERT = "->";
  @NonNls private static final String DIVERT_END = "END";
  public static final char LT = '<';
  public static final char GT = '>';

  private final HashMap<String, Container> namedContainers = new HashMap<>();
  private Container currentContainer;
  private int currentCounter;
  private final ArrayList<Container> currentChoices = new ArrayList<>();
  private boolean running;

  void initialize() {
    if (currentContainer.type == ContentType.KNOT)
      currentContainer = (Container) currentContainer.getContent(0);
    running = true;
  }

  public boolean hasNext() {
    return currentContainer != null && currentCounter < currentContainer.getContentSize();
  }

  public String next() throws InkRunTimeException {
    if (!hasNext())
      throw new InkRunTimeException("Did you forget to run canContinue()?");
    String ret = "";
    Content content = nextContent();
    boolean linebreak = false;
    while (!linebreak) {
      linebreak = true;
      currentCounter++;
      ret += resolveContent(content);
      if (ret.endsWith(GLUE))
        linebreak = false;
      if (hasNext()) {
        Content nextContent = lookAheadToContent();
        if (nextContent.text.startsWith(GLUE) || content.isChoice())
          linebreak = false;
        if (nextContent.type == ContentType.TEXT && nextContent.isDivert()) {
          Container divertTo = getDivertTarget(nextContent);
          if (divertTo != null && divertTo.getContent(0).text.startsWith(GLUE))
            linebreak = false;
        }
        content = nextContent();
      } else
        linebreak = true;
    }
    return cleanUpText(ret);
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
    return "";
  }

  private Content nextContent() throws InkRunTimeException {
    if (currentContainer == null)
      throw new InkRunTimeException("Current text container is NULL.");
    if (currentCounter >= currentContainer.getContentSize()) {
      if (currentContainer.isChoice() || currentContainer.isGather()) {
        Container p = currentContainer.parent;
        while (p != null) {
          Content c = p.getContent(p.getContentSize() - 1);
          if (c.isGather()) {
            currentContainer = (Container) c;
            return currentContainer.getContent(0);
          }
          p = ((Container) c).parent;
        }
      }
      //throw new InkRunTimeException("Knot or stitch on line " + currentContainer.lineNumber + " ended without choice or divert.");
      return null;
    }
    return currentContainer.getContent(currentCounter);
  }

  private Content lookAheadToContent() {
    if (currentContainer == null)
      return null;
    if (currentCounter >= currentContainer.getContentSize()) {
      if (currentContainer.isChoice() || currentContainer.isGather()) {
        Container p = currentContainer.parent;
        while (p != null) {
          Content c = p.getContent(p.getContentSize() - 1);
          if (c.isGather()) {
            return ((Container)c).getContent(0);
          }
          p = ((Container) c).parent;
        }
      }
      //throw new InkRunTimeException("Knot or stitch on line " + currentContainer.lineNumber + " ended without choice or divert.");
      return null;
    }
    return currentContainer.getContent(currentCounter);
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

  public List<String> allLines() throws InkRunTimeException {
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
    } else
      throw new InkRunTimeException("Trying to select a choice that does not exist");
  }

  public int getChoiceSize() {
    return currentChoices.size();
  }

  public Choice getChoice(int i) {
    return (Choice) currentChoices.get(i);
  }

  private String resolveDivert(Content content) throws InkRunTimeException {
    String ret = content.text.substring(0, content.text.indexOf(DIVERT)).trim();
    if (!ret.isEmpty())
      ret += GLUE;
    currentContainer = getDivertTarget(content);
    if (currentContainer != null)
      currentContainer.increment();
    currentCounter = 0;
    currentChoices.clear();
    return ret;
  }

  private Container getDivertTarget(Content content) throws InkRunTimeException {
    String d = content.text.substring(content.text.indexOf(DIVERT) + 2).trim();
    if (d.equals(DIVERT_END))
      return null;
    Container divertTo = namedContainers.get(d);
    if (divertTo == null) {
      Container currentKnot = currentContainer.getContainer(0);
      divertTo = namedContainers.get(currentKnot.id + InkParser.DOT + d);
      if (divertTo == null)
        throw new InkRunTimeException("Attempt to divert to non-defined " + d + " in line " + content.lineNumber);
    }
    return divertTo.type == ContentType.KNOT ? (Container) divertTo.getContent(0) : divertTo;
  }

  private static String cleanUpText(@NonNls String str) {
    return str.replaceAll(GLUE, " ") // clean up glue
              .replaceAll("\\s+", " ") // clean up white space
              .trim();
  }

  public BigDecimal getValue(String s) throws InkRunTimeException {
    if (namedContainers.containsKey(s)) {
      Container container = namedContainers.get(s);
      return BigDecimal.valueOf(container.getCount());
    }
    throw new InkRunTimeException("Could not identify the variable " + s);
  }

  void add(Container container) {
    if (container.getId() != null)
      namedContainers.put(container.getId(), container);
    // Set starting knot
    if (currentContainer == null)
      currentContainer = container;
  }

  public boolean isEnded() {
    return currentContainer == null;
  }


}