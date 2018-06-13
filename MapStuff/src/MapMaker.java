
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.Interactor;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.*;
import org.w3c.dom.svg.SVGStyleElement;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.*;
import java.util.List;
import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

public class MapMaker {

    private final static String MAP_FLAT = "BlankMap-World6-Equirectangular.svg";
    private static String web = "http://google.com";
    private final static String MAP_ROUND = "World Map.svg";
    public  static Color[] colors = createGradient(Color.BLUE, Color.RED, 90);
    Document doc;
    HashMap<String,HashMap<String,Integer[]>> map = new HashMap<String, HashMap<String, Integer[]>>();
    HashMap<String,Integer[]> hmap = new HashMap<String, Integer[]>();
    Integer[] maxValues;
    JSVGCanvas can;
    Integer[] integral = {0,0,0,0,0,0,0,0,0,0};
    String dateUpdate = "";
    CheckboxGroup cbg;
    Integer selected = 0;
    JComboBox petList= new JComboBox();;
    public boolean go = true;
    int comeon = 0;
    JFrame frame;
    static  double zoom;
    private int lastOffsetX;
    private int lastOffsetY;
    private double translateX;
    private double translateY;
    private Point textPt = new Point(900/2,900/2);
    private Point mousePt;
    JTextArea area = new JTextArea();
    boolean first = true;



