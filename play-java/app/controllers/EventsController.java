package controllers;

import play.mvc.*;
import play.db.*;
import views.html.*;
import java.sql.*;
import play.mvc.Http.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
  class Data {
            double lat;
	        double lng;
	        String market_name;
	        String type;
	        int time;
	        double weight;
	        Data [] arr;
	        int num;
	        String hour;
	        double score;
	        String DOW;
            Data(){
    	        lng=0;
    	        lat=0;
      	        time=0;
      	        market_name = "";
      	        type = "";
    	        weight = 1.0;
    	        arr = new Data[10];
    	        num = 0;
    	        score = 0.0;
    	        hour="";
    	        DOW="";
	        }
 }

public class EventsController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result create() throws Exception {
       
        String line = request().body().asText();
        Data [] data = new Data[100];
		for (int j=0; j<100; j++){
			data[j] = new Data();
		}
		SimpleDateFormat formatter = new SimpleDateFormat("kk");
		SimpleDateFormat formatter2 = new SimpleDateFormat("EEEE");
		int x = 0;
		String field = "";
		String value = "";
		Boolean readField = false;
		Boolean readValue = false;
			for(int i=0;i<line.length(); i++){
				if (readField){
					if(line.charAt(i) != '"'){
						field = field + line.charAt(i);
					}
					else{
						readField = false;
					}
				}
				if(readValue){
					if(line.charAt((i-1)) == ':' || line.charAt(i) == '"'){
					}
					else{
						if(line.charAt(i) == ',' || line.charAt(i) == '}'){
							readValue = false;
							if(field.equals("lat")){
								if((x+2) > data.length){
									Data[] a = new Data[(data.length)+100];
									for (int j=0; j<a.length; j++){
										a[j] = new Data();
									}
									for(int z=0; z<data.length; z++){
										a[z] = data[z];
									}
									data = (Data[]) a;
								}
								data[x].lat = Double.parseDouble(value);
							}
							else if (field.equals("lng")){
								data[x].lng = Double.parseDouble(value);
							}
							else if(field.equals("market_name")){
								data[x].market_name = value;
							}
							else if(field.equals("type")){
								data[x].type = value;
							}
							else if(field.equals("time")){
								data[x].time = Integer.parseInt(value);
								data[x].hour = formatter.format(new Date(data[x].time*1000L));
                                data[x].DOW = formatter2.format(new Date(data[x].time*1000L));
								x++;
							}
							field = "";
							value = "";
						}
						else{
							value = value + line.charAt(i);
						}
					}
				}
				if(line.charAt(i) == '"' && line.charAt((i+1)) != ':' && line.charAt(i-2) != ':' && line.charAt((i+1)) != ','){
					readField = true;
				}
				if(line.charAt(i) == ':'){
					readValue = true;
				}
			}
		
			Connection con;
			 con = DB.getConnection();
		
          
            Statement st = con.createStatement();
            for (int i=0; i<x; i++){
                st.executeUpdate("INSERT INTO App_Launch (lat, lng, weight, market, time, types, hour, dayOfWeek) VALUES ("+data[i].lat+","+data[i].lng+","+data[i].weight+",'"+data[i].market_name+"',"+data[i].time+",'"+data[i].type+"','"+data[i].hour+"','"+data[i].DOW+"')");
            }    
            return ok("OK " + data[0].DOW);
            // else{
            //     return ok("NOT OK");
            // }
    }

}
