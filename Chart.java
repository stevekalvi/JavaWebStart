/*
 * ShipmentInquiry.java
 *     Author  : Steve Kalvi
 *     JVM     : J2SE 5.o
 *     Purpose : View the Dollarized Shipment Trends in a Graphical Format
 */

import java.io.*;
import java.net.*;
import java.lang.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.awt.geom.Line2D.Float;

import java.awt.image.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import java.sql.DriverManager;
import java.sql.*;
//import java.sql.Driver;

public class Chart extends JFrame {

 JComboBox XSCALE, YSCALE, Quarter, Plant_Box;
 JPanel s1, s2;
 JLabel Xselect, Yselect, Enter_cust, Enter_quarter, Plant_Select;
 JTextField Customer;
 JButton Export_chart;
 BarChart bar1;
 Container cp;
 Horizontal horz = null;
 Vertical vert = null;
 getSales sales;
 int bar_chart_width = 300;
 int bar_chart_height = 200;
 int new_slider_value = 10;
 int old_slider_value = 10;
 Connection connection = null;
 PreparedStatement stmt = null;
 ResultSet rs = null;
 ArrayList arr = null;
 String[] yearlist = null;
 year_range range = null;
 private String plant_chosen = null;
 private Class cl = null;
 int month_rect_slice = 3;
 String url = "jdbc:hsqldb:/usr/local/resin/doc/skalvi/db/SALES";


 /*
  *  Construct the chart object.
  */

 public Chart() {

  sales = new getSales();

  JMenuBar menuBar = new JMenuBar();
  this.setJMenuBar(menuBar);

  JMenu menuFile = new JMenu("File");
  menuBar.add(menuFile);

  JMenu menuView = new JMenu("View");
  menuBar.add(menuView);

  JMenuItem menuFilePrint = new JMenuItem("Print");
  menuFile.add(menuFilePrint);

  JMenuItem menuFileExit = new JMenuItem("Exit");
  menuFile.add(menuFileExit);

  JMenuItem menuViewJNLP = new JMenuItem("JNLP Source");
  menuView.add(menuViewJNLP);

  JMenuItem menuViewJava = new JMenuItem("Java Source");
  menuView.add(menuViewJava);

  menuFileExitListener menuFileExitL = new menuFileExitListener();
  menuFileExit.addActionListener(menuFileExitL);

  menuFilePrintListener menuFilePrintL = new menuFilePrintListener();
  menuFilePrint.addActionListener(menuFilePrintL);


  menuWindowListener menuWindowL = new menuWindowListener();
  this.addWindowListener(menuWindowL);

  view_listener viewl = new view_listener();
  menuViewJNLP.addActionListener(viewl);
  menuViewJava.addActionListener(viewl);

  /*
   *  Set the background color for the container
   */

  cp = getContentPane(); // get the Window Pane for the Frame.
  cp.setBackground(Color.LIGHT_GRAY);


  this.setSize(800, 700);
  this.setTitle("Dollarized Shipment Inquiry Trend Analysis");
  horz = new Horizontal(); // Determine the X-Axis
  horz.setHorizontal("months");

  /*
   *  Determine the vertical spread by passing the maximum charted value
   */


  vert = new Vertical();

  vert.setVertical("dollars", sales.get_max());

  Plant_Box = new JComboBox();
  Plant_Box.addItem("123 Acme Foods");
  Plant_Box.addItem("456 General Foods");
  set_plant("123");

  sales.restateSales("123");
  repaint();


  ComboListener cbox = new ComboListener();
  Plant_Box.addActionListener(cbox);

  /*
   *  Create the Panel
   */

  s1 = new JPanel();
  s1.setBackground(Color.BLUE);

  /*
   *  Add the components to the NORTH panel
   */

  Plant_Select = new JLabel("Select Plant :");
  Plant_Select.setForeground(Color.WHITE);

  s1.add(Plant_Select);
  s1.add(Plant_Box);

  JSlider js = new JSlider(10, 180, 10);
  js.setMinimum(10);
  js.setMaximum(180);
  js.setMajorTickSpacing(20);
  js.setPaintTicks(true);

  this.add(s1, BorderLayout.NORTH);

  /*
   *  Add the BarChart to the CENTER panel
   */

  bar1 = new BarChart(50, 25, bar_chart_width, bar_chart_height, horz, vert);
  bar1.set_width(700);
  bar1.set_height(500);

  this.add(bar1, BorderLayout.CENTER);

 }