    public static void main(String... args) {

        new MapMaker().getInfoSetDoc();
    }
    public void updateSVG(String str,Integer intey){

        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        area.setText("");
        Integer total = 0;

        try {

            doc = f.createDocument(MAP_ROUND,
                    MapMaker.class.getClassLoader().getResourceAsStream(MAP_ROUND));

        } catch (IOException e) {
            e.printStackTrace();
        }
        NodeList stylesList = doc.getElementsByTagName("style");
        Node styleNode = stylesList.item(0);
        for (Map.Entry<String,HashMap<String,Integer[]>> entry : map.entrySet()) {
            String key = entry.getKey();
            HashMap<String, Integer[]> value = entry.getValue();
            if(key.equals(str)){
                for(Map.Entry<String,Integer[]> ent : value.entrySet()){

                    String s = ent.getKey();
                    Integer[] in1 = ent.getValue();
                    Integer in = in1[intey];//to work with taking it slow going great ready for the next step
                    Integer ha = integral[intey];
                    colors = createGradient(Color.BLUE, Color.RED, ha);
                    if(in != 0){
                        total += in;
                        int r =colors[in].getRed();
                        int g = colors[in].getGreen();
                        int b = colors[in].getBlue();
                        String hex = String.format("#%02x%02x%02x", r, g, b);
                        String how = "."+s+" {fill: "+hex+";}".replaceAll(""+(char)0,"");
                        byte[] array1 = new String(how.getBytes()).replaceAll("\0", "").getBytes();
                        how = new String(array1);
                        System.out.println(how.substring(1,3));
                        Locale l = new Locale("", how.substring(1,3));

                        area.append("country: " + l.getDisplayCountry() + "     " + in + "\n");
                        SVGStyleElement sty = (SVGStyleElement) doc.createElementNS(SVG_NAMESPACE_URI, "style");
                        sty.setAttributeNS(null, "type", "text/css");
                        sty.appendChild(doc.createCDATASection(how));
                        styleNode.getParentNode().appendChild(sty);
                    }

                }
                can.setDocument(doc);
                area.append("total: " + total);

            }

        }
    }
    public void getData() {
        try {
            final File file = new File(System.getProperty("user.home") + File.separator + "mapsplaygsutil.txt");
            String gutTF = "not configured";
            String gspub = "not configured";
            String folder = "not configured";
            String year = "2018";
            String month = "03";
            String packagename = "not configured";
            if (file.exists()) {

                    BufferedReader read = new BufferedReader(new FileReader(file));
                    String line = "";
                    while ((line = read.readLine()) != null) {
                        if (line.startsWith("gsutil")) {
                            String[] split = line.split(",");
                            gutTF = split[1];
                        }
                        if (line.startsWith("gspath")) {
                            String[] split = line.split(",");
                            gspub = split[1];
                        }
                        if (line.startsWith("year")) {
                            String[] split = line.split(",");
                            year = split[1];
                        }
                        if (line.startsWith("month")) {
                            String[] split = line.split(",");
                            month = split[1];
                        }
                        if (line.startsWith("folder")) {
                            String[] split = line.split(",");
                            folder = split[1];
                        }
                        if (line.startsWith("package")) {
                            String[] split = line.split(",");
                            packagename = split[1];
                        }
                    }
                    read.close();


                    String[] commands = new String[]{gutTF, "cp", "-r",
                            gspub + "/stats/installs/installs_" + packagename +"_"+ year + "" + month + "_country.csv",
                            folder};


                    Process child = Runtime.getRuntime().exec(commands);
                    try {
                        Thread.sleep(5000);
                    }catch (InterruptedException e){

                    }
                   // System.out.println("exit: " +child.exitValue());


                    File file1 = new File(folder + File.separator + "installs_" + packagename+"_"+year+month+"_country.csv");
                    BufferedReader reader = new BufferedReader(new FileReader(file1));
                    ArrayList<String> countrys = new ArrayList<String>();
                    String line1 = "";
                    int count1 = 0;
                    String date = "";
                    //Daily Device Installs,Daily Device Uninstalls,Daily Device Upgrades,Total User Installs,Daily User Installs,Daily User Uninstalls,Active Device Installs
                    //Daily Device Installs,Daily Device Uninstalls,Daily Device Upgrades,Total User Installs,Daily User Installs,Daily User Uninstalls,Active Device Installs,Install events,Update events,Uninstall events
                boolean first = true;
                comeon = 0;
                    while ((line1 = reader.readLine()) != null) {
                        if (line1.trim().equals("")) {
                            map.put(date, hmap);
                            hmap = new HashMap<String, Integer[]>();
                            for (int i = 0; i < integral.length; i++) {
                                integral[i]++;
                            }
                            break;
                        }

                        if (count1 == 0) {
                            count1++;
                        } else {
                            String[] columns = line1.split(",");
                            if(first){
                                map = new HashMap<String, HashMap<String, Integer[]>>();
                                hmap = new HashMap<String, Integer[]>();
                                comeon = columns.length - 3;

                                integral = new Integer[comeon];
                                for(int u = 0;u < integral.length;u++){
                                    integral[u] = 0;
                                }
                                first = false;
                            }
                            if (columns[0].startsWith("sep")) {

                            } else if (columns[0].startsWith("Date")) {

                            } else {
                                if (!date.equals(columns[0]) && !date.equals("")) {
                                    map.put(date, hmap);
                                    hmap = new HashMap<String, Integer[]>();
                                }
                                date = columns[0];
                                Integer[] how = new Integer[comeon];
                                for (int z = 3, x = 0; z < columns.length; z++, x++) {
                                    String how1 = columns[z].trim();
                                    byte[] array1 = new String(how1.getBytes()).replaceAll("\0", "").getBytes();
                                    how1 = new String(array1);
                                    how[x] = Integer.parseInt(how1);
                                    if (how[x] > integral[x]) {
                                        integral[x] = how[x];
                                    }
                                }
                                //String how = columns[3].trim();
                                //byte[] array1 = new String(how.getBytes()).replaceAll("\0", "").getBytes();
                                // how = new String(array1);
                                if (!columns[2].trim().equals("")) {
                                    //Integer now = Integer.parseInt(how);
                                    // hmap.put(columns[2].toLowerCase(),now);
                                    hmap.put(columns[2].toLowerCase(), how);
                                    //if(now > integral)
                                    // integral = now;
                                }

                            }
                        }
                    }
                    Object[] dates = map.keySet().toArray();
                    Arrays.sort(dates);
                    dateUpdate = (String) dates[0];
                    petList = new JComboBox(dates);
                    petList.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            JComboBox cb = (JComboBox) e.getSource();
                            dateUpdate = (String) cb.getSelectedItem();
                            updateSVG(dateUpdate, selected);
                        }
                    });
                }



        } catch (IOException e) {
            e.printStackTrace();
            go = false;
        }
    }
    @SuppressWarnings("unchecked")
    public void getInfoSetDoc(){
        try {
getData();
            System.out.println("int: " +integral);

            System.out.println("length: " + colors.length);
            // make a Document with the base map
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            doc = f.createDocument(MAP_ROUND,
                    MapMaker.class.getClassLoader().getResourceAsStream(MAP_ROUND));








            Interactor act = new Interactor() {
                public boolean startInteraction(InputEvent inputEvent) {
                    System.out.println("deal: "+inputEvent.paramString());
                    return true;
                }

                public boolean endInteraction() {
                    return true;
                }

                public void keyTyped(KeyEvent e) {
                    System.out.println(e.paramString());
                }

                public void keyPressed(KeyEvent e) {
                    System.out.println(e.paramString());
                }

                public void keyReleased(KeyEvent e) {
                    System.out.println(e.paramString());
                }

                public void mouseClicked(MouseEvent e) {
                        System.out.println(e.paramString());
                    lastOffsetX = e.getX();
                    lastOffsetY = e.getY();


                }

                public void mousePressed(MouseEvent e) {
                    System.out.println(e.paramString());

                    mousePt = e.getPoint();
                }

                public void mouseReleased(MouseEvent e) {
                    System.out.println(e.paramString());

                }

                public void mouseEntered(MouseEvent e) {
                    System.out.println(e.paramString());
                }

                public void mouseExited(MouseEvent e) {
                    System.out.println(e.paramString());
                }

                public void mouseDragged(MouseEvent e) {
                    System.out.println(e.paramString());
                    // new x and y are defined by current mouse location subtracted
                    // by previously processed mouse location
                    int newX = e.getX()-lastOffsetX;
                    int newY =  e.getY()- lastOffsetY;

                    // increment last offset to last processed by drag event.
                    lastOffsetX = e.getX();
                    lastOffsetY = e.getY();

                    // update the canvas locations
                    translateX += newX;
                    translateY += newY;
                    AffineTransform at = new AffineTransform();
                    int dx = e.getX() - mousePt.x;
                    int dy = e.getY() - mousePt.y;
                    textPt.setLocation(textPt.x + dx, textPt.y + dy);
                    mousePt = e.getPoint();

                    //at.translate(0,0);
                    at.translate(
                            //e.getX() -can.getWidth()*(1-zoom)/2,
                            //e.getY() -can.getHeight()*(1-zoom)/2
                            //translateX-can.getWidth()/2,translateY-can.getHeight()/2
                            //translateX,translateY
                            textPt.x,textPt.y

                    );
                    at.scale(zoom,zoom);
                    can.setRenderingTransform(at);


                }

                public void mouseMoved(MouseEvent e) {
                    System.out.println(e.paramString());
                }
            };
            frame = new JFrame();
            can = new JSVGCanvas();
            can.setPreferredSize(new Dimension(900,900));
            JScrollPane checkScroll = new JScrollPane();

//Create the combo box, select item at index 4.
//Indices start at 0, so 4 specifies the pig.

                if(!go){
                 petList = new JComboBox();
                 petList.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                           JComboBox cb = (JComboBox)e.getSource();
                            dateUpdate = (String)cb.getSelectedItem();
                            updateSVG(dateUpdate,selected);
                       }
                 });
                }

            cbg = new CheckboxGroup();
