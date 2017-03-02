import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by aleks on 2/23/17.
 */
public class TestIncrementalBestTrade {

  //If we know what the best two trades in a range are, we should be able to tell
  //in O(1) time what the best two trades are in a slightly expanded range.


  @Test
  public void testIncrementalTrades() {

    int[] originalTrades = new int[]{1,7,2,7};
    int nextValue = 11;

    //This is the sort of oracle of trades
    StockCalculation sc = new StockCalculation(originalTrades, 2);
    List<Trade> bestTwoTrades = sc.findOptimalTrades().getTradeList();

    int[] incrementalList = new int[originalTrades.length + 1];
    for (int i = 0; i < originalTrades.length; i++) {

      incrementalList[i] = originalTrades[i];
    }

    incrementalList[originalTrades.length] = nextValue;

    for (Integer i : incrementalList) {
      System.out.print(i + " ");
    }

    sc = new StockCalculation(incrementalList, 2);

    //Here is a function that will take the current 2 best trades, and the incremental value (increasing)
    //and return the actual two best trades
    List<Trade> expectedNextBestTwoTrades = sc.findOptimalTrades().getTradeList();

    /*
     * Bridge the gap here. Find how to get the same results given the best current two trades
     */
    List<Trade> nextBestTwoTrades = bestTrades(bestTwoTrades, nextValue);


    for(Trade t : expectedNextBestTwoTrades) {
      System.out.println(t);
    }

    Assert.assertEquals(expectedNextBestTwoTrades, nextBestTwoTrades);

  }


  private List<Trade> bestTrades(List<Trade> trades, int nextValue) {

    return trades;
  }





}