 public void set_plant(String plant_chosen) {
  this.plant_chosen = plant_chosen;
 }

 public String get_plant() {
  return this.plant_chosen;
 }

 private class Horizontal extends ArrayList {

  public void setHorizontal(String horizontal_type) {

    if (horizontal_type.equals("months")) {
     this.add("Oct");
     this.add("Nov");
     this.add("Dec");
     this.add("Jan");
     this.add("Feb");
     this.add("Mar");
     this.add("Apr");
     this.add("May");
     this.add("Jun");
     this.add("Jul");
     this.add("Aug");
     this.add("Sep");
    }

   } // setHorizontal

 } // Horizontal

 private class Vertical extends ArrayList {

  public void setVertical(String vertical_type, double amount) {
   if (vertical_type.equals("dollars")) {
    double coeffecient = 0;

    /*
     * Coeffecient based on the power of 10
     */

    for (double exp = 0; exp < 7; exp++) {
     if ((amount >= Math.pow(10, exp)) && amount <= Math.pow(10, exp + 1)) {
      coeffecient = Math.pow(10, exp);
     }
    }

    for (double i = coeffecient; i <= amount + coeffecient; i += coeffecient) {
     int c = (int) i;
     this.add("$" + c + " Mil");
    }

   }
  }


 }



 private class BarChart extends JComponent {
  int x;
  int y;
  int width;
  int height;
  ArrayList horz = null;
  ArrayList vert = null;

  /*
   * Create a Bar Chart by passing a horizontal and vertical arraylists.
   */

  public BarChart(int x, int y, int width, int height,
   ArrayList horz, ArrayList vert) {

   this.x = x;
   this.y = y;
   this.width = width;
   this.height = height;
   this.horz = horz;
   this.vert = vert;
  }

  public void set_width(int width)

  {
   this.width = width;
  }

  public void set_height(int height)

  {
   this.height = height;
  }

  public int get_width()

  {
   return this.width;
  }

  public int get_height()

  {
   return this.height;
  }

