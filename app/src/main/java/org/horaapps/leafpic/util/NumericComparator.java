

package org.horaapps.leafpic.util;


public class NumericComparator {

  public static final String TAG = "NumericComparator";



  public static int filevercmp (String s1, String s2) {
    String s1_suffix, s2_suffix;
    int s1_len, s2_len, result;


    int simple_cmp = strcmp (s1, s2);
    if (simple_cmp == 0)
      return 0;


    if (s1 == null || s1.length() == 0)
      return -1;
    if (s2 == null || s2.length() == 0)
      return 1;
    if (0 == strcmp (".", s1))
      return -1;
    if (0 == strcmp (".", s2))
      return 1;
    if (0 == strcmp ("..", s1))
      return -1;
    if (0 == strcmp ("..", s2))
      return 1;


    if (s1.codePointAt(0) == '.' && s2.codePointAt(0) != '.')
      return -1;
    if (s1.codePointAt(0) != '.' && s2.codePointAt(0) == '.')
      return 1;
    if (s1.codePointAt(0) == '.' && s2.codePointAt(0) == '.') {
      s1 = s1.substring(1, s1.length());
      s2 = s2.substring(1, s2.length());
    }


    s1_suffix = match_suffix (s1);
    s2_suffix = match_suffix (s2);
    s1_len = s1.length() - s1_suffix.length();
    s2_len = s2.length() - s2_suffix.length();


    if ((s1_suffix.length() > 0 || s2_suffix.length() > 0) && (s1_len == s2_len) && 0 == strncmp (s1, s2, s1_len)) {
      s1_len = s1.length();
      s2_len = s2.length();
    }

    result = verrevcmp (s1.substring(0, s1_len), s2.substring(0, s2_len));
    return result == 0 ? simple_cmp : result;
  }


  public static int verrevcmp (String s1, String s2) {
    int s1_pos = 0;
    int s2_pos = 0;
    while (s1_pos < s1.length() || s2_pos < s2.length()) {
      int first_diff = 0;
      while ((s1_pos < s1.length() && !c_isdigit (s1.codePointAt(s1_pos)))
             || (s2_pos < s2.length() && !c_isdigit (s2.codePointAt(s2_pos)))) {
        int s1_c = (s1_pos >= s1.length()) ? 0 : order (s1.codePointAt(s1_pos));
        int s2_c = (s2_pos >= s2.length()) ? 0 : order (s2.codePointAt(s2_pos));
        if (s1_c != s2_c)
          return s1_c - s2_c;
        s1_pos++;
        s2_pos++;
      }
      while (s1_pos < s1.length() && s1.codePointAt(s1_pos) == '0')
        s1_pos++;
      while (s2_pos < s2.length() && s2.codePointAt(s2_pos) == '0')
        s2_pos++;
      while (s1_pos < s1.length() && s2_pos < s2.length()
             && c_isdigit (s1.codePointAt(s1_pos))
             && c_isdigit (s2.codePointAt(s2_pos))) {
        if (first_diff == 0)
          first_diff = s1.codePointAt(s1_pos) - s2.codePointAt(s2_pos);
        s1_pos++;
        s2_pos++;
      }
      if (s1_pos < s1.length() && c_isdigit (s1.codePointAt(s1_pos)))
        return 1;
      if (s2_pos < s2.length() && c_isdigit (s2.codePointAt(s2_pos)))
        return -1;
      if (first_diff != 0)
        return first_diff;
    }
    return 0;
  }


  private static final int UNICODE_MAX = 0x10FFFF;


  private static int order (int c) {
    if (c_isdigit (c))
      return 0;
    else if (c_isalpha (c))
      return c;
    else if (c == '~')
      return -1;
    else
      return (int) c + UNICODE_MAX + 1;
  }


  private static String match_suffix (String str)
  {
    String match = "";
    boolean read_alpha = false;
    while (str.length() > 0) {
      if (read_alpha) {
        read_alpha = false;
        if (!c_isalpha (str.codePointAt(0)) && '~' != str.codePointAt(0))
          match = "";
      } else if ('.' == str.codePointAt(0)) {
        read_alpha = true;
        if (match.length() == 0)
          match = str;
      } else if (!c_isalnum (str.codePointAt(0)) && '~' != str.codePointAt(0)) {
        match = "";
      }
      str = str.substring(1, str.length());
    }
    return match;
  }


  private static int strcmp(final String s1, final String s2) {
    return s1.compareTo(s2);
  }


  private static int strncmp(final String s1, final String s2, int len) {
    int len1 = Math.min(len, s1.length());
    int len2 = Math.min(len, s2.length());
    return s1.substring(0, len1).compareTo(s2.substring(0, len2));
  }

  private static boolean c_isdigit(int c) {
    return Character.isDigit(c);
  }

  private static boolean c_isalpha(int c) {
    return Character.isLetter(c);
  }

  private static boolean c_isalnum(int c) {
    return Character.isLetterOrDigit(c);
  }
}
