import java.text.SimpleDateFormat

public final class StringUtil {

  public static String toCamelCase(Object obj)
  {
    String text = obj.toString()
    String[] words = text.split(" ").collect { word ->
      word.toLowerCase().capitalize()
    }
    words[0] = words[0].toLowerCase()
    return words.join("")
  }

  public static String toKey(String text)
  {
    return text.toLowerCase().replaceAll("\\s+", "_")
  }

  public static String toDate(Object date)
  {
    if (date instanceof Date)
    {
      return new SimpleDateFormat("MM/dd/yyyy").format(date)
    }
    else if (date instanceof String)
    {
      return date
    }
    return ""
  }

}


