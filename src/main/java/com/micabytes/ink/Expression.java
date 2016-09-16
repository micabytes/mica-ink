package com.micabytes.ink;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/* Based on https://github.com/uklimaschewski/EvalEx */
@SuppressWarnings("ALL")
public class Expression {
  /// The {@link MathContext} to use for calculations.
  private MathContext mc = null;
  /// The original infix expression.
  private String expression = null;
  /// The cached RPN (Reverse Polish Notation) of the expression.
  private List<String> rpn = null;
  /// All defined operators with name and implementation.
  private Map<String, Operator> operators = new TreeMap<String, Operator>(String.CASE_INSENSITIVE_ORDER);
  /// What character to use for decimal separators.
  private static final char decimalSeparator = '.';
  /// What character to use for minus sign (negative values).
  private static final char minusSign = '-';
  /// The BigDecimal representation of the left parenthesis, used for parsing varying numbers of function parameters.
  private static final BigDecimal PARAMS_START = new BigDecimal(0);

  /// The expression evaluators exception class.
  public static class ExpressionException extends RuntimeException {
    public ExpressionException(String message) {
      super(message);
    }
  }

  /// Abstract definition of a supported operator. An operator is defined by its name (pattern), precedence and if it is left- or right associative.
  public abstract class Operator {
    /// This operators name (pattern).
    private String oper;
    /// Operators precedence.
    private int precedence;
    /// Operator is left associative.
    private boolean leftAssoc;

    /**
     * Creates a new operator.
     *
     * @param oper       The operator name (pattern).
     * @param precedence The operators precedence.
     * @param leftAssoc  <code>true</code> if the operator is left associative,
     *                   else <code>false</code>.
     */
    public Operator(String oper, int precedence, boolean leftAssoc) {
      this.oper = oper;
      this.precedence = precedence;
      this.leftAssoc = leftAssoc;
    }

    public String getOper() {
      return oper;
    }

    public int getPrecedence() {
      return precedence;
    }

    public boolean isLeftAssoc() {
      return leftAssoc;
    }

    /**
     * Implementation for this operator.
     *
     * @param v1 Operand 1.
     * @param v2 Operand 2.
     * @return The result of the operation.
     */
    public abstract BigDecimal eval(Object v1, Object v2);
  }

  private class Tokenizer implements Iterator<String> {
    private int pos = 0;
    private String input;
    private String previousToken;

    public Tokenizer(String input) {
      this.input = input.trim();
    }

    @Override
    public boolean hasNext() {
      return (pos < input.length());
    }

    private char peekNextChar() {
      if (pos < (input.length() - 1)) {
        return input.charAt(pos + 1);
      } else return 0;
    }

    @Override
    public String next() {
      StringBuilder token = new StringBuilder();
      if (pos >= input.length()) {
        return previousToken = null;
      }
      char ch = input.charAt(pos);
      while (Character.isSpaceChar(ch) && pos < input.length()) {
        ch = input.charAt(++pos);
      }
      if (Character.isDigit(ch)) {
        while ((Character.isDigit(ch) || ch == decimalSeparator
            || ch == 'e' || ch == 'E'
            || (ch == minusSign && token.length() > 0
            && ('e' == token.charAt(token.length() - 1) || 'E' == token.charAt(token.length() - 1)))
            || (ch == '+' && token.length() > 0
            && ('e' == token.charAt(token.length() - 1) || 'E' == token.charAt(token.length() - 1)))
        ) && (pos < input.length())) {
          token.append(input.charAt(pos++));
          ch = pos == input.length() ? 0 : input.charAt(pos);
        }
      } else if (ch == minusSign
          && Character.isDigit(peekNextChar())
          && ("(".equals(previousToken) || ",".equals(previousToken)
          || previousToken == null || operators
          .containsKey(previousToken))) {
        token.append(minusSign);
        pos++;
        token.append(next());
      } else if (Character.isLetter(ch) || (ch == '_') || (ch == '\"')) {
        while ((Character.isLetter(ch) || Character.isDigit(ch) || (ch == '_') || (ch == '.') || (ch == '\"'))
            && (pos < input.length())) {
          token.append(input.charAt(pos++));
          ch = pos == input.length() ? 0 : input.charAt(pos);
        }
      } else if (ch == '(' || ch == ')' || ch == ',') {
        token.append(ch);
        pos++;
      } else {
        while (!Character.isLetter(ch) && !Character.isDigit(ch)
            && ch != '_' && !Character.isSpaceChar(ch)
            && ch != '(' && ch != ')' && ch != ','
            && (pos < input.length())) {
          token.append(input.charAt(pos));
          pos++;
          ch = pos == input.length() ? 0 : input.charAt(pos);
          if (ch == minusSign) {
            break;
          }
        }
        if (!operators.containsKey(token.toString())) {
          throw new ExpressionException("Unknown operator '" + token + "' at position " + (pos - token.length() + 1));
        }
      }
      return previousToken = token.toString();
    }