//,,,,
            Checkbox check = new Checkbox("Daily Device Installs",cbg,true);
            check.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    selected = 0;
                    updateSVG(dateUpdate,selected);

                }
            });
           // checkScroll.add(check);
            Checkbox check1 = new Checkbox("Daily Device Uninstalls",cbg,false);
            check1.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    selected = 1;
                    updateSVG(dateUpdate,selected);
                }
            });
            //checkScroll.add(check1);
            Checkbox check2 = new Checkbox("Daily Device Upgrades",cbg,false);
            check2.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    selected = 2;
                    updateSVG(dateUpdate,selected);
                }
            });
           // checkScroll.add(check2);
            Checkbox check3 = new Checkbox("Total User Installs",cbg,false);
            check3.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    selected = 3;
                    updateSVG(dateUpdate,selected);
                }
            });
           // checkScroll.add(check3);
            Checkbox check4 = new Checkbox("Daily User Installs",cbg,false);
            check4.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    selected = 4;
                    updateSVG(dateUpdate,selected);
                }
            });
           // checkScroll.add(check4);
            Checkbox check5 = new Checkbox("Daily User Uninstalls",cbg,false);
            check5.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    selected = 5;
                    updateSVG(dateUpdate,selected);
                }
            });
            //checkScroll.add(check5);
            Checkbox check6 = new Checkbox("Active Device Installs",cbg,false);
            check6.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    selected = 6;
                    updateSVG(dateUpdate,selected);
                }
            });
            //checkScroll.add(check6);
            Checkbox check7 = new Checkbox("Install events",cbg,false);

            Checkbox check8 = new Checkbox("Update events",cbg,false);

            Checkbox check9 = new Checkbox("Uninstall events",cbg,false);

            if(comeon != 7){
                check7.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        selected = 7;
                        updateSVG(dateUpdate,selected);
                    }
                });
               // checkScroll.add(check7);
                check8.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        selected = 8;
                        updateSVG(dateUpdate,selected);
                    }
                });
               // checkScroll.add(check8);
                check9.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        selected = 9;
                        updateSVG(dateUpdate,selected);
                    }
                });
                //checkScroll.add(check9);
            }

            can.setDocument(doc);
            System.out.println("width: "+can.getWidth());

            //can.getEnableImageZoomInteractor();
            //can.setSVGDocument(sdoc);
            //final boolean add = can.getInteractors().add(act);
            List list = can.getInteractors();

            list.add(act);
            JPanel pan = new JPanel();
            pan.setLayout(new BoxLayout(pan, BoxLayout.X_AXIS));
            pan.add(Box.createHorizontalGlue());
            //checkScroll.createHorizontalScrollBar();
           // pan.add(checkScroll);
            pan.add(petList);
            pan.add(check);
            pan.add(check1);
            pan.add(check2);
            pan.add(check3);
            pan.add(check4);
           pan.add(check5);
            pan.add(check6);
            if(comeon != 7) {
                pan.add(check7);
                pan.add(check8);
                pan.add(check9);
            }
            JMenuBar menuBar;
            JMenu menu;
            JMenuItem menuItem;
            menuBar = new JMenuBar();

