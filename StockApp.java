import java.util.ArrayList;
import java.util.List;

/**
 * Created by aleks on 9/20/16.
 */
public class StockApp {


  private static int totalUnitsOfWork = 0;
  public static int getTotalUnitsOfWork() {
    return totalUnitsOfWork;
  }

  public static void resetTotalUnitsOfWork() {
    StockApp.totalUnitsOfWork = 0;
  }

  static class TradeList {

    List<Trade> tradeList;
    TradeList() {
      this.tradeList = new ArrayList<Trade>();
    }

    public void add(Trade t) {
      this.tradeList.add(t);
    }

    public void addAll(TradeList trades) {
      for (Trade t: trades.getTradeList()) {
        this.tradeList.add(t);
      }
    }

    public List<Trade> getTradeList() {
      return this.tradeList;
    }

    public Trade get(int i) {
      return this.tradeList.get(i);
    }

    public int getTotalProfit() {
      int profit = 0;
      for (Trade t: this.tradeList) {
        profit += t.getProfit();
      }

      return profit;
    }
  }


  public static int maxProfit(final int[] trades, int numTransactions) {
    return maxProfit(trades, 0, Math.max(trades.length - 1, 0), numTransactions);
  }

  public static int maxProfit(final int[] stockPrices, int startIndex, int endingIndex, int numTransactions) {

    int profit = 0;
    TradeList bestTrades = optimalTradesRecursively(stockPrices, startIndex, endingIndex, numTransactions, null);
    for (Trade t : bestTrades.getTradeList()) {
      profit += t.getProfit();
    }

    return profit;
  }

  /**
   * Given a set of closing prices, return the set of stockPrices to maximize profit.
   * @param stockPrices
   * @param startIndex the day in which we can start the analysis (inclusive)
   * @param endingIndex the day in which the analysis ends (inclusive). It is defined to be
   *                    a valid entry into array.
   * @param numTransactions the number of trades (buy->sell) that we are trying to
   *                        optimize for
   * @param bestTradeMatrix a memoization table that describes the best possible trade in
   *                        a given range. bestTradeMatrix[i][j][k] represents the i trades
   *                        that can be made when a user can trade in the window represented
   *                        by day j and day k.
   * @return the list of the optional <numTransactions> trades that will produce the
   *         greatest total profit.
   */
  public static TradeList optimalTradesRecursively(final int[] stockPrices, int startIndex,
                                                     int endingIndex, int numTransactions,
                                                     TradeList[][][] bestTradeMatrix) {
    /**
     * 0 days -> 0 max transactions
     * 1 day  -> 1 max transaction (buy in the morning, sell in the evening)
     * 2 days -> 2 max transactions
     * 3 days -> 3 max transactions...
     *
     * So moral of the story, if the stockPrices.size < numTransactions, we can't process it...
     */
    if (stockPrices == null) {
      throw new IllegalArgumentException("trade info cannot be null");
    }

    if (startIndex == endingIndex) {
      TradeList tradeList = new TradeList();
      for (int i = 0; i < numTransactions; i++) {
        tradeList.add(new NilTrade());
      }
      return tradeList;
    }

    final int ARRAY_SIZE_TO_ACCOMMODATE = stockPrices.length;
    TradeList bestTrades = new TradeList();

    // Memoization
    // NOTE -- the index of numTransactions isn't 0-indexed
    if (bestTradeMatrix == null) {
      bestTradeMatrix = new TradeList[numTransactions+1][ARRAY_SIZE_TO_ACCOMMODATE][ARRAY_SIZE_TO_ACCOMMODATE];
    } else if (bestTradeMatrix[numTransactions][startIndex][endingIndex]!=null) {
      System.out.println("Hit for " + numTransactions +
          " transactions between " + startIndex + " and " + endingIndex + "!!");
        return bestTradeMatrix[numTransactions][startIndex][endingIndex];
    }

    if (numTransactions == 0) {
      return bestTrades;
    }

    if (numTransactions == 1) {
      /*
       * This is a base case where we go through the list of trades, in O(n) time to get the
       * single best transaction possible.
       *
       * Performance: O(n)
       */
      bestTrades.add(bestTradeInRangeIncreasingMemoized(stockPrices, startIndex,
          endingIndex, bestTradeMatrix));

    } else if (numTransactions == 2) {
      /*
       * This is a base case where we through the list of trades, in O(n), to get the
       * single 2 best transactions possible.
       *
       * First, we invoke the single-trade case ascending. O(n)
       * Then, we invoke the single-trade case descending. O(n)
       *
       * Then if we draw a line (of which O(n) positions are possible), and get the
       * best trade before and after that line, then if we were to sum those two lists
       * we'd get a third list that gives the best possible 2 trades.
       *
       * Performance: O(n)
       */
      findBestTwoTrades(startIndex, endingIndex, stockPrices, bestTradeMatrix, bestTrades);

    } else {
      /*
       * This is the general case (trades >=3 ), which uses the two base cases
       * Performance is O(n) * O(f(numTransactions - 1))
       */
      List<Trade> bestFirstTrade = new ArrayList<>();
      List<TradeList> bestRemainingTrades = new ArrayList<>();

      /**
       * Very important to discuss tradeBoundary.
       * It is the value for which the first trade may end and the second set of trades
       * may begin.
       *
       * Lots of potential for misunderstanding and OBO errors.
       *
       * Note that there is a +1 at the end of the range (which is not typical).
       * this is because we want to be inclusive of the last day. We want to
       * actually perform an operation on each member, and not just iterate
       * n times.
       */
      for (int tradeBoundary = startIndex; tradeBoundary < (endingIndex + 1); tradeBoundary++) {

        TradeList resultsFromSingleBestTrade = optimalTradesRecursively(stockPrices, startIndex, tradeBoundary, 1, bestTradeMatrix);
        if (resultsFromSingleBestTrade.getTradeList().size()!=1) {
          throw new IllegalStateException("Critical assumption violated!");
        }
        Trade t = resultsFromSingleBestTrade.get(0);
        bestFirstTrade.add(t);

        TradeList tradeList;
        if (tradeBoundary <= endingIndex) {

          //TODO: Test the effects of the recursive call here
          tradeList = optimalTradesRecursively(stockPrices, tradeBoundary, endingIndex,
              numTransactions - 1, bestTradeMatrix);
        } else {
          // So if we're asked to go beyond the bounds of what is normal, just add an empty list.
          tradeList = new TradeList();
        }
        bestRemainingTrades.add(tradeList);
      }

      // I think that the remaining trades are not respecting the boundary.
      // Indeed. Todo: Fix it.
      int bestIndex = getOptimalIndexForBoundary(bestFirstTrade, bestRemainingTrades);
      System.out.println("Best index is: " + bestIndex);
      bestTrades.add(bestFirstTrade.get(bestIndex));
      bestTrades.addAll(bestRemainingTrades.get(bestIndex));
    }

    if (bestTrades.getTradeList().size() != numTransactions) {
      throw new IllegalStateException("Expected " + numTransactions + " but returning " + bestTrades.getTradeList().size());
    }
    return bestTrades;
  }

