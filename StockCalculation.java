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

    TradeList(Trade t) {
      this.tradeList = new ArrayList<>();
      tradeList.add(t);
    }

    TradeList(TradeList tl) {

      this.tradeList = new ArrayList<>(tl.getTradeList().size());
      for(Trade t : tl.getTradeList()) {

        tradeList.add(t);
      }
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

    public void remove(Trade t) {
      this.tradeList.remove(t);
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

    // Memoization - NOTE -- the index of numTransactions isn't 0-indexed
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
      Trade increasingTrade = bestTradeInRangeIncreasingMemoized(startIndex, endingIndex);
      bestTrades.add(increasingTrade);

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
      findBestTwoTrades(startIndex, endingIndex, bestTrades);

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

  //TODO: Refactor to remove stockPrices, since this is a member variable.
  // It's private anyway, so it's not really built to be tested.
  private void findBestTwoTrades(int startIndex, int endingIndex,
                                 TradeList bestTrades) {

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
      if (bestTradeMatrix[1][rangeBoundary][closingPrices.length - 1] != null) {
        System.out.println("Hit for 1 trade from " + rangeBoundary + "," + (closingPrices.length - 1) + "!");
        optimalSecondTrade = bestTradeMatrix[1][rangeBoundary][closingPrices.length - 1].get(0);

      } else {
        System.out.println("[Miss] for 1 trade from " + rangeBoundary + "," + (closingPrices.length - 1) + "!");
        optimalSecondTrade = findOptimalTrades(rangeBoundary, closingPrices.length - 1, 1).get(0);

        // Memoize it
        TradeList memoizedTradeList = new TradeList();
        memoizedTradeList.add(optimalSecondTrade);
        bestTradeMatrix[1][rangeBoundary][closingPrices.length - 1] = memoizedTradeList;
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

  //TODO: Create a version of this that works for n transactions, instead of just one
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

    // Note: If running both the increasing and decreasing calculation, the result
    // should be the same. However, if it's already set, it'll just return it.
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
      bestTradeMatrix[1][startingDay][endingIndex] = wrapper;
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

  /**
   * Strategy:
   * For each value to be seen, keep computing the best trade that could be generated.
   * If that trade is better than the less profitable trade, then replace that trade
   * with this one and make the other trade the less profitable one.
   *
   * Second to that:
   *
   * As an edge case, we could get a single value that is higher than the sell price of the
   * chronolically latest trade - in that case, we could just sell for the newer price.
   * What if we include that data point as the lowest price seen when we initialize?
   *
   * Problem: There are still O(n^2) spots for all this.
   *
   */
  // O(n)

  public void populateBestTwoTradesList() {

    int ARRAY_SIZE_TO_ACCOMMODATE = closingPrices.length;
    int numTransactions = 2;

    if (bestTradeMatrix == null) {
      bestTradeMatrix = new TradeList[numTransactions+1][ARRAY_SIZE_TO_ACCOMMODATE][ARRAY_SIZE_TO_ACCOMMODATE];
    }

    //In O(n) time, we can initialize this array of two trades
    // TODO: Figure out what to do between 0 - 3. That part of the memoization Matrix will be empty otherwise.
    final TradeList nilTwoTradeList = new TradeList();
    nilTwoTradeList.add(new NilTrade());
    nilTwoTradeList.add(new NilTrade());

    for(int i = 0; i < closingPrices.length; i++) {
      if (bestTradeMatrix[2][i][i] != null) {
        bestTradeMatrix[2][i][i] = nilTwoTradeList;
      }
    }

    TradeList bestTwoTrades = new TradeList();
    findBestTwoTrades(0, 3, bestTwoTrades);

    Trade lessProfitableTrade = getLessProfitableTrade(bestTwoTrades.getTradeList());

    // Perform an O(1) calculation, n times
    int minSeen = -1;
    int dayOfLowestPrice = 3;
    int dayOfHighestPrice = 3;
    int maxProfitFromOneMoreTrade = 0;
    Trade bestTrade = null;
    for(int i=3; i<closingPrices.length; i++) {

      // TODO: Deal w/the possibility of the case where we have another value that will
      // invalidate the last trade

      if(closingPrices[i] - minSeen > maxProfitFromOneMoreTrade) {
        dayOfHighestPrice = i;
        maxProfitFromOneMoreTrade = (closingPrices[i] - minSeen);
        bestTrade = new Trade(dayOfLowestPrice, dayOfHighestPrice,
            closingPrices[dayOfLowestPrice], closingPrices[dayOfHighestPrice]);
      }

      if(closingPrices[i] < minSeen) {
        minSeen = closingPrices[i];
      }

      // One more step here. Now we need to see what happens if this trade
      // is better than either of the existing trades

      if (bestTrade.getProfit() > lessProfitableTrade.getProfit()) {
        bestTwoTrades.remove(lessProfitableTrade);
        bestTwoTrades.add(bestTrade);
        lessProfitableTrade = getLessProfitableTrade(bestTwoTrades.getTradeList());
      }

      //At each juncture just remember which two trades were best
      bestTradeMatrix[2][0][i] = new TradeList(bestTwoTrades);
    }


  }




  public TradeList bestTwoTradesIncreasing(TradeList currentBestTwoTrades, int nextValue,
                                           int minimumValueSeen, int maximumValueSeen) {

    Trade lessProfitableTrade = getLessProfitableTrade(currentBestTwoTrades.getTradeList());

    //TODO: Find out what things we need to have.

    // Let's say I have the minimum value seen.
    // Let's say I have the maximum value seen. Can I do anything with these two?

    // Let's define the best two trades (t1: t1_buy, t1_sell, t2_buy, t2_sell).

    // Examples where the best two trades would change:
    // [10, 20, 9, 18]
    // t1_buy = 10, t1_sell = 20, t2_buy = 9, t2_sell = 18

    // What if the next value is 6 (no change)
    // What if the value after that is 60 (then we'd have to reverse the trade that was least profitable

    // A thought: You could proceed as if the max profit seen so far is the profit from the minimum trade.
    // That way, the next trade opportunity would present itself. This folds nicely into the existing logic.

    // What about edge cases, such as:
    // a. [10, 20, 9, 18, 19]
    // And how would the response be different from:
    // b. [9, 18, 10, 20, 19]
    // In the first case, the 19 would replace the 18. In the second case, we couldn't do that b/c we'd
    // need to override the previous trade.

    // So, we'd need to drag around a ton of state, but if evaluating that state is O(1), then it'll work!

    // To get the right answer for a:
    // You'd need:
    // 1. Profit of smaller trade:
    // 2. Profit of larger trade (eventually, as it becomes the smaller one)
    // 3. Smallest value seen going forward
    // 4. Current value
    // Strategy: You'd calculate profit, check that profit against the smaller trade, and then adjust accordingly.

    // To get the right answer for b:
    //
    // Trade t = bestTradeInRangeIncreasingMemoized()
    return null;

  }

  // O(trades)
  private Trade getLessProfitableTrade(List<Trade> trades) {

    Trade returnTrade = trades.get(0);
    for (Trade t : trades) {
      if (t.getProfit() > returnTrade.getProfit()) {

        returnTrade = t;
      }
    }

    return returnTrade;
  }

}