  public void paint(Graphics g) {
   Graphics2D g2 = (Graphics2D) g;
   g2.setRenderingHint(
    RenderingHints.KEY_ANTIALIASING,
    RenderingHints.VALUE_ANTIALIAS_OFF);
   Shape s = new Rectangle2D.Float(x, y, width, height); // paint the background
   g2.setPaint(Color.WHITE);
   g2.fill(s);
   g2.draw(s);
   g2.setPaint(Color.BLACK);
   Shape s1 = new Rectangle2D.Float(x, y, width, height); // draw the border
   g2.draw(s1);

   /*
    * 	X-Axis legend. Insert a blank line between the x-axis and the legend.
    */

   int column_size = width / horz.size();
   int month_horizontal_scalar = width / 12;
   int month_center_point = 0;
   int center_column = column_size / 2;
   int left_col_margin = x + center_column;
   for (int i = 0; i < horz.size(); i++) {
    g2.drawString(horz.get(i).toString(), left_col_margin, y + height + 20);
    left_col_margin = left_col_margin + column_size;

    /*
     *  Get the Data for each Historical Month represented on the X-Axis....
     */

    int glmonth = 0;
    int bucket_count = 0;
    int yx = 1;

    if (range == null) {
     range = new year_range(); // create the list of years for the chart the first time through
    }

    if ((arr != null) && (arr.size() > 0)) // ignore when the chart is initially rendered or no data
    {

     /*
      * Count the number of yearly sales buckets for that month
      */

     String month_temp2 = horz.get(i).toString().toUpperCase(); // get the month X-Axis month labels
     for (int j = 0; j < arr.size(); j++) {
      monthly_sales temp_ms = (monthly_sales) arr.get(j);
      glmonth = temp_ms.getGLmonth();
      String month_temp1 = temp_ms.getMonth();
      String plant_temp1 = temp_ms.getPlant();
      double sales_amount = java.lang.Double.parseDouble(temp_ms.getSale$());
      String plant_chosen = get_plant();
      if ((month_temp1.equals(month_temp2)) && (plant_temp1.equals(plant_chosen))) {
       month_center_point = month_horizontal_scalar * glmonth + (glmonth * 3) + x; // 50 is the offset
       bucket_count++;
      }

     }

     if (bucket_count >= 3) { // Make sure at least 3 years of historical info exists for a month...

      /*
       *  Compute the X region for a single year's information
       */

      int month_find_margin = (int)(month_center_point + month_horizontal_scalar) / 25;
      int month_left_margin = month_center_point - month_find_margin;
      int month_right_margin = month_center_point + month_find_margin;

      for (int j = 0; j < arr.size(); j++) {
       monthly_sales temp_ms = (monthly_sales) arr.get(j);
       glmonth = temp_ms.getGLmonth();
       String month_temp1 = temp_ms.getMonth();
       String plant_temp1 = temp_ms.getPlant();
       String year$ = temp_ms.getyear();
       int year = temp_ms.getGLyear();
       double sales_amount = java.lang.Double.parseDouble(temp_ms.getSale$());
       sales_amount = sales_amount / 1000000; // scale sales amount to size
       String plant_chosen = get_plant();
       if ((month_temp1.equals(month_temp2)) && (plant_temp1.equals(plant_chosen))) {
        double row_size = height / vert.size();
        double sales_to_scale = sales_amount * row_size;
        int h = (int) sales_to_scale;
        int w = month_rect_slice;
        if (year$.equals(yearlist[0])) {
         g2.setColor(Color.BLUE);
         g2.drawRect(50, 570, 10, 10);
         g2.fillRect(50, 570, 10, 10);
         g2.setColor(Color.BLACK);
         g2.drawString("    ", 75, 580);
         g2.drawString(year$, 75, 580);
         g2.setColor(Color.BLUE);
        }
        if (year$.equals(yearlist[1])) {
         g2.setColor(Color.RED);
         g2.drawRect(110, 570, 10, 10);
         g2.fillRect(110, 570, 10, 10);
         g2.setColor(Color.BLACK);
         g2.drawString("    ", 135, 580);
         g2.drawString(year$, 135, 580);
         g2.setColor(Color.RED);
        }
        if (year$.equals(yearlist[2])) {
         g2.setColor(Color.GREEN);
         g2.drawRect(170, 570, 10, 10);
         g2.fillRect(170, 570, 10, 10);
         g2.setColor(Color.BLACK);
         g2.drawString("    ", 195, 580);
         g2.drawString(year$, 195, 580);
         g2.setColor(Color.GREEN);
        }
        if (year$.equals(yearlist[3])) {
         g2.setColor(Color.ORANGE);
         g2.drawRect(230, 570, 10, 10);
         g2.fillRect(230, 570, 10, 10);
         g2.setColor(Color.BLACK);
         g2.drawString("    ", 255, 580);
         g2.drawString(year$, 255, 580);
         g2.setColor(Color.ORANGE);
        }
        if (year$.equals(yearlist[4])) {
         g2.setColor(Color.GRAY);
         g2.drawRect(290, 570, 10, 10);
         g2.fillRect(290, 570, 10, 10);
         g2.setColor(Color.BLACK);
         g2.drawString("    ", 315, 580);
         g2.drawString(year$, 315, 580);
         g2.setColor(Color.GRAY);
        }
        int diff = height - h; // height of chart minus height of colored line graph...
        g2.drawRect(month_left_margin - 30 + month_rect_slice * yx + yx, y + diff, w, h);
        g2.fillRect(month_left_margin - 30 + month_rect_slice * yx + yx, y + diff, w, h);
        yx++;
        g2.setColor(Color.BLACK);
       }
      }

     } // draw true

    }
   }


   /*
    * 	Y-Axis legend.  Determine the actual element size so it doesn't overlay the chart.
    *  The bar graph should be a percentage drawn to scale.  Should we use parseInt?  Does this
    *  axis have to represent an amount.
    */

   int row_size = height / vert.size();
   int center_row = row_size / 2;
   int bottom_row_margin = y;
   for (int i = vert.size() - 1; i >= 0; i--) {
    g2.draw(new Line2D.Float(x, bottom_row_margin, x + width, bottom_row_margin));
    g2.drawString(vert.get(i).toString(), x - 40, bottom_row_margin + 5);
    bottom_row_margin = bottom_row_margin + row_size;

   }

   g2.setPaint(Color.BLACK); // redraw the Chart border....
   g2.draw(s1);


  }


 }