  private static void findBestTwoTrades(int startIndex, int endingIndex, int[] stockPrices,
                                        TradeList[][][] bestTradeMatrix, TradeList bestTrades) {

    List<Trade> bestFirstTradeGivenBoundary = new ArrayList<>();
    List<Trade> bestSecondTradeGivenBoundary = new ArrayList<>();

    Trade optimalFirstTrade;
    Trade optimalSecondTrade;


    StockApp.totalUnitsOfWork += (endingIndex - startIndex);

    for(int rangeBoundary = startIndex; rangeBoundary < (endingIndex + 1); rangeBoundary++) {

      // 1st trade
      if (bestTradeMatrix[1][startIndex][rangeBoundary] != null) {
        System.out.println("Hit for 1 trade from " + startIndex + "," + rangeBoundary + "!");
        optimalFirstTrade = bestTradeMatrix[1][startIndex][rangeBoundary].get(0);

      } else {
        //TODO: Annotate w/# of trades
        System.out.println("[Miss] for 1 trade from " + startIndex + "," + rangeBoundary + "!");
        // This line might cause problems

        optimalFirstTrade = optimalTradesRecursively(stockPrices, startIndex,
            rangeBoundary, 1, bestTradeMatrix).get(0);

        // Memoize it
        TradeList memoizedTradeList = new TradeList();
        memoizedTradeList.add(optimalFirstTrade);
        bestTradeMatrix[1][startIndex][rangeBoundary] = memoizedTradeList;
      }

      //// 2nd trade
      if (bestTradeMatrix[1][rangeBoundary][stockPrices.length - 1] != null) {
        System.out.println("Hit for 1 trade from " + rangeBoundary + "," + (stockPrices.length - 1) + "!");
        optimalSecondTrade = bestTradeMatrix[1][rangeBoundary][stockPrices.length - 1].get(0);

      } else {
        System.out.println("[Miss] for 1 trade from " + rangeBoundary + "," + (stockPrices.length - 1) + "!");
        optimalSecondTrade = optimalTradesRecursively(stockPrices, rangeBoundary,
            stockPrices.length - 1, 1, bestTradeMatrix).get(0);

        // Memoize it
        TradeList memoizedTradeList = new TradeList();
        memoizedTradeList.add(optimalSecondTrade);
        bestTradeMatrix[1][rangeBoundary][stockPrices.length - 1] = memoizedTradeList;
      }

      bestFirstTradeGivenBoundary.add(optimalFirstTrade);
      bestSecondTradeGivenBoundary.add(optimalSecondTrade);
    }

    // Now get the index (i.e. boundary day) at which sample_trades are
    // maximized, and return those sample_trades

    List<TradeList> listOfLists = new ArrayList<>();
    for (Trade t : bestSecondTradeGivenBoundary) {
      TradeList list = new TradeList();
      list.add(t);
      listOfLists.add(list);
    }

    int bestIndex = getOptimalIndexForBoundary(bestFirstTradeGivenBoundary, listOfLists);
    bestTrades.add(bestFirstTradeGivenBoundary.get(bestIndex));
    bestTrades.add(bestSecondTradeGivenBoundary.get(bestIndex));

    //Memoize the result
    bestTradeMatrix[2][startIndex][endingIndex] = bestTrades;
  }

