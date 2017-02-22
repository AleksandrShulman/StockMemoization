import org.junit.Assert;
import org.junit.Test;

/**
 * Created by aleks on 9/20/16.
 */
public class TestStockApp {

  final int[] sample_trades = {3, 10, 15, 35, 5, 10};

  @Test
  public void testFindBestTrade() {

    final Integer EXPECTED_LOWEST_PRICE_DAY = 0;
    final Integer EXPECTED_LOWEST_PRICE = 3;
    final Integer EXPECTED_HIGHEST_PRICE_DAY = 3;
    final Integer EXPECTED_HIGHEST_PRICE = 35;
    final Integer EXPECTED_PROFIT = EXPECTED_HIGHEST_PRICE - EXPECTED_LOWEST_PRICE;

    Trade result = StockCalculation.bestTradeInRangeIncreasingMemoized(sample_trades, 0, sample_trades.length - 1);

    Assert.assertEquals(EXPECTED_PROFIT, result.getProfit());
    Assert.assertEquals(EXPECTED_LOWEST_PRICE_DAY, result.getBuyDay());
    Assert.assertEquals(EXPECTED_HIGHEST_PRICE_DAY, result.getSellDay());
  }

  @Test
  public void testRangeDecreasingAndRangeIncreasingEqual() {

    Trade result = StockCalculation.bestTradeInRangeIncreasingMemoized(sample_trades, 0, sample_trades.length - 1);
    Trade result2 = StockCalculation.bestTradeInRangeDecreasingMemoized(sample_trades, 0, sample_trades.length - 1);

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

    Trade result = StockCalculation.bestTradeInRangeIncreasingMemoized(trades, 0, trades.length - 1);

    Assert.assertEquals(EXPECTED_PROFIT, result.getProfit());
    Assert.assertEquals(EXPECTED_BUY_PRICE_DAY, result.getBuyDay());
    Assert.assertEquals(EXPECTED_SELL_PRICE_DAY, result.getSellDay());
  }

  @Test
  public void testEmpty() {

    int EXPECTED_RESULT = 0;
    Assert.assertEquals(EXPECTED_RESULT, StockCalculation.maxProfit(new int[]{}, 0, 0, 2));
  }

  @Test
  public void testMaxProfitInRangeIncreasing() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    Assert.assertEquals(30, StockCalculation.maxProfit(trades, 1));

    trades = new int[]{5, 10};
    Assert.assertEquals(5, StockCalculation.maxProfit(trades, 1));

    trades = new int[]{5};
    Assert.assertEquals(0, StockCalculation.maxProfit(trades, 1));

    trades = new int[]{};
    Assert.assertEquals(0, StockCalculation.maxProfit(trades, 1));
  }

  @Test
  public void testMaxProfitInRangeDecreasing() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    Assert.assertEquals((Integer)30,
        StockCalculation.bestTradeInRangeDecreasingMemoized(trades, 0, trades.length - 1).getProfit());

    trades = new int[]{5, 10};
    Assert.assertEquals((Integer)5,
        StockCalculation.bestTradeInRangeDecreasingMemoized(trades, 0, trades.length - 1).getProfit());

    trades = new int[]{5};
    Assert.assertEquals((Integer)0,
        StockCalculation.bestTradeInRangeDecreasingMemoized(trades, 0, trades.length - 1).getProfit());
  }

  @Test
  public void testEmptyTradesListDecreasing() {

    int[] trades = new int[]{};
    Assert.assertEquals((Integer)0,
        StockCalculation.bestTradeInRangeDecreasingMemoized(trades, 0, Math.max(trades.length - 1, 0)).getProfit());
  }

  @Test
  public void testMaxProfitInRangeEquivalent() {

    int[] smallTrades = {35, 5, 10, 25};
    int smallIncreasingProfit =
        StockCalculation.bestTradeInRangeIncreasingMemoized(smallTrades, 0, smallTrades.length - 1).getProfit();
    int smallDecreasingProfit =
        StockCalculation.bestTradeInRangeDecreasingMemoized(smallTrades, 0, smallTrades.length - 1).getProfit();

    Assert.assertEquals(smallIncreasingProfit, smallDecreasingProfit);

    int[] trades = {5, 10, 15, 35, 5, 10, 25, 40, 10, 25, 5};
    int increasingProfit = StockCalculation.bestTradeInRangeIncreasingMemoized(trades, 3, 6).getProfit();
    int decreasingProfit = StockCalculation.bestTradeInRangeDecreasingMemoized(trades, 3, 6).getProfit();
    Assert.assertEquals(increasingProfit, decreasingProfit);
  }

  @Test
  public void testProfitSingleTrade() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    Assert.assertEquals(30, StockCalculation.maxProfit(trades, 1));
  }

  @Test
  public void testProfitTwoTrades() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    Assert.assertEquals(35, StockCalculation.maxProfit(trades, 0, trades.length - 1, 2));

    trades = new int[]{5, 10, 15, 35, 5, 10, 45, 25, 15, 20};
    Assert.assertEquals(70, StockCalculation.maxProfit(trades, 0, trades.length - 1, 2));
  }

  @Test
  public void testProfitThreeTrades() {

    int[] trades = {5, 10, 15, 35, 5, 10, 45, 25, 15, 20};
    Assert.assertEquals(75, StockCalculation.maxProfit(trades, 0, trades.length - 1, 3));

    trades = new int[]{5, 10, 15, 35, 5, 10, 45, 25, 15, 20, 30};
    Assert.assertEquals(85, StockCalculation.maxProfit(trades, 0, trades.length - 1, 3));
  }

  @Test
  public void testProfitFourTrades() {

    int[] trades = new int[]{5, 10, 15, 35, 5, 10, 45, 25, 15, 20, 30, 1, 2};

    System.out.println("One trade: " + StockCalculation.maxProfit(trades, 0, trades.length - 1, 1));
    System.out.println("Two sample_trades: " + StockCalculation.maxProfit(trades, 0, trades.length - 1, 2));
    System.out.println("Three sample_trades: " + StockCalculation.maxProfit(trades, 0, trades.length - 1, 3));

    Assert.assertEquals(86, StockCalculation.maxProfit(trades, 0, trades.length - 1, 4));
  }
}
