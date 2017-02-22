import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by aleks on 9/26/16.
 */
public class TestTrades {

  @Test
  public void testTwoTrades() {

    int[] stockPrices = new int[]{4,9,5,18,3,7,23,45,21,19,29,1,34,28,19,28,6,19,12,4,22};
    System.out.println("There are " + stockPrices.length + " items");
    StockCalculation sc = new StockCalculation(stockPrices, 2);
    int totalProfit = sc.calculateMaxProfit();

    int expectedTotalProfit = 33 + 42;
    Assert.assertEquals(expectedTotalProfit, totalProfit);
  }

  @Test
  public void testNilTrade() {

    int[] trades = new int[]{4,5,6,19};
    StockCalculation sc = new StockCalculation(trades, 2);

    Trade resultingTrade = sc.findOptimalTrades(2,2,2).get(0);
    Assert.assertTrue(resultingTrade instanceof NilTrade);
  }

  @Test
  public void testRecursiveEdgeCases() {

    int[] trades = new int[]{1,2};
    runTestCase(trades, 1, 1);

    trades = new int[]{1};
    runTestCase(trades, 1, 0);

    trades = new int[]{1, 0};
    runTestCase(trades, 1, 0);
  }

  @Test
  public void testRecursiveCase() {

    int [] trades = new int[]{1, 2, 3, 4, 5, 6};
    runTestCase(trades, 3, 5);

    trades = new int[]{1, 2, 1, 2, 1, 2, 1, 1};
    runTestCase(trades, 3, 3);

    int lastValue = 34;
    int expectedProfit = 66;
    trades = new int[]{7, 4, 5, 25, 6, 19, 2, 5, 6, 4, 5, lastValue};
    runTestCase(trades, 3, expectedProfit);

    lastValue++;
    expectedProfit++;
    trades = new int[]{7, 4, 5, 25, 6, 19, 2, 5, 6, 4, 5, lastValue};
    runTestCase(trades, 3, expectedProfit);

    trades = new int[]{1, 2, 3, 4, 5, 6, 1, 1};
    runTestCase(trades, 3, 5);
  }

  private void runTestCase(int[] closingPrices, int numTrades, Integer expectedTotalProfit) {
    StockCalculation sc = new StockCalculation(closingPrices, numTrades);
    List<Trade> optimalTrades = sc.findOptimalTrades().getTradeList();
    int totalProfit = 0;
    for (Trade t : optimalTrades) {
      if (! (t instanceof NilTrade)) {
        System.out.println(t);
        totalProfit += t.getProfit();
      }
    }
    System.out.println("Total Profit: " + totalProfit);

    if (expectedTotalProfit != null) {
      Assert.assertEquals((int)expectedTotalProfit, totalProfit);
    }
  }
}
