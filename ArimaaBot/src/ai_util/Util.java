package ai_util;

import java.util.*;
import java.text.*;

public class Util {



  private static final int loBitSetTab[] = {
    0
  };

  public static int LastOne( int val )
  {
    int tmp;

    tmp = val & 0xff;
    if ( tmp > 0 ) {
      return loBitSetTab[tmp];
    }

    tmp = (val >> 8) & 0xff;
    if ( tmp > 0 ) {
      return loBitSetTab[tmp] + 7;
    }

    tmp = (val >> 16) & 0xff;
    if ( tmp > 0 ) {
      return loBitSetTab[tmp] + 15;
    }

    tmp = (val >> 24) & 0xff;
    if ( tmp > 0 ) {
      return loBitSetTab[tmp] + 23;
    }

    return -1;
  }

  // Returns a nicely formatted string showing hash hit stats
  public static String ProbStats(String description, long total_probes,
                           long total_hits) {
    String result = "";

    // Description of type of move
    result += Util.LeftJustify(10, description);

    String percent_any_cuts = " XX.XX";
    if (total_probes != 0) {
      double percent = ( (double) total_hits) * 100 / ( (double) total_probes);
      percent_any_cuts = Util.format("#00.00", percent);
      percent_any_cuts = Util.RightJustify(6, percent_any_cuts);
    }

    result += percent_any_cuts + " ";
    result += Util.Pad(10, total_hits);
    result += Util.Pad(10, total_probes);

    result += "\n";

    return result;
  }


  private static final int hiBitSetTab[] = {
    0, 1, 2, 2, 3, 3, 3, 3,
    4, 4, 4, 4, 4, 4, 4, 4,
    5, 5, 5, 5, 5, 5, 5, 5,
    5, 5, 5, 5, 5, 5, 5, 5,
    6, 6, 6, 6, 6, 6, 6, 6,
    6, 6, 6, 6, 6, 6, 6, 6,
    6, 6, 6, 6, 6, 6, 6, 6,
    6, 6, 6, 6, 6, 6, 6, 6,
    7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7,
    7, 7, 7, 7, 7, 7, 7, 7,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8,
    8, 8, 8, 8, 8, 8, 8, 8
  };


  // returns most significant set bit
  // if all zero returns -1
  public static int FirstOne( long val ) {

    // Check the high32 bits
    int tmp = (int)(val >> 32);
    if (tmp!=0) {
      return FirstOne(tmp)+32;
    }

    // Check the low 32 bits
    return FirstOne((int)(val&0xffffffff));
  }


  // returns most significant set bit
  // if all zero returns -1
  public static int FirstOne( int val)
  {
    int tmp;

    tmp = (val >> 24) & 0xff;
    if (tmp!=0) {
      return hiBitSetTab[tmp] + 23;
    }

    tmp = (val >> 16) & 0xff;
    if (tmp!=0) {
      return hiBitSetTab[tmp] + 15;
    }

    tmp = (val >> 8) & 0xff;
    if (tmp!=0) {
      return hiBitSetTab[tmp] + 7;
    }

    return hiBitSetTab[val & 0xff]-1;
  }

  // Population count
  public static int PopCnt(long val) {
    int count = 0;

    while(val != 0) {
      count++;
      val &= val - 1;
    }

    return(count);
  }

  // Population count
  public static int PopCnt(int val) {
    val -= (0xaaaaaaaa & val) >>> 1;
    val = (val & 0x33333333) + ((val >>> 2) & 0x33333333);
    val = val + (val >>> 4) & 0x0f0f0f0f;
    val += val >>> 8;
    val += val >>> 16;
    return val & 0xff;
  }

  // Converts time to a nicely formatted text string
  public static String toTimeString( long time_in_milliseconds ) {

    String result = "";

    // Handle negative(?) length of time
    if ( time_in_milliseconds < 0 ) {
      result += "-";
      time_in_milliseconds = -time_in_milliseconds;
    }


    long hours = time_in_milliseconds / 1000 / 60 / 60;
    long minutes = time_in_milliseconds / 1000 / 60 % 60;
    long seconds = time_in_milliseconds/1000 % 60;
    long centiseconds = time_in_milliseconds/10%100;

    if ( hours > 0 )
      result += Util.format("00",hours)+":"+Util.format("00", minutes)+":"+Util.format("00",seconds);
    else
      result += Util.format("00",minutes)+":"+Util.format("00",seconds)+"."+Util.format("00",centiseconds);

    return result;
  }

  public static String format( String format_string, double number ) {
    DecimalFormat df = new DecimalFormat( format_string );
    String result = df.format( number );
    return result;
  }

  public static String format( String format_string, long number ) {
    DecimalFormat df = new DecimalFormat( format_string );
    String result = df.format( number );
    return result;
  }

  // right justifies a number in a fixed width field
  public static String Pad( int width, long value ) {
    String temp = (new Long(value)).toString();

    String result = "";
    for ( int i=temp.length(); i<width; i++ )
      result+=" ";

    result += temp;

    return result;
  }

  // Left justifies a text string in a fixed width field
  // String is truncated to fit in field
  public static String LeftJustify( int width, String text ) {
    String result = text;
    for ( int i=text.length(); i<width; i++ )
      result += " ";
    result = result.substring( 0, width );
    return result;
  }

  // Right justifies a text string in a fixed width field
  // String is truncated to fit in field
  public static String RightJustify( int width, String text ) {
    String result = "";
    for ( int i=text.length(); i<width; i++ )
      result += " ";
    result += text;
    result = result.substring( 0, width );
    return result;
  }



  /**
   * Lists all the subsets of the given set!!!!!!
   * @param d long
   */
  public static void enumsets(long d)
  {
      long n = 0;
      do {
          System.out.println(n);
          n = (n-d) & d;
      } while (n!=0);
  }


  public static void main( String args[] ) {
    long test = 0x11L;

    enumsets(0x131);
/*
    int result = FirstOne(test);
    System.out.println(result);

    for ( int i=-10; i<=10; i++ ) {
      System.out.println(i);
      int temp = i;
      while ( temp != 0 ) {
        int new_temp  = temp & (temp-1);
        System.out.println(" "+temp+" "+new_temp+" "+(temp-new_temp));
        temp = new_temp;
      }
    }
*/
  }

}