  // This is O(sample_trades)
  public static void maxProfitEachDayInRangeIncreasing(int[] trades, int startIndex, int endingIndex, int[] populatedProfilts) {

    int lowestPriceSeenSoFar = Integer.MAX_VALUE-1; //initialize to highest possible value
    int maximumProfitPossibleSoFar = 0;

    StockApp.totalUnitsOfWork += (endingIndex - startIndex);

    for (int day = startIndex; day<(endingIndex + 1); day++) {
      int result = updateMostMoney(maximumProfitPossibleSoFar, lowestPriceSeenSoFar, trades[day]);
      maximumProfitPossibleSoFar = Math.max(result, maximumProfitPossibleSoFar);
      populatedProfilts[day] = maximumProfitPossibleSoFar;
      if (trades[day] < lowestPriceSeenSoFar) {
        lowestPriceSeenSoFar = trades[day];
      }
    }
  }

  /**
   * Given a range and a set of memoized trades, it will find the best single
   * trade to make. This is the kernel of the application.
   * O(n)
   * @param trades array of closing ticker symbols, indexed by day, starting at day 0
   * @param startIndex analysing the trades starting from this day (inclusive)
   * @param endingIndex analysing the trades, ending at this day (inclusive)
   * @param bestTrades a memoization table that describes the best possible trade in
   *                   a given range. bestTradeMatrix[i][j] represents the best single
   *                   trade that can be made when a user can purchase a security on
   *                   day i and sell it up to day j.
   * @return the best single trade possible
   */
  public static Trade bestTradeInRangeIncreasingMemoized(int[] trades, int startIndex,
                                                         int endingIndex, TradeList[][][] bestTrades) {
    validateInputs(trades, startIndex, endingIndex);
    int ARRAY_SIZE_TO_ACCOMMODATE = trades.length;
    int numTransactions = 1;
    if (bestTrades == null) {
      bestTrades = new TradeList[numTransactions+1][ARRAY_SIZE_TO_ACCOMMODATE][ARRAY_SIZE_TO_ACCOMMODATE];
    }
    if (startIndex == endingIndex) {
      return new NilTrade();
    }

    if (bestTrades != null && bestTrades[1][startIndex][endingIndex] != null) {

      StockApp.totalUnitsOfWork += 1;
      System.out.println("Hit for 1 trade from " + startIndex + "," + endingIndex + "!");
      return bestTrades[1][startIndex][endingIndex].get(0);
    }

    System.out.println("Call to bestTradeInRangeIncreasingMemoized from " +
        startIndex + " to " + endingIndex + ", inclusive. This costs O(n). " +
        "Call increased from " + totalUnitsOfWork + " to " + ++totalUnitsOfWork);

    // Initializations
    int lowestPriceSeenSoFar = trades[startIndex]; //initialize to highest possible value
    int dayOfLowestPrice = startIndex;
    int dayOfHighestPrice = startIndex;

    Trade bestTrade = new Trade(dayOfLowestPrice, dayOfHighestPrice,
        trades[dayOfLowestPrice], trades[dayOfHighestPrice]);

    // Super-important: The loop's last iteration where day=endingIndex.
    // This is to produce the inclusive nature of the component.

    StockApp.totalUnitsOfWork += 2 * (endingIndex - startIndex);

    for (int day = startIndex; day <= (endingIndex); day++) {

      int currentPrice = trades[day];
      if (currentPrice - lowestPriceSeenSoFar > bestTrade.getProfit()) {

        bestTrade = new Trade(dayOfLowestPrice, day, lowestPriceSeenSoFar, currentPrice);
        // MEMOIZATION -- yikes
        System.out.println("About to memoize " + startIndex + ", " + day);
        TradeList wrapper = new TradeList();
        wrapper.add(bestTrade);
        bestTrades[1][startIndex][day] = wrapper;
      }

      // If today was a low day, we should have that data available
      if (trades[day] < lowestPriceSeenSoFar) {
        lowestPriceSeenSoFar = trades[day];
        dayOfLowestPrice = day;
      }
    }

    return bestTrade;
  }

