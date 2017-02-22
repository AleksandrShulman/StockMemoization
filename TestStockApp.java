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

    StockCalculation sc = new StockCalculation(sample_trades, 1);
    Trade result = sc.findOptimalTrades().get(0);

    Assert.assertEquals(EXPECTED_PROFIT, result.getProfit());
    Assert.assertEquals(EXPECTED_LOWEST_PRICE_DAY, result.getBuyDay());
    Assert.assertEquals(EXPECTED_HIGHEST_PRICE_DAY, result.getSellDay());
  }

  @Test
  public void testRangeDecreasingAndRangeIncreasingEqual() {

    StockCalculation sc = new StockCalculation(sample_trades, 1);
    Trade result = sc.bestTradeInRangeIncreasingMemoized(0, sample_trades.length - 1);
    Trade result2 = sc.bestTradeInRangeDecreasingMemoized(0, sample_trades.length - 1);

    Assert.assertEquals("Increasing and decreasing closingPrices different", result, result2);
  }

  @Test
  public void testFindBestTrade2() {

    //TODO: Fold into previous test
    int[] trades = {10, 30, 15, 35, 5, 75};
    final Integer EXPECTED_BUY_PRICE_DAY = 4;
    final Integer EXPECTED_BUY_PRICE = 5;
    final Integer EXPECTED_SELL_PRICE_DAY = 5;
    final Integer EXPECTED_SELL_PRICE = 75;
    final Integer EXPECTED_PROFIT = EXPECTED_SELL_PRICE - EXPECTED_BUY_PRICE;

    StockCalculation sc = new StockCalculation(trades, 1);
    Trade result = sc.bestTradeInRangeIncreasingMemoized(0, trades.length - 1);

    Assert.assertEquals(EXPECTED_PROFIT, result.getProfit());
    Assert.assertEquals(EXPECTED_BUY_PRICE_DAY, result.getBuyDay());
    Assert.assertEquals(EXPECTED_SELL_PRICE_DAY, result.getSellDay());
  }

  @Test
  public void testEmpty() {

    Integer EXPECTED_RESULT = 0;
    StockCalculation sc = new StockCalculation(new int[]{}, 2);
    Assert.assertEquals(EXPECTED_RESULT, sc.findOptimalTrades(0,0,2).get(0).getProfit());
  }

  @Test
  public void testMaxProfitInRangeIncreasing() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    StockCalculation sc = new StockCalculation(trades, 1);
    Assert.assertEquals(30, sc.calculateMaxProfit());

    trades = new int[]{5, 10};
    sc = new StockCalculation(trades, 1);
    Assert.assertEquals(5, sc.calculateMaxProfit());

    trades = new int[]{5};
    sc = new StockCalculation(trades, 1);
    Assert.assertEquals(0, sc.calculateMaxProfit());

    trades = new int[]{};
    sc = new StockCalculation(trades, 1);
    Assert.assertEquals(0, sc.calculateMaxProfit());
  }

  @Test
  public void testMaxProfitInRangeDecreasing() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    StockCalculation sc = new StockCalculation(trades, 1);
    Assert.assertEquals((Integer) 30,
        sc.bestTradeInRangeDecreasingMemoized(0, trades.length - 1).getProfit());

    trades = new int[]{5, 10};
    sc = new StockCalculation(trades, 1);
    Assert.assertEquals((Integer)5,
        sc.bestTradeInRangeDecreasingMemoized(0, trades.length - 1).getProfit());

    trades = new int[]{5};
    sc = new StockCalculation(trades, 1);
    Assert.assertEquals((Integer)0,
        sc.bestTradeInRangeDecreasingMemoized(0, trades.length - 1).getProfit());
  }

  @Test
  public void testProfitSingleTrade() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    StockCalculation sc = new StockCalculation(trades, 1);
    Assert.assertEquals(30, sc.calculateMaxProfit());
  }

  @Test
  public void testProfitTwoTrades() {

    int[] trades = {5, 10, 15, 35, 5, 10};
    StockCalculation sc = new StockCalculation(trades, 2);
    Assert.assertEquals(35, sc.calculateMaxProfit());

    trades = new int[]{5, 10, 15, 35, 5, 10, 45, 25, 15, 20};
    sc = new StockCalculation(trades, 2);
    Assert.assertEquals(70, sc.calculateMaxProfit());
  }

  @Test
  public void testProfitThreeTrades() {

    int[] trades = {5, 10, 15, 35, 5, 10, 45, 25, 15, 20};
    StockCalculation sc = new StockCalculation(trades, 3);
    Assert.assertEquals(75, sc.calculateMaxProfit());

    trades = new int[]{5, 10, 15, 35, 5, 10, 45, 25, 15, 20, 30};
    sc = new StockCalculation(trades, 3);
    Assert.assertEquals(85, sc.calculateMaxProfit());
  }

  @Test
  public void testProfitFourTrades() {

    int[] trades = new int[]{5, 10, 15, 35, 5, 10, 45, 25, 15, 20, 30, 1, 2};
    StockCalculation sc = new StockCalculation(trades, 1);

    System.out.println("One trade: " + sc.calculateMaxProfit());
    sc = new StockCalculation(trades, 2);
    System.out.println("Two sample_trades: " + sc.calculateMaxProfit());

    sc = new StockCalculation(trades, 3);
    System.out.println("Three sample_trades: " + sc.calculateMaxProfit());

    sc = new StockCalculation(trades, 4);
    Assert.assertEquals(86, sc.calculateMaxProfit());
  }
}
