/**
 * A class where we have an infinitesimally small trade
 */
public class NilTrade extends Trade {

  @Override
  public Integer getProfit() {
    return 0;
  }
}
