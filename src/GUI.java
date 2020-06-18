

import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

public class GUI {
    private Frame frame;
    private Panel panel;
    private Canvas canvas;
    private Graphics2D g;


    public GUI(String name, Dimension screenSize){
        frame = new Frame(name);
        frame.setSize(screenSize);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        frame.setLayout(new GridLayout(1, 1));

        panel = new Panel();
        panel.setLayout(new FlowLayout());
        canvas = new Canvas();
        panel.add(canvas);
        frame.add(panel);
        frame.setVisible(true);
        g = (Graphics2D) canvas.getGraphics();
        g.translate(frame.getWidth() / 2, frame.getHeight() / 2);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.RED);
    }

    public void drawRouter(ArrayList<LocalSearch.Point2D> points, ArrayList<ArrayList<Integer>> routers){
        canvas.repaint();
        double max = Integer.MAX_VALUE;
        for(LocalSearch.Point2D p : points){
            max = Math.max(max, Math.max(Math.abs(p.x), Math.abs(p.y)));
        }
        float scale = (float) (frame.getWidth() / 2 / max * 0.8);
        g.setColor(Color.BLUE);
        g.drawString("Police Office", scale * (float) points.get(0).x, -scale * (float) points.get(0).y - 8);
        g.fill(new Ellipse2D.Double(scale * (float) points.get(0).x - 5, -scale * (float) points.get(0).y - 5, 10, 10));
        g.setColor(Color.RED);
        for (int i = 1; i < points.size(); i++) {
            g.drawString(String.valueOf(i), scale * (float) points.get(i).x - 3, -scale * (float) points.get(i).y - 4);
            g.fill(new Ellipse2D.Double(scale * (float) points.get(i).x - 3, -scale * (float) points.get(i).y - 3, 6, 6));
        }

        Color[] color = {Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK};
        int c = 0;

        for (ArrayList router : routers) {
            g.setColor(color[(c++) % color.length]);
            for (int i = 0; i < router.size() - 1; i++) {
                LocalSearch.Point2D p = points.get((Integer) router.get(i));
                LocalSearch.Point2D q = points.get((Integer) router.get(i + 1));
                g.draw(new Line2D.Float(scale * (float) p.x, -scale * (float) p.y, scale * (float) q.x, -scale * (float) q.y));
            }
        }

        g.setColor(Color.BLUE);
        g.drawString("Police Office", scale * (float) points.get(0).x, -scale * (float) points.get(0).y - 8);

    }

    public static void main(String[] args) throws InterruptedException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        GUI gui = new GUI("Mini-Project Local Search", new Dimension(500, 500));
        Dataset data = new Dataset("./dataset/data_5_4_1");
        gui.drawRouter(data.points, null);
    }
}