    @Override
    public void remove() {
      throw new ExpressionException("remove() not supported");
    }

    public int getPos() {
      return pos;
    }

  }

  /**
   * Creates a new expression instance from an expression string with a given
   * default match context of {@link MathContext#DECIMAL32}.
   *
   * @param expression The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or
   *                   <code>"sin(y)>0 & max(z, 3)>3"</code>
   */
  public Expression(String expression) {
    this(expression, MathContext.DECIMAL32);
  }

  /**
   * Creates a new expression instance from an expression string with a given
   * default match context.
   *
   * @param expression         The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or
   *                           <code>"sin(y)>0 & max(z, 3)>3"</code>
   * @param defaultMathContext The {@link MathContext} to use by default.
   */
  public Expression(String expression, MathContext defaultMathContext) {
    this.mc = defaultMathContext;
    this.expression = expression;
    addOperator(new Operator("+", 20, true) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return ((BigDecimal) v1).add((BigDecimal) v2, mc);
      }
    });
    addOperator(new Operator("-", 20, true) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return ((BigDecimal) v1).subtract((BigDecimal) v2, mc);
      }
    });
    addOperator(new Operator("*", 30, true) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return ((BigDecimal) v1).multiply((BigDecimal) v2, mc);
      }
    });
    addOperator(new Operator("/", 30, true) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return ((BigDecimal) v1).divide((BigDecimal) v2, mc);
      }
    });
    addOperator(new Operator("%", 30, true) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return ((BigDecimal) v1).remainder((BigDecimal) v2, mc);
      }
    });
    addOperator(new Operator("&&", 4, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        boolean b1;
        boolean b2;
        if (v1 instanceof  Boolean)
          b1 = ((Boolean) v1).booleanValue();
        else
          b1 = !((BigDecimal) v1).equals(BigDecimal.ZERO);
        if (v2 instanceof Boolean)
          b2 = ((Boolean) v2).booleanValue();
        else
          b2 = !((BigDecimal) v2).equals(BigDecimal.ZERO);
        return b1 && b2 ? BigDecimal.ONE : BigDecimal.ZERO;
      }
    });

    addOperator(new Operator("||", 2, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        boolean b1;
        boolean b2;
        if (v1 instanceof  Boolean)
          b1 = ((Boolean) v1).booleanValue();
        else
          b1 = !((BigDecimal) v1).equals(BigDecimal.ZERO);
        if (v2 instanceof Boolean)
          b2 = ((Boolean) v2).booleanValue();
        else
          b2 = !((BigDecimal) v2).equals(BigDecimal.ZERO);
        return b1 || b2 ? BigDecimal.ONE : BigDecimal.ZERO;
      }
    });

    addOperator(new Operator(">", 10, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return ((BigDecimal) v1).compareTo((BigDecimal) v2) == 1 ? BigDecimal.ONE : BigDecimal.ZERO;
      }
    });

    addOperator(new Operator(">=", 10, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return ((BigDecimal) v1).compareTo((BigDecimal) v2) >= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
      }
    });

    addOperator(new Operator("<", 10, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return ((BigDecimal) v1).compareTo((BigDecimal) v2) == -1 ? BigDecimal.ONE
            : BigDecimal.ZERO;
      }
    });

    addOperator(new Operator("<=", 10, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return ((BigDecimal) v1).compareTo((BigDecimal) v2) <= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
      }
    });

    addOperator(new Operator("=", 7, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        if (v1 instanceof BigDecimal && v2 instanceof BigDecimal)
          return ((BigDecimal) v1).compareTo((BigDecimal) v2) == 0 ? BigDecimal.ONE : BigDecimal.ZERO;
        if (v1 instanceof String && v2 instanceof String)
          return (stripStringParameter((String) v1)).equals(stripStringParameter((String) v2)) ? BigDecimal.ONE : BigDecimal.ZERO;
        throw new ExpressionException("Both sides of an expression must be either numerical or a string");
      }
    });
    addOperator(new Operator("==", 7, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return operators.get("=").eval(v1, v2);
      }
    });

    addOperator(new Operator("!=", 7, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return operators.get("=").eval(v1, v2) == BigDecimal.ONE ? BigDecimal.ZERO : BigDecimal.ONE;
      }
    });
    addOperator(new Operator("<>", 7, false) {
      @Override
      public BigDecimal eval(Object v1, Object v2) {
        return operators.get("!=").eval(v1, v2);
      }
    });

    /*
    addFunction(new KnotFunction("NOT", 1) {
      @Override
      public BigDecimal eval(List<BigDecimal> parameters) {
        boolean zero = parameters.get(0).compareTo(BigDecimal.ZERO) == 0;
        return zero ? BigDecimal.ONE : BigDecimal.ZERO;
      }
    });

    addFunction(new KnotFunction("RANDOM", 0) {
      @Override
      public BigDecimal eval(List<BigDecimal> parameters) {
        double d = Math.random();
        return new BigDecimal(d, mc);
      }
    });
    */

  }

  private boolean isNumber(String st) {
    if (st.charAt(0) == minusSign && st.length() == 1) return false;
    if (st.charAt(0) == '+' && st.length() == 1) return false;
    if (st.charAt(0) == 'e' || st.charAt(0) == 'E') return false;
    for (char ch : st.toCharArray()) {
      if (!Character.isDigit(ch) && ch != minusSign
          && ch != decimalSeparator
          && ch != 'e' && ch != 'E' && ch != '+')
        return false;
    }
    return true;
  }

  private boolean isStringParameter(String st) {
    if (st.startsWith("\"") && st.endsWith("\""))
      return true;
    if (st.startsWith("\'") && st.endsWith("\'"))
      return true;
    return false;
  }

  private String stripStringParameter(String st) {
    if (st.startsWith("\"") && st.endsWith("\""))
      return st.substring(1, st.length() - 1);
    if (st.startsWith("\'") && st.endsWith("\'"))
      return st.substring(1, st.length() - 1);
    return st;
  }

  private List<String> shuntingYard(String expression, VariableMap story) throws InkRunTimeException {
    List<String> outputQueue = new ArrayList<String>();
    Stack<String> stack = new Stack<String>();
    Tokenizer tokenizer = new Tokenizer(expression);
    String lastFunction = null;
    String previousToken = null;
    while (tokenizer.hasNext()) {
      String token = tokenizer.next();
      if (isNumber(token)) {
        outputQueue.add(token);
      } else if (isStringParameter(token)) {
        outputQueue.add(token);
      } else if (story.hasVariable(token)) {
        outputQueue.add(token);
      } else if (story.hasFunction(token)) {
        stack.push(token);
        lastFunction = token;
      } else if (story.checkObject(token)) {
        stack.push(token);
        lastFunction = token;
      } else if (Character.isLetter(token.charAt(0))) {
        stack.push(token);
      } else if (",".equals(token)) {
        while (!stack.isEmpty() && !"(".equals(stack.peek())) {
          outputQueue.add(stack.pop());
        }
        if (stack.isEmpty()) {
          throw new ExpressionException("Parse error for function '"
              + lastFunction + "'");
        }
      } else if (operators.containsKey(token)) {
        Operator o1 = operators.get(token);
        String token2 = stack.isEmpty() ? null : stack.peek();
        while (token2 != null &&
            operators.containsKey(token2)
            && ((o1.isLeftAssoc() && o1.getPrecedence() <= operators
            .get(token2).getPrecedence()) || (o1
            .getPrecedence() < operators.get(token2)
            .getPrecedence()))) {
          outputQueue.add(stack.pop());
          token2 = stack.isEmpty() ? null : stack.peek();
        }
        stack.push(token);
      } else if ("(".equals(token)) {
        if (previousToken != null) {
          if (isNumber(previousToken)) {
            throw new ExpressionException(
                "Missing operator at character position "
                    + tokenizer.getPos());
          }
          // if the ( is preceded by a valid function, then it
          // denotes the start of a parameter list
          if (story.hasFunction(previousToken) || story.checkObject(previousToken)) {
            outputQueue.add(token);
          }
        }
        stack.push(token);
      } else if (")".equals(token)) {
        while (!stack.isEmpty() && !"(".equals(stack.peek())) {
          outputQueue.add(stack.pop());
        }
        if (stack.isEmpty()) {
          throw new InkRunTimeException("Mismatched parentheses");
        }
        stack.pop();
        if (!stack.isEmpty()
            && (story.hasFunction(stack.peek()) || story.checkObject(stack.peek()))) {
          outputQueue.add(stack.pop());
        }
      }
      previousToken = token;
    }
    while (!stack.isEmpty()) {
      String element = stack.pop();
      if ("(".equals(element) || ")".equals(element)) {
        throw new InkRunTimeException("Mismatched parentheses");
      }
      if (!operators.containsKey(element)) {
        throw new InkRunTimeException("Unknown operator or function: "
            + element);
      }
      outputQueue.add(element);
    }
    return outputQueue;
  }

  /**
   * Evaluates the expression.
   *
   * @return The result of the expression.
   */
  public Object eval(VariableMap story) throws InkRunTimeException {
    Stack<Object> stack = new Stack<>();
    for (String token : getRPN(story)) {
      if (operators.containsKey(token)) {
        Object v1 = stack.pop();
        Object v2 = stack.pop();
        stack.push(operators.get(token).eval(v2, v1));
      } else if (story.hasVariable(token)) {
        Object obj = story.getValue(token);
        if (obj instanceof Boolean) {
          if (((Boolean) obj).booleanValue())
            stack.push(BigDecimal.ONE);
          else
            stack.push(BigDecimal.ZERO);
        } else if (obj instanceof BigDecimal) {
          stack.push(((BigDecimal) obj).round(mc));
        } else {
          stack.push(obj);
        }
      } else if (story.hasFunction(token)) {
        Function f = story.getFunction(token);
        List<Object> p = new ArrayList<>(f.isFixedNumParams() ? f.getNumParams() : 0);
        // pop parameters off the stack until we hit the start of
        // this function's parameter list
        while (!stack.isEmpty() && stack.peek() != PARAMS_START) {
          Object param = stack.pop();
          if (param instanceof String)
            p.add(0, stripStringParameter((String) param));
          else
            p.add(0, param);
        }
        if (stack.peek() == PARAMS_START) {
          stack.pop();
        }
        if (f.isFixedNumParams() && p.size() != f.getNumParams()) {
          throw new ExpressionException("Function " + token + " expected " + f.getNumParams() + " parameters, got " + p.size());
        }
        Object fResult = f.eval(p, story);
        stack.push(fResult);
      } else if (story.checkObject(token)) {
        String var = token.substring(0, token.indexOf("."));
        String function = token.substring(token.indexOf(".") + 1);
        Object val = story.getValue(var);
        if (val == null) {
          stack.push("");
        } else {
          List<Object> p = new ArrayList<>();
          // pop parameters off the stack until we hit the start of  this function's parameter list
          while (!stack.isEmpty() && stack.peek() != PARAMS_START) {
            Object obj = stack.pop();
            if (obj != null) {
              if (obj instanceof String)
                p.add(0, stripStringParameter((String) obj));
              else
                p.add(0, obj);
            }
          }
          if (stack.peek() == PARAMS_START) {
            stack.pop();
          }
          Class[] paramTypes = new Class[p.size()];
          Object[] params = new Object[p.size()];
          for (int i = 0; i < p.size(); i++) {
            paramTypes[i] = p.get(i).getClass();
            params[i] = p.get(i);
          }
          Class valClass = val.getClass();
          try {
            Method m = valClass.getMethod(function, paramTypes);
            Object fResult = m.invoke(val, params);
            if (fResult instanceof Integer)
              fResult = BigDecimal.valueOf((Integer) fResult);
            else if (fResult instanceof Float)
              fResult = BigDecimal.valueOf((Float) fResult);
            else if (fResult instanceof Double)
              fResult = BigDecimal.valueOf((Double) fResult);
            stack.push(fResult);
          } catch (NoSuchMethodException e) {
            String errMsg = "Could not identify a method " + function + " on variable " + var + " (" + var.toString() + ", " + valClass.getName() + ") with the parameters:";
            for (int i = 0; i < paramTypes.length; i++)
              errMsg += " " + paramTypes[i].getName();
            errMsg += ". " + story.debugInfo();
            throw new InkRunTimeException(errMsg, e);
          } catch (InvocationTargetException e) {
            String errMsg = "Could not invoke a method " + function + " on variable " + var + " (" + var.toString() + ", " + valClass.getName() + ") with the parameters:";
            for (int i = 0; i < paramTypes.length; i++)
              errMsg += " " + paramTypes[i].getName();
            errMsg += ". " + story.debugInfo();
            throw new InkRunTimeException(errMsg, e);
          } catch (IllegalAccessException e) {
            String errMsg = "Could not access a method " + function + " on variable " + var + " (" + var.toString() + ", " + valClass.getName() + ") with the parameters:";
            for (int i = 0; i < paramTypes.length; i++)
              errMsg += " " + paramTypes[i].getName();
            errMsg += ". " + story.debugInfo();
            throw new InkRunTimeException(errMsg, e);
          }
        }
      } else if ("(".equals(token)) {
        stack.push(PARAMS_START);
      } else {
        if (isNumber(token))
          stack.push(new BigDecimal(token, mc));
        else
          stack.push(token);
      }
    }
    Object obj = stack.pop();
    if (obj instanceof BigDecimal)
      return ((BigDecimal) obj).stripTrailingZeros();
    if (obj instanceof String) {
      String s = (String) obj;
      if (isStringParameter(s))
        return stripStringParameter(s);
      return s;
    }
    return obj;
  }

  /**
   * Sets the precision for expression evaluation.
   *
   * @param precision The new precision.
   * @return The expression, allows to chain methods.
   */
  public Expression setPrecision(int precision) {
    this.mc = new MathContext(precision);
    return this;
  }

  /**
   * Sets the rounding mode for expression evaluation.
   *
   * @param roundingMode The new rounding mode.
   * @return The expression, allows to chain methods.
   */
  public Expression setRoundingMode(RoundingMode roundingMode) {
    this.mc = new MathContext(mc.getPrecision(), roundingMode);
    return this;
  }

  /**
   * Adds an operator to the list of supported operators.
   *
   * @param operator The operator to add.
   * @return The previous operator with that name, or <code>null</code> if
   * there was none.
   */
  public Operator addOperator(Operator operator) {
    return operators.put(operator.getOper(), operator);
  }

  /**
   * Adds a function to the list of supported functions
   *
   * @param function The function to add.
   * @return The previous operator with that name, or <code>null</code> if
   * there was none.
   *
  public KnotFunction addFunction(KnotFunction function) {
  return story.functions.put(function.getName(), function);
  }
   */

  /**
   * Cached access to the RPN notation of this expression, ensures only one
   * calculation of the RPN per expression instance. If no cached instance
   * exists, a new one will be created and put to the cache.
   *
   * @return The cached RPN instance.
   * @param story
   */
  private List<String> getRPN(VariableMap story) throws InkRunTimeException {
    if (rpn == null) {
      rpn = shuntingYard(this.expression, story);
      validate(rpn, story);
    }
    return rpn;
  }

  /**
   * Check that the expression have enough numbers and variables to fit the
   * requirements of the operators and functions, also check
   * for only 1 result stored at the end of the evaluation.
   */
  private void validate(List<String> rpn, VariableMap story) {
    /*-
		* Thanks to Norman Ramsey:
		* http://http://stackoverflow.com/questions/789847/postfix-notation-validation
		*/
    int counter = 0;
    Stack<Integer> params = new Stack<Integer>();
    for (String token : rpn) {
      if ("(".equals(token)) {
        // is this a nested function call?
        if (!params.isEmpty()) {
          // increment the current function's param count
          // (the return of the nested function call
          // will be a parameter for the current function)
          params.set(params.size() - 1, params.peek() + 1);
        }
        // start a new parameter count
        params.push(0);
      } else if (!params.isEmpty()) {
        if (story.hasFunction(token) || story.checkObject(token)) {
          // remove the parameters and the ( from the counter
          counter -= params.pop() + 1;
        } else {
          // increment the current function's param count
          params.set(params.size() - 1, params.peek() + 1);
        }
      } else if (operators.containsKey(token)) {
        //we only have binary operators
        counter -= 2;
      }
      if (counter < 0) {
        throw new ExpressionException("Too many operators or functions at: "
            + token);
      }
      counter++;
    }
    if (counter > 1) {
      throw new ExpressionException("Too many numbers or variables");
    } else if (counter < 1) {
      throw new ExpressionException("Empty expression");
    }
  }

}
