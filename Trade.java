/**
 * An action that occur
 */
public class Trade {

  final private Integer buyDay;
  final private Integer sellDay;
  final private Integer buyPrice;
  final private Integer sellPrice;
  final private Integer profit;

  protected Trade() {
    this.profit = 0;
    buyDay = null;
    sellDay = null;
    buyPrice = null;
    sellPrice = null;
  }

  public Trade(int buyDay, int sellDay, int buyPrice, int sellPrice) {

    if (buyDay > sellDay) {
      throw new IllegalArgumentException("It does not make sense to buy after selling!");
    }

    this.buyDay = buyDay;
    this.sellDay = sellDay;
    this.buyPrice = buyPrice;
    this.sellPrice = sellPrice;
    this.profit = this.sellPrice - this.buyPrice;
  }

  public Integer getProfit() {

    return this.profit;
  }

  @Override
  public boolean equals(Object a) {

    if (! (a instanceof Trade)) {
      return false;
    }

    if (this.getBuyDay() != ((Trade) a).getBuyDay()) {
      return false;
    }

    if (this.getSellDay() != ((Trade) a).getSellDay()) {
      return false;
    }

    if (this.profit != ((Trade) a).profit) {
      return false;
    }

    if (this.buyPrice != ((Trade) a).buyPrice) {
      return false;
    }

    if (this.sellPrice != ((Trade) a).sellPrice) {
      return false;
    }

    return true;

  }

  @Override
  public String toString() {
    return "Buy on day " + getBuyDay() + " for $" + buyPrice + " and sell on " + getSellDay() + " for $"
        + sellPrice + " with profit $" + profit;
  }

  public Integer getBuyDay() {
    return buyDay;
  }

  public Integer getSellDay() {
    return sellDay;
  }
}
