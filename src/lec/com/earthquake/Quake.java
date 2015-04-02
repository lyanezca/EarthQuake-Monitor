package lec.com.earthquake;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.location.Location;


public class Quake {
  private Date date;
  private String details;
  private Location location;
  private double magnitude;
  private String link;

  public Date getDate() { return date; }
  public String getDetails() { return details; }
  public Location getLocation() { return location; }
  public double getMagnitude() { return magnitude; }
  public String getLink() { return link; }
  
  public Quake(Date _d, String _det, Location _loc, double _mag, String _link) {
    date = _d;
    details = _det;
    location = _loc;
    magnitude = _mag;
    link = _link;
  }
  public Object getItem(int position){
	  return position;
  }
  
  @Override
  public String toString() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US);
    String dateString = sdf.format(date);
    String eqText = "When: " + dateString + " Magnitude: " + magnitude + " Where: " + details;

    return eqText;
  }
}