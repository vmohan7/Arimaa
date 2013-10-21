package ai_util;

import java.util.*;

// Handles time control for the engine

public class TimeControl {

  // TODO add set threshold command

  private long nominal_search_time = Long.MAX_VALUE;
  private long panic_search_time = Long.MAX_VALUE;
  private long mate_search_time = Long.MAX_VALUE;
  private boolean isPonderSearch = false;
  private int score_threshold = -50;
  private int search_depth_threshold = 4;


  private long search_start_time = 0;

  public TimeControl() {
  }

  public void setThreshold( int threshold ) {
    this.score_threshold = threshold;
  }

  public void setSearchDepthThreshold( int search_depth_threshold ) {
    this.search_depth_threshold = search_depth_threshold;
  }


  public boolean isPonderSearch() {
    return isPonderSearch;
  }

  public void setPondering( boolean isPonderSearch ) {
    this.isPonderSearch = isPonderSearch;
  }


  // Will search exactly this long
  public void setFixedSearchTime( long time_ms ) {
    this.nominal_search_time = time_ms;
    this.panic_search_time = time_ms;
    this.mate_search_time = time_ms;
  }


  // Will search at least this long
  public void setNominalSearchTime( long time_ms ) {
    this.nominal_search_time = time_ms;
  }

  // Will search at most this long, unless a mate score
  public void setPanicSearchTime( long time_ms ) {
    this.panic_search_time = time_ms;
    this.mate_search_time = time_ms;
  }


  // Will search this long, if going to be mated
  public void setMateSearchTime( long time_ms ) {
    this.mate_search_time = time_ms;
  }

  public void setSearchStartTime() {
    this.search_start_time = System.currentTimeMillis();
  }

  public long getElapsedSearchTime() {
    long current_time = System.currentTimeMillis();
    return ( current_time - this.search_start_time );
  }


  public long nominalTimeLimit() {
    return nominal_search_time;
  }

  public long maxTimeLimit() {
    return panic_search_time;
  }


  // returns true if enough time has elapsed to allow an easy move
  // OK to easy move if one third nominal time used up
  public boolean isEasyMoveTimeOK() {

    // Don't stop search if pondering
    if ( !isPonderSearch ) {
      if ( getElapsedSearchTime() > nominal_search_time/3 ) {
        return true;
      }
    }
    return false;
  }


  public boolean isTimeExpired(int score) {
    return isTimeExpired(score,99999);
  }

  // Called to determine if search time has expired
  public boolean isTimeExpired( int score, int search_depth ) {

    // If pondering, search forever
    if ( isPonderSearch ) {
      return false;
    }

    // Determine current time limit based on search status

    // If the score is bad, or search is too shallow
    long current_time_limit = nominal_search_time;
    // Allow up to max_search_time
    if ( score <= score_threshold || search_depth<=search_depth_threshold ) {
      current_time_limit = panic_search_time;
    }
    // If Score is a forced loss, add more time up to mate_search_time
    if ( score <= -30000 ) {
      current_time_limit = mate_search_time;
    }

    // Check if time has expired
    if ( getElapsedSearchTime() > current_time_limit )  {
      return true;
    }

    return false;
  }



  public String command( String command ) {
    String result = "time: unknown command";

    // Tokenize the command and force to lower case
    StringTokenizer tokenizer = new StringTokenizer( command, "= ", false );
    ArrayList token_list = new ArrayList();
    while ( tokenizer.hasMoreTokens() ) {
      token_list.add( tokenizer.nextToken().toLowerCase() );
    }


    // If an exception is thrown command is rejected
    try {

      // First token is the command
      String token = (String)token_list.get(0);

      if ( token.equals("status") ) {
        result = this.toString();
      }


      if ( token.equals("max_st") ) {
        String temp = (String) token_list.get(1);
        panic_search_time = Long.parseLong(temp);
        result = "Max Search Time: "+Util.toTimeString(panic_search_time);
      }

      if ( token.equals("nom_st") ) {
        String temp = (String) token_list.get(1);
        nominal_search_time = Long.parseLong(temp);
        result = "Nominal Search Time: "+Util.toTimeString(nominal_search_time);
      }

    // Something went wrong, report the error!
    } catch ( Exception e ) {
      result = "time: invalid command syntax";
    }

    return result;
  }

  public String toString() {
    String result = "";
    result += "Time Control Status\n";
    result += "Nominal Search Time: "+ Util.toTimeString(nominal_search_time)+"\n";
    result += "Max Search Time: "+ Util.toTimeString(panic_search_time) +"\n";
    result += "isPonderSearch: "+ isPonderSearch;
    return result;
  }

}

