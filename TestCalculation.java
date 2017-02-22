import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by aleks on 2/21/17.
 */
public class TestCalculation {

  final int[] sample_trades = {3, 10, 15, 35, 5, 10};

  @Test
  public void testBasicCalculation() {

    StockCalculation sc = new StockCalculation(sample_trades, 1);
    int maxProfit = sc.calculateMaxProfit();
    int expectedMaxProfit = 32;

    Assert.assertEquals(expectedMaxProfit, maxProfit);

    sc = new StockCalculation(sample_trades, 2);
    maxProfit = sc.calculateMaxProfit();
    expectedMaxProfit = 37;

    Assert.assertEquals(expectedMaxProfit, maxProfit);
  }
}