  public static Trade bestTradeInRangeIncreasingMemoized(int[] trades, int startIndex, int endingIndex) {
    return bestTradeInRangeIncreasingMemoized(trades, startIndex, endingIndex, null);
  }

  public static Trade bestTradeInRangeDecreasingMemoized(int[] trades, int startIndex, int endingIndex) {
    return bestTradeInRangeDecreasingMemoized(trades, startIndex, endingIndex, null);
  }

  public static Trade bestTradeInRangeDecreasingMemoized(int[] trades, int startIndex,
                                                 int endingIndex, TradeList[][][] bestTrades ) {

    validateInputs(trades, startIndex, endingIndex);
    int ARRAY_SIZE_TO_ACCOMMODATE = trades.length;
    int numTransactions = 1;
    if (bestTrades == null) {
      bestTrades = new TradeList[numTransactions+1][ARRAY_SIZE_TO_ACCOMMODATE][ARRAY_SIZE_TO_ACCOMMODATE];
    }
    if (startIndex == endingIndex) {
      return new NilTrade();
    }

    if (bestTrades != null && bestTrades[1][startIndex][endingIndex] != null) {

      StockApp.totalUnitsOfWork += 1;
      System.out.println("Hit for 1 trade from " + startIndex + "," + endingIndex + "!");
      return bestTrades[1][startIndex][endingIndex].get(0);
    }
    System.out.println("Call to bestTradeInRangeDecreasing from " +
        startIndex + " to " + endingIndex + ". This costs O(n). " +
        "Call increased from " + totalUnitsOfWork + " to " + ++totalUnitsOfWork);

    if (startIndex == endingIndex) {
      return new NilTrade();
    }

    // Initializations
    int lowestPriceSeenSoFar = trades[startIndex]; //initialize to highest possible value
    int highestPriceSeenSoFar = trades[startIndex]; //initialize to highest possible value
    int dayOfLowestPrice = startIndex;
    int dayOfHighestPrice = startIndex;

    Trade bestTrade = new Trade(dayOfLowestPrice, dayOfHighestPrice,
        trades[dayOfLowestPrice], trades[dayOfHighestPrice]);

    StockApp.totalUnitsOfWork += (endingIndex - startIndex);

    for (int startingDay = endingIndex; startingDay >= startIndex; startingDay--) {
      int currentPrice = trades[startingDay];
      if (highestPriceSeenSoFar - currentPrice > bestTrade.getProfit()) {

        bestTrade = new Trade(dayOfLowestPrice, dayOfHighestPrice, currentPrice, highestPriceSeenSoFar);
      }

      // If today was a low day, we should have that data available
      if (trades[startingDay] > highestPriceSeenSoFar) {
        highestPriceSeenSoFar = trades[startingDay];
        dayOfHighestPrice = startingDay;
      }

      if (trades[startingDay] < lowestPriceSeenSoFar) {
        lowestPriceSeenSoFar = trades[startingDay];
        dayOfLowestPrice = startingDay;
      }
    }

    return bestTrade;
  }

  // This is O(n)
  public static void maxProfitEachDayInRangeDecreasing(int[] trades, int startIndex, int endingIndex, int[] populatedProfits) {

    StockApp.totalUnitsOfWork += (endingIndex - startIndex);

    int highestPriceSeenSoFar = -1; //initialize to highest possible value
    int maximumProfitPossibleSoFar = 0;
    for (int startingDay = endingIndex; startingDay>=startIndex; startingDay--) {
      int result = updateMostMoneyDesc(maximumProfitPossibleSoFar, highestPriceSeenSoFar, trades[startingDay]);
      if (result > maximumProfitPossibleSoFar) {
        maximumProfitPossibleSoFar = result;
      }

      populatedProfits[startingDay] = maximumProfitPossibleSoFar;
      if (trades[startingDay] > highestPriceSeenSoFar) {
        highestPriceSeenSoFar = trades[startingDay];
      }
    }
  }

