package lec.com.earthquake;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Earthquake extends Activity {
  
  ListView earthquakeListView;
  TextView quakeDetailsTextView;
  ArrayAdapter<Quake> adapterquakes;
  ArrayList<Quake> earthquakes = new ArrayList<Quake>();
   
  static final private int QUAKE_DIALOG = 1;
  Quake selectedQuake;
  private ProgressDialog progress;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.main);
    progress = new ProgressDialog(this);
    
/*    if (android.os.Build.VERSION.SDK_INT > 9) {
    	StrictMode.ThreadPolicy policy = 
    	        new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	StrictMode.setThreadPolicy(policy);
    	}
 */   
    earthquakeListView = (ListView)this.findViewById(R.id.earthquakeListView);

    earthquakeListView.setOnItemClickListener(new OnItemClickListener() {
        @Override
		public void onItemClick(AdapterView<?> _av, View _v, int _index, long arg3) {
          selectedQuake = earthquakes.get(_index);
          showDialog(QUAKE_DIALOG);
        }
      });
    
    int layoutID = android.R.layout.simple_list_item_1;
    adapterquakes = new ArrayAdapter<Quake>(this, layoutID , earthquakes);
    earthquakeListView.setAdapter(adapterquakes);
    
    // Customizing the colors of earthquakes with a anonymous getView override
    
    ListView earthquakeListViewc = (ListView) this.findViewById(R.id.earthquakeListView);
    earthquakeListViewc.setAdapter(new ArrayAdapter<Quake>(this, android.R.layout.simple_list_item_1, earthquakes) {
      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
      	// View earthquakeview = convertView;
      	
      	// Get the current earthquake
      	Quake actualquake = getItem(position);
      	
          TextView earthquakeView = (TextView) super.getView(position, convertView, parent);
//          System.out.println("getView " + position + " " + convertView + "magnitude: " + actualquake.getMagnitude());
          int magnitudeabs = (int) Math.floor(actualquake.getMagnitude());
          
          // Change of color depending of magnitude
          
          switch (magnitudeabs) {  
          case 1 : earthquakeView.setTextColor(Color.parseColor("#F0F4C3"));
                   break;
          case 2 : earthquakeView.setTextColor(Color.parseColor("#E6EE9C"));
                   break;
          case 3 : earthquakeView.setTextColor(Color.parseColor("#EEFF41"));
          		   break;
          case 4 : earthquakeView.setTextColor(Color.parseColor("#FFEB3B"));
                   break;
          case 5 : earthquakeView.setTextColor(Color.parseColor("#FF9800"));
                   break;
          case 6 : earthquakeView.setTextColor(Color.parseColor("#EF5350"));
                   break;
          case 7 : earthquakeView.setTextColor(Color.parseColor("#E65100"));
 		           break;
          case 8 : earthquakeView.setTextColor(Color.parseColor("#E64A19"));
                   break;
          case 9 : earthquakeView.setTextColor(Color.parseColor("#B71C1C"));
                   break;
          case 10 : earthquakeView.setTextColor(Color.parseColor("#D50000"));
                    break; 
          default: earthquakeView.setTextColor(Color.WHITE);
          }
      return earthquakeView;
      }
    });
    
