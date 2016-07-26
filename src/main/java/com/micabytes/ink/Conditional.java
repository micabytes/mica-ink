package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Conditional extends Container {
  @NonNls private static final String STOPPING = "stopping";
  @NonNls private static final String SHUFFLE = "shuffle";
  @NonNls private static final String CYCLE = "cycle";
  @NonNls private static final String ONCE = "once";
  private static final String CONDITIONAL_DASH = "-";
  private static final String CONDITIONAL_COLON = ":";
  @NonNls private static final String ELSE = "else";
  private int selection;

  private static final class ConditionalOptions extends Content {
    final String condition;
    final List<Content> lines = new ArrayList<>();

    ConditionalOptions(String cond) {
      condition = cond;
      type = ContentType.CONDITIONAL_OPTION;
    }
  }

  Conditional(int l, String line, Container current) throws InkParseException {
    lineNumber = l;
    type = ContentType.CONDITIONAL;
    content = new ArrayList<>();
    String str = line.substring(1).trim();
    if (!str.isEmpty()) {
      if (!str.endsWith(":"))
        throw new InkParseException("Error in conditional block; condition not ended by \':\'. Line number: " + lineNumber);
      if (str.startsWith(CONDITIONAL_DASH))
        str = str.substring(1).trim();
      String condition = str.substring(0, str.length()-1).trim();
      verifySequenceCondition(condition);
      if (type == ContentType.CONDITIONAL)
        content.add(new ConditionalOptions(condition));
    }
    parent = current;
    parent.add(this);
  }

  public void parseLine(int l, String line) throws InkParseException {
    String str = line.endsWith(InkParser.CONDITIONAL_END)
        ? line.substring(0, line.indexOf(InkParser.CONDITIONAL_END))
        : line;
    if (type == ContentType.CONDITIONAL) {
      if (str.startsWith(CONDITIONAL_DASH) && !str.startsWith(Symbol.DIVERT)) {
        if (!str.endsWith(CONDITIONAL_COLON))
          throw new InkParseException("Error in conditional block; condition not ended by \':\'. LineNumber: " + l);
        String condition = str.substring(1, str.length()-1).trim();
        content.add(new ConditionalOptions(condition));
      }
      else {
        InkParser.parseLine(l, str, this);
      }
    }
    else {
      if (str.startsWith(CONDITIONAL_DASH) && !str.startsWith(Symbol.DIVERT)) {
        String first = str.substring(1).trim();
        content.add(new ConditionalOptions(""));
        InkParser.parseLine(l, first, this);
      }
      else {
        InkParser.parseLine(l, str, this);
      }
    }
  }

  private void verifySequenceCondition(@NonNls String str) {
    if ( STOPPING.equalsIgnoreCase(str) )
      type = ContentType.SEQUENCE_STOP;
    if ( SHUFFLE.equalsIgnoreCase(str) )
      type = ContentType.SEQUENCE_SHUFFLE;
    if ( CYCLE.equalsIgnoreCase(str) )
      type = ContentType.SEQUENCE_CYCLE;
    if ( ONCE.equalsIgnoreCase(str) )
      type = ContentType.SEQUENCE_ONCE;
  }

  public static boolean isConditionalHeader(String str) {
    return str.startsWith(StoryText.CBRACE_LEFT) && !str.contains(StoryText.CBRACE_RIGHT);
  }

  @Override
  public int getContentSize() {
    if (selection >= content.size())
      return 1;
    ConditionalOptions opt = (ConditionalOptions) content.get(selection);
    return opt.lines.size();
  }

  @Override
  public Content getContent(int i) {
    if (selection >= content.size())
      return new Content(lineNumber, "", this);
    ConditionalOptions opt = (ConditionalOptions) content.get(selection);
    return opt.lines.get(i);
  }

  @Override
  public int getContentIndex(Content c) {
    if (selection >= content.size())
      return 0;
    ConditionalOptions opt = (ConditionalOptions) content.get(selection);
    return opt.lines.indexOf(c);
  }

  @SuppressWarnings("RefusedBequest")
  @Override
  public void add(Content item) {
    ConditionalOptions cond = (ConditionalOptions) content.get(content.size() - 1);
    cond.lines.add(item);
  }

  @Override
  public void initialize(Story story, Content c) throws InkRunTimeException {
    evaluate(story);
    super.initialize(story, c);
  }

  @SuppressWarnings("OverlyComplexMethod")
  private void evaluate(Story story) throws InkRunTimeException {
    switch (type) {
      case CONDITIONAL:
        for (Content c : content) {
          ConditionalOptions opt = (ConditionalOptions) c;
          if (content.indexOf(c) == content.size()-1 && ELSE.equals(opt.condition)) {
            selection = content.indexOf(c);
            return;
          }
          else {
            Object eval = Variable.evaluate(opt.condition, story);
            if (eval instanceof Boolean) {
              if ((Boolean) eval) {
                selection = content.indexOf(c);
                return;
              }
            }
            else {
              BigDecimal val = (BigDecimal) eval;
              if (val.intValue() > 0) {
                selection = content.indexOf(c);
                return;
              }
            }
          }
        }
        // Failed
        selection = content.size();
        break;
      case SEQUENCE_CYCLE:
        selection = count % content.size();
        break;
      case SEQUENCE_ONCE:
        selection = count;
        break;
      case SEQUENCE_SHUFFLE:
        selection = new Random().nextInt(content.size());
        break;
      case SEQUENCE_STOP:
        selection = count >= content.size() ? content.size() - 1 : count;
        break;
      default:
        story.logException(new InkRunTimeException("Invalid conditional type."));
    }
  }

}
