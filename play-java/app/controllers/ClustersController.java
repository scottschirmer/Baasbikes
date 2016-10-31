package controllers;

import play.mvc.*;
import play.db.*;
import views.html.*;
import java.sql.*;


class Cluster {
	double lat;
	double lng;
	double radius;

	Cluster(){
		lng=0;
		lat=0;
		radius=0;
	}
}

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class ClustersController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index(String day, int hour, String market) throws Exception{
        //try{
            Connection con = DB.getConnection();
            Statement st = con.createStatement();
            String hou=String.valueOf(hour);
            ResultSet rs = st.executeQuery("SELECT * FROM App_Launch WHERE dayOfWeek = '"+day+"' AND hour = '"+hou+"' AND market = '"+market+"'");
            //ResultSet rs = st.executeQuery("SELECT * FROM Events");

            String lat = "";
            String lng = "";
            String weight = "";
            String market_name = "";
            String time = "";
            String type = "";
            String hr = "";
            String dow = "";
            Data [] data = new Data[1000];
            for (int j=0; j<1000; j++){
			    data[j] = new Data();
	    	}
            int cnt =0;
            while(rs.next()){
                data[cnt].lat=Double.parseDouble(rs.getString("lat"));
                data[cnt].lng=Double.parseDouble(rs.getString("lng"));
                data[cnt].weight=Double.parseDouble(rs.getString("weight"));
                data[cnt].market_name=rs.getString("market");
                data[cnt].weight=Integer.parseInt(rs.getString("weight"));
                data[cnt].type=rs.getString("types");
                data[cnt].hour=rs.getString("hour");
                data[cnt].DOW=rs.getString("dayOfWeek");
                cnt++;
                // lat += rs.getString("lat")+ "\n";
                // lng += rs.getString("lng")+ "\n";
                // weight += rs.getString("weight")+ "\n";
                // market_name += rs.getString("market")+ "\n";
                // time += rs.getString("time")+ "\n";
                // type += rs.getString("types")+ "\n";
                // hr += rs.getString("hour")+ "\n";
                // dow += rs.getString("dayOfWeek")+ "\n";
            }
            
            double scale = .35;
		double distance = 0;
		int count=0;
		for (int i=0; i<cnt; i++){
			count=0;
			for(int j=0; j<cnt; j++){
				double la = (data[i].lat - data[j].lat)*(data[i].lat - data[j].lat);
				double ln = (data[i].lng - data[j].lng)*(data[i].lng - data[j].lng);
				distance = Math.sqrt((la+ln));
				distance = distance*1500;
				if(distance < scale){
					if((count+2) > data[i].arr.length){
						Data[] a = new Data[(data[i].arr.length)+50];
						for (int v=0; v<a.length; v++){
							a[v] = new Data();
						}
						for(int z=0; z<data[i].arr.length; z++){
							a[z] = data[i].arr[z];
						}
						data[i].arr = (Data[]) a;
					}
					data[i].arr[count]=data[j];
					data[i].num++;
					data[i].score +=data[j].weight;
					count++;
				}
			}
		}
		if(scale>=.55)
			RemoveOutliers(data, cnt, scale);
		int n = 2;
		Data [] topN = new Data[n];
		Cluster [] clusters = new Cluster[n];
		int [] blacklist = new int[cnt];
		for (int z=0; z<cnt; z++){
			blacklist[z]=0;
		}
		for (int i=0; i<n; i++){
			int max=0;
			Data m = null;
			for (int j=0; j<cnt; j++){
				//find densest node while ignoring blacklist array
				if(blacklist[j] != 1){
					if(data[j].num > max){
						max = data[j].num;
						m = data[j];
					}
				}
				//store data point (cluster) in array of size n
				topN[i] = m;
				//populate/update blacklist
				for(int b=0; b<max; b++){
					for(int s=0; s<cnt; s++){
						if(m.arr[b].equals(data[s])){
							blacklist[s] = 1;
						}
					}
				}
			}
		}
// 		//----------------------------------------------------------------------------------------------------------------

		//form clusters
		for(int i=0; i<n; i++){
			Cluster c = new Cluster();
			c.lat=getLat(topN[i]);
			c.lng=getLng(topN[i]);
			c.radius=getRadius(topN[i]);
			clusters[i]=c;
		}
		//if tie -- pick the tighter cluster
		for (int i=0; i<n; i++){
			for (int j=0; j<n; j++){
				if(topN[i].num == topN[j].num){
					if(i<j && clusters[i].radius > clusters[j].radius){
						Cluster temp = clusters[i];
						clusters[i]=clusters[j];
						clusters[j]=temp;
					}
				}
			}
		}
		
// 		//create JSON string
		String json = "[";
		for(int i=0; i<n; i++){
			json = json + "{\"id\": " + (i+1) + ", ";
			json = json + "\"lat\": " + clusters[i].lat + ", ";
			json = json + "\"lng\": " + clusters[i].lng + ", ";
			if((i+1)==n){
				json = json + "\"radius\": " + clusters[i].radius + "}";
			}
			else
				json = json + "\"radius\": " + clusters[i].radius + "}, ";
		}
		json= json+"]";
            String cn=String.valueOf(cnt+json);
            return ok(cn);
    }
        //}
         //catch(Exception E){
        //    return ok("Bad Connection");
        //}
        
    public static void RemoveOutliers(Data [] data, int size, double scale){
		boolean good = false;
		for(int i=0; i<size; i++){
			int s=data[i].num;
			for(int j=0; j<s; j++){
				for(int k=0;k<s; k++){
					double la = (data[i].arr[j].lat - data[i].arr[k].lat)*(data[i].arr[j].lat - data[i].arr[k].lat);
					double ln = (data[i].arr[j].lng - data[i].arr[k].lng)*(data[i].arr[j].lng - data[i].arr[k].lng);
					double distance = Math.sqrt((la+ln));
					distance = distance*1500;
					if(distance<(scale/1.6) && distance != 0.0){
						good=true;
					}
				}
				if(!good){
					//remove outlier at jth position
					for(int l=j; l<data[i].num-1; l++){
						data[i].arr[l] = data[i].arr[l+1];
					}
					//data[i].arr[data[i].num] = null;
					data[i].num--;
				}
				good=false;
			}
		}
	}
	
