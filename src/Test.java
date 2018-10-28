
public class Test {
    public static void main(String[] args) {
        WindowInterface w = new WindowInterface("hello");
        long t0 = System.currentTimeMillis();
        long t;
        w.wOpen();
        System.out.println(System.currentTimeMillis());
        while (true){
            try {
                Thread.sleep(25);
                t=System.currentTimeMillis();
                double dt = (double) (t-t0) * 1e-3;
                t0=t;
                w.p.dt = dt;
                w.p.repaint();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
