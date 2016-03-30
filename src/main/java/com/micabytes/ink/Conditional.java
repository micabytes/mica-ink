package com.micabytes.ink;

import org.jetbrains.annotations.NonNls;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Conditional extends Container {
  private static final String CONDITIONAL_DASH = "-";
  private static final String CONDITIONAL_COLON = ":";
  private int selection;

  private static final class ConditionalOptions extends Content {
    String condition;
    List<Content> lines = new ArrayList<>();

    ConditionalOptions(String cond) {
      condition = cond;
    }
  }

  Conditional(int l, String line, Container current) throws InkParseException {
    lineNumber = l;
    type = ContentType.CONDITIONAL;
    content = new ArrayList<>();
    String str = line.substring(1).trim();
    if (!str.isEmpty()) {
      if (!str.endsWith(":"))
        throw new InkParseException("Line Number " + l + ": Error in conditional block; condition not ended by \':\'.");
      if (str.startsWith(CONDITIONAL_DASH))
        str = str.substring(1).trim();
      String condition = str.substring(0, str.length()-1).trim();
      checkSequenceCondition(condition);
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
      if (str.startsWith(CONDITIONAL_DASH) && !str.startsWith(Story.DIVERT)) {
        if (!str.endsWith(CONDITIONAL_COLON))
          throw new InkParseException("Line Number " + l + ": Error in conditional block; condition not ended by \':\'.");
        String condition = str.substring(1, str.length()-1).trim();
        content.add(new ConditionalOptions(condition));
      }
      else {
        InkParser.parseLine(l, str, this);
      }
    }
    else {
      if (str.startsWith(CONDITIONAL_DASH) && !str.startsWith(Story.DIVERT)) {
        String first = str.substring(1).trim();
        content.add(new ConditionalOptions(""));
        InkParser.parseLine(l, first, this);
      }
      else {
        InkParser.parseLine(l, str, this);
      }
    }
  }

  private void checkSequenceCondition(@NonNls String str) {
    if ( "stopping".equalsIgnoreCase(str) )
      type = ContentType.SEQUENCE_STOP;
    if ( "shuffle".equalsIgnoreCase(str) )
      type = ContentType.SEQUENCE_SHUFFLE;
    if ( "cycle".equalsIgnoreCase(str) )
      type = ContentType.SEQUENCE_CYCLE;
    if ( "once".equalsIgnoreCase(str) )
      type = ContentType.SEQUENCE_ONCE;
  }

  public static boolean isConditionalHeader(String str) {
    return str.startsWith(CBRACE_LEFT) && !str.contains(CBRACE_RIGHT);
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
  public int getContentIndex(Container c) {
    if (selection >= content.size())
      return 0;
    ConditionalOptions opt = (ConditionalOptions) content.get(selection);
    return opt.lines.indexOf(c);
  }

  @Override
  public void add(Content item) {
    ConditionalOptions cond = (ConditionalOptions) content.get(content.size() - 1);
    cond.lines.add(item);
  }

  @SuppressWarnings("OverlyComplexMethod")
  public void evaluate(Story story) throws InkRunTimeException {
    switch (type) {
      case CONDITIONAL:
        for (Content c : content) {
          ConditionalOptions opt = (ConditionalOptions) c;
          if (content.indexOf(c) == content.size()-1 && "else".equals(opt.condition)) {
            selection = content.indexOf(c);
            return;
          }
          else {
            BigDecimal val = Variable.evaluate(opt.condition, story);
            if (val.intValue() > 0) {
              selection = content.indexOf(c);
              return;
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
        if (count >= content.size())
          selection = content.size()-1;
        else
          selection = count;
        break;
      default:
        // TODO: Error
        break;
    }
  }

}
