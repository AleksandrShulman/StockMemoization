import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by aleks on 9/26/16.
 */
public class TestTrades {

  @Test
  public void testSingleTrade() {

    int[] trades = new int[]{4,5,6,19};
    Trade resultingTrade = StockApp.bestTradeInRangeDecreasing(trades, 0, trades.length - 1);
    Assert.assertTrue(! (resultingTrade instanceof NilTrade));
    Assert.assertEquals((Integer)15, resultingTrade.getProfit());
    System.out.println(resultingTrade);

    resultingTrade = StockApp.bestTradeInRangeIncreasing(trades, 0, trades.length - 1);
    Assert.assertTrue(! (resultingTrade instanceof NilTrade));
    Assert.assertEquals((Integer)15, resultingTrade.getProfit());
    System.out.println(resultingTrade);

    List<Trade> optimalTrades = StockApp.optimalTradesRecursively(trades, 0,
        trades.length - 1, 1, null).getTradeList();
    Assert.assertEquals(1, optimalTrades.size());
    Assert.assertEquals(optimalTrades.get(0), resultingTrade);
  }

  @Test
  public void testTwoTrades() {

    // With a matrix-based lookup, we've gotten time down to O(n^2) + 2.
    // This is better than a recursive lookup, which is 2*O(n^2), but not meaningfully.

    // So I think the approach is to have two lists of size n, otherwise a matrix
    // of size [numTransactions][n].

    // I think I'm not memoizing effectively. There is some piece of info that I am not making use of,
    // or some assumption that I'm not making, so I end up computing the entire matrix.

    // Or, possibly that my solution was O(n^3) before and I just didn't realize it, though
    // that doesn't seem right.


    // The key assumption I guess is that as I move the boundary, I should just do O(1) work. I also
    // record it. It's b/c my loops are not aligned. There are two loops. There is the boundary loop,
    // and then the memoization loop. I should restructure so that we do all the work as we move the
    // boundary.

    // Run through once and store the important info at each point. Perhaps moving from L->R:
    //   1. The smallest value seen so far
    //   2. The maximum profit at each point

    // Run through from R->L, and save:
    //   1. The largest value seen so far
    //   2. The maximum profit at each point

    // Now discard the first part and just keep the respective 2nd parts. This is 2 lists.
    // In O(n) time, we can find the largest value.

    // Actually, we had this before. And it was great for 2 sample_trades, making it O(n). However, it did not memoize,
    // so basically if we had 3 sample_trades, we'd need to compute 2 sample_trades for every position of the 3rd one. So the
    // operation would always be max(O(n^(sample_trades - 1)), n).

    // If we do memoize, it could actually be much faster for higher-order questions, right? Right? Or maybe not?
    // So at every location of the boundary stick O(n), you store

    int[] stockPrices = new int[]{4,9,5,18,3,7,23,45,21,19,29,1,34,28,19,28,6,19,12,4,22};
    System.out.println("There are " + stockPrices.length + " items");
    List<Trade> optimalTrades = StockApp.optimalTradesRecursively(stockPrices, 0,
        stockPrices.length - 1, 2, null).getTradeList();
    int totalProfit = 0;
    for (Trade t : optimalTrades) {
      System.out.println(t);
      totalProfit += t.getProfit();
    }

    System.out.println("Total maximal profit is: " + totalProfit);
  }

  @Test
  public void testNilTrade() {

    int[] trades = new int[]{4,5,6,19};

    Trade resultingTrade = StockApp.bestTradeInRangeDecreasing(trades, 2,2);
    Assert.assertTrue(resultingTrade instanceof NilTrade);

    resultingTrade = StockApp.bestTradeInRangeIncreasing(trades, 2, 2);
    Assert.assertTrue(resultingTrade instanceof NilTrade);
  }

  @Test
  public void testRecursiveEdgeCases() {

    int[] trades = new int[]{1,2};
    runRecursiveTestCase(trades, 1, 1);

    trades = new int[]{1};
    runRecursiveTestCase(trades, 1, 0);

    trades = new int[]{1, 0};
    runRecursiveTestCase(trades, 1, 0);
  }

  @Test
  public void testRecursiveCase() {

    int [] trades = new int[]{1, 2, 3, 4, 5, 6};
    runRecursiveTestCase(trades, 3, 5);

    trades = new int[]{1, 2, 1, 2, 1, 2, 1, 1};
    runRecursiveTestCase(trades, 3, 3);

    int lastValue = 34;
    int expectedProfit = 66;
    trades = new int[]{7, 4, 5, 25, 6, 19, 2, 5, 6, 4, 5, lastValue};
    runRecursiveTestCase(trades, 3, expectedProfit);

    lastValue++;
    expectedProfit++;
    trades = new int[]{7, 4, 5, 25, 6, 19, 2, 5, 6, 4, 5, lastValue};
    runRecursiveTestCase(trades, 3, expectedProfit);

    trades = new int[]{1, 2, 3, 4, 5, 6, 1, 1};
    runRecursiveTestCase(trades, 3, 5);
  }

  private void runRecursiveTestCase(int[] trades, int numTrades, Integer expectedTotalProfit) {
    List<Trade> optimalTrades = StockApp.optimalTradesRecursively(trades, 0,
        trades.length - 1, numTrades, null).getTradeList();
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
