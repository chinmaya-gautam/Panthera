package Panthera;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.Arrays;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

public class Panthera extends JFrame implements Runnable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static String userName = "user1";
	private final static String password = "oracle";
	private final static String serverName = "vmwin";
	private final static int portNumber = 1521;
	private final static String dbName = "XE";
	private static Lion[] lions = new Lion[14];
	private static Pond[] ponds = new Pond[8];
	private static Region[] regions = new Region[16];
	private static AmbulanceArea[] ambulances = new AmbulanceArea[5];
	
	private JPanel mapPanel;
	public static JCheckBox cbox;
	private boolean running = true;
	
	private static int mouse_x;
	private static int mouse_y;
	
	public static Color[] reg_fg_col = new Color[16];
	public static Color[] reg_bg_col = new Color[16];
	public static Color[] lon_fg_col = new Color[14];
	public static Color[] lon_bg_col = new Color[14];
	public static Color[] pnd_fg_col = new Color[8];
	public static Color[] pnd_bg_col = new Color[8];
	public static Color[] amb_fg_col = new Color[5];
	public static Color[] amb_bg_col = new Color[5];
	
	public static Image dbImage;
	public static Graphics dbg;
	
	public Panthera(){
		reset_colors();
		
		mapPanel = new JPanel();
		cbox = new JCheckBox();
		cbox.setText("show lions and ponds in the selected region");
		add(mapPanel, BorderLayout.CENTER);
		add(cbox,BorderLayout.SOUTH);
		mapPanel.addMouseListener(new AL());
	}
	
    private static ResultSet runQuery(Connection conn, String query) throws SQLException{
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();	
		return rs;
	}
	
    public static void reset_colors(){
    	for (int i =0;i<14;i++){
    		lon_fg_col[i] = Color.green;
    		lon_bg_col[i] = Color.white;
    	}
    	
    	for (int i=0; i<8; i++){
    		pnd_fg_col[i] = Color.black;
    		pnd_bg_col[i] = Color.blue;
    	}
    	
    	for (int i=0; i<16; i++){
    		reg_fg_col[i] = Color.black;
    		reg_bg_col[i] = Color.white;
    	}
    	
    	for (int i=0;i<5;i++){
    		amb_fg_col[i] = Color.gray;
    		amb_bg_col[i] = Color.orange;
    	}
    }
    
	private void getData(Connection conn) throws SQLException{
		String Query;
		ResultSet rs;
		// Get lion data
		Query = "select * from lion";
		rs = runQuery(conn, Query);
		int counter = 0;
		while (rs != null && rs.next()){
			String id = rs.getObject(1).toString();
			STRUCT st = (oracle.sql.STRUCT) rs.getObject(2);
			JGeometry j_geom = JGeometry.load(st);
			
				lions[counter].lionid = id;
				lions[counter].lionloc = j_geom;
			counter++;
			/*System.out.println(id);
			double[] arr = j_geom.getOrdinatesArray();
			for(int i=0;i < arr.length;i++){
				System.out.print(arr[i]);
				System.out.print(",");
			}
			System.out.println("");*/
		}
		
		//Get pond data
		Query = "select * from pond";
		rs = runQuery(conn, Query);
		counter = 0;
		while (rs != null && rs.next()){
			String id = rs.getObject(1).toString();
			STRUCT st = (oracle.sql.STRUCT) rs.getObject(2);
			JGeometry j_geom = JGeometry.load(st);
			
				ponds[counter].pondid = id;
				ponds[counter].pondloc = j_geom;
			counter++;
			/*System.out.println(id);
			double[] arr = j_geom.getOrdinatesArray();
			for(int i=0;i < arr.length;i++){
				System.out.print(arr[i]);
				System.out.print(",");
			}
			System.out.println("");*/
		}

		//Get region data
		Query = "select * from region";
		rs = runQuery(conn, Query);
		counter = 0;
		while (rs != null && rs.next()){
			String id = rs.getObject(1).toString();
			STRUCT st = (oracle.sql.STRUCT) rs.getObject(2);
			JGeometry j_geom = JGeometry.load(st);
			
				regions[counter].regionid = id;
				regions[counter].regionloc = j_geom;
			counter++;
			/*System.out.println(id);
			double[] arr = j_geom.getOrdinatesArray();
			for(int i=0;i < arr.length;i++){
				System.out.print(arr[i]);
				System.out.print(",");
			}
			System.out.println("");*/
		}
		
		//Get ambulance data
		Query = "select * from ambulancearea";
		rs = runQuery(conn, Query);
		counter = 0;
		while (rs != null && rs.next()){
			String id = rs.getObject(1).toString();
			STRUCT st = (oracle.sql.STRUCT) rs.getObject(2);
			JGeometry j_geom = JGeometry.load(st);
			
				ambulances[counter].ambulanceid = id;
				ambulances[counter].ambulanceloc = j_geom;
			counter++;
			/*System.out.println(id);
			double[] arr = j_geom.getOrdinatesArray();
			for(int i=0;i < arr.length;i++){
				System.out.print(arr[i]);
				System.out.print(",");
			}
			System.out.println("");*/
		}

	}
	
	

	private static Connection getConnectionUsingServiceName() throws SQLException, ClassNotFoundException{

        String sJDBCUrl 
            = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=" 
              + serverName + ")(PORT=" + portNumber 
              + "))(CONNECT_DATA= (SERVER = DEDICATED) (SERVICE_NAME=" + dbName + ")))";
        Class.forName("oracle.jdbc.driver.OracleDriver");
        return DriverManager.getConnection(sJDBCUrl, userName, password); 

    }
	
    public boolean executeUpdate(Connection conn, String command) throws SQLException {
	    Statement stmt = null;
	    try {
	        stmt = conn.createStatement();
	        stmt.executeUpdate(command); // This will throw a SQLException if it fails
	        return true;
	    } finally {

	    	// This will run whether we throw an exception or not
	        if (stmt != null) { stmt.close(); }
	    }
	}
	
	
	
    public void setGeom(String geomType){	
		//this.geomType = geomType;
	}

	public void drawPolygon(Graphics g, int[] x1Points, int[] y1Points, int num_points, Color fg, Color bg){
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(bg);
		g2.fillPolygon(x1Points, y1Points, num_points);
		g2.setColor(fg);
		g2.drawPolygon(x1Points, y1Points, num_points);
	}
	private void drawPoint(Graphics g, int[] x1Points, int[] y1Points, int num_points, Color fg, Color bg){
		
		Graphics2D g2 = (Graphics2D) g;
		for (int index = 0; index < num_points; index++) {
				g2.setColor(fg);
				g2.drawArc((int)x1Points[index], (int)y1Points[index], 2, 2, 0, 360);
		        
		};
	}
	
	private void drawCircle(Graphics g, int[] x1Points, int[] y1Points, int num_points, String geomType, Color fg, Color bg){
		
		// calculate the center
		int cx=0;
		int cy=0;
		int rad = 0;
		if(x1Points[0] == x1Points[1]){
			cx = (int) x1Points[0];
			cy = (int) ((y1Points[0] + y1Points[1]) /2);
			rad = (int) (y1Points[0] > y1Points[1]?cy-y1Points[1]:cy-y1Points[0]);
		}else if(x1Points[0] == x1Points[2]){
			cx = (int) x1Points[0];
			cy = (int) ((y1Points[0] + y1Points[2]) /2);
			rad = (int) (y1Points[0] > y1Points[2]?cy-y1Points[2]:cy-y1Points[0]);
		}else if(x1Points[1] == x1Points[2]){
			cx = (int) x1Points[1];
			cy = (int) ((y1Points[1] + y1Points[2]) /2);
			rad = (int) (y1Points[1] > y1Points[2]?cy-y1Points[2]:cy-y1Points[1]);
		}else if(y1Points[0] == y1Points[1]){
			cy = (int) y1Points[0];
			cx = (int) ((x1Points[0] + x1Points[1]) /2);
			rad = (int) (x1Points[0] > x1Points[1]?cx-x1Points[1]:cx-x1Points[0]);
		}else if(y1Points[0] == y1Points[2]){
			cy = (int) y1Points[0];
			cx = (int) ((x1Points[0] + x1Points[2]) /2);
			rad = (int) (x1Points[0] > x1Points[2]?cx-x1Points[2]:cx-x1Points[0]);
		}else if(y1Points[1] == y1Points[2]){
			cy = (int) y1Points[1];
			cx = (int) ((x1Points[1] + x1Points[2]) /2);
			rad = (int) (x1Points[1] > x1Points[2]?cx-x1Points[2]:cx-x1Points[1]);
		}
		
		Graphics2D g2 = (Graphics2D) g;
		for (int index = 0; index < num_points; index++) {
				g2.setColor(bg);
			  	g2.fillArc(cx, cy, rad, rad, 0, 360);
			  	g2.setColor(fg);
			  	g2.drawArc(cx, cy, rad, rad, 0, 360);
		        
		};
	}
	
	private void drawGeoms(){
		//Draw regions
		int[] xPoints = new int[100];
		int[] yPoints = new int[100];
		double [] points;
		Graphics g = mapPanel.getGraphics();
		for (int i=0; i < 16; i++){
			points = regions[i].regionloc.getOrdinatesArray();
			int ci = 0;
			int cj = 0;
			for (int j=0;j<points.length - 2; j++){
				if (j %2 == 1){
					xPoints[ci]=(int)points[j];
					ci ++;
				}else if (j%2 == 0){
					yPoints[cj]=(int)points[j];
					cj ++;
				}	
			}
			int num_points = points.length/2 - 1;
			drawPolygon(g, xPoints, yPoints, num_points, reg_fg_col[i], reg_bg_col[i]);
		}
		
		//draw lions
		for (int i=0; i < 14; i++){
			points = lions[i].lionloc.getOrdinatesArray();
			int ci = 0;
			int cj = 0;
			for (int j=0;j<points.length; j++){
				if (j %2 == 1){
					xPoints[ci]=(int)points[j];
					ci ++;
				}else if (j%2 == 0){
					yPoints[cj]=(int)points[j];
					cj ++;
				}	
			}
			int num_points=points.length/2;
			drawPoint(g, xPoints, yPoints, num_points, lon_fg_col[i], lon_bg_col[i]);
		}
		
		//draw ponds
		for (int i=0; i < 8; i++){
			points = ponds[i].pondloc.getOrdinatesArray();
			int ci = 0;
			int cj = 0;
			for (int j=0;j<points.length; j++){
				if (j %2 == 1){
					xPoints[ci]=(int)points[j];
					ci ++;
				}else if (j%2 == 0){
					yPoints[cj]=(int)points[j];
					cj ++;
				}	
			}
			int num_points=points.length/2;
			drawCircle(g, xPoints, yPoints, num_points,"pond", pnd_fg_col[i], pnd_bg_col[i]);
		}
		/*//draw ambulance
				for (int i=0; i < 5; i++){
					points = ambulances[i].ambulanceloc.getOrdinatesArray();
					int ci = 0;
					int cj = 0;
					for (int j=0;j<points.length; j++){
						if (j %2 == 1){
							xPoints[ci]=(int)points[j];
							ci ++;
						}else if (j%2 == 0){
							yPoints[cj]=(int)points[j];
							cj ++;
						}	
					}
					int num_points=points.length/2;
					drawCircle(g, xPoints, yPoints, num_points,"ambulance", amb_fg_col[i], amb_bg_col[i]);
				}*/
	}
	
	public void run() {

		// Connect to MySQL
		Connection conn = null;
		try {
			conn = getConnectionUsingServiceName();
			System.out.println("Connected to database");
		} catch (SQLException e) {
			System.out.println("ERROR: Could not connect to the database");
			e.printStackTrace();
			return;
		}catch (Exception e){
			System.out.println("class not found");
		}
		try {
			getData(conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		while(running){
			if(! cbox.isSelected()){
				reset_colors();
			}
			drawGeoms();
			repaint();
			try{
				Thread.sleep(100);
				
			}catch(InterruptedException e){
				
			}
		}
		}

	@Override
	public void paint(Graphics g) {};
	@Override
	public void repaint() {update(mapPanel.getGraphics());};
	@Override
	public void update(Graphics g) {
		dbImage = createImage(getWidth(), getHeight());
		dbg = dbImage.getGraphics();
		paint(dbg);
		dbg.drawImage(dbImage, 0, 0, this);
	};
	
	public void stop(){running = false;}
	public void destory(){running = false;}
	
	public static Region get_region_clicked(){
		String rid = null;
		Region reg = null;
		String statement = "select r.regionid from region r where sdo_relate(r.regionloc," 
				+ "sdo_geometry(2001,null,null,"
				+  "sdo_elem_info_array(1,1,1),"
				+ "sdo_ordinate_array(" + mouse_x +"," + mouse_y + ") "
				+ "), 'mask=contains') = 'TRUE'";
		try {
			Connection conn = getConnectionUsingServiceName();
			ResultSet rs = runQuery(conn, statement);
			while(rs != null && rs.next()){
				rid = rs.getObject(1).toString();
				System.out.println("region clicked: " + rid);
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i=0;i<16;i++){
			if (regions[i].regionid.equals(rid)){
				System.out.println("DEBUG: returning region!");
				return regions[i];
			}
		}
		return reg;
	}
	public static Lion[] get_lions_in_region(Region reg){
		if (reg == null){
			System.out.println("region is null!!");
			return null;
		}
		Lion[] lns = new Lion[14];
		for (int i = 0; i< 14; i++){
			lns[i] = null;
		}
		
		double[] ord_arr = reg.regionloc.getOrdinatesArray();
		String s_ord_arr = "";
		String temp_str = "";
		temp_str = Arrays.toString(ord_arr);
		s_ord_arr = temp_str.substring(1, temp_str.length()-1);
	
		String statement = "select l.lionid from lion l where sdo_relate(l.lionloc," 
				+ "sdo_geometry(2003,null,null,"
				+  "sdo_elem_info_array(1,1003,1),"
				+ "sdo_ordinate_array(" + s_ord_arr + ") "
				+ "), 'mask=inside') = 'TRUE'";
		try {
			Connection conn = getConnectionUsingServiceName();
			ResultSet rs = runQuery(conn, statement);
			int cntr = 0;
			while(rs != null && rs.next()){
				String lid = rs.getObject(1).toString();
				System.out.println("lion in region: " + lid);
				for (int i =0;i<14;i++){
					if (lions[i].lionid.equals(lid)){
						lns[cntr] = lions[i];
						cntr ++;
						System.out.println("we have a lion!!");
					}
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return lns;
	}
	
	public static Pond[] get_ponds_in_region(Region reg){
		if (reg == null){
			System.out.println("region is null!!");
			return null;
		}
		Pond[] pns = new Pond[8];
		for (int i = 0; i< 8; i++){
			pns[i] = null;
		}
		
		double[] ord_arr = reg.regionloc.getOrdinatesArray();
		String s_ord_arr = "";
		String temp_str = "";
		temp_str = Arrays.toString(ord_arr);
		s_ord_arr = temp_str.substring(1, temp_str.length()-1);
	
		String statement = "select p.pondid from pond p where sdo_relate(p.pondloc," 
				+ "sdo_geometry(2003,null,null,"
				+  "sdo_elem_info_array(1,1003,1),"
				+ "sdo_ordinate_array(" + s_ord_arr + ") "
				+ "), 'mask=inside') = 'TRUE'";
		try {
			Connection conn = getConnectionUsingServiceName();
			ResultSet rs = runQuery(conn, statement);
			int cntr = 0;
			while(rs != null && rs.next()){
				String pid = rs.getObject(1).toString();
				System.out.println("pond in region: " + pid);
				for (int i =0;i<8;i++){
					if (ponds[i].pondid.equals(pid)){
						pns[cntr] = ponds[i];
						cntr ++;
						System.out.println("we have a pond!!");
					}
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return pns;
	}
	
	public static void process_region(){
		Region reg = get_region_clicked();
		Pond[] pnds = get_ponds_in_region(reg);
		Lion[] lns = get_lions_in_region(reg);
		
		reset_colors();
		for (int i=0; i<14;i++){
			for (int j=0; j<lns.length;j++){
				if(lns[j] == null){
					continue;
				}
				if (lions[i].lionid.equals(lns[j].lionid)){
					System.out.println("chaning lion color for lion" + lions[i].lionid);
					lon_fg_col[i] = Color.red;
					lon_bg_col[i] = Color.white;
				}
			}
		}
		for (int i=0; i<8;i++){
			for (int j=0; j<pnds.length;j++){
				if(pnds[j] == null){
					continue;
				}
				if (ponds[i].pondid.equals(pnds[j].pondid)){
					System.out.println("chaning pond color for pond" + ponds[i].pondid);
					pnd_fg_col[i] = Color.red;
					pnd_bg_col[i] = Color.red;
				}
			}
		}
		
	}
	public static void main(String[] args) {
		Panthera app = new Panthera();
		for (int i=0;i<14;i++){lions[i] = new Lion();}
		for (int i=0;i<8; i++){ponds[i] = new Pond();}
		for (int i=0;i<16;i++){regions[i] = new Region();}
		for (int i=0;i<5; i++){ambulances[i] = new AmbulanceArea();}
		app.setDefaultCloseOperation(EXIT_ON_CLOSE);
		app.setSize(520,590);
		app.setVisible(true);
		app.run();
	}
	

static class AL extends MouseAdapter{
	public void mouseClicked(MouseEvent e){
		mouse_x = e.getY();
		mouse_y = e.getX();
		System.out.println("x: " + mouse_x + ",y: " + mouse_y);
		if(cbox.isSelected()){
			process_region();
		}
	}
}
}

class Lion{
	public String lionid;
	public JGeometry lionloc;
}
class Pond{
	public String pondid;
	public JGeometry pondloc;
}
class Region{
	public String regionid;
	public JGeometry regionloc;
} 
class AmbulanceArea{
	public String ambulanceid;
	public JGeometry ambulanceloc;
}

