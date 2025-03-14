// by Unit Grief

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.math.RoundingMode;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class App extends JPanel {

    // main parameters!
    
    String RE = "-1.86056001606243046116884561224553816816388165211730248560683399811082332303275779654547124574808599402224206962237319969943598679965909574397031890550391447692";
    String IM = "-0.000000000000000000000000000000000000000000000000000000000000000946117884967976158572027372020173885212738165928095188651999670707234112038169864232128279928551";
    int ITERATIONS = 100;
    int ZOOM = 3;

    // more parameters
    
    int CALCULATION_THRESHOLD = 12;
    BigDecimal REAL = new BigDecimal(RE);
    BigDecimal IMAGINARY = new BigDecimal(IM);
    int ACCURACY = ZOOM + 3;
    Thread[] threads = new Thread[CALCULATION_THRESHOLD];
    static Toolkit tk = Toolkit.getDefaultToolkit();
    static double width = tk.getScreenSize().getWidth()/2, height = tk.getScreenSize().getHeight()/2;
    static BufferedImage image = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_ARGB);
    int[] colors = new int[510];

    BigDecimal ZOOM1 = new BigDecimal("10").pow(ZOOM);
    ExecutorService executor = Executors.newFixedThreadPool(CALCULATION_THRESHOLD);

    void main() {

        // color scheme
        
        for (int i = 0; i < 510; i++)
            colors[i] = Color.HSBtoRGB((float)i / (float)510, 1, 1);
        
        // frame with an image
        
        JFrame frame = new JFrame();
        frame.setTitle("App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize((int)width,(int)height);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(this);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        for (int i = 0; i < threads.length; i++) {
            int[] i1 = {i};
            threads[i] = new Thread(() -> calculate(i1[0]));
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            threads[i].start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }

    BigDecimal FOUR = new BigDecimal(4);

    // method that calculates colors for each pixel of the image
    
    void calculate(int offset) {
        executor.execute(() -> {
            for (int x = (int) (offset * width / threads.length); x < (int) ((offset + 1) * width / threads.length); x++) {
                for (int y = (int) (height - 1); y > -1; y--) {
                    BigDecimal cx = ((new BigDecimal(x).subtract(new BigDecimal(width / 2.0))).divide(new BigDecimal(width / 4.0), ACCURACY, RoundingMode.HALF_UP))
                            .divide(ZOOM1, ACCURACY, RoundingMode.HALF_UP)
                            .add(REAL);
                    BigDecimal cy = ((new BigDecimal(y).subtract(new BigDecimal(height / 2.0))).divide(new BigDecimal(width / 4.0), ACCURACY, RoundingMode.HALF_UP))
                            .divide(ZOOM1, ACCURACY, RoundingMode.HALF_UP)
                            .add(IMAGINARY);
                    BigDecimal zy = new BigDecimal("0").setScale(ACCURACY, RoundingMode.HALF_UP);
                    BigDecimal zx = new BigDecimal("0").setScale(ACCURACY, RoundingMode.HALF_UP);
                    int iteration = 0;
                    BigDecimal xt;
                    while ((zx.multiply(zx)).add(zy.multiply(zy)).compareTo(FOUR) < 0 && iteration < ITERATIONS) {
                        
                        // you can change the formula to generate different fractals
                        
                        xt = zx.multiply(zy);
                        zx = (zx.multiply(zx)).subtract(zy.multiply(zy)).add(cx).setScale(ACCURACY,RoundingMode.HALF_UP);
                        zy = (xt.multiply(BigDecimal.TWO)).add(cy).setScale(ACCURACY,RoundingMode.HALF_UP);
                        iteration++;
                    }
                    if (iteration == ITERATIONS) image.setRGB(x, (int) (height - y - 1), Color.BLACK.getRGB());
                    else image.setRGB(x, (int) (height - y - 1), colors[iteration % 510]);
                    repaint();

                    // generate file with mandelbrot set image
                    
                    if (x == (int) ((offset + 1) * width / threads.length - 1) && y == 0) try {
                        ImageIO.write(image, "png", new File("test.png"));
                        System.out.println("image written");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}

