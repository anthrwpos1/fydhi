/* реализация скользящих средних.
 *
 * -Άνθρωπος
 */
public class MAVG {
    private Fractional[] state;     //вектор внутреннего состояния
    private Fractional[] ds;        //вектор добавок
    public Fractional[] dnout;      //n-я производная фильтруемой величины. dnout[0] - "нулевая производная" - сама отфильтрованная величина
    private int order;          //порядок скользящей средней
    private int compensation;   //компенсация тренда?
    public double dt;
    public double tau;
    int step = 0;

    MAVG(int order, double dt, double tau, boolean compensation, Fractional zero) {
        this.compensation = (compensation) ? 1 : 0;
        this.order = order + this.compensation;
        this.dt = dt;
        this.tau = tau;
        state = new Fractional[this.order];
        ds = new Fractional[this.order];
        dnout = new Fractional[this.order];
        for (int i = 0; i < this.order; i++) {
            state[i] = zero.clone();
            ds[i] = zero.clone();
            state[i] = zero.clone();
        }
        if (order > 4) System.out.println("Warning: Moving Average order > 4 is not recommended.");
    }

    public void mavg(Fractional in) {
        double dem = dem(order);
        ds[0] = in.sub(state[0]).div(dem);
        for (int i = 1; i < state.length; i++) {
            ds[i] = (state[i - 1].sub(state[i])).div(dem);
        }
        for (int i = 0; i < state.length; i++) {
            state[i] = state[i].add(ds[i]);
        }
        dnout[0] = state[order - 1];
        for (int i = 1; i < order; i++) {
            dnout[i] = ds[order - i].div(dt);
        }
        for (int m = 1; m < order - 1; m++) {
            for (int i = m + 1; i < order; i++) {
                dnout[order + m - i] = (dnout[order + m - i].sub(dnout[order + m - 1 - i])).div(dem).div(dt);
            }
        }
        if (compensation == 1) {
            double tav = ((double) step - 1) * dt;
            for (int i = 0; i < order - 1; i++) {
                dnout[i] = dnout[i].add(dnout[i + 1].mul(tav));
            }
        }
    }

    private double dem(int order) {
        step += ((double) step > tau / dt) ? 0 : 1;
        return ((step - 1) / order + 1);
    }

}