//    refreshEarthquakes();
    
    // getting the USGS Data Asynchronous
    
    new getEarthquakesUSGS().execute();
  }
  
  public class getEarthquakesUSGS extends AsyncTask <Void, Integer, Void> {
		/*
		  Getting the Atom RSS feed from site USGS  Asynchronously
		   */
		  @Override
		  protected void onPreExecute() {
		 
		      // Clear the old earthquakes
		      earthquakes.clear();
              // Refresh the ListView
			  earthquakeListView.invalidateViews();
			  // Show the Progress Dialog
			  progress.setMessage("Downloading USGS EarthQuakes Data");
		      progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		      //progress.setIndeterminate(true);
		      progress.show();
			  
		  }

		  /*
		  Start Thread of USGS Atom Feed
		   */
		  @Override
		  protected Void doInBackground(Void... params) {
			  // Get the XML
			  URL url;
			  try {
			    String quakeFeed = getString(R.string.quake_feed);
			    url = new URL(quakeFeed);
			         
			    URLConnection connection;
			    connection = url.openConnection();
			       
			    HttpURLConnection httpConnection = (HttpURLConnection)connection; 
			    int responseCode = httpConnection.getResponseCode(); 

			    if (responseCode == HttpURLConnection.HTTP_OK) { 
			      InputStream in = httpConnection.getInputStream(); 
			          
			      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			      DocumentBuilder db = dbf.newDocumentBuilder();

			      // Parse the earthquake feed.
			      Document dom = db.parse(in);      
			      Element docEle = dom.getDocumentElement();
			        
			          
			      // Get a list of each earthquake entry.
			      NodeList nl = docEle.getElementsByTagName("entry");
			      if (nl != null && nl.getLength() > 0) {
			        for (int i = 0 ; i < nl.getLength(); i++) {
			          Element entry = (Element)nl.item(i);
			          Element title = (Element)entry.getElementsByTagName("title").item(0);
			          Element g = (Element)entry.getElementsByTagName("georss:point").item(0);
			          Element when = (Element)entry.getElementsByTagName("updated").item(0);
			          Element link = (Element)entry.getElementsByTagName("link").item(0);

			          String details = title.getFirstChild().getNodeValue();
			          String hostname = "Source : earthquake.usgs.gov" + "\n";
			          String linkString = hostname + link.getAttribute("href");

			          String point = g.getFirstChild().getNodeValue();
			          String dt = when.getFirstChild().getNodeValue();  
			          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
			          Date qdate = new GregorianCalendar(0,0,0).getTime();
			          try {
			            qdate = sdf.parse(dt);
			          } catch (ParseException e) {
			            e.printStackTrace();
			          }

			          String[] location = point.split(" ");
			          Location l = new Location("dummyGPS");
			          l.setLatitude(Double.parseDouble(location[0]));
			          l.setLongitude(Double.parseDouble(location[1]));

			          String magnitudeString = details.split(" ")[1];
			          int end =  magnitudeString.length()-1;
			          double magnitude = Double.parseDouble(magnitudeString.substring(0, end));

			          details = details.split(",")[1].trim();
			              
			          Quake quake = new Quake(qdate, details, l, magnitude, linkString);

			          // Process a newly found earthquake
			          addNewQuake(quake);
			          publishProgress((int)(((i+1)/(float)(nl.getLength()-1))*100));

			          
			        }
			      }
			    }
			  } catch (MalformedURLException e) {
			    e.printStackTrace();
			  } catch (IOException e) {
			    e.printStackTrace();
			  } catch (ParserConfigurationException e) {
			    e.printStackTrace();
			  } catch (SAXException e) {
			    e.printStackTrace();
			  }
			  finally {
			  }
//			  System.out.println("get USGS Atom RSS done ");
			  return null;
			}
		  /*
		   In case of Cancelled 
		    */
		  @Override
		  protected void onCancelled() {
		      super.onCancelled();
//		      To Do
//		      
		  }

		  /*
		  Progress in real time
		    */
		  @Override
		  protected void onProgressUpdate(Integer... values) {
		      super.onProgressUpdate(values);

		      progress.setProgress(values[0]);
		      
		  }

		  /*
		  Action to do after the task completion
		   */
		  @Override
		  protected void onPostExecute(Void result) {
		      super.onPostExecute(result);

		      earthquakeListView.invalidateViews(); // for show the listView
		      progress.cancel();
		      System.out.println("showing quakes");
		      
		  }
        }

	

    private void addNewQuake(Quake _quake) {

	  // Add the new quake to our list of earthquakes.
	  earthquakes.add(_quake);
	    
	  // Notify the array adapter of a change, not necessary per quake
	  //adapterquakes.notifyDataSetChanged();

	}
    
    
    static final private int MENU_UPDATE = Menu.FIRST;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);

      menu.add(0, MENU_UPDATE, Menu.NONE, R.string.menu_update);
                  
      return true;
    }
            
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      super.onOptionsItemSelected(item);
           
      switch (item.getItemId()) {
        case (MENU_UPDATE): {
        	
 //         refreshEarthquakes(); 
          
          new getEarthquakesUSGS().execute();
          return true; 
        }
      } 
      return false;
    }
    
    @Override
    public Dialog onCreateDialog(int id) {
      switch(id) {
        case (QUAKE_DIALOG) :
          LayoutInflater li = LayoutInflater.from(this);
          View quakeDetailsView = li.inflate(R.layout.quake_details, null);

          AlertDialog.Builder quakeDialog = new AlertDialog.Builder(this);
          quakeDialog.setTitle("Quake Time");
          quakeDialog.setView(quakeDetailsView);
          return quakeDialog.create();
      }
      return null;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
      switch(id) {
        case (QUAKE_DIALOG) :
          SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
          String dateString = sdf.format(selectedQuake.getDate()); 
          String quakeText = "Magnitude " + selectedQuake.getMagnitude() + 
                             "\n" + selectedQuake.getDetails()  + "\n" +  
                             selectedQuake.getLink();

          AlertDialog quakeDialog = (AlertDialog)dialog;
          quakeDialog.setTitle(dateString);
          TextView tv = (TextView)quakeDialog.findViewById(R.id.quakeDetailsTextView);
          tv.setText(quakeText);

          break;
      }
    }
}