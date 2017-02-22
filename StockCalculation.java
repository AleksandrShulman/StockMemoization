import java.util.ArrayList;
import java.util.List;

/**
 * Created by aleks on 9/20/16.
 */
public class StockCalculation {

  private int totalUnitsOfWork = 0;
  public int getTotalUnitsOfWork() {
    return totalUnitsOfWork;
  }

  static class TradeList {

    List<Trade> tradeList;

    TradeList() {
      this.tradeList = new ArrayList<>();
    }

    public void add(Trade t) {
      this.tradeList.add(t);
    }

    public void addAll(TradeList trades) {
      for (Trade t : trades.getTradeList()) {
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
      for (Trade t : this.tradeList) {
        profit += t.getProfit();
      }

      return profit;
    }
  }

  final int[] closingPrices;
  TradeList[][][] bestTradeMatrix;
  final int numTransactions;

  public StockCalculation(int[] closingPrices, int numTransactions) {

    this.closingPrices = closingPrices;
    this.numTransactions = numTransactions;

    final int ARRAY_SIZE = closingPrices.length;

    this.bestTradeMatrix = new TradeList[numTransactions+1][ARRAY_SIZE][ARRAY_SIZE];
  }

  public int calculateMaxProfit() {
    TradeList finalList = findOptimalTrades();
    return finalList.getTotalProfit();
  }

  public TradeList findOptimalTrades() {
    return findOptimalTrades(0, closingPrices.length - 1, numTransactions);
  }

