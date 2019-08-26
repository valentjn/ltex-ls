package latex;

import java.util.ArrayList;
import java.util.regex.*;

public class CommandSignature {
  public enum ArgumentType {
    BRACE,
    BRACKET,
    PARENTHESIS,
  }

  public String name;
  public ArrayList<ArgumentType> argumentTypes = new ArrayList<ArgumentType>();

  private static String matchFromPosition(String text, int pos, Pattern pattern) {
    Matcher matcher = pattern.matcher(text.substring(pos));
    return (matcher.find() ? matcher.group() : "");
  }

  public String matchFromPosition(String text, int pos) {
    Pattern commandPattern = Pattern.compile("^" + Pattern.quote(name));
    Pattern commentPattern = Pattern.compile("^%.*?($|(\n[ \n\r\t]*))");
    Pattern braceArgumentPattern = Pattern.compile("^\\{[^\\}]*?\\}");
    Pattern bracketArgumentPattern = Pattern.compile("^\\[[^\\]]*?\\]");
    Pattern parenthesisArgumentPattern = Pattern.compile("^\\([^\\)]*?\\)");

    int startPos = pos;
    int nextArgumentIndex = 0;
    String match = matchFromPosition(text, pos, commandPattern);
    pos += match.length();

    for (int i = 0; i < argumentTypes.size(); i++) {
      match = matchFromPosition(text, pos, commentPattern);
      pos += match.length();
      Pattern argumentPattern = null;

      switch (argumentTypes.get(nextArgumentIndex)) {
        case BRACE: {
          argumentPattern = braceArgumentPattern;
          break;
        }
        case BRACKET: {
          argumentPattern = bracketArgumentPattern;
          break;
        }
        case PARENTHESIS: {
          argumentPattern = parenthesisArgumentPattern;
          break;
        }
      }

      match = matchFromPosition(text, pos, argumentPattern);
      if (match.isEmpty()) return "";
      pos += match.length();
    }

    return text.substring(startPos, pos);
  }
}
