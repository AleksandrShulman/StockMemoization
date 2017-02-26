# StockMemoization
Application to calculate optimal profit from N trades over M days of trading

The runtime is going to be O(n^(transactions)), for transactions > 2.
So finding the best single trade is O(n).
Finding the best two trades is O(n).
However, finding best 3 trades is O(n^2).

If there is a way to get around this through some clever memoization, I'd be super-curious to find out how. So far, all my memoization strategies have come up short b/c there is no O(1) way to find the best 2 trades in range [0..m], m < n, given that you know the best single trade in [0..m], and perhaps other info.

A promising strategy might be to take the same approach we did for one trade by walking through the list and seeing if the next value would make an improvement to the existing trade. Now -- we need to see if it would make an improvement to either of the existing 2 trades already figured out. 