// 	//get latitude of cluster
	public static double getLat(Data d){
		double lat = 0.0;
		double diff = 0.0;
		double min = 1000000;
		Data closest = new Data();
		Data other = new Data();
		double max = 0.0;
		for(int i=0; i<d.num; i++){
				double la = (d.lat - d.arr[i].lat)*(d.lat - d.arr[i].lat);
				double ln = (d.lng - d.arr[i].lng)*(d.lng - d.arr[i].lng);
				double distance = Math.sqrt((la+ln));
				distance = distance*1500;
				diff = .65 - distance;
				if(diff < min){
					min = diff;
					closest = d.arr[i];
				}
		}
		for(int i=0; i<d.num; i++){
			double la = (closest.lat - d.arr[i].lat)*(closest.lat - d.arr[i].lat);
			double ln = (closest.lng - d.arr[i].lng)*(closest.lng - d.arr[i].lng);
			double distance = Math.sqrt((la+ln));
			distance = distance*1500;
			if(distance > max){
				max = distance;
				other = d.arr[i];
			}
		}
		lat = (closest.lat + other.lat)/2.0;
		return lat;
	}
	
// 	//get longitude of cluster
	public static double getLng(Data d){
		double lng = 0.0;
		double diff = 0.0;
		double min = 1000000;
		Data closest = new Data();
		Data other = new Data();
		double max = 0.0;
		for(int i=0; i<d.num; i++){
				double la = (d.lat - d.arr[i].lat)*(d.lat - d.arr[i].lat);
				double ln = (d.lng - d.arr[i].lng)*(d.lng - d.arr[i].lng);
				double distance = Math.sqrt((la+ln));
				distance = distance*1500;
				diff = .65 - distance;
				if(diff < min){
					min = diff;
					closest = d.arr[i];
				}
		}
		for(int i=0; i<d.num; i++){
			double la = (closest.lat - d.arr[i].lat)*(closest.lat - d.arr[i].lat);
			double ln = (closest.lng - d.arr[i].lng)*(closest.lng - d.arr[i].lng);
			double distance = Math.sqrt((la+ln));
			distance = distance*1500;
			if(distance > max){
				max = distance;
				other = d.arr[i];
			}
		}
		lng = (closest.lng + other.lng)/2.0;
		return lng;
	}
	
// 	//get radius of cluster
	public static double getRadius(Data d){
		double rad = 0.0;
		double diff = 0.0;
		double min = 1000000;
		Data closest = new Data();
		Data other = new Data();
		double max = 0.0;
		for(int i=0; i<d.num; i++){
				double la = (d.lat - d.arr[i].lat)*(d.lat - d.arr[i].lat);
				double ln = (d.lng - d.arr[i].lng)*(d.lng - d.arr[i].lng);
				double distance = Math.sqrt((la+ln));
				distance = distance*1500;
				diff = .65 - distance;
				if(diff < min){
					min = diff;
					closest = d.arr[i];
				}
		}
		for(int i=0; i<d.num; i++){
			double la = (closest.lat - d.arr[i].lat)*(closest.lat - d.arr[i].lat);
			double ln = (closest.lng - d.arr[i].lng)*(closest.lng - d.arr[i].lng);
			double distance = Math.sqrt((la+ln));
			distance = distance*1500;
			if(distance > max){
				max = distance;
				other = d.arr[i];
			}
		}
		//Haversine formula 
		double a = Math.sin((Math.toRadians(other.lat)-Math.toRadians(closest.lat))/2.0)*Math.sin((Math.toRadians(other.lat)-Math.toRadians(closest.lat))/2.0) + Math.cos(Math.toRadians(other.lat))*Math.cos(Math.toRadians(closest.lat))*Math.sin((Math.toRadians(other.lng)-Math.toRadians(closest.lng))/2.0)*Math.sin((Math.toRadians(other.lng)-Math.toRadians(closest.lng))/2.0);
				double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt((1-a)));
				rad = c*6371;
				rad=rad*.621371;
				rad = rad/2.0;
		return rad;
	}

}