  // This is O(n). This will discard the values it finds
  public static int maxProfitInRangeIncreasing(int[] trades, int startIndex, int endingIndex) {
    return maxProfitInRange(trades, true, startIndex, endingIndex);
  }

  // This is O(n). This will discard the values it finds
  public static int maxProfitInRangeDecreasing(int[] trades, int startIndex, int endingIndex) {
    return maxProfitInRange(trades, false, startIndex, endingIndex);
  }

  // O(n)
  private static int maxProfitInRange(int[] trades, boolean increasing, int startIndex, int endingIndex) {
    validateInputs(trades, startIndex, endingIndex);

    if (trades.length == 0 || startIndex == endingIndex) {
      return 0;
    }

    int maxSeen = -1;
    int[] output = new int[trades.length];

    // These are O(n)
    if (increasing) {
      maxProfitEachDayInRangeIncreasing(trades, startIndex, endingIndex, output);
    } else {
      maxProfitEachDayInRangeDecreasing(trades, startIndex, endingIndex, output);
    }

    // This is O(n)
    for (int max : output) {
      maxSeen = Math.max(max, maxSeen);
    }

    return maxSeen;
  }

  // The kernel of process. We have the 3 pieces of state we need. This is O(1)
  private static int updateMostMoney(int maxSeenSoFar, int lowestPriceSeenSoFar, int currentPrice) {

    if (currentPrice - lowestPriceSeenSoFar > maxSeenSoFar) {
      return currentPrice - lowestPriceSeenSoFar;
    }
    return maxSeenSoFar;
  }

  // The kernel of process. We have the 3 pieces of state we need. This is O(1)
  private static int updateMostMoneyDesc(int maxSeenSoFar, int highestPriceSeenSoFar, int currentPrice) {

    if (highestPriceSeenSoFar - currentPrice > maxSeenSoFar) {
      return highestPriceSeenSoFar - currentPrice;
    }
    return maxSeenSoFar;
  }

  static void validateInputs(int[] trades, int startIndex, int endingIndex) {

    // Validation edge cases
    if (trades.length == 0 && startIndex == 0) {
      return;
    }

    if (trades.length == 0 && endingIndex == 0) {
      return;
    }

    if (trades == null) {
      throw new IllegalStateException("trade data cannot be null");
    }

    if (endingIndex >= trades.length) {
      // The request has exceeded what is appropriate.
      throw new IllegalStateException("Request exceeds bounds");
    }

    if (startIndex < 0 || startIndex > endingIndex || endingIndex > trades.length) {
      throw new IllegalStateException("Invalid starting and/or ending bounds. startIndex: "
          + startIndex + " endingIndex: " + endingIndex + " arrayLength: " + trades.length);
    }
  }

  /**
   * O(n) algorithm for getting the index at which the sum of the item from the first list
   * with that of the second list produces the maximum value.
   * @param l1 first list
   * @param l2 second list
   * @return int - maximum sum
   */
  private static int getOptimalIndexForBoundary(List<Trade> l1, List<TradeList> l2) {

    if (l1.size() != l2.size()) {
      throw new IllegalArgumentException("lists vary in size!");
    }

    // Routine conversion from sample_trades to profits
    int[] array1 = new int[l1.size()];
    int[] array2 = new int[l2.size()];

    for(int i = 0; i<l1.size(); i++) {
      array1[i] = l1.get(i).getProfit();
      TradeList innerList2List = l2.get(i);
      array2[i] = innerList2List.getTotalProfit();
    }

    return getIndexOfMaxTwoArrayAtIndex(array1, array2);
  }

  /**
   * O(n) algorithm for finding the index at which the sum of
   * the values in array1 and array2 are optimal
   * @param array1 The first array of values
   * @param array2 The second array of values
   * @return The optimal index
   */
  private static int getIndexOfMaxTwoArrayAtIndex(int[] array1, int[] array2) {

    // O(n) computation
    totalUnitsOfWork += array1.length;

    int maxProfitSum = 0;
    int bestI = 0;
    for (int i=0; i < array1.length-1; i++) {

      int potentialProfitSum = array1[i] + array2[i];
      if (potentialProfitSum > maxProfitSum) {
        maxProfitSum = potentialProfitSum;
        bestI = i;
      }
    }

    return bestI;
  }
}
