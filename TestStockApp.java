import org.junit.Assert;
import org.junit.Test;

/**
 * Created by aleks on 9/20/16.
 */
public class TestStockApp {

  final int[] sample_trades = {5, 10, 15, 35, 5, 10};

  @Test
  public void testFindBestTrade() {

    final Integer EXPECTED_LOWEST_PRICE_DAY = 0;
    final Integer EXPECTED_LOWEST_PRICE = 5;
    final Integer EXPECTED_HIGHEST_PRICE_DAY = 3;
    final Integer EXPECTED_HIGHEST_PRICE = 35;
    final Integer EXPECTED_PROFIT = EXPECTED_HIGHEST_PRICE - EXPECTED_LOWEST_PRICE;

    Trade result = StockApp.bestTradeInRangeIncreasingMemoized(sample_trades, 0, sample_trades.length - 1);

    Assert.assertEquals(EXPECTED_PROFIT, result.getProfit());
    Assert.assertEquals(EXPECTED_LOWEST_PRICE_DAY, result.getBuyDay());
    Assert.assertEquals(EXPECTED_HIGHEST_PRICE_DAY, result.getSellDay());
  }

  @Test
  public void validateRangeDecreasingAndRangeIncreasingEqual() {

    Trade result = StockApp.bestTradeInRangeIncreasingMemoized(sample_trades, 0, sample_trades.length - 1);
    Trade result2 = StockApp.bestTradeInRangeDecreasingMemoized(sample_trades, 0, sample_trades.length - 1);

    Assert.assertEquals("Increasing and decreasing trades different", result, result2);
  }

  @Test
  public void testFindBestTrade2() {

    int[] trades = {10, 30, 15, 35, 5, 75};
    final Integer EXPECTED_BUY_PRICE_DAY = 4;
    final Integer EXPECTED_BUY_PRICE = 5;
    final Integer EXPECTED_SELL_PRICE_DAY = 5;
    final Integer EXPECTED_SELL_PRICE = 75;
    final Integer EXPECTED_PROFIT = EXPECTED_SELL_PRICE - EXPECTED_BUY_PRICE;

    Trade result = StockApp.bestTradeInRangeIncreasingMemoized(trades, 0, trades.length - 1);

    Assert.assertEquals(EXPECTED_PROFIT, result.getProfit());
    Assert.assertEquals(EXPECTED_BUY_PRICE_DAY, result.getBuyDay());
    Assert.assertEquals(EXPECTED_SELL_PRICE_DAY, result.getSellDay());
  }

  @Test
  public void testEmpty() {

    int EXPECTED_RESULT = 0;
    Assert.assertEquals(EXPECTED_RESULT, StockApp.maxProfit(new int[]{},0,0,2));
  }

  @Test
  public void testMaxProfitInRangeIncreasing() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    Assert.assertEquals(30, StockApp.maxProfit(trades, 1));

    trades = new int[]{5, 10};
    Assert.assertEquals(5, StockApp.maxProfit(trades, 1));

    trades = new int[]{5};
    Assert.assertEquals(0, StockApp.maxProfit(trades, 1));

    trades = new int[]{};
    Assert.assertEquals(0, StockApp.maxProfit(trades, 1));
  }

  @Test
  public void testMaxProfitInRangeDecreasing() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    Assert.assertEquals(30, StockApp.maxProfitInRangeDecreasing(trades, 0, trades.length - 1));

    trades = new int[]{5, 10};
    Assert.assertEquals(5, StockApp.maxProfitInRangeDecreasing(trades, 0, trades.length - 1));

    trades = new int[]{5};
    Assert.assertEquals(0, StockApp.maxProfitInRangeDecreasing(trades, 0, trades.length - 1));
  }

  @Test
  public void testEmptyTradesListDecreasing() {

    int[] trades = new int[]{};
    Assert.assertEquals(0, StockApp.maxProfitInRangeDecreasing(trades, 0, Math.max(trades.length - 1, 0)));
  }

  @Test
  public void testMaxProfitInRangeEquivalent() {

    int[] smallTrades = {35, 5, 10, 25};
    int smallIncreasingProfit = StockApp.maxProfitInRangeIncreasing(smallTrades, 0, smallTrades.length - 1);
    int smallDecreasingProfit = StockApp.maxProfitInRangeDecreasing(smallTrades, 0, smallTrades.length - 1);

    Assert.assertEquals(smallIncreasingProfit, smallDecreasingProfit);

    int[] trades = {5, 10, 15, 35, 5, 10, 25, 40, 10, 25, 5};
    int increasingProfit = StockApp.maxProfitInRangeIncreasing(trades, 3, 6);
    int decreasingProfit = StockApp.maxProfitInRangeDecreasing(trades, 3, 6);
    Assert.assertEquals(increasingProfit, decreasingProfit);
  }

  @Test
  public void testProfitSingleTrade() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    Assert.assertEquals(30, StockApp.maxProfit(trades, 1));
  }

  @Test
  public void testProfitTwoTrades() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    Assert.assertEquals(35, StockApp.maxProfit(trades, 0, trades.length - 1, 2));

    trades = new int[]{5, 10, 15, 35, 5, 10, 45, 25, 15, 20};
    Assert.assertEquals(70, StockApp.maxProfit(trades, 0, trades.length - 1, 2));
  }

  @Test
  public void testProfitThreeTrades() {

    int[] trades = {5, 10, 15, 35, 5, 10, 45, 25, 15, 20};
    Assert.assertEquals(75, StockApp.maxProfit(trades, 0, trades.length - 1, 3));

    trades = new int[]{5, 10, 15, 35, 5, 10, 45, 25, 15, 20, 30};
    Assert.assertEquals(85, StockApp.maxProfit(trades, 0, trades.length - 1, 3));
  }

  @Test
  public void testProfitFourTrades() {

    int[] trades = new int[]{5, 10, 15, 35, 5, 10, 45, 25, 15, 20, 30, 1, 2};

    System.out.println("One trade: " + StockApp.maxProfit(trades, 0, trades.length - 1, 1));
    System.out.println("Two sample_trades: " + StockApp.maxProfit(trades, 0, trades.length - 1, 2));
    System.out.println("Three sample_trades: " + StockApp.maxProfit(trades, 0, trades.length - 1, 3));

    Assert.assertEquals(86, StockApp.maxProfit(trades, 0, trades.length - 1, 4));
  }
}