 private class menuFileExitListener implements ActionListener {

  public void actionPerformed(ActionEvent event) {
   System.gc();
   System.exit(0);
  }
 }


 private class menuFilePrintListener implements ActionListener {

  public void actionPerformed(ActionEvent event) {
   Print2DPrinterJob sp = new Print2DPrinterJob();
  }
 }


 private class show_source extends JFrame {


 }



 private class view_listener implements ActionListener {

  public void actionPerformed(ActionEvent event) {

   String option = event.getActionCommand();
   show_source s = new show_source();
   s.setSize(800, 700);
   JTextArea show_text = new JTextArea(30, 80);


   BufferedReader in = null;
   if (option.equals("JNLP Source")) {
    s.setTitle("JNLP (Java Network Launching Protocol) Configuration File");
    try {
     URL jnlpfile = new URL("http://www.myjavaserver.com/~skalvi/Inquiry/shipmentinquiry.jnlp"); in = new BufferedReader(new InputStreamReader(jnlpfile.openStream()));
    } catch (Exception f) {
     System.out.println("JNLP configuration file is not found on the web....." + f.toString());
    }
   }

   if (option.equals("Java Source")) {
    s.setTitle("Java Source File");
    try {
     URL jnlpfile = new URL("http://www.myjavaserver.com/~skalvi/Inquiry/ShipmentInquiry.java"); in = new BufferedReader(new InputStreamReader(jnlpfile.openStream()));
    } catch (Exception f) {
     System.out.println("Java Source file is not found on the web....." + f.toString());
    }
   }


   try {
    String line = in .readLine();
    while (line != null) {
     System.out.println(line);
     show_text.append(line);
     show_text.append("\n");
     line = in .readLine();
    } in .close();
   } catch (Exception f2) {
    System.out.println(f2.toString());
   }

   JScrollPane scroll = new JScrollPane(show_text,
    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

   JPanel j1 = new JPanel();
   j1.add(scroll);
   j1.setBackground(Color.WHITE);
   s.add(j1, BorderLayout.CENTER);
   s.setVisible(true);
  }

 }


 private class menuWindowListener extends WindowAdapter {

  public void windowClosing(WindowEvent e) {
   System.exit(0);
  }

 }


 private class ComboListener implements ActionListener {

  public void actionPerformed(ActionEvent event) {
   JComboBox cbx = (JComboBox) event.getSource();
   String s = (String) cbx.getSelectedItem();
   String plant = s.substring(0, 2); // parse the plant from the menu selection
   set_plant(plant);
   sales.restateSales(plant);
   repaint();

  }
 }

 /*
  *  This print service class was taken from the Java Print Service API guide.
  */

 private class Print2DPrinterJob implements Printable {

  public Print2DPrinterJob() {

   /* Construct the print request specification.
    * The print data is a Printable object.
    * the request additonally specifies a job name, 2 copies, and
    * landscape orientation of the media.
    */
   PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
   aset.add(OrientationRequested.LANDSCAPE);
   aset.add(new Copies(1));
   aset.add(new JobName("Shipment Inquiry", null));

   /* Create a print job */
   PrinterJob pj = PrinterJob.getPrinterJob();
   pj.setPrintable(this);
   /* locate a print service that can handle the request */
   PrintService[] services =
    PrinterJob.lookupPrintServices();

   if (services.length > 0) {
    System.out.println("selected printer " + services[0].getName());
    try {
     pj.setPrintService(services[0]);
     pj.pageDialog(aset);
     if (pj.printDialog(aset)) {
      pj.print(aset);
     }
    } catch (PrinterException pe) {
     System.err.println(pe);
    }
   }
  }

  /*
   *  Abstract print method must be overriden
   */

  public int print(Graphics g, PageFormat pf, int pageIndex) {

   if (pageIndex == 0) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.translate(pf.getImageableX(), pf.getImageableY());
    g2d.scale((double) 0.75, (double) 0.75); // Scale the image at 75%
    cp.printAll(g2d);
    return Printable.PAGE_EXISTS;
   } else {
    return Printable.NO_SUCH_PAGE;
   }
  }

 }

 private class monthly_sales {
  String TCUST;
  String tmon;
  String tglmm;
  String tglyr;
  String ttotsl;