  public TradeList findOptimalTrades(int startIndex, int endingIndex, int numTransactions) {
    /**
     * 0 days -> 0 max transactions
     * 1 day  -> 1 max transaction (buy in the morning, sell in the evening)
     * 2 days -> 2 max transactions
     * 3 days -> 3 max transactions...
     *
     * So moral of the story, if the stockPrices.size < numTransactions, we can't process it...
     */
    if (closingPrices == null) {
      throw new IllegalArgumentException("trade info cannot be null");
    }

    if (startIndex >= endingIndex) {
      TradeList tradeList = new TradeList();
      for (int i = 0; i < numTransactions; i++) {
        tradeList.add(new NilTrade());
      }
      return tradeList;
    }

    final int ARRAY_SIZE = closingPrices.length;
    TradeList bestTrades = new TradeList();

    // Memoization
    // NOTE -- the index of numTransactions isn't 0-indexed
    if (bestTradeMatrix == null) {
      bestTradeMatrix = new TradeList[numTransactions+1][ARRAY_SIZE][ARRAY_SIZE];
    } else if (bestTradeMatrix[numTransactions] != null) {
      if (bestTradeMatrix[numTransactions][startIndex] != null) {
        if (bestTradeMatrix[numTransactions][startIndex][endingIndex] != null) {
          System.out.println("Hit for " + numTransactions +
              " transactions between " + startIndex + " and " + endingIndex + "!!");
          return bestTradeMatrix[numTransactions][startIndex][endingIndex];
        }
      }
    }

    if (numTransactions == 0) {
      return bestTrades;
    }

    if (numTransactions == 1) {
      /*
       * This is a base case where we go through the list of closingPrices, in O(n) time to get the
       * single best transaction possible.
       *
       * Performance: O(n)
       */
      bestTrades.add(bestTradeInRangeIncreasingMemoized(startIndex, endingIndex));

    } else if (numTransactions == 2) {
      /*
       * This is a base case where we through the list of closingPrices, in O(n), to get the
       * single 2 best transactions possible.
       *
       * First, we invoke the single-trade case ascending. O(n)
       * Then, we invoke the single-trade case descending. O(n)
       *
       * Then if we draw a line (of which O(n) positions are possible), and get the
       * best trade before and after that line, then if we were to sum those two lists
       * we'd get a third list that gives the best possible 2 closingPrices.
       *
       * Performance: O(n)
       */
      findBestTwoTrades(startIndex, endingIndex, closingPrices, bestTradeMatrix, bestTrades);

    } else {
      /*
       * This is the general case (closingPrices >=3 ), which uses the two base cases
       * Performance is O(n) * O(f(numTransactions - 1))
       *
       * What is being done is we are splitting the problem into two parts.
       * We peel off one transaction, calculate that. And then repeat the problem
       * for (n-1) transactions, then add up the results in O(n) time.
       *
       */
      List<Trade> bestFirstTrade = new ArrayList<>();
      List<TradeList> bestRemainingTrades = new ArrayList<>();

      /**
       * Very important to discuss tradeBoundary.
       * It is the value for which the first trade may end and the second set of closingPrices
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

        TradeList resultsFromSingleBestTrade = findOptimalTrades(startIndex, tradeBoundary, 1);
        if (resultsFromSingleBestTrade.getTradeList().size()!=1) {
          throw new IllegalStateException("Critical assumption violated!");
        }
        Trade t = resultsFromSingleBestTrade.get(0);
        bestFirstTrade.add(t);

        TradeList tradeList;
        if (tradeBoundary <= endingIndex) {
          tradeList = findOptimalTrades(tradeBoundary, endingIndex, numTransactions - 1);
        } else {
          // So if we're asked to go beyond the bounds of what is normal, just add an empty list.
          tradeList = new TradeList();
        }
        bestRemainingTrades.add(tradeList);
      }

      int bestIndex = getOptimalIndexForBoundary(bestFirstTrade, bestRemainingTrades);
      bestTrades.add(bestFirstTrade.get(bestIndex));
      bestTrades.addAll(bestRemainingTrades.get(bestIndex));
    }

    if (bestTrades.getTradeList().size() != numTransactions) {
      throw new IllegalStateException("Expected " + numTransactions + " but returning " + bestTrades.getTradeList().size());
    }
    return bestTrades;
  }


  private void findBestTwoTrades(int startIndex, int endingIndex, int[] stockPrices,
                                        TradeList[][][] bestTradeMatrix, TradeList bestTrades) {

    List<Trade> bestFirstTradeGivenBoundary = new ArrayList<>();
    List<Trade> bestSecondTradeGivenBoundary = new ArrayList<>();

    Trade optimalFirstTrade;
    Trade optimalSecondTrade;

    totalUnitsOfWork += (endingIndex - startIndex);

    for(int rangeBoundary = startIndex; rangeBoundary < (endingIndex + 1); rangeBoundary++) {

      // 1st trade
      if (bestTradeMatrix[1][startIndex][rangeBoundary] != null) {
        System.out.println("Hit for 1 trade from " + startIndex + "," + rangeBoundary + "!");
        optimalFirstTrade = bestTradeMatrix[1][startIndex][rangeBoundary].get(0);

      } else {
        System.out.println("[Miss] for 1 trade from " + startIndex + "," + rangeBoundary + "!");
        optimalFirstTrade = findOptimalTrades(startIndex,
            rangeBoundary, 1).get(0);

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
        optimalSecondTrade = findOptimalTrades(rangeBoundary, stockPrices.length - 1, 1).get(0);

        // Memoize it
        TradeList memoizedTradeList = new TradeList();
        memoizedTradeList.add(optimalSecondTrade);
        bestTradeMatrix[1][rangeBoundary][stockPrices.length - 1] = memoizedTradeList;
      }

      // TODO: Add memoization?
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

  /**
   * Given a range and a set of memoized closingPrices, it will find the best single
   * trade to make. This is the kernel of the application.
   * O(n)
   * @param startIndex analysing the closingPrices starting from this day (inclusive)
   * @param endingIndex analysing the closingPrices, ending at this day (inclusive)
   * @return the best single trade possible
   */
  public Trade bestTradeInRangeIncreasingMemoized(int startIndex, int endingIndex) {
    validateInputs(closingPrices, startIndex, endingIndex);
    int ARRAY_SIZE_TO_ACCOMMODATE = closingPrices.length;
    int numTransactions = 1;
    if (startIndex == endingIndex) {
      return new NilTrade();
    }

    if (bestTradeMatrix != null && bestTradeMatrix[1][startIndex][endingIndex] != null) {

      totalUnitsOfWork += 1;
      System.out.println("Hit for 1 trade from " + startIndex + "," + endingIndex + "!");
      return bestTradeMatrix[1][startIndex][endingIndex].get(0);
    }

    if (bestTradeMatrix == null) {
      bestTradeMatrix = new TradeList[numTransactions+1][ARRAY_SIZE_TO_ACCOMMODATE][ARRAY_SIZE_TO_ACCOMMODATE];
    }

    // Initializations
    int lowestPriceSeenSoFar = closingPrices[startIndex]; //initialize to highest possible value
    int dayOfLowestPrice = startIndex;
    int dayOfHighestPrice = startIndex;

    Trade bestTrade = new Trade(dayOfLowestPrice, dayOfHighestPrice,
        closingPrices[dayOfLowestPrice], closingPrices[dayOfHighestPrice]);

    totalUnitsOfWork += 2 * (endingIndex - startIndex);

    for (int day = startIndex; day <= (endingIndex); day++) {
      int currentPrice = closingPrices[day];
      if (currentPrice - lowestPriceSeenSoFar > bestTrade.getProfit()) {

        bestTrade = new Trade(dayOfLowestPrice, day, lowestPriceSeenSoFar, currentPrice);
        // MEMOIZATION
        System.out.println("About to memoize " + startIndex + ", " + day);
      }

      TradeList wrapper = new TradeList();
      wrapper.add(bestTrade);
      bestTradeMatrix[1][startIndex][day] = wrapper;

      // If today was a low day, we should have that data available
      if (closingPrices[day] < lowestPriceSeenSoFar) {
        lowestPriceSeenSoFar = closingPrices[day];
        dayOfLowestPrice = day;
      }
    }

    return bestTrade;
  }

  public Trade bestTradeInRangeDecreasingMemoized(int startIndex, int endingIndex) {
    validateInputs(closingPrices, startIndex, endingIndex);
    int ARRAY_SIZE_TO_ACCOMMODATE = closingPrices.length;
    int numTransactions = 1;

    if (startIndex == endingIndex) {
      return new NilTrade();
    }

    if (bestTradeMatrix != null && bestTradeMatrix[1][startIndex][endingIndex] != null) {
      totalUnitsOfWork += 1;
      System.out.println("Hit for 1 trade from " + startIndex + "," + endingIndex + "!");
      return bestTradeMatrix[1][startIndex][endingIndex].get(0);
    }

    if (bestTradeMatrix == null) {
      bestTradeMatrix = new TradeList[numTransactions+1][ARRAY_SIZE_TO_ACCOMMODATE][ARRAY_SIZE_TO_ACCOMMODATE];
    }

    if (startIndex == endingIndex) {
      return new NilTrade();
    }

    // Initializations
    int highestPriceSeenSoFar = closingPrices[endingIndex]; //initialize to highest possible value
    int dayOfLowestPrice = endingIndex;
    int dayOfHighestPrice = endingIndex;

    Trade bestTrade = new Trade(dayOfLowestPrice, dayOfHighestPrice,
        closingPrices[dayOfLowestPrice], closingPrices[dayOfHighestPrice]);

    totalUnitsOfWork += (endingIndex - startIndex);

    for (int startingDay = endingIndex; startingDay >= startIndex; startingDay--) {
      int currentPrice = closingPrices[startingDay];
      if (highestPriceSeenSoFar - currentPrice > bestTrade.getProfit()) {

        bestTrade = new Trade(startingDay, dayOfHighestPrice, currentPrice, highestPriceSeenSoFar);
      }

      TradeList wrapper = new TradeList();
      wrapper.add(bestTrade);
      bestTradeMatrix[1][startIndex][endingIndex] = wrapper;
      // If today was a low day, we should have that data available
      if (closingPrices[startingDay] > highestPriceSeenSoFar) {
        highestPriceSeenSoFar = closingPrices[startingDay];
        dayOfHighestPrice = startingDay;
      }
    }

    return bestTrade;
  }

  static void validateInputs(int[] trades, int startIndex, int endingIndex) {

    if (trades == null) {
      throw new IllegalStateException("trade data cannot be null");
    }

    // Validation edge cases
    if (trades.length == 0 && startIndex == 0) {
      return;
    }

    if (trades.length == 0 && endingIndex == 0) {
      return;
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
  private int getOptimalIndexForBoundary(List<Trade> l1, List<TradeList> l2) {

    //TODO: Open question - should we be populating these into the bestmatrix data structure as we go along?
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
  private int getIndexOfMaxTwoArrayAtIndex(int[] array1, int[] array2) {

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
