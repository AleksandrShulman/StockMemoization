import org.junit.Assert;
import org.junit.Test;

/**
 * Created by aleks on 2/18/17.
 */
public class TestScalability {

  @Test
  public void testTradingOverhead() {



  }

  @Test
  public void testSingleTradeScalingTradeSize() {

    //5
    int[] trades = {5, 10, 20, 7, 9};

    Assert.assertEquals("Start experiment with counter at 0", 0, StockApp.getTotalUnitsOfWork());
    StockApp.maxProfit(trades, 1);

    int initialWorkUnits = StockApp.getTotalUnitsOfWork();

    //10
    trades = new int[]{5, 10, 20, 7, 9, 1, 2, 5, 4, 3};
    StockApp.resetTotalUnitsOfWork();
    StockApp.maxProfit(trades, 1);
    int doubleUnitsOfWork = StockApp.getTotalUnitsOfWork();

    // Use this information to do MX+b style determination to get linearity
    int x0 = 5;
    int x1 = 10;
    int x2 = 20;
    int y0 = initialWorkUnits;
    int y1 = doubleUnitsOfWork;

    // We can probably break this out into its own function to test
    float slope = ((float)y1 - (float)y0) / ((float)x1 - (float)x0);
    int constant = Math.round(y0 - (x0*slope));

    // If we double the # of items again, it should follow the formula
    int expectedUnitsOfWork = Math.round(slope * (x2) + Integer.valueOf(constant));

    float tolerance = .10f;

    trades = new int[]{5, 10, 20, 7, 9, 1, 2, 5, 4, 3, 5, 10, 30, 7, 9, 1, 12, 5, 4, 3};
    StockApp.resetTotalUnitsOfWork();
    StockApp.maxProfit(trades, 1);
    int quadUnitsOfWork = StockApp.getTotalUnitsOfWork();

    int lowerBound = Math.round((1 - tolerance) * expectedUnitsOfWork);
    int upperBound = Math.round((1 + tolerance) * expectedUnitsOfWork);
    Assert.assertTrue("Expected about " + expectedUnitsOfWork + " units of work, but got " + quadUnitsOfWork,
        quadUnitsOfWork > lowerBound && quadUnitsOfWork < upperBound);
  }



  @Test
  public void testSingleTradeScalingNumTransactions() {



  }

  @Test
  public void testTwoTradesScalingTradeSize() {



  }

  @Test
  public void testTwoTradesScalingNumTransactions() {


  }
}