//Build the first menu.
            menu = new JMenu("Settings");
            menu.setMnemonic(KeyEvent.VK_A);

            menuBar.add(menu);

//a group of JMenuItems
            menuItem = new JMenuItem("Settings",
                    KeyEvent.VK_T);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_1, ActionEvent.ALT_MASK));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    final File file = new File(System.getProperty("user.home") + File.separator + "mapsplaygsutil.txt");
                    String gutTF = "not configured";
                    String gspub = "not configured";
                    String folder = "not configured";
                    String year = "2018";
                    String month = "03";
                    String packagename = "not configured";
                    if(file.exists()){
                        try {
                            BufferedReader read = new BufferedReader(new FileReader(file));
                            String line = "";
                            while((line = read.readLine()) != null){
                                if(line.startsWith("gsutil")){
                                    String[] split = line.split(",");
                                    gutTF = split[1];
                                }
                                if(line.startsWith("gspath")){
                                    String[] split = line.split(",");
                                    gspub = split[1];
                                }
                                if(line.startsWith("year")){
                                    String[] split = line.split(",");
                                    year = split[1];
                                }
                                if(line.startsWith("month")){
                                    String[] split = line.split(",");
                                    month = split[1];
                                }
                                if(line.startsWith("folder")){
                                    String[] split = line.split(",");
                                    folder = split[1];
                                }
                                if(line.startsWith("package")){
                                    String[] split = line.split(",");
                                    packagename = split[1];
                                }
                            }
                            read.close();
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }else{

                    }

                    final JFrame frames = new JFrame("Settings");
                    frames.setSize(500,500);
                    JLabel gutilPath = new JLabel("gsutil Path gsutil.cmd: ");
                    final JTextField gutilPathT = new JTextField(gutTF,25);
                    JButton gutil = new JButton("Set");
                    JLabel gspubpath = new JLabel("gs://pubsite path example: gs://pubsite_prod_rev_16868979801885868339  ");
                    JLabel gyear = new JLabel("year ex:2018");
                    JLabel gmonth = new JLabel("month ex: 01,02,10...");
                    final JTextField tyear = new JTextField(year,25);
                    final JTextField tmonth = new JTextField(month,25);
                    final JTextField gspubpathT = new JTextField(gspub,25);
                    JLabel lfolder = new JLabel("set folder to save csv's");
                    JButton bfolder = new JButton("Set");
                    JLabel lpackage = new JLabel("apps package name");
                    final JTextField tpackage = new JTextField(packagename,25);

                    final JTextField gfolder = new JTextField(folder,25);

                    JButton save = new JButton("SAVE");
                    save.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            try {
                                FileWriter write = new FileWriter(file);
                                write.write("gsutil" + "," + gutilPathT.getText().trim() + "\n");
                                write.write("gspath" + "," + gspubpathT.getText().trim() + "\n");
                                write.write("year" + "," + tyear.getText().trim() + "\n");
                                write.write("month" + "," + tmonth.getText().trim() + "\n");
                                write.write("folder" + "," + gfolder.getText().trim() + "\n");
                                write.write("package" + "," + tpackage.getText().trim());
                                write.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                            getInfoSetDoc();
                        }
                    });
                    gutil.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            JFileChooser chooser = new JFileChooser();
                            int returnVal = chooser.showOpenDialog(frames);
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                String file = chooser.getSelectedFile().getAbsolutePath();
                                gutilPathT.setText(file);



                            }

                            }
                    });
                    bfolder.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            JFileChooser chooser = new JFileChooser();
                            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            int returnVal = chooser.showOpenDialog(frames);
                            if (returnVal == JFileChooser.APPROVE_OPTION) {
                                String file = chooser.getSelectedFile().getAbsolutePath();
                                gfolder.setText(file);



                            }
                        }
                    });
                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));

                    panel.add(gutilPath);

                    panel.add(gutilPathT);

                    panel.add(gutil);

                    panel.add(gspubpath);

                    panel.add(gspubpathT);
                    panel.add(gyear);
                    panel.add(tyear);
                    panel.add(gmonth);
                    panel.add(tmonth);
                    panel.add(lfolder);
                    panel.add(gfolder);
                    panel.add(bfolder);
                    panel.add(lpackage);
                    panel.add(tpackage);

                    panel.add(save);
                    frames.add(panel);
                    frames.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    frames.setVisible(true);
                }
            });
            menu.add(menuItem);

            final JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            //panel.add(petList);
            //panel.add(pan);

            panel.add(can);
            area = new JTextArea();
            JScrollPane areaScroll = new JScrollPane(area);
            areaScroll.setPreferredSize(new Dimension(900,300));
            panel.add(areaScroll);
            zoom = 1;

            can.addMouseWheelListener(new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e) {
                    System.out.println(e.paramString());
                   int haha = e.getWheelRotation();
                           System.out.println("wheel: "+ haha);
                           if(haha < 0){
                                zoom-=.021;
                           }else{
                               zoom+=.021;
                           }
                    AffineTransform at = new AffineTransform();

                    //at.translate(0,0);
                    at.translate(
                           //can.getWidth()*(1-zoom)/2,
                           //can.getHeight()*(1-zoom)/2
                           // translateX,translateY
                            //translateX-can.getWidth()/2,translateY-can.getHeight()/2
                            textPt.x,textPt.y

                    );
                    at.scale(zoom,zoom);

                    can.setRenderingTransform(at);
                }
            });

            Container contentPane = frame.getContentPane();
            contentPane.add(panel, BorderLayout.CENTER);
            checkScroll.add(pan);
            JScrollPane jScrollPane = new JScrollPane(pan);
            jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            jScrollPane.setPreferredSize(new Dimension (200, 50));

            contentPane.add(jScrollPane, BorderLayout.PAGE_START);
            //frame.add(pan);
           // frame.add(panel);
            frame.setJMenuBar(menuBar);
            frame.setSize(500,500);
            frame.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent componentEvent) {
                    // do stuff
                    textPt = new Point(can.getWidth()/2,can.getHeight()/2);
                }
            });
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();

            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            int x = (int) rect.getMaxX() - frame.getWidth();
            int y = 0;
            frame.setLocation(x, y);

            frame.setVisible(true);
            updateSVG(dateUpdate,selected);

           // frame.setSize(500,500);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    static String trimZeros(String str) {
        int pos = str.indexOf(0);
        return pos == -1 ? str : str.substring(0, pos);
    }
    public static Color[] createGradient(final Color one, final Color two, final int numSteps)

    {

        int r1 = one.getRed();

        int g1 = one.getGreen();

        int b1 = one.getBlue();

        int a1 = one.getAlpha();



        int r2 = two.getRed();

        int g2 = two.getGreen();

        int b2 = two.getBlue();

        int a2 = two.getAlpha();



        int newR = 0;

        int newG = 0;

        int newB = 0;

        int newA = 0;



        Color[] gradient = new Color[numSteps];

        double iNorm;

        for (int i = 0; i < numSteps; i++)

        {

            iNorm = i / (double)numSteps; //a normalized [0:1] variable

            newR = (int) (r1 + iNorm * (r2 - r1));

            newG = (int) (g1 + iNorm * (g2 - g1));

            newB = (int) (b1 + iNorm * (b2 - b1));

            newA = (int) (a1 + iNorm * (a2 - a1));

            gradient[i] = new Color(newR, newG, newB, newA);

        }



        return gradient;

    }

}