  public monthly_sales(String TCUST, String tmon, String tglmm, String tglyr, String ttotsl) {
   this.TCUST = TCUST;
   this.tmon = tmon;
   this.tglmm = tglmm;
   this.tglyr = tglyr;
   this.ttotsl = ttotsl;
  }

  public String getSale$() {
   return this.ttotsl;
  }

  public String getMonth() {
   return this.tmon;
  }

  public String getPlant() {
   return this.TCUST;
  }

  public int getGLmonth() {
   int glmonth = Integer.parseInt(this.tglmm);
   return glmonth;
  }

  public int getGLyear() {
   int glyear = Integer.parseInt(this.tglyr);
   return glyear;
  }

  public String getyear() {
   return tglyr;
  }

 }

 private class year_range {
  int year = 0;
  int month = 0;
  int range_size = 5;

  private year_range() {
   yearlist = new String[range_size];
   java.util.Date d = new java.util.Date();
   this.year = 1900 + d.getYear();
   month = d.getMonth();
   if (month >= 10) {
    month = month - 9;
    this.year++;
   }
   for (int i = 0; i < range_size; i++) {
    yearlist[i] = Integer.toString(this.year);
    this.year--;
   }

  }

 }

 /*
  *  Get a list of the years in the graph
  */

 private class getSales {
  double max_sales = 0.0;

  /*
   *  Constructor called when the program starts to initialize the scale of the chart
   */

  public getSales() {
   System.out.println("Got to get Sales");
   try {

    /*
     * Load the Java JDBC driver.
     */
    Class.forName("org.hsqldb.jdbcDriver");

    /*
     * Get a connection to the database.  Since we do not
     * provide a user id or password, a prompt will appear.
     */
    connection = DriverManager.getConnection(url, "sa", "");
    System.out.println("got the driver");
    /*
     *
     * Execute the query.
     */
    Statement select = connection.createStatement();
    stmt = connection.prepareStatement("SELECT * FROM SALESVOLUME");
    rs = stmt.executeQuery();
    while (rs.next()) {
     String ttotsl = rs.getString("TTOTSL");
     double sales = java.lang.Double.parseDouble(ttotsl);
     if (sales > max_sales) {
      max_sales = sales;
     }

    }

    max_sales = max_sales / 1000000.00;
   } catch (Exception e) {
    System.out.println(e.toString());
   } finally {

    /*
     * Clean up.
     */

    try {
     if (connection != null) {
      connection.close();
      stmt.close();
      rs.close();
     }
    } // try
    catch (Exception e) {} // catch
   } // finally



  }

  public double get_max() {
   return max_sales;
  }


  /*
   *  Restate the sales based on the selection critera
   */


  public void restateSales(String plant)

  {
   try {

    /*
     * Get a connection to the database.  Since we do not
     * provide a user id or password, a prompt will appear.
     */
    connection = DriverManager.getConnection(url, "sa", "");

    /*
     *
     * Execute the query.
     */

    arr = new ArrayList();
    Statement select = connection.createStatement();
    stmt = connection.prepareStatement("SELECT * FROM SALESVOLUME WHERE TCUST = ? ORDER BY TGLYR DESC");
    stmt.setString(1, plant);
    rs = stmt.executeQuery();
    while (rs.next()) {
     monthly_sales ms = new monthly_sales(rs.getString("TCUST"),
      rs.getString("TMON"),
      rs.getString("TGLMM"),
      rs.getString("TGLYR"),
      rs.getString("TTOTSL"));
     arr.add(ms);
     String ttotsl = rs.getString("TTOTSL");
     double sales = java.lang.Double.parseDouble(ttotsl);
     if (sales > max_sales) {
      max_sales = sales;
     }

    }
    max_sales = max_sales / 1000000.00; // Scale Max Sales for the drawing
   } catch (Exception e) {
    System.out.println(e.toString());
   } finally {

    /*
     * Clean up.
     */

    try {
     if (connection != null) {
      connection.close();
      stmt.close();
      rs.close();
     }
    } // try
    catch (Exception e) {} // catch
   } // finally

  }
 }

 public static void main(String args[]) {
  System.out.println("Started the program");
  try {
   //Tell the UIManager to use the platform look and feel
   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
  } catch (Exception e) {
   System.out.println(e.toString());
  }
  Chart c = new Chart();
  c.setVisible(true);
 }


}